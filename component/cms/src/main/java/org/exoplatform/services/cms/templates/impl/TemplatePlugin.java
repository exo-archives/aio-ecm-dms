/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.cms.templates.impl;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.impl.Utils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

public class TemplatePlugin extends BaseComponentPlugin {

  static final public String DIALOGS = "dialogs".intern();
  static final public String VIEWS = "views".intern();

  static final public String DEFAULT_DIALOG = "dialog1".intern();
  static final public String DEFAULT_VIEW = "view1".intern();

  static final String[] UNDELETABLE_TEMPLATES = {DEFAULT_DIALOG, DEFAULT_VIEW};  

  static final public String DEFAULT_DIALOGS_PATH = "/" + DIALOGS + "/" + DEFAULT_DIALOG;
  static final public String DEFAULT_VIEWS_PATH = "/" + VIEWS + "/" + DEFAULT_VIEW;

  static final public String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
  static final public String EXO_TEMPLATE = "exo:template".intern() ;
  static final public String EXO_ROLES_PROP = "exo:roles".intern() ;
  static final public String EXO_TEMPLATE_FILE_PROP = "exo:templateFile".intern() ;  
  static final public String DOCUMENT_TEMPLATE_PROP = "isDocumentTemplate".intern() ;  
  static final public String TEMPLATE_LABEL = "label".intern() ;

  private RepositoryService repositoryService_;
  private ConfigurationManager  configManager_;
  private NodeHierarchyCreator nodeHierarchyCreator_;
  private String cmsTemplatesBasePath_ ; 
  private InitParams params_ ;
  private String storedLocation_ ;
  private boolean autoCreateInNewRepository_=false;

  public TemplatePlugin(InitParams params, RepositoryService jcrService, ConfigurationManager configManager,
      NodeHierarchyCreator nodeHierarchyCreator) throws Exception {
    nodeHierarchyCreator_ = nodeHierarchyCreator;
    repositoryService_ = jcrService;
    configManager_ = configManager;
    cmsTemplatesBasePath_ = nodeHierarchyCreator_.getJcrPath(BasePath.CMS_TEMPLATES_PATH) ;
    params_ = params ;    
    ValueParam locationParam = params_.getValueParam("storedLocation") ;
    storedLocation_ = locationParam.getValue() ;
    ValueParam param = params_.getValueParam("autoCreateInNewRepository");
    if(param!=null) {
      autoCreateInNewRepository_ = Boolean.parseBoolean(param.getValue()) ;
    }        
  }

  public void init() throws Exception {               
    if(autoCreateInNewRepository_) {
      List<RepositoryEntry> repositories = repositoryService_.getConfig().getRepositoryConfigurations() ;      
      for(RepositoryEntry repo:repositories) {        
        importPredefineTemplates(repo.getName()) ;
      }
    }else {
      ValueParam valueParam = params_.getValueParam("repository") ;
      String repository = null ;
      if(valueParam != null) {
        repository = valueParam.getValue() ;
      }else {
        repository = repositoryService_.getDefaultRepository().getConfiguration().getName();
      }      
      importPredefineTemplates(repository) ;
    }        
  }

  public void init(String repository) throws Exception {        
    if(autoCreateInNewRepository_) {
      importPredefineTemplates(repository) ;
    }          
  }
  private void addTemplate(TemplateConfig templateConfig, Node templatesHome,String storedLocation) throws Exception{
    List nodetypes = templateConfig.getNodeTypes();
    TemplateConfig.NodeType nodeType = null ;       
    Iterator iter = nodetypes.iterator() ;
    while(iter.hasNext()) {
      nodeType = (TemplateConfig.NodeType) iter.next();
      Node nodeTypeHome = null;      
      nodeTypeHome = Utils.makePath(templatesHome, nodeType.getNodetypeName(),NT_UNSTRUCTURED);
      if(nodeType.getDocumentTemplate())
        nodeTypeHome.setProperty(DOCUMENT_TEMPLATE_PROP, true) ;
      else
        nodeTypeHome.setProperty(DOCUMENT_TEMPLATE_PROP, false) ;

      nodeTypeHome.setProperty(TEMPLATE_LABEL, nodeType.getLabel()) ;
      
      List dialogs = nodeType.getReferencedDialog();
      Node dialogsHome = Utils.makePath(nodeTypeHome, DIALOGS, NT_UNSTRUCTURED);
      addNode(storedLocation, dialogsHome, dialogs);
      
      List views = nodeType.getReferencedView();
      Node viewsHome = Utils.makePath(nodeTypeHome, VIEWS, NT_UNSTRUCTURED);
      addNode(storedLocation, viewsHome, views);
            
    }    
  }

  public void setBasePath(String basePath) { cmsTemplatesBasePath_ = basePath ; }

  @SuppressWarnings("unchecked")
  private void importPredefineTemplates(String repositoryName) throws Exception {
    ManageableRepository repository = repositoryService_.getRepository(repositoryName) ;
    String workspace = repository.getConfiguration().getDefaultWorkspaceName();
    Session session = repository.getSystemSession(workspace) ;
    Node templatesHome = Utils.makePath(session.getRootNode(), cmsTemplatesBasePath_, NT_UNSTRUCTURED);
    TemplateConfig templateConfig = null ;
    Iterator<ObjectParameter> iter = params_.getObjectParamIterator() ;
    //be carefull. Maybe lost data here
    //recommented by Hung: this code over the init template in second plugin
    /*if(templatesHome.hasNodes()) {
      session.logout();
      return ;
    }*/
    while(iter.hasNext()) {
      Object object = iter.next().getObject() ;
      if(!(object instanceof TemplateConfig)) {          
        break ;
      }
      templateConfig = (TemplateConfig)object ;
      addTemplate(templateConfig,templatesHome,storedLocation_) ;
    }
    session.save();
    session.logout();
  }

  private void addNode(String basePath, Node nodeTypeHome, List templates)  throws Exception {
    for (Iterator iterator = templates.iterator(); iterator.hasNext();) {
      TemplateConfig.Template template = (TemplateConfig.Template) iterator.next();
      String templateFileName = template.getTemplateFile();
      String path = basePath + templateFileName;            
      InputStream in = configManager_.getInputStream(path);
      String nodeName = 
        templateFileName.substring(templateFileName.lastIndexOf("/") + 1, templateFileName.indexOf("."));
      if(nodeTypeHome.hasNode(nodeName)) return ;
      Node contentNode = nodeTypeHome.addNode(nodeName, EXO_TEMPLATE);
      contentNode.setProperty(EXO_ROLES_PROP, template.getParsedRoles());
      contentNode.setProperty(EXO_TEMPLATE_FILE_PROP, in);      
    }
  }    
}
