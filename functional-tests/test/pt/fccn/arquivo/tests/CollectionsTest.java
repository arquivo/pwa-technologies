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

package pt.fccn.arquivo.tests;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Locale;
import pt.fccn.saw.selenium.WebDriverTestBase;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.*;
import static org.junit.Assert.*;

/**
 * A class for testing Archived Collections.
 * This class has tests that are applied to each archived collection to
 * ensure that the collection if available, pointing to the correct pages
 * and working as intended.
 * Tests run on WebDriver
 * @see pt.fccn.saw.test.WebDriverTestBase
 * 
 * @author David Cruz <david.cruz @ fccn.pt>
 * @version 1.5
 */
public class CollectionsTest extends WebDriverTestBase{
    public static final int OLDEST_YEAR_IN_ARCHIVE = 1996;
    public static final String QUERY = "fccn";
    public static final String QUERY_FAWP = "publico";
    public static final String QUERY_EXTENDED = "Fundação para a Computação Científica Nacional";
    Locale localPT = new Locale("pt");
    Locale localEN = new Locale("en");

    private DateFormat ResultListDateFormatter = new SimpleDateFormat("dd MMMMMMMM, yyyy", localPT);        
    private DateFormat ReplayBarDateFormatter = new SimpleDateFormat("H:m:s dd MMMMMMMMM, yyyy", localPT);
    private DateFormat historyDateFormatter = new SimpleDateFormat("d MMM", localPT);    
    private Pattern replayBarPattern = Pattern.compile("URL: (\\S+)\\s+Data: (.*) \\[");
    private Pattern urlPattern = Pattern.compile("(\\S+)\\?(.*)");


    /**
     * Test the document collection of the AWP6 crawl
     */
    @Test
    public void testAWP6() throws Exception {
        verifyCollection(QUERY, "01/12/2009", "31/12/2009");
    }

    /**
     * Test the document collection of the AWP7 crawl
     */
    @Test
    public void testAWP7() throws Exception {
        verifyCollection(QUERY_EXTENDED, "01/05/2010", "31/05/2010");
    }

    /**
     * Test the document collection of the AWP8 crawl
     */
    @Test
    public void testAWP8() throws Exception {
        verifyCollection(QUERY, "01/08/2010", "31/08/2010");
    }


    /****************************************************************************
     *
     *
     * Utility methods
     *
     *
     ****************************************************************************/

