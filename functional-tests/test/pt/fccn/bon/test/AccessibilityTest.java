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

package pt.fccn.bon.test;

import pt.fccn.saw.selenium.SeleniumTestBase;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * A example of a test class using HtmlUnitDriver.
 * 
 * @see pt.fccn.saw.test.SeleniumTestBase
 */
public class AccessibilityTest extends SeleniumTestBase {

	/**
	 * An example test.
	 * Before each test, the browser is set to the starting test url.
	 */
	@Test
	public void testExample() throws Exception {
		selenium.open("/");
		selenium.click("//a[@href='http://www.acesso.umic.pt/webax/index.php?ent=310']");
		selenium.waitForPageToLoad("30000");
	
		String validationMessage = selenium.getText("css=p.webax_info > strong");
		if (!validationMessage.equals("passa o nível AAA")) {
			fail("\neXaminator accessibility test failed.\n"
			+"Got messsage: "+ validationMessage +"\n"
			+"\tAAA:\t"+ selenium.getText("//p[@class='webax_info']/*[starts-with(@title, 'aaa:')]") +"\n"
			+"\tAA:\t"+ selenium.getText("//p[@class='webax_info']/*[starts-with(@title, 'aa:')]") +"\n"
			+"\tA:\t"+ selenium.getText("//p[@class='webax_info']/*[starts-with(@title, 'a:')]") +"\n"
			+"\tX:\t"+ selenium.getText("//p[@class='webax_info']/*[starts-with(@title, 'x:')]") +"\n");
		}
	}
}
