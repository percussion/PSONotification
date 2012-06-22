/*
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.pso.workflow PSOSpringMailProgram.java
 *
 */
package com.percussion.pso.workflow;

import com.percussion.pso.workflow.mail.spring.IPSOSpringMailService;
import com.percussion.pso.workflow.mail.spring.PSOSpringMailServiceLocator;
import com.percussion.workflow.mail.IPSMailMessageContext;
import com.percussion.workflow.mail.IPSMailProgram;
import com.percussion.workflow.mail.PSMailException;

/**
 * An implementation of the standard mail program that uses
 * Spring to send the email rather than our internal 
 * Javamail class.  This is useful in cases where the
 * mail server requires additional properties, such as
 * authentication credentials or custom protocols. 
 * <p>
 * Because this class is loaded each time by the 
 * workflow manager, we cannot inject dependencies 
 * into it directly.  Instead this class uses the 
 * PSO Spring Mail Service (IPSOSpringMailService) 
 * and its corresponding locator class.
 *
 * @author davidbenua
 * @see com.percussion.pso.workflow.mail.spring.IPSOSpringMailService
 * @see com.percussion.pso.workflow.mail.spring.PSOSpringMailServiceLocator
 */
public class PSOSpringMailProgram implements IPSMailProgram
{
   private static IPSOSpringMailService mailService = null; 
   /**
    * initializes the mail program
    * @see com.percussion.workflow.mail.IPSMailProgram#init()
    */
   public void init() throws PSMailException
   {
      //cannot do this in init(), as Spring environment does not yet exist
      //when this is called. 
      //mailService = PSOSpringMailServiceLocator.getMailService(); 
   }
   /**
    * Sends the message.
    * @param messageContext the message to send.
    * @throws PSMailException when any error occurs.  
    * @see com.percussion.workflow.mail.IPSMailProgram#sendMessage(com.percussion.workflow.mail.IPSMailMessageContext)
    */
   public void sendMessage(IPSMailMessageContext messageContext)
         throws PSMailException
   {
      if(mailService == null)
      {
         mailService = PSOSpringMailServiceLocator.getMailService(); 
      }
      mailService.send(messageContext);
   }
   /**
    * Terminates the mail program. 
    * Required by the interface, does nothing. 
    * @see com.percussion.workflow.mail.IPSMailProgram#terminate()
    */
   public void terminate() throws PSMailException
   {
      //nothing to do here. 
   }
   
   /**
    * Sets the mail service. Used only for testing. 
    * @param svc
    */
   protected void setMailService(IPSOSpringMailService svc)
   {
      mailService = svc; 
   }
}
