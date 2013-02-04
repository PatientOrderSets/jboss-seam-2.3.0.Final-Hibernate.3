/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */ 
package org.jboss.seam.example.blog.test.selenium;

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

/**
 * 
 * @author Jozef Hartinger
 */
public class NewEntryTest extends SeleniumBlogTest
{

   @Test(groups="entryTest")
   public void simpleEntryTest() {
      
      String id = "simpleBlogEntry";
      String title = "Simple blog entry";
      String excerpt = "This is an excerpt";
      String body = "This is a simple blog entry posted for testing purposes.";
      
      enterNewEntry(id, title, excerpt, body);
      browser.open(CONTEXT_PATH + "/entry/" + id);
      assertFalse("Entry not found.", browser.isElementPresent(getProperty("ENTRY_404")));
      assertEquals("Unexpected entry title found.", title, browser.getText(getProperty("ENTRY_TITLE")));
      assertEquals("Unexpected entry body found.", body, browser.getText(getProperty("ENTRY_BODY")));      
   }
}
