
PreRequisites --  Rhythmyx 5.7.  Fast Forward code must be installed. 

Installation Instructions

1) Install psonotification.jar in the /Rhythmyx/libExtras directory.
2a) If you have an existing RhythmyxServer.cp2 file, add psonotification to it.
2b) If you don't have a RhythmyxServer.cp2 file, use the one included in this package.
3) Register the Extensions using Extensions.xml and InstallExtensions.bat (or .sh)
4) Modify /Rhythmyx/rxconfig/Server/ContentEditors/ContentEditorSystemDef.xml
   find the reference to sys_wfSendNotifications and replace it with PSONotifyExtende
	
	
If you have implemented a custom subclass, you will register it as a Post-exit
and place its name in ContentEditorSystemDef.xml as above. 

You should not need to make any other changes, and your change will survive a server upgrade.  