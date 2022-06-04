# Solution

The web shop contains a fundamental flaw: it uses AES encryption in CBC mode to implement a signature, and AES-CBC does not guarantee data integrity.

To demonstrate this, observe the following code snippet:
```python
from Crypto.Cipher import AES

def flip(pos, bit, data):
    list1 = list(data)
    list1[pos] = list1[pos]^bit
    return bytes(list1)

b = b'cost=500&id=1&item=flag\t\t\t\t\t\t\t\t\t'

with open('/dev/urandom', 'rb') as fp:
    iv = fp.read(16)
    key = fp.read(16)

abc = AES.new(key, AES.MODE_CBC, iv)
c = abc.encrypt(b)
fgh = AES.new(key, AES.MODE_CBC, iv)
assert fgh.decrypt(c) == b

iv = flip(5, 5, iv)
opr = AES.new(key, AES.MODE_CBC, iv)
print(opr.decrypt(c))
```

If you run it, you will observe a proof-of-concept of how AES-CBC is vulnerable to a bit flipping attack.
```sh
$ python3 bitflip.py 
b'cost=000&id=1&item=flag\t\t\t\t\t\t\t\t\t'
```

By flipping a bit in the initialization vector (IV), the first AES block is influenced and produces a valid plaintext without influencing other blocks. This is the basis of this web challenge.

## Attacking the web shop

The web shop is based on redirects, therefore our flow would be as follows:
- Visit the site
- Click on _Buy_ where flag is located
- This redirects us to `/buy` with parameters that we can change
- By changing parameter values (flipping IV bit) we can buy the flag

For instance, if we request HTTP GET on `http://{TARGET}/shop?id=4`, the redirect URL in `Location` header will look similar to the following value:

```
reply: 'HTTP/1.1 302 FOUND\r\n'
header: Server: gunicorn
header: Date: Fri, 26 Nov 2021 19:01:21 GMT
header: Connection: close
header: Content-Type: text/html; charset=utf-8
header: Content-Length: 524
header: Location: http://localhost:8080/buy?item_id=4&cost=500&name=flag.txt&sign=5e120e38d3036bbfa4e8b93efad22587dd22760146f08a812cd475c4f796b6fb&iv=bff966a48a147b3fdc75752e486cd442
```

In order to buy the flag without any funds, we have to set the cost to `000` (to preserve same byte alignment) and flip the IV bit. Flipping the bit might take some guessing but can also be bruteforced if you are to lazy to setup your own instance of the next hand shop app.

In order to get the flag, the 10th byte's 5th bit must be flipped for the cost to set to `000` value, which python happily converts to integer 0.

Solution script `solve.py` mimicks required HTTP requests and prints out the flag:
```sh
$ python3 solve.py 
ibctf{0ur-f4v0r1t3-g4m3_iss_fl1pp3r-b1t-fl1pp3r}
```
