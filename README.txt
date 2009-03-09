Enterprise Content Management(ECM) > Document Management System(DMS)
Version 2.2.1

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

Check compatibility between ECM 2.2.1 and ECM 2.2
	
1. Changing configuration in exo.ecm.web.portal/src/main/java/conf/configuration.xml
Publication function: In ECM 2.2.1, the information in exo.ecm.component.publication/configuration.xml we can see:

	<component>
		<key>org.exoplatform.services.ecm.publication.PublicationService</key>
		<type>org.exoplatform.services.ecm.publication.impl.PublicationServiceImpl</type>
		<component-plugins>
			<component-plugin>
				<name>StaticAndDirect</name>
				<set-method>addPublicationPlugin</set-method>
				<type>org.exoplatform.services.ecm.publication.plugins.staticdirect.StaticAndDirectPublicationPlugin</type>
				<init-params>
					<values-param>
						<name>StaticAndDirect</name>
						<description>This publication lifecycle keeps the content at their original place. Any version of the Node can be published.</description>
					</values-param>
				</init-params>
			</component-plugin>
		</component-plugins>
  </component>

     And

	<external-component-plugins>
		<target-component>org.exoplatform.services.ecm.publication.PublicationService</target-component>      
	    <component-plugin>
			<name>addPublication</name>
			<set-method>addPublicationPlugin</set-method>
			<type>org.exoplatform.services.ecm.publication.plugins.staticdirect.StaticAndDirectPublicationPlugin</type>
			<init-params>
				<values-param>
					<name>StaticAndDirect</name>
					<description>StaticAndDirect Publication Plugin</description>
				</values-param>
			</init-params>
	    </component-plugin>
	</external-component-plugins>

	These above information is not exist any longer in this file in ECM 2.2. 
	They are moved and changed in a new file exo.ecm.web.portal/src/main/java/conf/portal/publication-configuration.xml. 
	So we must refer to this file by add code in exo.ecm.web.portal/src/main/java/conf/configuration.xml like:
	   <import>war:/conf/ecm/publication-configuration.xml</import>

	Coverflow and Thumbnail view mode:
	In ECM 2.2, We add new service ThumbnailRESTService in exo.ecm.component.cms for 
	Coverflow and Thumbnail view mode. To use this service, we declare for this class 
	by using new file: ecm-thumbnail-configuration.xml:

	<configuration>  
  		<component>
	 	 <key>org.exoplatform.services.cms.thumbnail.ThumbnailService</key>  
	 	 <type>org.exoplatform.services.cms.thumbnail.impl.ThumbnailServiceImpl</type>      
	 	 <init-params>
			<value-param>
				<name>smallSize</name>
				<value>32x32</value>
     		</value-param>
			<value-param>
				<name>mediumSize</name>
				<value>64x64</value>
            </value-param> 
			<value-param>
				<name>bigSize</name>
	     		<value>300x300</value>
     		</value-param>
			<value-param>
				<name>enable</name>
				<value>true</value>
    		</value-param>
			<value-param>
				<name>mimetypes</name>
				<value>image/jpeg;image/png;image/gif;image/bmp</value>
			</value-param>      
   		 </init-params>
		</component>
		<component>    
    		 <type>org.exoplatform.services.cms.thumbnail.impl.ThumbnailRESTService</type>    
 		</component>	
    </configuration>

    Then add the configuration information to exo.ecm.web.portal/src/main/java/conf/configuration.xml like:
	<import>war:/conf/ecm/ecm-thumbnail-configuration.xml</import>

2. Changing version in the configuration information for WorkflowServiceContainer class:
	In ECM 2.2.1, we can see in bonita-configuration.xml and jbpm-configuration.xml:

	<object type="org.exoplatform.services.workflow.ProcessesConfig">
		<fieldname="processLocation"><string>war:/conf/bp</string></field>
		<field name="predefinedProcess">
			<collection type="java.util.HashSet">
			<value><string>/exo.ecm.bp.bonita.payraise-2.2.1.jar</string></value>
			<value><string>/exo.ecm.bp.bonita.holiday-2.2.1.jar</string></value>
	        <value><string>/exo.ecm.bp.bonita.content.validation-2.2.1.jar</string></value>
			<value><string>/exo.ecm.bp.bonita.content.backup-2.2.1.jar</string></value>
	    </collection>
   </field>
   </object>

	And:

	<object type="org.exoplatform.services.workflow.ProcessesConfig">
	   <field name="processLocation"><string>war:/conf/bp</string></field>
	   <field name="predefinedProcess">
		<collection type="java.util.HashSet">
			<value><string>/exo.ecm.bp.jbpm.payraise-2.2.1.jar</string></value>
			<value><string>/exo.ecm.bp.jbpm.holiday-2.2.1.jar</string></value>
			<value><string>/exo.ecm.bp.jbpm.content.validation-2.2.1.jar</string></value>
			<value><string>/exo.ecm.bp.jbpm.content.backup-2.2.1.jar</string></value>
		</collection>
	   </field>
	</object>

	So, in ECM 2.2, we must changes in these file by replaces: 2.2.1.jar to 2.2.jar:

	<object type="org.exoplatform.services.workflow.ProcessesConfig">
	   <fieldname="processLocation"><string>war:/conf/bp</string></field>
	   <field name="predefinedProcess">
			<collection type="java.util.HashSet">
				<value><string>/exo.ecm.bp.bonita.payraise-2.2.1.jar</string></value>
				<value><string>/exo.ecm.bp.bonita.holiday-2.2.1.jar</string></value>
		        <value><string>/exo.ecm.bp.bonita.content.validation-2.2.1.jar</string></value>
				<value><string>/exo.ecm.bp.bonita.content.backup-2.2.1.jar</string></value>
			</collection>
		</field>
	</object>

