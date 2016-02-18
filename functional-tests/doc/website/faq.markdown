Title: Selenium Grid FAQ
CSS: stylesheets/site.css stylesheets/document.css stylesheets/faq.css
Use numbered headers: false

<div class="header">
  <a href="index.html"><img alt="Selenium_grid_logo_large" src="images/selenium-grid-logo-large.png"/></a>
  <p>FAQ</p>
</div>

Table Of Content:
{: class=toc}

* This will become a table of contents (this text will be scraped).
{:toc}

General
=======

 Would you recommend using Selenium Grid for performance/Load testing?
 ---------------------------------------------------------------------

  Selenium Grid is not designed for performance and load testing, but very
  efficient web acceptance/functional testing. The main reason for this is
  that conducting performance/load testing with real browser is a pretty bad
  idea as it is hard/expensive to scale the load and the actual load is very
  inconsistent.

  For load/performance testing I would advise using tools like JMeter, Grinder
  or httperf. What you can do though, is reuse your selenium tests to record
  the use cases you will use for your load testing. If you really want to 
  conduct load testing with Selenium, check out [Browser Mob](http://browsermob.com/welcome).

  To simulate 200 concurrent users for instance, you would need 200 concurrent
  browsers with a load testing framework based on Selenium Grid. Even if you
  use Firefox on Linux (so the most efficient setup) you will probably need at
  least 10 machines to generate that kind of load. Quite insane when
  JMeter/Grinder/httperf can generate the same kind of load with a single
  machine.

Installing
==========

  Do you have step by step, "assume I am an idiot", installation instructions?
  ------------------------------------------------------------------------------
  
   First if you are trying out Selenium Grid you are certainly *not* an idiot. ;-)
   Second, yes we do have step by step installation instructions, check out:
  
* [Step by Step Installation Instructions for Windows](./step_by_step_installation_instructions_for_windows.html)
* [Step by Step Installation Instructions for Mac OS X](./step_by_step_installation_instructions_for_osx.html)


Running the Demo
================

  Tests Are Failing When I Run the Demo. How can I troubleshoot the problem?
  --------------------------------------------------------------------------
  
> I started the Hub on the UNIX machine and then I've created 2 
> remote controls on a Window laptop. This part worked and I can see the
> Hub console listing the 2 remote controls. 
> When I start `ant run-demo-in-parallel` on the UNIX box, however, the 
> following error appears:
>
    [java] ===============================================
    [java] Selenium Grid Demo In Parallel
    [java] Total tests run: 4, Failures: 4, Skips: 0
    [java] ===============================================
>
> How can I investigate the problem?

  Here are a few things you can do to try to understand what is going on:
  
* Look at the TestNG report under `target/reports/index.html` on the test 
  runner machine (the UNIX one in your case), looking for the actual error 
  messages / stack traces.

* Check the Hub and remote control logs under the `log/` directory

