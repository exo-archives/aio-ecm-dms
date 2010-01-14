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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.cms.impl.DMSRepositoryConfiguration;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.services.cms.views.ViewConfig;
import org.exoplatform.services.cms.views.ViewConfig.Tab;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.ext.UIExtension;
import org.exoplatform.webui.ext.UIExtensionManager;
import org.picocontainer.Startable;
/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Quang Hung
 *          nguyenkequanghung@yahoo.com
 * Feb 27, 2006
 */
public class ManageViewServiceImpl implements ManageViewService, Startable {

  /**
   * Logger.
   */
  private static final Log LOG  = ExoLogger.getLogger(ManageViewServiceImpl.class);
  
  protected final static String EXO_TEMPLATE = "exo:template".intern() ;
  protected final static String TEMPLATE_PROP = "exo:templateFile".intern() ;
  protected final static String ADMIN_VIEW = "admin".intern() ;
  protected final static String DEFAULT_VIEW = "default".intern() ;
  protected final static String EXO_PERMISSIONS = "exo:accessPermissions".intern()  ;
  protected final static String BUTTON_PROP = "exo:buttons".intern() ;

  private final List<ManageViewPlugin> plugins_ = new ArrayList<ManageViewPlugin> ();
  private List<?> buttons_ ;
  private final RepositoryService repositoryService_ ;
  private String baseViewPath_ ;
  private final NodeHierarchyCreator nodeHierarchyCreator_ ;
  private final DMSConfiguration dmsConfiguration_;
  private final UIExtensionManager extensionManager_;
  
  /**
   * Constructor
   * @param jcrService            : Manage repository 
   * @param nodeHierarchyCreator  : Manage alias path
   * @param dmsConfiguration      : Manage dms-system workspace
   * @param extensionManager      : Manage UIComponent in each view
   * @throws Exception
   */
  public ManageViewServiceImpl(RepositoryService jcrService,
      NodeHierarchyCreator nodeHierarchyCreator, DMSConfiguration dmsConfiguration,
      UIExtensionManager extensionManager) throws Exception{
    repositoryService_ = jcrService ;
    nodeHierarchyCreator_ = nodeHierarchyCreator ;
    baseViewPath_ = nodeHierarchyCreator_.getJcrPath(BasePath.CMS_VIEWS_PATH) ;
    dmsConfiguration_ = dmsConfiguration;
    extensionManager_ = extensionManager;
  }

  /**
   * Add buttons defined in configuration file
   */
  private void initButtons() {
    List<UIExtension> extensions = extensionManager_.getUIExtensions(EXTENSION_TYPE);
    List<String> actions = new ArrayList<String>();
    if (extensions != null) {
      for (UIExtension extension : extensions) {
        actions.add(extension.getName());
      }
    }
    // prevent from any undesired modification
    buttons_ = Collections.unmodifiableList(actions);
  }
  
  //Start initiating from configuration file
  public void start() {
    try {
      initButtons();
      for(ManageViewPlugin plugin : plugins_) {
        plugin.init() ;
      } 
    } catch(Exception e) {
      LOG.error("an error occured while starting the component", e);
    }    
  }

  /**
   * {@inheritDoc}
   */
  public void stop() { }

  /**
   * {@inheritDoc}
   */
  public void init(String repository) throws Exception  {
    for(ManageViewPlugin plugin : plugins_) {
      plugin.init(repository) ;
    }    
  }

  /**
   * {@inheritDoc}
   */
  public void setManageViewPlugin(ManageViewPlugin viewPlugin) {
    plugins_.add(viewPlugin) ;
  }

  /**
   * {@inheritDoc}
   */
  public List<?> getButtons(){
    return buttons_ ;
  }

  /**
   * {@inheritDoc}
   */
  public Node getViewHome(String repository) throws Exception {    
    String viewsPath = nodeHierarchyCreator_.getJcrPath(BasePath.CMS_VIEWS_PATH);
    return (Node) getSession(repository).getItem(viewsPath);
  }

