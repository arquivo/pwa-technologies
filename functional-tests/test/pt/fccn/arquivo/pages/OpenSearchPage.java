/**
 * Copyright (C) 2015 Hugo Viana <hugo.viana@fccn.pt>
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
package pt.fccn.arquivo.pages;



import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import javax.xml.parsers.ParserConfigurationException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.StringReader;
import java.sql.Driver;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.sourceforge.htmlunit.corejs.javascript.ast.NewExpression;


/**
 * @author Hugo Viana
 *
 */
public class OpenSearchPage {
    private final WebDriver driver;
    private final String numberOfResultsTag = "feedContent";
    private final String listOfResultsTag = "resultados-lista";
    private static final String searchBox = "txtSearch";
    private static final String searchButton = "btnSubmit";
    private final String pageURLCheck = "opensearch";
 // Patern to detect if there are results
    private Pattern noResultsPattern = Pattern.compile("\\d Resultados");
   private boolean isPredProd=false;
    /**
     * Create a new OpenSearchPage from navigation
     * @param driver
     */
    public OpenSearchPage(WebDriver driver,boolean isPreProd){	
    	this.isPredProd=isPreProd;
        this.driver= driver;
        // Check that we're on the right page.
        if (!(driver.getCurrentUrl().contains(pageURLCheck))) {
            throw new IllegalStateException("This is not the results opensearch page\n URL of current page: " + driver.getCurrentUrl());
        }
    }
    
    /**
     * Gather search for a term
     * @param query Term that was searched
     * @return true if the term exists in the first result title or in the snippet text
     */
    public boolean existsResults() throws Exception{
        String resultsTags = driver.findElement(By.id(numberOfResultsTag)).getText();
        WebElement listOfTitles = driver.findElement(By.xpath("//*[@id='feedContent']"));
        
        if (resultsTags.length()<=0)
        	return false;
//                throw new Exception("No results for term");
        return true;
    }
    /**
     * Verify coherence in result between opensarch api and search on arquivo.pt
     * @param firstTitleOfResultList The first result when searching for a query through arquivo.pt full text-search
     * @return true if the first result are coherent with opensearch result, false otherwises
     */
    public boolean inspectCoherence(String firstTitleOfResultList) {
        WebElement listOfTitles = driver.findElement(By.xpath("//*[@id='feedContent']"));
        String resultTitle = listOfTitles.getText().split("\n")[0];
        return resultTitle.contains(firstTitleOfResultList);	
    }

    /**
     * It is only needed when the test is run individually, if not IndexPage will set this variable
     * Verify that the term exists in as a search result
     * @param query Term that was searched
     * @return true if the term exists in the first result title or in the snippet text
     */
    public String setFirstResult(String searchTerms) {
    	
    	//Create a new firefox driver
    	WebDriver searchDriver = new FirefoxDriver();
    	searchDriver.get(driver.getCurrentUrl().split("opensearch")[0]);
    	searchDriver.findElement(By.id(searchBox)).clear();
    	searchDriver.findElement(By.id(searchBox)).sendKeys(searchTerms);
    	searchDriver.findElement(By.id(searchButton)).submit();
    	WebElement listOfResults = searchDriver.findElement(By.id(listOfResultsTag));
    	searchDriver.close();
    	return listOfResults.findElement(By.xpath("//*[@id=\"resultados-lista\"]/ul/li[1]/h2")).getText();
    }
    
    
}
