from flask import Flask
from flask import render_template
from flask import request

import json
import lxml.etree
import yaml

SUPPORTED_REQ_TYPES = ["json", "xml", "yaml"]

app = Flask(__name__)

@app.route("/contact", methods=['GET', 'POST'])
def contact():
    reqtype = request.args.get("type")
    if reqtype not in SUPPORTED_REQ_TYPES:
        return "type must be one of {}".format(SUPPORTED_REQ_TYPES)

    data = request.get_data()
    if len(data) <= 0:
        return "no data received"

    try:
        if reqtype == "json":
            parsed = json.loads(data)
            name = parsed["person"]["name"]
        elif reqtype == "xml":
            parser = lxml.etree.XMLParser()
            parsed = lxml.etree.XML(data, parser)
            name = parsed.xpath("//person/name")[0].text
        elif reqtype == "yaml":
            parsed = yaml.safe_load(data)
            name = parsed["person"]["name"]
    except Exception as e:
        print(e)
        return "data provided is malformed"

    return "Thank you, Mr./Mrs. {}! Our sales team will contact you shortly.".format(name)

@app.route("/")
def index():
    return render_template("index.html")
