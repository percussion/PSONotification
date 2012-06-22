/*
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.workflow PSTokenSubstitutionProcessor.java
 *
 */
package com.percussion.workflow;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.search.IPSExecutableSearch;
import com.percussion.search.IPSSearchResultRow;
import com.percussion.search.PSExecutableSearchFactory;
import com.percussion.search.PSSearchException;
import com.percussion.search.PSWSSearchResponse;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSDataTypeConverter;
import com.percussion.util.PSStringOperation;
import com.percussion.util.PSStringTemplate;
import com.percussion.util.PSStringTemplate.IPSTemplateDictionary;
import com.percussion.util.PSStringTemplate.PSStringTemplateException;

public class PSTokenSubstitutionProcessor implements IPSTokenSubstitutionProcessor
{
   private static Log log = LogFactory.getLog(PSTokenSubstitutionProcessor.class);
   
   private Map<String, String> tokenValues;
   
   private Set<String> fieldNames; 
   
   private PSItemDefManager itemDefManager = null; 
   
   private SimpleDateFormat dateFormat = null; 
   
   public PSTokenSubstitutionProcessor()
   {
      tokenValues = new HashMap<String, String>(
            MAIL_TOKENS_DEFAULT_VALUE);
      fieldNames = new HashSet<String>();
   }
   
   private void initServices()
   {
      if(itemDefManager == null)
      {
         itemDefManager = PSItemDefManager.getInstance();
      }
      if(dateFormat == null)
      {
         String formatStr = PSWorkFlowUtils.properties.getProperty(
               DATE_FORMAT_PROP);
         dateFormat = StringUtils.isBlank(formatStr) ? 
               new SimpleDateFormat() : new SimpleDateFormat(formatStr);
      }
      
   }
   /**
    * @see com.percussion.workflow.IPSTokenSubstitutionProcessor#processTokenValues(int, com.percussion.server.IPSRequestContext)
    */
   public void processTokenValues(int contentId, IPSRequestContext request) throws RepositoryException
   {  
      tokenValues = getFieldValues(contentId, request); 
      tokenValues.putAll(processTokenValues(request));
      String wfc = FIELD_TOKEN_START + WORKFLOW_COMMENT_PROP + FIELD_TOKEN_END;
      if(tokenValues.containsKey(wfc))
      {
         String transitionComment =
            PSWorkFlowUtils.getTransitionCommentFromHTMLParams(
               request.getParameters());
         if(StringUtils.isNotBlank(transitionComment))
         {
            tokenValues.put(wfc, transitionComment); 
         }
      }
      
   }
   
   protected Map<String,String> getFieldValues(int contentId, IPSRequestContext request) 
      throws RepositoryException
   {
      initServices();
      List<Integer> ids = new ArrayList<Integer>();
      ids.add(contentId);
     
      IPSExecutableSearch search = 
         PSExecutableSearchFactory.createExecutableSearch(request, fieldNames, ids);
      
      PSItemDefinition itemDef;
      try
      {
         itemDef = itemDefManager.getItemDef(new PSLocator(contentId), request.getSecurityToken());
      } catch (PSInvalidContentTypeException ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         throw new RuntimeException("Invalid content type for item " + contentId); 
      }
      return getFieldValues(contentId, search, itemDef); 
   }
   /**
    * Loads the specified item and obtains values for all supplied field names.
    * The {@link #WORKFLOW_COMMENT_PROP} field name is assigned the value of
    * {@link #WORKFLOW_COMMENT_TOKEN}.
    * 
    * @param contentId The content id of the item.
    * 
    * @throws RepositoryException If an error occurs.
    */
   protected Map<String,String> getFieldValues(int contentId, IPSExecutableSearch search, PSItemDefinition itemDef) throws RepositoryException
   {
      tokenValues = new HashMap<String, String>(); 
      if (fieldNames.isEmpty())
         return tokenValues;
      
      IPSSearchResultRow row;
      try
      {
         PSWSSearchResponse result = search.executeSearch();
         List<IPSSearchResultRow> rows = result.getRowList();
         if (rows.isEmpty())
         {
            throw new RuntimeException("Failed to locate content item with id " 
               + contentId);            
         }
         
         row = rows.get(0);
      }
      catch (PSSearchException e)
      {
         // indicates a bug of some sort
         throw new RuntimeException("Error loading field values for content " +
            "item with id " + contentId + ": " + e.getLocalizedMessage(), e);
      }
      
      
      
      for (String fieldName : fieldNames)
      {
         String value = "";
         // replace newly formatted wfcomment token with old format         
         if (WORKFLOW_COMMENT_PROP.equals(fieldName))
         {
            value = WORKFLOW_COMMENT_TOKEN;
         }
         else
         {
            value = row.getColumnDisplayValue(fieldName);
            PSField field = itemDef.getFieldByName(fieldName);
            formatDateField(value, field); 
         }
         
         tokenValues.put(FIELD_TOKEN_START + fieldName + FIELD_TOKEN_END, value);
      }
      return tokenValues;
   }

