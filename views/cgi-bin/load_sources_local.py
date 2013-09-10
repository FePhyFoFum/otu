##################################################################
#
#
#
#    currently unused, but retained since it contains code that may
#    be useful for interacting with a local git repo
#
#
#
#

#!/usr/bin/python

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

UPLOAD_DIR = "/tmp"
bitbucket_location = ""


HTML_TEMPLATE = """<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html><head>
<title>OTU: get data in there</title>
 <link href="../css/bootstrap.min.css" rel="stylesheet" media="screen">
 <link href="../css/bootstrap-responsive.css" rel="stylesheet">
 <script src="../js/jquery-1.9.1.js"></script>
 <script src="../js/bootstrap.min.js"></script>
 <script src="../js/jquery-ui-1.10.3.custom.min.js"></script>
 <meta name="viewport" content="width=device-width, initial-scale=1.0">
 <style>
      body {
        padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
      }
    </style>
</head>
<body>
<div class="navbar navbar-inverse navbar-fixed-top">
      <div class="navbar-inner">
        <div class="container">
          <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="brand" href="#">OTU</a>
          <div class="nav-collapse collapse">
            <ul class="nav">
              <li class="active"><a href="#">Load and List Sources</a></li>
              <li><a href="search_sources.py">Search Public Sources</a></li>
              <li><a href="../source_view.html">View Source</a></li>
              <li><a href="../tree_dyn.html">View Tree</a></li>
            </ul>
          </div><!--/.nav-collapse -->
        </div>
      </div>
    </div>

<div class="container">
<h1>Add and List sources in the local database</h1>
<div class="row">
<div id="source-contain" class="span5">
<h1>Nexson</h1>
<h3>Upload From Git</h3>
<form action="load_sources_local.py" method="POST" class="form-inline" id="gitform" enctype="multipart/form-data">
<input type="hidden" id="hidden_nexson_from_git" name="hidden_nexson_from_git"/>
Source ID:
	    <select name="loadselect" id="loadselect" form="gitform">
	    GITFILELIST
	    </select>
	   <button name="submit" type="submit" class="btn">Submit</button>
</form>

<h3>Upload File</h3>
<form action=load_sources_local.py method="POST" enctype="multipart/form-data">
<input type="hidden" id="hidden_nexson_from_file" name="hidden_nexson_from_file"/>
<input name="file" type="file"><br>
<button class="btn disabled">Coming soon</button>
</form>
<hr/>
<h1><b>Newick</b></h1>
<h3>Upload File</h3>
<form action=load_sources_local.py method="POST" enctype="multipart/form-data">
<input type="hidden" id="hidden_newick_from_file" name="hidden_newick_from_file"/>
Source ID:<input type="text" id="sourceID" name="sourceID"/> <br/>
File name: <input name="file" type="file"><br>
<button name="submit" type="submit" class="btn">Submit</button>
</form>
</div>
<div id="source-contain" class="span7">
<h1>Source list</h1>
<table id="sources" class="table table-striped">
<thead>
<tr>
<th>Source</th>
<!--<th>Tree</th>-->
</tr>
</thead>
</table>
</div>
</div>
</div>
<script language ="javascript" type = "text/javascript" >
var treelinkurl ="../tree_dyn.html?treeID="
var sourcelinkurl ="../source_view.html?sourceID="

function getSourceTreeList(){
    var baseurl = "http://localhost:7474/db/data/ext/sourceJsons/graphdb/getSourceTreeList";
    var method = "POST";
    var xobjPost = new XMLHttpRequest();
    xobjPost.open(method, baseurl, false);
    xobjPost.setRequestHeader("Accept", "");
    xobjPost.setRequestHeader("Content-Type","application/json");
    xobjPost.send("");
    var jsonrespstr = xobjPost.responseText;
    var evjson = jQuery.parseJSON(eval(jsonrespstr));
    $(evjson.sources).each(function(index, element){
      $('#sources').append('<tr><td> <a href="'+sourcelinkurl+element[0]+'">'+element[0]+'</a> </td> <td> <a href="'+treelinkurl+element[1]+'" target="_blank">'+element[1]+'</a> </td></tr>');       
    })
}

function getSourceList(){
    var baseurl = "http://localhost:7474/db/data/ext/sourceJsons/graphdb/getSourceList";
    var method = "POST";
    var xobjPost = new XMLHttpRequest();
    xobjPost.open(method, baseurl, false);
    xobjPost.setRequestHeader("Accept", "");
    xobjPost.setRequestHeader("Content-Type","application/json");
    xobjPost.send("");
    var jsonrespstr = xobjPost.responseText;
    var evjson = jQuery.parseJSON(eval(jsonrespstr));
    $(evjson.sources).each(function(index, element){
      $('#sources').append('<tr><td> <a href="'+sourcelinkurl+element+'">'+element+'</a> </td></tr>');       
    })
}

//getSourceTreeList();
getSourceList();
</script> 
</body>
</html>
"""

