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
package pt.fccn.arquivo.pages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.RestoreAction;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * @author Simao Fontes
 *
 */
public class AdvancedPage {
    private final WebDriver driver;
    private final String pageURLCheck = "advanced.jsp"; 
    private final String listOfResultsTag = "resultados-lista";
    // Patern to detect if there are results
    
    // Tags for searching
    
    /**
     * Create a new AdvancedPage from navigation
     * @param driver
     */
    public AdvancedPage(WebDriver driver){
        this.driver= driver;
        // Check that we're on the right page.
        if (!(driver.getCurrentUrl().contains(pageURLCheck))) {
            throw new IllegalStateException("This is not the results search page\n URL of current page: " + driver.getCurrentUrl());
        }
    }
    
    /**
     * Verify that the title of the search page contains the term searched.
     * @param term term that was searched in the system
     * @return true if term is in the title of the page
     */
    public boolean titleIsCorrect(String query){
        if (driver.getTitle().contains(query) && !query.isEmpty())
            return true;
        else
            return false;
    }
    
    /**
     * Verify that advanced search returns the first page of sapo
     * @return true if it contains sapo.pt from 1996 on result list
     */
    public boolean existsInResults(boolean isPreProd) {
    	String title=null;
    	try {
    		
    		driver.findElement(By.id("adv_and")).clear();
    	    driver.findElement(By.id("adv_and")).sendKeys("sapo");
    	    driver.findElement(By.id("site")).clear();
    	    driver.findElement(By.id("site")).sendKeys("www.sapo.pt");
    	    
    	    driver.findElement(By.id("btnSubmitBottom")).click();
    	    WebElement listOfResults = driver.findElement(By.id(listOfResultsTag));
        	
    	    title=listOfResults.findElement(By.xpath("//*[@id=\"resultados-lista\"]/ul/li[1]/h2")).getText();
    	    
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			return false;
		}
        if (!title.contains("SAPO") || title==null)
        	return false;
        return true;
    }
    
}
