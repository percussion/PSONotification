/*
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * test.percussion.pso.workflow.mail.spring PSOSpringMailServiceImplTest.java
 *
 */
package test.percussion.pso.workflow.mail.spring;

import static org.junit.Assert.*;

import java.util.ArrayList;
import javax.mail.internet.AddressException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mail.SimpleMailMessage;

import com.percussion.pso.workflow.mail.spring.PSOSpringMailServiceImpl;
import com.percussion.workflow.mail.IPSMailMessageContext;
import com.percussion.workflow.mail.PSMailMessageCtxWritable;

public class PSOSpringMailServiceImplTest
{
   private static Log log = LogFactory.getLog(PSOSpringMailServiceImplTest.class);
   
   TestPSOSpringMailServiceImpl cut; 
   
   @Before
   public void setUp() throws Exception
   {
      cut = new TestPSOSpringMailServiceImpl(); 
   }
   @Test
   public final void testBuildMessage()
   {
      PSMailMessageCtxWritable msg = new PSMailMessageCtxWritable();
      msg.setBody("foo"); 
      msg.setUrl("bar");
      msg.setMailDomain("mydomain");
      msg.setTo(new ArrayList<String>(){{add("joe");add("fred");add("sam@nowhere");}}); 
      msg.setCC(new ArrayList<String>(){{add("bob");}}); 
      msg.setFrom("nobody"); 
      try
      {
         SimpleMailMessage res = cut.buildMessage(msg);
         
         assertNotNull(res); 
         String text = res.getText(); 
         assertNotNull(text);
         assertTrue(text.contains("foo")); 
         assertTrue(text.contains("bar")); 
         
         String[] to = res.getTo();
         assertNotNull(to);
         assertEquals(3, to.length);
         assertEquals("joe@mydomain", to[0]); 
         assertEquals("fred@mydomain", to[1]);
         assertEquals("sam@nowhere", to[2]); 
         
         String[] cc = res.getCc(); 
         assertNotNull(cc); 
         assertEquals(1, cc.length); 
         assertEquals("bob@mydomain", cc[0]); 
         
         String from = res.getFrom();
         assertNotNull(from);
         assertEquals("nobody@mydomain", from); 
         
      } catch (AddressException ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception caught");
      }
      
      
      
   }
   
   private class TestPSOSpringMailServiceImpl extends PSOSpringMailServiceImpl
   {

      @Override
      public SimpleMailMessage buildMessage(IPSMailMessageContext message)
            throws AddressException
      {
         return super.buildMessage(message);
      }
      
   }
}
