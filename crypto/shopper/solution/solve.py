from urllib.parse import urlparse
from urllib.parse import urlunparse
from urllib.parse import urlencode
from urllib.parse import parse_qsl

from binascii import hexlify
from binascii import unhexlify

from requests import Session
from re import search

TARGET_URL = "http://localhost:8080"
QS_IV_N = "iv"
QS_COST_N = "cost"

def flip(pos, bit, data):
	list1 = list(data)
	list1[pos] = list1[pos]^bit
	return bytes(list1)


if __name__ == "__main__":
	s = Session()
	# Get wallet cookie
	s.get(f"{TARGET_URL}/")
	# Get shop params
	resp = s.get(f"{TARGET_URL}/shop?id=4", allow_redirects=False)
	# Get redirect URL
	loc = resp.headers["Location"]
	parsed_loc = urlparse(loc)

	# Extract and flip IV bit
	qps = dict(parse_qsl(parsed_loc.query))
	iv = unhexlify(qps[QS_IV_N])
	iv = flip(10, 5, iv)
	# Set new params IV and cost
	qps[QS_IV_N] = hexlify(iv)
	qps[QS_COST_N] = "000"

	# Craft new redirect URL
	parsed_loc = parsed_loc._replace(query=urlencode(qps))
	loc = urlunparse(parsed_loc)

	# Buy the flag
	resp = s.get(f"{TARGET_URL}{loc}")
	if match := search("ibctf{[a-zA-Z0-9\-_]+}", resp.text):
		print(match.group(0))

