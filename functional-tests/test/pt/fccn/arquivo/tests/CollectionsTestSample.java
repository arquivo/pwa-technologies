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

import pt.fccn.saw.selenium.WebDriverTestBase;
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
public class CollectionsTestSample extends WebDriverTestBase {
	public static final int OLDEST_YEAR_IN_ARCHIVE = 1996;
	public static final String QUERY = "fccn";

	/**
	 * Test the document collection of the "Roteiro da Internet"
	 */
	@Test
	public void testRoteiro() throws Exception {
		verifyCollection(QUERY, "13/10/1996", "13/10/1996");
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
	 * 	information is consistent across pages.
	 * 2 - submit the same query, goes to the history page of the first result, find the
	 *	version of the archived page that corresponds to the one presented in the search
	 *	result page and verify tht information is consistent across pages.
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
			System.out.println(driver.getTitle());
			System.out.println(driver.findElement(By.tagName("h2")).getText() );
			System.out.println(driver.findElement(By.id("txtSearch")).getAttribute("value") );
			driver.findElement(By.id("txtSearch")).sendKeys(query);
		} catch (Exception e) {
            System.out.println("IN exception");
			fail("Collection testing failed.\n"
				+"Page:\t"+ driver.getCurrentUrl() +"\n"
				+"Cause:\t"+ e.getMessage() +"\n"
			);

		}
	}

}
