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

/**
 * Represents the page with the terms and conditions. 
 * @author Simao Fontes
 *
 */
public class TermsAndConditionsPage {
    private final WebDriver driver;
//    private static final String urlName = "terms-conditions.jsp";
    private static final String titleTextPT = "Termos e Condições — Sobre o Arquivo.pt";
    private static final String titleTextEN = "Terms and Conditions — Portuguese Web Archive";
    private static final String linkTextPT = "Português";
    private static final String linkTextEN = "English";
    private static final String contentID = "conteudo-termos";

    /**
     * Constructs a Terms and conditions.
     * @param driver <code>org.openqa.selenium.WebDriver</code> use for this page
     */
    public TermsAndConditionsPage(WebDriver driver) {
        this.driver = driver;
        // Check that we're on the right page.
        if (!(driver.getTitle().contains(titleTextPT))) {
            // Alternatively, we could navigate to the login page, perhaps logging out first
            throw new IllegalStateException("This is not the terms and conditions page\n"+driver.getCurrentUrl()+""+this.getClass().getName());
        }
    }

    /**
     * Change to the English version of the page
     */
    public boolean toEnglishVersion(){
        driver.findElement(By.linkText(linkTextEN)).click();
        // Check that we're on the right page.
        if (!(driver.getTitle()).contains(titleTextEN)) {
            // Alternatively, we could navigate to the login page, perhaps logging out first
            throw new IllegalStateException("This is not the terms and conditions page, in English\nTitle received is " + driver.getTitle()+" "+this.getClass().getName());
        	//return false;
        }
        return true;
    }

    /**
     * Change to the Portuguese version of the page
     */
    public boolean toPortugueseVersion(){
        driver.findElement(By.linkText(linkTextPT)).click();
        // Check that we're on the right page.
        if (!driver.getTitle().contains(titleTextPT)) {
            // Alternatively, we could navigate to the login page, perhaps logging out first
            //throw new IllegalStateException("This is not the terms and conditions page, in Portuguese\nTitle in page is " + driver.getTitle());
        	return false;
        }
        return true;
    }
}
