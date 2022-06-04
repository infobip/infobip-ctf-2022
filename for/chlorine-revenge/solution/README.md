# Solution

Task description contains two hints: a _Chacha_ dance video and a hexadecimal string which is the tunnel decryption key. Task attachment contains a pcap file that recorded an obvious DNS tunnel. If one uses those keywords and googles them together (e.g. _encrypted dns tunnel chacha githu_), one might stumble upon a tool used to generate the traffic - [chashell](https://github.com/sysdream/chashell). When this is recognized, there is no other way to get the flag but to programatically extract data from packet's DNS name and decrypt it with the provided key in the task description.

Github provides participants with the decryption implementation and, since there is not a lot of packets in the dump, decrypted content does not need to have proper ordering to get the complete flag.

Our sample decryption implementation uses gopacket and just prints message data without ordering it, which gives the complete flag:

```sh
$ go run main.go
uid=1000(vm) gid=1000(vm) groups=1000(vm),4(admuid=1000(vm) gid=1000(vm) groups=1000(vm),4(adm),24(cdrom),27(sudo),30(dip),46(plugdev),120(lp),24(cdrom),27(sudo),30(dip),46(plugdev),120(lpadmin),131(lxd),132(sambashare)
admin),131(lxd),132(sambashare)
total 12
drwxrwxr-x 2 vm vm 4096 ruj   6 19:10 total 12
drwxrwxr-x 2 vm vm 4096 ruj   6 19:10 .
drwxrwxr-x 5 vm vm 4096 ruj   6 19:03 ..
-rw-.
drwxrwxr-x 5 vm vm 4096 ruj   6 19:03 ..
-rw-rw-r-- 1 vm vm   52 ruj   6 19:10 flag.txt
rw-r-- 1 vm vm   52 ruj   6 19:10 flag.txt
ibctf{c0m3_m33t_m3-t0m0r0www_AM-m4k3_n0_s3ns3-Wibctf{c0m3_m33t_m3-t0m0r0www_AM-m4k3_n0_s3ns3-W4TT}
4TT}
```
