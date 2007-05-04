/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.control;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.ecm.jcr.ECMNameValidator;
import org.exoplatform.ecm.jcr.model.Preference;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIJCRBrowser;
import org.exoplatform.ecm.webui.component.UIVoteForm;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentWorkspace;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIPopupAction;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIAddLanguageContainer;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UICommentForm;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIDocumentForm;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIDocumentFormController;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIFolderForm;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIMultiLanguageManager;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UITaggingForm;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIUploadForm;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIWatchDocumentForm;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionContainer;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionForm;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionManager;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionTypeForm;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UICategoriesAddedList;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UICategoryManager;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIExportNode;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIImportNode;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIPropertiesManager;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIRelationManager;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIRelationsAddedList;
import org.exoplatform.ecm.webui.component.explorer.popup.info.UINodeTypeInfo;
import org.exoplatform.ecm.webui.component.explorer.popup.info.UIPermissionManager;
import org.exoplatform.ecm.webui.component.explorer.popup.info.UIReferencesList;
import org.exoplatform.ecm.webui.component.explorer.popup.info.UIViewMetadataContainer;
import org.exoplatform.ecm.webui.component.explorer.popup.info.UIViewMetadataManager;
import org.exoplatform.ecm.webui.component.explorer.popup.info.UIViewMetadataTemplate;
import org.exoplatform.ecm.webui.component.explorer.search.UIECMSearch;
import org.exoplatform.ecm.webui.component.explorer.search.UISearchContainer;
import org.exoplatform.ecm.webui.component.explorer.search.UISearchResult;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UISideBar;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UITreeExplorer;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UIViewRelationList;
import org.exoplatform.ecm.webui.component.explorer.versions.UIActivateVersion;
import org.exoplatform.ecm.webui.component.explorer.versions.UIVersionInfo;
import org.exoplatform.portal.component.view.Util;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.categories.CategoriesService;
import org.exoplatform.services.cms.metadata.MetadataService;
import org.exoplatform.services.cms.queries.QueryService;
import org.exoplatform.services.cms.relations.RelationsService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Aug 2, 2006
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/explorer/control/UIActionBar.gtmpl",
    events = {
        @EventConfig(listeners = UIActionBar.AddFolderActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.AddDocumentActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.EditDocumentActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.UploadActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.SearchActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.WatchDocumentActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.TaggingDocumentActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.MultiLanguageActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.ViewReferencesActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.ViewNodeTypeActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.ViewPermissionsActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.ViewPropertiesActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.ViewRelationsActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.ShowJCRStructureActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.ManageVersionsActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.ManageCategoriesActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.ManageRelationsActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.ManageActionsActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.ExportNodeActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.ImportNodeActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.SimpleSearchActionListener.class),
        @EventConfig(listeners = UIActionBar.AdvanceSearchActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.ViewMetadatasActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.ChangeTabActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.VoteActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIActionBar.CommentActionListener.class, phase = Phase.DECODE)
    }
)

public class UIActionBar extends UIForm {

  private Node view_ ;
  private String templateName_ ;
  private List<SelectItemOption<String>> tabOptions = new ArrayList<SelectItemOption<String>>() ;
  private List<String[]> tabs_ = new ArrayList<String[]>();

  final static private String FIELD_SELECT_TAB = "tabs" ;
  final static private String FIELD_SIMPLE_SEARCH = "simpleSearch" ;
  final static private String FIELD_ADVANCE_SEARCH = "advanceSearch" ;
  final static private String FIELD_SEARCH_TYPE = "searchType" ;
  final static private String OPT_SEARCH = "Search" ;
  final static private String EXO_TAXONOMIES_PATH = "exoTaxonomiesPath" ;
  final static private String CMS_PATH = "cmsPath" ;
  final static private String FIELD_SQL = "SQL" ;
  final static private String FIELD_XPATH = "xPath" ;

  final static private String ROOT_SQL_QUERY = "select * from nt:base where contains(*, '$1')" ;
  final static private String SQL_QUERY = "select * from nt:base where jcr:path like '$0/%' and contains(*, '$1')" ;
  private static final String ROOT_PATH_SQL_QUERY = "select * from nt:base where jcr:path like '%/$1' ";
  private static final String PATH_SQL_QUERY = "select * from nt:base where jcr:path like '$0/%/$1' ";
  
