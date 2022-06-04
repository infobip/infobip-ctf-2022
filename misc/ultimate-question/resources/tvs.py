flag = b"ibctf{t4bz-vs-sp4c3z_th3_0nly-c0d3rs-ult1m4t3-qu3st10n}"

bins = ''.join(["{0:08b}".format(i) for i in flag])
tvs = [0x09 if i == '1' else 0x20 for i in bins]

with open('ultimate-question', 'wb') as fp:
    fp.write(bytes(tvs))

