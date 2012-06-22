/* *****************************************************************************
 *
 * [ PSExitNotifyBase.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.workflow;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import com.percussion.cms.IPSConstants;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSStringOperation;
import com.percussion.workflow.mail.IPSMailMessageWritable;
import com.percussion.workflow.mail.IPSMailProgram;
import com.percussion.workflow.mail.PSMailException;
import com.percussion.workflow.mail.PSMailMessageCtxWritable;

/**
 * This exit sends mail notifications to the assigned roles for the new state
 * after transition. This class is loosely based on 
 * <code>com.percussion.workflow.PSExitNotifyAssignees</code>. 
 * 
 * <h2>Extending this exit</h2>
 * This version of the exit is designed to be extensible.  The base exit has
 * no functionality that is not in the standard production exit. To implement
 * additional functions, extend this class and implement one or more of the
 * protected methods provided for the purpose. 
 * <ul>
 * <li>checkNotification allows subclasses to abort notification before any 
 * processing is done. 
 * <li>getStateUserList allows subclasses to change the lookup of uses for a
 * workflow state.
 * <li>getTokenValues allows subclasses to define additional substitution 
 * tokens.  Only <code>$wfComment</code> is provided by default. 
 * <li>lookupUserMailAddress allows subclasses to change the user email lookup. 
 * <li>sendMail allows subclasses to change the message just before it it sent. 
 * <li>setCCAddress allows subclasses to process the CC list
 * <li>setFromAddress allows subclasses to change the From address
 * <li>setMessageUrl allows subclasses to change the URL included in the body 
 * of the message.
 * <li>setToAddress allows subclasses to process the To address list.
 * </ul>
 * In addition, these methods may throw 
 * <code>com.percussion.workflow.PSNotificationSkipException</code> at any point.
 * This exception will terminate notification processing silently without 
 * logging an error.  
 * <br>
 * <h2>Installation Instructions</h2>
 * <ol>
 * <li>Install PSONotification.jar and update <code>RhythmyxServer.cp2</code>
 * if necessary. 
 * <li>Register the exit, using the <code>Extensions.xml</code> provided. 
 * <li>Edit <code>ContentEditorSystemDef</code> and   
 * replace the reference to <code>sys_wfSendNotifications</code>
 * with <code>PSExitNotifyBase</code> 
 * </ol>
 * You should not need to make any other changes, and
 * your change will survive a server upgrade.  
 */
public class PSExitNotifyBase implements IPSResultDocumentProcessor
{
   /**
    * The fully qualified name of this extension.
    */
   static private String m_fullExtensionName = "";

   /* Set the parameter count to not initialized */
   static private int ms_correctParamCount = IPSExtension.NOT_INITIALIZED;

   protected static Logger log = Logger.getLogger(PSExitNotifyBase.class);
   
