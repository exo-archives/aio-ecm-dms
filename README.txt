Enterprise Content Management(ECM) > Document Management System(DMS)
Version 2.2.2

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
    * [ECM-3057] - Throw exception when select new repository
    * [ECM-3058] - In File Explorer, parameter categoryMandatoryWhenFileUpload in portlet preferences is not read if changed in UI
    * [ECM-3081] - Exception when open form 'Information Auditing' with node is nt:file
    * [ECM-3214] - ECM actions aren't triggered when a document is added in the JCR via the FTP connector

** Doc
    * [ECM-3069] - Update the readme

** Task
    * [ECM-3153] - Leverage sub components updates from ECM 2.2.x
    * [ECM-3479] - Make ECM 2.2. compatible with JBoss

4. MIGRATION GUIDE
---------------------------------------------------
4.1. Update pom.xml at location web\ecmportal\
In ECM 2.2.2 we changed something to make war overlay work more well
  <dependentWarExcludes>
    index.jsp,WEB-INF/web.xml,WEB-INF/conf/*.xml,
    WEB-INF/conf/common/*.xml,
    WEB-INF/conf/database/*.xml,
    WEB-INF/conf/database/*.xml,
    WEB-INF/conf/jcr/*.xml,
    WEB-INF/conf/organization/*.xml,
    WEB-INF/conf/portal/*.xml,
    WEB-INF/conf/portal/group/,
    WEB-INF/conf/portal/portal/,
    WEB-INF/conf/portal/user/,
    WEB-INF/classes/locale/navigation/,
    WEB-INF/conf/script/groovy/SkinConfigScript.groovy, 
    templates/groovy/webui/component/UIHomePagePortlet.gtmpl,
    templates/skin/webui/component/UIHomePagePortlet/
  </dependentWarExcludes>

4.2 Update application-registry-configuration.xml at location web\ecmportal\src\main\webapp\WEB-INF\conf\portal
	
	In ECM 2.2.2 we added the configuration to init dashboard as the default portlet.

	<object-param>
    <name>dashboard</name>
    <description>description</description>
    <object type="org.exoplatform.application.registry.ApplicationCategory">
      <field name="name"><string>dashboard</string></field>
      <field name="displayName"><string>Dashboard</string></field> 
      <field name="description"><string>Dashboard</string></field>
      <field name="accessPermissions">
        <collection type="java.util.ArrayList" item-type="java.lang.String">													
					<value><string>*:/platform/users</string></value>													
        </collection>
      </field>                 	             
      <field  name="applications">
        <collection type="java.util.ArrayList">
          <value>                 
            <object type="org.exoplatform.application.registry.Application">                     
              <field name="categoryName"><string>dashboard</string></field>
              <field name="applicationName"><string>DashboardPortlet</string></field>
		          <field name="displayName"><string>Dashboard Portlet</string></field>
		          <field name="description"><string>Dashboard Portlet</string></field>
              <field name="applicationType"><string>portlet</string></field>
              <field name="applicationGroup"><string>dashboard</string></field>
              <field name="accessPermissions">
				        <collection type="java.util.ArrayList" item-type="java.lang.String">													
									<value><string>*:/platform/users</string></value>													
				        </collection>
		          </field>                 
            </object>
          </value>
          <value>                 
            <object type="org.exoplatform.application.registry.Application">                     
              <field name="categoryName"><string>dashboard</string></field>
              <field name="applicationName"><string>GadgetPortlet</string></field>
		          <field name="displayName"><string>Gadget Wrapper Portlet</string></field>
		          <field name="description"><string>Gadget Wrapper Portlet</string></field>
              <field name="applicationType"><string>portlet</string></field>
              <field name="applicationGroup"><string>dashboard</string></field>
              <field name="accessPermissions">
				        <collection type="java.util.ArrayList" item-type="java.lang.String">													
									<value><string>*:/platform/users</string></value>													
				        </collection>
		          </field>                 
            </object>
          </value>
        </collection>                               
      </field> 
    </object>        
  </object-param>

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