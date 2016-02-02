package pt.fccn.arquivo.tests;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import pt.fccn.arquivo.pages.Arcproxyinspection;
import pt.fccn.arquivo.pages.IndexPage;
import pt.fccn.saw.selenium.WebDriverTestBase;



/**
 * @author nutchwax
 *
 */
public class TestArcproxy extends WebDriverTestBase{
	@Test
	public void TestArcproxy() {
		System.out.print("Running TestArcproxy. \n");
		IndexPage index = new IndexPage(driver);
		Ispre_prod=index.setPreProd(pre_prod);
		Arcproxyinspection arcproxy = index.arcProxy(Ispre_prod);

		try {
			assertTrue("There are problems in the coherence of ArcProxy ",arcproxy.inspectArcproxy(false));
			assertTrue("The date of archived pages are not the same before perfomed ",arcproxy.inspectArcproxy(true));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}


