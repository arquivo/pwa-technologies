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

package pt.fccn.arquivo.tests.older;

import java.util.ArrayList;
import pt.fccn.arquivo.tests.util.QueryResultPair;
import pt.fccn.saw.selenium.WebDriverTestBase;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.*;
import static org.junit.Assert.*;

/**
 * A class for tests related to the Advanced Search page.
 * Tests run on WebDriver
 * @see pt.fccn.saw.test.WebDriverTestBase
 * 
 * @author David Cruz <david.cruz @ fccn.pt>
 * @version 1.5
 */
public class AdvancedSearchTest extends WebDriverTestBase {

	/**
	 * Tests the negation text field from the Advanced Search form.
	 * Several queries are tested to confirm that they are correctly parsed
	 */
	@Test
	@Ignore
	public void testTermNegation() throws Exception {
		// Create the search query/result list
		ArrayList<QueryResultPair> queryList = new ArrayList<QueryResultPair>();
		queryList.add(new QueryResultPair("cm-lisboa", "-cm-lisboa"));
		queryList.add(new QueryResultPair("-cm-lisboa", "-cm-lisboa"));
		queryList.add(new QueryResultPair("cm-lisboa porto", "-cm-lisboa -porto"));
		queryList.add(new QueryResultPair("-cm-lisboa -porto", "-cm-lisboa -porto"));
		queryList.add(new QueryResultPair("     cm-lisboa   porto    ", "-cm-lisboa -porto"));
		queryList.add(new QueryResultPair("     -cm-lisboa   -porto    ", "-cm-lisboa -porto"));

		assertEquals("Arquivo da Web Portuguesa: pesquisa sobre o passado", driver.getTitle());

		// Submit each query and confirm that it is correctly parsed
		for(QueryResultPair pair : queryList) {
			driver.findElement(By.linkText("Pesquisa Avançada")).click();
			WebElement negationField = driver.findElement(By.id("adv_not"));
			negationField.clear();
			negationField.sendKeys(pair.getQuery());
			driver.findElement(By.xpath("//input[@value='Pesquisar']")).click();
			assertEquals(driver.findElement(By.id("query_top")).getAttribute("value"), pair.getResult());
			assertEquals(driver.getTitle(), pair.getResult() +" - Pesquisa do Arquivo da Web Portuguesa");
		}

		// Test the English interface
		driver.findElement(By.linkText("English")).click();
		assertEquals("Portuguese Web Archive: search the past", driver.getTitle());
		assertEquals("Search and access pages of the past", driver.findElement(By.tagName("h2")).getText());

		// Submit each query and confirm that it is correctly parsed
		for(QueryResultPair pair : queryList) {
			driver.findElement(By.linkText("Advanced Search")).click();
			WebElement negationField = driver.findElement(By.id("adv_not"));
			negationField.clear();
			negationField.sendKeys(pair.getQuery());
			driver.findElement(By.xpath("//input[@value='Search']")).click();
			assertEquals(driver.findElement(By.id("query_top")).getAttribute("value"), pair.getResult());
			assertEquals(driver.getTitle(), pair.getResult() +" - Portuguese Web Archive Search");
		}

	}
}
