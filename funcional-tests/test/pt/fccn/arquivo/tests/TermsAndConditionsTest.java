/**
[ * Copyright (C) 2011 Simao Fontes <simao.fontes@fccn.pt>
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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import pt.fccn.arquivo.pages.IndexPage;
import pt.fccn.arquivo.pages.TermsAndConditionsPage;
import pt.fccn.saw.selenium.WebDriverTestBase;

import pt.fccn.saw.selenium.WebDriverTestBase;


/**
 * @author Simao Fontes
 *
 */
public class TermsAndConditionsTest extends WebDriverTestBase{
    /**
     * Test the terms and conditions is in the correct language.
     */
    @Test
    public void termsAndConditionsTest() {
    	System.out.print("Running TermsAndConditionsTest. \n");
        IndexPage indexPage = new IndexPage(driver);
       
        TermsAndConditionsPage termsPage = indexPage.getTermsAndConditionsPage();
        assertTrue("This is not the terms and conditions page, in English\nTitle recived is ",termsPage.toEnglishVersion());
        assertTrue("This is not the terms and conditions page, in Portuguese\nTitle in page is ",termsPage.toPortugueseVersion());

    }
}
