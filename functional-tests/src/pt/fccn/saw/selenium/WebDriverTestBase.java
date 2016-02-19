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

import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.NoSuchElementException;




import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.By;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * The base class for tests using WebDriver to test specific browsers.
 * This test read system properties to know which browser to test or,
 * if tests are te be run remotely, it also read login information and
 * the browser, browser version and OS combination to be used.
 * 
 * The WebDriver tests provide the more precise results without the
 * restrictions present in selenium due to browers' security models.
 */
public class WebDriverTestBase{
    protected static WebDriver driver;
    protected static String testURL;
    protected static String browserVersion;
    protected static String titleOfFirstResult;
    protected static  String pre_prod="p41";
   //protected static  String pre_prod="p24";
    protected static boolean Ispre_prod=false;

    /**
     * Start and setup a WebDriver.
     * This method first check if we want a local or remote WebDriver.
     * If a local WebDriver is desired, it initialize the correct one.
     * If a remote WebDriver is desired, it configures and initialize it.
     *
     * This method is run only once per test class so we prevent the
     * overhead of initialization for each test.
     */
    @BeforeClass
    public static void setUp() throws Exception {
        // Read system properties.
        String username    = System.getProperty("test.remote.access.user");
        String apiKey      = System.getProperty("test.remote.access.key");
        String browser     = System.getProperty("test.browser", "*firefox");
        String os          = System.getProperty("test.os", "windows");
        browserVersion     = System.getProperty("test.browser.version", "16");
        String projectName = System.getProperty("test.project.name");
        testURL            = System.getProperty("test.url");
        
        //Decide which environment to choose
        // Decide if tests are to be run locally or remotely
        if(username == null || apiKey == null) {
            System.out.println("Run test localy");
            driver = selectLocalBrowser(browser);
        } else {
            System.out.println("Run test in saucelabs");
            parameterCleanupForRemote(browser, browserVersion);

            DesiredCapabilities capabillities = new DesiredCapabilities(
                    browser, browserVersion, selectPlatform(os));
            capabillities.setCapability("name", projectName +" - "+ new CurrentClassGetter().getClassName());
            capabillities.setCapability("record-screenshots", true);
            capabillities.setCapability("sauce-advisor", false);

            driver = new RemoteWebDriver(
                    new URL("http://"+ username +":"+ apiKey +"@ondemand.saucelabs.com:80/wd/hub"),
                    capabillities);
        }
        // Set the default time to wait for an element to appear in a webpage.
        driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
        
    }

    /**
     * This method is run before each test.
     * It sets the browsers to the starting test url
     */
    @Before
    public void preTest() {
        driver.get( testURL );
    }

    /**
     * Releases the resources used for the tests, i.e.,
     * It closes the WebDriver.
     */
    @AfterClass
    public static void tearDown() throws Exception {
        driver.quit();
        
    }

    /**
     * Creates a Local WebDriver given a string with the web browser name.
     * 
     * @param browser The browser name for the WebDriver initialization
     * @return The initialized Local WebDriver
     */
    private static WebDriver selectLocalBrowser(String browser) throws java.net.MalformedURLException{
        WebDriver driver = null;
        if (browser.contains("firefox")) {
            driver = new FirefoxDriver();
        } else if (browser.contains("iexplorer")) {
            driver = new InternetExplorerDriver();
        } else if (browser.contains("chrome")) {
            //DesiredCapabilities capabilities = DesiredCapabilities.chrome();
            //capabilities.setCapability("chrome.binary", "/usr/lib/chromium-browser/chromium-browser");
            //driver = new ChromeDriver(capabilities);
            driver = new ChromeDriver();
        } else if (browser.contains("opera")) {
            driver = new OperaDriver();
        } else if (browser.contains("remote-chrome")) {
            DesiredCapabilities capabilities = DesiredCapabilities.chrome();
            driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), capabilities); 
        } else if (browser.contains("remote-firefox")) {
            DesiredCapabilities capabilities = DesiredCapabilities.firefox();
            driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), capabilities); 
            driver.get("http://www.google.com");
        } else {
            // OH NOEZ! I DOAN HAZ DAT BROWSR!
            System.err.println("Cannot find suitable browser driver for ["+ browser +"]");
        }		
        return driver;
    }

    /**
     * Gets a suitable Platform object given a OS/Platform string..
     *
     * @param platformString The given string for the OS/Platform to use
     * @return The Platform object that represent the requested OS/Platform
     */
    private static Platform selectPlatform(String platformString) {
        Platform platform = null;

        if (platformString.contains("Windows")) {
            if (platformString.contains("2008")) {
                platform = Platform.VISTA;
            } else {
                platform = Platform.XP;
            }
        } else if (platformString.toLowerCase().equals("linux")){
            platform = Platform.LINUX;
        } else {
            System.err.println("Cannot find a suitable platform/OS for ["+ platformString +"]");
        }
        return platform;
    }

    /**
     * Miscellaneous cleaning for browser and browser's version strings.
     * @param browser The browser string to clean
     * @param browserVersion The browser version string to clean
     */
    private static void parameterCleanupForRemote(String browser, String browserVersion) {
        // Selenium1 likes to prepend a "*" to browser string.
        if (browser.startsWith("*")) {
            browser = browser.substring(1);
        }

        // SauceLabs doesn't use version numbering for Google Chrome due to
        // the fast release schedule of that browser.
        if (browser.contains("googlechrome")) {
            browserVersion = "";
        }
    }

    /**
     * Utility class to obtain the Class name in a static context.
     */
    public static class CurrentClassGetter extends SecurityManager {
        public String getClassName() {
            return getClassContext()[1].getName();
        }
    }
    
    /**
     * Checks if an element is present in the page
     */
    protected boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}
