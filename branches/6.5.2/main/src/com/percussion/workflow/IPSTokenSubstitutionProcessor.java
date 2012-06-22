/*
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.workflow IPSTokenSubstitutionProcessor.java
 *
 */
package com.percussion.workflow;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;

import com.percussion.server.IPSRequestContext;
public interface IPSTokenSubstitutionProcessor
{
   public void processTokenValues(int contentId, IPSRequestContext request)
         throws RepositoryException;
   /**
    * Parses the supplied text for field tokens and adds them to the supplied
    * set.
    * 
    * @param text The text to parse, assumed not <code>null</code>, may be 
    * empty.
    * @param fieldNames The set to which discovered field names are added, 
    * assumed not <code>null</code>. 
    */
   public void parseFields(String text);
   /**
    * 
    */
   public String replaceTokens(String str);
   public Map<String, String> getTokenValues();
   public Set<String> getFieldNames();
   public void addExtraTokens(Map<String,String> extras); 
   public void setDateFormat(SimpleDateFormat dateFormat);
}