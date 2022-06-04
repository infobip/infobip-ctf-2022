CC=gcc

yggdrasil:
	$(CC) -o verificator yggdrasil.c
	strip ./verificator
	openssl enc -aes-256-cbc -pbkdf2 -k 'e2rv?zIWXKSE!(3lFNkHV' -in original.txt -out data