  /**
   * {@inheritDoc}
   */
  public List<ViewConfig> getAllViews(String repository) throws Exception {
    List<ViewConfig> viewList = new ArrayList<ViewConfig>() ;
    ViewConfig view = null;
    Node viewNode  = null ;
    String viewsPath = nodeHierarchyCreator_.getJcrPath(BasePath.CMS_VIEWS_PATH);
    Session session = getSession(repository) ;
    try {
      Node viewHome = (Node)session.getItem(viewsPath) ;
      for(NodeIterator iter = viewHome.getNodes(); iter.hasNext();) {
        view = new ViewConfig() ;
        viewNode = iter.nextNode() ;
        view.setName(viewNode.getName()) ;      
        view.setPermissions(viewNode.getProperty(EXO_PERMISSIONS).getString()) ;
        view.setTemplate(viewNode.getProperty(EXO_TEMPLATE).getString()) ;
        List<Tab> tabList = new ArrayList<Tab>() ;
        for(NodeIterator tabsIterator = viewNode.getNodes(); tabsIterator.hasNext(); ) {
          Tab tab = new Tab();
          tab.setTabName(tabsIterator.nextNode().getName());
          tabList.add(tab) ;
        }
        view.setTabList(tabList) ;
        viewList.add(view) ;
      }
    } catch(AccessDeniedException ace) {
      return new ArrayList<ViewConfig>() ;
    } finally {
      if(session != null) session.logout();
    }
    return viewList ;    
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasView(String name, String repository) throws Exception {
    Session session = getSession(repository) ;
    Node viewHome = (Node)session.getItem(baseViewPath_) ;
    boolean b = viewHome.hasNode(name) ;
    session.logout();
    return b;
  }

  /**
   * {@inheritDoc}
   */
  public Node getViewByName(String name, String repository,SessionProvider provider) throws Exception{          
    Session session = getSession(repository,provider) ;
    try {
      return (Node)session.getItem(baseViewPath_ + "/" + name) ; 
    } catch(AccessDeniedException ace) {
      session.logout();
      return null ;
    } 
  }
  
  /**
   * {@inheritDoc}
   */
  public void addView(String name, String permissions, String template, List<?> tabs, 
      String repository) throws Exception{
    Session session = getSession(repository) ;
    Node viewHome = (Node)session.getItem(baseViewPath_) ;
    Node view ;
    if(viewHome.hasNode(name)) {
      view = viewHome.getNode(name) ;
      if(!view.isCheckedOut()) view.checkout() ;
      view.setProperty(EXO_PERMISSIONS, permissions) ;
      view.setProperty(EXO_TEMPLATE, template) ;
    }else {
      view = addView(viewHome, name, permissions, template) ;
    }
    String tabName ;
    String buttons ;
    for(int i = 0 ; i < tabs.size() ; i ++ ){
      try{
        Node tab = (Node) tabs.get(i)  ;
        tabName = tab.getName() ;
        buttons = tab.getProperty(BUTTON_PROP).getString() ;
      }catch(Exception e) {
        Tab tab = (Tab)tabs.get(i) ;
        tabName = tab.getTabName() ;
        buttons = tab.getButtons() ;
      }
      addTab(view, tabName, buttons) ;
    }
    viewHome.save() ;
    session.save();
    session.logout();
  }
  
  /**
   * {@inheritDoc}
   */
  public void removeView(String viewName, String repository) throws Exception {
    Session session = getSession(repository) ;
    Node viewHome = (Node)session.getItem(baseViewPath_) ;
    if(viewHome.hasNode(viewName)){
      Node view = viewHome.getNode(viewName) ;
      view.remove() ;
      viewHome.save() ;
      session.save();
    }              
    session.logout();
  }

  /**
   * {@inheritDoc}
   */
  public void addTab(Node view, String name, String buttons) throws Exception {
    Node tab ;
    if(view.hasNode(name)){
      tab = view.getNode(name) ;
    }else {
      tab = view.addNode(name, "exo:tab"); 
    }
    tab.setProperty("exo:buttons", buttons); 
    view.save() ;
  }

  /**
   * {@inheritDoc}
   */
  public Node getTemplateHome(String homeAlias, String repository,SessionProvider provider) throws Exception{
    String homePath = getJCRPath(homeAlias) ;
    Session session = getSession(repository,provider) ;
    try {
      return (Node)session.getItem(homePath); 
    } catch(AccessDeniedException ace) {
      session.logout();
      return null ;
    }
  }

  /**
   * Get path by alias 
   * @param jcrAlias
   * @return
   * @throws Exception
   */
  private String getJCRPath(String jcrAlias) throws Exception{
    return nodeHierarchyCreator_.getJcrPath(jcrAlias) ;
  }


  /**
   * Get session by repository
   * @param repository      : ManageableRepository object
   * @return
   * @throws Exception
   */
  private Session getSession(String repository) throws Exception {
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository) ;
    DMSRepositoryConfiguration dmsRepoConfig = dmsConfiguration_.getConfig(repository);
    return manageableRepository.getSystemSession(dmsRepoConfig.getSystemWorkspace()) ;    
  }
  
