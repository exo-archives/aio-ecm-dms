Enterprise Content Management(ECM) > Document Management System(DMS)
Version 2.5.3

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

Release Notes - exo-ecm-dms - Version dms-2.5.3

** Bug
    * [ECM-2021] - Error in display File Plan in special case
    * [ECM-2068] - Error with * for required fields in ECM
    * [ECM-2316] - Can not  select permission when edit dialog/view in special case
    * [ECM-2359] - Invalid error message when importing a nodetype without specifying its namespace
    * [ECM-2443] - BaseActionLauncherListener doesn't support anonymous access
    * [ECM-3073] - Can not delete drive
    * [ECM-3090] - Error in displaying 'Content' text-area of Cover-flow in 'Edit ECM Template' form
    * [ECM-3304] - The file size should not be too accurate
    * [ECM-3545] - Do not display Vietnamese language in the search result
    * [ECM-3615] - Error when trying to copy a forlder that contains two nodes with the same name (test, test[2])
    * [ECM-3689] - IE7: Error when close sidebar in File Explorer
    * [ECM-3733] - Error in using page iterator in Advanced search result
    * [ECM-3747] - Permission infor is not suitable with selected  version in edit template form
    * [ECM-3758] - some problems  when user without read right try to access document from tag cloud
    * [ECM-3788] - Error in displaying path in Content Browser after do search
    * [ECM-3789] - "exo data" appears in Document templates list when create new document after do search in CB
    * [ECM-3888] - Error in destination path in alert message when copy/cut & paste node
    * [ECM-4153] - Should not allow to view comment node from search result instead of show blank form
    * [ECM-4205] - localization and wording
    * [ECM-4241] - IE: Lose stylesheet when view documents in Content Browser 
    * [ECM-4248] - IE7: Error when select page in navigation bar with Vista and Mac skin
    * [ECM-4258] - Error when create new document have sam name in other categories
    * [ECM-4281] - Still display old name of document while viewing in Content Browser
    * [ECM-4335] - Error in showing breadcumb in Content Browser
    * [ECM-4338] - Error in selecting breadcrumb in Content Browser in specical case
    * [ECM-4354] - Blanks in drive names are not shown
    * [ECM-4355] - Show image replace content of File document when upload image into File 
    * [ECM-4362] - Clean up chaotic Add Taxonomy Tree dialog
    * [ECM-4363] - Incorrect English in Add Taxonomy Tree Dialog
    * [ECM-4364] - CRON actions are not working 
    * [ECM-4365] - DMS Action does not show the "Cron" information in edit mode of the action
    * [ECM-4379] - Can not do any action when select multi nodes in thumbnails view 
    * [ECM-4428] - DMS has Form Generator Portlet configuration
    * [ECM-4432] - Show both published and unpublished documents  while configuration for CB to show only published document (with script and query)
    * [ECM-4433] - Exception when edit default taxonomy tree in new repository
    * [ECM-4444] - Exception when select path for Personal drives while configuration for File Explorer
    * [ECM-4445] - All files are disappeared when delete blank form in "Upload" form
    * [ECM-4446] - Can not unlock multi nodes at the same time
    * [ECM-4456] - Little mistake in resouce bundle
    * [ECM-4457] - Unknown error when upload multi  file with 1 form is blank
    * [ECM-4461] - Can not add language for Sample node document in special case
    * [ECM-4483] - Problem with the import of the version history for a node type nt: file
    * [ECM-4504] - Can create taxonomy tree without select target path when add action for this tree
    * [ECM-4530] - Can not add new view
    * [ECM-4536] - StackOverflowError in org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer after removing site
    * [ECM-4537] - Exception when view metadata of node
    * [ECM-4539] - Exception when view File Plan after editing
    * [ECM-4541] - Exception when view new added language of uploaded file 
    * [ECM-4547] - Exception when add workflow lifecycle for node
    * [ECM-4548] - Exception when back to FE after approving task
    * [ECM-4558] - uicomponent.addCheckboxField doesn't change anything in the JCR
    * [ECM-4570] - JCR Explorer - selection interfeer with scrolling
    * [ECM-4573] - Need to click twice to remove a reference
    * [ECM-4577] - Need icon for document created by new added template
    * [ECM-4583] - Unknown error when click on Save button without select any file to upload
    * [ECM-4591] - Show content of published document after pressing F5  while chosing other node
    * [ECM-4593] - UI: blank at the bottom of the File Explorer
    * [ECM-4598] - Exception when anonymous create a content on public page
    * [ECM-4605] - Permission of new workspace of new repository is not shown
    * [ECM-4618] - Edit Drive generates a 'Workspace path invalid' error.
    * [ECM-4620] - Exception when delete comment of document while node is in check-in status
    * [ECM-4624] - Error in displaying page iterator
    * [ECM-4627] - "root" has full right with * but can not access drives that was assigned for other membership types
    * [ECM-4629] - FE: Can not jump to chosen path if hit enter after input the path in address bar
    * [ECM-4631] - Problem of Memory leak (OutOfMemoryError.) with DMS 
    * [ECM-4636] - "View document" func can not be done except select Collaboration Center drive
    * [ECM-4658] - Error when configuring FileExplorer
    * [ECM-4659] - Content Browser research
    * [ECM-4660] - Redirection to classic portal is hardcoded
    * [ECM-4685] - Find better French translation for DMS drives
    * [ECM-4689] - Cannot use AddTaxonomyActionScript when start server
    * [ECM-4712] - Exception when select new repository
    * [ECM-4714] - Show jcr:frozenNode in Content Browser when view published document
    * [ECM-4715] - Taxonomy import impossible if there are deleted taxonomies
    * [ECM-4720] - Error in org.exoplatform.services.cms.taxonomy.impl.TaxonomyPlugin
    * [ECM-4736] - Exception when create document in taxonomy in special case
    * [ECM-4738] - Cannot edit the node with sibling.
    * [ECM-4741] - Can not execute right click action of node in Icon View
    * [ECM-4759] - Workflow :Error when publishing an article in Admin Workflow Management
    * [ECM-4779] - Admin toolbar disapear when using File Explorer and ECM Admin
    * [ECM-4818] - Unknown error when add action(not exo:taxonomyAction) for taxonomy tree
    * [ECM-4825] - No Management  for exception  on service "org.exoplatform.services.deployment.ContentInitializerService"
    * [ECM-4826] - Build uses antrun plugin 1.2-SNAPSHOT

