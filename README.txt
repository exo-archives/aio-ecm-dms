
	Check compatibility between ECM 2.1 and ECM 2.2

	
1. Changing configuration in exo.ecm.web.portal/src/main/java/conf/configuration.xml
Publication function: In ECM 2.1, the information in exo.ecm.component.publication/configuration.xml we can see:

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
	In ECM 2.1, we can see in bonita-configuration.xml and jbpm-configuration.xml:

	<object type="org.exoplatform.services.workflow.ProcessesConfig">
		<fieldname="processLocation"><string>war:/conf/bp</string></field>
		<field name="predefinedProcess">
			<collection type="java.util.HashSet">
			<value><string>/exo.ecm.bp.bonita.payraise-2.1.jar</string></value>
			<value><string>/exo.ecm.bp.bonita.holiday-2.1.jar</string></value>
	        <value><string>/exo.ecm.bp.bonita.content.validation-2.1.jar</string></value>
			<value><string>/exo.ecm.bp.bonita.content.backup-2.1.jar</string></value>
	    </collection>
   </field>
   </object>

	And:

	<object type="org.exoplatform.services.workflow.ProcessesConfig">
	   <field name="processLocation"><string>war:/conf/bp</string></field>
	   <field name="predefinedProcess">
		<collection type="java.util.HashSet">
			<value><string>/exo.ecm.bp.jbpm.payraise-2.1.jar</string></value>
			<value><string>/exo.ecm.bp.jbpm.holiday-2.1.jar</string></value>
			<value><string>/exo.ecm.bp.jbpm.content.validation-2.1.jar</string></value>
			<value><string>/exo.ecm.bp.jbpm.content.backup-2.1.jar</string></value>
		</collection>
	   </field>
	</object>

	So, in ECM 2.2, we must changes in these file by replaces: 2.1.jar to 2.2-SNAPSHOT:

	<object type="org.exoplatform.services.workflow.ProcessesConfig">
	   <fieldname="processLocation"><string>war:/conf/bp</string></field>
	   <field name="predefinedProcess">
			<collection type="java.util.HashSet">
				<value><string>/exo.ecm.bp.bonita.payraise-2.2-SNAPSHOT.jar</string></value>
				<value><string>/exo.ecm.bp.bonita.holiday-2.2-SNAPSHOT.jar</string></value>
		        <value><string>/exo.ecm.bp.bonita.content.validation-2.2-SNAPSHOT.jar</string></value>
				<value><string>/exo.ecm.bp.bonita.content.backup-2.2-SNAPSHOT.jar</string></value>
			</collection>
		</field>
	</object>

And:

<object type="org.exoplatform.services.workflow.ProcessesConfig">
   <field name="processLocation"><string>war:/conf/bp</string></field>
   <field name="predefinedProcess">
	    <collection type="java.util.HashSet">
			<value><string>/exo.ecm.bp.jbpm.payraise-2.2-SNAPSHOT.jar</string></value>
			<value><string>/exo.ecm.bp.jbpm.holiday-2.2-SNAPSHOT.jar</string></value>
			<value><string>/exo.ecm.bp.jbpm.content.validation-2.2-SNAPSHOT.jar</string></value>
			<value><string>/exo.ecm.bp.jbpm.content.backup-2.2-SNAPSHOT.jar</string></value>
		</collection>
   </field>
</object>

Support add references when upload file

In the portlet File Explorer of ECM 2.2, you can add references to the file which will be uploaded. 
By default this function is not mandatory. You can change the value equal true in the parameter categoryMandatoryWhenFileUpload
to make sure every files will be added the categories when uploaded. 
You can see and change the configuration if you want in the file exo.ecm.portlet.ecm/src/main/webapp/WEB-INF/portlet.xml

<preference>
  <name>categoryMandatoryWhenFileUpload</name>     
  <value>false</value>        
  <read-only>false</read-only>
</preference>







