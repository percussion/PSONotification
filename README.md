PSONotification
===============

PreRequisites --  Rhythmyx 6.7 w/PSOToolkit.  

Installation Instructions

1) run Install.bat / Install.sh or ant -f deploy.xml 
2) Modify /Rhythmyx/rxconfig/Server/ContentEditors/ContentEditorSystemDef.xml
   find the reference to sys_wfSendNotifications and replace it with PSONotifyExtended
  	
If you have implemented a custom subclass, you will register it as a Post-exit
and place its name in ContentEditorSystemDef.xml as above. 

You may then edit your workflow notifications and add the extra tokens supported by this extension. 

You should not need to make any other changes, and your change will survive a server upgrade.

You can also change the CUSTOM_MAIL_CLASS in rxworkflow.properties to use either the PSOJndiMailProgram 
or the PSOSpringMailProgram.  These mail plugins work both with the standard notification and the 
extended notification exits in this package. 

The configuration of the PSOJndiMailProgram is done via the mail-service.xml in the JBoss deploy
directory. You will also need to modify the jboss-web.xml to include the resource reference.

The configuration of the PSOSpringMailProgram is done via the PSOMailService-beans.xml. 

These mail programs allow additional options to be specified (including user authentication) 
that our standard mail program does not support. 
  