** Improvement
    * [ECM-3328] - Fix the performance issue in the PublicationGetDocumentRESTService
    * [ECM-4204] - localize words on a roll list
    * [ECM-4325] - Support execute action with field nodeTypes and isDeep
    * [ECM-4552] - Translate :JCR: "Are you sure want to move?" in french version
    * [ECM-4555] - Clarification of translation
    * [ECM-4655] - Allows create a category (exo:category) inside an existing one instead of creating nt:folders

** Task
    * [ECM-4703] - Release DMS 2.5.3
    * [ECM-4899] - Fix bugs for All In One 1.6.1
    * [ECM-4910] - Cleanup the build process to be able to deploy on eXo Nexus with the release plugin for 2.5.x

** Sub-task
    * [ECM-4637] - [DEV] Show message when cut node is being viewed
    * [ECM-4844] - [DEV] Exception when view File Plan after editing
    * [ECM-4911] - Build - Cleanup the profile with properties, remove the reporting and emma config, add parent pom v6
    * [ECM-4918] - Build - Integrate module.js in the project to be used by exopackage and maven-exobuild-plugin
    * [ECM-4932] - JBPM and Bonita configuration.xml files contain hardcoded version of workflow
    * [ECM-4933] - Use Kernel, Core, .... SNAPSHOTs



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

You can refer to this link to see more details: http://wiki.exoplatform.com/xwiki/bin/view/ECM/ECM+Migration+from+DMS+2-3+to+DMS+2-5

DMS can be reached at:

   Web site: http://www.exoplatform.com
			 http://www.exoplatform.vn
   	 E-mail: exoplatform@ow2.org
			 exo-ecm@ow2.org
			 exo-dms@exoplatform.com
						

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