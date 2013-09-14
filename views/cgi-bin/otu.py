
"""
The PAGES dict defines the page views. Each view consists of:

1.  A python script in cgi bin/ that will be called on load to
    generate the page.

2.  An html template in /html to be sent to the python cgi.
    Conceivably this could be an empty file if no html template
    were required.

3.  The page title, which will be displayed in the nav bar.
"""

PAGES = {
    "SEARCH_AND_IMPORT" : {
        "title" : "Search and import sources",
        "link" : "load_sources.py",
        "html" : "load_sources.html" },
#    "SEARCH_SOURCES" : {
#        "title" : "Search sources",
#        "link" : "search_sources.py",
#        "html" : "search_sources.html" },
    "VIEW_SOURCE" : {
        "title" : "View source",
        "link" : "source_view.py",
        "html" : "source_view.html"},
    "VIEW_TREE" : {
        "title" : "View tree",
        "link" : "tree_view.py",
        "html" : "tree_view.html"},
    "PUSH_CHANGES" : {
        "title" : "Publish changes",
        "link" : "conf.py",
        "html" : "conf.html"},
    "TAXONOMY" : {
        "title" : "Taxonomy",
        "link" : "taxonomy.py",
        "html" : "taxonomy.html"},
    "CONFIGURE" : {
        "title" : "Configure",
        "link" : "conf.py",
        "html" : "conf.html"}
}

# define the order in which pages appear in the navbar
PAGE_ORDER = [ "SEARCH_AND_IMPORT", "VIEW_SOURCE", "VIEW_TREE", "PUSH_CHANGES", "TAXONOMY", "CONFIGURE" ]

def get_navbar(current):
    
    navbar = open("includes/navbar.html","rU").read()

    # use the defined order to keep the navbar from being squirrelly
    nav_links = ""
    for page_name in PAGE_ORDER:

        page_contents = PAGES[page_name]

        if current==page_name:
            li = "<li \"class=active\">"
            a = "<a href=\"#\">"
        else:
            li = "<li>"
            a = "<a href=\"" + page_contents["link"] + "\">"

        nav_links += li + a + page_contents["title"] + "</a></li>\n"

    return navbar.replace("$$NAV_LINKS$$", nav_links)

def get_html(current_page_name):
    template = "html/" + PAGES[current_page_name]["html"]
    html = open(template,"rU").read()

    head_content=open("includes/head_content.html","rU").read()
    html = html.replace("$$HEAD_CONTENT$$", head_content)

    html = html.replace("$$NAVBAR$$", get_navbar(current_page_name))

    return html
