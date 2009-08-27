Enterprise Content Management(ECM) > Document Management System(DMS)
Version 2.5.1

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
Release Notes - exo-ecm-dms - Version  dms-2.5.1


** Bug
    * [ECM-3151] - Cannot set value for auto created property(but is not protected)
    * [ECM-3397] - If category is mandatory, there is not star in upload panel or sample node template
    * [ECM-3549] - MAC, FF3: Impossible to use 1 click to open node from left pane in File Explorer
    * [ECM-3579] - Integration with KS: Links at the top-right corner of the forum portlet are badly positionned.
    * [ECM-3685] - Can not add category for document while editing this doc
    * [ECM-3739] - Error in displaying Metadata form
    * [ECM-3756] - problem when search in File Explorer
    * [ECM-3875] - Error message text when display to inform that can not leave blank name field
    * [ECM-3878] - Export/Import with Replace/Remove Existing UUID behavior is not correct
    * [ECM-3879] - Unknown error when delete taxonomy
    * [ECM-3886] - Home Path in Add taxonomy tree form is not marked with * but is required except choosing dms-system workspace
    * [ECM-3900] - Some UI errors in French
    * [ECM-3912] - Cannot config taxonomy path and workspace by xml
    * [ECM-3914] - Error when create new workspace
    * [ECM-3920] - Errors popup appears when opening the private/public drives on webos on ie6,7 after fresh login
    * [ECM-3929] - Can not upload file to import at the second time in ECM Admin
    * [ECM-3931] - Last Edited Documents: The gadget displays some strange files
    * [ECM-3932] - The auto-refresh of the google gadgest Last Edited Documents and Recently Published Documents doesn't seem to work properly
    * [ECM-3934] - Exception when create new workspace or new repository
    * [ECM-3938] - IE7: Can not select all nodes using mouse
    * [ECM-3940] - Modifications on a createRSSFeedAction are not taken into account
    * [ECM-3941] - RGR: Basic Publication Workflow does not work on 2.4RC1
    * [ECM-3943] - Bad display with File explorer on IE6(LTR & RTL)
    * [ECM-3950] - Popup appears when add tag for node in drive without side bar
    * [ECM-4010] - Should check if document has language before get that property
    * [ECM-4015] -  [ECM][file explore] cant Import a node when imported node is being locked
    * [ECM-4016] - Make it possible to use the RSSServlet with repositories that are not called "repository"
    * [ECM-4017] - Can not get documents using script in Content Browser
    * [ECM-4021] - ArrayIndexOutOfBoundsException in File Explore portlet Edit Mode
    * [ECM-4025] - Error in displaying of 'Add Taxonomy Tree' form in Vista and Mac skin
    * [ECM-4028] - Restoring previous version of multilanguage article loose all languages except root one
    * [ECM-4030] - Issues in 2.5 Test Campaign
    * [ECM-4063] - Lost link icon in File explorer portlet
    * [ECM-4067] - "Copy URL to clipboard" doesn't seem to work on FF with Flash Player 10
    * [ECM-4082] - FileExplorer display is not stable
    * [ECM-4083] - Satic&Direct Publication Service + Content Browser Portlet does not work correctly
    * [ECM-4094] - can not create node in file type with vietnamese content
    * [ECM-4101] - Icons in Action bar of Site Explorer was drop down when resize the browser
    * [ECM-4106] - file explorer does not delete all selected documents
    * [ECM-4144] - Can not view content of document in CB
    * [ECM-4166] - Can not create folder with WebDAV
    * [ECM-4185] - RGR: DMS Interceptors do not receive the correct "Context"  the "path" is null in 2.5 (was ok in 2.3)
    * [ECM-4186] - Publication "Static and Direct" service change the permission of a node
    * [ECM-4187] - Can not view WebCotent in Content Browser portlet
    * [ECM-4194] - Error when view the document in the Content Browser with the imported node (without import/export version history)
    * [ECM-4214] - Potential "race conditions issue" at UIExtensionManagerImpl initialization
    * [ECM-4215] - File Explorer direct access (from URL) does not work properly

