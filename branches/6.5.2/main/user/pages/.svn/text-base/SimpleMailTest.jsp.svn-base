<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="javax.naming.Context"%>
<%@page import="javax.naming.InitialContext"%>
<%@page import="javax.naming.NamingException"%>
<%@page import="javax.mail.Session"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Simple Mail Test</title>
</head>
<body>
<p>used for testing configuration of email JNDI bindings. 
If all is working, this page will display. 
Otherwise, you will get an exception. </p> 
<%
try
{  
   
   
   //log.debug("binding mail context"); 
   Context initCtx = new InitialContext();
   //log.debug("listing initial context"); 
   //listContext(initCtx);
   Context envCtx = (Context)initCtx.lookup("java:comp/env");
   //log.debug("listing env context"); 
   //listContext(envCtx);
   
   Session mailSession = (Session)envCtx.lookup("mail/defaultMail");
   %>
   <p>mail session bound to  <%=mailSession.getClass().getName() %>  </p>
   <%
    } catch (NamingException ex)
{
    out.write("Unexpected Exception " + ex); 
}

%>

</body>
</html>