/**
 * 
 */
package pt.fccn.rcaap.tests.saris;

import pt.fccn.saw.selenium.WebDriverTestBase;

import org.junit.*;

import static org.junit.Assert.*;

import org.openqa.selenium.*;

/**
 * @author fribeiro
 *
 */
public class ArcaSimpleSearch extends WebDriverTestBase{
	private StringBuffer verificationErrors = new StringBuffer();
	
	@Test
	  public void testPesquisaSimples() throws Exception {
	    driver.get(testURL + "/");
	    driver.findElement(By.id("tequery")).clear();
	    driver.findElement(By.id("tequery")).sendKeys("Accumulation");
	    driver.findElement(By.name("submit")).click();

	    assertEquals("Resultados da pesquisa", driver.findElement(By.cssSelector("h1")).getText());
	    String resString = driver.findElement(By.xpath("//p[2]")).getText();
	    
	    /*It is not possible to get the results in other way, with the xpath expression
	     * Workaround - Possibly in future remove this
	     */
	    resString = resString.replaceAll("Resultados.+de\\s+", "");
	    resString = resString.replaceAll("\\.", "");
	    int numberOfSearchResults = Integer.parseInt(resString);
	   
	    driver.findElement(By.cssSelector("img[alt=\"DSpace\"]")).click();
	    driver.findElement(By.id("tequery")).clear();
	    driver.findElement(By.id("tequery")).sendKeys("Accumulation");
	    driver.findElement(By.name("submit")).click();
	    try {
            assertTrue(numberOfSearchResults > 0);
        } catch (Error e) {
            verificationErrors.append(e.toString());
        }
	    
	    
	    /*//No query expression should return a non valid value or Valor de pesquisa inválido
	    driver.findElement(By.id("tequery")).clear();
	    driver.findElement(By.name("submit")).click();
	    resString = driver.findElement(By.xpath("//p[2]")).getText();
	    assertEquals("Valor de pesquisa inválido", driver.findElement(By.xpath("//p[2]")).getText());
	    */
	    
	    driver.findElement(By.id("tequery")).clear();
	    driver.findElement(By.id("tequery")).sendKeys(" ");
	    driver.findElement(By.name("submit")).click();
	    resString = driver.findElement(By.xpath("//p[2]")).getText();
	    assertEquals("Valor de pesquisa inválido", driver.findElement(By.xpath("//p[2]")).getText());
	    
	  }
}
