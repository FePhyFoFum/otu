#!/usr/bin/python

import otu
import cgi,cgitb
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

current = "TAXONOMY"
HTML = otu.get_html(current)

#####################


# send the html to the browser
print "content-type: text/html\n"
print HTML
