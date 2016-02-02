Title: Selenium Grid FAQ
CSS: stylesheets/site.css stylesheets/document.css stylesheets/faq.css
Use numbered headers: false

<div class="header">
  <a href="index.html"><img alt="Selenium_grid_logo_large" src="images/selenium-grid-logo-large.png"/></a>
  <p>Configuring and Tuning</p>
</div>

Table Of Contents:
{: class=toc}

* This will become a table of contents (this text will be scraped).
{:toc}

Selenium Grid Main Configuration File
=====================================

  Once you have Selenium Grid up and running, you might want to tweak some of
  its settings.

  Most of Selenium Grid configuration is defined in the
  `grid_configuration.yml` file located at the root of Selenium Grid
  installation directory *on the machine where the Hub is running*.

  This file is in the human-friendly YAML format, which should be mostly
  self explanatory.
  
Defining New Environments
=========================

  Selenium Grid provides predefined environments for standard browsers
  to get you started, however, chances are that very soon, you will want 
  to define your own.

  To define a new environment, edit the `grid_configuration.yml` file 
  located at the root of Selenium Grid installation directory 
  *on the machine where the Hub is running*:
  
  Search for a section like:

    hub:
	   port: 4444
	   # ...
	   environments:
	       - name:    "Firefox on Windows"
	         browser: "*firefox"
	       - name:    "Firefox on OS X"
	         browser: "*firefox"
	       - name:    "IE on Windows"
	         browser: "*iehta"
	
  An environment is basically a binding between an arbitrary string you pick (the name)
  and a real Selenium browser string that will be used to start the session on the
  actual remote control (`*firefox`, `safari`, `*iexplore`, `*hta`, ...). To add
  a new one, just add a new entry in the list, starting with a dash (`-`) and keeping the
  same indentation level. For instance, to add a new environment called 
  "Shinny New Environment" that will use the `*safari` mode type:

      hub:
         port: 4444
         # ...
         environments:
             - name:    "Firefox on Windows"
               browser: "*firefox"
             - name:    "Firefox on OS X"
               browser: "*firefox"
             - name:    "IE on Windows"
               browser: "*iehta"
             - name:    "Shinny New Environment"
               browser: "*iehta"

    
Changing Hub's Port
===================

  By default, Selenium Grid Hub is started on port 4444, which is
  also Selenium RC default port (this is on purpose, since, from the test's
  point of view, the Hub looks just like a regular RC).

  You can change the Hub port by editing the `grid_configuration.yml` file, which is
  located at the root of Selenium Grid installation directory *on the machine
  where the Hub is running*:

  Search for a section like:

    hub:
	   port: 4444	   

  And change `4444` to the port number of your liking. For instance:

    hub:
         port: 1234

Changing Hub Self-Healing Parameters
====================================

  `remoteControlPollingIntervalInSeconds` and `sessionMaxIdleTimeInSeconds` 
  configuration parameters can be set in `grid_configuration.yml`.
  For instance:

     hub:
        port: 4444
        remoteControlPollingIntervalInSeconds: 180
        sessionMaxIdleTimeInSeconds: 300
        environments:
            - name:    "Firefox on Windows"
              browser: "*firefox"
  
 * `remoteControlPollingIntervalInSeconds` : is how often the Hub will
   check for registered Remote Controls status and idle testing sessions.
 
 * `sessionMaxIdleTimeInSeconds` : is how long a testing session can be idle
   before the Hub automatically unregisters the associated Remote Control.

Changing Remote Control Self-Healing Parameters
===============================================

  When starting the Remote Control you can set `hubPollerIntervalInSeconds`
  to control how often the Remote Controls will check the Hub status
  and automatically re-register themselves in case the Hub does not list
  them as registered. 
  
  e.g.
  
    ant -DhubPollerIntervalInSeconds=120 launch-hub  

Changing Maximum Wait Time for New Session
==========================================

  By default the Hub will block until a Remote Control becomes available when
  requesting a new browser session.  This can be problematic if the requesting
  client times out.  Since the Hub is unaware of this timeout, it will request
  the new session anyway and that session will become effectively orphaned.

  If your client cannot wait for the session you may change the maximum time the
  Hub will wait in `grid_configuration.yml`.  When the `newSessionMaxWaitTimeInSeconds`
  value is exceeded, the Hub will return an Error indicating that no Remote Controls
  were available to fulfill the request.

  For instance:

      hub:
         newSessionMaxWaitTimeInSeconds: 120

  This sample configuration will instruct the Hub to block for up to 2 minutes
  while waiting for a Remote Control to become available to handle the new session
  request.

  _NB: If this configuration value is not provided, the default value of infinite
  will be used._