#!/usr/bin/python

import otu
import urllib2, tarfile
import cgi, cgitb
cgitb.enable()

import os, sys
try: # Windows needs stdio set for binary mode.
    import msvcrt
    msvcrt.setmode (0, os.O_BINARY) # stdin  = 0
    msvcrt.setmode (1, os.O_BINARY) # stdout = 1
except ImportError:
    pass

##################### page definition.

### to make new pages, see the otu class in cgi-bin/

current = "CONFIGURE"
HTML = otu.get_html(current)

# where we put files that we're going to work with
DOWNLOAD_DIR = os.path.join(os.path.expanduser("~"),"Downloads")

# recognized events
WARNING = "warning"
SUCCESS = "success"
NO_ACTION = "no_action"

#####################

def process_incoming_form (form, download_dir):

    log = open("logging","w")
    log.write("Incoming form contents:\n")
    for i in form.keys():
        log.write(i + ": " + form[i].value + "\n")

    result = {}

    # don't do anything if the form is empty
    if (len(form.keys()) < 1):
        result["event"] = NO_ACTION

    elif form.has_key("download_ott_and_initiate_install"):
        result = download_ott(download_dir, log)

    else:
        result["event"] = WARNING
        result["message"] = "Unrecognized action. Nothing to be done..."

    log.close()
    return result

def download_ott(download_dir, log):
    
    geturl = "http://dev.opentreeoflife.org/ott/ott2.2.tgz"
    log.write ("\n" + geturl + "\n")

    req = urllib2.Request(geturl)
    f = urllib2.urlopen(req)

    ott_save_path = os.path.join(download_dir,"ott2.2.tar.gz")
    log.write("\n" + ott_save_path + "\n")

    # write the taxonomy into the file
    ottfile = file(ott_save_path, 'wb')
    while True:
        chunk = f.read(1000000)
        if not chunk: break
        ottfile.write (chunk)
    ottfile.close()

    # extract the taxonomy
    os.chdir(download_dir)
    tar = tarfile.open(ott_save_path)
    tar.extractall()
    tar.close()
    
    tax_file_path = os.path.join(download_dir,"ott2.2","taxonomy")
    if os.path.exists(tax_file_path):
        return { "event": "initiate_ott_install", "ott_path": tax_file_path }
    else:
        return { "event": "failure" }


# ===== now actually do stuff

# take actions specified in the incoming form
result = process_incoming_form (cgi.FieldStorage(), DOWNLOAD_DIR)
message = ""

event_html = ""
if result["event"] == "initiate_ott_install":
    event_html = "<script>$(document).ready(function() { window.location.replace('conf.py?ott_path=" \
        + result["ott_path"] + "'); })</script>"

# send the html to the browser
print "content-type: text/html\n"
print HTML.replace("$EVENT_HTML$",event_html)
