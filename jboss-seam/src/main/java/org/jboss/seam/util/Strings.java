/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.util;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

/**
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 */
public class Strings
{
   
   public static String unqualify(String name)
   {
      return unqualify(name, '.');
   }
   
   public static String unqualify(String name, char sep)
   {
      return name.substring( name.lastIndexOf(sep)+1, name.length() );
   }
   
   public static boolean isEmpty(String string)
   {
      int len;
      if (string == null || (len = string.length()) == 0)
      {
         return true;
      }
      
      for (int i = 0; i < len; i++)
      {
         if ((Character.isWhitespace(string.charAt(i)) == false))
         {
            return false;
         }
      }
      return true;
   }
   
   public static String nullIfEmpty(String string)
   {
      return isEmpty(string) ? null : string;
   }
   
   public static String emptyIfNull(String string)
   {
      return string == null ? "" : string;
   }

   public static String toString(Object component)
   {
      try {
         PropertyDescriptor[] props = Introspector.getBeanInfo( component.getClass() )
               .getPropertyDescriptors();
         StringBuilder builder = new StringBuilder();
         for (PropertyDescriptor descriptor : props)
         {
            builder.append( descriptor.getName() )
               .append('=')
               .append( descriptor.getReadMethod().invoke(component) )
               .append("; ");
         }
         return builder.toString();
      }
      catch (Exception e) {
         return "";
      }
   }

   public static String[] split(String strings, String delims)
   {
      if (strings==null)
      {
         return new String[0];
      }
      else
      {      
         StringTokenizer tokens = new StringTokenizer(strings, delims);
         String[] result = new String[ tokens.countTokens() ];
         int i=0;
         while ( tokens.hasMoreTokens() )
         {
            result[i++] = tokens.nextToken();
         }
         return result;
      }
   }
   
   public static String toString(Object... objects)
   {
      return toString(" ", objects);
   }
   
   public static String toString(String sep, Object... objects)
   {
      if (objects.length==0) return "";
      StringBuilder builder = new StringBuilder();
      for (Object object: objects)
      {
         builder.append(sep).append(object);
      }
      return builder.substring(sep.length());
   }
   
   public static String toClassNameString(String sep, Object... objects)
   {
      if (objects.length==0) return "";
      StringBuilder builder = new StringBuilder();
      for (Object object: objects)
      {
         builder.append(sep);
         if (object==null)
         {
            builder.append("null");
         }
         else
         {
            builder.append( object.getClass().getName() );
         }
      }
      return builder.substring(sep.length());
   }
   
   public static String toString(String sep, Class... classes)
   {
      if (classes.length==0) return "";
      StringBuilder builder = new StringBuilder();
      for (Class clazz: classes)
      {
         builder.append(sep).append( clazz.getName() );
      }
      return builder.substring(sep.length());
   }
   
   public static String toString(InputStream in) throws IOException {
      final StringBuilder out = new StringBuilder();
      final byte[] b = new byte[4096];
      for ( int n; (n = in.read(b)) != -1; ) 
      {
         out.append(new String(b, 0, n));
      }
      return out.toString();
  }

}


