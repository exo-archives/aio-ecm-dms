/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.ecm.jcr.AlphaNodeComparator;
import org.exoplatform.ecm.jcr.CommentsComponent;
import org.exoplatform.ecm.jcr.ECMViewComponent;
import org.exoplatform.ecm.jcr.JCRExceptionManager;
import org.exoplatform.ecm.jcr.PropertiesComparator;
import org.exoplatform.ecm.jcr.TypeNodeComparator;
import org.exoplatform.ecm.jcr.VoteComponent;
import org.exoplatform.ecm.jcr.model.Preference;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.portal.component.view.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.comments.CommentsService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.cms.voting.VotingService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.component.UIRightClickPopupMenu;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Sep 3, 2006
 * 10:07:15 AM
 * Editor : Pham Tuan
 *          phamtuanchip@gmail.com
 * Nov 10, 2006         
 */
@ComponentConfig(
    events = {
        @EventConfig(listeners = UIDocumentInfo.ChangeNodeActionListener.class),
        @EventConfig(listeners = UIDocumentInfo.ViewNodeActionListener.class),
        @EventConfig(listeners = UIDocumentInfo.SortActionListener.class),
        @EventConfig(listeners = UIDocumentInfo.VoteActionListener.class),
        @EventConfig(listeners = UIDocumentInfo.ChangeLanguageActionListener.class)
    }
)
public class UIDocumentInfo extends UIComponent implements ECMViewComponent, VoteComponent, CommentsComponent {
  public static final String DEFAULT_LANGUAGE = "default".intern() ;
  
  private String typeSort_ ;
  private String typeSortOrder_ ;
  private String nameSortOrder_ ;
  private String language_ = DEFAULT_LANGUAGE ;
  private Node currentNode_ ;

  public UIDocumentInfo() throws Exception {}

