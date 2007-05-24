package org.exoplatform.services.cms.metadata;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.impl.Utils;
import org.exoplatform.services.cms.templates.impl.TemplateConfig;
import org.exoplatform.services.cms.templates.impl.TemplatePlugin;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.picocontainer.Startable;

/**
 * @author Hung Nguyen Quang
 * @mail   nguyenkequanghung@yahoo.com
 */

public class MetadataServiceImpl implements MetadataService, Startable{
  
  final static public String NT_UNSTRUCTURED = "nt:unstructured" ;
  final static public String EXO_TEMPLATE = "exo:template" ;
  final static public String EXO_ROLES_PROP = "exo:roles" ;
  final static public String EXO_TEMPLATE_FILE_PROP = "exo:templateFile" ;
  final static public String INTERNAL_USE = "exo:internalUse".intern() ;
  final static public String METADATA_TYPE = "exo:metadata".intern() ;
  final static public String DIALOGS = "dialogs" ;
  final static public String VIEWS = "views" ;
  final static public String DIALOG1 = "dialog1" ;
  final static public String VIEW1 = "view1" ;
  
  private RepositoryService repositoryService_;
  private CmsConfigurationService cmsConfigService_ ;
  private ConfigurationManager configManager_ ;
  private Session session_ ;
  private List<TemplatePlugin> plugins_;
  
  public MetadataServiceImpl(CmsConfigurationService cmsConfigService, 
      RepositoryService repositoryService, ConfigurationManager configManager) throws Exception{
    cmsConfigService_ = cmsConfigService ;
    repositoryService_ = repositoryService ;
    configManager_ = configManager ;    
    plugins_ = new ArrayList<TemplatePlugin>();
  }
  
  public void start() {
    try {      
      init() ;
    } catch (Exception e) {
      e.printStackTrace() ;
    }    
  }
  
  public void stop() {}
  
  public void addPlugins(ComponentPlugin plugin) {
    if (plugin instanceof TemplatePlugin) plugins_.add((TemplatePlugin) plugin);    
  }
  
  private void init() throws Exception{
    session_ = repositoryService_.getDefaultRepository().getSystemSession(cmsConfigService_.getWorkspace()) ;
    String metadataPath = cmsConfigService_.getJcrPath(BasePath.METADATA_PATH);
    Node root = session_.getRootNode();
    Node metadataHome = null ;
    List nodetypes = null;
    for (int j = 0; j < plugins_.size(); j++) {
      nodetypes = plugins_.get(j).getNodeTypes();
      if (nodetypes.isEmpty()) return;
      TemplateConfig.NodeType nodeType = (TemplateConfig.NodeType) nodetypes.iterator().next();
      String nodeTypeName = nodeType.getNodetypeName();
      try {
        metadataHome = (Node)session_.getItem(metadataPath);
      } catch (PathNotFoundException e) {
      }
      String sourcePath = cmsConfigService_.getContentLocation() + "/system" 
                          + metadataPath.substring(metadataPath.lastIndexOf("/")) ;
      
      for (Iterator iter = nodetypes.iterator(); iter.hasNext();) {
        nodeType = (TemplateConfig.NodeType) iter.next();
        nodeTypeName = nodeType.getNodetypeName();
        Node nodeTypeHome = null;
        if (!metadataHome.hasNode(nodeTypeName)){
          nodeTypeHome = Utils.makePath(metadataHome, nodeTypeName, NT_UNSTRUCTURED);          
        } else {
          nodeTypeHome = metadataHome.getNode(nodeTypeName);
        }
        List dialogs = nodeType.getReferencedDialog();
        if(dialogs.size() > 0) {
          Node dialogsHome = createTemplateHome(nodeTypeHome, true);
          addNode(sourcePath, dialogsHome, dialogs);
        }
        List views = nodeType.getReferencedView();
        if(views.size() > 0) {
          Node viewsHome = createTemplateHome(nodeTypeHome, false);
          addNode(sourcePath, viewsHome, views);
        }
      }
    }
    root.save();
  }
  
