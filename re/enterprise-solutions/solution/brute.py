import itertools
import subprocess

for i in itertools.product(b"0123456789", repeat=4):
	pin = bytes(i)
	process = subprocess.Popen("./out", stdin=subprocess.PIPE, stdout=subprocess.PIPE)
	out, err = process.communicate(input=pin)
	if b"UNAUTHORIZED" not in out:
		print(f"PIN: {pin}")
