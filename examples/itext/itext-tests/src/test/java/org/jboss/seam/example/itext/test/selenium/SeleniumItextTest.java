package org.jboss.seam.example.itext.test.selenium;

import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SeleniumItextTest extends SeamSeleniumTest
{
   public static final String HOME_PAGE = "/index.seam";
   public static final String HOME_PAGE_TITLE = "Seam PDF";
   
   @BeforeMethod
   @Override
   public void setUp() {
      super.setUp();
      browser.open(CONTEXT_PATH + HOME_PAGE);
   }
   
   /**
    * Place holder - just verifies that example deploys
    */
   @Test
   public void homePageLoadTest() {
      assertEquals("Unexpected page title.", HOME_PAGE_TITLE, browser.getTitle());
   }
}