  private void addNode(String metadataPath, Node templateHome, List templates)  throws Exception {
    for (Iterator iterator = templates.iterator(); iterator.hasNext();) {
      TemplateConfig.Template template = (TemplateConfig.Template) iterator.next();
      String templateFileName = template.getTemplateFile();
      String path = metadataPath + templateFileName;
      InputStream in = configManager_.getInputStream(path);
      String nodeName = 
        templateFileName.substring(templateFileName.lastIndexOf("/") + 1, templateFileName.indexOf("."));
      Node contentNode = templateHome.addNode(nodeName, EXO_TEMPLATE);
      contentNode.setProperty(EXO_ROLES_PROP, template.getParsedRoles());
      contentNode.setProperty(EXO_TEMPLATE_FILE_PROP, in);
    }
  }
  
  public void addMetadata(String nodetype, boolean isDialog, String role, String content, boolean isAddNew) throws Exception {    
    String metadataPath = cmsConfigService_.getJcrPath(BasePath.METADATA_PATH);
    Node metadataHome = (Node)session_.getItem(metadataPath) ;
    if(!isAddNew) {
      if(isDialog){
        Node dialog1 = metadataHome.getNode(nodetype).getNode(DIALOGS).getNode(DIALOG1) ;
        dialog1.setProperty(EXO_ROLES_PROP, role.split(";"));
        dialog1.setProperty(EXO_TEMPLATE_FILE_PROP, content);
        dialog1.save() ;
      } else {
        Node view1 = metadataHome.getNode(nodetype).getNode(VIEWS).getNode(VIEW1) ;
        view1.setProperty(EXO_ROLES_PROP, role.split(";"));
        view1.setProperty(EXO_TEMPLATE_FILE_PROP, content);
        view1.save() ;
      }      
    } else {
      Node metadata = null ;
      if(metadataHome.hasNode(nodetype)) metadata = metadataHome.getNode(nodetype) ;
      else metadata = metadataHome.addNode(nodetype, NT_UNSTRUCTURED) ;
      addTemplate(metadata, role, content, isDialog) ;
      metadataHome.save() ;
    }    
    session_.save() ;    
  }
  
  private void addTemplate(Node nodetype, String role, String content, boolean isDialog) throws Exception {
    Node templateHome = createTemplateHome(nodetype, isDialog) ;
    Node template = null ;
    if(isDialog) {
      if(templateHome.hasNode(DIALOG1)) template = templateHome.getNode(DIALOG1) ;
      else template = templateHome.addNode(DIALOG1, EXO_TEMPLATE) ;
    } else {
      if(templateHome.hasNode(VIEW1)) template = templateHome.getNode(VIEW1) ;
      else template = templateHome.addNode(VIEW1, EXO_TEMPLATE) ;
    }    
    template.setProperty(EXO_ROLES_PROP, role.split(";")) ;
    template.setProperty(EXO_TEMPLATE_FILE_PROP, content) ;
  }
  
  public void removeMetadata(String nodetype) throws Exception {
    String metadataPath = cmsConfigService_.getJcrPath(BasePath.METADATA_PATH);
    Node metadataHome = (Node)session_.getItem(metadataPath) ;
    Node metadata = metadataHome.getNode(nodetype) ; 
    metadata.remove() ;
    metadataHome.save() ;
    session_.save() ;
  } 
  
  public List<String> getMetadataList() throws Exception {
    List<String> metadataTypes = new ArrayList<String>() ;
    for(NodeType metadata:getAllMetadatasNodeType()) {
      metadataTypes.add(metadata.getName()) ;
    }
    return metadataTypes ;
  }
  
  public List<NodeType> getAllMetadatasNodeType() throws Exception {
    List<NodeType> metadataTypes = new ArrayList<NodeType>() ;    
    ExtendedNodeTypeManager ntManager = repositoryService_.getDefaultRepository().getNodeTypeManager();     
    NodeTypeIterator ntIter = ntManager.getMixinNodeTypes() ;
    while(ntIter.hasNext()) {
      NodeType nt = ntIter.nextNodeType() ;
      if(nt.isNodeType(METADATA_TYPE)) metadataTypes.add(nt) ;
    }
    return metadataTypes ;
  }
  
