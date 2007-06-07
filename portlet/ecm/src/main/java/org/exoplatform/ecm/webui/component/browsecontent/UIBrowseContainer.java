/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.ecm.jcr.ECMViewComponent;
import org.exoplatform.ecm.jcr.JCRResourceResolver;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.portal.component.view.UIPortal;
import org.exoplatform.portal.component.view.UIPortlet;
import org.exoplatform.portal.component.view.Util;
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
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPageIterator;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Dec 14, 2006 5:15:47 PM
 */
@ComponentConfig(
    events = {
        @EventConfig(listeners = UIBrowseContainer.ChangeNodeActionListener.class),
        @EventConfig(listeners = UIBrowseContainer.BackActionListener.class),
        @EventConfig(listeners = UIBrowseContainer.ViewByTagActionListener.class),
        @EventConfig(listeners = UIBrowseContainer.SelectActionListener.class)
    }
)
public class UIBrowseContainer extends UIContainer implements ECMViewComponent {
  private boolean isShowCategoryTree_ = false ;
  private boolean isShowDocumentDetail_ = false ;
  private boolean isShowSearchForm_ = false ;
  private boolean isShowDocumentList_ = false ;
  private boolean isShowAllDocument_ = false ;
  private boolean isShowDocumentByTag_ = false ;
  private String tagPath_ = "" ;
  private TreeNode treeRoot_ ;
  private Node rootNode_ ;
  private Node currentNode_ ;
  private Node selectedTab_ ;
  private String oldTemplate_  ;
  private int rowPerBlock_ = 6 ;
  final public String documentView_ = "DocumentView" ;
  final public String KEY_CURRENT = "currentNode" ;
  final public String KEY_SELECTED = "selectedNode" ;
  private Map<String, Node> history_  ;
  private String templatePath_ ;
  private String categoryPath_ ;
  private String templateType_ ;
  private JCRResourceResolver jcrTemplateResourceResolver_ ;

  @SuppressWarnings("unused")
  public UIBrowseContainer() throws Exception {
    ManageViewService vservice = getApplicationComponent(ManageViewService.class) ;
    addChild(UIPageIterator.class, null, null) ;
    addChild(UIToolBar.class, null, null) ;
    addChild(UISearchController.class, null, null) ;    
    addChild(UIDocumentDetail.class, null, null).setRendered(false) ;
    loadPortletConfig(getPortletPreferences()) ;
  }

  public PortletPreferences getPortletPreferences() {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletRequest prequest = pcontext.getRequest() ;
    PortletPreferences portletPref = prequest.getPreferences() ;
    return portletPref ;
  }
  private String getTemplatePath(String ucase, String tempName) {
    ManageViewService viewService = getApplicationComponent(ManageViewService.class) ;
    List<Node> templateList = new ArrayList<Node>() ;  
    try {
      templateList.addAll(viewService.getAllTemplates(BasePath.CB_DETAIL_VIEW_TEMPLATES)) ;
      templateList.addAll(viewService.getAllTemplates(BasePath.CB_PATH_TEMPLATES)) ;
      templateList.addAll(viewService.getAllTemplates(BasePath.CB_QUERY_TEMPLATES)) ;
      templateList.addAll(viewService.getAllTemplates(BasePath.CB_SCRIPT_TEMPLATES)) ;    
      for(Node temp : templateList) {
        Node parentNode = temp.getParent() ; 
        if(parentNode.getName().equals(ucase)) return parentNode.getNode(tempName).getPath() ;      
      }
    } catch (Exception e) {
      e.printStackTrace() ;
    }
    return null ;
  }
  public void loadPortletConfig(PortletPreferences preferences ) throws Exception {
    String templateType = preferences.getValue(Utils.CB_USECASE, "") ;
    templateType_ = templateType ;
    String tempName = preferences.getValue(Utils.CB_TEMPLATE, "") ;
    setShowSearchForm(false) ;
    setShowDocumentDetail(false) ; 
    templatePath_ = getTemplatePath(templateType, tempName) ; 
    if(templateType.equals(Utils.CB_USE_FROM_PATH)) {
      String categoryPath = preferences.getValue(Utils.JCR_PATH, "") ;
      rootNode_ = (Node) getSession().getItem(categoryPath) ;
      if(isEnableToolBar()) initToolBar(false, true, true) ;
      else initToolBar(false, false, false) ;
      setPageIterator(getSubDocumentList(getSelectedTab())) ;
      if(preferences.getValue(Utils.CB_TEMPLATE, "").equals("TreeList")) {
        if(isEnableToolBar()) initToolBar(true, false, true) ;
        setTreeRoot(getRootNode()) ;
        buildTree(getRootNode().getPath()) ;
        setPageIterator(getSubDocumentList(getCurrentNode())) ;
      }
    } 
    if(templateType.equals(Utils.CB_USE_DOCUMENT)) {
      setShowDocumentDetail(true) ;
      String categoryPath = preferences.getValue(Utils.JCR_PATH, "") ;
      Node documentNode = getNodeByPath(categoryPath + preferences.getValue(Utils.CB_DOCUMENT_NAME, "")) ;
      initDocumentDetail(documentNode) ;
      if(isEnableToolBar()) initToolBar(false, false, false) ;
    } 
    if(templateType.equals(Utils.CB_USE_JCR_QUERY)) {
      if(isShowCommentForm() || isShowVoteForm()) initToolBar(false, false, false) ;
      setPageIterator(getNodeByQuery(-1)) ;
    } 
    if(templateType.equals(Utils.CB_USE_SCRIPT)) { 
      if(isShowCommentForm() || isShowVoteForm()) initToolBar(false, false, false) ;
      setPageIterator(getNodeByScript()) ;
    }
  }

