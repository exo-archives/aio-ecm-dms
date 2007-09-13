/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.portlet.ActionResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.WindowState;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.ecm.jcr.JCRResourceResolver;
import org.exoplatform.ecm.utils.SessionsUtils;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.portal.webui.portal.PageNodeEvent;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.folksonomy.FolksonomyService;
import org.exoplatform.services.cms.scripts.CmsScript;
import org.exoplatform.services.cms.scripts.DataTransfer;
import org.exoplatform.services.cms.scripts.ScriptService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Dec 14, 2006 5:15:47 PM
 */
@ComponentConfigs(
    {
      @ComponentConfig(
          events = {
              @EventConfig(listeners = UIBrowseContainer.ChangeNodeActionListener.class),
              @EventConfig(listeners = UIBrowseContainer.BackActionListener.class),
              @EventConfig(listeners = UIBrowseContainer.ViewByTagActionListener.class),
              @EventConfig(listeners = UIBrowseContainer.BackViewActionListener.class),
              @EventConfig(listeners = UIBrowseContainer.SelectActionListener.class),
              @EventConfig(listeners = UIBrowseContainer.ChangePageActionListener.class)
          }
      ),
      @ComponentConfig(
          type = UIPageIterator.class, id = "UICBPageIterator",
          template = "system:/groovy/webui/core/UIPageIterator.gtmpl",
          events = @EventConfig(listeners = UIBrowseContainer.ShowPageActionListener.class) 
      )
    }
)

public class UIBrowseContainer extends UIContainer {
  final public static String CATEGORYPATH = "categoryPath" ;
  final public static String CURRENTNODE = "currentNode" ;
  final public static String HISTORY = "history" ;
  final public static String ISSHOWALLDOCUMENT = "isShowAllDocument" ;
  final public static String ISSHOWCATEGORYTREE = "isShowCategoryTree" ;
  final public static String ISSHOWDOCUMENTBYTAG = "isShowDocumentByTag" ;
  final public static String ISSHOWDOCUMENTDETAIL = "isShowDocumentDetail" ;
  final public static String ISSHOWDOCUMENTLIST = "isShowDocumentList" ;
  final public static String ISSHOWPAGEACTION = "isShowPageAction" ;
  final public static String ISSHOWSEARCHFORM= "isShowSearchForm" ;
  final public static String KEY_CURRENT = "currentNode" ;
  final public static String KEY_SELECTED = "selectedNode" ;
  final public static String NODESHISTORY = "nodesHistory" ;
  final public static String OLDTEMPLATE = "oldTemplate" ;
  final public static String ROOTNODE = "rootNode" ;
  final public static String ROWPERBLOCK = "rowPerBlock" ;
  final public static String SELECTEDTAB = "selectedTab" ;
  final public static String TAGPATH = "tagPath" ;
  final public static String TEMPLATEDETAIL = "templateDetail" ;
  final public static String TEMPLATEPATH = "templatePath" ;
  final public static String TREELIST = "TreeList" ;
  final public static String TREEROOT = "treeRoot" ;
  final public static String USECASE = "usecase" ;

  private String categoryPath_ ;
  private Node currentNode_ ; 

  private String detailTemplate_ ;

  private boolean isShowAllDocument_  ;

  private boolean isShowCategoriesTree_ = true ;

  private boolean isShowDetailDocument_ = false ;
  private boolean isShowDocumentByTag_ = false ;
  private boolean isShowDocumentList_  = false ;    

  private boolean isShowPageAction_ ;
  private boolean isShowSearchForm_ ;
  private JCRResourceResolver jcrTemplateResourceResolver_ ;

  @SuppressWarnings("unchecked")
  private LinkedList<String> nodesHistory_ = new LinkedList<String>();
  @SuppressWarnings("unchecked")
  private Map<String,Node> nodesHistoryMap_ = new HashMap<String,Node>() ;
  private Node rootNode_ ;

  private int rowPerBlock_ = 6;
  private Node selectedTab_ ;
  private String tagPath_ ;  

  private String templatePath_ ;  
  private BCTreeNode treeRoot_ ;  
  @SuppressWarnings("unused")
  public UIBrowseContainer() throws Exception {
    ManageViewService vservice = getApplicationComponent(ManageViewService.class) ;
    addChild(UIPageIterator.class, "UICBPageIterator", "UICBPageIterator") ;
    addChild(UITagList.class, null, null);
    UICategoryTree uiTree = createUIComponent(UICategoryTree.class, null, null) ;
    addChild(uiTree) ;
    addChild(UIToolBar.class, null, null) ;
    addChild(UISearchController.class, null, null) ;    
    addChild(UIDocumentDetail.class, null, null) ;
  }  

  public void changeNode(Node selectNode) throws Exception {
    setShowAllChildren(false) ;
    setShowDocumentByTag(false) ;
    setShowDocumentDetail(false) ;
    if(selectNode.equals(getRootNode())) {
      setCurrentNode(null) ;
      setSelectedTab(null) ;
    } else {
      setSelectedTab(selectNode) ;
      setCurrentNode(selectNode.getParent()) ;
      setPageIterator(getSubDocumentList(getSelectedTab())) ;
    }
  }

  public String[] getActions() { return new String[] {"back"} ;}  

