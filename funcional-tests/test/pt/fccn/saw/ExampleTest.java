package pt.fccn.saw;

import org.openqa.selenium.*;
import org.junit.*;
import pt.fccn.saw.selenium.WebDriverTestBase;
import static org.junit.Assert.*;

public class ExampleTest extends WebDriverTestBase {
	@Test
	public void testSimpleSearch() {
		driver.get(testURL + "/");
		driver.findElement(By.cssSelector("#lga")).click();
		driver.findElement(By.cssSelector("#gbqfq")).clear();
		driver.findElement(By.cssSelector("#gbqfq")).sendKeys("teste");
		assertEquals("teste - Google Search", driver.getTitle());
	}
}
