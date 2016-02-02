/**
 * Copyright (C) 2011 Simao Fontes <simao.fontes@fccn.pt>
 * Copyright (C) 2011 SAW Group - FCCN <saw@asa.fccn.pt>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.fccn.arquivo.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import pt.fccn.arquivo.pages.HighlightsPage;
import pt.fccn.arquivo.pages.IndexPage;
import pt.fccn.saw.selenium.WebDriverTestBase;

/**
 * @author Simao Fontes
 *
 */
public class HighlightsTest extends WebDriverTestBase{
    /**
     * Test the link for more highlights
     */
    @Test
    public void highlightLinkTest() {
    	System.out.print("Running HighlightsTest. \n");
        IndexPage index = new IndexPage(driver);
       index.langToEnglish();
        HighlightsPage highlightPage = index.goToHighlightsPage();	
        assertTrue("The page displayed has not got the correct text being displayed",
                highlightPage.isPageCorrect());
        assertTrue("The page  is not online",
                highlightPage.goThroughHighlights());
        
        assertTrue("The page link is broken ",
                highlightPage.checkLinkHighligths());
        assertTrue("The page link is broken ",
                highlightPage.checkHighligthsPageLinks());
    }
}