And:

<object type="org.exoplatform.services.workflow.ProcessesConfig">
   <field name="processLocation"><string>war:/conf/bp</string></field>
   <field name="predefinedProcess">
	    <collection type="java.util.HashSet">
			<value><string>/exo.ecm.bp.jbpm.payraise-2.2.1.jar</string></value>
			<value><string>/exo.ecm.bp.jbpm.holiday-2.2.1.jar</string></value>
			<value><string>/exo.ecm.bp.jbpm.content.validation-2.2.1.jar</string></value>
			<value><string>/exo.ecm.bp.jbpm.content.backup-2.2.1.jar</string></value>
		</collection>
   </field>
</object>

3. Support add references when upload file

	Since ECM 2.2.1, in the portlet File Explorer, you can add references to the file which will be uploaded. By default this function is not mandatory. You can change the value equal true in the parameter *categoryMandatoryWhenFileUpload* to make sure every files will be added the categories when uploaded.
	You can see and change the configuration if you want in the file exo.ecm.portlet.ecm/src/main/webapp/WEB-INF/portlet.xml
	<preference>
	  <name>categoryMandatoryWhenFileUpload</name>     
	  <value>false</value>        
	  <read-only>false</read-only>
	</preference>

4. Add filter for resouces in web\ecmportal\src\main\webapp\WEB-INF\web.xml

	<filter-mapping>
		<filter-name>ResourceRequestFilter</filter-name>
		<url-pattern>*.css</url-pattern> 
	</filter-mapping>

	<filter-mapping>
		<filter-name>ResourceRequestFilter</filter-name>
		<url-pattern>*.gif</url-pattern> 
	</filter-mapping>

	<filter-mapping>
		<filter-name>ResourceRequestFilter</filter-name>
		<url-pattern>*.png</url-pattern> 
	</filter-mapping>

	<filter-mapping>
		<filter-name>ResourceRequestFilter</filter-name>
		<url-pattern>*.jpg</url-pattern> 
	</filter-mapping>

5. Add more workspace named gadgets in web\ecmportal\src\main\webapp\WEB-INF\conf\jcr\repository-configuration.xml:
	<workspace name="gadgets">
	  <!-- for system storage -->
	  <container class="org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer">
		<properties>
		  <property name="source-name" value="jdbcexo"/>
		  <property name="dialect" value="hsqldb"/>
		  <!-- property name="db-type" value="mysql"/ -->
		  <property name="multi-db" value="false"/>
		  <property name="update-storage" value="true"/>
		  <property name="max-buffer-size" value="200k"/>
		  <property name="swap-directory" value="../temp/swap/gadgets"/>
		</properties>
		<value-storages>
		  <value-storage id="gadgets" class="org.exoplatform.services.jcr.impl.storage.value.fs.TreeFileValueStorage">
			<properties>
			  <property name="path" value="../temp/values/gadgets"/>
			</properties>
			<filters>
			  <filter property-type="Binary"/>
			</filters>
		  </value-storage>
		</value-storages>
	  </container>
	  <initializer class="org.exoplatform.services.jcr.impl.core.ScratchWorkspaceInitializer">
		<properties>
		  <property name="root-nodetype" value="nt:unstructured"/>
		  <property name="root-permissions" value="any read;*:/platform/administrators read;*:/platform/administrators add_node;*:/platform/administrators set_property;*:/platform/administrators remove"/>
		</properties>
	  </initializer>
	  <cache enabled="true" class="org.exoplatform.services.jcr.impl.dataflow.persistent.LinkedWorkspaceStorageCacheImpl">
		<properties>
		  <property name="max-size" value="20k"/>
		  <property name="live-time" value="1h"/>
		</properties>
	  </cache>
	  <query-handler class="org.exoplatform.services.jcr.impl.core.query.lucene.SearchIndex">
		<properties>
		  <property name="index-dir" value="../temp/jcrlucenedb/gadgets"/>
		</properties>
	  </query-handler>
	  <lock-manager>
			  <time-out>15m</time-out><!-- 15min -->
			  <persister class="org.exoplatform.services.jcr.impl.core.lock.FileSystemLockPersister">
				<properties>
				  <property name="path" value="../temp/lock/gadgets"/>
				</properties>
			  </persister>
			</lock-manager>
	</workspace>

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