  public UIActionBar() throws Exception{
    UIFormSelectBox selectTab  = new UIFormSelectBox(FIELD_SELECT_TAB, FIELD_SELECT_TAB, tabOptions) ;
    selectTab.setOnChange("ChangeTab") ;
    addUIFormInput(selectTab) ;
    addChild(new UIFormStringInput(FIELD_SIMPLE_SEARCH, FIELD_SIMPLE_SEARCH, null).addValidator(ECMNameValidator.class)) ;
    
    List<SelectItemOption<String>> typeOptions = new ArrayList<SelectItemOption<String>>() ;
    typeOptions.add(new SelectItemOption<String>(FIELD_SQL, Query.SQL)) ;
    typeOptions.add(new SelectItemOption<String>(FIELD_XPATH, Query.XPATH)) ;
    addChild(new UIFormSelectBox(FIELD_SEARCH_TYPE, FIELD_SEARCH_TYPE, typeOptions)) ;
    addChild(new UIFormStringInput(FIELD_ADVANCE_SEARCH, FIELD_ADVANCE_SEARCH, null)) ;
  }

  public void setTabOptions(String viewName) throws Exception {
    tabOptions.clear() ;
    tabs_.clear() ;
    tabs_ = new ArrayList<String[]>() ;
    ManageViewService vservice = getApplicationComponent(ManageViewService.class) ;
    view_ = vservice.getViewHome().getNode(viewName) ;
    NodeIterator tabs = view_.getNodes() ;
    int i = 0;
    while (tabs.hasNext()) {
      Node tab = tabs.nextNode();
      tabOptions.add(new SelectItemOption<String>(tab.getName(), String.valueOf(i++))) ;
      setListButton(tab.getName()) ;
    }
    tabOptions.add(new SelectItemOption<String>(OPT_SEARCH, String.valueOf(i++))) ;
    getUIFormSelectBox(FIELD_SELECT_TAB).setOptions(tabOptions).setValue(tabOptions.get(0).getValue()) ;
    String template = view_.getProperty("exo:template").getString() ;
    templateName_ = template.substring(template.lastIndexOf("/") + 1) ;
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    uiExplorer.setRenderTemplate(template) ;
  }

  public String getTemplateName() { return templateName_ ;  }
  
  private void setListButton(String tabName) throws PathNotFoundException, RepositoryException {
    Node tabNode = view_.getNode(tabName) ;
    String buttons = tabNode.getProperty("exo:buttons").getString() ;
    String[] buttonsInTab = StringUtils.split(buttons, ";") ;
    for(int j = 0 ; j < buttonsInTab.length ; j ++ ) {
      String buttonName = buttonsInTab[j].trim() ;
      buttonName = buttonName.substring(0, 1).toUpperCase() + buttonName.substring(1);
      buttonsInTab[j] = buttonName ;
    }
    tabs_.add(buttonsInTab) ;
  }
  
  public List<String[]> getTabs() throws Exception { return tabs_ ; }
  
  public int getSelectedTab() {
    return Integer.parseInt(getUIFormSelectBox(FIELD_SELECT_TAB).getValue()) ;
  }
  
  public List<Query> getSavedQueries() throws Exception {
    String userName = Util.getUIPortal().getOwner() ;
    return getApplicationComponent(QueryService.class).getQueries(userName) ;
  }
  
  public List<String> getMetadataTemplates() throws Exception {
    MetadataService metadataService = getApplicationComponent(MetadataService.class) ;
    Node node = getAncestorOfType(UIJCRExplorer.class).getCurrentNode() ;
    List<String> templates = new ArrayList<String>();
    
    NodeType[] nodeTypes = node.getMixinNodeTypes();
    for(NodeType nt : nodeTypes) {
      try {
        String path = metadataService.getMetadataPath(nt.getName(), false);
        templates.add("jcr:" + path);
      } catch(Exception e) {
      }
    }
    Item primaryItem = null;
    try {
      primaryItem = node.getPrimaryItem();
    } catch (ItemNotFoundException e) {
    }
    if (primaryItem != null && primaryItem.isNode()) {
      Node primaryNode = (Node) node.getPrimaryItem();
      NodeType[] primaryTypes = primaryNode.getMixinNodeTypes();
      for(NodeType nt : primaryTypes) {
        String path = metadataService.getMetadataPath(nt.getName(), false);
        templates.add("jcr:" + path);
      }
    }
    return templates;
  }
  
