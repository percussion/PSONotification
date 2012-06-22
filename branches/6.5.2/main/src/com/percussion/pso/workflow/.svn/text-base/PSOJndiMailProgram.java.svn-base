/*
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.pso.workflow PSOJndiMailProgram.java
 *
 */
package com.percussion.pso.workflow;

import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.extension.IPSExtensionErrors;
import com.percussion.workflow.mail.IPSMailMessageContext;
import com.percussion.workflow.mail.IPSMailProgram;
import com.percussion.workflow.mail.PSMailException;

/**
 * Mail program that sends via the container's JNDI mail connection. 
 * The JNDI mail program has capabilities that our simplistic Java mail 
 * client lacks, including support for authentication.  
 * <p>
 * The JNDI mail program is configured in the  
 *
 * @author davidbenua
 *
 */
public class PSOJndiMailProgram implements IPSMailProgram
{
   private static Log log = LogFactory.getLog(PSOJndiMailProgram.class);
   private javax.mail.Session mailSession = null; 
   /**
    * 
    */
   public PSOJndiMailProgram()
   {
      
   }
   /**
    * @see com.percussion.workflow.mail.IPSMailProgram#init()
    */
   public void init() throws PSMailException
   {
      try
      {
         log.debug("binding mail context"); 
         Context initCtx = new InitialContext();
         Context envCtx = (Context)initCtx.lookup("java:comp/env");
         if(log.isDebugEnabled())
         {
            listContext(envCtx);
         }
         Object mailObject = envCtx.lookup("mail/defaultMail");
         mailSession = (Session)PortableRemoteObject.narrow(mailObject, Session.class); 
          
      } catch (NamingException ex)
      {
          log.error("Unexpected Exception " + ex,ex);
      }
   }
   
   @SuppressWarnings("unchecked")
   private void listContext(Context ctx)
   {
      try
      {
         log.debug("listing context " + ctx.getNameInNamespace()); 
         NamingEnumeration ne = ctx.list("java:");
         while (ne.hasMore())
         {
            NameClassPair pair = (NameClassPair) ne.next();
            log.debug("pair: " + pair);
         }
      } catch (NamingException ex)
      {
         log.error("Naming Exception " + ex,ex);
      }   
    
   }
   /**
    * @see com.percussion.workflow.mail.IPSMailProgram#sendMessage(com.percussion.workflow.mail.IPSMailMessageContext)
    */
   public void sendMessage(IPSMailMessageContext messageContext)
         throws PSMailException
   {
      try
      {
         Message msg = new MimeMessage(mailSession);
         String mailDomain = messageContext.getMailDomain();
         if ((null == mailDomain) || mailDomain.length() == 0)
         {
            throw new PSMailException(IPSExtensionErrors.MAIL_DOMAIN_EMPTY);
         }
         String mailCc = messageContext.getCc();
         String mailURL = messageContext.getURL();

         if (StringUtils.isNotBlank(messageContext.getFrom()))
         {
            msg.setFrom(makeAddress(messageContext.getFrom(), mailDomain)[0]);
         }

         msg.setRecipients(Message.RecipientType.TO,
                           makeAddress(messageContext.getTo(), mailDomain));

         if ((null != mailCc) &&  mailCc.length() > 0)
         {
                  msg.setRecipients(Message.RecipientType.CC,
                           makeAddress(mailCc, mailDomain));
         }

         msg.setSubject(messageContext.getSubject());
         msg.setSentDate(new java.util.Date());

         if ((null == mailURL) || mailURL.length() == 0)
         {
            msg.setText(messageContext.getBody());
         }
         else
         {
            msg.setText(messageContext.getBody() + "\r\n\r\n" +
                        messageContext.getURL());
         }
         Transport.send(msg);
      }
      catch (AddressException e)
      {
         throw new PSMailException(e);
      }
      catch (MessagingException e)
      {
         throw new PSMailException(e);
      }
      
   }
   
   /**
    * Helper routine that construct an array of internet address from a
    * comma-separated list of users or roles, appending the mail domain name to
    * any address that does not contain a "@".
    *
    * @param sUserList  comma-separated list of users.
    * @param mailDomain name of the mail domain. May optionally contain a
    *                   leading "@".
    *
    * @throws AddressException, if it cannot make Internet Addresses.
    *
    */
   private InternetAddress[] makeAddress(String sUserList,
                                         String mailDomain)
      throws AddressException
   {
      ArrayList<InternetAddress> l = new ArrayList<InternetAddress>();
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

            InternetAddress userInetAddress =
                  new InternetAddress(userAddressString);
            l.add(userInetAddress);
         }
      }
      return l.toArray(new InternetAddress[0]);
 
   }


   /**
    * @see com.percussion.workflow.mail.IPSMailProgram#terminate()
    */
   public void terminate() throws PSMailException
   {
      
   }
}
