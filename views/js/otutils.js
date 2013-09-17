// ===== util methods

// return an xhr with default settings
function getXhr(url, callback, testing) {
    var xhr = new XMLHttpRequest();
    xhr.open("POST", url, callback ? true : false);
    xhr.setRequestHeader("Accept", "");
    xhr.setRequestHeader("Content-Type","Application/json");
    if (callback) {
        xhr.onreadystatechange=function() {
            if (testing) {alert(xhr.responseText)};
            if (xhr.readyState==4 && xhr.status==200) {
               callback();
            }
        };
    }
    return xhr;
}

// extract a variables from the GET arguments list
function getQueryVariable(variable) {
    var query = window.location.search.substring(1);
    var vars = query.split("&");
    for (var i=0;i<vars.length;i++) {
        var pair = vars[i].split("=");
        if (pair[0] == variable) { return pair[1]; }
    }
    return(false);
}

// Set a property of the graph root node. properties must be specified in the OTUGraphProperty enum.
// An optional callback function can be specified, to which the JSON resulting from the service to set
// the property will be passed.
function setGraphProperty(pname, pvalue, ptype) {
    var setPropertyService = "http://localhost:7474/db/data/ext/ConfigurationPlugins/graphdb/setGraphProperty";
    var xhr = getXhr(setPropertyService, function() {
        var response = JSON.parse(xhr.responseText);
        if (callback) {
            callback(response);
        }
    });
    xhr.send(JSON.stringify({propertyName: pname, value: pvalue, type: ptype}));
}

// Get a property of the root node of the graph. an optional callback function
// can be specified to which the JSON resulting from the property query will be passed
function getGraphProperty(pname, callback) {
    var getPropertyService = "http://localhost:7474/db/data/ext/ConfigurationPlugins/graphdb/getGraphProperty";
    var xhr = getXhr(getPropertyService, function() {
        var response=JSON.parse(xhr.responseText);
        if (callback) {
            callback(response);
        }
    });
    xhr.send(JSON.stringify({propertyName: pname}));
}
