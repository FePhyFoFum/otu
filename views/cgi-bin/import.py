#!/usr/bin/python

"""
A cgi script to handle interactions with the file system required during source loading, which is
not easily done(or even possible?) with javascript. When finished with the required operations,
this script will substitute results information into the specified HTML_TEMPLATE string, and will
print the result to the web browser.
"""

import otu
from time import time
import math
import json
import urllib2
import cgi,cgitb
from StringIO import StringIO
cgitb.enable()

import os, sys
try: # Windows needs stdio set for binary mode.
    import msvcrt
    msvcrt.setmode (0, os.O_BINARY) # stdin  = 0
    msvcrt.setmode (1, os.O_BINARY) # stdout = 1
except ImportError:
    pass

##################### page definition. to make new pages, see the otu.PAGES dict

current = "IMPORT"
HTML = otu.get_html(current)

#####################


# where we put files that we're going to work with
UPLOAD_DIR = "/tmp"

# recognized events
ADDED = "added"
WARNING = "warning"
SUCCESS = "success"
NO_ACTION = "no_action"

resturlsinglenewick = "http://localhost:7474/db/data/ext/sourceJsons/graphdb/putSourceNewickSingle"
resturlnexsonfile = "http://localhost:7474/db/data/ext/sourceJsons/graphdb/putSourceNexsonFile"

# ===== functions for saving uploaded files

def process_incoming_form (form, form_field, upload_dir):

    log = open("logging","w")
    log.write("Incoming form contents:\n")
    for i in form.keys():
        log.write(i + ": " + form[i].value + "\n")
    log.close()

    result = {}

    # don't do anything if the form is empty
    if (len(form.keys()) < 1):
        result["event"] = NO_ACTION

    elif form.has_key("hidden_nexson_from_git"):
        result = save_git_file_nexson(form, upload_dir)

    elif form.has_key("local_file_type"):

        filetype = form["local_file_type"].value
        
        if filetype == "nexson":
            result = save_uploaded_file_nexson(form, form_field,upload_dir)
        elif filetype == "newick":
            result = save_uploaded_file_newick(form, form_field,upload_dir)
        elif filetype == "nexus":
            result = save_uploaded_file_nexus(form, form_field,upload_dir)            
            
    elif form.has_key("deleted_source_id"):
        result["event"] = SUCCESS
        result["message"] = "Source " + form["deleted_source_id"].value + " was removed from the database."

    else:
        result["event"] = WARNING
        result["message"] = "Unrecognized action. Nothing to be done..."

    return result
        
def save_uploaded_file_newick (form, form_field, upload_dir):
    log = open("logging","a")
    """This saves a file uploaded by an HTML form.
       The form_field is the name of the file input field from the form.
       For example, the following form_field would be "file_1":
           <input name="file" type="file">
       The upload_dir is the directory where the file will be written.
       If no file was uploaded or if the field does not exist then
       this does nothing.
    """
    if not form.has_key(form_field): return False
#    sourceid = form["sourceId"].value
    sourceid = str(int(math.floor(time())))
    fileitem = form[form_field]
    if not fileitem.file: return False
    fout = file (os.path.join(upload_dir, fileitem.filename), 'wb')
    while 1:
        chunk = fileitem.file.read(100000)
        if not chunk: break
        fout.write (chunk)
    fout.close()
    fout = open (os.path.join(upload_dir, fileitem.filename), 'r')
    data = ""
    for i in fout:
        data = make_json_with_newick(sourceid,i.strip())
        log.write( data+"\n")
        break
    fout.close()
    req = urllib2.Request(resturlsinglenewick, headers = {"Content-Type": "application/json",
        # Some extra headers for fun
        "Accept": "*/*",   # curl does this
        "User-Agent": "my-python-app/1", # otherwise it uses "Python-urllib/..."
        },data = data)
    f = urllib2.urlopen(req)
    log.close()
    return {"event": ADDED, "sourceId": sourceid}

