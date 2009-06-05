Enterprise Content Management(ECM) > Document Management System(DMS)
Version 2.4rc1

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
Release Notes - exo-ecm-dms - Version dms-2.4-RC1

** Bug
    * [ECM-2329] - Unable to add an action
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
    * [ECM-3478] - The attached file is deleted from the document with type Podcast in edit mode
    * [ECM-3492] - File explorer has a view bug for the first entry in list view
    * [ECM-3499] - new content or modified content don't show until we restart the AS or disconnect
    * [ECM-3507] - Cannot use Dashboard portlet in DMS Trunk (so cannot use the new DMS gadgets
    * [ECM-3512] - Cannot edit Documents by "Collaboration Action" which use "UIOneNodePathSelector"
    * [ECM-3541] - Bugs display after refactoring
    * [ECM-3546] - Error with action on opening node
    * [ECM-3555] - Unknown error when add metadata while node/parent is in check in status
    * [ECM-3557] - Unknown error when click Previous View after viewing uploaded file of a document
    * [ECM-3561] - BC: Unknown error when search by category with space at the first/last in searching keyword 
    * [ECM-3562] - Error when back to previous node in special case
    * [ECM-3567] - UI issue in JCR FE detailed view, difference between english and french view
    * [ECM-3580] - Missing icons in the Intranet's file explorer on Mac and Vista style skins unlike default style one
    * [ECM-3582] - Bad displaying of the list of sub nodes on FileExplorer
    * [ECM-3598] - Hard coded labels in webapp/groovy/webui/component/explorer/UIConfirmMessage.gtmpl
    * [ECM-3599] - Resizing of the container according to the contents 
    * [ECM-3600] - The links are not properly displayed in the path selector
    * [ECM-3610] - Drag and drop node into itself -> node disappears
    * [ECM-3620] - Change resource bundle in Fast Content and Browser Content in Vietnamese language
    * [ECM-3673] - Exception when select News page in navigation bar
    * [ECM-3680] - Cannot find a document from a Tag
    * [ECM-3681] - The test to know if the current node is the root node is incorrect
    * [ECM-3682] - Invisible content after an import-export
    * [ECM-3684] - Error when access Content Browser or Ideas-> Read Ideas
    * [ECM-3685] - Can not add category for document while editing this doc
    * [ECM-3688] - Exception when manage task in Business Process Controller
    * [ECM-3724] - Possible problem with categories when upload document
    * [ECM-3741] - Unknown error when create or edit query in ECM Administration
    * [ECM-3745] - Must select category while creating document 
    * [ECM-3752] - Unknown error when select category of new taxonomy tree to add for document

** Doc
    * [ECM-3687] - Create the article "How to migrate a DMS 2.3 to DMS 2.4" in the wiki
		* [ECM-3760] - Update the wiki articles to describe the improvements with import/export


** Improvement
    * [ECM-2562] - In File Explorer: allow user to rezie column and rearrange them
    * [ECM-3313] - siteexplorer visual ergonomy (step 1)
    * [ECM-3314] - FileExplorer : buttons => plugin buttons
    * [ECM-3431] - File Explorer Drag and Drop too delicate
    * [ECM-3433] - Broadcast some event when create/edit a document by CMSService
    * [ECM-3454] - New Taxonomy Management
    * [ECM-3484] - Broadcast some event when copy/cut/clone a node in FE
    * [ECM-3577] - Reduce the effect of drag and drop in file explorer
    * [ECM-3581] - use the upload file size limit in the upload form
    * [ECM-3601] - The path selector should not allow to select a link by default to avoid unexpected bugs
    * [ECM-3611] - Auto create dms-system workspace when create new repository
    * [ECM-3619] - Use of appropriate tests
    * [ECM-3671] - Allow to translate node whose type extends nt:folder or nt:unstructured
    * [ECM-3734] - Allow to export/import a versionnable node
    * [ECM-3746] - The workspace creator must be up to date

** New Feature
    * [ECM-2547] - create an opensocial gadget that list the last documents edited by your contacts
    * [ECM-3316] - New taxonomy management : using symlinks

** Task
    * [ECM-3411] - Add a logger and use the logger to print all the errors caught in the class org.exoplatform.ecm.webui.form.UIDialogForm
    * [ECM-3427] - Add the new Upload Service which allows to limit the size of the uploaded files
    * [ECM-3563] - Edit the label in form to add permission when edit metadata
    * [ECM-3565] - Edit the label when create workspace
    * [ECM-3590] - Move all the content of system:/jcr:system/exo:ecm to dms-system:/exo:ecm
    * [ECM-3716] - Allow to disable the symlinks
    * [ECM-3717] - Ensure that the new TaxonomyService can be used instead of the old service everywhere in the code

** Sub-task
    * [ECM-3542] - Still displaying symlink node after target node is deleted
    * [ECM-3543] - Show wrong message and throw exception when create a document in node that user does not have "Add" right
    * [ECM-3553] - Show wrong message when create the same name node in Document Folder or in document
    * [ECM-3554] - Unknown error when rename for node with "New name" field is blank
    * [ECM-3566] - Change message when create new query with special character in 'Name' field
    * [ECM-3583] - Apply these changes in the trunk
    * [ECM-3586] - Describe this new feature in the guides
    * [ECM-3592] - Change the configuration files
    * [ECM-3593] - Test the ECM Administration
    * [ECM-3594] - Test the File Explorer
    * [ECM-3595] - Find a way to migrate 
    * [ECM-3603] - Add a new category selector for the dialog template and replace the old one in all the default templates (Podcast, Sample Node, Kofax, File and File Plan) by this new one
    * [ECM-3609] - Always show message and throw exception when select new repository


** Other resources and links
	Company site        http://www.exoplatform.com
	Community JIRA      http://jira.exoplatform.org
	Comminity site      http://www.exoplatform.org
	Developers wiki     http://wiki.exoplatform.org
	Documentation       http://docs.exoplatform.org 


4. MIGRATION GUIDE
---------------------------------------------------
Migrate from DMS 2.3 to DMS 2.4
Since DMS 2.4 we have some main changes and need to be migrated

4.1 New features with Symlink and Taxonomy Management
4.2 Using new workspace dms-system
4.3 Create default taxonomy tree to Manage Taxonomy
4.4 Filter all action in File Explorer, ECM Admin
4.5 Gadget for last edited documents
4.6 Set up size of uploading file

You can refer to this link to see more details: http://wiki.exoplatform.com/xwiki/bin/view/ECM/ECM+Migration+from+DMS+2-3+to+DMS+2-4

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