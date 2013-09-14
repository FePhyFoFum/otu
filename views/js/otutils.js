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
