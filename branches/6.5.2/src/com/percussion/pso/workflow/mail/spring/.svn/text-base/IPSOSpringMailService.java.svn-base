/*
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.pso.workflow.mail.spring IPSOSpringMailService.java
 *
 */
package com.percussion.pso.workflow.mail.spring;
import com.percussion.workflow.mail.IPSMailMessageContext;
import com.percussion.workflow.mail.PSMailException;


/**
 * Adaptor service for Spring's mail sender. 
 * This service sends IPSMailMessageContext objects through
 * Spring's mailSender.  The advantage here is that Spring provides
 * additional configuration and options that our 
 * simple mail program does not.
 *
 * @author davidbenua
 *
 */
public interface IPSOSpringMailService
{
   /**
    * Sends a message.
    * @param message the message to send. 
    */
   public void send(IPSMailMessageContext message)
     throws PSMailException;
}