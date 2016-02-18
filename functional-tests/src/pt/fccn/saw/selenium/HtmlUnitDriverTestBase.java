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

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;

import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import org.junit.After;
import org.junit.Before;



/**
 * The base class for tests using the HtmlUnitDriver.
 * HtmlUnitDriver is a pure-java WebDriver that reduces initialization overhead
 * Compared to Browser-based WebDrivers and uses less resources.
 * The downside is that, when activated, JavaScript/DOM behavior is an approximation
 * of what you may find in web browsers.
 */
public class HtmlUnitDriverTestBase {
	protected static WebDriver selenium;
	protected static String testURL;

	/**
	 * Start and setup a HtmlUnitDriver.
	 * This is run before each test.
	 */
	@Before
	public void setUp() throws Exception {
		testURL = System.getProperty("test.url");

		selenium = new HtmlUnitDriver();
		// Set the default time to wait for an element to appear in a
		// webpage.
		selenium.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

		selenium.get( testURL );
	}
	
	@After
	public void tearDown() throws Exception {
        	selenium.quit();
	}

	/**
	 * Enable Javascript interpretation by HtmlUnitWebDriver
	 * 
	 * HtmlUnitWebDriver used Rhino for Javascript/DOM interpretation
	 * Thus the results won't be the same as if we were using a browser
	 * @param enable Enable JavaScript interpretation.
	 */
	protected void enableJavascript(boolean enable) {
		if (selenium != null)
			((HtmlUnitDriver)selenium).setJavascriptEnabled(enable);
	}
}
