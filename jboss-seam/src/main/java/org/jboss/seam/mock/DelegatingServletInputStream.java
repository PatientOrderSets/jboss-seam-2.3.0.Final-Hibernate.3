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

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Delegating implementation of {@link javax.servlet.ServletInputStream}.
 * <p/>
 * <p>Used by {@link MockHttpServletRequest}; typically not directly
 * used for testing application controllers.
 *
 * @author Juergen Hoeller
 * @see MockHttpServletRequest
 * @since 1.0.2
 */
public class DelegatingServletInputStream extends ServletInputStream
{

   private final InputStream sourceStream;


   /**
    * Create a DelegatingServletInputStream for the given source stream.
    *
    * @param sourceStream the source stream (never <code>null</code>)
    */
   public DelegatingServletInputStream(InputStream sourceStream)
   {
      this.sourceStream = sourceStream;
   }

   /**
    * Return the underlying source stream (never <code>null</code>).
    */
   public final InputStream getSourceStream()
   {
      return this.sourceStream;
   }


   @Override
   public int read() throws IOException
   {
      return this.sourceStream.read();
   }

   @Override
   public void close() throws IOException
   {
      super.close();
      this.sourceStream.close();
	}

}