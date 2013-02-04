package org.jboss.seam.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public class EnumerationEnumeration<T> implements Enumeration<T>
{
   private Enumeration<T>[] enumerations;
   private int loc = 0;

   public EnumerationEnumeration(Enumeration<T>[] enumerations)
   {
      this.enumerations = enumerations;
   }

   public boolean hasMoreElements()
   {
      for (int i = loc; i < enumerations.length; i++)
      {
         if (enumerations[i].hasMoreElements())
         {
            return true;
         }
      }
      return false;
   }

   public T nextElement()
   {
      while ( isCurrentEnumerationAvailable() )
      {
         if ( currentHasMoreElements() )
         {
            return currentNextElement();
         }
         else
         {
            nextEnumeration();
         }
      }
      throw new NoSuchElementException();
   }

   private void nextEnumeration()
   {
      loc++;
   }

   /*private boolean isNextEnumerationAvailable()
   {
      return loc < enumerations.length-1;
   }*/

   private boolean isCurrentEnumerationAvailable()
   {
      return loc < enumerations.length;
   }

   private T currentNextElement()
   {
      return enumerations[loc].nextElement();
   }

   private boolean currentHasMoreElements()
   {
      return enumerations[loc].hasMoreElements();
   }
    
}
