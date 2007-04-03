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
import org.exoplatform.container.xml.PortalContainerInfo;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.services.cms.views.impl.ViewDataImpl.Tab;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.templates.velocity.impl.JCRResourceLoaderImpl;
/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Quang Hung
 *          nguyenkequanghung@yahoo.com
 * Feb 27, 2006
 */
public class ManageViewServiceImpl implements ManageViewService {

  protected final static String EXO_TEMPLATE = "exo:template".intern() ;
  protected final static String TEMPLATE_PROP = "exo:templateFile".intern() ;
  protected final static String ADMIN_VIEW = "admin".intern() ;
  protected final static String DEFAULT_VIEW = "default".intern() ;
  protected final static String EXO_PERMISSIONS = "exo:permissions".intern()  ;
  protected final static String BUTTON_PROP = "exo:buttons".intern() ;

  private ManageViewPlugin viewPlugin_ ;
  private String buttons_ ;
  private RepositoryService jcrService_ ;
  private CmsConfigurationService cmsConfigurationService_ ;
  private Session session_ ;
  private PortalContainerInfo containerInfo_ ;
  private CacheService cacheService_ ;
  
  public ManageViewServiceImpl(InitParams params, RepositoryService jcrService,
      CmsConfigurationService cmsConfigurationService, ManageDriveService dservice,
      PortalContainerInfo containerInfo, CacheService cacheService) throws Exception{
    containerInfo_ = containerInfo ;
    cacheService_ = cacheService ;
    jcrService_ = jcrService ;
    cmsConfigurationService_ = cmsConfigurationService ;
    ValueParam buttonParam = params.getValueParam("buttons") ;
    buttons_ = buttonParam.getValue() ;
    ManageableRepository jcrRepository = jcrService_.getRepository();
    session_ = jcrRepository.getSystemSession(cmsConfigurationService_.getWorkspace());
  }

  public void setManageViewPlugin(ManageViewPlugin viewPlugin) {
    viewPlugin_ = viewPlugin ;
  }

