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

// Configuration object
var waxConfig;

function openConfig() {
  window.open("chrome://waxtoolbar/content/waxconfig.xul", "waxconfig",  "chrome,width=400,height=300");
}

function initConfig() {
  waxConfig = new WaxConfigObject();
  document.getElementById("WAX_ProxyHost_Text").value = waxConfig.proxyHost;
  document.getElementById("WAX_ProxyPort_Text").value = waxConfig.proxyPort;
  document.getElementById("WAX_NutchHost_Text").value = waxConfig.nutchHost;
  document.getElementById("WAX_NutchPort_Text").value = waxConfig.nutchPort;
}

function configOKClicked() {
  waxConfig.proxyHost = document.getElementById("WAX_ProxyHost_Text").value;
  waxConfig.proxyPort = document.getElementById("WAX_ProxyPort_Text").value;
  waxConfig.nutchHost = document.getElementById("WAX_NutchHost_Text").value;
  waxConfig.nutchPort = document.getElementById("WAX_NutchPort_Text").value;
  self.close();
}

function configCancelClicked() {
  self.close();
}

function checkboxAction() {
  if(sameConfig()) {
    updateNutchConfig();
  }
  else {
    var nutchHost = document.getElementById("WAX_NutchHost_Text").disabled = false;
    var nutchPort = document.getElementById("WAX_NutchPort_Text").disabled = false;
  }
}

function textUpdated() {
   if(sameConfig()) {
    updateNutchConfig();
  }
}

function updateNutchConfig() {
  var nutchHost = document.getElementById("WAX_NutchHost_Text");
  var nutchPort = document.getElementById("WAX_NutchPort_Text");
  nutchHost.value = document.getElementById("WAX_ProxyHost_Text").value;
  nutchHost.disabled = true;
  nutchPort.value = document.getElementById("WAX_ProxyPort_Text").value;
  nutchPort.disabled = true;
}

function sameConfig() {
  return document.getElementById("WAX_SameConfig_Checkbox").checked;
}

// Configuration class
function WaxConfigObject() {
  this.prefs = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefBranch);
}

WaxConfigObject.prototype = {
    get proxyHost() {
        return this.prefs.getCharPref("wax.proxy.host");
    },

    set proxyHost(host) {
        this.prefs.setCharPref("wax.proxy.host", host);
    },

    get proxyPort() {
      return this.prefs.getCharPref("wax.proxy.port");
    },

    set proxyPort(port) {
        this.prefs.setCharPref("wax.proxy.port", port);
    },

    get nutchHost() {
      return this.prefs.getCharPref("wax.nutchwax.host");
    },

    set nutchHost(host) {
        this.prefs.setCharPref("wax.nutchwax.host", host);
    },

    get nutchPort() {
      return this.prefs.getCharPref("wax.nutchwax.port");
    },

    set nutchPort(port) {
        this.prefs.setCharPref("wax.nutchwax.port", port);
    }
};
