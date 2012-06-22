/*
 * com.percussion.pso.workflow PSONotifyExtended.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.workflow;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.fastforward.utils.PSRelationshipHelper;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.workflow.PSExitNotifyBase;
import com.percussion.workflow.mail.IPSMailMessageWritable; 

/**
 * New functionality on <b>PSONotifyExtended</b>
 * <br><br>
 * * The following macros can be used in both the notification's subject and body:
 * <br>
 * <ul>
 * <li><b>$sysTitle</b> - Replaced with system field "sys_title" value</li>
 * <li><b>$lastModifier</b> - Replaced with user name who last modified the content item</li>
 * <li><b>$lastModifyDate</b> - Replaced with content item's last modification date, using the format specified in <b>NOTIFICATION_DATE_FORMAT</b> on <b>rxworkflow.properties</b> file. See below for details.</li>
 * </ul>
 * &nbsp;&nbsp;&nbsp;When including any of the above macros inside either the notification's subject or body, they will be replaced by the corresponding value.
 * <br><br>
 * * The macro <b>$creator</b> will be replaced by the user's email who created the content item that fires the notification's sending. 
 * <br><b>IMPORTANT:</b> This macro can be used in both "Additional Recipient List" and "CC List" fields. 
 * <br>Including this macro inside any other field will result in displaying the macro as is (it won't be replaced).
 * <br><br>
 * * The date format used for macro <b>$lastModifyDate</b> is controlled from the key <b>NOTIFICATION_DATE_FORMAT</b> in the file <b>rxworkflow.properties</b>. 
 * <br>
 * <ul>
 * <li>The key is not included by default, so it's required to insert it as a new line in the <b>rxworkflow.properties</b> file. </li>
 * <li>Valid values for this key are the same as valid format date strings values for SimpleDateFormat Java class. </li>
 * <li>If this key is not included, a default date format will be used. </li>
 * <li>If an invalid date format is specified, an exception will be thrown. </li>
 * </ul>
 */
public class PSONotifyExtended extends PSExitNotifyBase
{
   private static Log log = LogFactory.getLog(PSONotifyExtended.class);
   /**
    * 
    */
   public PSONotifyExtended()
   {
      super();
   }
   
   
   /**
    * @see com.percussion.workflow.PSExitNotifyBase#setToAddress(com.percussion.server.IPSRequestContext, com.percussion.workflow.mail.IPSMailMessageWritable, java.util.Iterator, java.lang.String)
    */
   @SuppressWarnings("unchecked")
   protected void setToAddress(IPSRequestContext request, IPSMailMessageWritable messageCtx, Iterator usersTo, String communityId)
   {
      AddressCreatorIterator acitr = new AddressCreatorIterator(request,usersTo,communityId); 
      super.setToAddress(request, messageCtx, acitr, communityId);
   }



   @SuppressWarnings("unchecked")
   protected void setCCAddress(IPSRequestContext request, IPSMailMessageWritable messageCtx, Iterator usersTo, String communityId) 
   {
	AddressCreatorIterator acitr = new AddressCreatorIterator(request,usersTo,communityId); 
	super.setCCAddress(request, messageCtx, acitr, communityId);
   }

   
   /**
    * @see com.percussion.workflow.PSExitNotifyBase#addExtraTokens(com.percussion.server.IPSRequestContext,
    *      int)
    */
   @Override
   protected Map<String, String> addExtraTokens(IPSRequestContext request,
         int contentId)
   {
      Map<String, String> extras = new HashMap<String, String>();
      PSComponentSummary summary = PSONotifyExtended
            .getComponentSummary(request);
      if (summary != null)
      {
         extras.put("$sysTitle", summary.getName());
         extras.put("$lastModifier", summary.getContentLastModifier());
         extras.put("$lastModifyDate", formatDate(summary
               .getContentLastModifiedDate()));
      }
      return extras;
   }



   private String formatDate(Date date)
   {
      SimpleDateFormat sdf = getDateFormat(); 
      
      return sdf.format(date); 
   }
   
   protected static PSComponentSummary getComponentSummary(IPSRequestContext req) 
   {
      PSComponentSummary summary = null; 
      PSRelationshipHelper helper = new PSRelationshipHelper(req); 
      String contentId = req.getParameter(IPSHtmlParameters.SYS_CONTENTID); 
      if(contentId == null || contentId.length() == 0)
      {
         log.debug("no content id found, skipped loading item summary");
         return null; 
      }
      PSLocator loc = new PSLocator(contentId); 
      try
      {
         summary = helper.getComponentSummary(loc);
      } catch (Exception e) 
      {
         log.warn("Error loading component summary for notification", e);
         return null; 
      }
      return summary; 
   }
   
   @SuppressWarnings("unchecked")
   public class AddressCreatorIterator implements Iterator
   {
      private IPSRequestContext req = null;
      private PSComponentSummary summary = null; 
      private Iterator source; 
      private String communityId; 
      
      public AddressCreatorIterator(IPSRequestContext request, Iterator source, String communityId)
      {
         this.req = request;
         this.source = source;
         this.communityId = communityId; 
      }

      /**
       * @see java.util.Iterator#hasNext()
       */
      public boolean hasNext()
      {
         return source.hasNext(); 
      }

      /**
       * @see java.util.Iterator#next()
       */
      public Object next()
      {
         Object nxt = source.next(); 
         if(CREATOR.equalsIgnoreCase((String)nxt))
         {
            log.debug("found $creator token"); 
            if(summary == null)
            {
               summary = getComponentSummary(this.req); 
            }
            String user = summary.getContentCreatedBy(); 
            if(user != null && user.trim().length() > 0)
            {
               log.debug("creator user is " + user); 
               String email = lookupUserMailAddress(req, user, communityId);
               if(email != null && user.trim().length() > 0)
               {
                  nxt = email;
                  log.debug("creator email is " + nxt); 
               }
               else
               {
                  log.warn("No email for user " + user); 
               }
            }
            else
            {
               log.warn("Content Creator not found for id " 
                     + req.getParameter(IPSHtmlParameters.SYS_CONTENTID)); 
            }
            
         }
         return nxt; 
      }

      /**
       * @see java.util.Iterator#remove()
       */
      public void remove()
      {
         source.remove();          
      }
      
      private static final String CREATOR = "$creator"; 
   }
   
   
}
