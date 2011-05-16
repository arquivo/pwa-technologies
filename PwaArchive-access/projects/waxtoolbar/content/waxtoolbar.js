/* Copyright (C) 2006 National Library of Sweden.
*
* WAX Toolbar is free software; you can redistribute it and/or modify
* it under the terms of the GNU Lesser Public License as published by
* the Free Software Foundation; either version 2.1 of the License, or
* any later version.
*
* WAX Toolbar is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser Public License for more details.
*
* You should have received a copy of the GNU Lesser Public License
* along with the program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

var archiveMode = false;
var searchMode = "url";

// Our own WAX configurations
var conf = new WaxConfigObject();
// The original proxy configurations
var prefs=Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefBranch);
var originalProxy = prefs.getCharPref("network.proxy.http");
var originalPort = prefs.getIntPref("network.proxy.http_port");
var originalType = prefs.getIntPref("network.proxy.type");
var originalNoProxies = prefs.getCharPref("network.proxy.no_proxies_on");

var uid = getId();
var availableTimes;
var currPosition;
var proxyObject;
var requestObject;

/*****************************/
/* PROXY MODE RELATED STUFF  */
/*****************************/
function toggleArchiveMode() {
  var choice = document.getElementById("WAX_ArchiveMode_Radiogroup").selectedIndex;
  if(choice == 0) {
    archiveMode = false;
    stopProxyMode();
  }
  else {
    archiveMode = true;
    startProxyMode();
  }
  var currentUrl = getCurrentUrl();
  if(reloadPage(currentUrl))
    window._content.location.reload();
}

function startProxyMode() {
    proxyObject = new ProxyObject();
    var observerService = Components.classes["@mozilla.org/observer-service;1"].getService(Components.interfaces.nsIObserverService);
    observerService.addObserver(proxyObject, "http-on-modify-request", false);
    prefs.setIntPref("network.proxy.type", 1);
    prefs.setCharPref("network.proxy.http", conf.proxyHost);
    prefs.setIntPref("network.proxy.http_port", conf.proxyPort);
    prefs.setCharPref("network.proxy.no_proxies_on", originalNoProxies + "," + conf.proxyHost);
    proxyDisabled(false);
}

function stopProxyMode() {
    var observerService = Components.classes["@mozilla.org/observer-service;1"].getService(Components.interfaces.nsIObserverService);
    observerService.removeObserver(proxyObject, "http-on-modify-request");
    proxyObject = null;
    prefs.setIntPref( "network.proxy.type", originalType);
    prefs.setCharPref("network.proxy.http", originalProxy);
    prefs.setIntPref("network.proxy.http_port", originalPort);
    prefs.setCharPref("network.proxy.no_proxies_on", originalNoProxies);
    proxyDisabled(true);
}

function proxyDisabled(mode) {
  var forward = document.getElementById("WAX_ForwardTime_Button");
  forward.setAttribute("disabled", mode);
  var change = document.getElementById("WAX_ChangeTime_Button");
  change.setAttribute("disabled", mode);
  var back = document.getElementById("WAX_BackwardTime_Button");
  back.setAttribute("disabled", mode);
  if(mode) {
    forward.setAttribute("image", "chrome://waxtoolbar/content/images/nav-next-disabled.png");
    back.setAttribute("image", "chrome://waxtoolbar/content/images/nav-prev-disabled.png");
    setInfoLabel("");
  }
  else {
    forward.setAttribute("image", "chrome://waxtoolbar/content/images/nav-next.png");
    back.setAttribute("image", "chrome://waxtoolbar/content/images/nav-prev.png");
  }
}

/**************************/
/*  SEARCH RELATED STUFF  */
/**************************/
function changeSearchMode(mode) {
 var button = document.getElementById("WAX_Search_Button");
 if(mode == "url") {
  searchMode = "url";
  button.setAttribute("image", "chrome://waxtoolbar/content/images/search-url.png");
  button.setAttribute("tooltiptext", "Search URL");
 }
 else {
  searchMode = "text";
  button.setAttribute("image", "chrome://waxtoolbar/content/images/search-text.png");
  button.setAttribute("tooltiptext", "Search full-text");
 }
}

function waxSearch() {
  var textbox = document.getElementById("WAX_Search_Textbox");
  var searchtext = textbox.value;
  if(searchMode == "url") {
    var searchUrl = "http://" + conf.proxyHost + ":" + conf.proxyPort + "/query?type=urlquery&url=" + escapeUrl(searchtext);
  }
  else {
    var searchUrl = "http://" + conf.nutchHost + ":" + conf.nutchPort + "/nutchwax/search.jsp?query=" + searchtext + "&hitsPerPage=10";
  }
  window._content.document.location = searchUrl;
}

/************************/
/*  TIME RELATED STUFF  */
/************************/
function changeTime(time) {
  if(archiveMode) {
    var currentUrl = getCurrentUrl();
    var escapedUrl = escapeUrl(currentUrl);
    var redirectUrl = "http://" + conf.proxyHost + ":" + conf.proxyPort + "/jsp/QueryUI/Redirect.jsp?url=" + escapedUrl + "&time=" + time;
    window._content.document.location = redirectUrl;
  }
}

