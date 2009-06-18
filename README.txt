Enterprise Content Management(ECM) > Document Management System(DMS)
Version 2.3.2

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
	ECM (Web Content Management): helps in maintaining, controlling, changing and reassembling the content on a web-page. It also 
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

** Bug
    * [ECM-2329] - Unable to add an action
    * [ECM-2469] - IE6 - Combo-box appears on top of all others components
    * [ECM-2568] - Can't automatically unlock node after locker sign out
    * [ECM-2753] - Upload icon not visible on Firefox 3 (Ubuntu)
    * [ECM-3011] - Change messages  in Content Browser in Vietnamese language
    * [ECM-3029] - Change message in Fast Content Creator in Vietnamese language
    * [ECM-3044] - Show duplicate information of user when user has 2 membership type in this group
    * [ECM-3064] - exception when trying to create a portal with a new group
    * [ECM-3091] - Error when displaying an image in exo:article when user not loggued
    * [ECM-3092] - not target link on webdav view button
    * [ECM-3096] - Throw exception when view content of File Plan in 'Version Infor' pop-up after manage publication
    * [ECM-3221] - Bad error message displayed when we have not enough rights to add a folder
    * [ECM-3222] - Bad error message displayed when we have not enough rights to add an article and the article form is not displayed properly
    * [ECM-3225] - Read Action are not working as expected : no right cick menu entry 
    * [ECM-3310] - Display bug on the File Explorer when we choose the "Dutch" language
    * [ECM-3401] - the method UIDialogForm.executeScript contains an incoherent test
    * [ECM-3428] - Allocate more space when the name of a column is too long
    * [ECM-3432] - Error in ContentBrowser when target folder is not readable by current user
    * [ECM-3474] - Cannot sort a column in the detailed view
    * [ECM-3478] - The attached file is deleted from the document with type Podcast in edit mode
    * [ECM-3486] - Constant warning in console regarding bonita when navgating the JCR using DAV
    * [ECM-3492] - File explorer has a view bug for the first entry in list view
    * [ECM-3499] - new content or modified content don't show until we restart the AS or disconnect
    * [ECM-3507] - Cannot use Dashboard portlet in DMS Trunk (so cannot use the new DMS gadgets
    * [ECM-3510] - Problem after importing document with special permissions
    * [ECM-3512] - Cannot edit Documents by "Collaboration Action" which use "UIOneNodePathSelector"
    * [ECM-3567] - UI issue in JCR FE detailed view, difference between english and french view
    * [ECM-3580] - Missing icons in the Intranet's file explorer on Mac and Vista style skins unlike default style one
    * [ECM-3582] - Bad displaying of the list of sub nodes on FileExplorer
    * [ECM-3598] - Hard coded labels in webapp/groovy/webui/component/explorer/UIConfirmMessage.gtmpl
    * [ECM-3599] - Resizing of the container according to the contents 
    * [ECM-3679] - Can not create query in File Explorer
    * [ECM-3681] - The test to know if the current node is the root node is incorrect
    * [ECM-3682] - Invisible content after an import-export
    * [ECM-3718] - Right-click menu is not shown when right click on node in new added repository
    * [ECM-3719] - 'msg' message when try to move node by drag & drop
    * [ECM-3720] - 'msg' message when try to delete sub node of a being locked node by user who is not locker
    * [ECM-3724] - Possible problem with categories when upload document
    * [ECM-3731] - Impossible to choose value in list for searching by property in Advanced search
    * [ECM-3852] - Typo Issue in  org.exoplatform.ecm.webui.component.admin.metadata.UIMetadataForm: The repository is retrieved instead of the  
                   workspace 
    * [ECM-3876] - Even if the renaming process fails, the relations are removed

** Improvement
    * [ECM-3431] - File Explorer Drag and Drop too delicate
    * [ECM-3433] - Broadcast some event when create/edit a document by CMSService
    * [ECM-3577] - Reduce the effect of drag and drop in file explorer
    * [ECM-3581] - use the upload file size limit in the upload form
    * [ECM-3619] - Use of appropriate tests
    * [ECM-3671] - Allow to translate node whose type extends nt:folder or nt:unstructured
    * [ECM-3734] - Allow to export/import a versionnable node
    * [ECM-3746] - The workspace creator must be up to date

** New Feature
    * [ECM-2547] - create an opensocial gadget that list the last documents edited by your contacts

** Task
    * [ECM-3411] - Add a logger and use the logger to print all the errors caught in the class org.exoplatform.ecm.webui.form.UIDialogForm
    * [ECM-3585] - Use the parent POM 1.1.1 for dms 2.3.x and 2.4.x
    * [ECM-3696] - Release DMS 2.3.2
    * [ECM-3850] - Integrate JCR 1.10.4 in DMS 2.3.2
    * [ECM-3889] - Integrate with Workflow 1.0.2

** Other resources and links
	Company site        http://www.exoplatform.com
	Community JIRA      http://jira.exoplatform.org
	Comminity site      http://www.exoplatform.org
	Developers wiki     http://wiki.exoplatform.org
	Documentation       http://docs.exoplatform.org 


4. MIGRATION GUIDE
---------------------------------------------------
Check compatibility between ECM 2.2 and ECM 2.3
Since ECM 2.3, we split ECM to 3 products as DMS, Workflow and WCM. So, how can we upgrade from 2.2 to 2.3? 
You can follow these steps to get successful upgrade. 

4.1. Migration for drives path
	Since ECM 2.3, the DriveMigrationService was available at the location ecm/dms/trunk/component/migration/2.3/drives. 
This service is used to rename the old drive which contains invalid characters and prevent the WARNING messages at the console.

4.2. How to use for Workflow Publication Plugin?
	Since ECM 2.3, the Workflow Publication Plugin was availabled at the location 
ecm/contentvalidation/trunk/component/workflowPublication/. This plugin is used to publish a content of documents. 
It breaks a work process down into tasks. 
See more details about http://wiki.exoplatform.com/xwiki/bin/view/ECM/Migrate+to+ECM2_3

4.3 Set up size of uploading file
	When we upload file in File Explorer, we need limit size of file. Maximum size of file is set up in portlet-preferences.xml at the location dms/core/web/portal/.../WEB-INF/conf/portal/group/platform/users. In this file, we set 30M by default for file size

 <portlet-preferences>
 ...
  <preference>
   <name>uploadFileSizeLimitMB</name>     
   <value>30</value>        
   <read-only>false</read-only>
  </preference>
 ...
 </portlet-preferences>

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