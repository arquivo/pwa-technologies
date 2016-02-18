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

import pt.fccn.arquivo.pages.AdvancedPage;
import pt.fccn.arquivo.pages.IndexPage;
import pt.fccn.saw.selenium.WebDriverTestBase;

/**
 * @author Hugo Viana
 *
 */
public class AdvancedTest extends WebDriverTestBase{
    /**
     * Tests Advanced search page
     */
	String term = "Pesquisa Avançada — Arquivo.pt";
    @Test
    public void AdvancedTest() {
    	System.out.print("Running AdvancedTest. \n");
        IndexPage index = new IndexPage(driver);
        Ispre_prod=index.setPreProd(pre_prod);
        AdvancedPage advancedPage = index.goToAdvancedPage();	
        assertTrue("The page displayed has not got the title text being displayed",advancedPage.titleIsCorrect(term));
        assertTrue("The page  is not online",advancedPage.existsInResults(Ispre_prod));

    }
}