    /**
     * Tests a collection to check that both search results and archived are present
     * and shown in a consistent way.
     * This test has two steps:
     * 1 - submit a search with the query 'query', click the first result and verify that
     *     information is consistent across pages.
     * 2 - submit the same query, goes to the history page of the first result, find the
     *    version of the archived page that corresponds to the one presented in the search
     *    result page and verify tht information is consistent across pages.
     *
     * @param query The query string used to test the collection.
     * @param startDate The starting date of the collection's crawl.
     * @param endDate The ending date of the collection's crawl.
     */
    protected void verifyCollection(String query, String startDate, String endDate) throws Exception {
    
        try {
            /**
             * Check an archived page through the search result page
             */
            //((org.openqa.selenium.htmlunit.HtmlUnitDriver)driver).setJavascriptEnabled(true);
            driver.get("http://arquivo.pt/check.txt");
            driver.get("http://arquivo.pt/");
            assertEquals("Arquivo da Web Portuguesa: pesquisa sobre o passado", driver.getTitle());
            driver.findElement(By.id("txtSearch")).clear();
            driver.findElement(By.id("txtSearch")).sendKeys(query);
            driver.findElement(By.id("btnSubmit")).click();

            driver.findElement(By.id("txtSearch")).clear();
            driver.findElement(By.id("txtSearch")).sendKeys(query);
            driver.findElement(By.id("dateStart_top")).clear();
            driver.findElement(By.id("dateStart_top")).sendKeys(startDate);
            driver.findElement(By.id("dateEnd_top")).clear();
            driver.findElement(By.id("dateEnd_top")).sendKeys(endDate);
            driver.findElement(By.id("btnSubmit")).click();

            String titleFirstResult = driver.findElement(By.cssSelector("#resultados-lista h2")).getText();
            System.out.println("Title for first result: " + titleFirstResult);
            String urlFirstResult = driver.findElement(By.cssSelector("#resultados-lista .url")).getText();

            Calendar dateFirstResult = new GregorianCalendar();
            dateFirstResult.setTime(
                ResultListDateFormatter.parse(driver.findElement(By.cssSelector("#resultados-lista .date")).getText())
            );

            driver.findElement(By.cssSelector("#resultados-lista h2 a")).click();

            // Locating the replay bar on the archived page.
            // If we can't get it on the current document, we have to search for 
            // frames and try to find the replay bar there.
            WebElement replayBar = null;

            try {
                replayBar = driver.findElement(By.id("replay_bar"));
            } catch(Exception e) {
                List<WebElement> framesList = driver.findElements(By.tagName("frame"));
            
                for (WebElement element : framesList) {
                    driver.switchTo().frame(element);
                    try {
                        replayBar = driver.findElement(By.id("replay_bar"));
                        if (replayBar != null) {
                            // found. Break the loop
                            break;
                        }
                    } catch(Exception e2) {}
                    driver.switchTo().defaultContent();
                }
            }

            // Extract the URL and date from the replay bar
            Matcher m = replayBarPattern.matcher(driver.findElement(By.cssSelector("#replay_bar")).getText());
            m.find();

            String urlReplayBar = m.group(1);
            String dateStringReplayBar = m.group(2);
            //System.out.println("replay_bar url: " + urlReplayBar + " date:" + dateStringReplayBar);

            // Verify that the title is the same
            if (!titleFirstResult.equals(driver.getTitle())
                && !titleFirstResult.equals(urlReplayBar))
            {
                fail("Search result title and page title are different.\n"
                    +"search result: \t"+ titleFirstResult +"\n"
                    +"page title: \t");
            } 

            // Verify that the url of the archived page is the same that the one of the search result
            assertEquals(urlFirstResult, urlReplayBar);

            // Verify that the date on the replay bar is the same that the date on the first sarch result
            Calendar dateReplayBar = new GregorianCalendar();
            dateReplayBar.setTime(
                ReplayBarDateFormatter.parse(dateStringReplayBar)
            );

            if ( dateFirstResult.get(Calendar.DAY_OF_MONTH) != dateReplayBar.get(Calendar.DAY_OF_MONTH)
                || dateFirstResult.get(Calendar.MONTH) != dateReplayBar.get(Calendar.MONTH)
                || dateFirstResult.get(Calendar.YEAR) != dateReplayBar.get(Calendar.YEAR) ) 
            {
                fail("Date not consistent between result list and archived page");
            }

            // go to homepage
            //driver.findElement(By.cssSelector("#replay_bar b a")).click();
            WebElement e = driver.findElement(By.cssSelector("#replay_bar b a"));
            System.out.println("Text from link:" + e.getText() + " href: " + e.getAttribute("href"));
            e.click();
            System.out.println("Where am i: " + driver.getCurrentUrl());
        
            /**
             * Check an archived page through the history page
             */
            driver.findElement(By.id("txtSearch")).sendKeys(query);
            driver.findElement(By.id("btnSubmit")).click();
            
            //driver.findElement(By.id("txtSearch")).sendKeys(query);
            driver.findElement(By.id("dateStart_top")).clear();
            driver.findElement(By.id("dateStart_top")).sendKeys(startDate);
            driver.findElement(By.id("dateEnd_top")).clear();
            driver.findElement(By.id("dateEnd_top")).sendKeys(endDate);
            driver.findElement(By.id("btnSubmit")).click();

            String urlResultWayback = driver.findElement(By.cssSelector("#resultados-lista h2 a")).getAttribute("href");
            // Go the history page of the first result
            driver.findElement(By.cssSelector("#resultados-lista .outras-datas")).click();

            // Verify that history page's search field is the same as the first result URL
            assertEquals(urlFirstResult, driver.findElement(By.id("txtSearch")).getAttribute("value"));

            // Locate the archived page version that corresponds to the first search result and click it
            int selectedYear = dateFirstResult.get(Calendar.YEAR);
            int columnNumber = selectedYear - OLDEST_YEAR_IN_ARCHIVE + 1;
            String requestedVersionDate = historyDateFormatter.format(dateFirstResult.getTime());

            //List<WebElement> yearColumns = driver.findElements(By.xpath("//div[@id='resultados-lista']/table/tbody/tr/td["+ columnNumber +"]/a[contains(text(),'"+ requestedVersionDate + "')]"));   
            /*assertEquals("No corresponding version for the first search result was found in the history page.\n"+
                    "Search for ["+ requestedVersionDate +"] under column '"+ selectedYear +"' returned nothing",
                    requestedVersionDate, driver.findElement(By.linkText(requestedVersionDate)).getText() );
            */


            // Search for url that appears in the result in calendar page.

            //System.out.println("urlResultWayback:" + urlResultWayback);
            //System.out.println("url from  table:" + driver.findElement(By.linkText(requestedVersionDate)).getAttribute("href"));

            Matcher u = urlPattern.matcher(urlResultWayback);
            u.find();
            String linkResultList = u.group(1);
            //System.out.println("linkFirst: "+ linkFirst);
            boolean linkFound = false;

            List<WebElement> datesOfDay = driver.findElements(By.linkText(requestedVersionDate));
            //System.out.println("Query: " + query + " Date start: " + startDate + " number of elements:" + datesOfDay.size());
            for (WebElement element : datesOfDay) {
                //System.out.println("FOR: element href: " + element.getAttribute("href"));
                Matcher t = urlPattern.matcher(element.getAttribute("href"));
                t.find();
                String linkCalendarList = t.group(1);
                //System.out.println("FOR: Link calendar: "+ linkCalendarList + "\nLink Result list: " +linkResultList);
                if (linkResultList.equals(linkCalendarList)) {
                    linkFound = true;
                    break;
                }
            }

            assertTrue("No corresponding version for the first search result was found in the history page.\n"
                    +"Search for ["+ requestedVersionDate +"] under column '"+ selectedYear +"' returned nothing", linkFound);


            //assertEquals("Version and URL are not the same.", linkResultList, linkCalendarList);
            //List<WebElement> yearColumns = driver.findElements(By.linkText(requestedVersionDate));

            //int linkPosition = -1;

            //System.out.println("Requested version date: " + requestedVersionDate);
            //System.out.println("List size: " + yearColumns.size());

            //for (WebElement element : yearColumns) {
            //    linkPosition++;
                //System.out.println("Date in loop pos: " + linkPosition + " getText: " + element.getText());
            //    if (requestedVersionDate.equals(element.getText())) {
                    //System.out.println("IF: requestedVersionDate Equals replay bar version");
                    //System.out.println("IF: requestedVersionDate: " + requestedVersionDate + " element text: " + element.getText());
                    //System.out.println("IF: element link: " + element.getText());
                    //System.out.println("IF: element link: " + element.getAttribute("href"));
                    //element.click();
                    //System.out.println("IF: Where am i :" + driver.getCurrentUrl());

            //        break;
            //    }
            //}

            //if (linkPosition < 0) {
            //    fail("No corresponding version for the first search result was found in the history page.\n"
            //    +"Search for ["+ requestedVersionDate +"] under column '"+ selectedYear +"' returned nothing");
            //}

            //System.out.println("Linkposition type: " + yearColumns.get(linkPosition) + " get text link position: " + yearColumns.get(linkPosition).getText() + "Href:" + yearColumns.get(linkPosition).getAttribute("href"));
            //yearColumns.get(linkPosition).click();
            //driver.findElement(By.xpath("//div[@id='resultados-lista']/table/tbody/tr[" + linkPosition + "]/td["+ columnNumber +"]/a"));
            //driver.findElement(By.linkText(requestedVersionDate)).click();
            //System.out.println("Where am i :" + driver.getCurrentUrl());
            //assertEquals("FCCN - Fundação para a Computação Científica Nacional", driver.getTitle()); 
            /*
            // Locating the replay bar on the archived page.
            // If we can't get it on the current document, we have to search for 
            // frames and try to find the replay bar there.
            replayBar = null;

            try {
                replayBar = driver.findElement(By.id("replay_bar"));
            } catch(Exception e) {
                List<WebElement> framesList = driver.findElements(By.tagName("frame"));
            
                for (WebElement element : framesList) {
                    driver.switchTo().frame(element);
                    try {
                        replayBar = driver.findElement(By.id("replay_bar"));
                        if (replayBar != null) {
                            // found. Break the loop
                            break;
                        }
                    } catch(Exception e2) {}
                    driver.switchTo().defaultContent();
                }
            }

            // Extract the URL and date from the replay bar
            m = replayBarPattern.matcher(driver.findElement(By.cssSelector("#replay_bar")).getText());
            m.find();

            urlReplayBar = m.group(1);
            dateStringReplayBar = m.group(2);

            // Verify that the url of the archived page is the same that the one of the search result
            assertEquals(urlFirstResult, urlReplayBar);

            // Verify that the date on the replay bar is the same that the date on the first sarch result
            dateReplayBar = new GregorianCalendar();
            dateReplayBar.setTime(
                ReplayBarDateFormatter.parse(dateStringReplayBar)
            );

            if ( dateFirstResult.get(Calendar.DAY_OF_MONTH) != dateReplayBar.get(Calendar.DAY_OF_MONTH)
                || dateFirstResult.get(Calendar.MONTH) != dateReplayBar.get(Calendar.MONTH)
                || dateFirstResult.get(Calendar.YEAR) != dateReplayBar.get(Calendar.YEAR) ) 
            {
                fail("Date not consistent between result list and archived page.\n"
                +"First result:\t"+ ResultListDateFormatter.format(dateFirstResult.getTime())
                +"\nReplay bar:\t"+ ResultListDateFormatter.format(dateReplayBar.getTime()));
            }
            */
        } catch (Exception e) {
            fail("Collection testing failed.\n"
                +"Page:\t"+ driver.getCurrentUrl() +"\n"
                +"Cause:\t"+ e.getMessage() +"\n"
            );
        }
    }
}
