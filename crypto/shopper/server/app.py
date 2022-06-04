from flask import flash
from flask import Flask
from flask import g
from flask import make_response
from flask import render_template
from flask import request
from flask import redirect
from flask import url_for

from Crypto.Cipher import AES
from Crypto.Random import get_random_bytes

from urllib import parse

import binascii
import os
import sqlite3

app = Flask(__name__)
app.secret_key = binascii.unhexlify(os.getenv("FLASK_SECRET_KEY"))
key = binascii.unhexlify(os.getenv("SHOP_SIGNING_KEY"))

def db():
    dbconn = getattr(g, "_db", None)
    if dbconn is None:
        dbconn = g._db = sqlite3.connect("shop.db")
    return dbconn

def pad(bs):
    padlen = AES.block_size - (len(bs) % AES.block_size)
    return bs + bytes([padlen for _ in range(padlen)])

def unpad(bs):
    return bs[:-1*bs[-1]]

@app.teardown_appcontext
def db_teardown(exception):
    dbconn = getattr(g, "_db", None)
    if dbconn is not None:
        dbconn.close()

@app.route("/buy")
def buy():
    item_id = int(request.args.get("item_id"))
    cost = int(request.args.get("cost"))
    name = request.args.get("name")
    signature = binascii.unhexlify(request.args.get("sign"))
    iv = binascii.unhexlify(request.args.get("iv"))

    wallet = request.cookies.get("wallet").split(",")
    if len(wallet) != 2:
        flash("Unknown wallet")
        return redirect(url_for("index"))

    dbcur = db().cursor()
    dbcur.execute("SELECT amount FROM wallet WHERE hash=:hash", {"hash": wallet[0]})
    amount = dbcur.fetchone()[0]

    if amount < cost:
        flash("Insufficient funds")
        return redirect(url_for("index"))

    aes = AES.new(key, AES.MODE_CBC, iv)
    signval = unpad(aes.decrypt(signature)).decode()
    qs = {k: v[0] for k, v in parse.parse_qs(signval).items()}
    if item_id != int(qs["id"]) or cost != int(qs["cost"]) or name != qs["name"]:
        flash("Transaction validation failed")
        return redirect(url_for("index"))

    dbcur = db().cursor()
    dbcur.execute("SELECT data FROM content WHERE id=:id", {"id": item_id})
    content = dbcur.fetchone()[0]
    flash(content)
    return redirect("/")

@app.route("/wallet_import")
def wallet_import():
    hsh = request.args.get("wallet")
    
    dbcur = db().cursor()
    dbcur.execute("SELECT amount FROM wallet WHERE hash=:hash", {"hash": hsh})
    amount = dbcur.fetchone()[0]
    
    resp = make_response(redirect("/"))
    resp.set_cookie("wallet", "{},{}".format(hsh, amount))
    return resp

@app.route("/shop")
def shop():
    item_id = int(request.args.get("id"))
    dbcur = db().cursor()
    dbcur.execute("SELECT id, cost, name FROM items WHERE id=:id", {"id": item_id})
    item = dbcur.fetchone()

    iv = get_random_bytes(16)
    aes = AES.new(key, AES.MODE_CBC, iv)
    signval = "id={}&cost={}&name={}".format(item[0], item[1], item[2]).encode()
    signature = aes.encrypt(pad(signval))

    return redirect(url_for("buy", item_id=item[0], cost=item[1], name=item[2], sign=binascii.hexlify(signature), iv=binascii.hexlify(iv)))

@app.route("/")
def index():
    if "wallet" in request.cookies:
        wallet, amount = request.cookies.get("wallet").split(",")
    else:
        return redirect(url_for("wallet_import", wallet="4c9184f37cff01bcdc32dc486ec36961"))

    dbcur = db().cursor()
    dbcur.execute("SELECT id, cost, name FROM items")
    items = dbcur.fetchall()
    return render_template("index.html", items=items, wallet=wallet, amount=amount)

