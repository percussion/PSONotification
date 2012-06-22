/*
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.workflow PSTokenSubstitutionProcessorFactory.java
 *
 */
package com.percussion.workflow;

public class PSTokenSubstitutionProcessorFactory implements IPSTokenSubstitutionProcessorFactory
{
   public IPSTokenSubstitutionProcessor getTokenSubstitutionProcessor()
   {
      return new PSTokenSubstitutionProcessor(); 
   }
}
