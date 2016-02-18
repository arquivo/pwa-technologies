/**
 * 
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Nutchwax 
 *
 */
public class HighlightsPage {
    private final WebDriver driver;
    
    private static final String titleTextEN = "Examples of pages preserved by Arquivo.pt — Portuguese Web Archive";
    
    
    private static final String titleTextPT = "Exemplos de páginas preservadas no Arquivo.pt &mdash; Sobre o Arquivo.pt";
    private static final String titleFirstPortuguesePage = "Home Page de Portugal / Portugal Home Page";
    private static final String titleSmithsonian ="The Smithsonian Institution Home Page";
    private static final String titleClix = "UEFA Euro 2004";
    private static final String titleExpo = "EURO - O que é o euro ?";
    private static final String titlePublico = "PUBLICO";
    private static final String titleSapo = "Projecto HidroNet - Links 1";
    private static final String titleTim = "Tim Berners-Lee";
    private static final String titlePresidenciais ="portuguese presidentials of 2001";
    private static final String h1Title= "First Portuguese web page (1996)";
    private static final String titleEuro ="Futebol Internacional - Notícias do dia";
    private static final String titleEloy ="Eloy Rodrigues - HOME PAGE";
    private static final String titlePortugalTelecom ="P O R T U G A L T E L E C O M";
    private static final String titleMinistre ="Ministère de l'Education Nationale";
    private static final String titleSic ="SIC Online - Cavaco Silva em Bragança";
    private static final String titleCimiterio ="Visita ao Cemitério";
    private static final String titleSapoDesporto ="Sapo Infordesporto";
    private static final String titleNimas ="NIMAS - FITAS EM CARTAZ";
    private static final String titleXLDB ="Referencias";
    
    
    public HighlightsPage(WebDriver driver) {
        this.driver = driver;
        // Check that we're on the right page.
        String pageTitle= driver.getTitle();
        if (!(pageTitle.contentEquals(titleTextEN) || (pageTitle.contentEquals(titleTextPT)))){
            throw new IllegalStateException("This is not the " + this.getClass().getName()+ " page\n Title of current page: " + pageTitle);
        }
    }
    
    /**
     * Verify if page has correct text
     * @return true if page contains the expected text
     */
    public boolean isPageCorrect() {
        return (h1Title.compareTo(driver.findElement(By.xpath("//a[contains(.,'First Portuguese web page (1996)')]")).getText()) == 0);
        
    }
    
    /**
     * Run through the links of highlights
     * @return true if all links from 
     */
    public boolean goThroughHighlights()  {
    	
        List <WebElement> listOfHighlights = driver.findElements(By.id("boxes"));
        for (WebElement element : listOfHighlights) {
        	element.click();
        	
        }
        return true;
    }
    
    
    /**
     * @return true if all of links are not broken in the main page
     */
    public boolean checkLinkHighligths(){
    	List<WebElement> linkList= driver.findElements(By.tagName("a"));
   	 int statuscode=0;
       for(int i=0 ; i<linkList.size() ; i++)
       {
       	if(linkList.get(i).getAttribute("href") != null)
       	  {
       		if (linkList.get(i).getAttribute("href").contains("/wayback")){
				statuscode=getResponseCode(linkList.get(i).getAttribute("href"));
				if (statuscode!= 200){
					return false;
				}
       		}
       	  }	
       }
       return true;
    }
    
    /**
     * @return true if all of the links are correct
     */
    public boolean checkHighligthsPageLinks(){
    	
    	String title=null;	
    	int i =0;
    	List<String> aux = getHiglightsUrl();
       for(i=0 ; i<aux.size() ; i++)
       {
    	   title = getIdTitle(aux.get(i));
    	   
       	 	if (!inspectTitlesMatches(title)){
       	 		System.out.print("\n\nunexpected title: "+title);
       	 		return false;
       	 	}
       }
       return true;
    }
    
    /**
     * @return a list with all of the current url's 
     */
    private List<String> getHiglightsUrl(){
    	List<WebElement> linkList= driver.findElements(By.className("external-link"));
    	List<String> highlights = new ArrayList<String>();
    	for( int i =0; i< linkList.size();i++)
    		highlights.add(linkList.get(i).getAttribute("href"));
    	return highlights;
    	
    }
    /**
	 * @param Url - every highlights url
	 * @return Title of the webpage
	 */
	private String  getIdTitle (String Url){
		WebDriverWait wait = new WebDriverWait(driver, 15);
		
		
		driver.get(Url);
		//wait until title was loaded
		wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("title")));
		return driver.getTitle();
	}

    /**
     * titlepage: current driven title
     * @return true if matches any title
     */
    public boolean inspectTitlesMatches(String titlepage){
    	
    	if (titlePresidenciais.equals(titlepage)){
    		return true;
    	}
		    
		if (titleClix.equals(titlepage)){
			return true;
		}
		
		if (titleExpo.equals(titlepage)){
			return true;
		}
		
		if (titleSmithsonian.equals(titlepage)){
			return true;
		}
		if (titlePublico.equals(titlepage)){
			return true;
		}
		
		if (titleFirstPortuguesePage.equals(titlepage)){
			return true;
		}
		
		if (titleSapo.equals(titlepage)){
			return true;
		}
		
		if (titleTim.equals(titlepage)){
			return true;
		}
		if (titleEuro.equals(titlepage)){
			return true;
		}
		if (titleEloy.equals(titlepage)){
			return true;
		}
		if (titleXLDB.equals(titlepage)){
			return true;
		}
		if (titleSapoDesporto.equals(titlepage)){
			return true;
		}
		if (titlePortugalTelecom.equals(titlepage)){
			return true;
		}
		if (titleMinistre.equals(titlepage)){
			return true;
		}
		if (titleCimiterio.equals(titlepage)){
			return true;
		}
		if (titleNimas.equals(titlepage)){
			return true;
		}
		if (titleSic.equals(titlepage)){
			return true;
		}
		
		else{
			System.out.print("\n\n"+titlepage);
			return false;
		}
    	
    	
    }
    
    /**
     * @param urlString
     * @return the statuscode from the page
     */
    public int getResponseCode(String urlString) {         
        URL u=null;
        HttpURLConnection huc=null;
		try {
			u = new URL(urlString);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        try {
			huc = (HttpURLConnection) u.openConnection();
			huc.setRequestMethod("GET");  
	        huc.connect();  
	        return huc.getResponseCode();  
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;  
        
  } 
}
