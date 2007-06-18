package org.exoplatform.services.cms.views.impl;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;

public class ManageViewPlugin extends BaseComponentPlugin {

  private static String CB_PATH_TEMPLATE = "pathTemplate".intern() ;
  private static String CB_QUERY_TEMPLATE = "queryTemplate".intern() ;
  private static String CB_DETAIL_VIEW_TEMPLATE = "detailViewTemplate".intern() ;
  private static String CB_SCRIPT_TEMPLATE = "scriptTemplate".intern() ;
  private static String ECM_EXPLORER_TEMPLATE = "ecmExplorerTemplate".intern() ;
  private InitParams params_ ;
  private RepositoryService repositoryService_ ;
  private CmsConfigurationService cmsConfigService_ ; 
  private ConfigurationManager cservice_ ;
  public ManageViewPlugin(RepositoryService repositoryService, 
      InitParams params, ConfigurationManager cservice, 
      CmsConfigurationService cmsConfigService) throws Exception {
    params_ = params ;
    repositoryService_ = repositoryService ;
    cmsConfigService_ = cmsConfigService ;
    cservice_ = cservice ;
    //initRepository() ;
  }

  public void init() throws Exception {
    Iterator<ObjectParameter> it = params_.getObjectParamIterator() ;       
    String viewsPath = cmsConfigService_.getJcrPath(BasePath.CMS_VIEWS_PATH);
    String templatesPath = cmsConfigService_.getJcrPath(BasePath.CMS_VIEWTEMPLATES_PATH);
    Node viewManager = null ;
    String warViewPath = cmsConfigService_.getContentLocation() 
    + "/system" + templatesPath.substring(templatesPath.lastIndexOf("exo:ecm") + 7) ;
    while(it.hasNext()){
      Object object = it.next().getObject() ;
      ViewDataImpl data = null ;
      TemplateDataImpl temp = null ;
      if(object instanceof ViewDataImpl){
        data = (ViewDataImpl)object ;
        if(data.getAutoCreate()) {
          List<RepositoryEntry> repositories = repositoryService_.getConfig().getRepositoryConfigurations() ;
          for(RepositoryEntry repo : repositories) {
            viewManager = (Node)getSession(repo.getName()).getItem(viewsPath) ;
            String nodeName = data.getName() ;
            if(!viewManager.hasNode(nodeName)){
              Node view = addView(viewManager, nodeName, data.getPermissions(), data.getTemplate()) ;
              List tabList = data.getTabList() ;
              for(Iterator iter = tabList.iterator() ; iter.hasNext() ; ){
                ViewDataImpl.Tab tab = (ViewDataImpl.Tab) iter.next()  ;
                addTab(view, tab.getTabName(), tab.getButtons()) ;
              }
            }
            viewManager.save() ;
            viewManager.getSession().save() ;
          }
        }else {
          viewManager = (Node)getSession(data.getRepository()).getItem(viewsPath) ;
          String nodeName = data.getName() ;
          if(!viewManager.hasNode(nodeName)){
            Node view = addView(viewManager, nodeName, data.getPermissions(), data.getTemplate()) ;
            List tabList = data.getTabList() ;
            for(Iterator iter = tabList.iterator() ; iter.hasNext() ; ){
              ViewDataImpl.Tab tab = (ViewDataImpl.Tab) iter.next()  ;
              addTab(view, tab.getTabName(), tab.getButtons()) ;
            }
          }
          viewManager.save() ;
          viewManager.getSession().save() ;
        }        
      }else {
        temp = (TemplateDataImpl)object ;
        if(temp.getAutoCreate()) {
          List<RepositoryEntry> repositories = repositoryService_.getConfig().getRepositoryConfigurations() ;
          for(RepositoryEntry repo : repositories) {
            addTemplate(temp, getSession(repo.getName()), warViewPath, cservice_, cmsConfigService_) ;
          }
        } else {
          addTemplate(temp, getSession(temp.getRepository()), warViewPath, cservice_, cmsConfigService_) ;
        }
      }
    }
  }
  
  public void init(String repository) throws Exception {
    Iterator<ObjectParameter> it = params_.getObjectParamIterator() ;       
    String viewsPath = cmsConfigService_.getJcrPath(BasePath.CMS_VIEWS_PATH);
    String templatesPath = cmsConfigService_.getJcrPath(BasePath.CMS_VIEWTEMPLATES_PATH);
    Node viewManager = null ;
    String warViewPath = cmsConfigService_.getContentLocation() 
    + "/system" + templatesPath.substring(templatesPath.lastIndexOf("exo:ecm") + 7) ;    
    while(it.hasNext()){
      Object object = it.next().getObject() ;
      ViewDataImpl data = null ;
      TemplateDataImpl temp = null ;
      if(object instanceof ViewDataImpl){
        data = (ViewDataImpl)object ;
        if(data.getAutoCreate()) {
          viewManager = (Node)getSession(repository).getItem(viewsPath) ;
          String nodeName = data.getName() ;
          if(!viewManager.hasNode(nodeName)){
            Node view = addView(viewManager, nodeName, data.getPermissions(), data.getTemplate()) ;
            List tabList = data.getTabList() ;
            for(Iterator iter = tabList.iterator() ; iter.hasNext() ; ){
              ViewDataImpl.Tab tab = (ViewDataImpl.Tab) iter.next()  ;
              addTab(view, tab.getTabName(), tab.getButtons()) ;
            }
          }
          viewManager.save() ;
          viewManager.getSession().save() ;
        }
      }else {
        temp = (TemplateDataImpl)object ;
        if(temp.getAutoCreate()) {
          addTemplate(temp, getSession(repository), warViewPath, cservice_, cmsConfigService_) ;
        }
      }
    }
  }
  
  private Session getSession(String repository) throws Exception{
    String workspace = cmsConfigService_.getWorkspace(repository) ;
    return repositoryService_.getRepository(repository).getSystemSession(workspace) ;
  }
  
  private Node addView(Node viewManager, String name, String permissions, String template) throws Exception {
    Node contentNode = viewManager.addNode(name, "exo:view");
    contentNode.setProperty("exo:permissions", permissions);
    contentNode.setProperty("exo:template", template);  
    viewManager.save() ;

    return contentNode ;
  }

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

  private void addTemplate(TemplateDataImpl tempObject, Session session, String warViewPath, 
      ConfigurationManager cservice, CmsConfigurationService cmsConfigService) throws Exception {
    String name = tempObject.getName() ;
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
    String templateHomePath = cmsConfigService.getJcrPath(alias) ;    
    Node templateHomeNode = (Node)session.getItem(templateHomePath) ;        
    if(templateHomeNode.hasNode(name)) return  ;
    String warPath = warViewPath + tempObject.getWarPath() ;
    InputStream in = cservice.getInputStream(warPath) ;
    Node templateNode = templateHomeNode.addNode(name,"exo:template") ;
    templateNode.setProperty("exo:templateFile", in) ;
    templateHomeNode.save() ; 
    session.save() ;
  }
}
