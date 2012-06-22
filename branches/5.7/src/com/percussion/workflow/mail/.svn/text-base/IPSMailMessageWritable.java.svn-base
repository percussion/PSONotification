/*
 * com.percussion.workflow.mail IPSMailMessageWritable.java
 *  
 * @author davidbenua
 *
 */
package com.percussion.workflow.mail;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * This interface specified a writable message context.  It extends the standard 
 * notification mail message context, which is immutable.
 *
 * @author DavidBenua
 */
public interface IPSMailMessageWritable extends IPSMailMessageContext
{
   /**
    * Sets the message body.  
    * @param body the body to set. 
    */
   public void setBody(String body); 
   
   /**
    * Adds addresses to the CC list. 
    * @param cc - a string containing one or more addresses, separated with commas.  
    */
   public void addCC(String cc);
   
   /**
    * Adds a list of addresses to the CC list
    * @param cc a list of addresses as <code>java.lang.String</code> objects. Must not be <code>null</code>
    * but may be <code>empty</code>   
    */
   public void addCC(Iterator cc); 
   
   /**
    * Sets the CC list.  Note that unlike the <code>addCC</code> methods, this method resets the list. 
    * @param cc the list of addresses for the CC list. 
    * Must not be <code>null</code> but may be <code>empty</code>   
    */
   public void setCC(Collection cc); 
   
   /**
    * Sets the from address. 
    * @param from the from address to set. 
    */
   public void setFrom(String from); 
   
   /**
    * Sets the mail domain. 
    * @param mailDomain the mail domain to set. 
    */
   public void setMailDomain(String mailDomain);
   
   /**
    * Sets the SMTP Host. The host must be a valid network address. 
    * @param smtpHost the SMTP Host to set. 
    */
   public void setSmtpHost(String smtpHost); 
   
   /**
    * Sets the subject.
    * @param subject the subject to set. 
    */
   public void setSubject(String subject);
   
   /**
    * Adds addresses to the TO list.  
    * @param to one or more addresses to add to the TO field. Multiple addresses should be delimited
    * by commas. 
    */
   public void addTo(String to);

   /**
    * Adds addresses to the TO list.
    * @param to a list of addresses to add.
    * Must not be <code>null</code> but may be <code>empty</code>
    */
   public void addTo(Iterator to); 
   
   /**
    * Sets the TO list.
    * Note that unlike the <code>addTo</code> methods, this method resets the list.
    * @param to the list of addresses to add. This list should contain <code>java.lang.String</code> objects. 
    */
   public void setTo(Collection to); 
   
   /**
    * Sets the URL.  This URL will be included in the mail message. 
    * @param url
    */
   public void setUrl(String url);
   
   /**
    * Gets the current CC list.  Callers must not use this method to modify the CC list, call 
    * <code>setCC</code> instead. 
    * @return the CC list.
    */
   public Set getCCList();
   
   /**
    * Gets the current TO list. Callers must not use this method to modify the TO list, call 
    * <code>setTo</code> instead. 
    * @return the list of To addresses. 
    */
   public Set getToList(); 
}