   protected String formatDateField(String value, PSField field)
   {
      
      if(StringUtils.isNotBlank(value))
      {
         // handle date formatting
         if (field == null)
         {
            return value; //field not found, don't convert it. 
         }
         String dataType = field.getDataType(); 
         if (dataType.equals(PSField.DT_DATE) 
            || dataType.equals(PSField.DT_DATETIME) || 
            dataType.equals(PSField.DT_TIME))
         {
            // parse value back to a date object
            Date date = PSDataTypeConverter.parseStringToDate(value);
            if (date != null)
            {
               value = dateFormat.format(date);
            }
         }
      }      
      return value;
   }
   
   /**
    * @see com.percussion.workflow.IPSTokenSubstitutionProcessor#parseFields(java.lang.String)
    */
   public void parseFields(String text)
   {
      PSStringTemplate template = getFieldTemplate(text);
      try
      {
         template.expand(new IPSTemplateDictionary() {

            public String lookup(String key)
            {
               if (!StringUtils.isBlank(key))
                  fieldNames.add(key);
               return "";
            }});
      }
      catch (PSStringTemplateException e)
      {
         // won't happen, ignore
      }
   }

   /**
    * Creates a template expander for field tokens, set to ignore unmatched
    * tokens.
    * 
    * @param text The text to parse, assumed not <code>null</code>, may be
    * empty
    * 
    * @return The field template, never <code>null</code>.
    */
   private PSStringTemplate getFieldTemplate(String text)
   {
      PSStringTemplate template = new PSStringTemplate(text, FIELD_TOKEN_START, 
         FIELD_TOKEN_END);
      template.setIgnoreUnmatchedSequence(true);
      return template;
   }

   
   protected Map<String,String> processTokenValues(IPSRequestContext request)
   {
      Map<String, String> tokenValues = new HashMap<String, String>();
      
      for (int i = 0; i < MAIL_TOKENS.length; i++)
      {
         String tokenName = MAIL_TOKENS[i];
         if (tokenName.equals(WORKFLOW_COMMENT_TOKEN))
         {  
            String transitionComment =
               PSWorkFlowUtils.getTransitionCommentFromHTMLParams(
                  request.getParameters());
            if (StringUtils.isNotBlank(transitionComment))
            {
               tokenValues.put(tokenName, transitionComment);
            }
         }
      }
      return tokenValues;
   }

   public void addExtraTokens(Map<String,String> extras)
   {
      tokenValues.putAll(extras);
   }
   /**
    * @see com.percussion.workflow.IPSTokenSubstitutionProcessor#replaceTokens(java.lang.String)
    */
   public String replaceTokens(String str)
   {
      if ((str != null) && (str.trim().length() > 0))
      {
         for(Map.Entry<String,String>item : tokenValues.entrySet())
         {
            str = PSStringOperation.replace(str,
              item.getKey(), item.getValue());
         }
      }
      return str;
   }
   
   
   private static final Map<String,String> MAIL_TOKENS_DEFAULT_VALUE = new HashMap<String,String>();

   /**
    * Constant for the property name which is used to create the 
    * {@link #WORKFLOW_COMMENT_TOKEN}. The value of this constant is 
    * "wfcomment". 
    */
   private static final String WORKFLOW_COMMENT_PROP = "wfcomment";   

   /**
    * Constant for the token name which is replaced by the user's comment for
    * the current transition. The value of this constant is "$wfcomment". The
    * runtime value of this token is the transition comment entered by the
    * user.
    */
   private static final String WORKFLOW_COMMENT_TOKEN = "$" + 
      WORKFLOW_COMMENT_PROP;
   
   /**
    * Tokens which can be used in the notification mail subject or body.
    * Currently only one token (<code>WORKFLOW_COMMENT_TOKEN</code>) is
    * supported.
    */
   private static final String[] MAIL_TOKENS = {WORKFLOW_COMMENT_TOKEN};

   /**
    * The string used as the start of field tokens.
    */
   private static final String FIELD_TOKEN_START = "${";
   
   /**
    * The string used as the end of field tokens.
    */
   private static final String FIELD_TOKEN_END = "}";

   /**
    * Constant for the name of the workflow property specifying the format to 
    * use for dates in messages.
    */
   private static final String DATE_FORMAT_PROP = "NOTIFICATION_DATE_FORMAT";


   /**
    * @see com.percussion.workflow.IPSTokenSubstitutionProcessor#getTokenValues()
    */
   public Map<String, String> getTokenValues()
   {
      return tokenValues;
   }

  
   /**
    * @see com.percussion.workflow.IPSTokenSubstitutionProcessor#getFieldNames()
    */
   public Set<String> getFieldNames()
   {
      return fieldNames;
   }

   
   protected void setItemDefManager(PSItemDefManager itemDefManager)
   {
      this.itemDefManager = itemDefManager;
   }

   public void setDateFormat(SimpleDateFormat dateFormat)
   {
      this.dateFormat = dateFormat;
   }
   
   
}