** Improvement
    * [ECM-3412] - Add checkbox fields and radio buttons
    * [ECM-3921] - Missing some input in UIDialogForm
    * [ECM-3924] - Rename (Read) actions to a more contextual name for exampe  (User Action/Contextual Action)
    * [ECM-3939] - replace illegal xml entities when creating a rss xml file
    * [ECM-4022] - Add methods in UIExtensionFilterType to have better visibility in the code of UIExtensionManagerImpl
    * [ECM-4072] - Allow annotation inheritance and Check the return type of the method that has been annotated with @UIExtensionFilters 
    * [ECM-4073] - Disable the upload size limit when using the "Import Node" action
    * [ECM-4146] - Broadcast events on storeNodeByUUID method.
    * [ECM-4182] - If we allow a node nt:unstructured can be added mix:i18n, we should allow add language for it.

** Task
    * [ECM-3880] - Change message "exists" when copy/cut & paste a taxonomy into a destination contains one with same name
    * [ECM-3881] - Little error with label in Taxonomy's Permission form 
    * [ECM-3882] - Little error in message content when cut/paste on the same taxonomy
    * [ECM-3884] - Change title for Add Action form when add new action on taxonomy
    * [ECM-3911] - Make a Stress Test of the main features of DMS
    * [ECM-3947] - Make DMS 2.5 relies on Portal 2.5.5
    
** Sub-task
    * [ECM-3846] - Error when compare 2 versions in html source 
    * [ECM-3858] - Can't rename child node while parent node is being locked not by locker
    * [ECM-3913] - Create a benchmark from your tests
    * [ECM-4031] - Edit workspace form is blank & exception in console when view form to edit ws of new added repo
    * [ECM-4032] - Can not delete new added workspace while editing repo
    * [ECM-4033] - Exception in console whenever choose a repository from list to view (no error in UI)
    * [ECM-4034] - Error after signout while selecting new added repo
    * [ECM-4035] - Error when signout after deleted the being selected new added repo
    * [ECM-4036] - Error when switch between repositories
    * [ECM-4038] - Error in showing node in Taxonomy tree
    * [ECM-4039] - Can not Delete child node while parent node is being locked by user is not locker
    * [ECM-4040] - Remove no needed text in View tab of Edit Template form
    * [ECM-4041] - [File explorer][admin] form version infor of File document is showed not good on IE7
    * [ECM-4042] - Always display FE in new added view as default instead of displaying in current selecting view
    * [ECM-4044] - [file explore][admin] show Error when unpublished a node
    * [ECM-4045] - IE 7: [ File explorer ] UI error when folder tree is so long
    * [ECM-4046] - can not copy/delete/cut/lock many nodes 
    * [ECM-4048] - Show content of message is empty when unlock a node in special case
    * [ECM-4050] - [file explorer] icon of node is displayed not well on IE7
    * [ECM-4052] - can not login on IE7 again in special case
    * [ECM-4053] - IE7[File explorer ] : at left pane, don't show menu when right click on folder icon of node 
    * [ECM-4054] - The record number of File plan is still "0" although added some documents into
    * [ECM-4055] - [file explorer][admin] show UNKNOWN ERROR in some special case in editting permission of node
    * [ECM-4056] - [file explore] [admin] Name of action added for node can be contained special chars
    * [ECM-4057] - Error when view documents using tag when user does not have permission to view in this doc
    * [ECM-4058] - [file explore][admin] The value of field 'name' and "value" of new properiest are null or empty
    * [ECM-4059] - Error when go to folder of node after do search but user does not have any right in this folder
    * [ECM-4060] - Unknown error when add new ECM/BC template with blank Name
    * [ECM-4061] - Can not set localize for uploaded file
    * [ECM-4064] - File explorer : FF: UI error when add comment for document
    * [ECM-4065] - Upload file: Automatic remove selected file when add item into List taxonomy
    * [ECM-4066] - Remove all categories added although don't select them in special case


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
			 exo-dms@exoplatform.com
						

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