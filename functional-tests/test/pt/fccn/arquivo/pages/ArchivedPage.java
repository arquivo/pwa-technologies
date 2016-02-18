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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * The <code>ArchivedPage</code> describes a page that has been archived in a 
 * web archive. It's described by a web page and a replay bar.
 * @author Simao Fontes
 *
 */
public class ArchivedPage {
    private WebDriver driver;
    
    Locale localPT = new Locale("pt");
    private DateFormat ReplayBarDateFormatter = new SimpleDateFormat("H:m:s dd MMMMMMMMM, yyyy", localPT);
    private Pattern replayBarPattern = Pattern.compile("URL: (\\S+)\\s+Data: (.*) \\[");
    private Calendar dateReplayBar;
    private String urlFromReplaybar;
    private String replaybarId = "replay_bar";
    
    /**
     * Creates a new archived page
     */
    public ArchivedPage(WebDriver driver){
        //System.out.println("Creating archived page.");
        this.driver = driver;
        // Check for the correct page
        // The archived pages are checd by url since the title changes from page to page
        String pageUrl = driver.getCurrentUrl();
        //System.out.println("Url for archived page:" + pageUrl);
        if (!(pageUrl.contains("wayback"))){
            throw new IllegalStateException("This is not an archived page\n Url of current page: " + pageUrl);
        }
    }
    
    /**
     * Retrieve the information in the replay bar
     * @throws ParseException 
     */
    private void retrieveReplaybarInformation() {
        // Find the replaybar in the frames
        WebElement replayBar = null;
        try {
            replayBar = driver.findElement(By.id(replaybarId));
            System.out.println("Found Element: " + replayBar.getText());
        } catch(NoSuchElementException e) {
            List<WebElement> framesList = driver.findElements(By.tagName("frame"));
            for (WebElement element : framesList) {
                driver.switchTo().frame(element);
                try {
                    replayBar = driver.findElement(By.id(replaybarId));
                    if (replayBar != null) {
                        // found. Break the loop
                        break;
                    }
                } catch(NoSuchElementException e2) {
                    e2.printStackTrace();
                    replayBar = null;
                }
                driver.switchTo().defaultContent();
            }
        }
        
        // Extract the URL and date from the replay bar
        Matcher m = replayBarPattern.matcher(replayBar.getText());
        m.find();
        try {
            urlFromReplaybar = m.group(1);
            String dateStringReplayBar = m.group(2);
            dateReplayBar = new GregorianCalendar();
                dateReplayBar.setTime(
                    ReplayBarDateFormatter.parse(dateStringReplayBar)
                );
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            dateReplayBar = null;
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            urlFromReplaybar = null;
            dateReplayBar = null;
        }
    }
    
    /**
     * Retrieve the date from the replay bar
     * @return Returns a calendar with the date from 
     */
    public Calendar getReplaybarDate() {
        retrieveReplaybarInformation();
        return dateReplayBar;
    }
    
    /**
     * Retrieve the URL for an archived page
     */
    public String getReplaybarUrl(){
        retrieveReplaybarInformation();
        return urlFromReplaybar;
    }
}
