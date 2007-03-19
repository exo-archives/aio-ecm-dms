/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import org.exoplatform.ecm.jcr.JCRResourceResolver;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.folksonomy.FolksonomyService;
import org.exoplatform.services.cms.scripts.CmsScript;
import org.exoplatform.services.cms.scripts.DataTransfer;
import org.exoplatform.services.cms.scripts.ScriptService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.templates.groovy.ResourceResolver;
import org.exoplatform.webui.application.RequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
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
   //template = "app:/groovy/webui/component/browse/test.gtmpl",
    events = {
        @EventConfig(listeners = UIBrowseContainer.ChangeNodeActionListener.class),
        @EventConfig(listeners = UIBrowseContainer.BackActionListener.class),
        @EventConfig(listeners = UIBrowseContainer.ViewByTagActionListener.class),
        @EventConfig(listeners = UIBrowseContainer.CloseActionListener.class),
        @EventConfig(listeners = UIBrowseContainer.SelectActionListener.class)
    }
)
public class UIBrowseContainer extends UIContainer {
  private boolean isShowCategoryTree_ = true ;
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
  private JCRResourceResolver jcrTemplateResourceResolver_ ;

  public UIBrowseContainer() throws Exception {
    addChild(UIPageIterator.class, null, null) ;
    addChild(UIToolBar.class, null, null).setRendered(false) ;
    addChild(UIDocumentDetail.class, null, null).setRendered(false) ;
    loadPortletConfig(getPortletPreferences()) ;
  }

  public PortletPreferences getPortletPreferences() {
    PortletRequestContext pcontext = (PortletRequestContext)RequestContext.getCurrentInstance() ;
    PortletRequest prequest = pcontext.getRequest() ;
    PortletPreferences portletPref = prequest.getPreferences() ;
    return portletPref ;
  }


  public void loadPortletConfig(PortletPreferences preferences ) throws Exception {
    String templateType = preferences.getValue(Utils.CB_USECASE, "") ;
    String templateName = preferences.getValue(Utils.CB_TEMPLATE, "") ;
    CmsConfigurationService cmsConfiguration = getApplicationComponent(CmsConfigurationService.class) ;
    setShowSearchForm(false) ;
    String categoryPath = preferences.getValue(Utils.JCR_PATH, "") ;
    rootNode_ = (Node) getSession().getItem(categoryPath) ;
    if(templateType.equals(Utils.CB_USE_FROM_PATH)) {
      currentNode_ = null ;
      selectedTab_ = null ;
      if(isEnableToolBar()) initToolBar(false, true, true) ;
      else initToolBar(false, false, false) ;
      templatePath_ = cmsConfiguration.getJcrPath(BasePath.CB_PATH_TEMPLATES) + Utils.SLASH + templateName  ;
      setPageIterator(getSubDocumentList(getSelectedTab())) ;
      if(preferences.getValue(Utils.CB_TEMPLATE, "").equals("TreeList")) {
        if(isEnableToolBar()) initToolBar(true, false, true) ;
        setTreeRoot(getRootNode()) ;
        buildTree(getRootNode().getPath()) ;
      }
    } 
    if(templateType.equals(Utils.CB_USE_DOCUMENT)) {
      Node documentNode = getNodeByPath(categoryPath + preferences.getValue(Utils.CB_DOCUMENT_NAME, "")) ;
      initDocumentDetail(documentNode) ;
      if(isEnableToolBar()) initToolBar(false, false, false) ;
      templatePath_ = cmsConfiguration.getJcrPath(BasePath.CB_DETAIL_VIEW_TEMPLATES) + Utils.SLASH + templateName  ;
    } 
    if(templateType.equals(Utils.CB_USE_JCR_QUERY)) {
      if(isShowCommentForm() || isShowVoteForm()) initToolBar(false, false, false) ;
      setPageIterator(getNodeByQuery()) ;
      templatePath_ = cmsConfiguration.getJcrPath(BasePath.CB_QUERY_TEMPLATES) + Utils.SLASH  + templateName  ;
    } 
    if(templateType.equals(Utils.CB_USE_SCRIPT)) { 
      if(isShowCommentForm() || isShowVoteForm()) initToolBar(false, false, false) ;
      templatePath_ = cmsConfiguration.getJcrPath(BasePath.CB_SCRIPT_TEMPLATES) + Utils.SLASH  + templateName  ;
    }
  }

  public String getTemplate() {return templatePath_ ; }

  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(RequestContext context, String template) {
    if(jcrTemplateResourceResolver_ == null) newJCRTemplateResourceResolver() ;
    return jcrTemplateResourceResolver_ ;
  }