  public String getTemplate() {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    String userName = Util.getUIPortal().getOwner() ;
    try {
      String nodeType = uiExplorer.getCurrentNode().getPrimaryNodeType().getName() ;
      if(uiExplorer.getPreference().isJcrEnable()) return uiExplorer.getDocumentInfoTemplate();
      else if(isNodeTypeSupported(nodeType)) return templateService.getTemplatePathByUser(false, nodeType, userName) ;
    } catch(Exception e) {
      e.printStackTrace() ;
    }
    String temp = uiExplorer.getDocumentInfoTemplate() ;
    return temp; 
  }

  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return getAncestorOfType(UIJCRExplorer.class).getJCRTemplateResourceResolver() ;
  }
  
  public UIRightClickPopupMenu getContextMenu() {
    return getAncestorOfType(UIWorkingArea.class).getChild(UIRightClickPopupMenu.class) ;
  }
  
  public Node getNodeByUUID(String uuid) throws Exception{
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    Session session = uiExplorer.getSession() ;
    return session.getNodeByUUID(uuid);
  }

  public List<String> getMultiValues(Node node, String name) throws Exception {
    return getAncestorOfType(UIJCRExplorer.class).getMultiValues(node, name) ;
  }
  
  public String getDownloadLink(Node node) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource dresource ;
    if(!node.getPrimaryNodeType().getName().equals("nt:file")) return null; 
    Node jcrContentNode = node.getNode("jcr:content") ;
    InputStream input = jcrContentNode.getProperty("jcr:data").getStream() ;
    dresource = new InputStreamDownloadResource(input, "image") ;
    dresource.setDownloadName(node.getName()) ;
    return dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
  }
  
  public String getImage(Node node) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource dresource ;
    Node imageNode = node.getNode("exo:image") ;
    InputStream input = imageNode.getProperty("jcr:data").getStream() ;
    dresource = new InputStreamDownloadResource(input, "image") ;
    dresource.setDownloadName(node.getName()) ;
    return dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
  }
  
  public String getWebDAVServerPrefix() throws Exception {    
    PortletRequestContext portletRequestContext = PortletRequestContext.getCurrentInstance() ;
    String prefixWebDAV = portletRequestContext.getRequest().getScheme() + "://" + 
                          portletRequestContext.getRequest().getServerName() + ":" +
                          String.format("%s",portletRequestContext.getRequest().getServerPort()) ;
    return prefixWebDAV ;
  }
  
  public Node getViewNode(String nodeType) throws Exception {
    return getAncestorOfType(UIJCRExplorer.class).getCurrentNode().getNode(nodeType) ;
  }
  
  public String getActionsList(Node node) throws Exception {
    return getAncestorOfType(UIWorkingArea.class).getActionsList(node) ;
  }
  
  public List<Node> getCustomActions(Node node) throws Exception {
    return getAncestorOfType(UIWorkingArea.class).getCustomActions(node) ;
  }
  
  public boolean isPreferenceNode(Node node) throws Exception {
    return getAncestorOfType(UIWorkingArea.class).isPreferenceNode(node) ;
  }
  
  public boolean isReadAuthorized(ExtendedNode node) throws Exception {
    return getAncestorOfType(UIWorkingArea.class).isReadAuthorized(node) ;
  }
  
  @SuppressWarnings("unchecked")
  public Object getComponentInstanceOfType(String className) {
    Object service = null;
    try {
      ClassLoader loader =  Thread.currentThread().getContextClassLoader();
      Class clazz = loader.loadClass(className);
      service = getApplicationComponent(clazz);
    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
    } 
    return service;
  }
  
  public String getNodeOwner(Node node) throws RepositoryException { 
    return ((ExtendedNode) node).getACL().getOwner();
  }
  
  public String getNodePath(Node node) throws Exception {
    return node.getPath() ;
  }
  
  public boolean isVersionable(Node node) throws RepositoryException {
    return  getAncestorOfType(UIWorkingArea.class).isVersionable(node) ;
  }
  
  public List<Node> getRelations() throws Exception {
    List<Node> relations = new ArrayList<Node>() ;
    if (getNode().hasProperty("exo:relation")) {
      Value[] vals = getNode().getProperty("exo:relation").getValues();
      for (int i = 0; i < vals.length; i++) {
        String uuid = vals[i].getString();
        Node node = getNodeByUUID(uuid);
        relations.add(node);
      }
    }
    return relations;
  }

  public List<Node> getAttachments() throws Exception {
    List<Node> attachments = new ArrayList<Node>() ;
    NodeIterator childrenIterator = getNode().getNodes();;
    while (childrenIterator.hasNext()) {
      Node childNode = childrenIterator.nextNode();
      String nodeType = childNode.getPrimaryNodeType().getName();
      if ("nt:file".equals(nodeType)) attachments.add(childNode);
    }
    return attachments;
  }

  public boolean isNodeTypeSupported(String nodeTypeName) {
    try {      
      TemplateService templateService = getApplicationComponent(TemplateService.class);
      return templateService.isManagedNodeType(nodeTypeName);
    } catch (Exception e) {
      return false;
    }
  }
  
  public String getNodeType() throws Exception { return null; }

  public List<String> getSupportedLocalise() throws Exception {
    List<String> local = new ArrayList<String>() ;
    if(getNode().hasNode("languages")){
      Node languages = getNode().getNode("languages") ;
      NodeIterator iter = languages.getNodes() ;
      while(iter.hasNext()) {
        local.add(iter.nextNode().getName()) ;
      }
      local.add(getNode().getProperty("exo:language").getString()) ;      
    } 
    return local ;
  }

  public String getTemplatePath() throws Exception { return null; }

  public boolean isNodeTypeSupported() { return false; }
  
  public String getVersionName(Node node) throws Exception {
    return node.getBaseVersion().getName() ;
  }
  
  public void setNode(Node node) { 
    try {
      getAncestorOfType(UIJCRExplorer.class).setSelectNode(node) ;
    } catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  public boolean hasPropertyContent(Node node, String property){
    try {
      String value = node.getProperty(property).getString() ;
      if(value.length() > 0) return true ;
    } catch (Exception e) {
      e.printStackTrace() ;      
    }
    return false ;
  }
  
  public boolean isRssLink() { return false ; }
  public String getRssLink() { return null ; }
  
  public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ;  
  }

  public String getWorkspaceName() throws Exception {
    return getNode().getSession().getWorkspace().getName();
  }

  public Node getNode() throws Exception { 
    Node node = getAncestorOfType(UIJCRExplorer.class).getCurrentNode() ;  
    currentNode_ = node ;
    if(currentNode_.hasProperty("exo:language")) {
      String defaultLang = currentNode_.getProperty("exo:language").getString() ;
      if(!language_.equals("default") && !language_.equals(defaultLang)) {
        Node curNode = currentNode_.getNode("languages/" + language_) ;
        language_ = defaultLang ;
        return curNode ;
      } 
    }    
    return currentNode_; 
  }
  
  public String getIcons(Node node, String size) throws Exception {
    return Utils.getNodeTypeIcon(node, size) ;
  }
  public double getRating() throws Exception {
    return currentNode_.getProperty("exo:votingRate").getDouble();
  }

  public long getVoteTotal() throws Exception {
    return currentNode_.getProperty("exo:voteTotal").getLong();
  }

  public List<Node> getComments() throws Exception {
    return getApplicationComponent(CommentsService.class).getComments(currentNode_, getLanguage()) ;
  }

  public String getCommentTemplate() throws Exception {
    TemplateService tempServ = getApplicationComponent(TemplateService.class) ;
    return tempServ.getTemplatePath(false, "exo:comment", "view1") ;
  }

  public String getVoteTemplate() throws Exception {
    TemplateService tempServ = getApplicationComponent(TemplateService.class) ;
    return tempServ.getTemplatePath(false, "exo:vote", "view1") ;
  }

  //TODO:  Need to use Comparator,  You can call me  when when you are working on this
  @SuppressWarnings("unchecked")
  private Map<String, Node> sortByAlphaBeta(List<Node> childrenList) throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    Preference pref = uiExplorer.getPreference();
    String order = pref.getOrder() ;
    Map<String, Node> nodesMap = new TreeMap(new AlphaNodeComparator(order));
    String nodeType = "" ;
    int j = 0 ;
    for(Node childNode : childrenList) {
      nodeType = childNode.getPrimaryNodeType().getName();
      // unstructured, taxonomy node considered as folder node for sorting
      if (Utils.NT_UNSTRUCTURED.equals(nodeType) || Utils.EXO_TAXANOMY.equals(nodeType)) nodeType = Utils.NT_FOLDER;          
      if (Utils.NT_FOLDER.equals(nodeType)) nodeType = "1";
      else nodeType = "2";
      String key = (nodeType + childNode.getName() + Integer.toString(j)).toLowerCase() ;
      nodesMap.put(key, childNode);
      j++;
    }
    return nodesMap ;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Node> sortByNodeType(List<Node> childrenList) throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    Preference pref = uiExplorer.getPreference();
    String order = pref.getOrder() ;
    Map<String, Node> nodesMap = new TreeMap(new TypeNodeComparator(order));
    int j = 0 ;
    for(Node childNode : childrenList) {
      Node jcrContentNode = null;
      String nodeType = Utils.DEFAULT;
      String mimeType = Utils.DEFAULT;        
      nodeType = childNode.getPrimaryNodeType().getName();
      // unstructured, taxonomy node considered as folder node for sorting
      if (Utils.NT_UNSTRUCTURED.equals(nodeType) || Utils.EXO_TAXANOMY.equals(nodeType)) nodeType = Utils.NT_FOLDER ;          
      if (Utils.NT_FILE.equals(nodeType)) jcrContentNode = childNode.getNode(Utils.JCR_CONTENT);
      else if (Utils.NT_RESOURCE.equals(nodeType)) jcrContentNode = childNode;
      if (jcrContentNode != null && jcrContentNode.hasProperty(Utils.JCR_MIMETY)) {
        mimeType = jcrContentNode.getProperty(Utils.JCR_MIMETY).getString();
      }
      // 2 node types for sorting : folder or file
      if (Utils.NT_FOLDER.equals(nodeType)) nodeType = "folder";
      else nodeType = "file";
      // mime type if available. Ex : pdf for application/pdf
      if (!Utils.DEFAULT.equals(mimeType)) {
        StringTokenizer strTk = new StringTokenizer(mimeType, "/");
        if (strTk.countTokens() == 2) {
          strTk.nextToken();
          mimeType = strTk.nextToken();
        }
      }
      String key = nodeType + "//" + mimeType + "//" + childNode.getName() + Integer.toString(j) ;
      nodesMap.put(key.toLowerCase(), childNode);
      j++;
    }
    return nodesMap ;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Node> sortByProperty(List<Node> childrenList) throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    Preference pref = uiExplorer.getPreference();
    String property = pref.getProperty() ;
    String order = pref.getOrder() ;
    Map<String, Node> nodesMap = new TreeMap(new PropertiesComparator(order));
    String indexList = "2" ;
    String remainList = "1" ;
    int j = 0 ;
    if(order.equals("Ascending")) {
      indexList = "1" ;
      remainList = "2" ;
    } 
    List<Node> remainNodes = new ArrayList<Node>() ;
    for(Node childNode : childrenList) {
      if(childNode.hasProperty(property)) {
        NodeType nt = childNode.getPrimaryNodeType() ;
        PropertyDefinition[] propertiesDef = nt.getPropertyDefinitions() ;
        try {
          for(int i = 0; i < propertiesDef.length; i ++) {
            if(propertiesDef[i].getName().equals(property)) {
              int type = propertiesDef[i].getRequiredType() ;
              if(type == 1) { //String
                String strProperty = childNode.getProperty(property).getString() ;
                String key = (indexList + strProperty + Integer.toString(j)).toLowerCase() ;
                nodesMap.put(key, childNode);
              } else if(type == 3 ) { //Long
                String value = String.valueOf(childNode.getProperty(property).getLong()) ;
                String key = (indexList + value + Integer.toString(j)).toLowerCase() ;
                nodesMap.put(key, childNode);
              } else if(type == 5) { //Date
                String value = 
                  String.valueOf(childNode.getProperty(property).getDate().getTimeInMillis()) ;
                String key = (indexList + value + Integer.toString(j)).toLowerCase() ;
                nodesMap.put(key, childNode);
              } else if(type == 6) { //Boolean
                String value = String.valueOf(childNode.getProperty(property).getBoolean()) ;
                String key = (indexList + value + Integer.toString(j)).toLowerCase() ;
                nodesMap.put(key, childNode);
              } else {
                remainNodes.add(childNode) ;
                break ;
              }
            }
          }
        } catch(Exception ex) {
          remainNodes.add(childNode) ;
          break ;
        }
      } else {
        remainNodes.add(childNode) ;
      }
      j++;
    }
    for(int i = 0; i < remainNodes.size(); i ++) {
      Node node = remainNodes.get(i) ;
      String key = (remainList + node.getName() + Integer.toString(j)).toLowerCase() ;
      nodesMap.put(key, node);
      j++ ;
    }
    return nodesMap ;
  }

  public Iterator getChildrenList() throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    Preference pref = uiExplorer.getPreference();
    List<Node> childrenList = 
      uiExplorer.getChildrenList(uiExplorer.getCurrentNode(), pref.isShowPreferenceDocuments()) ;
    Map<String,Node> nodesMap;
    if (Preference.ALPHABETICAL_SORT.equals(pref.getSort())) { nodesMap = sortByAlphaBeta(childrenList) ; }
    else if(Preference.PROPERTY_SORT.equals(pref.getSort())) { nodesMap = sortByProperty(childrenList) ; }
    else { nodesMap = sortByNodeType(childrenList) ; }
    return nodesMap.values().iterator();
  }

  public String getTypeSort() { 
    if(typeSort_ == null) return Preference.TYPE_SORT ;
    return typeSort_ ; 
  }

  public String getTypeSortOrder() { 
    if(typeSortOrder_ == null) return Preference.ASCENDING_ORDER ;
    return typeSortOrder_ ;  
  }
  public String getNameSortOrder() { 
    if(nameSortOrder_ == null) return Preference.ASCENDING_ORDER ;
    return nameSortOrder_ ;  
  }
  
  public void setLanguage(String language) { language_ = language ; }
  public String getLanguage() { return language_ ; }

  static  public class ViewNodeActionListener extends EventListener<UIDocumentInfo> {
    public void execute(Event<UIDocumentInfo> event) throws Exception {
      UIDocumentInfo uicomp = event.getSource() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class);      
      String uri = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String workspaceName = event.getRequestContext().getRequestParameter("workspaceName") ;
      Session session ;
      if(workspaceName == null ) {
        session = uiExplorer.getSession() ;
      } else {
        RepositoryService repositoryService  = uicomp.getApplicationComponent(RepositoryService.class) ;
        session = repositoryService.getRepository().getSystemSession(workspaceName) ;
      }
      uiExplorer.setSelectNode(uri, session) ;
      uiExplorer.updateAjax(event) ;
    }
  }

  static  public class ChangeNodeActionListener extends EventListener<UIDocumentInfo> {
    public void execute(Event<UIDocumentInfo> event) throws Exception {
      UIDocumentInfo uicomp =  event.getSource() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ; 
      String uri = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String workspaceName = event.getRequestContext().getRequestParameter("workspaceName") ;
      Session session = uiExplorer.getSessionByWorkspace(workspaceName);
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class) ;
      String prefPath = uiExplorer.getPreferencesPath() ;
      String prefWorkspace = uiExplorer.getPreferencesWorkspace() ;
      if((prefPath.length() > 0) && (uiExplorer.getCurrentWorkspace().equals(prefWorkspace))) {
        if(!uri.contains(prefPath)) {         
          JCRExceptionManager.process(uiApp,new PathNotFoundException());
          return ;
        }
        try {
          if ((".." + prefPath).equals(uri)) {
            if (prefPath.equals(uiExplorer.getCurrentNode().getPath())) {
              uiExplorer.setSelectNode(uiExplorer.getCurrentNode().getParent());
              uiExplorer.updateAjax(event) ;
            }
          } else {
            uiExplorer.setSelectNode(uri, session);
            uiExplorer.updateAjax(event) ;
          }
        } catch(Exception e) {
          JCRExceptionManager.process(uiApp, e);
        }
      } else {
        try {
          if ("../".equals(uri)) {
            if (!"/".equals(uiExplorer.getCurrentNode().getPath())) {
              uiExplorer.setSelectNode(uiExplorer.getCurrentNode().getParent());
              uiExplorer.updateAjax(event) ;
            }
          } else {
            uiExplorer.setSelectNode(uri, session);
            uiExplorer.updateAjax(event) ;
          }
        } catch(Exception e) {
          JCRExceptionManager.process(uiApp, e);
        }
      }
    }
  }

  static  public class SortActionListener extends EventListener<UIDocumentInfo> {
    public void execute(Event<UIDocumentInfo> event) throws Exception {
      UIDocumentInfo uicomp = event.getSource() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      String sortParam = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String[] array = sortParam.split(";") ;
      Preference pref = uiExplorer.getPreference() ;
      if(array[0].trim().equals(Preference.TYPE_SORT)) {
        if(array[1].trim().equals(Preference.ASCENDING_ORDER)) {
          uicomp.typeSortOrder_ = Preference.ASCENDING_ORDER ;
        } else if(array[1].trim().equals(Preference.DESCENDING_ORDER)) {
          uicomp.typeSortOrder_ = Preference.DESCENDING_ORDER ;
        }
        uicomp.typeSort_ = Preference.TYPE_SORT ;
      } else if(array[0].trim().equals(Preference.ALPHABETICAL_SORT)) {
        if(array[1].trim().equals(Preference.ASCENDING_ORDER)) {
          uicomp.nameSortOrder_ = Preference.ASCENDING_ORDER ;
        } else if(array[1].trim().equals(Preference.DESCENDING_ORDER)) {
          uicomp.nameSortOrder_ = Preference.DESCENDING_ORDER ;
        }
        uicomp.typeSort_ = Preference.ALPHABETICAL_SORT ;
      }
      if(array.length == 2) {
        pref.setSort(array[0].trim()) ;
        pref.setOrder(array[1].trim()) ; 
      } else if(array.length == 3) {
        pref.setSort(array[0].trim()) ;
        pref.setProperty(array[1].trim()) ;
        pref.setOrder(array[2].trim()) ;
      } else {
        return ;
      }       
      uiExplorer.updateAjax(event) ;
    }
  }
  
  static public class ChangeLanguageActionListener extends EventListener<UIDocumentInfo> {
    public void execute(Event<UIDocumentInfo> event) throws Exception {
      UIDocumentInfo uiDocumentInfo = event.getSource() ;
      UIJCRExplorer uiExplorer = uiDocumentInfo.getAncestorOfType(UIJCRExplorer.class) ;
      String selectedLanguage = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiDocumentInfo.setLanguage(selectedLanguage) ;
      uiExplorer.updateAjax(event) ;
    }   
  }
  
  static  public class VoteActionListener extends EventListener<UIDocumentInfo> {
    public void execute(Event<UIDocumentInfo> event) throws Exception {
      UIDocumentInfo uiComp = event.getSource() ;
      String userName = Util.getUIPortal().getOwner() ;
      double objId = Double.parseDouble(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      VotingService votingService = uiComp.getApplicationComponent(VotingService.class) ;
      votingService.vote(uiComp.getNode(), objId, userName) ;
    }
  }
}