  public List getButtons(){
    if (buttons_ == null || buttons_.length() < 1) return null ;
    List buttonList = new ArrayList() ;
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

  public Node getViewHome() throws Exception {    
    String viewsPath = cmsConfigurationService_.getJcrPath(BasePath.CMS_VIEWS_PATH);
    return (Node) session_.getItem(viewsPath);
  }

  public List getAllViews() throws Exception {
    List viewList = new ArrayList() ;
    ViewDataImpl view ;
    Node viewNode ;
    String viewsPath = cmsConfigurationService_.getJcrPath(BasePath.CMS_VIEWS_PATH);
    Session session ;
    Node viewHome ;
    try{ 
      session = getUserSession() ;
      viewHome = (Node)session.getItem(viewsPath) ;
    }catch(Exception ex) {
      session = jcrService_.getRepository().getSystemSession(cmsConfigurationService_.getWorkspace()) ;
      viewHome = (Node)session.getItem(viewsPath) ;
    }
    for(NodeIterator iter = viewHome.getNodes(); iter.hasNext();) {
      view = new ViewDataImpl() ;
      viewNode = iter.nextNode() ;
      view.setName(viewNode.getName()) ;      
      view.setPermissions(viewNode.getProperty(EXO_PERMISSIONS).getString()) ;
      view.setTemplate(viewNode.getProperty(EXO_TEMPLATE).getString()) ;
      List<String> tabList = new ArrayList<String>() ;
      for(NodeIterator tabsIterator = viewNode.getNodes(); tabsIterator.hasNext(); ) {
        tabList.add(tabsIterator.nextNode().getName()) ;
      }
      view.setTabList(tabList) ;
      viewList.add(view) ;
    }
    return viewList ;    
  }

  public boolean hasView(String name) throws Exception {
    Node viewHome = getViewHome() ;
    return viewHome.hasNode(name) ;
  }
  
  public Node getViewByName(String name) throws Exception{          
    Node selectedView = getViewHome().getNode(name) ;
    return  selectedView;    
  }

  public Node getDefaultView() throws Exception {    
    return getViewHome().getNode(DEFAULT_VIEW) ;    
  }

  public Node getAdminView() throws Exception{    
    return getViewHome().getNode(ADMIN_VIEW) ;    
  }

  public void addView(String name, String permissions, String template, List tabs) throws Exception{
    Node viewHome = getViewHome() ;
    Node view ;
    if(viewHome.hasNode(name)) {
      view = viewHome.getNode(name) ;
      view.setProperty(EXO_PERMISSIONS, permissions) ;
      view.setProperty(EXO_TEMPLATE, template) ;
    }else {
      view = viewPlugin_.addView(viewHome, name, permissions, template) ;
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
      viewPlugin_.addTab(view, tabName, buttons) ;
    }
    viewHome.save() ;
  }

  public List<Node> getAllViewByPermission(String permission) throws Exception {
    List<Node> viewsByPermission = new ArrayList<Node>() ;
    for(NodeIterator iter = getViewHome().getNodes() ;iter.hasNext();) {
      Node view = iter.nextNode() ;
      String permissions = view.getProperty(EXO_PERMISSIONS).getString() ;
      if(permissions.indexOf(permission) > -1 ) 
        viewsByPermission.add(view) ;
    }
    return viewsByPermission ;
  }

  public void removeView(String viewName) throws Exception {
    Node viewHome = getViewHome() ;
    if(viewHome.hasNode(viewName)){
      Node view = viewHome.getNode(viewName) ;
      view.remove() ;
      viewHome.save() ;
      session_.save() ;
      session_.refresh(true) ;
    }              
  }

  public void addTab(Node view, String name, String buttons) throws Exception {
    viewPlugin_.addTab(view, name, buttons) ;
  }

  public Node getTemplateHome(String homeAlias) throws Exception{
    String homePath = getJCRPath(homeAlias) ;
    try{      
      Session session = getUserSession() ;
      return (Node)session.getItem(homePath) ;
    }catch(Exception e) {
      return (Node)session_.getItem(homePath) ;
    }
  }
  
  private String getJCRPath(String jcrAlias) throws Exception{
    return cmsConfigurationService_.getJcrPath(jcrAlias) ;
  }
  
  private Session getUserSession() throws Exception{
    try{
      return jcrService_.getRepository().login(cmsConfigurationService_.getWorkspace()) ;
    }catch (Exception e){
      return session_ ;
    }
  }
  
  public List<Node> getAllTemplates(String homeAlias) throws Exception {
    Node templateHomNode = getTemplateHome(homeAlias) ;
    List<Node> list = new ArrayList<Node>() ;
    for(NodeIterator iter = templateHomNode.getNodes() ; iter.hasNext() ;) {
      list.add(iter.nextNode()) ;
    }
    return list;
  }
  
  public Node getTemplate(String path) throws Exception{    
    Session session  = getUserSession() ;
    return (Node)session.getItem(path) ;        
  }
  
  public void addTemplate(String name, String content, String homeTemplate) throws Exception {
    Session session = getUserSession() ;
    Node templateHome = (Node)session.getItem(homeTemplate) ;
    Node newTemp = null ;
    if(templateHome.hasNode(name)) {
      newTemp = templateHome.getNode(name) ;      
    }else {
      newTemp = templateHome.addNode(name,EXO_TEMPLATE) ;
    }
    newTemp.setProperty(TEMPLATE_PROP,content) ;
    templateHome.save() ;
    //removeFromCache(newTemp.getPath()) ;
  }

  public void removeTemplate(String templatePath) throws Exception {    
    Session session = getUserSession() ;
    Node selectedTemplate = (Node)session.getItem(templatePath) ;
    Node parent = selectedTemplate.getParent() ;
    String path = selectedTemplate.getPath() ;
    selectedTemplate.remove() ;
    parent.save() ;
    session.save() ;
    //removeFromCache(path) ;
  }
  
  protected void removeFromCache(String templateName) {
    try{
      ExoCache jcrcache_ = cacheService_.getCacheInstance(JCRResourceLoaderImpl.class.getName()) ;
      String portalName = containerInfo_.getContainerName() ;
      String key = portalName + "jcr:" +templateName ; 
      Object cachedobject = jcrcache_.get(key);
      if (cachedobject != null) {
        jcrcache_.remove(key);      
      }
    }catch(Exception e) {      
    }
  }

}
