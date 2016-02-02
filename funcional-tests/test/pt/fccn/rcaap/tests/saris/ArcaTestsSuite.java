/*
Copyright (C) 2013 Fernando Ribeiro <fernando.ribeiro@fccn.pt>
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
package pt.fccn.rcaap.tests.saris;


/*
 * It is possible to create individual packages for SARIS
 * In Jenkins we need to add a directory for the workspace to be the same for all SARI tests
 * And not create multiple project files from GIT in Jenkins
 * Test this when possible - 7-10-2013
 */
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
* @author Fernando Ribeiro
*
*/
@RunWith(Suite.class)
@SuiteClasses({ ArcaHomePage.class, ArcaSimpleSearch.class})
//@SuiteClasses({ ArcaSimpleSearch.class})
public class ArcaTestsSuite {

}