  public void newJCRTemplateResourceResolver() {
    try {
      jcrTemplateResourceResolver_ = new JCRResourceResolver(getSession(), "exo:templateFile") ;
    } catch (Exception e) {
      throw new RuntimeException() ;
    }
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
  public Node getNodeByPath(String nodePath) throws Exception {
    return (Node)getSession().getItem(nodePath) ;
  }
  public Node getRootNode() {return rootNode_ ;}
  public Node getCurrentNode() {
    if (currentNode_ == null) currentNode_ = rootNode_ ;
    return currentNode_ ;
  }
  public Node getSelectedTab() throws Exception {
    if (selectedTab_ == null){
      NodeIterator iter = getCurrentNode().getNodes() ;
      if(iter.hasNext()) selectedTab_ = iter.nextNode() ;
    }
    return selectedTab_ ;
  }


  private boolean isCategories(NodeType nodeType) {
    for(String type:Utils.CATEGORY_NODE_TYPES) {
      if(nodeType.isNodeType(type)) return true ;
    }
    return false ;
  }


  public Session getSession() throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    Session session = repositoryService.getRepository().getSystemSession(getWorkSpace()) ;
    return session ;
  }
  public void setCurrentNode(Node node) throws Exception {currentNode_ = node ;}
  public void setSelectedTab (Node node) { selectedTab_ = node ;}
  public boolean isShowDocumentList() {return isShowDocumentList_ ;}
  public void setShowDocumentList(boolean isShowDocumentList){isShowDocumentList_ = isShowDocumentList ;}
  public boolean isRootNode() throws Exception {return getCurrentNode().equals(getRootNode()) ;}
  public String getOwner(Node node) throws Exception{
    return ((ExtendedNode)node).getACL().getOwner() ;
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
    toolBar.getChild(UICBSearchForm.class).setRendered(false) ;
    toolBar.getChild(UICBSearchResults.class).setRendered(false) ;
  }

  public void initDocumentDetail(Node docNode) throws Exception {
    if(isShowDocumentDetail()) {
      UIDocumentDetail uiDocumetDetail = getChild(UIDocumentDetail.class) ;
      uiDocumetDetail.setNode(docNode) ;
      uiDocumetDetail.setRendered(true) ;
    }
  }

