/*
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * test.percussion.workflow PSTokenSubstitutionProcessorTest.java
 *
 */
package test.percussion.workflow;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSField;
import com.percussion.search.IPSExecutableSearch;
import com.percussion.search.IPSSearchResultRow;
import com.percussion.search.PSWSSearchResponse;
import com.percussion.server.IPSRequestContext;
import com.percussion.workflow.PSTokenSubstitutionProcessor;

public class PSTokenSubstitutionProcessorTest
{
   Log log = LogFactory.getLog(PSTokenSubstitutionProcessorTest.class); 
   TestTokenSubstitutionProcessor cut;
   Mockery context; 
   PSItemDefManager defManager;
   
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery(){{ setImposteriser(ClassImposteriser.INSTANCE);}}; 
      cut = new TestTokenSubstitutionProcessor();
      defManager = context.mock(PSItemDefManager.class);
      cut.setItemDefManager(defManager); 
   }
   @Test
   public final void testGetFieldValues()
   {
      log.info("testing getFieldValues"); 
      
      final IPSExecutableSearch search = context.mock(IPSExecutableSearch.class); 
      final PSItemDefinition itemDef = context.mock(PSItemDefinition.class); 
      final PSWSSearchResponse response = context.mock(PSWSSearchResponse.class); 
      final IPSSearchResultRow row = context.mock(IPSSearchResultRow.class); 
      final List<IPSSearchResultRow> rowlist = new ArrayList<IPSSearchResultRow>(){{
         add(row);
         }};
      cut.parseFields("${foo} ${wfcomment}"); 
      
      try
      {
         context.checking(new Expectations(){{
            one(search).executeSearch();
            will(returnValue(response));
            one(response).getRowList();
            will(returnValue(rowlist)); 
            one(row).getColumnDisplayValue("foo");
            will(returnValue("bar")); 
            one(itemDef).getFieldByName("foo"); 
            will(returnValue(null)); 
         }});
         
         Map<String,String> values = cut.getFieldValues(42, search, itemDef);
         assertNotNull(values);
         log.info("values is " + values); 
         assertTrue(values.containsKey("${foo}")); 
         assertEquals("bar", values.get("${foo}")); 
         assertTrue(values.containsKey("${wfcomment}")); 
         assertEquals("$wfcomment", values.get("${wfcomment}"));
         
         String replaced = cut.replaceTokens("${foo}");
         assertNotNull(replaced); 
         log.info("replaced is " + replaced); 
         assertEquals("bar", replaced); 
         
         replaced = cut.replaceTokens("${wfcomment}"); 
         assertNotNull(replaced); 
         log.info("replaced is " + replaced); 
         assertEquals("$wfcomment", replaced); 
         
         context.assertIsSatisfied(); 
         
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception caught"); 
      }
      
   }
   
   @Test
   public final void testParseFields()
   {
      cut.parseFields("fee ${fi} fo ${fum}" );
      Set<String> fields = cut.getFieldNames(); 
      assertNotNull(fields); 
      assertEquals(fields.size(), 2); 
      assertTrue(fields.contains("fi")); 
   }
   
   
   @Test
   public final void testGetTokenValues()
   {
      final IPSRequestContext request = context.mock(IPSRequestContext.class);
      final Map<String,Object> pmap = new HashMap<String, Object>(){{
         put("commenttext", "baz");
      }};
      cut.parseFields("${wfcomment}"); 
      Set<String> fnames = cut.getFieldNames(); 
      assertNotNull(fnames);
      log.info("field names -" + fnames); 
      
      context.checking(new Expectations(){{
         one(request).getParameters();
         will(returnValue(pmap));
      }});
      
      Map<String, String> values = cut.processTokenValues(request); 
      assertNotNull(values);
      log.debug("values are " + values);
      assertEquals(1,values.size()); 
   }
   
   @Test 
   public final void testFormatDateField()
   {
      log.info("testing formatDateField"); 
      final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
      cut.setDateFormat(dateFormat); 
      final PSField field = context.mock(PSField.class);
      String value = "2001-09-11"; 
      context.checking(new Expectations(){{
         one(field).getDataType();
         will(returnValue("date")); 
      }});
      String result = cut.formatDateField(value, field); 
      assertNotNull(result);
      assertTrue(result.length() > 0); 
      log.info("result is " + result); 
      assertTrue(result.contains("September")); 
      context.assertIsSatisfied();       
   }
   
   @Test 
   public final void testFormatDateFieldText()
   {
      log.info("testing formatDateField as text"); 
      final PSField field = context.mock(PSField.class);
      String value = "foo"; 
      context.checking(new Expectations(){{
         one(field).getDataType();
         will(returnValue("text")); 
      }});
      String result = cut.formatDateField(value, field); 
      assertNotNull(result);
      assertTrue(result.length() > 0); 
      log.info("result is " + result); 
      assertEquals("foo", result); 
      context.assertIsSatisfied(); 
      
   }
   @Test 
   public final void testFormatDateFieldNull()
   {
      log.info("testing formatDateField null field"); 
      String value = "foobar"; 
      String result = cut.formatDateField(value, null); 
      assertNotNull(result);
      assertTrue(result.length() > 0); 
      log.info("result is " + result); 
      assertEquals("foobar", result); 
      
   }
   private class TestTokenSubstitutionProcessor extends PSTokenSubstitutionProcessor
   {
           
      @Override
      public Map<String, String> getFieldValues(int contentId,
            IPSExecutableSearch search, PSItemDefinition itemDef)
            throws RepositoryException
      {
         return super.getFieldValues(contentId, search, itemDef);
      }


      @Override
      public Map<String, String> processTokenValues(IPSRequestContext request)
      {
         return super.processTokenValues(request);
      }

      @Override
      public void setItemDefManager(PSItemDefManager itemDefManager)
      {
         super.setItemDefManager(itemDefManager);
      }

      @Override
      public  String formatDateField(String value, PSField field)
      {
         return super.formatDateField(value, field);
      }

      @Override
      public void setDateFormat(SimpleDateFormat dateFormat)
      {
         super.setDateFormat(dateFormat);
      }
      
   }
}
