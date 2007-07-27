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
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.views.TemplateConfig;
import org.exoplatform.services.cms.views.ViewConfig;
import org.exoplatform.services.cms.views.ViewConfig.Tab;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;

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
  private boolean autoCreateInNewRepository_ = false ;
  private String repository_ ;

  public ManageViewPlugin(RepositoryService repositoryService, InitParams params, ConfigurationManager cservice, 
      CmsConfigurationService cmsConfigService) throws Exception {
    params_ = params ;
    repositoryService_ = repositoryService ;
    cmsConfigService_ = cmsConfigService ;
    cservice_ = cservice ;
    ValueParam autoInitParam = params.getValueParam("autoCreateInNewRepository") ;    
    if(autoInitParam !=null) {
      autoCreateInNewRepository_ = Boolean.parseBoolean(autoInitParam.getValue()) ;
    }
    ValueParam param = params.getValueParam("repository") ;
    if(param !=null) {
      repository_ = param.getValue();
    }
  }

  public void init() throws Exception {
    Iterator<ObjectParameter> it = params_.getObjectParamIterator() ;       
    String viewsPath = cmsConfigService_.getJcrPath(BasePath.CMS_VIEWS_PATH);
    String templatesPath = cmsConfigService_.getJcrPath(BasePath.CMS_VIEWTEMPLATES_PATH);    
    String warViewPath = cmsConfigService_.getContentLocation() 
    + "/system" + templatesPath.substring(templatesPath.lastIndexOf("exo:ecm") + 7) ;
    ViewConfig viewObject = null ;
    TemplateConfig templateObject = null ;
    Session session = null ;
    if(autoCreateInNewRepository_) {
      for(RepositoryEntry entry:repositoryService_.getConfig().getRepositoryConfigurations()) {
        session = getSession(entry.getName()) ;
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
      return ;
    }
    if(repository_ == null || repository_.length() == 0) return ;
    session = getSession(repository_) ;
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

  public void init(String repository) throws Exception {
    if(!autoCreateInNewRepository_) return ;
    Iterator<ObjectParameter> it = params_.getObjectParamIterator() ;       
    String viewsPath = cmsConfigService_.getJcrPath(BasePath.CMS_VIEWS_PATH);
    String templatesPath = cmsConfigService_.getJcrPath(BasePath.CMS_VIEWTEMPLATES_PATH);    
    String warViewPath = cmsConfigService_.getContentLocation() 
    + "/system" + templatesPath.substring(templatesPath.lastIndexOf("exo:ecm") + 7) ;
    Session session = getSession(repository) ;
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

  private Session getSession(String repository) throws Exception {
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository) ;    
    String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName() ;
    return manageableRepository.getSystemSession(workspace) ;
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
    String templateHomePath = cmsConfigService_.getJcrPath(alias) ;    
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