   /**************  IPSExtension Interface Implementation ************* */
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      if (ms_correctParamCount == IPSExtension.NOT_INITIALIZED)
      {
         ms_correctParamCount = 0;

         Iterator iter = extensionDef.getRuntimeParameterNames();
         while(iter.hasNext())
         {
            iter.next();
            ms_correctParamCount++;
         }
         m_fullExtensionName = extensionDef.getRef().toString();
      }
   }

   /**
    * Process the notification messages. 
    * @see com.percussion.extension.IPSResultDocumentProcessor#processResultDocument(java.lang.Object[], com.percussion.server.IPSRequestContext, org.w3c.dom.Document)
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      log.debug("Entering Notification"); 
      PSWorkFlowUtils.printWorkflowMessage(request,
            "\nNotify Assignees: enter processResultDocument ");
      int transitionID = 0;
      int toStateID = 0;
      int fromStateID = 0;
      int contentID = 0;
      int workflowID = 0;
      int revisionID = 0;
      String userName = null;
      PSWorkFlowContext wfContext = null;
      PSTransitionsContext tc = null;
      PSWorkflowRoleInfo wfRoleInfo = null;
      PSContentStatusContext csc = null;
      String contentURL = null;
      String lang = null;
      PSConnectionMgr connectionMgr = null;
      Connection connection = null;
      try
      {
         if (null == request)
         {
            throw new PSExtensionProcessingException(m_fullExtensionName,
                  new IllegalArgumentException("The request must not be null"));
         }
         lang = (String) request
               .getSessionPrivateObject(PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
         if (lang == null)
            lang = PSI18nUtils.DEFAULT_LANG;
         wfContext = (PSWorkFlowContext) request
               .getPrivateObject(IPSWorkFlowContext.WORKFLOW_CONTEXT_PRIVATE_OBJECT);
         if (null == wfContext)
         {
            log.debug("No transition, no notification sent"); 
            PSWorkFlowUtils.printWorkflowMessage(request,
                  "Notify assignees: - no transition was performed - "
                        + "no notifications will be sent");
            return resDoc;
         }
         revisionID = wfContext.getBaseRevisionNum();
         transitionID = wfContext.getTransitionID();
         if (IPSConstants.TRANSITIONID_NO_ACTION_TAKEN == transitionID
               || IPSConstants.TRANSITIONID_CHECKINOUT == transitionID)
         {
            PSWorkFlowUtils.printWorkflowMessage(request,
                  "Notify assignees: - no transition was performed - "
                        + "no notifications will be sent");
            return resDoc; // no action at all - no history
         }
         workflowID = wfContext.getWorkflowID();
         toStateID = wfContext.getStateID();
         // Note: we could get content id from workflow context
         if (null == params[0] || 0 == params[0].toString().trim().length())
         {
            log.debug("no content id, no notification sent");
            return resDoc; // no content id means no notifications!
         }
         contentID = new Integer(params[0].toString()).intValue();
         if (0 == contentID)
         {
            log.debug("content id = 0, no notification sent");
            return resDoc; // no content id means no notifications!
         }
         if (null == params[1] || 0 == params[1].toString().trim().length())
         {
            throw new PSInvalidParameterTypeException(lang,
                  IPSExtensionErrors.EMPTY_USRNAME1);
         }
         userName = params[1].toString();
         // Get the connection
         connectionMgr = new PSConnectionMgr();
         connection = connectionMgr.getConnection();
         /*
          * get the old state id from the transition context TODO - put the old
          * state id into the workflow context, to avoid database reads.
          */
         tc = new PSTransitionsContext(transitionID, workflowID, connection);
         tc.close();
         fromStateID = tc.getTransitionFromStateID();
         wfRoleInfo = (PSWorkflowRoleInfo) request
               .getPrivateObject(PSWorkflowRoleInfo.WORKFLOW_ROLE_INFO_PRIVATE_OBJECT);
         if (null == wfRoleInfo)
         {
            throw new PSExtensionProcessingException(m_fullExtensionName,
                  new PSRoleException(lang,
                        IPSExtensionErrors.ROLEINFO_OBJ_NULL));
         }
         contentURL = PSWorkFlowUtils.getContentItemURL(contentID, revisionID,
               request, true);
         csc = new PSContentStatusContext(connection, contentID);
         sendNotifications(contentURL, workflowID, transitionID, fromStateID,
               toStateID, userName, wfRoleInfo, request, connection, String
                     .valueOf(csc.getCommunityID()));
      
      } catch (PSNotificationSkipException se)
      {
         log.debug("Notification Skipped");         
      } catch (PSExtensionProcessingException pe)
      {
         log.error("Error while sending notification with user " + userName
               + " and contentid " + contentID, pe);
         throw (PSExtensionProcessingException)pe.fillInStackTrace(); 
      } catch (Exception e)
      {
         log.error("Error while sending notification with user " + userName
               + " and contentid " + contentID, e);
         // error message should be improved
         String language = null;
         // if(e instanceof PSEntryNotFoundException || e instanceof
         // PSInvalidParameterTypeException )
         // {
         // language = e.getLanguageString();
         // }
         if (language == null)
            language = PSI18nUtils.DEFAULT_LANG;
         throw new PSExtensionProcessingException(language,
               m_fullExtensionName, e);
      } finally
      {
         if (csc != null)
         {
            try
            {
               csc.close();
            } catch (Exception ex)
            {
               // no-op
            }
         }
         try
         {
            if (null != connectionMgr)
               connectionMgr.releaseConnection();
         } catch (SQLException sqe)
         {
         }
         log.debug("End of notification");
         PSWorkFlowUtils.printWorkflowMessage(request,
               "Notify Assignees: exit processResultDocument ");
      }
      return resDoc;
   }

   /**
    * Executive method for sending mail notifications, Gets notifications,
    * constructs recipient list and sends the mail notifications.
    * 
    * @param contentURL
    *           URL of the content item
    * @param workflowID
    *           WorkflowID for the content item
    * @param fromStateID
    *           ID of content state before transition
    * @param toStateID
    *           ID of content state after transition
    * @param userName
    *           name of user sending the notification param wfRoleInfo Object
    *           containing role info such as from and to state adhoc user
    *           information.
    * @param request
    *           request context for the exit
    * @param connection
    *           connection to back-end database
    * @param communityId
    *           the community id by which to filter the subjects to which the
    *           notifications are sent, may be <code>null</code> to ignore the
    *           ccommunity filter.
    * @throws PSMailException
    *            if an error occurs while sending the mail.
    * @throws SQLException
    *            if a database error occurs
    * @throws PSEntryNotFoundException
    *            if there is no data base entry for the content item.
    */
   private void sendNotifications(String contentURL, int workflowID, 
      int transitionID, int fromStateID, int toStateID, String userName, 
      PSWorkflowRoleInfo wfRoleInfo, IPSRequestContext request, 
      Connection connection, String communityId)
         throws PSEntryNotFoundException, PSMailException, SQLException
   {
      PSWorkFlowUtils.printWorkflowMessage(
         request, "  Entering Method sendNotifications");

      List toStateUserList = new ArrayList();
      List fromStateUserList = new ArrayList();
  
      String emailCCString = "";
      PSNotificationsContext nc = null;
      PSTransitionNotificationsContext tnc = null;
      int notificationID = 0;
      String additionalRecipientString = "";
      String subject = "";
      String body = "";
 
        
      
      //Get the notification messages for the transition

      tnc =
            new PSTransitionNotificationsContext(workflowID,
                                                 transitionID,
                                                 connection);
      if (0 == tnc.getCount())
      {
         log.info(
            "  There are no notifications for the transition " +
            transitionID +  " in the workflow " + workflowID + ".");
         return;
      }

      if(!this.checkNotification(request,wfRoleInfo, tnc))
      {
         log.debug("Notification not performed"); 
         return;
      }
      
      // Get whichever state role lists will be needed
      if (tnc.requireFromStateRoles())
      {
         fromStateUserList = this.getStateUserList(request, wfRoleInfo, wfRoleInfo.getFromStateCauc(), workflowID, 
               connection, fromStateID, communityId);
      }


      if (tnc.requireToStateRoles())
      {
         toStateUserList = this.getStateUserList(request, wfRoleInfo, wfRoleInfo.getToStateCauc(), workflowID, 
               connection, toStateID, communityId);
      }

      // get the values for the supported tokens once for the request
      Map tokenValues = getTokenValues(request);

      do // Loop over the notifications for this transition
      {
         IPSMailMessageWritable messageContext = new PSMailMessageCtxWritable();        
         
         this.setFromAddress(request, messageContext, userName, communityId);

         additionalRecipientString = "";
 
         notificationID = tnc.getNotificationID();
         
         // This will throw an exception if the notification does not exist
         nc = new PSNotificationsContext(workflowID,
                                         notificationID,
                                         connection);

         
         subject = parseTokens(nc.getSubject(), tokenValues);
         log.debug("Message Subject is " + subject); 
         messageContext.setSubject(subject); 
         
         body = parseTokens(nc.getBody(), tokenValues);
         log.debug("Message Body is " + body); 
         messageContext.setBody(body); 
         
         additionalRecipientString = tnc.getAdditionalRecipientList();
         
         emailCCString = tnc.getCCList();

         this.setCCAddress(request, messageContext, emailCCString, communityId); 
         
         
         if (tnc.notifyToStateRoles() && !toStateUserList.isEmpty())
         {
            this.setToAddress(request, messageContext, toStateUserList.iterator(), communityId);
         }

         if (tnc.notifyFromStateRoles() && !fromStateUserList.isEmpty())
         {
            this.setToAddress(request, messageContext, fromStateUserList.iterator(), communityId);
         }

         this.setToAddress(request, messageContext, additionalRecipientString, communityId); 

         messageContext.setUrl(contentURL); 
         
         if(messageContext.getToList().isEmpty())
         {
            /*
             * A message must have "To" recipients. Therefore if there are
             * "CC" recipients but no "To"  recipients, send the mail "To"
             * the CC list. This is desired because they should get mail even
             * if all the roles have turned off notification.
             */
             Collection ccList = messageContext.getCCList(); 
             if(!ccList.isEmpty())
             {
                messageContext.setTo(ccList);
                messageContext.setCC(Collections.EMPTY_LIST);
                log.info("  There are no \"to\" recipients for notification  " +
                      notificationID + " in the workflow " + workflowID +
                      ". \"to\" email will be sent to the \"CC\" recipients");
             }
             else
             {
                log.info("  There are no \"to\" or \"CC\"  recipients for notification  "
                      + notificationID + " in the workflow " + workflowID +
                      ", so this notification will not be sent.");
                continue;
             }
             
         }
         
         /*
          * Send mail using the mail domain, host and plugin specified in the
          * workflow properties file.
          */
         String ccMessage = "";
         if (!messageContext.getCCList().isEmpty())
         {
            ccMessage = " , CC to " + messageContext.getCc();
         }
         log.debug("Email sent to " + messageContext.getTo() + ccMessage + ". " +
            "Email sent by " + messageContext.getFrom());

         this.sendMail(request, messageContext);
      }
      while (tnc.moveNext());  // End Loop over mail notifications

      PSWorkFlowUtils.printWorkflowMessage(
         request, "  Exiting Method sendNotifications");
   }

   /**
    * Check to see if this notification is required. Subclasses should override
    * this method if needed. The method in this class always returns 
    * <code>true</code>. 
    * @param request the callers request context. 
    * @param roleInfo the workflow role info for this transition. 
    * @param tnc the transition notification context for the current transition.
    * @return <code>true</code> if the notification should proceed. 
    */
   protected boolean checkNotification(IPSRequestContext request, PSWorkflowRoleInfo roleInfo, 
          PSTransitionNotificationsContext tnc)
   {
      return true;   
   }
   
   /**
    * Sets the From address of the mail message. 
    * Subclasses may override this 
    * method to change the "From" address of notification messages dynamically.
    * Applications which need to "From" address for all notification should
    * modify <code>workflow.properties</code> instead of overriding this method.  
    * @param request the users request context. 
    * @param messageCtx the message context to be sent. 
    * @param userFrom the From address specified in the system configuration. 
    * @param communityId the community of the item being transitioned. This value 
    * is needed to filter email address lookups. 
    * 
    */
   protected void setFromAddress(IPSRequestContext request, IPSMailMessageWritable messageCtx, String userFrom, String communityId)
   {
      String userEmailAddress = lookupUserMailAddress(request, userFrom, communityId);
      if(userEmailAddress != null )
      {
         messageCtx.setFrom(userEmailAddress); 
      }
   }

   /**
    * Sets the "To" address for the mail message.  
    * Override this method to modify the "To" address. Note that this method
    * may be called multiple times for the From state assignees, To state 
    * assignees and the Additional Users.  
    * @param request the request context of the user who caused the transition. 
    * @param messageCtx the message context for the current message. 
    * @param usersTo a comma delimited list of additional users.  
    * @param communityId the community of the item. Used for filtering 
    * user roles by community. 
    */
   protected void setToAddress(IPSRequestContext request, IPSMailMessageWritable messageCtx, String usersTo, String communityId)
   {
      if(usersTo == null)
      {
         return;        
      }
      usersTo = usersTo.trim(); 
      if(usersTo.length() == 0)
      {
         return; 
      }
      List toList = PSWorkFlowUtils.tokenizeString(usersTo,PSWorkFlowUtils.EMAIL_STRING_DELIMITER);
      this.setToAddress(request, messageCtx, toList.iterator(), communityId); 
   }
   
   /**
    * Sets the "To" Address from a List of users.  
    * @see #setToAddress(IPSRequestContext, IPSMailMessageWritable, String, String)
    * @param request the users request context
    * @param messageCtx the current mail message
    * @param usersTo the list of users to add.
    * @param communityId the community of the item being transitioned. 
    */
   protected void setToAddress(IPSRequestContext request, IPSMailMessageWritable messageCtx, Iterator usersTo, String communityId)
   {
      messageCtx.addTo(usersTo); 
   }
   
   /**
    * Sets the CC list.  
    * Override this method to change the CC list. 
    * @param request the users request context
    * @param messageCtx the current message
    * @param users the delimited string of users 
    * @param communityId the community of the item being transitioned. 
    */
   protected void setCCAddress(IPSRequestContext request, IPSMailMessageWritable messageCtx, String users, String communityId) 
   {
      if(users == null)
      {
         return;        
      }
      users = users.trim(); 
      if(users.length() == 0)
      {
         return; 
      }
      List ccList = PSWorkFlowUtils.tokenizeString(users,PSWorkFlowUtils.EMAIL_STRING_DELIMITER); 
      this.setCCAddress(request,messageCtx, ccList.iterator(), communityId); 
   }
   
   protected void setCCAddress(IPSRequestContext request, IPSMailMessageWritable messageCtx, Iterator users, String communityId)
   {
      messageCtx.addCC(users); 
   }
   
   /**
    * Sets the URL included in the body of the message.
    * @param request the users request context
    * @param messageCtx the current message
    * @param messageUrl the URL to include in the message. 
    */
   protected void setMessageUrl(IPSRequestContext request, IPSMailMessageWritable messageCtx, String messageUrl)
   {
      messageCtx.setUrl(messageUrl);    
   }
   
   /**
    * Looks up the user email address
    * @param request the users request context
    * @param userName the user name to lookup
    * @param communityId the community used to filter the user. 
    * @return the email address of the specified user. 
    */
   protected String lookupUserMailAddress(IPSRequestContext request, String userName, String communityId)
   {
      List userEmailAddressList; 
      String userEmailAddress; 
      
      if ((null == userName) || userName.length() == 0)
      {
         throw new IllegalArgumentException(
            "User name may not be null or empty");
      }
      log.debug("Looking up address for user " + userName); 
      userEmailAddressList = PSWorkflowRoleInfo.getSubjectEmailAddresses(
         userName, request, communityId);
      if (!userEmailAddressList.isEmpty())
      {
         userEmailAddress = (String) userEmailAddressList.get(0);
         if ((null == userEmailAddress) || userEmailAddress.length() == 0)
         {
            userEmailAddress = userName;
         }
      }
      else
      {
         userEmailAddress = userName;
      }
      
      log.debug("Using email address " + userEmailAddress); 
      return userEmailAddress; 
   }
 
   /**
    * Gets the list of assigned users for a given state.
    * @param request the callers request context.
    * @param wfRoleInfo the workflow role info for this transition.
    * @param stateAdhocContext the adhoc assignee context
    * @param workflowID the WorkFlow ID for this item.
    * @param connection the database backend connection.
    * @param stateID the 
    * @param communityId
    * @return the list of users for the specified state
    * @throws SQLException
    */
   protected List getStateUserList(IPSRequestContext request,
      PSWorkflowRoleInfo wfRoleInfo, PSContentAdhocUsersContext stateAdhocContext, 
      int workflowID, Connection connection,
      int stateID, String communityId) 
      throws SQLException
   {
      PSStateRolesContext stateRoleContext = null;
      List stateUserList = new ArrayList();
      log.debug("Finding notify users for state " + stateID + 
            " in workflow " + workflowID); 
      try
      {
         stateRoleContext = new PSStateRolesContext(workflowID, connection,
               stateID, PSWorkFlowUtils.ASSIGNMENT_TYPE_ASSIGNEE);
         List stateRoleNotificationList = PSWorkflowRoleInfo
               .getStateRoleNameNotificationList(stateRoleContext);
         if (null != stateRoleNotificationList
               && !stateRoleNotificationList.isEmpty())
         {
            stateRoleNotificationList = PSWorkflowRoleInfo
                  .getRolesEmailAddresses(stateRoleNotificationList,
                        request, communityId);
            if (null != stateRoleNotificationList
                  && !stateRoleNotificationList.isEmpty())
            {
               stateUserList.addAll(stateRoleNotificationList);
            }
         }
         log.debug("state role notification list is " + stateRoleNotificationList);
         log.debug("state user list is " + stateUserList); 
         List fromStateAdhocActorNotificationList = PSWorkflowRoleInfo
               .getStateAdhocActorNotificationList(stateAdhocContext,
                     stateRoleContext, request);
         if (null != fromStateAdhocActorNotificationList
               && !fromStateAdhocActorNotificationList.isEmpty())
         {
            fromStateAdhocActorNotificationList = PSWorkflowRoleInfo
                  .getSubjectsEmailAddresses(
                        fromStateAdhocActorNotificationList, request,
                        communityId);
            if (null != fromStateAdhocActorNotificationList
                  && !fromStateAdhocActorNotificationList.isEmpty())
            {
               stateUserList.addAll(fromStateAdhocActorNotificationList);
            }
         }
         log.debug("state user list is " + stateUserList);
      } catch (PSEntryNotFoundException e)
      {
         log.debug("Entry Not found ", e); 
      } catch (PSRoleException e)
      {
         log.debug("Role Exception", e); 
      }
      if (stateUserList.isEmpty())
      {
         log.info("  No state role recipients for state " + stateID
               + " in the workflow " + workflowID + ".");
      }
      return stateUserList;
   }

   /**
    * Returns a map containing the token name (<code>String</code>) as key
    * and token value (<code>String</code>) as value. This map can be used
    * to substitute the token name with the token value in a string.
    * <p>
    * Override this method to define additional substitution values for the
    * token processor. 
    *
    * @param request request context for the exit, assumed not
    * <code>null</code>, used to obtain the values for the tokens
    *
    * @return the map containing the token name and values. This map contains
    * non-<code>null</code> value for all supported token names. The value
    * may be empty if this token does not the corresponding property defined in
    * "rxconfig/Workflow/rxworkflow.properties" file and a non-empty value
    * could not be obtained from the request context object. If a
    * non-<code>null</code> and non-empty value is obtained from the request
    * context then it is used as the token value, otherwise the value
    * configured in "rxconfig/Workflow/rxworkflow.properties" file is used
    * (which defaults to empty if no property containing the token name as
    * key is defined).
    */
   protected Map getTokenValues(IPSRequestContext request)
   {
      Map tokenValues = new HashMap(MAIL_TOKENS_DEFAULT_VALUE);
      for (int i = 0; i < MAIL_TOKENS.length; i++)
      {
         String tokenName = MAIL_TOKENS[i];
         if (tokenName.equals(WORKFLOW_COMMENT_TOKEN))
         {
            String transitionComment =
               PSWorkFlowUtils.getTransitionCommentFromHTMLParams(
                  request.getParameters());
            if ((transitionComment != null) &&
               (transitionComment.trim().length() > 0))
            {
               tokenValues.put(tokenName, transitionComment);
            }
         }
      }
      return tokenValues;
   }

   /**
    * Replaces the all the token names contained in the specified map with
    * the corresponding token value in the specified string.
    *
    * @param str the string in which to replace the token name with the
    * token value, may be <code>null</code> or empty, in which case no action
    * is performed.
    *
    * @param tokenValues map containing the token name (<code>String</code>)
    * as key and the token value (<code>String</code>) as value. Assumed
    * not <code>null</code>.
    *
    * @return the modified string with the token names subsituted with the
    * corresponding token value, may be <code>null</code> or empty if
    * <code>str</code> is <code>null</code> or empty.
    */
   protected static String parseTokens(String str, Map tokenValues)
   {
      if ((str != null) && (str.trim().length() > 0))
      {
         Iterator it = tokenValues.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry item = (Map.Entry)it.next();
            str = PSStringOperation.replace(str,
               (String)item.getKey(), (String)item.getValue());
         }
      }
      return str;
   }

   /**
    * Send mail using the specified message context.
    * The mail domain, SMTP host and optionally the mail
    * plugin to be used are specified by properties in the rxworkflow
    * properties file. If no custom mail plugin is specified, the Rhythmyx
    * javamail plugin will be used.
    *
 
    * @throws             PSMailException if the plugin class can not be
    *                     instantiated, or the plugin throws an exception
    */
   protected void sendMail(IPSRequestContext request, IPSMailMessageWritable messageContext)
      throws PSMailException
   {
      String mailDomain;
      String smtpHost;

      mailDomain =
            PSWorkFlowUtils.properties.getProperty("MAIL_DOMAIN", "");
      if (null == mailDomain)
      {
         throw new PSMailException(IPSExtensionErrors.MAIL_DOMAIN_NULL);
      }
      mailDomain  = mailDomain.trim();
      if (0 == mailDomain.length())
      {
         throw new PSMailException(IPSExtensionErrors.MAIL_DOMAIN_EMPTY);
      }

      messageContext.setMailDomain(mailDomain); 
      
      smtpHost =
            PSWorkFlowUtils.properties.getProperty("SMTP_HOST", "");
      if (null == smtpHost)
      {
         throw new PSMailException(IPSExtensionErrors.SMTP_HOST_NULL);
      }
      smtpHost = smtpHost.trim();
      if (0 == smtpHost.length())
      {
         throw new PSMailException(IPSExtensionErrors.SMTP_HOST_EMPTY);
      }

      messageContext.setSmtpHost(smtpHost); 

      /*
       * Get the registered plugin (java class name) for the custom mail
       * program from the properties file. The default is
       * 'com.percussion.workflow.mail.PSJavaxMailProgram'
       */
      String sClassName = PSWorkFlowUtils.properties.getProperty(
         "CUSTOM_MAIL_CLASS",
         "com.percussion.workflow.mail.PSJavaxMailProgram");

      IPSMailProgram mailPlugin = null;

      /*
       * Load the mail plugin class and create the plugin object.
       */
      try
      {
         mailPlugin = (IPSMailProgram)Class
               .forName(sClassName).newInstance();
      }
      catch(Exception e)
      {
         log.error("Exception loading mail plugin ", e);
         throw new PSMailException(e); 
      }

      try
      {
         mailPlugin.init();
         mailPlugin.sendMessage(messageContext);
         mailPlugin.terminate();
      }
      catch (PSNotificationSkipException e)
      {
         log.debug("Notification skipped."); 
         throw (PSNotificationSkipException)e.fillInStackTrace(); 
      }
      catch (PSMailException e)
      {
         log.error("Exception sending mail ", e);
         throw (PSMailException)e.fillInStackTrace();       
      }
      catch (Exception e2)
      {
         log.error("Exception sending mail ", e2);
         throw new PSMailException(e2);                
      }
   }

   /**************  IPSExtension Interface Implementation ************* */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Constant for the token name which is replaced by the user's comment for
    * the current transition. The value of this constant is "$wfcomment". The
    * runtime value of this token is the transition comment entered by the
    * user.
    */
   private static final String WORKFLOW_COMMENT_TOKEN = "$wfcomment";

   /**
    * Tokens which can be used in the notification mail subject or body.
    * Currently only one token (<code>WORKFLOW_COMMENT_TOKEN</code>) is
    * supported.
    */
   private static final String[] MAIL_TOKENS = {WORKFLOW_COMMENT_TOKEN};

   /**
    * Map containing the token name (<code>String</code>) as key and the
    * default value for the token (<code>String</code>) as value. Default value
    * for the token can be specified in the
    * "rxconfig/Workflow/rxworkflow.properties" properties file in the format:
    * <p>
    * wfcomment=No comment available
    * <p>
    * If any token is missing from the properties file, its value defaults to
    * empty.
    * Default value for the token is used when the user does not enter any
    * value for the token.
    */
   private static final Map MAIL_TOKENS_DEFAULT_VALUE = new HashMap();

   static
   {
      for (int i = 0; i < MAIL_TOKENS.length; i++)
      {
         String propertyName = MAIL_TOKENS[i].substring(1);
         MAIL_TOKENS_DEFAULT_VALUE.put(MAIL_TOKENS[i],
            PSWorkFlowUtils.properties.getProperty(propertyName, ""));
      }
   }
}
