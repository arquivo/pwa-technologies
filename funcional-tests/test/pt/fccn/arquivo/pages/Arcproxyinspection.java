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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Hugo Viana
 *
 *
 *Functionality: 
 * inspecting Arcproxy are proper performed, which meaning that all links are well linked.
 * Configuration file with index linked located on ROOT with monitored_indexes name
 * This test was made taking in consideration that p41 is the pre-prodution server and p58 and p62 is the prodution environment 
 */
/**
 * @author nutchwax
 *
 */
public class Arcproxyinspection {
	private final WebDriver driver;
	private String filename_prod= "monitored_indexes"; // files which contains webepage to be fetch for prodution
	private String filename_pre_prod= "monitored_indexes_pre_prod"; //files which contains webepage to be fetch for pre-prodution
	private String broker_p58 ="p58.";
	private String broker_p62 ="p62.";
	private List<String> DateList = new ArrayList<String>();
	private boolean isPredProd=false;

	/**
	 * Create a new Arcproxy navigation
	 * @param driver
	 */
	public Arcproxyinspection(WebDriver driver, boolean isPredProd){	
		this.driver= driver;
		this.isPredProd=isPredProd;

	}

	/**
	 * The file monitered_index contains syntax with DocID and the standard wayback syntax
	 * @param inspectDate -  If it is wayback syntax compare date, if not just navigate over index
	 * @return
	 */
	public  boolean inspectArcproxy(boolean inspectDate)
	{
		BufferedReader reader=null;
		String id=null;
		String[] Url = driver.getCurrentUrl().split("/index.jsp");
		String title_p58=null;
		String date_p58=null;
		String title_p62=null;
		String date_p62=null;


		//to test for a new server different than p58 or p62
		if (this.isPredProd){
			this.broker_p58 ="";
			this.broker_p62 ="";
		}
		int i =0;
		boolean result=true;
		try
		{
			if (!this.isPredProd){
				reader = new BufferedReader(new FileReader(filename_prod));
			}
			else{
				reader = new BufferedReader(new FileReader(filename_pre_prod));
			}
	
			
				while ((id = reader.readLine()) != null)
				{
					if(!inspectDate ){
						title_p58=getIdTitle(broker_p58, id,Url);
						date_p58=getIdDate(broker_p58,id);
						DateList.add(date_p58); //Contains every dates of web content fetched from the file
						title_p62=getIdTitle(broker_p62, id,Url);
						date_p62=getIdDate(broker_p62,id);						
						if (date_p58 !=null && date_p62 !=null && !isPredProd){
							//If occurs same pages with different titles or dates
							if (!date_p58.equals(date_p62) && !title_p58.equals(title_p62)){
								System.out.print("Inconsistence collection: "+ id +"");
								reader.close();
								result = false;
								throw new IllegalStateException("Inconsistence collection: "+ id +"");
							}
							//If the collection is offline
							if (date_p58 ==null || date_p62==null){
								System.out.print("Offline collection: "+ id +"");
								reader.close();
								result = false;
								throw new IllegalStateException("The collection which contains " +id+ 
					                     "is offline");
							}
							
							else
								result = true;
						}
					}
					else {
						result =inspectDate(id,i);

					}
					i++;
				}
				reader.close();
		}
		//Problems opening the file monitored_indexes
		catch (IOException e)
		{
			System.out.print(reader +" :"+this.getClass().getName()+"\nThere was problems opening configuration ");
			return false;
		}
			
			return result;
		
	}

	/**
	 * @param server - must contain server p58. or p62.
	 * @param id - index id
	 * @param Url - Driver URL
	 * @return
	 */
	private String  getIdTitle (String server, String id,String[] Url){
		WebDriverWait wait = new WebDriverWait(driver, 15);
		if (!this.isPredProd)
			driver.get(server+Url[0].substring(7)+"wayback/"+id);
		else
			driver.get(Url[0].substring(7)+"wayback/"+id);
		
		//wait until title was loaded
		wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("title")));
		return driver.getTitle();
	}

	/**
	 * @param server - must contains server either p58. or p62.
	 * @param id
	 * @return
	 */
	private String  getIdDate (String server, String id){
		WebElement replay_bar_with_date =null;
		String aux=null;
		String[] date_aux=null;
		
		//This could happen when a page is offline, in that case it can not find the replay_bar with the date		
		try {
			replay_bar_with_date = driver.findElement(By.xpath("//div[@id='replay_bar']"));
		} catch ( org.openqa.selenium.NoSuchElementException e) {
			// TODO Auto-generated catch block
			//System.out.print("\nReplay bar not injected. "+id+"\n");
			return null;
		}
		aux =replay_bar_with_date.getText();
		date_aux = aux.split("Data: ");

		return date_aux[1].replace("[ [esconder] ]", "");
	}


	/**
	 * Inspects if the date is accordingly with the date on the replay bar
	 * @param id
	 * @param i
	 * @return
	 */
	private boolean  inspectDate (String id, int i){
		//WebElement replay_bar_with_date =null;
		String server=null;
		boolean result = true;

		server = DateList.get(i);
		String aux=null;
		String[] date_aux=null;
		DateFormat format = new SimpleDateFormat("HH:mm:ss dd MMMM, yyyy ", new Locale("pt","PT"));
		DateFormat format_arquivo = new SimpleDateFormat("yyyyMMddHHmmss", new Locale("pt","PT"));
		Date date = null;
		String timestamp_file=null;
		String[] timestamp_site= null;
		timestamp_site= id.split("/");
		if (timestamp_site[0].contains("id")) // This syntax does not contain timestamp
			return true;
		try
		{
			
			date = format.parse(server);
			timestamp_file = format_arquivo.format(date).trim();
			if (!(timestamp_file.compareTo(timestamp_site[0].trim())==0)){ // If the timestamp are equals
				System.out.print("\nDate problems on:"+id+"\nDate: "+timestamp_site[0]+"\nDate timestamp:"+timestamp_file+"\n");
				result=false;
			}
		}
		catch (Exception e)
		{
			//System.out.print("\n\nProblems parsing date of the website "+id+"\n"+this.getClass());
			return false;

		}

		return true;
	}
}
