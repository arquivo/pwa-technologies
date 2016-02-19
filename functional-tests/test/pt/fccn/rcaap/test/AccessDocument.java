package pt.fccn.rcaap.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.fccn.saw.selenium.WebDriverTestBase;
import org.junit.*;
import static org.junit.Assert.*;
import org.openqa.selenium.*;

public class AccessDocument extends WebDriverTestBase {
    private StringBuffer verificationErrors = new StringBuffer();

    /**
     * Test if results can be retrieved from b-on search
     * @throws Exception
     */
    @Test
    public void testAccessToDocument() throws Exception {
        String query = "An industrial reference fluid";
        Pattern findPersistentUrl = Pattern.compile("Please use this identifier to cite or link to this item: (\\S+)$");

        driver.get(testURL + "/");
        driver.findElement(By.cssSelector("#txt")).clear();
        driver.findElement(By.cssSelector("#txt")).sendKeys(query);
        driver.findElement(By.cssSelector("input[name=\"pesquisar\"]")).click();

        // Grab first result
        WebElement firstResult = driver.findElement(By.xpath("//div[@class='listBox']/div[@class='listItem']"));
        // Grab title to compare to document page
        String firstResultTitle = firstResult.findElement(By.xpath("//h2")).getText();
        System.out.println("First result title: " + firstResultTitle);
        // Click "more info"
        firstResult.findElement(By.xpath("//div[@class='floatBox']/div[@class='floatRight']/a")).click();
        
        System.out.println(driver.getTitle() + "\n" + driver.getCurrentUrl());
        
        // Grab document description
        WebElement documentDescription = driver.findElement(By.xpath("//div[@id='detailBox']"));
        // Title from document is correct
        String documentTitle = documentDescription.findElement(By.xpath("//h2")).getText();
        
        assertTrue("Document title does not match document clicked.", documentTitle.equals(firstResultTitle));
        
        // Collect the Persistent url to verify if opens correct document
        WebElement persistentUrlElement = documentDescription.findElement(By.xpath("//div[@id='mainInfo']/p[3]/a"));
        String persistentUrlForDocument = persistentUrlElement.getAttribute("href");
        persistentUrlElement.click();
        
        driver.get(persistentUrlForDocument);
        System.out.println(persistentUrlForDocument);
        assertTrue("Persistent Url not found in document", driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*" + persistentUrlForDocument + "[\\s\\S]*$"));
    }
}
