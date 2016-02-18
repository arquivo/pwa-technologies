package pt.fccn.rcaap.tests.saris;

import pt.fccn.saw.selenium.WebDriverTestBase;

import org.junit.*;

import static org.junit.Assert.*;

import org.openqa.selenium.*;

public class ArcaHomePage extends WebDriverTestBase{
	private StringBuffer verificationErrors = new StringBuffer();
	/**
     * Test if results can be retrieved from b-on search
     * @throws Exception
     */
    @Test
    public void testSimpleSearch() throws Exception {
    	driver.get(testURL + "/"); 
        try {
        	assertTrue(isElementPresent(By.cssSelector("table.miscTable > tbody > tr > td.oddRowEvenCol > h3")));
     	    assertEquals("Bem-vindo à ARCA, o Repositório do Instituto Gulbenkian de Ciência", driver.findElement(By.cssSelector("h3")).getText());
     	    assertEquals("Pesquisa rápida", driver.findElement(By.cssSelector("label")).getText());
     	    driver.findElement(By.cssSelector("img[alt=\"DSpace\"]")).click();
        } catch (Error e) {
            verificationErrors.append(e.toString());
        }
        
    }

}