  public String getTemplate() {return templatePath_ ;}

  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    if(jcrTemplateResourceResolver_ == null) newJCRTemplateResourceResolver() ;
    return jcrTemplateResourceResolver_ ;
  }

  public void newJCRTemplateResourceResolver() {
    try {
      jcrTemplateResourceResolver_ = new JCRResourceResolver(getSession(), "exo:templateFile") ;
    } catch (Exception e) {}
  }

  public boolean isEnableRefDocument() {
    return Boolean.parseBoolean(getPortletPreferences().getValue(Utils.CB_REF_DOCUMENT, "")) ;
  }
  public boolean isEnableChildDocument() {
    return Boolean.parseBoolean(getPortletPreferences().getValue(Utils.CB_CHILD_DOCUMENT, "")) ;
  }
  public boolean isEnableToolBar() {
    return Boolean.parseBoolean(getPortletPreferences().getValue(Utils.CB_VIEW_TOOLBAR, "")) ;
  }
  public boolean isShowTagmap() {
    return Boolean.parseBoolean(getPortletPreferences().getValue(Utils.CB_VIEW_TAGMAP, "")) ;
  }
  public boolean isShowCommentForm() {
    return Boolean.parseBoolean(getPortletPreferences().getValue(Utils.CB_VIEW_COMMENT, "")) ;
  }
  public boolean isCommentAndVote() { return (isShowVoteForm() || isShowCommentForm()) ;}
  public boolean isShowVoteForm() {
    return Boolean.parseBoolean(getPortletPreferences().getValue(Utils.CB_VIEW_VOTE, "")) ;
  }
  public boolean isShowDocumentByTag() {return isShowDocumentByTag_ ;}
  public void setShowDocumentByTag(boolean isShowByTag) {isShowDocumentByTag_ = isShowByTag;}

  public boolean isShowDocumentDetail() { return isShowDocumentDetail_ ;}
  public void setShowDocumentDetail(boolean isShowDocument) {isShowDocumentDetail_ = isShowDocument ;}
  public boolean isShowSearchForm() { return isShowSearchForm_ ;}
  public void setShowSearchForm(boolean isShowSearch) {isShowSearchForm_ = isShowSearch ;}
  public boolean isShowCategoryTree() {return isShowCategoryTree_ ;}
  public void setShowCategoryTree(boolean  isShowCategoryTree) {
    isShowCategoryTree_ = isShowCategoryTree ;
  }
  public boolean isShowAllDocument() {return isShowAllDocument_ ;}
  public void setShowAllChildren(boolean isShowAll) { isShowAllDocument_ = isShowAll ;}
  public void saveOldTemplateType(String templatePath) {oldTemplate_ = templatePath ;}
  public String getOldTemplate() {return oldTemplate_ ;}
  public String getWorkSpace() {
    return getPortletPreferences().getValue(Utils.WORKSPACE_NAME, "") ;
  }
  public String getCategoryPath() {return categoryPath_ ;}
  public void setCategoryPath(String path) {categoryPath_ = path ;}

  public String getQueryStatement() {
    return getPortletPreferences().getValue(Utils.CB_QUERY_STATEMENT, "") ;
  }
  public String getQueryLanguage() {
    return getPortletPreferences().getValue(Utils.CB_QUERY_LANGUAGE, "") ;
  }
  public int getItemPerPage() {
    return Integer.parseInt(getPortletPreferences().getValue(Utils.CB_NB_PER_PAGE, "")) ;
  }
  public int getRowPerBlock() {return rowPerBlock_ ;}
  public TreeNode getTreeRoot() { return treeRoot_ ;}
  public void setTreeRoot(Node node) throws Exception { treeRoot_ = new TreeNode(node) ;}
  public String[] getActions() { return new String[] {"back"} ;}
  public Node getNodeByUUID(String uuid) throws Exception{ return getSession().getNodeByUUID(uuid);}

  public Node getNodeByPath(String nodePath) throws Exception{
    try{
      return (Node)getSession().getItem(nodePath) ;
    } catch(Exception e){
      getSession().refresh(true) ;
      return null  ;
    }
  }
  public Node getRootNode() {return rootNode_ ;}
  public Node getCurrentNode() {
    if (currentNode_ == null) currentNode_ = getRootNode() ;
    return currentNode_ ;
  }
  public Node getSelectedTab() throws Exception {
    if (selectedTab_ == null) selectedTab_ = getCurrentNode() ;
    return selectedTab_ ;
  }
  protected boolean isCategories(NodeType nodeType) {
    for(String type : Utils.CATEGORY_NODE_TYPES) {
      if(nodeType.isNodeType(type)) return true ;
    }
    return false ;
  }

  private boolean canRead(Node node) {
    ExtendedNode eNode = (ExtendedNode)node ;
    try{
      eNode.checkPermission(PermissionType.READ) ;
      return true ;
    } catch(Exception ac){}
    return false ;
  }

  public Session getSession() throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    Session session = repositoryService.getRepository().getSystemSession(getWorkSpace()) ;
    return session ;
  }

  public void refresh() throws Exception {
    PortletPreferences preferences = getPortletPreferences() ; 
    if(templateType_.equals(Utils.CB_USE_FROM_PATH)) {
      setPageIterator(getSubDocumentList(getSelectedTab())) ;
      if(preferences.getValue(Utils.CB_TEMPLATE, "").equals("TreeList")) {
        setPageIterator(getSubDocumentList(getCurrentNode())) ;
      }
    } 
    if(templateType_.equals(Utils.CB_USE_DOCUMENT)) {
      setShowDocumentDetail(true) ;
    } 
    if(templateType_.equals(Utils.CB_USE_JCR_QUERY)) {
      setPageIterator(getNodeByQuery(-1)) ;
    } 
    if(templateType_.equals(Utils.CB_USE_SCRIPT)) { 
      setPageIterator(getNodeByScript()) ;
    }
  }

  public void setCurrentNode(Node node) throws Exception {currentNode_ = node ;}
  public void setSelectedTab (Node node) { selectedTab_ = node ;}
  public boolean isShowDocumentList() {return isShowDocumentList_ ;}
  public void setShowDocumentList(boolean isShowDocumentList){isShowDocumentList_ = isShowDocumentList ;}
  public boolean isRootNode() throws Exception {return getCurrentNode().equals(getRootNode()) ;}

  public String getOwner(Node node) throws Exception{
    return ((ExtendedNode) node).getACL().getOwner();  
  }

  private boolean isReferenceableNode(Node node) throws Exception {
    NodeType[] nodeTypes = node.getMixinNodeTypes() ;
    for(NodeType type : nodeTypes) {
      if(type.getName().equals(Utils.MIX_REFERENCEABLE)) return true ;
    }
    return false ;
  }

  public void initToolBar(boolean showTree, boolean showPath,boolean showSearch) throws Exception {
    UIToolBar toolBar = getChild(UIToolBar.class) ;
    toolBar.setEnableTree(showTree) ;
    toolBar.setEnablePath(showPath) ;
    toolBar.setEnableSearch(showSearch) ;
    toolBar.setRendered(true) ;
  }
  public void viewDocument(Node docNode, boolean hasDocDetail ,boolean hasDocList) throws Exception {
    setShowDocumentDetail(hasDocDetail) ;
    setShowDocumentList(hasDocList) ;
    initDocumentDetail(docNode) ;
    initToolBar(false, false, false) ;
    String templateType = Utils.CB_USE_DOCUMENT ;
    String tempName = getPortletPreferences().getValue(Utils.CB_BOX_TEMPLATE, "") ;
    templatePath_ = getTemplatePath(templateType, tempName) ;
  }
  public void initDocumentDetail(Node docNode) throws Exception {
    if(isShowDocumentDetail()) {
      UIDocumentDetail uiDocumetDetail = getChild(UIDocumentDetail.class) ;
      uiDocumetDetail.setNode(docNode) ;
      uiDocumetDetail.setRendered(true) ;
    }
  }

  public String getIcons(Node node, String type) throws Exception {
    return Utils.getNodeTypeIcon(node, type) ; 
  }

  public List getCurrentList() throws Exception {
    return getChild(UIPageIterator.class).getCurrentPageData() ;
  }
  public int getNumberOfPage() {
    return getChild(UIPageIterator.class).getAvailablePage();
  }
  public UIPageIterator getUIPageIterator() throws Exception {
    return getChild(UIPageIterator.class) ;
  }
  public void setPageIterator(List<Node> data) throws Exception {
    ObjectPageList objPageList = new ObjectPageList(data, getItemPerPage()) ;
    getChild(UIPageIterator.class).setPageList(objPageList) ;
  }

  public  List<Node> getNodeByQuery(int recoderNumber) throws Exception{
    List<Node> queryDocuments = new ArrayList<Node>() ;
    QueryManager queryManager = getSession().getWorkspace().getQueryManager();
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

  public List<Node> getNodeByScript() throws Exception {
    String[] array = getPortletPreferences().getValue(Utils.CB_SCRIPT_NAME, "").split(Utils.SEMI_COLON) ;
    DataTransfer data = new DataTransfer() ;
    ScriptService scriptService = (ScriptService)PortalContainer.getComponent(ScriptService.class) ;
    data.setWorkspace(array[0].trim()) ;
    data.setSession(getSession()) ;
    Node scripts = scriptService.getCBScriptHome() ;
    CmsScript cmsScript = scriptService.getScript(scripts.getName()+"/"+array[1].trim()) ;
    cmsScript.execute(data);
    return data.getContentList() ;
  }

  @SuppressWarnings("unchecked")
  public Map getContent() throws Exception {
    TemplateService templateService  = getApplicationComponent(TemplateService.class) ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    List templates = templateService.getDocumentTemplates() ;
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
            content.put(child.getName(), childOfSubCategory) ;
            subCategoryList.add(child.getPath()) ;
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

  public List<Node> getCategoryList(Node node) throws Exception{
    List<Node> nodes = new ArrayList<Node>() ;
    NodeIterator item = node.getNodes() ;
    while(item.hasNext()) {
      Node child = item.nextNode() ;
      if(isCategories(child.getPrimaryNodeType())) nodes.add(child) ; 
    }
    return nodes ;
  }

  public void buildTree(String path) throws Exception {
    treeRoot_.getChildren().clear() ;
    String[] arr = path.replaceFirst(treeRoot_.getPath(), "").split("/") ;
    TreeNode temp = treeRoot_ ;
    for(String nodeName : arr) {
      if(nodeName.length() == 0) continue ;
      temp.setChildren(getCategoryList(temp.getNode())) ;
      temp = temp.getChild(nodeName) ;
      if(temp == null) return ;
    }
    temp.setChildren(getCategoryList(temp.getNode())) ;
  }

  @SuppressWarnings("unchecked")
  public Map getPathContent() throws Exception {
    TemplateService templateService  = getApplicationComponent(TemplateService.class) ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    List templates = templateService.getDocumentTemplates() ;
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
            content.put(childNode.getName(), childOfSubCategory) ;
            subCategoryList.add(childNode.getPath()) ;
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
      if(!parent.getPath().equals(rootNode_.getPath())) content.put("previous", parent.getPath()) ;
      history = getHistory(templates, parent) ;
    }
    content.put("history", history) ;
    return content ;
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
  
  protected void historyNext() {}
  
  protected void historyBack() {}
  
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

  private RepositoryService getRepositoryService() { 
    return getApplicationComponent(RepositoryService.class) ;
  }
  private List<Node> getReferences(RepositoryService repositoryService, Node node, boolean isShowAll,
      int size, List templates) throws Exception {
    List<Node> refDocuments = new ArrayList<Node>() ;
    if(isEnableRefDocument() && isReferenceableNode(node)) {
      String uuid = node.getUUID() ;
      String[] workspaces = repositoryService.getRepository().getWorkspaceNames() ;
      int itemCounter = getRowPerBlock() - size ;
      if(isShowAll) itemCounter = getItemPerPage() - size ;
      for(String workspace : workspaces) {
        Session session = repositoryService.getRepository().getSystemSession(workspace) ;
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

  public List<Node> getTagLink() throws Exception {
    FolksonomyService folksonomyService = getApplicationComponent(FolksonomyService.class) ;
    return folksonomyService.getAllTags() ;
  }

  public String getTagPath() {return tagPath_ ;}

  public void setTagPath(String tagName) {tagPath_ = tagName ;}

  public List<Node> getDocumentByTag()throws Exception {
    FolksonomyService folksonomyService = getApplicationComponent(FolksonomyService.class) ;
    return folksonomyService.getDocumentsOnTag(getTagPath()) ;
  }

  public Map<String ,String> getTagStyle() throws Exception {
    FolksonomyService folksonomyService = getApplicationComponent(FolksonomyService.class) ;
    Map<String , String> tagStyle = new HashMap<String ,String>() ;
    for(Node tag : folksonomyService.getAllTagStyle()) {
      tagStyle.put(tag.getName(), tag.getProperty("exo:htmlStyle").getValue().getString()) ;
    }
    return tagStyle ;
  }

  public void changeNode(Node selectNode) throws Exception {
    setShowDocumentByTag(false) ;
    setShowAllChildren(false) ;
    if(selectNode.equals(getRootNode())) {
      setCurrentNode(null) ;
      setSelectedTab(null) ;
      loadPortletConfig(getPortletPreferences()) ;
      return ;
    }
    setSelectedTab(selectNode) ;
    setCurrentNode(selectNode.getParent()) ;
    setPageIterator(getSubDocumentList(getSelectedTab())) ;
  }

  @SuppressWarnings("unchecked")
  public List<Node> getSubDocumentList(Node selectedNode) throws Exception {
    List<Node> subDocumentList = new ArrayList<Node>() ;
    if(selectedNode == null) return subDocumentList ;
    TemplateService templateService  = getApplicationComponent(TemplateService.class) ;
    List<String> templates = templateService.getDocumentTemplates() ;
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

  public void storeHistory() throws Exception {
    if(history_ == null) history_ = new HashMap<String, Node>() ;
    history_.clear() ;
    history_.put(KEY_CURRENT, getCurrentNode());
    history_.put(KEY_SELECTED, getSelectedTab());
  }

  public void selectNode(Node node) throws Exception {
    setCurrentNode(node) ;
    buildTree(node.getPath()) ;
  }

  static public class ChangeNodeActionListener extends EventListener<UIBrowseContainer> {
    public void execute(Event<UIBrowseContainer> event) throws Exception {
      UIBrowseContainer uiContainer = event.getSource() ;
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String catPath = event.getRequestContext().getRequestParameter("category") ;  
      if(objectId.lastIndexOf(Utils.SEMI_COLON) > 0) {
        uiContainer.storeHistory() ;
        String path = objectId.substring(objectId.lastIndexOf(Utils.SEMI_COLON)+1) ;
        Node selected = uiContainer.getNodeByPath(path) ;
        uiContainer.changeNode(selected) ;
        uiContainer.setShowAllChildren(true) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
        return ;
      }
      Node selectNode = uiContainer.getNodeByPath(objectId) ;
      if(selectNode == null) {
        UIApplication app = uiContainer.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UIBrowseContainer.msg.invalid-node", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(app.getUIPopupMessages()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
        return ;
      }
      TemplateService templateService  = uiContainer.getApplicationComponent(TemplateService.class) ;
      List templates = templateService.getDocumentTemplates() ;
      if(templates.contains(selectNode.getPrimaryNodeType().getName())) {
        if(catPath != null) {
          uiContainer.setCategoryPath(catPath) ;
          Node currentCat  = uiContainer.getNodeByPath(catPath);
          uiContainer.storeHistory() ;
          uiContainer.setPageIterator(uiContainer.getSubDocumentList(currentCat)) ;
          if(uiContainer.isShowDocumentByTag_) uiContainer.setPageIterator(uiContainer.getDocumentByTag()) ;
        } 
        uiContainer.viewDocument(selectNode, true, true) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
        return ;
      }
      String templateType = uiContainer.getPortletPreferences().getValue(Utils.CB_USECASE, "") ;
      if((templateType.equals(Utils.CB_USE_JCR_QUERY)) || (templateType.equals(Utils.CB_SCRIPT_NAME))) {
        UIApplication app = uiContainer.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UIBrowseContainer.msg.template-notsupported", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(app.getUIPopupMessages()) ;
        return ;
      }
      uiContainer.changeNode(selectNode) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }

  static public class BackActionListener extends EventListener<UIBrowseContainer> {
    public void execute(Event<UIBrowseContainer> event) throws Exception {
      UIBrowseContainer uiContainer = event.getSource() ;
      if(uiContainer.isShowDocumentByTag()) uiContainer.setShowDocumentByTag(false) ;
      if(uiContainer.isShowDocumentDetail()) {
        UIDocumentDetail uiDocumentDetail = uiContainer.getChild(UIDocumentDetail.class) ;      
        uiContainer.setShowDocumentDetail(false) ;
        uiDocumentDetail.setRendered(false) ;
      }
      if(uiContainer.isShowAllDocument()) uiContainer.setShowAllChildren(false) ;
      uiContainer.setCurrentNode(uiContainer.history_.get(uiContainer.KEY_CURRENT)) ;
      uiContainer.setSelectedTab(uiContainer.history_.get(uiContainer.KEY_SELECTED)) ;
      uiContainer.history_.clear() ;
      uiContainer.loadPortletConfig(uiContainer.getPortletPreferences()) ;
      if(uiContainer.treeRoot_ != null) uiContainer.buildTree(uiContainer.getCurrentNode().getPath()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
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

  static public class SelectActionListener extends EventListener<UIBrowseContainer> {
    public void execute(Event<UIBrowseContainer> event) throws Exception {
      UIBrowseContainer uiContainer = event.getSource() ;
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Node node = uiContainer.getNodeByPath(path) ;
      if(node == null) {
        UIApplication app = uiContainer.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UIBrowseContainer.msg.invalid-node", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(app.getUIPopupMessages()) ;
        return ;
      }
      uiContainer.selectNode(node) ;
      uiContainer.setPageIterator(uiContainer.getSubDocumentList(uiContainer.getCurrentNode())) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }

  public String encodeHTML(String text) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Node> getAttachments() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Node> getComments() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Object getComponentInstanceOfType(String className) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getDownloadLink(Node node) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public String getImage(Node node) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource dresource ;
    Node imageNode = node.getNode(Utils.EXO_IMAGE) ;
    InputStream input = imageNode.getProperty(Utils.JCR_DATA).getStream() ;
    dresource = new InputStreamDownloadResource(input, "image") ;
    dresource.setDownloadName(node.getName()) ;
    return dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
  }

  public String getLanguage() {
    // TODO Auto-generated method stub
    return null;
  }

  public Node getNode() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public String getNodeType() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Node getOriginalNode() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public String getPortalName() {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Node> getRelations() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public String getRssLink() {
    // TODO Auto-generated method stub
    return null;
  }

  public List getSupportedLocalise() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public String getTemplatePath() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public String getViewTemplate(String nodeTypeName, String templateName) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public String getWebDAVServerPrefix() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public String getWorkspaceName() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isNodeTypeSupported() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isRssLink() {
    // TODO Auto-generated method stub
    return false;
  }

  public void setLanguage(String language) {
    // TODO Auto-generated method stub

  }

  public void setNode(Node node) {
    // TODO Auto-generated method stub

  }

}
