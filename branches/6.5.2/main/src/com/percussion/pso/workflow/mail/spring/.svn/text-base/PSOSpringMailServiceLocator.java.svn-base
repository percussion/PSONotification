/*
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.pso.workflow.mail.spring PSOSpringMailServiceLocator.java
 *
 */
package com.percussion.pso.workflow.mail.spring;

import com.percussion.services.PSBaseServiceLocator;

/**
 * Service Locator for Spring mail service.  
 *
 * @author davidbenua
 *
 */
public class PSOSpringMailServiceLocator extends PSBaseServiceLocator
{
   /**
    * Gets the mail service.
    * @return the mail service implementation. 
    */
   public static IPSOSpringMailService getMailService()
   {
      return (IPSOSpringMailService) getBean(PSO_SPRING_MAIL_SERVICE); 
   }
   
   /**
    * Bean name for spring mail service
    */
   public static final String PSO_SPRING_MAIL_SERVICE = "psoSpringMailService"; 
}
