Enterprise Content Management(ECM) > Document Management System(DMS)
Version 2.5

You may find it helpful to see the details at wiki place of ECM
http://wiki.exoplatform.org/xwiki/bin/view/ECM/

TABLE OF CONTENTS
---------------------------------------------------
1. What is eXo ECM
2. How to set up eXo ECM
3. Release notes
4. Migration guide


1. WHAT IS EXO ECM
---------------------------------------------------
	Enterprise Content Management is the strategies, methods and tools used to capture, manage, store, preserve and deliver 
contents and documents related to organizational processes with the purpose of improving operational productivity and efficiency. 
ECM tools and strategies allow the management of an organization's unstructured information, whether that information exists. 
The eXo Platform ECM portlet provides you with a portal solution that can help you achieve these processes easily and it is carefully 
designed so that you can leverage your business content across all formats for competitive gain. An environment for employees is also 
provided to share and collaborate digital contents as well as delivering a comprehensive unified solution with rich functionalities.
ECM consists of three parts: 
	DMS (Document Management System): used to store, manage and track electronic documents and electronic images. DMS allows 
documents to be modified and managed easily and conveniently by managing versions, properties, ect.
	Workflow: is the way of looking at and controlling the processes presented in an organization such as service provision or 
information processing, etc. It is an effective tool to use in times of crisis to make certain that the processes are efficient and 
effective with the purpose of better and more cost efficient organization.
	WCM (Web Content Management): helps in maintaining, controlling, changing and reassembling the content on a web-page. It also 
helps webmasters who handle all tasks needed to run a website, including development, deployment, design, content publication and 
monitoring.


2. HOW TO SET UP EXO ECM
---------------------------------------------------
eXo Enterprise Content Management requires the Java 2 Standard Edition Runtime Environment (JRE) or Java Development Kit version 5.x

2.1. Install Java SE 1.5 (Java Development Kit)
Based on JavaEE, our ECM runs currently fine with version 1.5 so if you are using newer version, please download and install this 
version to make ECM works fine. We will support newer version of Java very soon.

2.2. Download eXo Enterprise Content Management version from: http://forge.objectweb.org/projects/exoplatform/

2.3. Unzip that package under a path that does not contain any space (in Windows).

2.4. Open a shell session and go to the bin/ directory that has just been extracted.

2.5. Then run the command :
	Windows:
		eXo.bat run
	Linux, Unix, Mac OS
	chmod u+x *.sh ./eXo run

2.6. Open your web browsers, now eXo ECM can run on FireFox 2 or newer, Internet Explorer 6 or newer 
(we recommend using FireFox 3+ or Internet Explorer 7+ for the best result) 
and navigate to URL: http://localhost:8080/portal

2.7. When the page has been loaded, click "Login" on the top right corner. Specify the username "root" and the password "exo".

3. RELEASE NOTES 
---------------------------------------------------
Release Notes - exo-ecm-dms - Version dms-2.5

