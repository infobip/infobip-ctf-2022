We cannot store any secrets on our infrastructure. Therefore, we provide with per user verificator program so users can check if they still remember their decryption password. SMART!

openssl enc -d -aes-256-cbc -pbkdf2 -p -in data -out flag.txt