  public SessionProvider getAnonimProvider() { return SessionsUtils.getAnonimProvider() ; } 
  public String getCategoryPath() { return categoryPath_ ; }
  public List getCurrentList() throws Exception {
    return getChild(UIPageIterator.class).getCurrentPageData() ;
  }
  public Node getCurrentNode() throws Exception { 
    if(currentNode_ == null) return rootNode_ ;
    return currentNode_ ; 
  }
  public List<Node> getDocumentByTag()throws Exception {
    String repository = getRepository() ;
    FolksonomyService folksonomyService = getApplicationComponent(FolksonomyService.class) ;
    return folksonomyService.getDocumentsOnTag(getTagPath(), repository) ;
  }
  public String getIcons(Node node, String type) throws Exception {
    return Utils.getNodeTypeIcon(node, type) ; 
  }
  public String getImage(Node node) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource dresource ;
    Node contentNode = null;
    if(node.hasNode(Utils.EXO_IMAGE)) {
      contentNode = node.getNode(Utils.EXO_IMAGE) ;
    } else if(node.hasNode(Utils.JCR_CONTENT)) {
      if(!node.getPrimaryNodeType().getName().equals(Utils.NT_FILE)) return ""; 
      contentNode = node.getNode(Utils.JCR_CONTENT) ;
      String mimeType = contentNode.getProperty(Utils.JCR_MIMETYPE).getString() ;
      if(mimeType.startsWith("text")) return contentNode.getProperty(Utils.JCR_DATA).getString() ;
    }
    if(contentNode == null) return null;
    InputStream input = contentNode.getProperty(Utils.JCR_DATA).getStream() ;
    if(input.available() == 0) return null ;
    dresource = new InputStreamDownloadResource(input, "image") ;
    dresource.setDownloadName(node.getName()) ;
    return dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
  }
  
  public String getImage(Node node, String nodeTypeName) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource dresource ;
    Node contentNode = null;
    if(node.hasNode(nodeTypeName)) {
      contentNode = node.getNode(nodeTypeName) ;
    } else if(node.hasNode(Utils.JCR_CONTENT)) {
      if(!node.getPrimaryNodeType().getName().equals(Utils.NT_FILE)) return ""; 
      contentNode = node.getNode(Utils.JCR_CONTENT) ;
      String mimeType = contentNode.getProperty(Utils.JCR_MIMETYPE).getString() ;
      if(mimeType.startsWith("text")) return contentNode.getProperty(Utils.JCR_DATA).getString() ;
    }
    if(contentNode == null) return null;
    InputStream input = contentNode.getProperty(Utils.JCR_DATA).getStream() ;
    if(input.available() == 0) return null ;
    dresource = new InputStreamDownloadResource(input, "image") ;
    dresource.setDownloadName(node.getName()) ;
    return dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
  }  

  public int getItemPerPage() {
    return Integer.parseInt(getPortletPreferences().getValue(Utils.CB_NB_PER_PAGE, "")) ;
  }

  public Node getNodeByPath(String nodePath) throws Exception{
    try{
      return (Node)getSession().getItem(nodePath) ;
    } catch(NullPointerException en) {
      return rootNode_ ;
    } catch(Exception e){
      e.printStackTrace() ;
      return null  ;
    }
  }
  public  List<Node> getNodeByQuery(int recoderNumber) throws Exception{
    List<Node> queryDocuments = new ArrayList<Node>() ;
    QueryManager queryManager = null ;
    try{
      queryManager = getSession().getWorkspace().getQueryManager();
    }catch (Exception e) {
      e.printStackTrace();
      return queryDocuments ;
    }           
    String queryStatiement = getQueryStatement() ;
    if(!Boolean.parseBoolean(getPortletPreferences().getValue(Utils.CB_QUERY_ISNEW,""))) {
      String queryPath = getPortletPreferences().getValue(Utils.CB_QUERY_STORE,"") ;
      Node queryNode = getNodeByPath(queryPath) ;
      queryStatiement = queryNode.getProperty("jcr:statement").getString() ;
    }
    Query query = queryManager.createQuery(queryStatiement, getQueryLanguage());
    QueryResult queryResult = query.execute();
    NodeIterator iter = queryResult.getNodes();
    int count = 0 ; 
    while (iter.hasNext() && (count++ != recoderNumber)) {
      queryDocuments.add(iter.nextNode()) ;
    }
    return queryDocuments ;
  }

  public  List<Node> getNodeByQuery(int recoderNumber,Session session) throws Exception{
    List<Node> queryDocuments = new ArrayList<Node>() ;
    QueryManager queryManager = null ;
    try{
      queryManager = session.getWorkspace().getQueryManager();
    }catch (Exception e) {
      e.printStackTrace();
      return queryDocuments ;
    }           
    String queryStatiement = getQueryStatement() ;
    if(!Boolean.parseBoolean(getPortletPreferences().getValue(Utils.CB_QUERY_ISNEW,""))) {
      String queryPath = getPortletPreferences().getValue(Utils.CB_QUERY_STORE,"") ;
      Node queryNode = getNodeByPath(queryPath) ;
      queryStatiement = queryNode.getProperty("jcr:statement").getString() ;
    }
    Query query = queryManager.createQuery(queryStatiement, getQueryLanguage());
    QueryResult queryResult = query.execute();
    NodeIterator iter = queryResult.getNodes();
    int count = 0 ; 
    while (iter.hasNext() && (count++ != recoderNumber)) {
      queryDocuments.add(iter.nextNode()) ;
    }
    return queryDocuments ;
  }
  public  List<Node> getNodeByQuery(String queryType, String queryString) throws Exception{
    List<Node> queryDocuments = new ArrayList<Node>() ;
    try {
      ManageableRepository repository = getRepositoryService().getRepository(getRepository()) ;
      String workspace = repository.getConfiguration().getDefaultWorkspaceName() ;
      QueryManager queryManager = null ;
      Session session = getSystemProvider().getSession(workspace, repository) ;
      queryManager = session.getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(queryString, queryType);
      QueryResult queryResult = query.execute();
      NodeIterator iter = queryResult.getNodes();
      while (iter.hasNext()) {
        queryDocuments.add(iter.nextNode()) ;
      }
    }
    catch(Exception e) {
      e.printStackTrace() ;
    }
    return queryDocuments ;
  }    
  public LinkedList<String> getNodesHistory() { return nodesHistory_ ; }

  public int getNumberOfPage() {
    return getChild(UIPageIterator.class).getAvailablePage();
  }
  public String getOwner(Node node) throws Exception{
    if(node.hasProperty("exo:owner")) {
      return node.getProperty("exo:owner").getString();
    }
    return SystemIdentity.ANONIM ;
  }
  @SuppressWarnings("unchecked")
  public Map getPathContent() throws Exception {
    TemplateService templateService  = getApplicationComponent(TemplateService.class) ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    List templates = templateService.getDocumentTemplates(getRepository()) ;
    List<String> tabList = new ArrayList<String>() ;
    List<String> subCategoryList = new ArrayList<String>() ;
    List<Node> subDocumentList = new ArrayList<Node>() ;
    Map content = new HashMap() ;
    boolean isShowDocument = isEnableChildDocument() ;
    boolean isShowReferenced = isEnableRefDocument() ;
    int itemCounter = getRowPerBlock() ;
    if(isShowAllDocument()) itemCounter = getItemPerPage();

    if(getSelectedTab().equals(getCurrentNode())) {
      NodeIterator tabIter = getCurrentNode().getNodes() ;
      while(tabIter.hasNext()) {
        Node childNode = tabIter.nextNode() ;
        if(canRead(childNode)) {
          NodeType nt = childNode.getPrimaryNodeType() ;
          if(templates.contains(nt.getName())&&(isShowDocument)) { 
            subDocumentList.add(childNode) ;
          }
          if(isShowReferenced) subDocumentList.addAll(getReferences(repositoryService,
              childNode, isShowAllDocument(), subDocumentList.size(), templates)) ;        
          if(isCategories(nt)&&(!templates.contains(nt.getName()))) {
            Map childOfSubCategory = new HashMap() ;
            List<Node> subCategoryDoc = new ArrayList<Node>() ;
            List<String> subCategoryCat = new ArrayList<String>() ;
            NodeIterator item = childNode.getNodes() ;
            while(item.hasNext()) {
              Node node = item.nextNode() ;
              if(canRead(node)){
                NodeType nodeType = node.getPrimaryNodeType() ;
                if(templates.contains(nodeType.getName())&&(isShowDocument)) { 
                  if(subCategoryDoc.size() < getRowPerBlock()) subCategoryDoc.add(node) ;
                }
                if(isCategories(nodeType)&&(!templates.contains(nodeType.getName()))) subCategoryCat.add(node.getPath()) ;
              }
            }
            if(isShowReferenced) subCategoryDoc.addAll(getReferences(repositoryService, childNode,
                false, subCategoryDoc.size(), templates)) ;
            childOfSubCategory.put("doc", subCategoryDoc) ;
            childOfSubCategory.put("sub", subCategoryCat) ;
            String path = childNode.getPath() ;
            String keyPath = path.substring(path.lastIndexOf("/") + 1) ;
            content.put(keyPath, childOfSubCategory) ;
            subCategoryList.add(path) ;
          } 
        }
      }
      content.put("tabList", tabList) ;
      content.put("subDocumentList", subDocumentList) ;      
      content.put("subCategoryList", subCategoryList) ;
      return content ;
    }

    NodeIterator tabIter = getCurrentNode().getNodes() ;
    while(tabIter.hasNext()) {
      Node tab = tabIter.nextNode() ;
      if(canRead(tab)) {
        if(!templates.contains(tab.getPrimaryNodeType().getName())){
          if(isCategories(tab.getPrimaryNodeType()))tabList.add(tab.getPath()) ;
          if(tab.getPath().equals(getSelectedTab().getPath())) {
            NodeIterator childs = tab.getNodes() ;
            while(childs.hasNext()) {
              Node child = childs.nextNode() ;
              String nt = child.getPrimaryNodeType().getName() ;
              if(templates.contains(nt) && (isShowDocument)) {
                if(subDocumentList.size() < itemCounter) subDocumentList.add(child) ;
              }
              if(isCategories(child.getPrimaryNodeType()) && !templates.contains(nt)){
                Map childOfSubCategory = getChildOfSubCategory(repositoryService, child, templates) ;
                content.put(child.getName(), childOfSubCategory) ;
                subCategoryList.add(child.getPath()) ;
              }
            }
            if(isShowReferenced) subDocumentList.addAll(getReferences(repositoryService,
                getSelectedTab(), isShowAllDocument(), subDocumentList.size(), templates)) ;
          }
        }
      }
    }
    content.put("tabList", tabList) ;
    content.put("subCategoryList", subCategoryList) ;
    content.put("subDocumentList", subDocumentList) ;
    List<String> history = new ArrayList<String>() ;
    if(!isRootNode()) {
      Node parent = getCurrentNode().getParent() ;
      if(!parent.getPath().equals(getRootNode().getPath())) content.put("previous", parent.getPath()) ;
      history = getHistory(templates, parent) ;
    }
    content.put("history", history) ;
    return content ;
  }

  public PortletPreferences getPortletPreferences() {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletRequest prequest = pcontext.getRequest() ;
    PortletPreferences portletPref = prequest.getPreferences() ;
    return portletPref ;
  }

  public String getQueryLanguage() {
    return getPortletPreferences().getValue(Utils.CB_QUERY_LANGUAGE, "") ;
  }
  public String getQueryStatement() {
    return getPortletPreferences().getValue(Utils.CB_QUERY_STATEMENT, "") ;
  }

  public String getRepository() {
    return getPortletPreferences().getValue(Utils.REPOSITORY, "") ;
  }
  public Node getRootNode() throws Exception { return rootNode_ ; }
  public int getRowPerBlock() { return rowPerBlock_ ; }
  public Node getSelectedTab() throws Exception { 
    if(this.selectedTab_ == null) return rootNode_ ;
    return this.selectedTab_ ;  
  }

  public Session getSession() throws Exception{
    Session session = null ;
    String categoryPath = getPortletPreferences().getValue(Utils.JCR_PATH,"") ;
    String workspace = getWorkSpace() ;
    ManageableRepository manageableRepository = getRepositoryService().getRepository(getRepository()) ;
    if(categoryPath.startsWith("/jcr:system")) {         
      session = getSystemProvider().getSession(workspace,manageableRepository) ;
    }else {
      if(SessionsUtils.isAnonim()) {
        //TODO Anonim Session
        //session = getAnonimProvider().getSession(workspace,manageableRepository) ;
        session = getSystemProvider().getSession(workspace,manageableRepository) ;
      }else {
        session = getSessionProvider().getSession(workspace,manageableRepository) ; 
      }
    }
    return session ;
  }
  public SessionProvider getSessionProvider() { return SessionsUtils.getSessionProvider() ; }
  @SuppressWarnings("unchecked")
  public List<Node> getSubDocumentList(Node selectedNode) throws Exception {
    List<Node> subDocumentList = new ArrayList<Node>() ;
    if(selectedNode == null) return subDocumentList ;
    TemplateService templateService  = getApplicationComponent(TemplateService.class) ;
    List<String> templates = templateService.getDocumentTemplates(getRepository()) ;
    NodeIterator item = selectedNode.getNodes() ;
    if(isEnableChildDocument())
      while (item.hasNext()) {
        Node node = item.nextNode() ;
        if(templates.contains(node.getPrimaryNodeType().getName())) {
          if(canRead(node)) subDocumentList.add(node) ; 
        }
      }
    if(isEnableRefDocument()) subDocumentList.addAll(getReferences(getRepositoryService(),
        selectedNode, isShowAllDocument(), subDocumentList.size(), templates)) ;
    return subDocumentList ;
  }

  public SessionProvider getSystemProvider() { return SessionsUtils.getSystemProvider() ; }
  public List<Node> getTagLink() throws Exception {
    String repository = getRepository() ;
    FolksonomyService folksonomyService = getApplicationComponent(FolksonomyService.class) ;
    return folksonomyService.getAllTags(repository) ;
  }
  public String getTagPath() { return this.tagPath_ ; }

  public Map<String ,String> getTagStyle() throws Exception {
    String repository = getRepository() ;
    FolksonomyService folksonomyService = getApplicationComponent(FolksonomyService.class) ;
    Map<String , String> tagStyle = new HashMap<String ,String>() ;
    for(Node tag : folksonomyService.getAllTagStyle(repository)) {
      tagStyle.put(tag.getName(), tag.getProperty("exo:htmlStyle").getValue().getString()) ;
    }
    return tagStyle ;
  }

  public String getTemplate() {
    if(isShowDetailDocument_) return detailTemplate_ ;
    return templatePath_ ; 
  }

  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    if(jcrTemplateResourceResolver_ == null) newJCRTemplateResourceResolver() ;
    return jcrTemplateResourceResolver_ ;
  } 

  @SuppressWarnings("unchecked")
  public List<Node> getSortedListNode(Node node) throws Exception {
    NodeIterator nodeIter = node.getNodes() ;
    List<Node> nodes = new ArrayList<Node>() ;
    while(nodeIter.hasNext()) {
      Node childNode = nodeIter.nextNode() ;
      nodes.add(childNode) ;
    }
    Collections.sort(nodes, new NodeNameComparator()) ;
    return nodes ;
  }
  
  @SuppressWarnings("unchecked")
  public List<Node> getSortedListNodeByDate(Node node, boolean isASC) throws Exception {
    NodeIterator nodeIter = node.getNodes() ;
    List<Node> nodes = new ArrayList<Node>() ;
    while(nodeIter.hasNext()) {
      Node childNode = nodeIter.nextNode() ;
      nodes.add(childNode) ;
    }
    if(isASC) Collections.sort(nodes, new DateASCComparator()) ;
    else Collections.sort(nodes, new DateDESCComparator()) ;
    return nodes ;
  }
  
  static public class NodeNameComparator implements Comparator {
    public int compare(Object o1, Object o2) throws ClassCastException {
      try {
        String name1 = ((Node)o1).getName() ;
        String name2 = ((Node)o2).getName() ;
        return name2.compareToIgnoreCase(name1) ;
      } catch(Exception e) {
        return 0;
      }
    }
  }
  
  static public class DateASCComparator implements Comparator {
    public int compare(Object o1, Object o2) throws ClassCastException {
      try {
        Date date1 = ((Node)o1).getProperty(Utils.EXO_CREATED_DATE).getDate().getTime() ;
        Date date2 = ((Node)o2).getProperty(Utils.EXO_CREATED_DATE).getDate().getTime() ;
        return date1.compareTo(date2) ;
      } catch(Exception e) {
        return 0;
      }
    }
  }
  
  static public class DateDESCComparator implements Comparator {
    public int compare(Object o1, Object o2) throws ClassCastException {
      try {
        Date date1 = ((Node)o1).getProperty(Utils.EXO_CREATED_DATE).getDate().getTime() ;
        Date date2 = ((Node)o2).getProperty(Utils.EXO_CREATED_DATE).getDate().getTime() ;
        return date2.compareTo(date1) ;
      } catch(Exception e) {
        return 0;
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  public Map getTreeContent() throws Exception {
    TemplateService templateService  = getApplicationComponent(TemplateService.class) ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    List templates = templateService.getDocumentTemplates(getRepository()) ;
    List<String> subCategoryList = new ArrayList<String>() ;
    List<Node> subDocumentList = new ArrayList<Node>() ;
    Map content = new HashMap() ;
    NodeIterator childIter = getCurrentNode().getNodes() ;
    boolean isShowDocument = isEnableChildDocument() ;
    boolean isShowReferenced = isEnableRefDocument() ;
    while(childIter.hasNext()) {
      Node child = childIter.nextNode() ;
      if(canRead(child)) {
        if(templates.contains(child.getPrimaryNodeType().getName())&&(isShowDocument)) {       
          if(canRead(child)) subDocumentList.add(child) ;
        } else {
          if(isCategories(child.getPrimaryNodeType())) {
            Map childOfSubCategory = getChildOfSubCategory(repositoryService, child, templates) ;
            String path = child.getPath() ;
            String keyPath = path.substring(path.lastIndexOf("/") + 1) ;
            content.put(keyPath, childOfSubCategory) ;
            subCategoryList.add(path) ;
          }
        }
      }
    }

    if(isShowReferenced) subDocumentList.addAll(getReferences(repositoryService,
        getCurrentNode(), isShowAllDocument(), subDocumentList.size(), templates)) ;
    content.put("subCategoryList", subCategoryList) ;
    content.put("subDocumentList", subDocumentList) ;
    return content ;
  } 
  public BCTreeNode getTreeRoot() { return treeRoot_ ;  }
  public UIPageIterator getUIPageIterator() throws Exception {
    return getChild(UIPageIterator.class) ;
  }

  public String getUseCase() {
    return getPortletPreferences().getValue(Utils.CB_USECASE, "") ;
  }    

  public String getWorkSpace() {
    return getPortletPreferences().getValue(Utils.WORKSPACE_NAME, "") ;
  }
  public void initToolBar(boolean showTree, boolean showPath,boolean showSearch) throws Exception {
    UIToolBar toolBar = getChild(UIToolBar.class) ;
    toolBar.setEnableTree(showTree) ;
    toolBar.setEnablePath(showPath) ;
    toolBar.setEnableSearch(showSearch) ;
    toolBar.setRendered(true) ;
  }

  public boolean isCommentAndVote() { return (isShowVoteForm() || isShowCommentForm()) ;}

  public boolean isEnableChildDocument() {
    return Boolean.parseBoolean(getPortletPreferences().getValue(Utils.CB_CHILD_DOCUMENT, "")) ;
  }
  public boolean isEnableRefDocument() {
    return Boolean.parseBoolean(getPortletPreferences().getValue(Utils.CB_REF_DOCUMENT, "")) ;
  }

  public boolean isEnableToolBar() {
    return Boolean.parseBoolean(getPortletPreferences().getValue(Utils.CB_VIEW_TOOLBAR, "")) ;
  }
  public boolean isRootNode() throws Exception {return getCurrentNode().equals(getRootNode()) ;}
  public boolean isShowAllDocument() { return this.isShowAllDocument_ ; }

  public boolean isShowCategoryTree() { return isShowCategoriesTree_ ; }  
  public boolean isShowCommentForm() {
    return Boolean.parseBoolean(getPortletPreferences().getValue(Utils.CB_VIEW_COMMENT, "")) ;
  }
  public boolean isShowDocumentByTag() { return isShowDocumentByTag_ ; }

  public boolean isShowDocumentDetail() { return isShowDetailDocument_ ; }
  public boolean isShowDocumentList() { return this.isShowDocumentList_ ; }
  public boolean isShowSearchForm() { return isShowSearchForm_ ;  }  

  public boolean isShowTagmap() {
    return Boolean.parseBoolean(getPortletPreferences().getValue(Utils.CB_VIEW_TAGMAP, "")) ;
  }

  public boolean isShowVoteForm() {
    return Boolean.parseBoolean(getPortletPreferences().getValue(Utils.CB_VIEW_VOTE, "")) ;
  }

  //TODO maybe need change name of this method
  public void loadPortletConfig(PortletPreferences preferences ) throws Exception {
    String tempName = preferences.getValue(Utils.CB_TEMPLATE, "") ;
    String repoName = getRepository() ;
    String workspace = getWorkSpace() ;
    ManageableRepository manageableRepository = getRepositoryService().getRepository(repoName) ;        
    ManageViewService viewService = getApplicationComponent(ManageViewService.class) ;
    setShowDocumentByTag(false) ;
    setShowDocumentDetail(false) ;
    if(getUseCase().equals(Utils.CB_USE_JCR_QUERY)) {
      setTemplate(viewService.getTemplateHome(BasePath.CB_QUERY_TEMPLATES, repoName,SessionsUtils.getSystemProvider()).getNode(tempName).getPath()) ;
      if(isShowCommentForm() || isShowVoteForm()) initToolBar(false, false, false) ;
      if(!isShowDocumentByTag()) setPageIterator(getNodeByQuery(-1)) ;
      return ;
    } 
    if(getUseCase().equals(Utils.CB_USE_SCRIPT)) { 
      setTemplate(viewService.getTemplateHome(BasePath.CB_SCRIPT_TEMPLATES, repoName,SessionsUtils.getSystemProvider()).getNode(tempName).getPath()) ;
      if(isShowCommentForm() || isShowVoteForm()) initToolBar(false, false, false) ;
      String scriptName = preferences.getValue(Utils.CB_SCRIPT_NAME, "") ;
      if(!isShowDocumentByTag()) setPageIterator(getNodeByScript(repoName, scriptName)) ;
      return ;
    }    
    String categoryPath = preferences.getValue(Utils.JCR_PATH, "") ;
    if(getUseCase().equals(Utils.CB_USE_FROM_PATH)) {
      setTemplate(viewService.getTemplateHome(BasePath.CB_PATH_TEMPLATES, repoName,SessionsUtils.getSystemProvider()).getNode(tempName).getPath()) ;
      Node node = (Node)getSession().getItem(categoryPath) ;
      setRootNode(node) ;
      setCategoryPath(categoryPath) ;
      setSelectedTab(node) ;
      setCurrentNode(node) ;      
      initToolBar(false, isEnableToolBar(), isEnableToolBar()) ;
      if(getTemplateName().equals(TREELIST)) {
        if(isEnableToolBar()) initToolBar(true, false, true) ;
        getChild(UICategoryTree.class).setTreeRoot(getRootNode()) ;
        getChild(UICategoryTree.class).buildTree(getCurrentNode().getPath()) ;
      }
      if(!isShowDocumentByTag()) setPageIterator(getSubDocumentList(getSelectedTab())) ;
      return ;
    } 
    if(getUseCase().equals(Utils.CB_USE_DOCUMENT)) {
      setTemplateDetail(viewService.getTemplateHome(BasePath.CB_DETAIL_VIEW_TEMPLATES, repoName,SessionsUtils.getSystemProvider()).getNode(tempName).getPath()) ;      
      String documentPath = categoryPath + preferences.getValue(Utils.CB_DOCUMENT_NAME, "") ;
      Node documentNode = null;      
      try{
        documentNode = (Node)getSession().getItem(documentPath) ;
      }catch (Exception e) { 
        e.printStackTrace() ;
      }      
      viewDocument(documentNode, false) ;
      if(isEnableToolBar()) initToolBar(false, false, false) ;
      return ;
    }     
  }
  public void newJCRTemplateResourceResolver() {
    try{      
      RepositoryService repositoryService  = getApplicationComponent(RepositoryService.class) ;      
      ManageableRepository repository = repositoryService.getRepository(getRepository()) ;
      String workspace = repository.getConfiguration().getDefaultWorkspaceName() ;
      Session session = getSystemProvider().getSession(workspace,repository) ;         
      jcrTemplateResourceResolver_ = new JCRResourceResolver(session, Utils.EXO_TEMPLATEFILE) ;
    }catch(Exception e) {
      e.printStackTrace() ;
    }     
  }
  public void processRender(WebuiRequestContext context) throws Exception {
    try {
      getApplicationComponent(RepositoryService.class).getRepository(getRepository()) ;
      super.processRender(context) ;
    } catch (Exception e) {
      getAncestorOfType(UIBrowseContentPortlet.class).setPorletMode(PortletRequestContext.HELP_MODE) ;
      return ;
    }
  }

  public void record(String str) { getNodesHistory().add(str) ;  }

  public void refreshContent() throws Exception{
    if(!showPageAction()) { 
      if(isShowDocumentByTag()) {
        setPageIterator(getDocumentByTag()) ;
      } else {
        if(getUseCase().equals(Utils.CB_USE_FROM_PATH)) {
          if(getNodeByPath(getCategoryPath()) == null || getNodeByPath(getRootNode().getPath()) == null) {
            UIBrowseContentPortlet uiPorlet = getAncestorOfType(UIBrowseContentPortlet.class) ;
            uiPorlet.setPorletMode(PortletRequestContext.HELP_MODE) ;
            uiPorlet.reload() ;
          } else if(getNodeByPath(getSelectedTab().getPath()) == null || getNodeByPath(getCurrentNode().getPath()) == null) {
            setSelectedTab(null) ;
            setCurrentNode(null) ;
          }
          setPageIterator(getSubDocumentList(getSelectedTab())) ;
        } else if(getUseCase().equals(Utils.CB_USE_SCRIPT)) {
        } else if(getUseCase().equals(Utils.CB_USE_JCR_QUERY)) {
          setPageIterator(getNodeByQuery(-1)) ;
        } else if(getUseCase().equals(Utils.USE_DOCUMENT)) {
          if(getChild(UIDocumentDetail.class).isValidNode()) {
            getChild(UIDocumentDetail.class).setRendered(true) ;         
          } else {
            getChild(UIDocumentDetail.class).setRendered(false) ;
            UIBrowseContentPortlet uiPortlet = getAncestorOfType(UIBrowseContentPortlet.class) ;
            uiPortlet.getChild(UIPopupAction.class).deActivate() ;
          } 
        }
        if(isShowDocumentDetail()) {
          UIDocumentDetail uiDocumentDetail = getChild(UIDocumentDetail.class) ;      
          if(getChild(UIDocumentDetail.class).isValidNode()) {
            getChild(UIDocumentDetail.class).setRendered(true) ;         
          } else {
            if(isShowDocumentByTag() && isShowDocumentDetail()) {
              setShowDocumentDetail(false) ;
              uiDocumentDetail.setRendered(false) ;
            } else {
              setShowDocumentByTag(false) ;
              setShowDocumentDetail(false) ;
              uiDocumentDetail.setRendered(false) ;
              if(getUseCase().equals(Utils.CB_USE_FROM_PATH) && getHistory() != null) {
                setCurrentNode(getHistory().get(UIBrowseContainer.KEY_CURRENT)) ;
                setSelectedTab(getHistory().get(UIBrowseContainer.KEY_SELECTED)) ;
                getHistory().clear() ;
              }
              UIBrowseContentPortlet uiPortlet = getAncestorOfType(UIBrowseContentPortlet.class) ;
              uiPortlet.getChild(UIPopupAction.class).deActivate() ;
            } 
          }
        }
      } 
      setShowPageAction(false) ;
    }
  }

  public void setCategoryPath(String path) {
    this.categoryPath_ = path ;
  }

  public void setCurrentNode(Node node) throws Exception { this.currentNode_ = node ; }
  public void setPageIterator(List<Node> data) throws Exception {
    ObjectPageList objPageList = new ObjectPageList(data, getItemPerPage()) ;
    getChild(UIPageIterator.class).setPageList(objPageList) ;
  }
  public void setRowPerBlock(int number) { this.rowPerBlock_ = number ; }

  public void setSelectedTab (Node node) { this.selectedTab_ = node ; }

  public void setShowAllChildren(boolean isShowAll) { 
    this.isShowAllDocument_ = isShowAll ;
  }

  public void setShowCategoryTree(boolean  isShowCategoryTree) {
    this.isShowCategoriesTree_ = isShowCategoryTree ;
  }

  public void setShowDocumentByTag(boolean isShowByTag) {
    this.isShowDocumentByTag_ = isShowByTag ;
  }
  public void setShowDocumentDetail(boolean isShowDocument) {
    this.isShowDetailDocument_ = isShowDocument ;
  }

  public void setShowDocumentList(boolean isShowDocumentList){
    this.isShowDocumentList_ = isShowDocumentList ; 
  }

  public void setShowSearchForm(boolean isShowSearch) {
    this.isShowSearchForm_ = isShowSearch ;
  }

  public void setTagPath(String tagPath) { this.tagPath_ = tagPath ; }

  public void setTemplate(String temp) { this.templatePath_ = temp ; }

  public void setTreeRoot(Node node) throws Exception { this.treeRoot_ = new BCTreeNode(node) ; }

  public void storeHistory() throws Exception {
    getHistory().clear() ;
    getHistory().put(KEY_CURRENT, getCurrentNode());
    getHistory().put(KEY_SELECTED, getSelectedTab());    
  }

  public void viewDocument(Node docNode ,boolean hasDocList) throws Exception {
    setShowDocumentDetail(true) ;
    setShowDocumentList(hasDocList) ;
    UIDocumentDetail uiDocumetDetail = getChild(UIDocumentDetail.class) ;
    uiDocumetDetail.setNode(docNode) ;
    uiDocumetDetail.setRendered(true) ;
  }

  protected Map<String, Node>  getHistory() { return nodesHistoryMap_ ; }

  protected List<Node> getNodeByScript(String repository,String scriptName) throws Exception {
    DataTransfer data = new DataTransfer() ;
    ScriptService scriptService = getApplicationComponent(ScriptService.class) ;
    data.setWorkspace(getPortletPreferences().getValue(Utils.WORKSPACE_NAME, "")) ;
    data.setRepository(repository) ;
    Node scripts = scriptService.getCBScriptHome(repository) ;
    CmsScript cmsScript = scriptService.getScript(scripts.getName()+ "/" + scriptName , repository) ;
    try {
      cmsScript.execute(data);
    } catch (Exception e) {
      e.printStackTrace() ;
    }
    return data.getContentList() ;
  }

  protected String getTemlateDetail() { return detailTemplate_ ; }
  protected String getTemplateName() {
    return getPortletPreferences().getValue(Utils.CB_TEMPLATE, "") ;
  }

  protected void historyBack() throws Exception {
    if(getTemplateName().equals(TREELIST)) {
      setSelectedTab(null) ;
      setCurrentNode(getNodeByPath(getNodesHistory().removeLast())) ;
    } else {
      setSelectedTab(getNodeByPath(getNodesHistory().removeLast()))  ;
    }
  }

  protected void historyNext() {}

  protected boolean isCategories(NodeType nodeType) {
    for(String type : Utils.CATEGORY_NODE_TYPES) {
      if(nodeType.getName().equals(type)) return true ;
    }
    return false ;
  }  
  protected void setRootNode(Node node) { this.rootNode_ = node ; }
  protected void setShowPageAction(boolean isShowPage) { this.isShowPageAction_ = isShowPage ; }

  protected void setTemplateDetail(String template) { this.detailTemplate_ = template ; }

  protected boolean showPageAction() { return isShowPageAction_ ; }

  private boolean canRead(Node node) {
    ExtendedNode eNode = (ExtendedNode)node ;
    try{
      eNode.checkPermission(PermissionType.READ) ;
      return true ;
    } catch(Exception ac){}
    return false ;
  }

  private Map getChildOfSubCategory(RepositoryService repositoryService, Node subCat,
      List documentTemplates) throws Exception {
    List<String> subCategories = new ArrayList<String>() ;
    List<Node> childDocOrReferencedDoc = new ArrayList<Node>() ;
    Map<String, List> childMap = new HashMap<String, List>() ;
    NodeIterator items  =  subCat.getNodes() ;
    boolean isShowDocument = isEnableChildDocument() ;
    boolean isShowReferenced = isEnableRefDocument() ;
    while (items.hasNext()) {
      Node item = items.nextNode() ;
      if(canRead(item)){
        NodeType nt = item.getPrimaryNodeType() ;
        if(documentTemplates.contains(nt.getName())&&(isShowDocument)){
          if(childDocOrReferencedDoc.size() < getRowPerBlock()) childDocOrReferencedDoc.add(item) ;
        } else {
          if(isCategories(item.getPrimaryNodeType())) subCategories.add(item.getPath()) ;          
        }
      }
    }
    if(isShowReferenced) childDocOrReferencedDoc.addAll(getReferences(repositoryService, subCat,
        false, childDocOrReferencedDoc.size(), documentTemplates)) ;
    childMap.put("sub", subCategories) ;
    childMap.put("doc", childDocOrReferencedDoc) ;
    return childMap ;
  }

  private List<String> getHistory(List documentTemplates, Node parentNode) throws Exception {
    List<String> historyList = new ArrayList<String>() ;
    NodeIterator iter = parentNode.getNodes() ;
    while(iter.hasNext()) {
      Node node = iter.nextNode() ;
      String nt = node.getPrimaryNodeType().getName() ;
      if(!documentTemplates.contains(nt)) historyList.add(node.getPath()) ;
    }
    return historyList ;
  } 
  private List<Node> getReferences(RepositoryService repositoryService, Node node, boolean isShowAll,
      int size, List templates) throws Exception {
    List<Node> refDocuments = new ArrayList<Node>() ;    
    String repository = getRepository() ;
    ManageableRepository manageableRepository = repositoryService.getRepository(repository) ;
    SessionProvider provider = getSystemProvider() ;
    if(isEnableRefDocument() && isReferenceableNode(node)) {
      String uuid = node.getUUID() ;
      String[] workspaces = manageableRepository.getWorkspaceNames() ;
      int itemCounter = getRowPerBlock() - size ;
      if(isShowAll) itemCounter = getItemPerPage() - size ;
      for(String workspace : workspaces) {
        Session session = provider.getSession(workspace,manageableRepository) ;
        try {
          Node taxonomyNode = session.getNodeByUUID(uuid) ;
          PropertyIterator iter = taxonomyNode.getReferences() ;
          while (iter.hasNext() && (refDocuments.size() < itemCounter)) {
            Node refNode = iter.nextProperty().getParent() ;
            if (templates.contains(refNode.getPrimaryNodeType().getName())) refDocuments.add(refNode);
          }
        }catch (Exception e) {}
      }
    }
    return refDocuments ;
  }

  private RepositoryService getRepositoryService() { 
    return getApplicationComponent(RepositoryService.class) ;
  }

  private boolean isReferenceableNode(Node node) throws Exception {
    NodeType[] nodeTypes = node.getMixinNodeTypes() ;
    for(NodeType type : nodeTypes) {
      if(type.getName().equals(Utils.MIX_REFERENCEABLE)) return true ;
    }
    return false ;
  }   
  
  public String getWebDAVServerPrefix() throws Exception {    
    PortletRequestContext portletRequestContext = PortletRequestContext.getCurrentInstance() ;
    String prefixWebDAV = portletRequestContext.getRequest().getScheme() + "://" + 
                          portletRequestContext.getRequest().getServerName() + ":" +
                          String.format("%s",portletRequestContext.getRequest().getServerPort()) ;
    return prefixWebDAV ;
  }

  static public class BackActionListener extends EventListener<UIBrowseContainer> {
    public void execute(Event<UIBrowseContainer> event) throws Exception {
      UIBrowseContainer uiContainer = event.getSource() ;
      if(uiContainer.isShowDocumentByTag() && uiContainer.isShowDocumentDetail()) {
        UIDocumentDetail uiDocumentDetail = uiContainer.getChild(UIDocumentDetail.class) ;      
        uiContainer.setShowDocumentDetail(false) ;
        uiDocumentDetail.setRendered(false) ;
      } else {
        uiContainer.setShowDocumentByTag(false) ;
        UIDocumentDetail uiDocumentDetail = uiContainer.getChild(UIDocumentDetail.class) ;      
        uiContainer.setShowDocumentDetail(false) ;
        uiDocumentDetail.setRendered(false) ;
        if(uiContainer.getUseCase().equals(Utils.CB_USE_FROM_PATH) && uiContainer.getHistory() != null) {
          uiContainer.setCurrentNode(uiContainer.getHistory().get(UIBrowseContainer.KEY_CURRENT)) ;
          uiContainer.setSelectedTab(uiContainer.getHistory().get(UIBrowseContainer.KEY_SELECTED)) ;
          uiContainer.getHistory().clear() ;
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }

  static public class BackViewActionListener extends EventListener<UIBrowseContainer> {
    public void execute(Event<UIBrowseContainer> event) throws Exception {
      String normalState = event.getRequestContext().getRequestParameter("normalState") ;
      if(normalState != null) {
        ActionResponse response = event.getRequestContext().getResponse() ;
        response.setWindowState(WindowState.NORMAL);
      }
      UIBrowseContainer uiContainer = event.getSource() ;
      if(uiContainer.isShowDocumentDetail()) {
        UIDocumentDetail uiDocumentDetail = uiContainer.getChild(UIDocumentDetail.class) ;      
        uiContainer.setShowDocumentDetail(false) ;
        uiDocumentDetail.setRendered(false) ;
      }
      uiContainer.refreshContent();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }

  static public class ChangeNodeActionListener extends EventListener<UIBrowseContainer> {
    public void execute(Event<UIBrowseContainer> event) throws Exception {
      String useMaxState = event.getRequestContext().getRequestParameter("useMaxState") ;
      if(useMaxState != null) {
        ActionResponse response = event.getRequestContext().getResponse() ;
        response.setWindowState(WindowState.MAXIMIZED);
      }
      UIBrowseContainer uiContainer = event.getSource() ;
      uiContainer.setShowDocumentDetail(false) ;
      uiContainer.setShowAllChildren(false) ;
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String catPath = event.getRequestContext().getRequestParameter("category") ;  
      Node selectNode = uiContainer.getNodeByPath(objectId) ;   
      if(selectNode == null) {
        UIApplication app = uiContainer.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UIBrowseContainer.msg.invalid-node", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(app.getUIPopupMessages()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
        return ;
      }
      TemplateService templateService  = uiContainer.getApplicationComponent(TemplateService.class) ;
      List templates = templateService.getDocumentTemplates(uiContainer.getRepository()) ;
      if(templates.contains(selectNode.getPrimaryNodeType().getName())) {
        if(catPath != null) {
          uiContainer.setCategoryPath(catPath) ;
          Node currentCat  = uiContainer.getNodeByPath(catPath);
          uiContainer.storeHistory() ;
          uiContainer.setPageIterator(uiContainer.getSubDocumentList(currentCat)) ;
        }
        ManageViewService vservice = uiContainer.getApplicationComponent(ManageViewService.class) ;
        String repoName = uiContainer.getPortletPreferences().getValue(Utils.REPOSITORY, "") ;
        String detailTemplateName = uiContainer.getPortletPreferences().getValue(Utils.CB_BOX_TEMPLATE, "") ;
        uiContainer.setTemplateDetail(vservice.getTemplateHome(BasePath.CB_DETAIL_VIEW_TEMPLATES, repoName,SessionsUtils.getSystemProvider())
            .getNode(detailTemplateName).getPath())  ;
        uiContainer.viewDocument(selectNode, true) ;
      } else {
        String templateType = uiContainer.getPortletPreferences().getValue(Utils.CB_USECASE, "") ;
        if((templateType.equals(Utils.CB_USE_JCR_QUERY)) || (templateType.equals(Utils.CB_SCRIPT_NAME))) {
          UIApplication app = uiContainer.getAncestorOfType(UIApplication.class) ;
          app.addMessage(new ApplicationMessage("UIBrowseContainer.msg.template-notsupported", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(app.getUIPopupMessages()) ;
        } else {
          uiContainer.changeNode(selectNode) ;
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer.getAncestorOfType(UIBrowseContentPortlet.class)) ;
    }
  }

  static public class SelectActionListener extends EventListener<UIBrowseContainer> {
    public void execute(Event<UIBrowseContainer> event) throws Exception {
      UIBrowseContainer uiContainer = event.getSource() ;
      UICategoryTree cateTree = uiContainer.getChild(UICategoryTree.class) ;
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Node node = uiContainer.getNodeByPath(path) ;
      if(node == null) {
        UIApplication app = uiContainer.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UIBrowseContainer.msg.invalid-node", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(app.getUIPopupMessages()) ;
        return ;
      }
      uiContainer.setShowDocumentDetail(false) ;
      uiContainer.setShowDocumentByTag(false) ;
      uiContainer.setShowAllChildren(false) ;
      uiContainer.setSelectedTab(null) ;
      uiContainer.setCurrentNode(node) ;
      cateTree.buildTree(node.getPath()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }

  static  public class ChangePageActionListener extends EventListener<UIBrowseContainer> {
    public void execute(Event<UIBrowseContainer> event) throws Exception {
      UIPortal uiPortal = Util.getUIPortal();
      String uri  = event.getRequestContext().getRequestParameter(OBJECTID);
      String[] arrUri = {uri} ;
      if(uri.contains("/")) arrUri = uri.split("/") ;
      PageNodeEvent<UIPortal> pnevent ;
      pnevent = new PageNodeEvent<UIPortal>(uiPortal, PageNodeEvent.CHANGE_PAGE_NODE, null, arrUri[0]) ;      
      uiPortal.broadcast(pnevent, Event.Phase.PROCESS) ;
      uiPortal.getSelectedNode().setLabel(uri) ;
    }
  }
  
  static  public class ShowPageActionListener extends EventListener<UIPageIterator> {
    public void execute(Event<UIPageIterator> event) throws Exception {
      UIPageIterator uiPageIterator = event.getSource() ;
      int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      uiPageIterator.setCurrentPage(page) ;
      if(uiPageIterator.getParent() == null) return ;
      UIBrowseContainer uiBCContainer = uiPageIterator.getAncestorOfType(UIBrowseContainer.class) ;
      uiBCContainer.setShowPageAction(true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiBCContainer);
    }
  }

  static public class ViewByTagActionListener extends EventListener<UIBrowseContainer> {
    public void execute(Event<UIBrowseContainer> event) throws Exception {
      UIBrowseContainer uiContainer = event.getSource() ;
      String tagPath = event.getRequestContext().getRequestParameter(OBJECTID);
      uiContainer.setShowDocumentByTag(true) ;
      uiContainer.setTagPath(tagPath) ;
      uiContainer.setPageIterator(uiContainer.getDocumentByTag()) ;
      uiContainer.storeHistory() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }
}
