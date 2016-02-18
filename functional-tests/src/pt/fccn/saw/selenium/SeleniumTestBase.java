/*
    Copyright (C) 2011 David Cruz <david.cruz@fccn.pt>
    Copyright (C) 2011 SAW Group - FCCN <saw@asa.fccn.pt>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package pt.fccn.saw.selenium;

import com.thoughtworks.selenium.*;

import org.junit.After;
import org.junit.Before;


/**
 * The base class for tests using Selenium (now known as Selenium 1).
 * This test read system properties to know if the tests are to be run
 * locally or remotely. It also read login information (for remote tests)
 * and the browser, browser version and OS combination to be used.
 *
 * Selenium has some restrictions due to the security models of browsers.
 * Yet, Selenium 1 has a better support from tools and libraries.
 */
public class SeleniumTestBase extends SeleneseTestBase {
        private static String browserVersion;

	/**
	 * Start and setup a Selenium Remote Connection.
	 * This method decide if we connect to localhost or to
	 * a remote host.
	 *
	 * This method is run before each tests. 
	 */
	@Before
	public void setUp() throws Exception {
                String url = System.getProperty("test.url");
                String username = System.getProperty("test.remote.access.user");
                String apiKey = System.getProperty("test.remote.access.key");
                String browser = System.getProperty("test.browser");
        	browserVersion = System.getProperty("test.browser.version");
                String os = System.getProperty("test.os");
		String projectName = System.getProperty("test.project.name");

		DefaultSelenium selenium = null;

                // If SauceLabs keys aren't available, run locally
                if(username == null || apiKey == null){
		    selenium = new DefaultSelenium("localhost", 4444, browser, url);
                } else {
		    parameterCleanupForRemote(browser, browserVersion);		

                    selenium = new DefaultSelenium(
                    "ondemand.saucelabs.com",
                    80,
                    "{\"username\": \"" + username + "\"," +
                    "\"access-key\": \"" + apiKey + "\"," +
                    "\"os\": \"" + os + "\"," +
                    "\"browser\": \"" + browser + "\"," +
                    "\"browser-version\": \"" + browserVersion + "\"," +
                    "\"name\": \""+ projectName +" - "+ this.getClass().getName() +"\"}",
                    url);
		}

		selenium.start();
		this.selenium = selenium;
	
		// set the browser to the starting test URL	
		selenium.open("/");
		selenium.waitForPageToLoad("30000");
	}

	/**
	 * Closes connection.
	 */
	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Miscellaneous cleaning for browser and browser's version strings.
	 * @param browser The browser string to clean
	 * @param browserVersion The browser version string to clean
	 */
	private void parameterCleanupForRemote(String browser, String browserVersion) {
		// Selenium1 likes to prepend a "*" to browser string.
		if (browser.startsWith("*")) {
			browser = browser.substring(1);
		}

		// SauceLabs doesn't use version numbering for Google Chrome due to
		// the fast release schedule of that browser.
		if (browser.contains("googlechrome")) {
			this.browserVersion = "";
		}
	}
}
