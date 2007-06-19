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
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.impl.Utils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;

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
  private CmsConfigurationService cmsConfigService_;
  private String cmsTemplatesBasePath_ ; 
  private InitParams params_ ;

  public TemplatePlugin(InitParams params, RepositoryService jcrService, ConfigurationManager configManager,
      CmsConfigurationService cmsConfigService) throws Exception {
    cmsConfigService_ = cmsConfigService;
    repositoryService_ = jcrService;
    configManager_ = configManager;
    cmsTemplatesBasePath_ = cmsConfigService_.getJcrPath(BasePath.CMS_TEMPLATES_PATH) ;
    params_ = params ;    
  }
  
  public void init() throws Exception {
    Session session = null ;    
    Iterator<ObjectParameter> it = params_.getObjectParamIterator() ;
    TemplateConfig templateConfig = null ;
    
    while(it.hasNext()) {
      Object obj = it.next().getObject() ;
      if(obj instanceof TemplateConfig) {
        templateConfig = (TemplateConfig)obj ;
        if(templateConfig.getAutoCreate()) {
          List<RepositoryEntry> repositories = repositoryService_.getConfig().getRepositoryConfigurations() ;
          for(RepositoryEntry repo : repositories) {
            session = repositoryService_.getRepository(repo.getName()).
              getSystemSession(cmsConfigService_.getWorkspace(repo.getName()));
            addTemplate(templateConfig, session) ;
          }        
        }else {
          session = repositoryService_.getRepository("repository").
            getSystemSession(cmsConfigService_.getWorkspace("repository"));
          addTemplate(templateConfig, session) ;
        }
      }
    }    
  }
  
  public void init(String repository) throws Exception{
    Session session = null ;    
    Iterator<ObjectParameter> it = params_.getObjectParamIterator() ;
    TemplateConfig templateConfig = null ;
    while(it.hasNext()) {
      templateConfig = (TemplateConfig)it.next().getObject() ;
      if(templateConfig.getAutoCreate()) {
        session = repositoryService_.getRepository(repository).
          getSystemSession(cmsConfigService_.getWorkspace(repository));
        addTemplate(templateConfig, session) ;
      }
    }   
  }
  private void addTemplate(TemplateConfig templateConfig, Session session) throws Exception{
    List nodetypes = templateConfig.getNodeTypes();
    TemplateConfig.NodeType nodeType = null ;
    String location = templateConfig.getLocation();  
    Node templatesHome = Utils.makePath(session.getRootNode(), cmsTemplatesBasePath_, NT_UNSTRUCTURED);
    String sourcePath = cmsConfigService_.getContentLocation() + "/system" 
    + cmsTemplatesBasePath_.substring(cmsTemplatesBasePath_.lastIndexOf("/")) ;
    if (location.equals("jar")) 
      sourcePath = "jar:/conf/system" + cmsTemplatesBasePath_.substring(cmsTemplatesBasePath_.lastIndexOf("/")) ;
    Iterator iter = nodetypes.iterator() ;
    while(iter.hasNext()) {
      nodeType = (TemplateConfig.NodeType) iter.next();
      Node nodeTypeHome = null;
      if (!templatesHome.hasNode(nodeType.getNodetypeName())){
        nodeTypeHome = Utils.makePath(templatesHome, nodeType.getNodetypeName(),NT_UNSTRUCTURED);
        if(nodeType.getDocumentTemplate())
          nodeTypeHome.setProperty(DOCUMENT_TEMPLATE_PROP, true) ;
        else
          nodeTypeHome.setProperty(DOCUMENT_TEMPLATE_PROP, false) ;
      } else{
        nodeTypeHome = templatesHome.getNode(nodeType.getNodetypeName());
      }
      nodeTypeHome.setProperty(TEMPLATE_LABEL, nodeType.getLabel()) ;
      
      List dialogs = nodeType.getReferencedDialog();
      Node dialogsHome = Utils.makePath(nodeTypeHome, DIALOGS, NT_UNSTRUCTURED);
      addNode(sourcePath, dialogsHome, dialogs);
      
      List views = nodeType.getReferencedView();
      Node viewsHome = Utils.makePath(nodeTypeHome, VIEWS, NT_UNSTRUCTURED);
      addNode(sourcePath, viewsHome, views);
    }
    session.save() ;
  }
  
  public void setBasePath(String basePath) { cmsTemplatesBasePath_ = basePath ; }
  
  private void addNode(String basePath, Node nodeTypeHome, List templates)  throws Exception {
    for (Iterator iterator = templates.iterator(); iterator.hasNext();) {
      TemplateConfig.Template template = (TemplateConfig.Template) iterator.next();
      String templateFileName = template.getTemplateFile();
      String path = basePath + templateFileName;
      InputStream in = configManager_.getInputStream(path);
      String nodeName = 
        templateFileName.substring(templateFileName.lastIndexOf("/") + 1, templateFileName.indexOf("."));
      Node contentNode = null ;
      if(nodeTypeHome.hasNode(nodeName)) {
        contentNode = nodeTypeHome.getNode(nodeName);
      }else {
        contentNode = nodeTypeHome.addNode(nodeName, EXO_TEMPLATE);
      }
      contentNode.setProperty(EXO_ROLES_PROP, template.getParsedRoles());
      contentNode.setProperty(EXO_TEMPLATE_FILE_PROP, in);
    }
  }  
}