  static public class AddFolderActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class) ;
      Node currentNode = uiExplorer.getCurrentNode() ;
      Session session = uiExplorer.getSession() ;
      if(uiExplorer.isPreferenceNode(currentNode)) {
        String preferenceWS = currentNode.getSession().getWorkspace().getName() ;
        session = uiExplorer.getSessionByWorkspace(preferenceWS) ;
      }
      if(uiExplorer.nodeIsLocked(currentNode.getPath(), session)) {
        Object[] arg = { uiExplorer.getCurrentNode().getPath() } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }
      UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(UIFolderForm.class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class AddDocumentActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class) ;
      Session session = uiExplorer.getSession() ;
      if(uiExplorer.isPreferenceNode(uiExplorer.getCurrentNode())) {
        String preferenceWS = uiExplorer.getCurrentNode().getSession().getWorkspace().getName() ;
        session = uiExplorer.getSessionByWorkspace(preferenceWS) ;
      }
      if(uiExplorer.nodeIsLocked(uiExplorer.getCurrentNode().getPath(), session)) {
        Object[] arg = { uiExplorer.getCurrentNode().getPath() } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }
      UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(UIDocumentFormController.class, null, 700, 550) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
     }
  }

  static public class EditDocumentActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uicomp = event.getSource() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      Node selectedNode = uiExplorer.getCurrentNode() ;
      Session session = uiExplorer.getSession() ;
      if(uiExplorer.isPreferenceNode(uiExplorer.getCurrentNode())) {
        String preferenceWS = uiExplorer.getCurrentNode().getSession().getWorkspace().getName() ;
        session = uiExplorer.getSessionByWorkspace(preferenceWS) ;
      }      
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class) ;
      if(uiExplorer.nodeIsLocked(selectedNode.getPath(), session)) {
        Object[] arg = { selectedNode.getPath() } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }
      if(selectedNode.isNodeType(Utils.EXO_ACTION)) {
        UIActionContainer uiContainer = uiExplorer.createUIComponent(UIActionContainer.class, null, null) ;
        uiExplorer.setIsHidePopup(true) ;
        UIActionForm uiActionForm =  uiContainer.getChild(UIActionForm.class) ;
        uiContainer.getChild(UIActionTypeForm.class).setRendered(false) ;
        uiActionForm.createNewAction(selectedNode, selectedNode.getPrimaryNodeType().getName(), false) ;
        uiActionForm.setNode(selectedNode) ;
        UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class) ;
        uiPopupAction.activate(uiContainer, 700, 550) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      } else {
        TemplateService tservice = uicomp.getApplicationComponent(TemplateService.class) ;
        List documentNodeType = tservice.getDocumentTemplates() ;
        String nodeType = selectedNode.getPrimaryNodeType().getName() ;
        if(documentNodeType.contains(nodeType)){
          UIDocumentForm uiDocumentForm = 
            uiExplorer.createUIComponent(UIDocumentForm.class, null, null) ;
          uiDocumentForm.setTemplateNode(nodeType) ;
          uiDocumentForm.setNode(selectedNode) ;
          uiDocumentForm.addNew(false) ;
          UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class) ;
          uiPopupAction.activate(uiDocumentForm, 700, 550) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        } else {
          uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.not-support", null)) ;
          return ;
        }
      }
    }
  }

  static public class UploadActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class) ;
      Session session = uiExplorer.getSession() ;
      if(uiExplorer.isPreferenceNode(uiExplorer.getCurrentNode())) {
        String preferenceWS = uiExplorer.getCurrentNode().getSession().getWorkspace().getName() ;
        session = uiExplorer.getSessionByWorkspace(preferenceWS) ;
      }         
      if(uiExplorer.nodeIsLocked(uiExplorer.getCurrentNode().getPath(), session)) {
        Object[] arg = { uiExplorer.getCurrentNode().getPath() } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }
      UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(UIUploadForm .class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class SearchActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiJCRExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      UIPopupAction uiPopupAction = uiJCRExplorer.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(UIECMSearch .class, 700) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class WatchDocumentActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      UIJCRExplorer uiExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class) ;
      NodeType nodeType = uiExplorer.getCurrentNode().getPrimaryNodeType() ;
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
      Session session = uiExplorer.getSession() ;
      if(uiExplorer.isPreferenceNode(uiExplorer.getCurrentNode())) {
        String preferenceWS = uiExplorer.getCurrentNode().getSession().getWorkspace().getName() ;
        session = uiExplorer.getSessionByWorkspace(preferenceWS) ;
      }   
      if(uiExplorer.nodeIsLocked(uiExplorer.getCurrentNode().getPath(), session)) {
        Object[] arg = { uiExplorer.getCurrentNode().getPath() } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }
      TemplateService templateService = uiActionBar.getApplicationComponent(TemplateService.class) ;
      if(templateService.isManagedNodeType(nodeType.getName())) {
        UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class) ;
        uiPopupAction.activate(UIWatchDocumentForm .class, 600) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        return ;
      }
      uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.unsupported-watch", null,
          ApplicationMessage.WARNING)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return ;
    }
  }
  
  static public class TaggingDocumentActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      UIJCRExplorer uiExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class) ;
      TemplateService templateService = uiActionBar.getApplicationComponent(TemplateService.class) ;
      NodeType nodeType = uiExplorer.getCurrentNode().getPrimaryNodeType() ;
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
      Session session = uiExplorer.getSession() ;
      if(uiExplorer.isPreferenceNode(uiExplorer.getCurrentNode())) {
        String preferenceWS = uiExplorer.getCurrentNode().getSession().getWorkspace().getName() ;
        session = uiExplorer.getSessionByWorkspace(preferenceWS) ;
      }         
      if(uiExplorer.nodeIsLocked(uiExplorer.getCurrentNode().getPath(), session)) {
        Object[] arg = { uiExplorer.getCurrentNode().getPath() } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }
      if(templateService.isManagedNodeType(nodeType.getName())) {
        UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class) ;
        uiPopupAction.activate(UITaggingForm.class, 600) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        return ;
      } 
      uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.unsupported-tagging", null,
          ApplicationMessage.WARNING)) ;
    }
  }
  
  static public class MultiLanguageActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      UIJCRExplorer uiExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class) ;
      TemplateService templateService = uiActionBar.getApplicationComponent(TemplateService.class) ;
      NodeType nodeType = uiExplorer.getCurrentNode().getPrimaryNodeType() ;
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
      Session session = uiExplorer.getSession() ;
      if(uiExplorer.isPreferenceNode(uiExplorer.getCurrentNode())) {
        String preferenceWS = uiExplorer.getCurrentNode().getSession().getWorkspace().getName() ;
        session = uiExplorer.getSessionByWorkspace(preferenceWS) ;
      }   
      if(uiExplorer.nodeIsLocked(uiExplorer.getCurrentNode().getPath(), session)) {
        Object[] arg = { uiExplorer.getCurrentNode().getPath() } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }
      if(!templateService.isManagedNodeType(nodeType.getName())) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.unsupported-multilanguage", null, 
            ApplicationMessage.WARNING)) ;
        return ;
      }
      UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(UIMultiLanguageManager.class, null,720, 550) ;
      UIMultiLanguageManager uiMultiManager = 
        uiPopupAction.findFirstComponentOfType(UIMultiLanguageManager.class) ;
      UIAddLanguageContainer uiAddContainer = uiMultiManager.getChild(UIAddLanguageContainer.class) ;
      if(nodeType.getName().equals(Utils.NT_FILE)) {
        String mimeType = uiExplorer.getCurrentNode().getNode(Utils.JCR_CONTENT).getProperty(Utils.JCR_MIMETY).getString() ;
        if(mimeType.startsWith("text")) {
          uiAddContainer.setComponentDisplay(nodeType.getName()) ;
        } else {
          uiAddContainer.addChild(UIUploadForm.class, null, null) ;
        }
      } else {
        uiAddContainer.setComponentDisplay(nodeType.getName()) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  
  static public class ViewReferencesActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiJCRExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      UIPopupAction uiPopupAction = uiJCRExplorer.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(UIReferencesList .class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class ViewNodeTypeActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiJCRExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      UIPopupAction uiPopupAction = uiJCRExplorer.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(UINodeTypeInfo .class, 700) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class ViewPermissionsActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiJCRExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      UIPopupAction uiPopupAction = uiJCRExplorer.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(UIPermissionManager .class, 700) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class ViewPropertiesActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiJCRExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      UIPropertiesManager uiPropertiesManager = 
        uiJCRExplorer.createUIComponent(UIPropertiesManager.class, null, null) ;
      UIPopupAction uiPopupAction = uiJCRExplorer.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(uiPropertiesManager, 700, 0) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class ViewRelationsActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiJCRExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      UIWorkingArea uiWorkingArea = uiJCRExplorer.getChild(UIWorkingArea.class) ;
      UISideBar uiSideBar = uiWorkingArea.getChild(UISideBar.class) ;
      UIViewRelationList uiViewRelationList = uiSideBar.getChild(UIViewRelationList.class) ;
      if(uiJCRExplorer.getPreference().isShowSideBar()) {
        if(uiViewRelationList.isRendered()) {
          uiSideBar.setRenderedChild(UITreeExplorer.class) ;
        } else {
          uiSideBar.setRenderedChild(UIViewRelationList.class) ;
        }
      } else {
        uiJCRExplorer.getPreference().setShowSideBar(true) ;
        uiSideBar.setRenderedChild(UIViewRelationList.class) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
    }
  }

  static public class ShowJCRStructureActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiJCRExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      Preference pref = uiJCRExplorer.getPreference() ;
      if(uiJCRExplorer.getPreference().isJcrEnable()) pref.setJcrEnable(false) ;
      else pref.setJcrEnable(true) ;
    }
  }

  static public class ManageVersionsActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      UIJCRExplorer uiExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class) ;
      UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class) ;
      Node currentNode = uiExplorer.getCurrentNode() ;
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
      Session session = uiExplorer.getSession() ;
      if(uiExplorer.isPreferenceNode(uiExplorer.getCurrentNode())) {
        String preferenceWS = uiExplorer.getCurrentNode().getSession().getWorkspace().getName() ;
        session = uiExplorer.getSessionByWorkspace(preferenceWS) ;
      }   
      if(uiExplorer.nodeIsLocked(currentNode.getPath(), session)) {
        Object[] arg = { currentNode.getPath() } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }
      if(currentNode.canAddMixin(Utils.MIX_VERSIONABLE)) {
        uiPopupAction.activate(UIActivateVersion.class, 400) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        return ;
      }
      if (currentNode.isNodeType(Utils.MIX_VERSIONABLE)) {
        uiPopupAction.activate(UIVersionInfo.class, 700) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        return ;
      }
      uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.cannot-enable-version", null)) ;
    }
  }

  static public class ManageCategoriesActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      CmsConfigurationService cmsService = uiActionBar.getApplicationComponent(CmsConfigurationService.class) ;
      UIJCRExplorer uiExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class) ;
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
      Session session = uiExplorer.getSession() ;
      if(uiExplorer.isPreferenceNode(uiExplorer.getCurrentNode())) {
        String preferenceWS = uiExplorer.getCurrentNode().getSession().getWorkspace().getName() ;
        session = uiExplorer.getSessionByWorkspace(preferenceWS) ;
      }   
      if(uiExplorer.nodeIsLocked(uiExplorer.getCurrentNode().getPath(), session)) {
        Object[] arg = { uiExplorer.getCurrentNode().getPath() } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }
      uiExplorer.setIsHidePopup(true) ;
      CategoriesService categoriesService = uiActionBar.getApplicationComponent(CategoriesService.class) ;
      UICategoryManager uiManager = uiExplorer.createUIComponent(UICategoryManager.class, null, null) ;
      UICategoriesAddedList uiCateAddedList = uiManager.getChild(UICategoriesAddedList.class) ;
      uiCateAddedList.updateGrid(categoriesService.getCategories(uiExplorer.getCurrentNode())) ;
      UIJCRBrowser uiJCRBrowser = uiManager.getChild(UIJCRBrowser.class) ;
      uiJCRBrowser.setFilterType(null) ;
      uiJCRBrowser.setRootPath(cmsService.getJcrPath(EXO_TAXONOMIES_PATH)) ;
      uiJCRBrowser.setIsTab(true) ;
      uiJCRBrowser.setComponent(uiCateAddedList, null) ;
      UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(uiManager, 630, 0) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class ManageRelationsActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      UIJCRExplorer uiExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class) ;
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
      Session session = uiExplorer.getSession() ;
      if(uiExplorer.isPreferenceNode(uiExplorer.getCurrentNode())) {
        String preferenceWS = uiExplorer.getCurrentNode().getSession().getWorkspace().getName() ;
        session = uiExplorer.getSessionByWorkspace(preferenceWS) ;
      }   
      if(uiExplorer.nodeIsLocked(uiExplorer.getCurrentNode().getPath(), session)) {
        Object[] arg = { uiExplorer.getCurrentNode().getPath() } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }
      CmsConfigurationService cmsService = 
        uiActionBar.getApplicationComponent(CmsConfigurationService.class) ;
      UIRelationManager uiRelationManager = 
        uiExplorer.createUIComponent(UIRelationManager.class, null, null) ;
      RelationsService relateService = 
        uiActionBar.getApplicationComponent(RelationsService.class) ;
      UIRelationsAddedList uiRelateAddedList = 
        uiRelationManager.getChild(UIRelationsAddedList.class) ;
      uiRelateAddedList.updateGrid(relateService.getRelations(uiExplorer.getCurrentNode())) ;
      UIJCRBrowser uiJCRBrowser = uiRelationManager.getChild(UIJCRBrowser.class) ;
      uiJCRBrowser.setFilterType(new String[] {Utils.EXO_ARTICLE}) ;
      uiJCRBrowser.setRootPath(cmsService.getJcrPath(CMS_PATH)) ;
      uiJCRBrowser.setIsTab(true) ;
      uiJCRBrowser.setComponent(uiRelateAddedList, null) ;
      uiExplorer.setIsHidePopup(true) ;
      UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(uiRelationManager, 630, 0) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class ManageActionsActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      UIJCRExplorer uiExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class) ;
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
      Session session = uiExplorer.getSession() ;
      if(uiExplorer.isPreferenceNode(uiExplorer.getCurrentNode())) {
        String preferenceWS = uiExplorer.getCurrentNode().getSession().getWorkspace().getName() ;
        session = uiExplorer.getSessionByWorkspace(preferenceWS) ;
      }   
      if(uiExplorer.nodeIsLocked(uiExplorer.getCurrentNode().getPath(), session)) {
        Object[] arg = { uiExplorer.getCurrentNode().getPath() } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }
      UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(UIActionManager.class, null, 610, 550) ;
      uiExplorer.setIsHidePopup(true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class ExportNodeActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class) ;
      Session session = uiExplorer.getSession() ;
      if(uiExplorer.isPreferenceNode(uiExplorer.getCurrentNode())) {
        String preferenceWS = uiExplorer.getCurrentNode().getSession().getWorkspace().getName() ;
        session = uiExplorer.getSessionByWorkspace(preferenceWS) ;
      }   
      if(uiExplorer.nodeIsLocked(uiExplorer.getCurrentNode().getPath(), session)) {
        Object[] arg = { uiExplorer.getCurrentNode().getPath() } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }
      UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(UIExportNode.class, 610) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class ImportNodeActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class) ;
      Session session = uiExplorer.getSession() ;
      if(uiExplorer.isPreferenceNode(uiExplorer.getCurrentNode())) {
        String preferenceWS = uiExplorer.getCurrentNode().getSession().getWorkspace().getName() ;
        session = uiExplorer.getSessionByWorkspace(preferenceWS) ;
      }   
      if(uiExplorer.nodeIsLocked(uiExplorer.getCurrentNode().getPath(), session)) {
        Object[] arg = { uiExplorer.getCurrentNode().getPath() } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }
      UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(UIImportNode.class, 610) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class SimpleSearchActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiForm = event.getSource() ;
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
      String text = uiForm.getUIStringInput(FIELD_SIMPLE_SEARCH).getValue() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      Node currentNode = uiExplorer.getCurrentNode() ;
      QueryManager queryManager = uiExplorer.getSession().getWorkspace().getQueryManager() ;
      String queryText = StringUtils.replace(SQL_QUERY, "$0", currentNode.getPath()) ;      
      if ("/".equals(currentNode.getPath())) queryText = ROOT_SQL_QUERY ;
      String queryPath ;
      if ("/".equals(currentNode.getPath()))  queryPath = ROOT_PATH_SQL_QUERY;
      else if(currentNode.getParent().getPath().equals("/")) queryPath = StringUtils.replace(PATH_SQL_QUERY, "$0", "");
      else queryPath = StringUtils.replace(PATH_SQL_QUERY, "$0", currentNode.getParent().getPath());
      String statement = StringUtils.replace(queryText, "$1", text) ;
      String statementPath = StringUtils.replace(queryPath, "$1", text) ;
      
      Query query = queryManager.createQuery(statement, Query.SQL);                
      Query pathQuery = queryManager.createQuery(statementPath, Query.SQL);
      
      QueryResult queryResult = query.execute();
      QueryResult pathQueryResult = pathQuery.execute();
      
      UIDocumentWorkspace uiDocumentWorkspace = uiExplorer.getChild(UIWorkingArea.class).
                                                      getChild(UIDocumentWorkspace.class) ;
      UISearchResult uiSearchResult = uiDocumentWorkspace.getChild(UISearchResult.class) ;
      uiSearchResult.setIsQuickSearch(true) ;
      uiSearchResult.setQueryResults(queryResult) ;
      uiSearchResult.setQueryResults(pathQueryResult) ;
      if(uiSearchResult.resultMap_ == null || uiSearchResult.resultMap_.size() ==0) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.not-found", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      uiSearchResult.updateGrid() ;
      uiDocumentWorkspace.setRenderedChild(UISearchResult.class) ;
    }
  }
  
  static public class AdvanceSearchActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiJCRExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      UIPopupAction uiPopupAction = uiJCRExplorer.getChild(UIPopupAction.class) ;
      UISearchContainer uSearchContainer = event.getSource().createUIComponent(UISearchContainer.class, null, null) ;
      uiPopupAction.activate(uSearchContainer, 700, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  
  static public class VoteActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class) ;
      Session session = uiExplorer.getSession() ;
      if(uiExplorer.isPreferenceNode(uiExplorer.getCurrentNode())) {
        String preferenceWS = uiExplorer.getCurrentNode().getSession().getWorkspace().getName() ;
        session = uiExplorer.getSessionByWorkspace(preferenceWS) ;
      }   
      if(!uiExplorer.getCurrentNode().isNodeType("mix:votable")) {
        uiApp.addMessage(new ApplicationMessage("UIVoteForm.msg.not-support", null)) ;
        return ;
      }
      if(uiExplorer.nodeIsLocked(uiExplorer.getCurrentNode().getPath(), session)) {
        Object[] arg = { uiExplorer.getCurrentNode().getPath() } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }
      UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(UIVoteForm.class, 300) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  
  static public class ViewMetadatasActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      UIJCRExplorer uiJCRExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class) ;
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
      if(uiActionBar.getMetadataTemplates().size() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIViewMetadataContainer.msg.path-not-found", null)) ;
        return ;
      }
      UIPopupAction uiPopupAction = uiJCRExplorer.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(UIViewMetadataManager.class, 700) ;
      UIViewMetadataManager uiMetadataManager = 
        uiPopupAction.findFirstComponentOfType(UIViewMetadataManager.class) ;
      UIViewMetadataContainer uiMetadataContainer = uiMetadataManager.getChild(UIViewMetadataContainer.class) ;
      int i = 0 ;
      for(String template : uiActionBar.getMetadataTemplates()) {
        String[] nodeTypes = template.split("/") ;
        UIViewMetadataTemplate uiMetaView = 
          uiMetadataContainer.createUIComponent(UIViewMetadataTemplate.class, null, nodeTypes[4]) ;
        uiMetaView.setTemplateType(nodeTypes[4]) ;
        uiMetadataContainer.addChild(uiMetaView) ;
        if(i != 0) uiMetaView.setRendered(false) ;
        i++ ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  
  static public class CommentActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class) ;
      Session session = uiExplorer.getSession() ;
      if(uiExplorer.isPreferenceNode(uiExplorer.getCurrentNode())) {
        String preferenceWS = uiExplorer.getCurrentNode().getSession().getWorkspace().getName() ;
        session = uiExplorer.getSessionByWorkspace(preferenceWS) ;
      }   
      if(!uiExplorer.getCurrentNode().isNodeType("mix:commentable")) {
        uiApp.addMessage(new ApplicationMessage("UICommentForm.msg.not-support", null)) ;
        return ;
      }
      if(uiExplorer.nodeIsLocked(uiExplorer.getCurrentNode().getPath(), session)) {
        Object[] arg = { uiExplorer.getCurrentNode().getPath() } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }
      UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(UICommentForm.class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  
  static public class ChangeTabActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource()) ;
    }
  }
}