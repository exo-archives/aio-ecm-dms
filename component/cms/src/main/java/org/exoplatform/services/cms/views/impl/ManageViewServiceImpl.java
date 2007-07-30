/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.views.impl;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.services.cms.views.ViewConfig;
import org.exoplatform.services.cms.views.ViewConfig.Tab;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.picocontainer.Startable;
/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Quang Hung
 *          nguyenkequanghung@yahoo.com
 * Feb 27, 2006
 */
public class ManageViewServiceImpl implements ManageViewService, Startable {

  protected final static String EXO_TEMPLATE = "exo:template".intern() ;
  protected final static String TEMPLATE_PROP = "exo:templateFile".intern() ;
  protected final static String ADMIN_VIEW = "admin".intern() ;
  protected final static String DEFAULT_VIEW = "default".intern() ;
  protected final static String EXO_PERMISSIONS = "exo:permissions".intern()  ;
  protected final static String BUTTON_PROP = "exo:buttons".intern() ;

  private List<ManageViewPlugin> plugins_ = new ArrayList<ManageViewPlugin> ();
  private String buttons_ ;
  private RepositoryService repositoryService_ ;
  private String baseViewPath_ ;
  private CmsConfigurationService cmsConfigurationService_ ;

  public ManageViewServiceImpl(InitParams params, RepositoryService jcrService,
      CmsConfigurationService cmsConfigurationService) throws Exception{
    repositoryService_ = jcrService ;
    cmsConfigurationService_ = cmsConfigurationService ;
    baseViewPath_ = cmsConfigurationService.getJcrPath(BasePath.CMS_VIEWS_PATH) ;
    ValueParam buttonParam = params.getValueParam("buttons") ;
    buttons_ = buttonParam.getValue() ;
  }

  public void start() {
    try{
      for(ManageViewPlugin plugin : plugins_) {
        plugin.init() ;
      } 
    }catch(Exception e) {
      e.printStackTrace() ;
    }    
  }

  public void stop() { }

  public void init(String repository) throws Exception  {
    for(ManageViewPlugin plugin : plugins_) {
      plugin.init(repository) ;
    }    
  }

  public void setManageViewPlugin(ManageViewPlugin viewPlugin) {
    plugins_.add(viewPlugin) ;
  }

  @SuppressWarnings("unchecked")
  public List getButtons(){
    List buttonList = new ArrayList() ;
    if (buttons_ == null || buttons_.length() < 1) return buttonList ;    
    if(buttons_.indexOf(";") > -1) {
      String[] buttons = StringUtils.split(buttons_ , ";") ;
      if(buttons == null || buttons.length < 1) return null ;
      for(int i = 0 ; i < buttons.length ; i ++) {
        buttonList.add(buttons[i].trim()) ;
      }
    } else {
      buttonList.add(buttons_) ;
    }    
    return buttonList ;
  }

  public Node getViewHome(String repository) throws Exception {    
    String viewsPath = cmsConfigurationService_.getJcrPath(BasePath.CMS_VIEWS_PATH);
    return (Node) getSession(repository).getItem(viewsPath);
  }

  public List<ViewConfig> getAllViews(String repository) throws Exception {
    List<ViewConfig> viewList = new ArrayList<ViewConfig>() ;
    ViewConfig view = null;
    Node viewNode  = null ;
    String viewsPath = cmsConfigurationService_.getJcrPath(BasePath.CMS_VIEWS_PATH);
    Session session = getSession(repository) ;
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
    session.logout();
    return viewList ;    
  }

  public boolean hasView(String name, String repository) throws Exception {
    Session session = getSession(repository) ;
    Node viewHome = (Node)session.getItem(baseViewPath_) ;
    boolean b = viewHome.hasNode(name) ;
    session.logout();
    return b;
  }

  public Node getViewByName(String name, String repository,SessionProvider provider) throws Exception{          
    Session session = getSession(repository,provider) ;
    Node viewHome = (Node)session.getItem(baseViewPath_) ;
    return  viewHome.getNode(name);    
  }
  
  public void addView(String name, String permissions, String template, List tabs, 
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

  public Node getTemplateHome(String homeAlias, String repository,SessionProvider provider) throws Exception{
    String homePath = getJCRPath(homeAlias) ;
    Session session = getSession(repository,provider) ;
    return (Node)session.getItem(homePath) ;
  }

  private String getJCRPath(String jcrAlias) throws Exception{
    return cmsConfigurationService_.getJcrPath(jcrAlias) ;
  }


  private Session getSession(String repository) throws Exception {
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository) ;
    String worksapce = manageableRepository.getConfiguration().getDefaultWorkspaceName();
    return manageableRepository.getSystemSession(worksapce) ;    
  }
  
  private Session getSession(String repository,SessionProvider sessionProvider) throws Exception{
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository) ;
    String worksapce = manageableRepository.getConfiguration().getDefaultWorkspaceName();
    return sessionProvider.getSession(worksapce,manageableRepository) ;
  }

  public List<Node> getAllTemplates(String homeAlias, String repository,SessionProvider provider) throws Exception {
    Node templateHomNode = getTemplateHome(homeAlias, repository,provider) ;
    List<Node> list = new ArrayList<Node>() ;
    for(NodeIterator iter = templateHomNode.getNodes() ; iter.hasNext() ;) {
      list.add(iter.nextNode()) ;
    }
    return list;
  }

  public Node getTemplate(String path, String repository,SessionProvider provider) throws Exception{    
    return (Node)getSession(repository,provider).getItem(path) ;
  }

  public String addTemplate(String name, String content, String homeTemplate, String repository) throws Exception {
    Node templateHome = (Node)getSession(repository).getItem(homeTemplate) ;
    Node newTemp = null ;
    if(templateHome.hasNode(name)) {
      newTemp = templateHome.getNode(name) ;      
    }else {
      newTemp = templateHome.addNode(name,EXO_TEMPLATE) ;
    }
    newTemp.setProperty(TEMPLATE_PROP,content) ;
    templateHome.save() ;
    return newTemp.getPath() ;    
  }

  public void removeTemplate(String templatePath, String repository) throws Exception {
    Node selectedTemplate = (Node)getSession(repository).getItem(templatePath) ;
    Node parent = selectedTemplate.getParent() ;
    selectedTemplate.remove() ;
    parent.save() ;
    parent.getSession().save() ;    
  }

  private Node addView(Node viewManager, String name, String permissions, String template) throws Exception {
    Node contentNode = viewManager.addNode(name, "exo:view");
    contentNode.setProperty("exo:permissions", permissions);
    contentNode.setProperty("exo:template", template);  
    viewManager.save() ;

    return contentNode ;
  }
  
}
