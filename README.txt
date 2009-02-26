Enterprise Content Management(ECM) > Document Management System(DMS)
Version 2.3

You may find it helpful to see the details at wiki place of ECM
http://wiki.exoplatform.org/xwiki/bin/view/ECM/

TABLE OF CONTENTS
-----------------
1 Check compatibility between ECM 2.2 and ECM 2.3


Since ECM 2.3, we split ECM to 3 products as [DMS], [Workflow>Workflows] and [WCM>http://wiki.exoplatform.org/xwiki/bin/view/WCM].
So, how can we upgrade from 2.2 to 2.3 ? You can follow these steps to get successful upgrade.
For now, we have the ECM suite which contains 3 products as 

ECM(Enterprise Content Management)
 |
 |__DMS(Document Management System)
 |
 |__Workflow
 |
 |__WCM(Web Content Management)

1.1 Migration for drives path

Since ECM 2.3, the DriveMigrationService was available at the location *ecm/dms/trunk/component/migration/2.3/drives*. This service is used to rename the old drive which contains invalid characters and prevent the WARNING messages at the console.\\

			How to do it?\\
* Compile the source code to create new jar *(exo.ecm.dms.component.migration.2.3.drives-2.3.jar)* by command: 

mvn clean install

   * Stop server and copy this jar to library.<br>
   * Run server DriveMigrationService to apply changes.<br>
   * After server has started, stop server again and remove this jar, restart server.

1.1 How to use for Workflow Publication Plugin ?

Since ECM 2.3, the Workflow Publication Plugin was availabled at the location *ecm/contentvalidation/trunk/component/workflowPublication/*. This plugin is used to publish a content of documents. It breaks a work process down into tasks. See more details about [Workflow Publication Plugin].

How to do it?
* Compile the source code to create new jar *(exo.ecm.contentvalidation.component.workflowPublication-1.0-SNAPSHOT.jar)* by command: 

mvn clean install

   * Stop server and copy this jar to server library (for example: \tomcat\lib)<br>
   * Run server to apply the changes.<br>

Here are the structure of this jar

exo.ecm.contentvalidation.component.workflowPublication-1.0-SNAPSHOT.jar
 |
 |__conf
 |  |
 |  |__nodetypes-workflow-publication-config.xml
 |  | 
 |  |__workflow-templates-configuration.xml
 |  |
 |  |__conf.portal
 |     |
 |     |__configuration.xml
 |
 |__...
 |
 |__...

   
* *configuration.xml*: This file is used to plug a new plugin (Workflow Publication Plugin). It will specify to two files: ~~nodetypes-workflow-publication-config.xml~~, ~~workflow-templates-configuration.xml~~ to configure for this plugin

<?xml version="1.0" encoding="ISO-8859-1"?>

<configuration>		       
  
  <component>
    <key>org.exoplatform.services.ecm.publication.PublicationService</key>
    <type>org.exoplatform.services.ecm.publication.impl.PublicationServiceImpl</type>    
  </component>

  <component>
    <key>org.exoplatform.services.ecm.publication.PublicationPresentationService</key>
    <type>org.exoplatform.services.ecm.publication.impl.PublicationPresentationServiceImpl</type>
  </component> 
  	
  <external-component-plugins>
    <target-component>org.exoplatform.services.jcr.RepositoryService</target-component>
    <component-plugin>
      <name>add.nodeType</name>
      <set-method>addPlugin</set-method>
      <type>org.exoplatform.services.jcr.impl.AddNodeTypePlugin</type>
      <priority>122</priority>
      <init-params>
        <values-param>
          <name>autoCreatedInNewRepository</name>
          <description>Node types configuration file</description>
          <value>jar:/conf/nodetypes-workflow-publication-config.xml</value>
        </values-param>
       </init-params>
     </component-plugin>
  </external-component-plugins>
  
  <external-component-plugins>
    <target-component>org.exoplatform.services.ecm.publication.PublicationService</target-component>      
    <component-plugin>
      <name>Workflow</name>
      <set-method>addPublicationPlugin</set-method>
      <type>org.exoplatform.services.ecm.publication.plugins.workflow.WorkflowPublicationPlugin</type>
      <description>Workflow Publication</description>	    	    	   
      <init-params>
        <value-param>
	  <name>validator</name>
	  <value>*:/platform/administrators</value>
        </value-param>
        <value-param>
	  <name>to_workspace</name>
          <value>collaboration</value>
        </value-param>
        <value-param>
          <name>destPath</name>
          <value>/Documents/Live</value>
        </value-param>
        <value-param>
	  <name>destPath_currentFolder</name>
	  <value>true</value>
        </value-param>
        <value-param>
	  <name>isEditable</name>
	  <value>false</value>
        </value-param>
        <value-param>
	  <name>backupWorkspace</name>
	  <value>backup</value>
	</value-param>
      </init-params>
    </component-plugin>
  </external-component-plugins>	
  <import>jar:/conf/workflow-templates-configuration.xml</import>
</configuration>


* *nodetypes-workflow-publication-config.xml*: This file is used to configure a new nodetype

<nodeTypes xmlns:nt="http://www.jcp.org/jcr/nt/1.0" xmlns:mix="http://www.jcp.org/jcr/mix/1.0"
  xmlns:jcr="http://www.jcp.org/jcr/1.0">
  
  <nodeType name="publication:workflowPublication" isMixin="true" hasOrderableChildNodes="false" primaryItemName="">
    <supertypes>
      <supertype>publication:publication</supertype>
    </supertypes>
    <propertyDefinitions>
      <propertyDefinition name="publication:validator" requiredType="String" autoCreated="true" mandatory="true"
        onParentVersion="COPY" protected="false" multiple="false">
        <valueConstraints/>
        <defaultValues>
          <defaultValue>*</defaultValue>
        </defaultValues>
      </propertyDefinition>
      <propertyDefinition name="publication:businessProcess" requiredType="String" autoCreated="true" mandatory="true"
        onParentVersion="COPY" protected="true" multiple="false">
        <valueConstraints/>
        <defaultValues>
          <defaultValue>content-publishing</defaultValue>
        </defaultValues>
      </propertyDefinition>
      <propertyDefinition name="publication:backupPath" requiredType="String" autoCreated="false" mandatory="true" onParentVersion="COPY" protected="false" multiple="false">
        <valueConstraints/>
      </propertyDefinition>
    </propertyDefinitions>
  </nodeType>
  
  <nodeType name="publication:workflowAction" isMixin="false" hasOrderableChildNodes="false" primaryItemName="">
    <supertypes>
      <supertype>exo:businessProcessAction</supertype>
    </supertypes>
    <propertyDefinitions>
      <propertyDefinition name="exo:validator" requiredType="String" autoCreated="false" mandatory="true"
        onParentVersion="COPY" protected="false" multiple="false"/>
      <propertyDefinition name="exo:businessProcess" requiredType="String" autoCreated="true" mandatory="true"
        onParentVersion="COPY" protected="true" multiple="false">
        <valueConstraints/>
        <defaultValues>
          <defaultValue>content-publishing</defaultValue>
        </defaultValues>
      </propertyDefinition>
    </propertyDefinitions>
  </nodeType>
  
</nodeTypes>


* *workflow-templates-configuration.xml*: This file is used to decide the template of publication : workflowAction. It will specify the dialog as well as view for that template.

<?xml version="1.0" encoding="UTF-8"?>
<configuration>

  <component>
    <key>org.exoplatform.services.cms.templates.TemplateService</key>
    <type>org.exoplatform.services.cms.templates.impl.TemplateServiceImpl</type>    
  </component>
    
  <external-component-plugins>
    <target-component>org.exoplatform.services.cms.templates.TemplateService</target-component>
    <component-plugin>
      <name>addTemplates</name>
      <set-method>addTemplates</set-method>
      <type>org.exoplatform.services.cms.templates.impl.TemplatePlugin</type>    
      <init-params>
        <value-param>
          <name>autoCreateInNewRepository</name>
	  <value>true</value>
        </value-param>
        <value-param>
          <name>storedLocation</name>
	  <value>jar:/resources/templates/workflowAction</value>
	</value-param>
	<value-param>
	  <name>repository</name>
	  <value>repository</value>
	</value-param>	        
	<object-param>
	  <name>template.configuration</name>
	  <description>configuration for the localtion of templates to inject in jcr</description>
	  <object type="org.exoplatform.services.cms.templates.impl.TemplateConfig">            	
	    <field name="nodeTypes">
	      <collection type="java.util.ArrayList">               
	        <value>
	          <object type="org.exoplatform.services.cms.templates.impl.TemplateConfig$NodeType">
	            <field name="nodetypeName"><string>publication:workflowAction</string></field>
	            <field name="documentTemplate"><boolean>false</boolean></field>
                    <field name="label"><string>Workflow Publication Action</string></field>
	            <field name="referencedView">
	              <collection type="java.util.ArrayList">
	                <value>
	                  <object type="org.exoplatform.services.cms.templates.impl.TemplateConfig$Template">             
	                    <field name="templateFile"><string>/workflowActionView.gtmpl</string></field>
	                    <field name="roles"><string>*</string></field>                
	                  </object>                           
	                </value>  
	              </collection>
	            </field>                  
	            <field name="referencedDialog">
	              <collection type="java.util.ArrayList">
	                <value>
	                  <object type="org.exoplatform.services.cms.templates.impl.TemplateConfig$Template">             
	                    <field name="templateFile"><string>/workflowActionDialog.gtmpl</string></field>
	                    <field name="roles"><string>*</string></field>                
	                  </object>                           
	                </value>                      
	              </collection>
	            </field>                                    
	          </object>
	        </value> 
	      </collection>
	    </field>
	  </object>
	</object-param>
      </init-params>       
    </component-plugin>
  </external-component-plugins>
  
</configuration>


==========================================================================================
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