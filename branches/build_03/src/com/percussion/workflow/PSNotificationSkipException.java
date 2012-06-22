/*
 * com.percussion.workflow PSNotificationSkipException.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.workflow;

/**
 * An innocuous exception for the PSNotifyBase class.  Throw this 
 * exception when the notification should be aborted without 
 * sending mail.  This does not imply that an error has occurred. 
 *
 * @author DavidBenua
 *
 */
public class PSNotificationSkipException extends RuntimeException
{
   /**
    * 
    */
   public PSNotificationSkipException()
   {
      super("Notification Skipped");     
   }
   /**
    * @param message
    */
   public PSNotificationSkipException(String message)
   {
      super(message);
   }
   /**
    * @param cause
    */
   public PSNotificationSkipException(Throwable cause)
   {
      super(cause);
   }
   /**
    * @param message
    * @param cause
    */
   public PSNotificationSkipException(String message, Throwable cause)
   {
      super(message, cause);
   }
}