function waxForward() {
  if(archiveMode) {
    var currentUrl = getCurrentUrl();
    var escapedUrl = escapeUrl(currentUrl);
    if(currPosition < (availableTimes.length - 1)) {
      var time = availableTimes[++currPosition];
      var redirectUrl = "http://" + conf.proxyHost + ":" + conf.proxyPort + "/jsp/QueryUI/Redirect.jsp?url=" + escapedUrl + "&time=" + time;
      window._content.document.location = redirectUrl;
    } 
  }
}

function waxBack() {
  if(archiveMode) {
    var currentUrl = getCurrentUrl();
    var escapedUrl = escapeUrl(currentUrl);
    if(currPosition > 0) {
      var time = availableTimes[--currPosition];
      var redirectUrl = "http://" + conf.proxyHost + ":" + conf.proxyPort + "/jsp/QueryUI/Redirect.jsp?url=" + escapedUrl + "&time=" + time;
      window._content.document.location = redirectUrl;
    }
  }
}

function waxOnUrlLoad() {
  if(archiveMode) {
    var currentUrl = getCurrentUrl();
    var escapedUrl = escapeUrl(currentUrl);
    getAllTimes(escapedUrl);
  }
}

function getAllTimes(url) {
  requestObject = new XMLHttpRequest();
  requestObject.onreadystatechange = onReadyStateChange;
  var searchUrl = "http://" + conf.proxyHost + ":" + conf.proxyPort + "/xmlquery?type=urlquery&url=" + url;
  requestObject.open("GET", searchUrl);
  requestObject.send("");
}

function onReadyStateChange() {
  if(requestObject.readyState == 4) {
    if(requestObject.status == 200) {
      availableTimes = new Array();
      var xmldoc = requestObject.responseXML;
      var times = xmldoc.getElementsByTagName("capturedate");
      for(i = 0; i < times.length; i++) {
        var closest = checkIfClosest(times[i]);
        if(closest)
          currPosition = i;
        availableTimes.push(times[i].firstChild.nodeValue);
      }
      updateCurrentTimeLabel(currPosition);
      updateChangeTimeMenu(availableTimes);
    }
    else {
      alert("There was a problem retrieving the XML data:\n" + requestObject.statusText);
    }
  }
}

function checkIfClosest(captureDate) {
  var result = captureDate.parentNode;
  var closest = result.getElementsByTagName("closest")[0];
  if(closest != null)
    return true;
  return false;
}

function updateCurrentTimeLabel(currPosition) {
  if(availableTimes.length > 0) {
    setInfoLabel("Historical website from: " + prettierDate(availableTimes[currPosition]));
  }
  else {
    setInfoLabel("");
  }
}

function updateChangeTimeMenu(array) {
  var menu = document.getElementById("WAX_ChangeTime_Menu");
  while(menu.hasChildNodes()) {
    menu.removeChild(menu.firstChild);
  }
  for(i = 0; i < array.length; i++) {
    var menuItem = document.createElement("menuitem");
    menuItem.setAttribute("id", array[i]);
    menuItem.setAttribute("label", prettierDate(array[i]));
    menuItem.setAttribute("oncommand", "changeTime(id)");
    menu.appendChild(menuItem);
  }
}

/**********************/
/*  HELPER FUNCTIONS  */
/**********************/
function getCurrentUrl() {
  return window._content.document.location + "";
}

function escapeUrl(url) {
  return escape(url).replace(/\+/g, '%2B').replace(/\"/g,'%22').replace(/\'/g, '%27').replace(/\//g,'%2F');
}

function reloadPage(currentUrl) {
  var regex = new RegExp("(http:\/\/)?"+(conf.proxyHost+":"+conf.proxyPort|conf.nutchHost+":"+conf.nutchPort)+".*");
  return (currentUrl.match(regex) == null);
}

function prettierDate(dateString) {
  return dateString.replace(/(....)(..)(..)(..)(..)(..)/g, "$1-$2-$3 $4:$5:$6");
}

function setInfoLabel(message) {
  var label = document.getElementById("WAX_Info_Label");
  label.setAttribute("value", message);
}

function getId() {
  prefs = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefBranch);
  var id = "";
  if(prefs.prefHasUserValue("wax.proxy.unique_id")) {
    id = prefs.getCharPref("wax.proxy.unique_id");
  }
  else {
    for(i = 0; i < 32; i++) {
      temp = Math.floor(16*Math.random());
      id += temp.toString(16);
    }
    prefs.setCharPref("wax.proxy.unique_id", id);
  }
  return id;
}

/*************************/
/*  HEADER ADDING STUFF  */
/*************************/
function ProxyObject() {
}

ProxyObject.prototype = {
    // Observer interface method
    observe: function(subject, topic, data) {
        if (topic == 'http-on-modify-request') {
            subject.QueryInterface(Components.interfaces.nsIHttpChannel);
            subject.setRequestHeader("Proxy-Id", uid, false);
        }
    }
};
