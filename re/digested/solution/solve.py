from hashlib import sha256

EXPECTED = [170, 34, 113, 38, 233, 215, 219, 249, 217, 58, 110, 190, 255, 53, 227, 35, 71, 76, 174, 2, 129, 78, 28, 58, 203, 41, 109, 43, 153, 244, 191, 216]
ALPHABET = 'abcdefghijklmnopqrstuwvxyz0123456789-_'

combos = [""]
for i in range(32):
	for j in ALPHABET:
		hash = sha256(j.encode()).digest()
		
		if hash[i] == EXPECTED[i]:
			print("HIT {}: {}".format(i, j))
			
			new_combo = True
			for c in range(len(combos)):
				if len(combos[c]) == i:
					combos[c] += j
					new_combo = False

			if new_combo:
				for c in range(len(combos)):
					combos.append(combos[c][:-1]+j)

print(combos)
