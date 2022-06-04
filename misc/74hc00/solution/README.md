# Solution

The name of the challenge and its description suggests that it is related to a 74HC00 chip which is an implementation of a NAND circuit. The 74HC00 chip contains 4 NAND gates offering an input of 8 bits (byte) to be translated to 4 bits (nybble).

Participants must feed given bytes (unhexed content) to a 74HC00 chip implementation to translate them into a series of nybbles which will represent the flag.

```python3
Python 3.8.10 (default, Jun  2 2021, 10:49:15) 
[GCC 9.4.0] on linux
Type "help", "copyright", "credits" or "license" for more information.
>>> content = 
KeyboardInterrupt
>>> from binascii import unhexlify
>>> content = unhexlify('efebc39fe3fdcbd4f3ffc3cbc381c2fbfbffc9cfcb7fe7bccaf8f7ffebf1d73fdbfcd74fcb1fd7cddb63dbe9e3edfbffc3bde6f6fbffd77de7fbcbf5d4efd3cbd175d33dfb8de3abf4f0f4f1ebdff70dca7ef9ffc2dcd5fbec44ef17cffdcf8befcff76ec17cfaffe2ddc5fbdc41c3e1f2efd9dff8f5d2f6fb8cf2cfdb2bcbeff34eca3ef2ffd8ced2fbed64cb72f2ffe30cc4ffd1eed4cff0f0e9fbd873e23bc24e')
>>> def nand(b1, b2):
...     return "0" if b1 == "1" and b2 == "1" else "1" 
... 
>>> def chip(bits):
...     return ''.join([nand(bits[i], bits[i+1]) for i in range(0, len(bits), 2)])
... 
>>> hexr = ''
>>> for i in content:
...     bits = format(i, "08b")
...     bit_result = format(int(chip(bits), 2), "x")
...     hexr += bit_result
... 
>>> hexr
'466c616720666f722074686973206368616c6c656e67652069732069626374667b692d6e3333642d793075725f4e414e442d793075725f67347433732d346e642d793075725f6b306d70757433727a7a7d'
>>> from binascii import unhexlify
>>> unhexlify(hexr)
b'Flag for this challenge is ibctf{i-n33d-y0ur_NAND-y0ur_g4t3s-4nd-y0ur_k0mput3rzz}'
```
