package pt.fccn.bon.test;

import org.openqa.selenium.*;
import org.junit.*;
import pt.fccn.saw.selenium.WebDriverTestBase;
import static org.junit.Assert.*;

public class AccessMetasearchTest extends WebDriverTestBase {
    private StringBuffer verificationErrors = new StringBuffer();
    @Test
    public void testAcessoMetapesquisa() throws Exception {
        driver.get(testURL + "/");
        String linkForSearch = (driver.findElement(By.cssSelector("img.modsih"))).findElement(By.xpath("..")).getAttribute("href");
        // find correct window
        driver.get(linkForSearch);
        driver.findElement(By.cssSelector("li.EXLFooterLastLink > a")).click();
        // Verifica que existe o título de pesquisa rápida
        assertEquals("Pesquisa rápida", driver.findElement(By.cssSelector("h1")).getText());
    }
}