** Bug
    * [ECM-3486] - Constant warning in console regarding bonita when navgating the JCR using DAV
    * [ECM-3556] - Error in vote bar of Sample Node document
    * [ECM-3667] - Pre Save interceptor can't prevent save and show error message 
    * [ECM-3686] - Display symlink node in form to add category
    * [ECM-3690] - Always show message "The query is invalid. Please check your constraint" when do advanced search
    * [ECM-3691] - Can not access CB after renamed the Categories path
    * [ECM-3692] - Error in showing CB when select a node contain a symlink for the Categories path
    * [ECM-3693] - Exception when change workspace after selecting a node in Manage Relation & Add Sym link form
    * [ECM-3694] - Change title for UIRelationsAddedList tab in Manage Relation form
    * [ECM-3723] - Errow when select symlink node(folder) of other workspace in Content Browser
    * [ECM-3732] - Do not display the change of File document in 'Auditing Information' form
    * [ECM-3738] - Error in displaying 'Node Type Information' form
    * [ECM-3740] - Error in displaying the position of name in File Plan document 
    * [ECM-3744] - Error in displaying File Plan in Fast Content Creator
    * [ECM-3749] - Display symlink node (when node is not symlink) in Content Browser
    * [ECM-3750] - Exception when view content of document (using tag) in Content Browser in special case
    * [ECM-3757] - Unknown error when click on 'Manage Publication' icon in special case
    * [ECM-3842] - When we remove the target of a link, the link is no more displayed in the FE which creates side effects
    * [ECM-3878] - Export/Import with Replace/Remove Existing UUID behavior is not correct
    * [ECM-3890] - ContentTypeFilterPlugin doesn't read good value for repository param
    * [ECM-3891] - No one can access drive that select workspace with "any permission"
    * [ECM-3892] - New added repo disappears after re-run tomcat
    * [ECM-3893] - Errors when do actions on being locked node
    * [ECM-3894] - Drag'n'Drop in File Explorer doesn't work with accentuated characters
    * [ECM-3895] - Drag'n'Drop in File Explorer doesn't work well in IE7
    * [ECM-3903] - Impossible to see the detail of a content with arabic name (IE6 IE7)
    * [ECM-3905] - Probleme when editing a field metadata in dialog
    * [ECM-3906] - Re initializing field date when choosing an image in a dialog form
    * [ECM-3907] - Bad display of file explorer's adress bar in IE with arabic language
    * [ECM-3909] - Creating unnecessary folders for User
    * [ECM-3915] - Unknown error when delete a query in ECM Administraion
    * [ECM-3919] - Labels partly Arabized
    * [ECM-3926] - RGR:  Impossible to edit an existing action in the file explorer "Manage Actions" dialog.
    * [ECM-3942] - Error when display taxonomy
    * [ECM-3944] - NullPointerException in RSSServlet - should use a PortalContainer


** Doc
    * [ECM-3949] - Create an How to use the UI Extension Framework


** Improvement
    * [ECM-3473] - Add new logo to the product homepage
    * [ECM-3746] - The workspace creator must be up to date
    * [ECM-3930] - URLs are hard coded in the source code of the Google Gadgets LastEditedDocument and PublishedDocument
    * [ECM-3933] - The new FE ergonomy doesn't support RTL properly
    * [ECM-3945] - Useless logs


** Task
    * [ECM-3563] - Edit the label in form to add permission when edit metadata
    * [ECM-3565] - Edit the label when create workspace
    * [ECM-3762] - Create Unit Tests and Add javadoc to all public and protected methods of all implementations of all our services 
    * [ECM-3896] - Add new logo for DMS and workflow
    * [ECM-3923] - Navigation and Locale sanitization
    * [ECM-3946] - Add a "Take a tour" and a small description for all the main applications to the home page

** Other resources and links
	Company site        http://www.exoplatform.com
	Community JIRA      http://jira.exoplatform.org
	Comminity site      http://www.exoplatform.org
	Developers wiki     http://wiki.exoplatform.org
	Documentation       http://docs.exoplatform.org 


4. MIGRATION GUIDE
---------------------------------------------------
Migrate from DMS 2.3 to DMS 2.5
Since DMS 2.5 we have some main changes and need to be migrated

4.1 New features with Symlink and Taxonomy Management
4.2 Using new workspace dms-system
4.3 Create default taxonomy tree to Manage Taxonomy
4.4 Filter all action in File Explorer, ECM Admin
4.5 Gadget for last edited documents
4.6 Set up size of uploading file

You can refer to this link to see more details: http://wiki.exoplatform.com/xwiki/bin/view/ECM/ECM+Migration+from+DMS+2-3+to+DMS+2-5

DMS can be reached at:

   Web site: http://www.exoplatform.com
						 http://www.exoplatform.vn
   	 E-mail: exoplatform@ow2.org
						 exo-ecm@ow2.org
						

Copyright (C) 2003-2007 eXo Platform SAS.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Affero General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, see<http://www.gnu.org/licenses/>.