  public void initDocumentList(Node node) throws Exception {
    if(isShowDocumentDetail() && isShowDocumentList()) {
      UIDocumentList uiList = getChild(UIDocumentList.class) ;
      if(uiList == null ) uiList = addChild(UIDocumentList.class, null, null) ;
      uiList.setDocNode(node) ;
      uiList.updateGrid() ;
    }
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
  public  List<Node> getNodeByQuery() throws Exception{
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
    while (iter.hasNext()) {
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
    int itemCounter = getRowPerBlock() ;
    if(isShowAllDocument()) itemCounter = getItemPerPage();
    while(childIter.hasNext()) {
      Node child = childIter.nextNode() ;
      if(templates.contains(child.getPrimaryNodeType().getName())) {
        subDocumentList.add(child) ;
      } else {
        if(isCategories(child.getPrimaryNodeType())) {
          Map childOfSubCategory = getChildOfSubCategory(repositoryService, child, templates) ;
          content.put(child.getName(), childOfSubCategory) ;
          subCategoryList.add(child.getPath()) ;
        }
      }
    }
    content.put("subCategoryList", subCategoryList) ;
    content.put("subDocumentList", subDocumentList) ;
    return content ;
  }
  
  public List<Node> getChildrenList(Node node, List filter) throws Exception{
    List<Node> nodes = new ArrayList<Node>() ;
    NodeIterator item = node.getNodes() ;
    while(item.hasNext()) {
      Node child = item.nextNode() ;
      if(!filter.contains(child.getPrimaryNodeType().getName())) nodes.add(child) ;
    }
    return nodes ;
  }
  public void buildTree(String path) throws Exception {
    TemplateService templateService  = getApplicationComponent(TemplateService.class) ;
    List filter = templateService.getDocumentTemplates() ;
    treeRoot_.getChildren().clear() ;
    String[] arr = path.replaceFirst(treeRoot_.getPath(), "").split("/") ;
    TreeNode temp = treeRoot_ ;
    for(String nodeName : arr) {
      if(nodeName.length() == 0) continue ;
      temp.setChildren(getChildrenList(temp.getNode(), filter)) ;
      temp = temp.getChild(nodeName) ;
      if(temp == null) return ;
    }
    temp.setChildren(getChildrenList(temp.getNode(), filter)) ;
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
    NodeIterator tabIter = getCurrentNode().getNodes() ;
    boolean isShowDocument = isEnableChildDocument() ;
    boolean isShowReferenced = isEnableRefDocument() ;
    int itemCounter = getRowPerBlock() ;
    if(isShowAllDocument()) itemCounter = getItemPerPage();
    while(tabIter.hasNext()) {
      Node tab = tabIter.nextNode() ;
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
      if(!documentTemplates.contains(item.getPrimaryNodeType().getName())) subCategories.add(item.getPath()) ;
      else if(isShowDocument) {
        if(childDocOrReferencedDoc.size() < getRowPerBlock()) childDocOrReferencedDoc.add(item) ;
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
      setCurrentNode(getRootNode()) ;
      setSelectedTab(null) ;
      return ;
    }
    setSelectedTab(selectNode) ;
    setCurrentNode(selectNode.getParent()) ;
    setPageIterator(getSubDocumentList(getSelectedTab())) ;
  }

  @SuppressWarnings("unchecked")
  public List<Node> getSubDocumentList(Node selectedTab) throws Exception {
    TemplateService templateService  = getApplicationComponent(TemplateService.class) ;
    List<String> templates = templateService.getDocumentTemplates() ;
    List<Node> subDocumentList = new ArrayList<Node>() ;
    NodeIterator item = selectedTab.getNodes() ;
    if(isEnableChildDocument())
      while (item.hasNext()) {
        Node node = item.nextNode() ;
        if(templates.contains(node.getPrimaryNodeType().getName())){
          subDocumentList.add(node) ; }
      }
    if(isEnableRefDocument()) subDocumentList.addAll(getReferences(getRepositoryService(),
        getSelectedTab(), isShowAllDocument(), subDocumentList.size(), templates)) ;
    return subDocumentList ;
  }

  public void viewDocument(Node docNode) throws Exception {
    setShowDocumentDetail(true) ;
    setShowDocumentList(true) ;
    initDocumentDetail(docNode) ;
    initDocumentList(docNode) ;
    CmsConfigurationService cmsConfiguration = getApplicationComponent(CmsConfigurationService.class) ;
    templatePath_ = cmsConfiguration.getJcrPath(BasePath.CB_DETAIL_VIEW_TEMPLATES) + Utils.SLASH +
                                                         documentView_  ;
  }
  public void storeHistory() throws Exception {
    if(history_ == null) history_ = new HashMap<String, Node>() ;
    history_.clear() ;
    history_.put(KEY_CURRENT, getCurrentNode());
    history_.put(KEY_SELECTED, getSelectedTab());
  }
  public void back() throws Exception {
    if((history_ != null)&&(history_.size() > 0 )) {
      setCurrentNode(history_.get(KEY_CURRENT)) ;
      setSelectedTab(history_.get(KEY_SELECTED)) ;
      history_.clear() ;
    }
    getChild(UIDocumentDetail.class).setRendered(false) ;
    getChild(UIDocumentList.class).setRendered(false) ;
    setPageIterator(getSubDocumentList(getSelectedTab())) ;
  }

  @SuppressWarnings("unchecked")
  static public class ChangeNodeActionListener extends EventListener<UIBrowseContainer> {
    public void execute(Event<UIBrowseContainer> event) throws Exception {
      UIBrowseContainer uiContainer = event.getSource() ;
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
      if(objectId.lastIndexOf(Utils.SEMI_COLON) > 0) {
        uiContainer.storeHistory() ;
        String path = objectId.substring(objectId.lastIndexOf(Utils.SEMI_COLON)+1) ;
        Node selected = uiContainer.getNodeByPath(path) ;
        uiContainer.changeNode(selected) ;
        uiContainer.setShowAllChildren(true) ;
        return ;
      }
      if(objectId.equals("ViewMore")) {
        uiContainer.setShowAllChildren(true) ;
        return ;
      }
      Node selectNode = uiContainer.getNodeByPath(objectId) ;
      TemplateService templateService  = uiContainer.getApplicationComponent(TemplateService.class) ;
      List<String> templates = templateService.getDocumentTemplates() ;
      if(templates.contains(selectNode.getPrimaryNodeType().getName())) {
        uiContainer.storeHistory() ;
        uiContainer.viewDocument(selectNode) ;
        return ;
      }
      uiContainer.changeNode(selectNode) ;
    }
  }

  static public class BackActionListener extends EventListener<UIBrowseContainer> {
    public void execute(Event<UIBrowseContainer> event) throws Exception {
      UIBrowseContainer uiContainer = event.getSource() ;
      uiContainer.setShowDocumentDetail(false) ;
      uiContainer.loadPortletConfig(uiContainer.getPortletPreferences()) ;
      uiContainer.back() ;
    }
  }
  static public class ViewByTagActionListener extends EventListener<UIBrowseContainer> {
    public void execute(Event<UIBrowseContainer> event) throws Exception {
      UIBrowseContainer uiContainer = event.getSource() ;
      String tagPath = event.getRequestContext().getRequestParameter(OBJECTID);
      uiContainer.setShowDocumentByTag(true) ;
      uiContainer.setTagPath(tagPath) ;
      uiContainer.setPageIterator(uiContainer.getDocumentByTag()) ;
    }
  }
  static public class CloseActionListener extends EventListener<UIBrowseContainer> {
    public void execute(Event<UIBrowseContainer> event) throws Exception {
      UIBrowseContainer uiContainer = event.getSource() ;
      uiContainer.setShowAllChildren(false) ;
      uiContainer.setShowDocumentByTag(false) ;
      uiContainer.setPageIterator(uiContainer.getSubDocumentList(uiContainer.getSelectedTab())) ;
    }
  }
  
  static public class SelectActionListener extends EventListener<UIBrowseContainer> {
    public void execute(Event<UIBrowseContainer> event) throws Exception {
      UIBrowseContainer uiContainer = event.getSource() ;
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Node node = uiContainer.getNodeByPath(path) ;
      uiContainer.setCurrentNode(node) ;
      uiContainer.buildTree(path) ;
    }
  }

}