  private Node createTemplateHome(Node nodetype, boolean isDialog) throws Exception{
    if(isDialog) {
      Node dialogs = null ;
      if(nodetype.hasNode(DIALOGS)) dialogs = nodetype.getNode(DIALOGS) ;      
      else dialogs = nodetype.addNode(DIALOGS, NT_UNSTRUCTURED) ;
      return dialogs ;
    }
    Node views = null ;
    if(nodetype.hasNode(VIEWS)) views = nodetype.getNode(VIEWS) ;      
    else views = nodetype.addNode(VIEWS, NT_UNSTRUCTURED) ;
    return views ;    
  }
  
  public String getMetadataTemplate(String name, boolean isDialog) throws Exception {
    String metadataPath = cmsConfigService_.getJcrPath(BasePath.METADATA_PATH);
    Node metadataHome = (Node)session_.getItem(metadataPath) ;
    Node template = null ;
    if(!hasMetadata(name)) return null;
    if(isDialog) template = metadataHome.getNode(name).getNode(DIALOGS).getNode(DIALOG1) ;
    else template = metadataHome.getNode(name).getNode(VIEWS).getNode(VIEW1) ;
    return template.getProperty(EXO_TEMPLATE_FILE_PROP).getString();
  }
  
  public String getMetadataPath(String name, boolean isDialog) throws Exception {
    String metadataPath = cmsConfigService_.getJcrPath(BasePath.METADATA_PATH);
    Node metadataHome = (Node)session_.getItem(metadataPath) ;
    if(!hasMetadata(name)) return null;
    Node template = null ;
    if(isDialog){
      template = metadataHome.getNode(name).getNode(DIALOGS).getNode(DIALOG1) ;
    } else {
      template = metadataHome.getNode(name).getNode(VIEWS).getNode(VIEW1) ;
    }
    return template.getPath();
  }
  
  public String getMetadataRoles(String name, boolean isDialog) throws Exception {
    String metadataPath = cmsConfigService_.getJcrPath(BasePath.METADATA_PATH);
    Node metadataHome = (Node)session_.getItem(metadataPath) ;
    Node template = null ;
    if(!hasMetadata(name)) return null;
    if(isDialog){
      template = metadataHome.getNode(name).getNode(DIALOGS).getNode(DIALOG1) ;
    } else {
      template = metadataHome.getNode(name).getNode(VIEWS).getNode(VIEW1) ;
    }
    Value[] values = template.getProperty(EXO_ROLES_PROP).getValues() ;
    StringBuffer roles = new StringBuffer() ;
    for(int i = 0 ; i < values.length ; i ++ ){
      if(roles.length() > 0 )roles.append("; ") ;
      roles.append(values[i].getString()) ;
    }
    return roles.toString();
  }  
  
  public boolean hasMetadata(String name) throws Exception {
    String metadataPath = cmsConfigService_.getJcrPath(BasePath.METADATA_PATH);
    Node metadataHome = (Node)session_.getItem(metadataPath) ;
    if(metadataHome.hasNode(name)) return true ;
   return false ; 
  }

  public List<String> getExternalMetadataType() throws Exception {
    List<String> extenalMetaTypes = new ArrayList<String>() ;
    for(NodeType metadata: getAllMetadatasNodeType()) {      
      ExtendedNodeType extNT = (ExtendedNodeType)metadata ;
      PropertyDefinition internalUseDef = extNT.getPropertyDefinitions(INTERNAL_USE).getAnyDefinition() ;
      if(!internalUseDef.getDefaultValues()[0].getBoolean()) extenalMetaTypes.add(metadata.getName()) ;
    }
    return extenalMetaTypes ;
  }
  
  public NodeType getMetadataTypeByName(String metadataTypeName) throws Exception {
    NodeType metadataType = repositoryService_.getDefaultRepository().getNodeTypeManager().getNodeType(metadataTypeName) ;
    if(metadataType.isNodeType(METADATA_TYPE)) return metadataType ;
    return null ;  
  }
}
