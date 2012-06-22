/*
 * com.percussion.workflow PSCaseInsensitiveString.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.workflow;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

/**
 * A String that compares case insensitive by default.
 * This does not really belong here, but the way that the 
 * old code does this is braindead.   
 * 
 * @author DavidBenua
 *
 */
public class PSCaseInsensitiveString
      implements
         Serializable,
         Comparable,
         Cloneable
{
   private String s; 
   /**
    * 
    */
   public PSCaseInsensitiveString(String s)
   {
      this.s = s; 
   }
   
   /**
    * Copy constructor. 
    * @param ss
    */
   public PSCaseInsensitiveString(PSCaseInsensitiveString ss)
   {
      this.s = ss.toString(); 
   }
   
   /**
    * @see java.lang.Comparable#compareTo(java.lang.Object)
    */
   public int compareTo(Object o)
   {
      return String.CASE_INSENSITIVE_ORDER.compare(s, o); 
   }
   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object obj)
   {
      if(obj == null)
      {
         return false; 
      }
      if(!(obj instanceof PSCaseInsensitiveString) && 
            !(obj instanceof String))
      {
         return false; 
      }
      return s.equalsIgnoreCase(obj.toString()); 
   }
   
   private volatile int hash = 0; 
   /**
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      if(hash == 0)
      { 
         hash = s.toUpperCase().toLowerCase().hashCode();  
      }
      return hash; 
   }
   /**
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      return s; 
   }
   
   /**
    * Produces a string representation of a collection of PSCaseInsensitiveStrings. 
    * @param values a collection of PSCaseInsensitiveString objects. 
    * @param separator the desired separator. 
    * @return the delimited string. Never <code>null</code>
    */
   public static String append(Collection values, String separator)
   {
      if(values == null)
         throw new IllegalArgumentException("values to append can not be null");

      if(separator == null)
         separator = ",";

      Iterator iter = values.iterator();

      StringBuffer buffer = new StringBuffer();

      String sep = null;
      while(iter.hasNext())
      {
         Object value = iter.next(); 
         if(sep != null)
         {
            buffer.append(sep);
         }
         else
         {
            sep = separator; 
         }
         buffer.append(value);
      }

      return buffer.toString();
   }

   
}
