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
package org.exoplatform.services.cms.views.impl;

import java.io.InputStream;
import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.views.TemplateConfig;
import org.exoplatform.services.cms.views.ViewConfig;
import org.exoplatform.services.cms.views.ViewConfig.Tab;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

public class ManageViewPlugin extends BaseComponentPlugin {

  private static String CB_PATH_TEMPLATE = "pathTemplate".intern() ;
  private static String CB_QUERY_TEMPLATE = "queryTemplate".intern() ;
  private static String CB_DETAIL_VIEW_TEMPLATE = "detailViewTemplate".intern() ;
  private static String CB_SCRIPT_TEMPLATE = "scriptTemplate".intern() ;
  private static String ECM_EXPLORER_TEMPLATE = "ecmExplorerTemplate".intern() ;
  private InitParams params_ ;
  private RepositoryService repositoryService_ ;
  private NodeHierarchyCreator nodeHierarchyCreator_ ; 
  private ConfigurationManager cservice_ ;
  private boolean autoCreateInNewRepository_ = false ;  
  private String predefinedViewsLocation_ = "war:/conf/ecm/artifacts";

  public ManageViewPlugin(RepositoryService repositoryService, InitParams params, ConfigurationManager cservice, 
      NodeHierarchyCreator nodeHierarchyCreator) throws Exception {
    params_ = params ;
    repositoryService_ = repositoryService ;
    nodeHierarchyCreator_ = nodeHierarchyCreator ;
    cservice_ = cservice ;
    ValueParam autoInitParam = params.getValueParam("autoCreateInNewRepository") ;    
    if(autoInitParam !=null) {
      autoCreateInNewRepository_ = Boolean.parseBoolean(autoInitParam.getValue()) ;
    }    
    ValueParam predefinedViewLocation = params.getValueParam("predefinedViewsLocation"); 
    if(predefinedViewLocation != null) {
      predefinedViewsLocation_ = predefinedViewLocation.getValue();
    }
  }

  public void init() throws Exception {    
    if(autoCreateInNewRepository_) {
      for(RepositoryEntry entry:repositoryService_.getConfig().getRepositoryConfigurations()) {
        importPredefineViews(entry.getName()) ;
      }
      return ;
    }
    ValueParam param = params_.getValueParam("repository") ;
    String repository = null ;
    if(param !=null) {
      repository = param.getValue();
    }else {
      repository = repositoryService_.getDefaultRepository().getConfiguration().getName();
    }    
    importPredefineViews(repository) ;
  }

  public void init(String repository) throws Exception {
    if(!autoCreateInNewRepository_) return ;
    importPredefineViews(repository) ;
  }
  
  @SuppressWarnings("unchecked")
  private void importPredefineViews(String repository) throws Exception {
    Iterator<ObjectParameter> it = params_.getObjectParamIterator() ;       
    String viewsPath = nodeHierarchyCreator_.getJcrPath(BasePath.CMS_VIEWS_PATH);
    String templatesPath = nodeHierarchyCreator_.getJcrPath(BasePath.CMS_VIEWTEMPLATES_PATH);    
    String warViewPath = predefinedViewsLocation_ + templatesPath.substring(templatesPath.lastIndexOf("exo:ecm") + 7) ;
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository) ;    
    String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName() ;
    Session session = manageableRepository.getSystemSession(workspace) ;
    ViewConfig viewObject = null ;
    TemplateConfig templateObject = null ;
    Node viewHomeNode = (Node)session.getItem(viewsPath) ;
    while(it.hasNext()) {
      Object object = it.next().getObject();
      if(object instanceof ViewConfig) {
        viewObject = (ViewConfig)object ;
        String viewNodeName = viewObject.getName();
        if(viewHomeNode.hasNode(viewNodeName)) continue ;
        Node viewNode = addView(viewHomeNode,viewNodeName,viewObject.getPermissions(),viewObject.getTemplate()) ;
        for(Tab tab:viewObject.getTabList()) {
          addTab(viewNode,tab.getTabName(),tab.getButtons()) ;
        }
      }else if(object instanceof TemplateConfig) {
        templateObject = (TemplateConfig) object;
        addTemplate(templateObject,session,warViewPath) ;
      }          
    }        
    session.save();
    session.logout();
  }
  
  @SuppressWarnings("hiding")
  private Node addView(Node viewManager, String name, String permissions, String template) throws Exception {
    Node contentNode = viewManager.addNode(name, "exo:view");
    contentNode.setProperty("exo:accessPermissions", permissions);
    contentNode.setProperty("exo:template", template);  
    viewManager.save() ;
    return contentNode ;
  }
  
  @SuppressWarnings("hiding")
  private void addTab(Node view, String name, String buttons) throws Exception {
    Node tab ;
    if(view.hasNode(name)){
      tab = view.getNode(name) ;
    }else {
      tab = view.addNode(name, "exo:tab"); 
    }
    tab.setProperty("exo:buttons", buttons); 
    view.save() ;
  }

  private void addTemplate(TemplateConfig tempObject, Session session, String warViewPath) throws Exception {    
    String type = tempObject.getTemplateType() ;
    String alias = "" ;    
    if(type.equals(ECM_EXPLORER_TEMPLATE)) {
      alias = BasePath.ECM_EXPLORER_TEMPLATES ;
    }else if(type.equals(CB_PATH_TEMPLATE)) {
      alias = BasePath.CB_PATH_TEMPLATES ;
    }else if(type.equals(CB_QUERY_TEMPLATE)) {
      alias = BasePath.CB_QUERY_TEMPLATES ;
    }else if(type.equals(CB_SCRIPT_TEMPLATE)) {
      alias = BasePath.CB_SCRIPT_TEMPLATES ;
    }else if(type.equals(CB_DETAIL_VIEW_TEMPLATE)) {
      alias = BasePath.CB_DETAIL_VIEW_TEMPLATES ;
    }         
    String templateHomePath = nodeHierarchyCreator_.getJcrPath(alias) ;    
    Node templateHomeNode = (Node)session.getItem(templateHomePath) ;    
    String templateName = tempObject.getName() ;    
    if(templateHomeNode.hasNode(templateName)) return  ;
    String warPath = warViewPath + tempObject.getWarPath() ;
    InputStream in = cservice_.getInputStream(warPath) ;
    Node templateNode = templateHomeNode.addNode(templateName,"exo:template") ;
    templateNode.setProperty("exo:templateFile", in) ;
    templateHomeNode.save() ;     
  }
}
