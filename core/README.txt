Enterprise Content Management(ECM) > Document Management System(DMS)
Version 2.3

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
** In DMS-2.3-final, we focus to some main changes:
	- RTL for ECM Admin portlet
	- RTL for Worfklow controller portlet
	- RTL for Workflow administration portlet
	- RTL for File Explorer portlet
	- RTL for Browse Content portlet
	- RTL for Fast Content Creator portlet
	- Created REST service to get the publised documents
	- Allow set permission for each category
	- Allow use xml file with ressource bundle in Bpar
	- Some documents added on the wiki
	- Supported copy paste cross drives
 	- Support BC is the publication aware
 	- Some bugs fixed
 	- Support RTL when change language in document template
 	- Refactor the location of contentvalidation
 	- Add some documents in Hows to section
 	- Complete the translation for Arabic

** Bug
    	* [ECM-3329] - Make language selection work for RTL when choosing another language from a document
	* [ECM-2979] - Bug when edit property without name space
	* [ECM-3057] - Throw exception when select new repository
	* [ECM-3045] - Throw exception when view webDAV
	* [ECM-3101] - Upload Document: File Name/Location dissapears when assignin a category
	* [ECM-3081] - Exception when open form 'Information Auditing' with node is nt:file
	* [ECM-3214] - ECM actions aren't triggered when a document is added in the JCR via the FTP connector
	* [ECM-2686] - Search don't work with non-Latin item location
	* [ECM-2964] - Can edit persmission of owner in special case
	* [ECM-3100] - Unknown error when specifying a space at the end of a search content name
	* [ECM-3058] - In File Explorer, parameter categoryMandatoryWhenFileUpload in portlet preferences is not read if changed in UI
	* [ECM-3225] - Read Action are not working as expected : no right cick menu entry 
	* [ECM-3299] - Cannot use * as name of a child node while creating a new nodeType
	* [ECM-3151] - Cannot set value for auto created property(but is not protected)
	* [ECM-3306] - No label appears when the label has not been translated

** Improvement
	* [ECM-3300] - Automaticaly redirected to the 1st page after removing an element not located on the 1st page	
	* [ECM-3344] - Change the way content-validation is stored in DMS
	* [ECM-3346] - Finish the code to sort any column in JCR FE
	* [ECM-2981] - Show allow user view property of root drive node if the node is not real root node

** New Feature
	* [ECM-3149] - Create gadgets for delivering information on documents
	* [ECM-3297] - Document in the wiki how to define icons for node types
	* [ECM-3335] - Document in the wiki how to manage versions of a document
	* [ECM-3333] - Document in the wiki how to creat a drive
	* [ECM-3334] - Add a Fast Content Creator section in practical how-tos
	* [ECM-3332] - Document in the wiki the tags (aka folksonomy) feature
	* [ECM-3069] - Update the readme

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