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

import java.util.Map;
import java.util.TreeMap;
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
public class UtilitiesTest extends WebDriverTestBase {

	/**
	 * Test the "Help" link to confirm that help pages are correctly linked
	 * Before each test, the browser is set to the starting test url.
	 */
	@Test
	@Ignore
	public void testHelp() throws Exception {
		driver.findElement(By.linkText("Ajuda")).click();
		assertEquals("Pesquisa — Arquivo da Web Portuguesa", driver.getTitle());
		if (!driver.findElement(By.id("content")).getText().contains("Como posso pesquisar no arquivo?")) {
			fail("Help section isn't present in the PT interface");
		}

		driver.navigate().back();

		// Test the English interface
		driver.findElement(By.linkText("English")).click();
		driver.findElement(By.linkText("Help")).click();
	
		assertEquals("Search — Portuguese Web Archive", driver.getTitle());
		if (!driver.findElement(By.id("content")).getText().contains("How can I search in the Archive?")) {
			fail("Help section isn't present in the EN interface");
		}
		
	}

	/**
	 * Tests if we correctly link to the informative site (i.e., sobre.arquivo.pt)
	 */
	@Test
	@Ignore
	public void testAbout() throws Exception {
		//Test in Portuguese
		driver.findElement(By.linkText("Sobre o Arquivo")).click();
		if (!driver.getCurrentUrl().startsWith("http://sobre.arquivo.pt/")) {
			fail("Not on informative page: "+ driver.getCurrentUrl());
		}
		assertEquals("Page title is different", "Arquivo da Web Portuguesa", driver.getTitle());
		assertEquals("H1 title is different", "Arquivo da Web Portuguesa", driver.findElement(By.tagName("h1")).getText());
		
		driver.navigate().back();

		//Test in English
		driver.findElement(By.linkText("English")).click();

		driver.findElement(By.linkText("About the Archive")).click();
		if (!driver.getCurrentUrl().startsWith("http://sobre.arquivo.pt/")) {
			fail("Not on informative page: "+ driver.getCurrentUrl());
		}
		assertEquals("Page title is different", "Portuguese Web Archive", driver.getTitle());
		assertEquals("H1 title is different", "Portuguese Web Archive", driver.findElement(By.tagName("h1")).getText());
	}

	/**
	 * Tests if the "Terms and Conditions" page is present
	 */
	@Test
	@Ignore
	public void testTermsAndConditions() throws Exception {
		// Test in Portuguese
		driver.findElement(By.linkText("Termos & Condições")).click();

		assertEquals("Page title is different", "Termos e Condições - Arquivo da Web Portuguesa", driver.getTitle());
		assertEquals("H1 title is different","Termos e Condições", driver.findElement(By.tagName("h1")).getText());

		// Switch to English
		driver.findElement(By.linkText("English")).click();

		assertEquals("Page title is different", "Terms and Conditions - Portuguese Web Archive", driver.getTitle());
		assertEquals("H1 title is different","Terms and Conditions", driver.findElement(By.tagName("h1")).getText());

		// Go to homepage and check if the English homepage posts to the correct page
		driver.findElement(By.xpath("//div[@id='logo']/a/img")).click();

		assertEquals("Wrong English homepage", "Portuguese Web Archive: search the past", driver.getTitle());

		driver.findElement(By.linkText("Terms & Conditions")).click();

		assertEquals("Page title is different", "Terms and Conditions - Portuguese Web Archive", driver.getTitle());
		assertEquals("H1 title is different","Terms and Conditions", driver.findElement(By.tagName("h1")).getText());
	}

	/**
	 * Tests if we correctly link to the page that describes the "Technology"
	 */
	@Test
	@Ignore
	public void testTechnologiesLink() throws Exception {
		// Test in Portuguese
		driver.findElement(By.linkText("Tecnologias")).click();

		assertEquals("Page title is different", "Tecnologia — Arquivo da Web Portuguesa", driver.getTitle());
		assertEquals("H1 title is different","Tecnologia", driver.findElement(By.tagName("h1")).getText());

		driver.navigate().back();

		// Go to homepage and check if the English homepage posts to the correct page
		driver.findElement(By.linkText("English")).click();
		assertEquals("Wrong English homepage", "Portuguese Web Archive: search the past", driver.getTitle());
		
		driver.findElement(By.linkText("Technologies")).click();

		assertEquals("Page title is different", "Technology — Portuguese Web Archive", driver.getTitle());
		assertEquals("H1 title is different","Technology", driver.findElement(By.tagName("h1")).getText());
	}

	/**
	 * Test the "Sponsor" links to confirm that help pages are correctly linked
	 * Before each test, the browser is set to the starting test url.
	 */
	@Test
	@Ignore
	public void testSponsors() throws Exception {
		Map<String,String> sponsors = new TreeMap<String,String>();
		sponsors.put(
			"//img[@alt='FCCN - Fundação para a Computação Científica Nacional']",
			"FCCN");
		sponsors.put(
			"//img[@alt='UMIC - Agência para a Sociedade de Conhecimento']",
			"Umic - Início");
		sponsors.put(
			"//img[@alt='POSC - Programas Operacionais Sociedade do Conhecimento']",
			"Quadro Comunitário de Apoio III - Programas Operacionais - PO POSC");
		sponsors.put(
			"//img[@alt='FEDER - Fundo Europeu de Desenvolvimento Regional']",
			"Política Regional Inforegio - FEDER - Fundo Europeu de Desenvolvimento Regional");
		sponsors.put(
			"//img[@alt='MCTES - Ministério da Ciência, Tecnologia e Ensino Superior']",
			"MCTES - Ministério da Ciência, Tecnologia e Ensino Superior");

		for (String xpath : sponsors.keySet()) {
			driver.findElement(By.xpath(xpath)).click();
			assertEquals(sponsors.get(xpath), driver.getTitle());
			driver.navigate().back();
		}
	
		// Test the English interface
		sponsors.clear();
		sponsors.put(
			"//img[@alt='FCCN - Foundation for National Scientific Computing']",
			"FCCN");
		sponsors.put(
			"//img[@alt='UMIC - Knowledge Society Agency']",
			"Umic - Início");
		sponsors.put(
			"//img[@alt='POSC - Operational Programme for the Information Society']",
			"Quadro Comunitário de Apoio III - Programas Operacionais - PO POSC");
		sponsors.put(
			"//img[@alt='ERDF - European Regional Development Fund']",
			"Política Regional Inforegio - FEDER - Fundo Europeu de Desenvolvimento Regional");
		sponsors.put(
			"//img[@alt='MCTES - Ministry of Science, Technology and Higher Education']",
			"MCTES - Ministério da Ciência, Tecnologia e Ensino Superior");
	
		driver.findElement(By.linkText("English")).click();
		assertEquals("Portuguese Web Archive: search the past", driver.getTitle());

		for (String xpath : sponsors.keySet()) {
			driver.findElement(By.xpath(xpath)).click();
			assertEquals(sponsors.get(xpath), driver.getTitle());
			driver.navigate().back();
		}
		

	}
}
