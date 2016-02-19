package pt.fccn.bon.test;

import pt.fccn.saw.selenium.WebDriverTestBase;
import org.junit.*;
import static org.junit.Assert.*;
import org.openqa.selenium.*;

public class SimpleSearchTest extends WebDriverTestBase{
    private StringBuffer verificationErrors = new StringBuffer();

    /**
     * Test if results can be retrieved from b-on search
     * @throws Exception
     */
    @Test
    public void testSimpleSearch() throws Exception {
        driver.get(testURL + "/");
        driver.findElement(By.cssSelector("#find_request_2")).clear();
        driver.findElement(By.cssSelector("#find_request_2")).sendKeys("Teste plata");
        driver.findElement(By.cssSelector("#ok1")).click();
        int numberOfSearchResults = Integer.parseInt(driver.findElement(By.xpath("//div[@id='resultsNumbersTile']/h1/em[1]")).getText());
        try {
            assertTrue(numberOfSearchResults > 0);
        } catch (Error e) {
            verificationErrors.append(e.toString());
        }
        if (numberOfSearchResults > 0){
            WebElement firstResult = driver.findElement(By.id("exlidResult0"));
            assertNotNull("There were no results found.",firstResult);
        }
    }
}
