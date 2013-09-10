#!/usr/bin/python

import os
import otu
import cgi,cgitb
cgitb.enable()

import os
try: # Windows needs stdio set for binary mode.
    import msvcrt
    msvcrt.setmode (0, os.O_BINARY) # stdin  = 0
    msvcrt.setmode (1, os.O_BINARY) # stdout = 1
except ImportError:
    pass

##################### page definition.

### to make new pages, see the otu class in cgi-bin/

current = "VIEW_TREE"
html = otu.get_html(current)

#####################

#NO_ACTION = "no_action"
#WARNING = "warning"
#LOADED_TNRS_RESULTS = "loaded_results"

#def process_incoming_form (form):

#    log = open("logging", "w")
#    log.write("Incoming form contents:\n")
#    for i in form.keys():
#        log.write(i + ": " + form[i].value + "\n")
#    log.close()

#    result = {}

    # don't do anything if the form is empty
#    if (len(form.keys()) < 1):
#        result["event"] = NO_ACTION

#    elif form.has_key("tnrs_results_path"):
#        result["event"] = LOADED_TNRS_RESULTS
#        json = open(form["tnrs_results_path"].value)
#        result["treeId"] = form["treeId"].value
#        result["tnrs_results"] = json.read();

#    else:
#        result["event"] = WARNING
#        result["message"] = "Unrecognized action. Nothing to be done..."

#    return result

# ===== now actually do stuff

# take actions specified in the incoming form
#result = process_incoming_form (cgi.FieldStorage())

#if result["event"] == LOADED_TNRS_RESULTS:
#    html = html.replace("$TNRS_RESULTS$", result["tnrs_results"]).replace("$TREE_ID$", "\"" + result["treeId"] + "\"")
#else:
#    html = html.replace("$TNRS_RESULTS$", "false").replace("$TREE_ID$", "false")

# send the html to the browser
print "content-type: text/html\n"
print html