def save_uploaded_file_nexson (form, form_field, upload_dir):
    return {"event": WARNING, "message": "Manual nexson upload not (yet) supported."}

def save_uploaded_file_nexus (form, form_field, upload_dir):
    return {"event": WARNING, "message": "Manual nexus upload not (yet) supported."}


def save_git_file_nexson(form, upload_dir):
    log = open("logging","a")
    recenthash = form["recenthash"].value
    sourceId = form["nexsonid"].value
    log.write("\n" + "recenthash " + recenthash + ", nexsonid " + sourceId + "\n")
    geturl = "https://bitbucket.org/api/1.0/repositories/blackrim/avatol_nexsons/raw/"+recenthash+"/"+str(sourceId)
    print geturl
    req = urllib2.Request(geturl)
    f = urllib2.urlopen(req)
    nexson = json.dumps(json.loads(f.read()))
    data = make_json_with_nexson(sourceId,nexson)
    clen = len(data)
    req2 = urllib2.Request(resturlnexsonfile, headers = {"Content-Type": "application/json",
	"Content-Length": clen,
        # Some extra headers for fun
        "Accept": "*/*",   # curl does this
        "User-Agent": "my-python-app/1", # otherwise it uses "Python-urllib/..."
        },data = data)
    log.write('curl -X POST '+resturlnexsonfile+' -H "Content-Type: application/json"'+' -d \''+data+'\'')
    f = urllib2.urlopen(req2)
    log.close()
    result = json.loads(f.read())
#    result["sourceId"] = sourceId
    return result

def make_json_with_newick(sourcename, newick):
    data = json.dumps({"sourceId": sourcename, "newickString": newick})
    return data

def make_json_with_nexson(sourcename, nexson):
    data = json.dumps({"sourceId": sourcename, "nexsonString": nexson})
    return data
    
# ===== functions for getting source info

def get_bitbucket_recent_hash():
    commiturl = "https://bitbucket.org/api/2.0/repositories/blackrim/avatol_nexsons/commits"
    req = urllib2.Request(commiturl)
    f = urllib2.urlopen(req)
    cjson = json.loads(f.read())
    return cjson["values"][0]["hash"]

def get_bitbucket_file_list(recenthash):
    listurl = "https://bitbucket.org/api/1.0/repositories/blackrim/avatol_nexsons/raw/"+recenthash+"/"
    req = urllib2.Request(listurl)
    f = urllib2.urlopen(req)
    files = []
    for i in f:
	try:
	    int(i.strip())
	    files.append(i.strip())
	except:
	    continue
    retstr = ""
    for i in files:
	retstr += "<option value="+i+">"+i+"</option>\n"
    return retstr

# ===== functions for making the web page

def print_html_form (result, recenthash, gitfilelist):
    """This prints out the html form. Note that the action is set to
      the name of the script which makes this is a self-posting form.
      In other words, this cgi both displays a form and processes it.
    """

    print "content-type: text/html\n"
    html = HTML.replace("$GITFILELIST$",gitfilelist).replace("$RECENTHASH$",recenthash).replace("$MESSAGE$",message)
    
    print html

# ===== now actually do stuff

# take actions specified in the incoming form
result = process_incoming_form (cgi.FieldStorage(), "file", UPLOAD_DIR)
message = ""

# determine the result
if result["event"] == ADDED:
    message = '<p class="highlight">Added source ' + result["sourceId"] + ' to the database. <a href="source_view.py?sourceId=' + result["sourceId"] + '">Click here to edit this source</a>.</p>'

elif result["event"] == SUCCESS:
    message = '<p class="warning-weak">'+result["message"]+'</p>'
    
elif result["event"] == WARNING:
    message = '<p class="warning-strong">'+result["message"]+'</p>'
    

# display the page
recenthash = get_bitbucket_recent_hash()
gitfilelist = get_bitbucket_file_list(recenthash)
print_html_form (message, recenthash, gitfilelist)
