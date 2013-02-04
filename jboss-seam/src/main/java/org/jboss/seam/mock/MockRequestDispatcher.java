/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.seam.mock;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * Mock implementation of the {@link javax.servlet.RequestDispatcher} interface.
 * <p/>
 * <p>Used for testing the web framework; typically not necessary for
 * testing application controllers.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.0.2
 */
public class MockRequestDispatcher implements RequestDispatcher
{

   private static final LogProvider logger = Logging.getLogProvider(MockRequestDispatcher.class);
   
   private final String url;


   /**
    * Create a new MockRequestDispatcher for the given URL.
    *
    * @param url the URL to dispatch to.
    */
   public MockRequestDispatcher(String url)
   {
      this.url = url;
   }


   public void forward(ServletRequest request, ServletResponse response)
   {
      if (response.isCommitted())
      {
         throw new IllegalStateException("Cannot perform forward - response is already committed");
      }
      getMockHttpServletResponse(response).setForwardedUrl(this.url);
      if (logger.isDebugEnabled())
      {
         logger.debug("MockRequestDispatcher: forwarding to URL [" + this.url + "]");
      }
   }

   public void include(ServletRequest request, ServletResponse response)
   {
      getMockHttpServletResponse(response).setIncludedUrl(this.url);
      if (logger.isDebugEnabled())
      {
         logger.debug("MockRequestDispatcher: including URL [" + this.url + "]");
      }
   }

   /**
    * Obtain the underlying EnhancedMockHttpServletResponse,
    * unwrapping {@link javax.servlet.http.HttpServletResponseWrapper} decorators if necessary.
    */
   protected EnhancedMockHttpServletResponse getMockHttpServletResponse(ServletResponse response)
   {
      if (response instanceof EnhancedMockHttpServletResponse)
      {
         return (EnhancedMockHttpServletResponse) response;
      }
      if (response instanceof HttpServletResponseWrapper)
      {
         return getMockHttpServletResponse(((HttpServletResponseWrapper) response).getResponse());
      }
      throw new IllegalArgumentException("MockRequestDispatcher requires MockHttpServletResponse");
	}

}