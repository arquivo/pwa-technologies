package pt.fccn.rcaap.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.fccn.saw.selenium.WebDriverTestBase;
import org.junit.*;
import static org.junit.Assert.*;
import org.openqa.selenium.*;

public class SimpleSearchTest extends WebDriverTestBase {
    private StringBuffer verificationErrors = new StringBuffer();

    /**
     * Test if results can be retrieved from b-on search
     * @throws Exception
     */
    @Test
    public void testSimpleSearch() throws Exception {
        String query = "plata test";

        driver.get(testURL + "/");
        driver.findElement(By.cssSelector("#txt")).clear();
        driver.findElement(By.cssSelector("#txt")).sendKeys(query);
        driver.findElement(By.cssSelector("input[name=\"pesquisar\"]")).click();

        Pattern resultsPattern = Pattern.compile("(\\d+) documents found, page (\\d+) of (\\d+)$");
        WebElement resultsString = driver.findElement(By.cssSelector("div#col1.floatLeft h1"));//"div[@id='col1']/h1"));

        Matcher resultsPatternMatcher = resultsPattern.matcher(resultsString.getText());
        System.out.println("resultsString: " + resultsString.getText());
        
        assertTrue("Pattern did not match, restults not in correct format", 
                resultsPatternMatcher.find());
        
        int numberOfSearchResults = Integer.parseInt(resultsPatternMatcher.group(1));
        System.out.println("Number of results " + numberOfSearchResults);
        
        try {
            assertTrue(numberOfSearchResults > 0);
        } catch (Error e) {
            verificationErrors.append(e.toString());
        }
        
        // Verify if search results returned
        if (numberOfSearchResults > 0){
            WebElement firstResult = driver.findElement(By.xpath("//div[@class='listBox']/div[@class='listItem']"));
            assertNotNull("There were no results found.",firstResult);
        }
    }
}
