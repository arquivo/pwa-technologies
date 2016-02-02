package pt.fccn.bon.test;

import org.openqa.selenium.*;
import org.junit.*;
import pt.fccn.saw.selenium.WebDriverTestBase;
import static org.junit.Assert.*;

public class AccessPeriodicalPublicationsTest extends WebDriverTestBase{
    @Test
    public void testAccessToPeriodicalPublications() {
        driver.get(testURL + "/");
        WebElement searchService = driver.findElement(By.xpath("//img[@alt='metapesquisa']"));
        String linkForSearch = (searchService.findElement(By.xpath("..")).getAttribute("href"));
        System.out.println("SearchServicehref: " + linkForSearch);
        driver.get(linkForSearch);
        driver.findElement(By.linkText("Periódicos/e-books")).click();
        assertTrue(isElementPresent(By.cssSelector("img[alt=\"ExLibris header image\"]")));
        driver.findElement(By.cssSelector("#param_pattern_value")).click();
        driver.findElement(By.cssSelector("a.selected")).click();
        assertEquals("Pesquisar periódicos", driver.findElement(By.cssSelector("a.selected")).getText());
    }
}
