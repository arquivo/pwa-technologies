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



import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * @author Simao Fontes
 *
 */
public class IndexPage {
    // Webdriver that handles page interractions
    private final WebDriver driver;
    private static final String pageURLCheck = "index.jsp";
    private String url =null;
    private static final String searchBox = "txtSearch";
    private static final String searchButton = "btnSubmit";
    private static final String highlightId = "ver-destaques";
    private static final String linkTextEN = "English";
    private String termsandconditionstitleTextPT = "Termos e Condições";
    private static final String linkTextPT = "Português";
    private static final String titleTextEN = "Arquivo.pt - the Portuguese Web Archive: search pages from the past";
    private static final String titleTextPT = "Arquivo.pt: pesquisa sobre o passado";
    private static final String cssTermsConditions = "#terms-conditions";
    private boolean isPreProd=false;
    /**
     * Starts a new Index page
     */
    public IndexPage(WebDriver driver){
        this.driver = driver;
        
        // Check that we're on the right page.
        String pageTitle= driver.getTitle();
        if (!(pageTitle.contentEquals(titleTextEN) || (pageTitle.contentEquals(titleTextPT)))){
            throw new IllegalStateException("This is not the index page\n Title of current page: " + pageTitle);
        }
    }
    
    /**
     * @param pre_prod
     * @return
     */
    public boolean setPreProd(String pre_prod){
    	 if (driver.getCurrentUrl().contains(pre_prod)){
         	this.isPreProd=true;
         }
         return isPreProd;
    }
    
    /**
     * Searches for a string in the interface
     * @param searchTerms String of terms to search for
     * @return result page for query
     */
    public SearchPage search(String searchTerms){
        driver.findElement(By.id(searchBox)).clear();
        driver.findElement(By.id(searchBox)).sendKeys(searchTerms);
        driver.findElement(By.id(searchButton)).submit();
        return new SearchPage(driver,isPreProd);
    }
    /**
     * Searches for a string in the interface
     * @param searchTerms String of terms to search for
     * @return result page for query
     */
    public OpenSearchPage opensarch(String searchTerms,boolean isPredprod){
    	String[] Url = driver.getCurrentUrl().split(".pt");
    	driver.get(Url[0]+".pt/opensearch?query="+searchTerms);
        return new OpenSearchPage(driver,isPredprod);
    }
    
    
    /**
     * Change language of the page to english
     */
    public void langToEnglish(){
    	
    	 WebElement langElem = driver.findElement(By.linkText(linkTextEN));
         langElem.click();
         String pageTitle = driver.getTitle();
         if (!titleTextEN.contentEquals(pageTitle))
             throw new IllegalStateException("Could not click the link for " +
                     "English, The title was not correct after click.\nTitle: " + 
                     pageTitle);
    }
    /**
     * Click the Highlights page
     */
    public HighlightsPage goToHighlightsPage(){
        WebElement highligthLink = driver.findElement(By.id(highlightId));
        highligthLink.click();
        return new HighlightsPage(driver);
    }
    
    /**
     * Return the terms and conditions page
     * @return terms and conditions page
     */
    public TermsAndConditionsPage getTermsAndConditionsPage(){
        driver.findElement(By.linkText(termsandconditionstitleTextPT)).click();
        return new TermsAndConditionsPage(driver);
    }
    
    /**
     * Return the image source from sponsor
     * @return the image
     */
    public boolean isSponsorGovImageCorrect (){
        WebElement image = driver.findElement(By.cssSelector("img[usemap=\"#logomap\"]"));
        String src = image.getAttribute("src");
        return src.endsWith("mec-web.png");
    }
    /**
     * Click the Highlights page
     */
    public AdvancedPage goToAdvancedPage(){
        WebElement advancedLink = driver.findElement(By.id("pesquisa-avancada"));
        advancedLink.click();
        return new AdvancedPage(driver);
    }
    
    /**
     * Click the Highlights page
     */
    public Arcproxyinspection arcProxy(Boolean isPreProd){
        
        return new Arcproxyinspection(driver,isPreProd);
    }
    
    /**
     * Make a search by URL and inspect if the hostname is not case-sensitive
     * for instance, fccn.pt and fccn.PT are the same
     * @param query
     * @return
     */
    public boolean searchbyURL(String query,String queryPT){
	 	
    	this.url = driver.getCurrentUrl();
    	String date="26 Nov"; // historical link selected
    	String title = getTitlesearchbyURL(query,date);
    	String title_cap=getTitlesearchbyURL(queryPT,date);
    	if (title==null)
    		return false;
    	if (!title.equals(title_cap))
    		return false;
    	return true;
	
}

/**
 * Get the title of a page 
 * query url
 * date on format "10 Dez"
 * @return title of the webapge on 10 Dez
 */
public String getTitlesearchbyURL(String query,String date){
		driver.get(this.url);
		
		driver.findElement(By.id("txtSearch")).clear();
		driver.findElement(By.id("txtSearch")).sendKeys(query);
		driver.findElement(By.id("btnSubmit")).click();
		String title=null;
		try {
			driver.findElement(By.linkText(date)).click();
			title= driver.getTitle();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return title;
		}
		 
		return title;
	}
}
