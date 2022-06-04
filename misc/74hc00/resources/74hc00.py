from string import printable
from binascii import hexlify, unhexlify
from random import choice

def int_to_bits(i):
    return format(i, "08b")

def char_to_bits(char):
    return int_to_bits(ord(char))

def bits_to_hex(bits):
    return format(int(bits, 2), "x")

def hex_to_int(h):
    return int(h, 16)

def nand(b1, b2):
    return "0" if b1 == "1" and b2 == "1" else "1"

def chip(bits):
    return ''.join([nand(bits[i], bits[i+1]) for i in range(0, len(bits), 2)])

def chip_map():
    chip_list = []

    for i in range(16):
        chip_list.append([])

    for i in range(256):
        bits = int_to_bits(i)
        bit_result = bits_to_hex(chip(bits))
        chip_list[hex_to_int(bit_result)].append(i)

    return chip_list

def c74hc00_reverse(content):
    chip_list = chip_map()
    hexcontent_str = hexlify(content).decode()
    result = b''

    for i in hexcontent_str:
        nybble = hex_to_int(i)
        randbyte = bytes([choice(chip_list[nybble])])
        result += randbyte

    return result

def c74hc00(content):
    hexresult = ''

    for i in content:
        bits = int_to_bits(i)
        bit_result = bits_to_hex(chip(bits))
        hexresult += bit_result

    return hexresult


def convert_flag():
    flag = b"Flag for this challenge is ibctf{i-n33d-y0ur_NAND-y0ur_g4t3s-4nd-y0ur_k0mput3rzz}"
    return c74hc00_reverse(flag)

if __name__ == "__main__":
    converted = convert_flag()
    print(converted)
    print(hexlify(converted))
    hexresult = c74hc00(converted)
    print(hexresult)
    print(unhexlify(hexresult))