resturlsinglenewick = "http://localhost:7474/db/data/ext/sourceJsons/graphdb/putSourceNewickSingle"
resturlnexsonfile = "http://localhost:7474/db/data/ext/sourceJsons/graphdb/putSourceNexsonFile"
resturlnexsongitdir = "http://localhost:7474/db/data/ext/ConfigurationPlugins/graphdb/getNexsonGitDir"

def make_json_with_newick(sourcename,newick):
    data = json.dumps({"sourceID": sourcename, "newickString": newick})
    return data

def make_json_with_nexson(sourcename,nexson):
    data = json.dumps({"sourceID": sourcename, "nexsonString": nexson})
    return data
    
def print_html_form (success,gitfilelist):
    print "content-type: text/html\n"
    print HTML_TEMPLATE.replace("GITFILELIST",gitfilelist);

def print_redirect ():
    redirectURL = "../conf.html"
    print 'Content-Type: text/html'
    print 'Location: %s' % redirectURL
    print # HTTP says you have to have a blank line between headers and content
    print '<html>'
    print '  <head>'
    print '    <meta http-equiv="refresh" content="0;url=%s" />' % redirectURL
    print '    <title>You are going to be redirected</title>'
    print '  </head>' 
    print '  <body>'
    print '    Redirecting... <a href="%s">Click here if you are not redirected</a>' % redirectURL
    print '  </body>'
    print '</html>'
    
def save_uploaded_file(form_field, upload_dir):
    form = cgi.FieldStorage()
    log = open("logging","w")
    log.write("HERE")
    for i in form.keys():
	log.write(i)
    log.close()
    if form.has_key("hidden_newick_from_file"):
        save_uploaded_file_newick(form, form_field,upload_dir)
    elif form.has_key("hidden_nexson_from_git"):
        save_git_file_nexson(form, upload_dir)
    elif form.has_key("hidden_nexson_from_file"):
        save_uploaded_file_nexson(form, form_field,upload_dir)
    else:
        return False

def get_bitbucket_file_list():
    files = []
    for i in os.listdir(bitbucket_location):
        try:
            int(i)
            files.append(i)
        except:
            continue
    retstr = ""
    files.sort()
    for i in files:
	retstr += "<option value="+i+">"+i+"</option>\n"
    return retstr
        
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
    sourceid = form["sourceID"].value
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
    return True

def save_git_file_nexson(form, upload_dir):
    log = open("logging","a")
    sourceID = form["loadselect"].value
    fl = open(bitbucket_location+"/"+sourceID)
    nexson = json.dumps(json.loads(fl.read()))
    fl.close()
    data = make_json_with_nexson(sourceID,nexson)
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
    return True

def save_uploaded_file_nexson (form, form_field, upload_dir):
    return False

def get_nexson_git_dir():
    req2 = urllib2.Request(resturlnexsongitdir, headers = {"Content-Type": "application/json",
        # Some extra headers for fun
        "Accept": "*/*",   # curl does this
        "User-Agent": "my-python-app/1", # otherwise it uses "Python-urllib/..."
        },data = "")
    f = urllib2.urlopen(req2)
    v = f.read()
    rep = json.loads(json.loads(str(v)))
    return rep["nexsongitdir"]
    

bitbucket_location = get_nexson_git_dir()
if "null" in bitbucket_location:
    print_redirect ()
else:
    success = save_uploaded_file ("file", UPLOAD_DIR)
    gitfilelist = get_bitbucket_file_list()
    print_html_form (success,gitfilelist)
