/*
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.pso.workflow.mail.spring PSOSpringMailServiceImpl.java
 *
 */
package com.percussion.pso.workflow.mail.spring;

import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.mail.internet.AddressException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import com.percussion.workflow.mail.IPSMailMessageContext;
import com.percussion.workflow.mail.PSMailException;

/**
 * Implementation for the Spring mail service. 
 * This service uses the standard Spring email integration
 * packages to send  
 * 
 *
 * @author davidbenua
 *
 */
public class PSOSpringMailServiceImpl implements IPSOSpringMailService
{
   private static Log log = LogFactory.getLog(PSOSpringMailServiceImpl.class);
   
   private MailSender mailSender;
   
   /**
    * Sends a message via the Spring mail sender. 
    * @see com.percussion.pso.workflow.mail.spring.IPSOSpringMailService#send(com.percussion.workflow.mail.IPSMailMessageContext)
    */
   public void send(IPSMailMessageContext message)
      throws PSMailException
   {
      log.debug("sending message " + message); 
      try{
         
         SimpleMailMessage smsg = buildMessage(message);
         mailSender.send(smsg);
      }catch (Exception ex)
      {
            log.error("Unexpected Exception Sending mail" + ex,ex);
            throw new PSMailException(ex); 
      } 
   }   

   /**
    * Builds a Spring SimpleMailMessage based on a MessageContext.
    * The message context must contain a valid address and mail domain.
    * @param message the message context
    * @return the Spring message. 
    * @throws AddressException if the address cannot be built. 
    */
   protected SimpleMailMessage buildMessage(IPSMailMessageContext message)
         throws AddressException
   {
      SimpleMailMessage smsg = new SimpleMailMessage();
      String domain = message.getMailDomain();
      Validate.notEmpty(domain);
      smsg.setTo(makeAddress(message.getTo(), domain));
      smsg.setFrom(makeAddress(message.getFrom(), domain)[0]);
      smsg.setCc(makeAddress(message.getCc(), domain));
      smsg.setSubject(message.getSubject());
      smsg.setSentDate(new java.util.Date());
      String mailURL = message.getURL();
      if (StringUtils.isBlank(mailURL))
      {
         smsg.setText(message.getBody());
      } else
      {
         smsg.setText(message.getBody() + "\r\n\r\n" + mailURL);
      }
      return smsg;
   }
   
   /**
    * Helper routine that construct an array of internet address from a
    * comma-separated list of users or roles, appending the mail domain name to
    * any address that does not contain a "@".
    * 
    * @param sUserList
    *            comma-separated list of users.
    * @param mailDomain
    *            name of the mail domain. May optionally contain a leading "@".
    * 
    * @throws AddressException,
    *             if it cannot make Internet Addresses.
    * 
    */
   private String[] makeAddress(String sUserList,
                                         String mailDomain)
      throws AddressException
   {
      ArrayList<String> l = new ArrayList<String>();
      if(StringUtils.isBlank(sUserList))
      {
         return l.toArray(new String[0]); 
      }
      StringTokenizer tokenizer = new StringTokenizer(sUserList, ",");
      String sToken = null;
      String userAddressString = null;
      if (!mailDomain.startsWith("@"))
      {
         mailDomain = "@" + mailDomain;
      }
      while(tokenizer.hasMoreElements())
      {
         sToken = tokenizer.nextToken().trim();
         if (0 != sToken.length() )
         {
            if (-1 == sToken.indexOf('@'))
            {
               userAddressString = sToken +  mailDomain;
            }
            else
            {
               userAddressString = sToken;
            }

            l.add(userAddressString);
         }
      }
      return l.toArray(new String[0]);
 
   }
   /**
    * Gets the mail sender. 
    * @return the mailSender
    */
   public MailSender getMailSender()
   {
      return mailSender;
   }

   /**
    * Sets the mail sender. 
    * @param mailSender the mailSender to set
    */
   public void setMailSender(MailSender mailSender)
   {
      this.mailSender = mailSender;
   }
    
   
}
