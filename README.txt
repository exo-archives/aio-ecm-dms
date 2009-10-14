Copyright (C) 2003-2009 eXo Platform SAS.

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

Enterprise Content Management(ECM) > Document Management System(DMS)
Version 2.5.2

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

Release Notes - exo-ecm-dms - Version dms-2.5.2

** Bug
    * [ECM-3151] - Cannot set value for auto created property(but is not protected)
    * [ECM-3430] - File Explorer does not render correctly
    * [ECM-3843] - MAC OS: Can not load icon for thumbnail view in right pane
    * [ECM-3925] - File explorer does not extend vertically
    * [ECM-3935] - Lose version history when Import node
    * [ECM-4012] - Correct misleading error messages in JCR Explorer import function
    * [ECM-4024] - Error in displaying File Explorer Edit Form with Vista and Mac skin
    * [ECM-4026] - Error in displaying form to add new drive with Vista skin
    * [ECM-4027] - Error in displaying 'Add Query' form with Vista and Mac skin
    * [ECM-4071] - lost all version when do something with node in check-in status
    * [ECM-4080] - Import of a file with its history does not work
    * [ECM-4084] - Improved display in the left side of the File Explorer
    * [ECM-4098] - [DMS] Cannot do advanced search (search by property, category, type..)
    * [ECM-4105] - Strange behaviour of Permission
    * [ECM-4109] - Actions on a folder disappear from Action bar after chose Collaboration tab on a document
    * [ECM-4112] - [DMS] Alert message appears below Upload file pop up
    * [ECM-4113] - [DMS] Unknown error when create new ECM/BC templates without name
    * [ECM-4128] - Left panel display issue
    * [ECM-4129] - Default value in selectbox does not work in form dialog metadata
    * [ECM-4135] - [DMS][file explore][admin tab] it can add properties for node when 'Name' field of properties is blank
    * [ECM-4145] - Throw wrong exception in UIDocumentForm
    * [ECM-4147] - http error 404 when retrieving WebDav icons
    * [ECM-4148] - Select category form is not shown for normal user when add new document
    * [ECM-4149] - IE7: Error in displaying document after deleted the comment
    * [ECM-4150] - Change message when delete permission of taxonomy tree
    * [ECM-4155] - Error when view multi-languages document from search result
    * [ECM-4163] - Category is not deleted in special case
    * [ECM-4167] - Can not create document in the drive of new repository
    * [ECM-4190] - UI Bug in tabs when using advanced search/view content in French (in fact depend fof the width of the tabs)
    * [ECM-4193] - OutOfMemoryError when exporting content
    * [ECM-4199] - Content Browser the published node after exprot/import
    * [ECM-4202] - Unknown error when add action for taxonomy in special case
    * [ECM-4213] - Resize Comment pop-up in File Explorer
    * [ECM-4216] - Display document/uploaded file which added category  in form to add category in special case
    * [ECM-4217] - Do not show content of document after configuring for CB using document
    * [ECM-4235] - RGR:  Modification of the behavior of the ECM addSelectBoxField between 2.5.0 and 2.5.1
    * [ECM-4240] - Unknown error when view deleted document in dms-system workspace in Content Browser
    * [ECM-4249] - Error when configuring for FE using Parameterize type without select drive
    * [ECM-4275] - Bad HTML in article summary or body makes site explorer look and behave incorrect
    * [ECM-4276] - Size optimization and Unnecessary blank zone on JCR 
    * [ECM-4280] - Permission of workspace is changed to 'any' while viewing
    * [ECM-4282] - Content of releated document is not shown in Content Browser
    * [ECM-4289] - File Explorer Tree View (left pane) does not "link" the various level when the folder name is long (2 lines or more0)
    * [ECM-4291] - Unknown error when delete attachment of Article or Sample Node document
    * [ECM-4296] - Static and Direct Publication plugin cannot be used by 'standard' user until administrator has used it
    * [ECM-4297] - FolksonomyServiceImpl may let open jcr session for ever
    * [ECM-4332] - Auto init category when add/upload file into document which add category 
    * [ECM-4334] - Category is disappeared after edit document (only Article)
    * [ECM-4337] - Unknown error when do action with node which added category in specal case
    * [ECM-4345] - Unknown error when create new folder has space character at the end
    * [ECM-4357] - RepositoryException at first opening of ECMAdmin when there isn't a repository called "repository"
    * [ECM-4359] - 3 scrollbars when using UIListView dsplaying many documents
    * [ECM-4360] - Editing node in UIListView changes page back to first one
    * [ECM-4382] - Exception when add language for uploaded file
    * [ECM-4391] - Can't find Bonita class PayRaiseUserNameHook on JBoss
    * [ECM-4401] - Portlet ContentBrowser does not update query on front
    * [ECM-4435] - Concurrent access: simultaneous creation of several documents with the same name
    * [ECM-4443] - WCM publication cause data loss when restoring previous version of content
    * [ECM-4451] - Problem with empty paths

** Improvement
    * [ECM-3669] - Set a default value to a multi-value field in a dialog form
    * [ECM-4206] - Should show label of input for validator popup message instead of name of input.
    * [ECM-4278] - No autoscroll on the scrollbar in the frame "tree view" of FileExplorer where i drap a node (drap&drop)
    * [ECM-4293] - The lock token must be stored in a dedicated ExoCache to be able to replicate them over a cluster
    * [ECM-4294] - The LockManagerListener should be an Exo Listener instead of being an HttpSessionListener
    * [ECM-4324] - File Explorer toolbar contains lot of spaces between button (especially in FR language)
    * [ECM-4398] - Change error message "Invalid characters found in the file name"

** Task
    * [ECM-4218] - Make DMS 2.5.x and 2.6.x rely on jcr 1.10.5
    * [ECM-4219] - Search and fix all the potential memory leaks in the cms services
    * [ECM-4239] - Change confirm message when delete permission of taxonomy tree
    * [ECM-4339] - The init parameter "ldap.userDN.key" is missing in the file activedirectory-configuration.xml
    * [ECM-4378] - Release DMS 2.5.2
    * [ECM-4447] - Upgrade to Portal 2.5.6

** Sub-task
    * [ECM-3898] - Lock operation in FileExplorer - fails

** Other resources and links
	Company site        http://www.exoplatform.com
	Community JIRA      http://jira.exoplatform.org
	Comminity site      http://www.exoplatform.org
	Developers wiki     http://wiki.exoplatform.org
	Documentation       http://docs.exoplatform.org 


4. MIGRATION GUIDE
---------------------------------------------------
Migrate from DMS 2.2 to DMS 2.5
Since DMS 2.5 we have some main changes and need to be migrated

4.1 Taxonomy migration from 2.2 to 2.5

You can refer to this link to see more details: http://wiki.exoplatform.com/xwiki/bin/view/ECM/Taxonomy+Migration+from+ECM+2-2-x+to+DMS+2-5

DMS can be reached at:

   Web site:http://www.exoplatform.com
						http://www.exoplatform.vn
   E-mail:exoplatform@ow2.org
					exo-ecm@ow2.org
					exo-dms@exoplatform.com
		

