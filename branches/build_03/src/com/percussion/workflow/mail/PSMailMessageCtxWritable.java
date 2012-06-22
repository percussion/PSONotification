/*[ PSMailMessageCtxWritable.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.workflow.mail;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.Set;
import com.percussion.workflow.PSCaseInsensitiveString;
import com.percussion.workflow.PSWorkFlowUtils;

/**
 * This class implements the interface <code>IPSMailMessageWritable</code> 
 * 
 * @author davidbenua
 */
public class PSMailMessageCtxWritable implements IPSMailMessageWritable
{
   /**
    * @see com.percussion.workflow.mail.IPSMailMessageWritable#getCCList()
    */
   public Set getCCList()
   {
      return m_Cc; 
   }

   /**
    * @see com.percussion.workflow.mail.IPSMailMessageWritable#getToList()
    */
   public Set getToList()
   {
      return m_To; 
   }

   /**
    * @see com.percussion.workflow.mail.IPSMailMessageWritable#setCC(java.util.Collection)
    */
   public void setCC(Collection cc)
   {
      m_Cc.clear();
      this.addCC(cc.iterator()); 
   }

   /**
    * @see com.percussion.workflow.mail.IPSMailMessageWritable#setTo(java.util.Collection)
    */
   public void setTo(Collection to)
   {
      m_To.clear();
      this.addTo(to.iterator()); 
      
   }

   public PSMailMessageCtxWritable(String from, Set to, Set cc,
      String subject, String body, String url, String mailDomain,
      String smtpHost)
   {
      if(to == null || to.size() < 1)
      {
         throw new IllegalArgumentException(
            "Recipient's address must not be " +
            "empty or null in message context");
      }
      m_From = from;
      this.addCC(cc.iterator()); 
      this.addTo(to.iterator()); 
      m_Subject = subject;
      m_Body = body;
      m_Url = url;
      m_MailDomain = mailDomain;
      m_SmtpHost = smtpHost;
   }
   
   public PSMailMessageCtxWritable()
   {
      
   }

   /*
    * Implementation of the method in the interface
    */
   public String getFrom()
   {
      return m_From;
   }

   /*
    * Implementation of the method in the interface
    */
   public String getTo()
   {
      if(m_To.isEmpty())
      {
         return null; 
      }
      
      return PSCaseInsensitiveString.append(m_To, PSWorkFlowUtils.EMAIL_STRING_SEPARATOR);
   }

   /*
    * Implementation of the method in the interface
    */
   public String getCc()
   {
      if(m_Cc.isEmpty())
      {
         return null; 
      }
      return PSCaseInsensitiveString.append(m_Cc, PSWorkFlowUtils.EMAIL_STRING_SEPARATOR);
   }

   /*
    * Implementation of the method in the interface
    */
   public String getSubject()
   {
      return m_Subject;
   }

   /*
    * Implementation of the method in the interface
    */
   public String getBody()
   {
      return m_Body;
   }

   /*
    * Implementation of the method in the interface
    */
   public String getURL()
   {
      return m_Url;
   }

   /*
    * Implementation of the method in the interface
    */
   public String getMailDomain()
   {
      return m_MailDomain;
   }

   /*
    * Implementation of the method in the interface
    */
   public String getSmtpHost()
   {
      return m_SmtpHost;
   }

   /**
    * Name of the user the mail notification is issued on behalf of.
    */
   private String m_From = null;

   /**
    * Names of the users or roles the mail notification is required to reach.
    */
   private Set m_To = new LinkedHashSet(); 

   /**
    * CC list users a copy of the mail notification is to be sent.
    */
   private Set m_Cc = new LinkedHashSet();

   /**
    * Subject of the mail notification.
    */
   private String m_Subject = null;

   /**
    * The Body of the mail message.
    */
   private String m_Body = null;

   /**
    * Url of the content item to include in the mail notification.
    */
   private String m_Url = null;

   /**
    * Mail Domain of the sender or recipients.
    */
   private String m_MailDomain = null;

   /**
    * SMTP host name for the mail plugin
    */
   private String m_SmtpHost = null;

   /**
    * @param body The m_Body to set.
    */
   public void setBody(String body)
   {
      m_Body = body;
   }

   /**
    * @param cc The m_Cc to set.
    */
   public void addCC(String cc)
   {
      this.addCC(PSWorkFlowUtils.tokenizeString(cc, PSWorkFlowUtils.EMAIL_STRING_SEPARATOR).iterator());
   }
   
   public void addCC(Iterator list)
   {
      while(list.hasNext())
      { 
         PSCaseInsensitiveString cis = 
            new PSCaseInsensitiveString((String)list.next()); 
         m_Cc.add(cis);   
      }
      
   }
   

   /**
    * @param from The m_From to set.
    */
   public void setFrom(String from)
   {
      m_From = from;
   }

   /**
    * @param mailDomain The m_MailDomain to set.
    */
   public void setMailDomain(String mailDomain)
   {
      m_MailDomain = mailDomain;
   }

   /**
    * @param smtpHost The m_SmtpHost to set.
    * @see com.percussion.workflow.mail.IPSMailMessageWritable#setSmtpHost(java.lang.String)
    */
   public void setSmtpHost(String smtpHost)
   {
      m_SmtpHost = smtpHost;
   }

   /**
    * @param subject The m_Subject to set.
    * @see com.percussion.workflow.mail.IPSMailMessageWritable#setSubject(java.lang.String)
    */
   public void setSubject(String subject)
   {
      m_Subject = subject;
   }

   /**
    * @param to The m_To to set.
    * @see com.percussion.workflow.mail.IPSMailMessageWritable#addTo(java.lang.String)
    */
   public void addTo(String to)
   {
      this.addTo(PSWorkFlowUtils.tokenizeString(to, PSWorkFlowUtils.EMAIL_STRING_SEPARATOR).iterator()); 
   }

   /**
    * @see com.percussion.workflow.mail.IPSMailMessageWritable#addTo(java.util.Iterator)
    */
   public void addTo(Iterator list)
   {
      while(list.hasNext())
      { 
         PSCaseInsensitiveString cis = 
            new PSCaseInsensitiveString((String)list.next()); 
         m_To.add(cis);   
      }
   }

   /**
    * Sets the URL.
    * @param url The m_Url to set.
    * @see com.percussion.workflow.mail.IPSMailMessageWritable#setUrl(java.lang.String)
    */
   public void setUrl(String url)
   {
      m_Url = url;
   }
}
