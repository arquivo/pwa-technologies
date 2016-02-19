Recursive tests developed with selenium framework for Arquivo.pt
---------------

For lauching selenium tests
------------------
```
  ant launch-server
  ant test -Dtestcase=<test-class-path> ex:pt.fccn.arquivo.tests.CollectionsTest 
  ant test -Dtestcase=pt.fccn.arquivo.tests.AllTests -Dtest.url=http://arquivo.pt
```
Documentation
--------------

  All of the tests developed  are described in 
```
http://wiki.priv.fccn.pt/Testes#Funcional_Tests_.28Selenium.29.
```
Hello world tests

```
 http://wiki.priv.fccn.pt/Create_plata_test
```
The source code takes into consideration that Arquivo.pt has one machine for PRODUTION and other for PRE-PRODUTION. By default, pre-prod machine is p41 and p58 and p62 are the prodution machines.
If there a need to change pre-prod machine, you could change variable pre_prod on class WebDriverTestBase to whatever you want.
On the root there are two configuration files, which contain collections to be fetch in order to guarantee that each collection are online with the correct order. 
Files are named:
```
monitored_indexes_pre_prod - Website whichs that are acessable through pre-prod server.
monitored_indexes - Websites that are on prodution.
```
Firefox Addon to aid producing selenium tests
-------------------------------
```
https://addons.mozilla.org/en-US/firefox/addon/selenium-ide/
```
Tests from selenium IDE should be exported has: "Java / JUnit 4 / WebDriver"


Test in saucelabs:
-----------------
```
  SAUCE_ONDEMAND_USERNAME="sawfccn" SAUCE_ONDEMAND_ACCESS_KEY="f4090c57-6ef7-4b83-9951-e30475d2a518" ant test -Dtestcase=pt.fccn.arquivo.tests.CollectionsTest
```