  /**
   * Get session by repository and SessionProvider
   * @param repository
   * @param sessionProvider
   * @return
   * @throws Exception
   */
  private Session getSession(String repository,SessionProvider sessionProvider) throws Exception{
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository) ;
    DMSRepositoryConfiguration dmsRepoConfig = dmsConfiguration_.getConfig(repository);
    return sessionProvider.getSession(dmsRepoConfig.getSystemWorkspace(), manageableRepository) ;
  }

  /**
   * {@inheritDoc}
   */
  public List<Node> getAllTemplates(String homeAlias, String repository,SessionProvider provider) throws Exception {
    Node templateHomNode = getTemplateHome(homeAlias, repository,provider) ;
    List<Node> list = new ArrayList<Node>() ;
    if(templateHomNode == null) return list ;
    for(NodeIterator iter = templateHomNode.getNodes() ; iter.hasNext() ;) {
      list.add(iter.nextNode()) ;
    }
    return list;
  }

  /**
   * {@inheritDoc}
   */
  public Node getTemplate(String path, String repository,SessionProvider provider) throws Exception{    
    return (Node)getSession(repository,provider).getItem(path) ;
  }


  /**
   * {@inheritDoc}
   */
  public String addTemplate(String name, String content, String homeTemplate, String repository) throws Exception {
    Session session = getSession(repository) ;
    Node templateHome = (Node)session.getItem(homeTemplate) ;
    Node newTemp = null ;
    if(templateHome.hasNode(name)) {
      newTemp = templateHome.getNode(name) ;      
    }else {
      newTemp = templateHome.addNode(name,EXO_TEMPLATE) ;
    }
    newTemp.setProperty(TEMPLATE_PROP,content) ;
    templateHome.save() ;
    session.save();
    session.logout();
    return newTemp.getPath() ;    
  }


  /**
   * {@inheritDoc}
   */
  public void removeTemplate(String templatePath, String repository) throws Exception {
    Node selectedTemplate = (Node) getSession(repository).getItem(templatePath);
    Node parent = selectedTemplate.getParent();
    selectedTemplate.remove();
    parent.save();
    parent.getSession().save();    
  }

  /**
   * Add view node into one node with given name, permssion and template
   * @param viewManager
   * @param name
   * @param permissions
   * @param template
   * @return
   * @throws Exception
   */
  private Node addView(Node viewManager, String name, String permissions, String template) throws Exception {
    Node contentNode = viewManager.addNode(name, "exo:view");
    contentNode.setProperty("exo:accessPermissions", permissions);
    contentNode.setProperty("exo:template", template);
    viewManager.save();
    return contentNode;
  }
  
}
