# Solution

Since this is a programming challenge, participants must read the documentation and write code to extract the data from slots to get the flag.

```sh
$ python3 solution.py 
s: b'R\x00\x00\x00\x00\x00si' => r: b'o\xf9\xe8\xd7\xc6\xe5x\x86p\xfdi\xc0\xfe\xf6\xd5\x87'
ticket: b'\xf9\xe8\xd7\xc6\xe5'
s: b'R\xf9\xe8\xd7\xc6\xe5re' => r: b'o\n\x11<\x93\x02#\x9b\x10\xab\x0cV\xa2\x9f\xdb\xda'
slot list: [{'taken': False, 'datasize': 10, 'start_addr': b'\x11<'}, {'taken': True, 'datasize': 19, 'start_addr': b'\x02#'}, {'taken': True, 'datasize': 27, 'start_addr': b'\x10\xab'}, {'taken': False, 'datasize': 12, 'start_addr': b'V\xa2'}, {'taken': True, 'datasize': 31, 'start_addr': b'\xdb\xda'}]
s: b'R\xf9\xe8\xd7\xc6\xe5rs\x02#' => r: b'o9ibctf{n0fl33\x87:'
s: b'R\xf9\xe8\xd7\xc6\xe5rs\x87:' => r: b'o\x03t-th3-fl4g}\x00\xac3'
slot data: b'ibctf{n0t-th3-fl4g}\x00'
s: b'R\xf9\xe8\xd7\xc6\xe5rs\x10\xab' => r: b'ohibctf{th1s_1\x83\x11'
s: b'R\xf9\xe8\xd7\xc6\xe5rs\x83\x11' => r: b'o\xbas-n0t-th4ub3z\x9b'
s: b'R\xf9\xe8\xd7\xc6\xe5rsz\x9b' => r: b'o\x063-fl4g}\x00\x00\x00\x00\x002\x12'
slot data: b'ibctf{th1s_1s-n0t-th3-fl4g}\x00\x00\x00\x00\x00'
s: b'R\xf9\xe8\xd7\xc6\xe5rs\xdb\xda' => r: b'o+ibctf{369_l3v\xa5'
s: b'R\xf9\xe8\xd7\xc6\xe5rsv\xa5' => r: b'o8t-_s33-y0ur7wj'
s: b'R\xf9\xe8\xd7\xc6\xe5rswj' => r: b'oB0u_g3t_l0w}\x00\x99\xaa'
slot data: b'ibctf{369_l3t-_s33-y0u_g3t_l0w}\x00'
```