* Look at the Windows machine where the remote controls are running: Are the
  remote controls logging any command at all? Are browsers popping up at
  least? Take a good look at the Selenium Remote Control logs while the tests
  are running.

  If the remote controls on the Windows machine are not even contacted, this
  is most likely to be a setting/network/browser configuration problem. Check
  the way you launched the hub and the remote controls, as well as your
  network.

  If the remote controls on the windows machine are contacted but fail to run
  the tests properly, this is probably just a problem on the local machine.
  Try to run Selenium Grid demo exclusively on this machine (or any Selenium
  Test you have) to understand the nature of the problem in a less complex
  environment.

  If finding the actual problem still end up being hard, please 
  [post a message in the Selenium Grid forum](http://clearspace.openqa.org/community/selenium/advanced)
  with precise information on:

* The O.S. and browser combination that you are using
* Whether you see browsers popping up or not
* A screenshot of the Hub console before you launch the tests
  (The console can usually be accessed at http://localhost:4444/console
   on the machine running the Hub)
* The test client side errors (TestNG reports)
* Selenium Grid Hub logs (`log/hub.log`)
* Remote Control logs (`log/rc-*.log`)


 Running the Demo Using a Different Browser
 ------------------------------------------
 
> When i run the selenium grid demo then all remote control by default launched Firefox.
> so how and where could I update the browser name incase of Selenium Grid.


Here is how you would do to run the demo with the Safari web browser for instance.

Launch the Hub the standard way:

    ant launch-hub

Launch a remote control declaring that it provides the Safari environment:

    ant -Denvironment="*safari" launch-remote-control

Launch the other remote controls the same way:

    ant -Denvironment="*safari" -Dport=5556 launch-remote-control
    ant -Denvironment="*safari" -Dport=5557 launch-remote-control

Launch the demo overriding the browser system property:

      ant -Dbrowser="*safari" run-demo-in-parallel
    
Voila! you're done!

If you are using Ruby automation (power to you), you can also achieve the
same thing easily:

Launch the Hub the standard way:

    rake hub:start

Launch the remote controls declaring that they provides the Safari environment:

    rake rc:start_all ENVIRONMENT=*safari

Launch the demo overriding the browser system property:

    ant -Dbrowser="*safari" run-demo-in-parallel


Customizing Selenium Grid
=========================

  Can I change the version of Selenium RC used by the Grid?
  ---------------------------------------------------------
  
  Every remote control that you launch with `ant launch-remote-control` or
  `rake rc:start` is using the first Selenium Remote Control standalone jar 
  packaged in the `vendor` directory whose name matches `selenium-server-*.jar`.
   
  To use your own version of Selenium Remote Control, just
  delete the `vendor/selenium-server-*.jar` file that comes with Selenium
  Grid distribution and replace it any standalone Remote Control jar that 
  you fancy! This technique is especially useful to workaround
  regressions in Remote Control nightly builds...
  
Analysing Failures
==================


 When we test the application with Selenium Grid, we get nondeterministic results
 --------------------------------------------------------------------------------

> Locally, when we test the application with Selenium Grid, we get 
> nondeterministic results. Tests seem to fail randomly. Messing with the 
> number of nodes in the grid seems to help, but its really annoying that we 
> can't seem to get consistent results.

  Most likely some tests are timing out in a non-deterministic manner because
  your CPU or Network is over-utilized. Monitor your CPU and Network activity on
  all the machines involved. Once you find the bottleneck launch fewer
  processes. For instance if your load average is way higher than the number of
  CPUs on the machine running the remote controls, cut the number of remote
  controls you launch by two until you get to a sustainable machine load.

  Make sure you spend some time figuring out the optimal number of 
  concurrent test runners and remote controls to run in parallel on each 
  machine, before deploying a Selenium Grid infrastructure your organization
  is going to depend on.


Managing the Hub and the Remote Controls
=========================================

 How can I shutdown Selenium Grid Hub?
 -------------------------------------
 
  If you are using the rake task provided with Selenium Grid you can just
  run:

    rake hub:stop
    
  If Rake is not an option or you want to hook your instrumentation logic
  you can also shutdown the Hub with a single HTTP request. You need to 
  submit a POST request to the Hub targeting `/lifecycle-manager` with a
  parameter `action=shutdown`. For instance if the Hub is running on
  `localhost` on port `4444` you could shut it down with:
  
    curl -d action=shutdown http://localhost:4444/lifecycle-manager
    
  or

    wget --post-data action=shutdown action=shutdown http://localhost:4444/lifecycle-manager


 How can I pass additional parameters to the Remote Controls when starting them as part of Selenium Grid?
 --------------------------------------------------------------------------------------------------------

  Additional parameters can be passed to Selenium remote controls at startup

  Just set the `seleniumArgs` Java property when launching the remote control. 
  For instance, to start a remote control in multi window and debug mode you 
  would use:

    ant -DseleniumArgs="-multiWindow -debug" launch-remote-control

  Of course you can also achieve the same thing with the Rake task:

    rake hub:start SELENIUM_ARGS="-multiWindow -debug"


 How can I use the `-firefoxProfileTemplate` option?
 ---------------------------------------------------

  On OS X or UNIX, you can use a custom Firefox profile with Selenium
  Grid by launching the remote controls with the `-firefoxProfileTemplate`
  option:

    ant -DseleniumArgs="-firefoxProfileTemplate /path/to/my_profile" launch-remote-control

  On Windows figuring out the right path to provide for your profile
  can be confusing espcially if you file path contains spaces
  (like `\Documents and Settings`). Just make sure you are
  providing the short filename (visible with a `dir /x`) and everything
  will be fine.

 I need to run the Hub and Remote Control in background.... How can I do it?
 ------------------------------------------------------------------------

  On UNIX you just add a ampersand at the end of the command line. See:

 * [Working with the UNIX shell](http://www.washington.edu/computing/unix/startdoc/shell.html)
 * [`nohup`](http://en.wikipedia.org/wiki/Nohup)
  
  On Windows you can use "start /wait/ /b" : Check out the [`start` command reference](http://www.ss64.com/nt/start.html) for more details.
  
  This said, if you are running on a UNIX platform or Mac OS X, the 
  easiest way to start the Hub and Remote Controls is to use the Rake 
  tasks that come with Selenium Grid distribution. 
  `cd` to the root of the Selenium distribution and type:
  
      rake hub:start BACKGROUND=true
  
  Which will launch the hub in the background. You can then launch
  remote controls in the background with:
  
      rake rc:start PORT=4445 BACKGROUND=true
  
  In practice, it is actually easier to launch the hub and all 
  the remote controls in the background with a single command:
  
      rake all:start
  
  Of course you can also stop them all in a similar way:
  
      rake all:start

 When starting Firefox I get: java java.lang.RuntimeException: Firefox refused shutdown while preparing a profile"
 ------------------------------------------------------------------------------------------------------------------

> Here is my log on this error:
> ...
> java Caused by: org.seleniumhq.selenium.server.browserlaunchers.FirefoxChromeLauncher$FileLockRemainedException: Lock file still present! C:\DOKUME1\Semadou\LOKALE1\Temp\customProfileDir9d4a3879bb7d4ca5b75dbbb488ec30b1\parent.lock


  Sometimes Selenium Remote Control does not stop Firefox properly on Windows
  and things get very messy (leaving lock files behind). This does happen when
  you Ctrl-C while running the test suite for instance.

  If you encounter this problem, I would advise you to:

* Kill all running Firefox instances and make sure that there is no Firefox process in the task manager (or even better reboot)
* Delete all the directories: `C:\DOCUME1\<your login>\LOCALS1\Temp\customProfileDir*`
* While you are at it cleanup `C:\DOCUME1\<your login>\LOCALS1\Temp as much as possible`
* Run your tests or the demo again

 Why do I have duplicate entries in the Hub after restarting my Remote Controls?
 ------------------------------------------------------------------------

> Say I have a hub setup with three RCs ready and waiting, and then I
> go and restart those RCs, my hub now shows 6 RCs even though there are
> actually only 3. Is this anything to worry about? Will the hub try
> to send requests to the "dead" entries? Do I have to restart the hub
> every time I need to restart and RC?

  You are probably stopping the remote controls a little too "harshly"? For
  instance, if you do a `kill -9` or use Windows task manager, the JVM shutdown
  hook does not have a chance to unregister the remote control and the Hub
  does not realize that the remote control is gone. 
  
  But if you stop the remote control in a more "civil" manner everything should
  be fine (e.g. `kill` or using the shutdown method in selenium client api). 
  Even better use the Rake tasks provided with Selenium Grid distribution:
  `rake rc:start_all` and `rake rc:stop_all` multiple times on my machine. 
  
  If you end up in this state I do recommend that you restart the Hub which
  would otherwise end up in a "non predictable state". Future releases of
  Selenium Grid will take care of this problem transparently, but for now a
  restart is safer.

 Can I configure the Remote Control to use a custom HTTP/HTTPS proxy?
 --------------------------------------------------------------------

  To use custom http proxy settings set the `http.proxyHost` and the
  `http.proxyPort` java system properties when starting the remote
  controls and the hub. 
  
  For instance:
  
    ant -Dhttp.proxyHost=my_proxy.my_company.com -Dhttp.proxyPort=3128 launch-hub
    ant -Dhttp.proxyHost=my_proxy.my_company.com -Dhttp.proxyPort=3128 launch-remote-control

  Note that the JVM use different properties for http and https
  proxies. So if you also want to use the same proxy for http *and* https 
  you need to use:

      ant -Dhttp.proxyHost=my_proxy.my_company.com -Dhttp.proxyPort=3128 \
          -Dhttps.proxyHost=my_proxy.my_company.com -Dhttps.proxyPort=3128 \
          launch-hub

      ant -Dhttp.proxyHost=my_proxy.my_company.com -Dhttp.proxyPort=3128 \
          -Dhttps.proxyHost=my_proxy.my_company.com -Dhttps.proxyPort=3128 \
          launch-remote-control  

Running the Examples Included in Selenium Grid Distribution
===========================================================

 How to run the Java example?
 ----------------------------

1. Go to the root directory of your Selenium Grid distribution

2. Launch Selenium Grid Hub and 4 remote controls as explained in
   ["Run the Demo"](http://selenium-grid.seleniumhq.org/run_the_demo.html)

3. Go to the Java example directory: `cd ./examples/java`

4. Launch the tests with: `ant run`  

 How to Run the Ruby Example?
 ----------------------------

1. Go to the root directory of your Selenium Grid distribution

2. (Re)start Selenium Grid Hub and the remote controls with:
   `rake all:restart`

3. Go to the Ruby example directory: `cd ./examples/ruby`

4. Launch the tests with: `rake tests:run_in_parallel`  


 The Ruby Example Does Not Seem to Work on Windows!
--------------------------------------------------

> When running the Ruby example on Windows I get:
>
> `[DeepTest] Started DeepTest service at druby://0.0.0.0:6969
> c:/ruby/lib/ruby/gems/1.8/gems/deep_test-1.2.2/lib/deep_test.rb:15:in fork: the fork() function is unimplemented on this machine (NotImplementedError)`

  This is expected. The Ruby example will *not* work on Windows.

  The Ruby example demonstrates best practices for high ROI in-browser web
  testing in Ruby. As a consequence it relies on
  [DeepTest](http://deep-test.rubyforge.org), the best parallel and distributed
  test runner available for Ruby. In turns, DeepTest make extensive use of
  `Kernel.fork()`... which is not implemented on Windows.

  This is not really a problem as the Ruby community has widely embraced the
  Mac OS X and UNIX platforms which provide a far better environment to
  execute your tests. Besides, with Selenium Grid (or Selenium RC) it is very
  simple to run your tests on UNIX while driving a web browser running on
  Windows. So there is no need to run your tests on Windows to test a Windows'
  browser. Consequently I strongly advise to always run your tests on Linux or
  Mac OS X and just target a Selenium RC running on a Windows platform when
  you need to test Internet Explorer. You will also save yourself a lot of
  headaches as very few Ruby users run on Windows anyway...
 

 Running the Ruby Example Using a Different Browser
 --------------------------------------------------
 
  Here is how you would do to run the Ruby example with the Safari web browser for instance.

  Launch the Hub the standard way (from the root of Selenium Grid distribution):

    rake hub:start

  Launch a  bunch of remote controls declaring that they provides the Safari environment:

    rake rc:start_all ENVIRONMENT=*safari

  Launch the tests overriding the browser environment variable:

      cd examples/ruby
      rake tests:run_in_parallel SELENIUM_RC_BROWSER="*safari"
    
  Voila! you're done!


Running Your Tests Against Selenium Grid
========================================

I have some test cases and I want to run them against Selenium Grid, what do I need to do?
------------------------------------------------------------------------------------------

  The idea is that all you have to do to take advantage of the Selenium Grid
  is to point your Selenium client driver to the Hub and run your tests in
  parallel.

### Java ###
  
  If you writing your tests using Java, the best is to run your
  tests with [TestNG parallel runner](http://testng.org/doc/documentation-main.html#parallel-running). 

  You can find a concrete example on
  how this can be achieved in the standard Selenium Grid distribution under
  the [`examples/java`](http://svn.openqa.org/svn/selenium-grid/trunk/examples/java/) 
  directory.

### Ruby ###

  If you use Ruby, the best is to use
  [DeepTest](http://deep-test.rubyforge.org) which can even distribute the test run 
  accross multiple machines.

  You can find a concrete example (a nice test reports) on
  how this can be achieved in the standard Selenium Grid distribution under
  the [`examples/ruby`](http://svn.openqa.org/svn/selenium-grid/trunk/examples/ruby/) 
  directory.

### Python ###

   You need to come up with a way to run your python tests in parallel.
   Saucelab has 
   [blog post](http://saucelabs.com/blog/index.php/2009/09/running-your-selenium-tests-in-parallel-python/)
   that should help you start on the right track.

### .Net ###

  I have no expertise in .Net but [Gallio](http://www.gallio.org/)
  seems to be able to [run tests in parallel](http://igorbrejc.net/development/continuous-integration/gallio-running-tests-in-parallel)
  
### Other ###

  To take advantage of Selenium Grid power, you need to come up with a way to
  run your tests in parallel. How exactly you achieve this usually depends on
  your testing framework and programming language of choice. Try
  googling around for a parallel or distributed test runner for your language.

  If you cannot find any, your fallback plan is to write your own parallel
  test runner by launching multiple processes targeting different test file
  sets and checking the process exit statuses. Not the most elegant/efficient
  way, but that can get you started. This is actually the way I originally
  started with Ruby and you can find an example on how this worked in the Ruby
  example included in Selenium Grid distribution:

  [`examples/ruby/lib/multi_process_behaviour_runner.rb`](http://svn.openqa.org/svn/selenium-grid/trunk/examples/ruby/lib/multi_process_behaviour_runner.rb)

  You launch the whole thing with:

    #
    # Legacy way to drive tests in parallel before DeepTest RSpec support.
    # Kept to document a simple way to run the tests in parallel for non-Ruby
    # platforms.
    #
    desc("[DEPRECATED] Run all behaviors in parallel spawing multiple
    processes. DeepTest offers a better alternative.")
    task :'tests:run_in_parallel:multiprocess' => :create_report_dir do
     require File.expand_path(File.dirname(__FILE__) +
    '/lib/multi_process_behaviour_runner')
     runner = MultiProcessSpecRunner.new(10)
     runner.run(Dir['*_spec.rb'])
    end

 Is there a way to generate test reports using Selenium?
 -------------------------------------------------------

  The short answer is that yes you can generate test reports with Selenium.
  How to achieve this (and their exact format) will however depend on the
  programming language and test runner you are using (for instance 
  `JUnit`, `TestNG`, `Test::Unit` or `RSpec`).

  You can look at the [`examples/ruby`](http://svn.openqa.org/svn/selenium-grid/trunk/examples/ruby/) 
  directory in the Selenium Grid
  distribution to see how you can use RSpec and Selenium to generate reports
  which [include HTML capture and OS screenshots when a test
  fail](http://ph7spot.com/examples/rspec_report/index.html).

 My test cases are in HTML (Selenese), how can I run those against Selenium Grid ?
 ------------------------------------------------------------------

 You would need a parallel test runner for Selenium Grid.

 I might eventually end up working on such a parallel test runner for HTML 
 test suites, nevertheless my time is limited and this feature is quite low 
 in my priority list: in my experience HTML test suites are a nightmare 
 to maintain you are better off writing and refactoring real code by the time 
 your test suite grows big enough that it takes too long to run.
 
 This said, there might be hope as some guys seem to be working on it though: see 
 [this thread](http://clearspace.openqa.org/thread/11482)

 Here are more details on why this feature is not high on my priority list:

1. I write all my tests using a full-featured programming language 
   (not HTML) because I believe that it is a far better approach 
   to in-browser testing -- especially when it come to maintenance.

2. If you have enough tests to feel the need for Selenium Grid, 
   then test maintenance should matter to you... and using HTML test 
   cases is not going to help!

3. I have limited development cycles and work on Selenium grid in my 
   free time. There a lot of other features to work on that are more 
   important to me and my teams.

4. Selenium Grid is an open-source project. If this running HTML tests 
   in parallel is important enough to a large number of users, somebody 
   will write a patch that I will be glad to incorporate it in the codebase.
 
 
 My test is not working when I use HTTPS!
 ----------------------------------------

  Selenium and Selenium Grid support HTTPS out-of-the-box. Just make sure
  you are using one of the "privileged" browser modes,
  namely `*chrome`, `*hta` and `*safari`.

 How can I avoid SSL certificate popups?
 ---------------------------------------

  First make sure you are using a priviledged browser mode 
  (namely `*chrome`, `*hta` or `*safari`).

  The generic solution is to accept the Certificate manually the first
  time and run the test again. For Firefox the solution is actually a
  little more involved and will depend whether your SSL certificate is
  valid or not:

### Your SSL Certificate is Valid ###

1. Generate a Firefox [profile](http://support.mozilla.com/en-US/kb/Profiles)
    accepting the certificate:

   1. Start Firefox manually

   2. Go to the web page trigerring the certificate popup and 
      accept permanently the certification.

   3. Close Firefox.

2. Copy the Firefox profile you just changed to a new directory
  (eg `seleniumFirefoxProfile`). You will typically find the
  Firefox profile under (`~/Library/Mozilla/Profiles/`, 
  `~/.mozilla/firefox/` or `C:\Documents and settings\%USER%\Application Data\Mozilla\Profiles`).
              

3. Now start the Selenium Remote Control using the profile you just copied
   using the `-firefoxProfileTemplate` option.

    java -jar selenium-server-1.0.jar -firefoxProfileTemplate ~/seleniumFirefoxProfile

   Or if you are using Selenium Grid:

    ant -DseleniumArgs="-firefoxProfileTemplate ~/seleniumFirefoxProfile" launch-remote-control

### Your SSL Certificate is Invalid ###

 When you are running Selenium Grid against Developement or QA 
 environments you can run into invalid SSL certificated (expired
 certificate for instance).
                        
 In that case not only accept permanently the certificate but 
 also install the "Remember Mismatch" Firefox plugin when generating
 the firefox profile you will use for Selenium. 

 If you are desperate, there is another solution (quite brutal): 
 When you generate the Firefox profile to use for Selenium, 
 type about:copy in the browser address line. Then Search every 
 `security.warn` attribute and set it to false!
                           
 I get some strange errors when I run multiple Internet Explorer instances on the same machine
 ---------------------------------------------------------------------------------------------

 Selenium Grid does not officially support running multiple Internet
 Explorer on a _single_ Windows machine. This is mostly because:

* People who know IE better than I do (Dan Fabulich) tell me that if
you run 2 browsers as the same user in HTA mode they end up sharing a
singleton instance in memory, which could cause problems.

* The `*iexplore` mode is changing the registry settings at each
session start/end to have IE use a specific Remote Control as HTTP
proxy. If you run multiple Remote Controls at the same time you can
see the problems coming! ;-)

**Currently, the only robust solution for running multiple IE instances
on a single machine with Selenium Grid is to use virtualization** 
(multiple VMs, a single IE instance per VM).

 This said, I am not satisfied wit the current state of affairs and I
 am currently working on better support for IE in Selenium Grid 1.2.

Development
===========

 Where can I find Selenium Grid nightly builds?
 ----------------------------------------------

  Download them from Selenium Grid [core build artifacts](http://xserve.seleniumhq.org:8080/view/Selenium%20Grid/job/Grid%20Core%20-%20Mac/lastSuccessfulBuild/artifact/trunk/target/dist/)

 Where Can I Get Feedback Selenium Grid on Continuous Integration Builds?
 ------------------------------------------------------------------------
  
  Check out latest Selenium Grid builds on 
  [http://xserve.seleniumhq.org:8080/view/Selenium%20Grid](http://xserve.seleniumhq.org:8080/)


