Title: Selenium Grid Self-Healing
CSS: stylesheets/site.css stylesheets/document.css stylesheets/faq.css
Use numbered headers: false

<div class="header">
  <a href="index.html"><img alt="Selenium_grid_logo_large" src="images/selenium-grid-logo-large.png"/></a>
  <p>Self-Healing</p>
</div>

Table Of Content:
{: class=toc}

* This will become a table of contents (this text will be scraped).
{:toc}

Heartbeat Mechanism
===================

 To ease the maintenance of a Selenium Grid deployment:

 * Selenium Grid Hub sends periodic heartbeat messages to all registered
   Remote Controls to make sure that they are still up-and-running.

 * Selenium Grid Remote Controls send periodic heartbeat messages to the Hub
   to check that it still considers them as registered.

  Keep in mind that **there is obviously a performance tradeoff in how often
  these heartbeat messages are sent**:

 * If you send them too often they will use most of your network bandwidth for
   heartbeat management messages as opposed to actual testing commands.

 * If you wait too long to send heartbeat messages, it might take longer for
   the Hub and the Remote Controls to automatically unregister or reconnect.

  
Hub: Automatic Unregistering of Unresponsive Remote Controls
===========================================================

  Remote Controls sometimes die, are killed or just hang. To better
  cope with these situations, Selenium Grid Hub checks the status of registered
  Remote Controls. It will automatically unregister:

 * A Remote Control detected as **unavailable when reserving it to start a new
   test session**

 * A Remote Control that **does not respond to a heartbeat message request**
   (periodic poll every `remoteControlPollingIntervalInSeconds` seconds,
   default is 2 minutes)

 * A _reserved_ Remote Control whose **testing session has been idle for more
   than `sessionMaxIdleTimeInSeconds` seconds** (3 minutes by default).
   Sessions are checked every `remoteControlPollingIntervalInSeconds` seconds
   (at the same time heartbeat messages are sent).

 `remoteControlPollingIntervalInSeconds` and `sessionMaxIdleTimeInSeconds` are 
 configuration parameters that can be set in `grid_configuration.yml`. For instance:

    hub:
       port: 4444
       remoteControlPollingIntervalInSeconds: 180
       sessionMaxIdleTimeInSeconds: 300
       environments:
           - name:    "Firefox on Windows"
             browser: "*firefox"
      

Remote Control: Automatic Re-registering to the Hub
===================================================

  Sometimes you might want to restart Selenium Grid Hub without restarting all
  the Remote Controls that were previously registered. Or the Hub might
  temporarily get disconnected from the remote controls. To better cope with
  these situations, the Remote Controls periodically poll the Hub (every
  `hubPollerIntervalInSeconds` seconds, default is 3 minutes).
  
  If the Hub can be contacted but does not have any registered Remote Control 
  matching the information sent by the Remote Control submitting the
  heartbeat request, **the Remote Control will re-register itself to the Hub
  automatically**.

  You can specify `hubPollerIntervalInSeconds` when starting the 
  Remote Controls. For instance:

    ant -DhubPollerIntervalInSeconds=120 launch-hub  


