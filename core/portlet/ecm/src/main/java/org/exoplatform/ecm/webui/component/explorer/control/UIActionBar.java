/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ecm.webui.component.explorer.control;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;

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
import org.exoplatform.ecm.jcr.SearchValidator;
import org.exoplatform.ecm.jcr.model.Preference;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentWorkspace;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.ecm.webui.component.explorer.auditing.UIActivateAuditing;
import org.exoplatform.ecm.webui.component.explorer.auditing.UIAuditingInfo;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIAddLanguageContainer;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UICommentForm;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIDocumentForm;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIDocumentFormController;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIFolderForm;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIMultiLanguageManager;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UITaggingForm;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIVoteForm;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIWatchDocumentForm;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionContainer;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionForm;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionManager;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionTypeForm;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActivePublication;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UICategoriesAddedList;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UICategoryManager;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIExportNode;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIImportNode;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIPropertiesManager;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIPropertyForm;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIPublicationManager;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIRelationManager;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIRelationsAddedList;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UISimpleCategoriesAddedList;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UISimpleCategoryManager;
import org.exoplatform.ecm.webui.component.explorer.popup.info.UINodeTypeInfo;
import org.exoplatform.ecm.webui.component.explorer.popup.info.UIPermissionManager;
import org.exoplatform.ecm.webui.component.explorer.popup.info.UIReferencesList;
import org.exoplatform.ecm.webui.component.explorer.popup.info.UIViewMetadataContainer;
import org.exoplatform.ecm.webui.component.explorer.popup.info.UIViewMetadataManager;
import org.exoplatform.ecm.webui.component.explorer.popup.info.UIViewMetadataTemplate;
import org.exoplatform.ecm.webui.component.explorer.search.UIContentNameSearch;
import org.exoplatform.ecm.webui.component.explorer.search.UIECMSearch;
import org.exoplatform.ecm.webui.component.explorer.search.UISavedQuery;
import org.exoplatform.ecm.webui.component.explorer.search.UISearchResult;
import org.exoplatform.ecm.webui.component.explorer.search.UISimpleSearch;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UISideBar;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UITreeExplorer;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UIViewRelationList;
import org.exoplatform.ecm.webui.component.explorer.symlink.UISymLinkManager;
import org.exoplatform.ecm.webui.component.explorer.thumbnail.UIThumbnailForm;
import org.exoplatform.ecm.webui.component.explorer.upload.UIUploadManager;
import org.exoplatform.ecm.webui.component.explorer.versions.UIActivateVersion;
import org.exoplatform.ecm.webui.component.explorer.versions.UIVersionInfo;
import org.exoplatform.ecm.webui.tree.selectone.UIOneNodePathSelector;
import org.exoplatform.ecm.webui.tree.selectone.UIOneTaxonomySelector;
import org.exoplatform.ecm.webui.utils.PermissionUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.cms.impl.DMSRepositoryConfiguration;
import org.exoplatform.services.cms.metadata.MetadataService;
import org.exoplatform.services.cms.queries.QueryService;
import org.exoplatform.services.cms.relations.RelationsService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.services.ecm.publication.PublicationPresentationService;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.ecm.publication.plugins.webui.UIPublicationLogList;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

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
      @EventConfig(listeners = UIActionBar.ManageAuditingActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionBar.ManagePublicationsActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionBar.ManageSimpleCategoriesActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionBar.ManageCategoriesActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionBar.ManageRelationsActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionBar.ManageActionsActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionBar.ExportNodeActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionBar.ImportNodeActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionBar.SimpleSearchActionListener.class),
      @EventConfig(listeners = UIActionBar.AdvanceSearchActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionBar.SavedQueriesActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionBar.ViewMetadatasActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionBar.ChangeTabActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionBar.VoteActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionBar.CommentActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionBar.OverloadThumbnailActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionBar.ManageHiddenActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionBar.AddSymLinkActionListener.class, phase = Phase.DECODE)
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
  final static private String FIELD_SQL = "SQL" ;
  final static private String FIELD_XPATH = "xPath" ;

  final static private String ROOT_SQL_QUERY = "select * from nt:base where contains(*, '$1') order by exo:dateCreated DESC, jcr:primaryType DESC" ;
  final static private String SQL_QUERY = "select * from nt:base where jcr:path like '$0/%' and contains(*, '$1') order by jcr:path DESC, jcr:primaryType DESC";

  public UIActionBar() throws Exception{
    UIFormSelectBox selectTab  = new UIFormSelectBox(FIELD_SELECT_TAB, FIELD_SELECT_TAB, tabOptions) ;
    selectTab.setOnChange("ChangeTab") ;
    addUIFormInput(selectTab) ;
    addChild(new UIFormStringInput(FIELD_SIMPLE_SEARCH, FIELD_SIMPLE_SEARCH, null).addValidator(SearchValidator.class));

    List<SelectItemOption<String>> typeOptions = new ArrayList<SelectItemOption<String>>();
    typeOptions.add(new SelectItemOption<String>(FIELD_SQL, Query.SQL));
    typeOptions.add(new SelectItemOption<String>(FIELD_XPATH, Query.XPATH));
    addChild(new UIFormSelectBox(FIELD_SEARCH_TYPE, FIELD_SEARCH_TYPE, typeOptions));
    addChild(new UIFormStringInput(FIELD_ADVANCE_SEARCH, FIELD_ADVANCE_SEARCH, null));
  }

  public void setTabOptions(String viewName) throws Exception {
    tabOptions.clear();
    tabs_.clear();
    tabs_ = new ArrayList<String[]>();
    String repository = getAncestorOfType(UIJCRExplorer.class).getRepositoryName();
    view_ = getApplicationComponent(ManageViewService.class).getViewByName(viewName,repository, 
        SessionProviderFactory.createSystemProvider()); 
    NodeIterator tabs = view_.getNodes();
    int i = 0;
    while (tabs.hasNext()) {
      Node tab = tabs.nextNode();
      tabOptions.add(new SelectItemOption<String>(tab.getName(), String.valueOf(i++)));
      setListButton(tab.getName());
    }
    RequestContext context = RequestContext.getCurrentInstance();
    ResourceBundle res = context.getApplicationResourceBundle();
    String searchLabel = res.getString("UIJCRAdvancedSearch.action.Search"); 
    tabOptions.add(new SelectItemOption<String>(searchLabel, String.valueOf(i++)));
    getUIFormSelectBox(FIELD_SELECT_TAB).setOptions(tabOptions).setValue(tabOptions.get(0).getValue());
    String template = view_.getProperty("exo:template").getString();
    templateName_ = template.substring(template.lastIndexOf("/") + 1);
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    uiExplorer.setRenderTemplate(template);
  }

  public String getTemplateName() { return templateName_;  }

  private void setListButton(String tabName) throws PathNotFoundException, RepositoryException {
    Node tabNode = view_.getNode(tabName);
    if(tabNode.hasProperty("exo:buttons")) {
      String buttons = tabNode.getProperty("exo:buttons").getString();
      String[] buttonsInTab = StringUtils.split(buttons, ";");
      for(int j = 0; j < buttonsInTab.length; j ++ ) {
        String buttonName = buttonsInTab[j].trim();
        buttonName = buttonName.substring(0, 1).toUpperCase() + buttonName.substring(1);
        buttonsInTab[j] = buttonName;
      }
      tabs_.add(buttonsInTab);
    }
  }

  public List<String[]> getTabs() throws Exception { return tabs_; }

  public int getSelectedTab() {
    return Integer.parseInt(getUIFormSelectBox(FIELD_SELECT_TAB).getValue());
  }

  public List<Query> getSavedQueries() throws Exception {
    String userName = Util.getPortalRequestContext().getRemoteUser();
    String repository = getAncestorOfType(UIJCRExplorer.class).getRepositoryName();
    return getApplicationComponent(QueryService.class).getQueries(userName, repository, 
        SessionProviderFactory.createSystemProvider());
  }

  public Hashtable<String, String> getMetadataTemplates() throws Exception {
    MetadataService metadataService = getApplicationComponent(MetadataService.class) ;
    Node node = getAncestorOfType(UIJCRExplorer.class).getCurrentNode() ;
    String repository = getAncestorOfType(UIJCRExplorer.class).getRepositoryName() ;
    Hashtable<String, String> templates = new Hashtable<String, String>();
    List<String> metaDataList = metadataService.getMetadataList(repository);

    NodeType[] nodeTypes = node.getMixinNodeTypes();
    for(NodeType nt : nodeTypes) {
      if(metaDataList.contains(nt.getName())) {
        templates.put(nt.getName(), metadataService.getMetadataPath(nt.getName(), false, repository));
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
        if(metaDataList.contains(nt.getName())) {
          templates.put(nt.getName(), metadataService.getMetadataPath(nt.getName(), false, repository));
        }
      }
    }
    return templates;
  }

  private boolean isRootNode(UIJCRExplorer uiExplorer, Node node) throws Exception {
    return uiExplorer.getRootNode().equals(node);
  }

  static public class AddFolderActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
      Node currentNode = uiExplorer.getCurrentNode();
      if(!PermissionUtil.canRead(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.has-not-add-permission", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(!PermissionUtil.canAddNode(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.has-not-add-permission", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(uiExplorer.nodeIsLocked(currentNode)) {
        Object[] arg = { uiExplorer.getCurrentNode().getPath() };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } 
      if(!currentNode.isCheckedOut()) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
      UIPopupContainer.activate(UIFolderForm.class, 600);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }

  static public class AddDocumentActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
      Node currentNode = uiExplorer.getCurrentNode();
      if(!PermissionUtil.canRead(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.has-not-add-permission", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(!PermissionUtil.canAddNode(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.has-not-add-permission", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(uiExplorer.nodeIsLocked(currentNode)){
        Object[] arg = { currentNode.getPath() };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(!currentNode.isCheckedOut()) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }     
      UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
      UIDocumentFormController uiController = 
        event.getSource().createUIComponent(UIDocumentFormController.class, null, null);
      uiController.setCurrentNode(uiExplorer.getCurrentNode());
      uiController.setRepository(uiExplorer.getRepositoryName());
      if(uiController.getListFileType().size() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.empty-file-type", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      uiController.init();
      UIPopupContainer.activate(uiController, 800, 600);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }

  static public class EditDocumentActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uicomp = event.getSource();
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class);
      Node selectedNode = uiExplorer.getCurrentNode();        
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class);
      if(!PermissionUtil.canSetProperty(selectedNode)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.has-not-edit-permission", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(uiExplorer.nodeIsLocked(selectedNode) ){
        Object[] arg = { selectedNode.getPath() };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(!uiExplorer.getCurrentNode().isCheckedOut()) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(selectedNode.isNodeType(Utils.EXO_ACTION)) {
        UIActionContainer uiContainer = uiExplorer.createUIComponent(UIActionContainer.class, null, null);
        uiExplorer.setIsHidePopup(true);
        UIActionForm uiActionForm =  uiContainer.getChild(UIActionForm.class);
        uiContainer.getChild(UIActionTypeForm.class).setRendered(false);
        uiActionForm.createNewAction(selectedNode, selectedNode.getPrimaryNodeType().getName(), false);
        uiActionForm.setIsUpdateSelect(false);
        uiActionForm.setNodePath(selectedNode.getPath());
        uiActionForm.setWorkspace(selectedNode.getSession().getWorkspace().getName());
        uiActionForm.setStoredPath(selectedNode.getPath());
        UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
        UIPopupContainer.activate(uiContainer, 700, 550);
        event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
      } else {
        TemplateService tservice = uicomp.getApplicationComponent(TemplateService.class);
        String repository = uicomp.getAncestorOfType(UIJCRExplorer.class).getRepositoryName();
        List<String> documentNodeType = tservice.getDocumentTemplates(repository);
        String nodeType = null;
        if(selectedNode.hasProperty("exo:presentationType")) {
          nodeType = selectedNode.getProperty("exo:presentationType").getString();
        }else {
          nodeType = selectedNode.getPrimaryNodeType().getName();
        }        
        if(documentNodeType.contains(nodeType)){
          UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
          UIDocumentFormController uiController = 
            event.getSource().createUIComponent(UIDocumentFormController.class, null, "EditFormController");
          UIDocumentForm uiDocumentForm = uiController.getChild(UIDocumentForm.class);
          uiDocumentForm.setRepositoryName(uiExplorer.getRepositoryName());
          uiDocumentForm.setContentType(nodeType);
          if(uiDocumentForm.getTemplate() == null) {
            uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.template-null", null));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
          uiDocumentForm.setNodePath(selectedNode.getPath());
          uiDocumentForm.addNew(false);
          uiDocumentForm.setWorkspace(selectedNode.getSession().getWorkspace().getName());
          uiDocumentForm.setStoredPath(selectedNode.getPath());
          uiController.setRenderedChild(UIDocumentForm.class);
          UIPopupContainer.activate(uiController, 800, 600);          
          event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
        } else {
          uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.not-support", null));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
      }
    }
  }

  static public class UploadActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
      Node currentNode = uiExplorer.getCurrentNode();
      if(!PermissionUtil.canRead(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.has-not-add-permission", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(!PermissionUtil.canAddNode(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.has-not-add-permission", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if( uiExplorer.nodeIsLocked(currentNode)) {
        Object[] arg = { currentNode.getPath() };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(!currentNode.isCheckedOut()) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }      
      UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
      UIUploadManager uiUploadManager = event.getSource().createUIComponent(UIUploadManager.class, null, null);
      UIPopupContainer.activate(uiUploadManager, 600, 500);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }

  static public class SearchActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiJCRExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      UIPopupContainer UIPopupContainer = uiJCRExplorer.getChild(UIPopupContainer.class);
      UIPopupContainer.activate(UIECMSearch .class, 700);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }

  static public class WatchDocumentActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource();
      UIJCRExplorer uiExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class);      
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class);      
      Node currentNode = uiExplorer.getCurrentNode();
      if(uiExplorer.nodeIsLocked(currentNode)) {
        Object[] arg = { currentNode.getPath() };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(!PermissionUtil.canSetProperty(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.access-denied", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }

      NodeType nodeType = currentNode.getPrimaryNodeType();
      TemplateService templateService = uiActionBar.getApplicationComponent(TemplateService.class);
      String repository = uiActionBar.getAncestorOfType(UIJCRExplorer.class).getRepositoryName();
      if(templateService.getDocumentTemplates(repository).contains(nodeType.getName())) {
        if(!currentNode.isCheckedOut()) {
          uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.watch-checkedin", null));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
        UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
        UIPopupContainer.activate(UIWatchDocumentForm.class, 600);
        event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
        return;
      }
      uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.unsupported-watch", null,
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    }
  }

  static public class TaggingDocumentActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource();
      UIJCRExplorer uiExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class);
      TemplateService templateService = uiActionBar.getApplicationComponent(TemplateService.class);
      Node currentNode = uiExplorer.getCurrentNode();
      NodeType nodeType = uiExplorer.getCurrentNode().getPrimaryNodeType();
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class);
      if(!PermissionUtil.canSetProperty(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.access-denied", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(uiExplorer.nodeIsLocked(currentNode)) {
        Object[] arg = { currentNode.getPath() };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }      
      String repository = uiActionBar.getAncestorOfType(UIJCRExplorer.class).getRepositoryName();
      if(templateService.getDocumentTemplates(repository).contains(nodeType.getName())) {
        if(!currentNode.isCheckedOut()) {
          uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.tagnode-checkedin", null));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
        UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
        UIPopupContainer.activate(UITaggingForm.class, 600);
        event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
        return;
      }
      uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.unsupported-tagging", null,
          ApplicationMessage.WARNING));
    }
  }

  static public class MultiLanguageActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource();
      UIJCRExplorer uiExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class);
      TemplateService templateService = uiActionBar.getApplicationComponent(TemplateService.class);
      Node currentNode = uiExplorer.getCurrentNode();
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class);
      if(!PermissionUtil.canSetProperty(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.access-denied", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(uiExplorer.nodeIsLocked(currentNode)) {
        Object[] arg = { currentNode.getPath() };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(!currentNode.isCheckedOut()) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.multilang-checkedin", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      String repository = uiActionBar.getAncestorOfType(UIJCRExplorer.class).getRepositoryName();
      NodeType nodeType = currentNode.getPrimaryNodeType();
      if(!templateService.getDocumentTemplates(repository).contains(nodeType.getName())) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.unsupported-multilanguage", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }      
      UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
      UIPopupContainer.activate(UIMultiLanguageManager.class, null,780, 550);
      UIMultiLanguageManager uiMultiManager = 
        UIPopupContainer.findFirstComponentOfType(UIMultiLanguageManager.class);
      UIAddLanguageContainer uiAddContainer = uiMultiManager.getChild(UIAddLanguageContainer.class);
      if(nodeType.getName().equals(Utils.NT_FILE)) {
        String mimeType = uiExplorer.getCurrentNode().getNode(Utils.JCR_CONTENT).
        getProperty(Utils.JCR_MIMETYPE).getString();
        if(mimeType.startsWith("text")) uiAddContainer.setComponentDisplay(nodeType.getName());
        else uiAddContainer.addChild(UIUploadManager.class, null, null);
      } else {
        uiAddContainer.setComponentDisplay(nodeType.getName());
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }

  static public class ViewReferencesActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar= event.getSource();
      UIJCRExplorer uiJCRExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class);
      Node currentNode = uiJCRExplorer.getCurrentNode();
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class);
      if(!uiJCRExplorer.isReferenceableNode(currentNode)) {
        Object[] args = {currentNode.getPath()};
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.is-not-referenable", args,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(uiActionBar.isRootNode(uiJCRExplorer, currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.cannot-action-in-rootnode", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      UIPopupContainer UIPopupContainer = uiJCRExplorer.getChild(UIPopupContainer.class);
      UIPopupContainer.activate(UIReferencesList.class, 600);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }

  static public class ViewNodeTypeActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiJCRExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      UIPopupContainer UIPopupContainer = uiJCRExplorer.getChild(UIPopupContainer.class);
      UIPopupContainer.activate(UINodeTypeInfo.class, 700);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }

  static public class ViewPermissionsActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiJCRExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      UIPopupContainer UIPopupContainer = uiJCRExplorer.getChild(UIPopupContainer.class);
      UIPermissionManager uiPerMan = UIPopupContainer.activate(UIPermissionManager.class, 700);
      uiPerMan.checkPermissonInfo(uiJCRExplorer.getCurrentNode());
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }

  static public class ViewPropertiesActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {      
      UIActionBar uiActionBar = event.getSource();
      UIJCRExplorer uiJCRExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class);
      Node node = uiJCRExplorer.getCurrentNode();
      UIPropertiesManager uiPropertiesManager = 
        uiJCRExplorer.createUIComponent(UIPropertiesManager.class, null, null);
      UIPropertyForm uiForm = uiPropertiesManager.getChild(UIPropertyForm.class);
      uiForm.setRepositoryName(uiJCRExplorer.getRepositoryName());
      uiForm.getUIFormSelectBox(UIPropertyForm.FIELD_NAMESPACE).setOptions(uiForm.getNamespaces());
      UIPopupContainer UIPopupContainer = uiJCRExplorer.getChild(UIPopupContainer.class);
      UIPopupContainer.activate(uiPropertiesManager, 700, 0);
      if(uiJCRExplorer.nodeIsLocked(node)){
        uiPropertiesManager.setLockForm(true);
      } else {
        uiPropertiesManager.setLockForm(!PermissionUtil.canSetProperty(node));
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }

  static public class ViewRelationsActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource();
      UIJCRExplorer uiJCRExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class);
      Node currentNode = uiJCRExplorer.getCurrentNode();
      if(uiActionBar.isRootNode(uiJCRExplorer, currentNode)) {
        UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.cannot-action-in-rootnode", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      UIWorkingArea uiWorkingArea = uiJCRExplorer.getChild(UIWorkingArea.class);
      UISideBar uiSideBar = uiWorkingArea.getChild(UISideBar.class);
      UIViewRelationList uiViewRelationList = uiSideBar.getChild(UIViewRelationList.class);
      if(uiJCRExplorer.getPreference().isShowSideBar()) {
        if(uiViewRelationList.isRendered())  {
          UITreeExplorer treeExplorer = uiSideBar.getChild(UITreeExplorer.class);
          treeExplorer.buildTree();
          uiSideBar.setRenderedChild(UITreeExplorer.class);
        }
        else uiSideBar.setRenderedChild(UIViewRelationList.class);
      } else {
        uiJCRExplorer.getPreference().setShowSideBar(true);
        uiSideBar.setRenderedChild(UIViewRelationList.class);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea);
    }
  }

  static public class ShowJCRStructureActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiJCRExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      Preference pref = uiJCRExplorer.getPreference();
      if(uiJCRExplorer.getPreference().isJcrEnable()) pref.setJcrEnable(false);
      else pref.setJcrEnable(true);
      uiJCRExplorer.refreshExplorer();
    }
  }

  static public class ManageVersionsActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource();
      UIJCRExplorer uiExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class);
      UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
      Node currentNode = uiExplorer.getCurrentNode();
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class);
      uiExplorer.setIsHidePopup(false);
      if(uiActionBar.isRootNode(uiExplorer, currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.cannot-enable-version-rootnode", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(!PermissionUtil.canSetProperty(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.access-denied", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if (uiExplorer.nodeIsLocked(currentNode)) {
        Object[] arg = { currentNode.getPath() };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }      
      if (currentNode.canAddMixin(Utils.MIX_VERSIONABLE)) {
        UIPopupContainer.activate(UIActivateVersion.class, 400);
        event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
        return;
      }
      if (currentNode.isNodeType(Utils.MIX_VERSIONABLE)) {
        UIPopupContainer.activate(UIVersionInfo.class, null, 700, 500);
        event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
        return;
      }
      uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.cannot-enable-version", null));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
    }
  }

  static public class ManagePublicationsActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource();
      UIJCRExplorer uiExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class);
      UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
      Node currentNode = uiExplorer.getCurrentNode();
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class);
      uiExplorer.setIsHidePopup(false);
      PublicationService publicationService = uiActionBar.getApplicationComponent(PublicationService.class);
      PublicationPresentationService publicationPresentationService = uiActionBar.getApplicationComponent(PublicationPresentationService.class);
      if(publicationService.getPublicationPlugins().size() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.manage-publication.no-publication-lifecycle",null,ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      String currentRepository = uiExplorer.getRepositoryName();
      TemplateService templateService = uiActionBar.getApplicationComponent(TemplateService.class);
      List<String> documentTypes = templateService.getDocumentTemplates(currentRepository);
      if (!documentTypes.contains(currentNode.getPrimaryNodeType().getName())) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.manage-publication.not-supported-nodetype", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }      
      if (uiActionBar.isRootNode(uiExplorer, currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.cannot-enable-publication-rootnode",
            null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if (!PermissionUtil.canSetProperty(currentNode) || !PermissionUtil.canRemoveNode(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.access-denied", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if (uiExplorer.nodeIsLocked(currentNode)) {
        Object[] arg = { currentNode.getPath() };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } 
      if (!publicationService.isNodeEnrolledInLifecycle(currentNode)) {                  
        UIActivePublication activePublication = uiActionBar.createUIComponent(UIActivePublication.class,null,null);
        if(publicationService.getPublicationPlugins().size() == 1) {
          activePublication.setRendered(false);
          uiExplorer.addChild(activePublication);
          String lifecycleName = publicationService.getPublicationPlugins().keySet().iterator().next();
          activePublication.enrolNodeInLifecycle(currentNode,lifecycleName,event.getRequestContext());                    
          return;
        }
        activePublication.setRendered(true);
        activePublication.updateLifecyclesGrid();
        UIPopupContainer.activate(activePublication, 600, 300);
        event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
        return;         
      }
      UIContainer cont = uiActionBar.createUIComponent(UIContainer.class, null, null);
      UIForm uiForm = publicationPresentationService.getStateUI(currentNode, cont);
      if(uiForm instanceof UIPopupComponent) {
        //This is special case for wcm want to more than 2 tabs in PublicationManager
        //The uiForm in this case should be a UITabPane or UIFormTabPane and need be a UIPopupComponent        
        UIPopupContainer.activate(uiForm, 700, 500);
      }else {
        UIPublicationManager uiPublicationManager = 
          uiExplorer.createUIComponent(UIPublicationManager.class, null, null);
        uiPublicationManager.addChild(uiForm);
        uiPublicationManager.addChild(UIPublicationLogList.class, null, null).setRendered(false);
        UIPublicationLogList uiPublicationLogList = 
          uiPublicationManager.getChild(UIPublicationLogList.class);      
        UIPopupContainer.activate(uiPublicationManager, 700, 500);
        uiPublicationLogList.setNode(uiExplorer.getCurrentNode());
        uiPublicationLogList.updateGrid(); 
      }            
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }

  static public class ManageAuditingActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource();
      UIJCRExplorer uiExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class);
      UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
      Node currentNode = uiExplorer.getCurrentNode();

      if (!currentNode.isNodeType(Utils.EXO_AUDITABLE)) {
        UIPopupContainer.activate(UIActivateAuditing.class, 400);
        event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
        return;
      }
      UIAuditingInfo uiAuditingInfo = uiExplorer.createUIComponent(UIAuditingInfo.class, null, null);
      UIPopupContainer.activate(uiAuditingInfo, 700, 500);
      uiAuditingInfo.updateGrid();
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
      return;
    }
  }

  static public class ManageSimpleCategoriesActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource();
      UIJCRExplorer uiExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class);
      String repository = uiExplorer.getRepositoryName();
      ManageableRepository manaRepository = 
        uiActionBar.getApplicationComponent(RepositoryService.class).getRepository(repository);
      String workspaceName = manaRepository.getConfiguration().getSystemWorkspaceName();
      NodeHierarchyCreator nodeHierarchyCreator = uiActionBar.getApplicationComponent(NodeHierarchyCreator.class);
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class);
      Node currentNode = uiExplorer.getCurrentNode();
      if(uiActionBar.isRootNode(uiExplorer, currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.cannot-action-in-rootnode", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }               
      if(!currentNode.isCheckedOut()) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(!PermissionUtil.canSetProperty(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.access-denied", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(uiExplorer.nodeIsLocked(currentNode)) {
        Object[] arg = { currentNode.getPath() };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      TemplateService templateService = uiActionBar.getApplicationComponent(TemplateService.class);
      NodeType nodeType = uiExplorer.getCurrentNode().getPrimaryNodeType();
      if(!templateService.getDocumentTemplates(repository).contains(nodeType.getName())) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.not-supported", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;        
      }
      uiExplorer.setIsHidePopup(true);
      UISimpleCategoryManager uiSimpleCategoryManager = uiExplorer.createUIComponent(UISimpleCategoryManager.class, null, null);
      UIOneNodePathSelector uiNodePathSelector = uiSimpleCategoryManager.getChild(UIOneNodePathSelector.class);
      uiNodePathSelector.setIsDisable(workspaceName, true);
      uiNodePathSelector.setExceptedNodeTypesInPathPanel(new String[] {Utils.EXO_SYMLINK});
      String rootTreePath = nodeHierarchyCreator.getJcrPath(BasePath.EXO_TAXONOMIES_PATH);      
      uiNodePathSelector.setRootNodeLocation(repository, workspaceName, rootTreePath);
      uiNodePathSelector.init(uiExplorer.getSessionProvider());
      UISimpleCategoriesAddedList uiSimpleCategoriesAddedList = uiSimpleCategoryManager.getChild(UISimpleCategoriesAddedList.class);
      uiNodePathSelector.setSourceComponent(uiSimpleCategoriesAddedList, null);
      UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
      UIPopupContainer.activate(uiSimpleCategoryManager, 630, 500);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }
  
  static public class ManageCategoriesActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource();
      UIJCRExplorer uiExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class);
      String repository = uiExplorer.getRepositoryName();
      DMSConfiguration dmsConfiguration = uiExplorer.getApplicationComponent(DMSConfiguration.class);
      DMSRepositoryConfiguration dmsRepoConfig = dmsConfiguration.getConfig(repository);
      String workspaceName = dmsRepoConfig.getSystemWorkspace();
      NodeHierarchyCreator nodeHierarchyCreator = uiActionBar.getApplicationComponent(NodeHierarchyCreator.class);
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class);
      Node currentNode = uiExplorer.getCurrentNode();
      if(uiActionBar.isRootNode(uiExplorer, currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.cannot-action-in-rootnode", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }               
      if(!currentNode.isCheckedOut()) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(!PermissionUtil.canSetProperty(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.access-denied", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(uiExplorer.nodeIsLocked(currentNode)) {
        Object[] arg = { currentNode.getPath() };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      TemplateService templateService = uiActionBar.getApplicationComponent(TemplateService.class);
      NodeType nodeType = uiExplorer.getCurrentNode().getPrimaryNodeType();
      if(!templateService.getDocumentTemplates(repository).contains(nodeType.getName())) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.not-supported", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;        
      }
      uiExplorer.setIsHidePopup(true);
      UICategoryManager uiManager = uiExplorer.createUIComponent(UICategoryManager.class, null, null);
      UIOneTaxonomySelector uiOneTaxonomySelector = uiManager.getChild(UIOneTaxonomySelector.class);
      uiOneTaxonomySelector.setIsDisable(workspaceName, false);
      uiOneTaxonomySelector.setExceptedNodeTypesInPathPanel(new String[] {Utils.EXO_SYMLINK});
      String rootTreePath = nodeHierarchyCreator.getJcrPath(BasePath.TAXONOMIES_TREE_STORAGE_PATH);      
      Session session = uiExplorer.getSessionByWorkspace(dmsRepoConfig.getSystemWorkspace());
      Node rootTree = (Node) session.getItem(rootTreePath);
      NodeIterator childrenIterator = rootTree.getNodes();
      while (childrenIterator.hasNext()) {
        Node childNode = childrenIterator.nextNode();
        rootTreePath = childNode.getPath();
        uiOneTaxonomySelector.setRootTaxonomyName(rootTreePath.substring(rootTreePath.lastIndexOf("/") + 1));
        break;
      }
      uiOneTaxonomySelector.setRootNodeLocation(repository, workspaceName, rootTreePath);
      uiOneTaxonomySelector.init(uiExplorer.getSessionProvider());
      UICategoriesAddedList uiCateAddedList = uiManager.getChild(UICategoriesAddedList.class);
      uiOneTaxonomySelector.setSourceComponent(uiCateAddedList, null);
      UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
      UIPopupContainer.activate(uiManager, 630, 500);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }

  static public class ManageRelationsActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource();
      UIJCRExplorer uiExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class);
      Node currentNode = uiExplorer.getCurrentNode();
      if(uiActionBar.isRootNode(uiExplorer, currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.cannot-action-in-rootnode", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }      
      if(!currentNode.isCheckedOut()) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(!PermissionUtil.canSetProperty(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.access-denied", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }               
      if(uiExplorer.nodeIsLocked(currentNode)) {
        Object[] arg = { currentNode.getPath() };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }            
      uiExplorer.setIsHidePopup(true);
      RepositoryService repoService = uiActionBar.getApplicationComponent(RepositoryService.class);
      UIRelationManager uiRelationManager = 
        uiExplorer.createUIComponent(UIRelationManager.class, null, null);
      RelationsService relateService = 
        uiActionBar.getApplicationComponent(RelationsService.class);
      UIRelationsAddedList uiRelateAddedList = 
        uiRelationManager.getChild(UIRelationsAddedList.class);
      List<Node> relations = 
        relateService.getRelations(uiExplorer.getCurrentNode(), uiExplorer.getRepositoryName(), 
            SessionProviderFactory.createSessionProvider());
      uiRelateAddedList.updateGrid(relations, 1);
      String repository = uiActionBar.getAncestorOfType(UIJCRExplorer.class).getRepositoryName();
      String defaultWsName = 
        repoService.getRepository(repository).getConfiguration().getDefaultWorkspaceName();
      UIOneNodePathSelector uiNodePathSelector = uiRelationManager.getChild(UIOneNodePathSelector.class);
      uiNodePathSelector.setIsDisable(defaultWsName, false);
      uiNodePathSelector.setRootNodeLocation(repository, defaultWsName, "/");
      TemplateService tservice = uiActionBar.getApplicationComponent(TemplateService.class);
      List<String> documentNodeType = tservice.getDocumentTemplates(repository);
      String [] arrAcceptedNodeTypes = new String[documentNodeType.size()];
      documentNodeType.toArray(arrAcceptedNodeTypes) ;
      uiNodePathSelector.setAcceptedNodeTypesInPathPanel(arrAcceptedNodeTypes);
      uiNodePathSelector.setIsShowSystem(false);
      uiNodePathSelector.setAcceptedNodeTypesInTree(new String[] {Utils.NT_UNSTRUCTURED, Utils.NT_FOLDER});
      uiNodePathSelector.init(uiExplorer.getSessionProvider());
      uiNodePathSelector.setSourceComponent(uiRelateAddedList, null);
      UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
      UIPopupContainer.activate(uiRelationManager, 630, 500);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }

  static public class ManageActionsActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource();
      UIJCRExplorer uiExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class);
      Node currentNode = uiExplorer.getCurrentNode();
      if(!PermissionUtil.canSetProperty(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.access-denied", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(uiExplorer.nodeIsLocked(currentNode)) {
        Object[] arg = { currentNode.getPath() };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(!currentNode.isCheckedOut()) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }      
      UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
      UIPopupContainer.activate(UIActionManager.class, null, 610, 550);
      uiExplorer.setIsHidePopup(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);      
    }
  }

  static public class ExportNodeActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);                   
      UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
      UIPopupContainer.activate(UIExportNode.class, 610);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }

  static public class ImportNodeActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
      Node currentNode = uiExplorer.getCurrentNode();
      if(!PermissionUtil.canRead(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.access-denied", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(uiExplorer.nodeIsLocked(currentNode)) {
        Object[] arg = { currentNode.getPath() };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(!currentNode.isCheckedOut()) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }      
      UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
      UIPopupContainer.activate(UIImportNode.class, 610);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }

  static public class SimpleSearchActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiForm = event.getSource();
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class);
      String text = uiForm.getUIStringInput(FIELD_SIMPLE_SEARCH).getValue();
      Node currentNode = uiExplorer.getCurrentNode();
      String queryStatement = null;
      if("/".equals(currentNode.getPath())) {
        queryStatement = ROOT_SQL_QUERY;        
      }else {
        queryStatement = StringUtils.replace(SQL_QUERY,"$0",currentNode.getPath());
      }
      queryStatement = StringUtils.replace(queryStatement,"$1", text.replaceAll("'", "''"));            
      uiExplorer.removeChildById("ViewSearch");
      UIDocumentWorkspace uiDocumentWorkspace = uiExplorer.getChild(UIWorkingArea.class).
      getChild(UIDocumentWorkspace.class);
      UISearchResult uiSearchResult = uiDocumentWorkspace.getChildById(UIDocumentWorkspace.SIMPLE_SEARCH_RESULT);           
      QueryManager queryManager = currentNode.getSession().getWorkspace().getQueryManager();
      long startTime = System.currentTimeMillis();
      Query query = queryManager.createQuery(queryStatement, Query.SQL);        
      QueryResult queryResult = query.execute();                  
      uiSearchResult.setIsQuickSearch(true);
      uiSearchResult.clearAll();
      uiSearchResult.setQueryResults(queryResult);            
      uiSearchResult.updateGrid(true);
      long time = System.currentTimeMillis() - startTime;
      uiSearchResult.setSearchTime(time);
      uiDocumentWorkspace.setRenderedChild(UISearchResult.class);
    }
  }

  static public class AdvanceSearchActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiJCRExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      UIPopupContainer UIPopupContainer = uiJCRExplorer.getChild(UIPopupContainer.class);
      UIECMSearch uiECMSearch = event.getSource().createUIComponent(UIECMSearch.class, null, null);
      UIContentNameSearch contentNameSearch = uiECMSearch.findFirstComponentOfType(UIContentNameSearch.class);
      String currentNodePath = uiJCRExplorer.getCurrentNode().getPath();
      contentNameSearch.setLocation(currentNodePath);
      UISimpleSearch uiSimpleSearch = uiECMSearch.findFirstComponentOfType(UISimpleSearch.class);
      uiSimpleSearch.getUIFormInputInfo(UISimpleSearch.NODE_PATH).setValue(currentNodePath);
      UIPopupContainer.activate(uiECMSearch, 700, 500);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }

  static public class SavedQueriesActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiJCRExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      UIPopupContainer UIPopupContainer = uiJCRExplorer.getChild(UIPopupContainer.class);
      UISavedQuery uiSavedQuery = event.getSource().createUIComponent(UISavedQuery.class, null, null);
      uiSavedQuery.setIsQuickSearch(true);
      uiSavedQuery.setRepositoryName(uiJCRExplorer.getRepositoryName());
      uiSavedQuery.updateGrid(1);
      UIPopupContainer.activate(uiSavedQuery, 700, 400);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }

  static public class VoteActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);   
      Node currentNode = uiExplorer.getCurrentNode();
      if(!PermissionUtil.canSetProperty(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.access-denied", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }      
      if(!currentNode.isNodeType("mix:votable")) {
        uiApp.addMessage(new ApplicationMessage("UIVoteForm.msg.not-support", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } 
      if(!currentNode.isCheckedOut()) {
        Object[] arg = { uiExplorer.getCurrentNode().getPath() };
        uiApp.addMessage(new ApplicationMessage("UIVoteForm.msg.not-checkedout", arg));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(uiExplorer.nodeIsLocked(currentNode)) {
        Object[] arg = { currentNode.getPath() };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }      
      UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
      UIPopupContainer.activate(UIVoteForm.class, 300);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }

  static public class ViewMetadatasActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource();
      UIJCRExplorer uiJCRExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class);
      Hashtable<String, String> metaDataTemp = uiActionBar.getMetadataTemplates();
      if (metaDataTemp.size() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIViewMetadataContainer.msg.path-not-found", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      UIPopupContainer UIPopupContainer = uiJCRExplorer.getChild(UIPopupContainer.class);
      UIPopupContainer.activate(UIViewMetadataManager.class, 700);
      UIViewMetadataManager uiMetadataManager = 
        UIPopupContainer.findFirstComponentOfType(UIViewMetadataManager.class);
      UIViewMetadataContainer uiMetadataContainer = uiMetadataManager.getChild(UIViewMetadataContainer.class);
      int i = 0;
      Enumeration<String> enu = metaDataTemp.keys();
      while (enu.hasMoreElements()) {
        String key = (String) enu.nextElement();
        String template = metaDataTemp.get(key);
        if(template != null && template.length() > 0) {
          UIViewMetadataTemplate uiMetaView = 
            uiMetadataContainer.createUIComponent(UIViewMetadataTemplate.class, null, key) ;
          uiMetaView.setTemplateType(key) ;
          uiMetadataContainer.addChild(uiMetaView) ;
          if(i != 0) uiMetaView.setRendered(false) ;
          i++ ;
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }

  static public class CommentActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
      Node currentNode = uiExplorer.getCurrentNode();
      if(!PermissionUtil.canRead(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.access-add-denied", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(!currentNode.isNodeType("mix:commentable")) {
        uiApp.addMessage(new ApplicationMessage("UICommentForm.msg.not-support", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } 
      Object[] arg = { currentNode.getPath() };
      if(!currentNode.isCheckedOut()) {        
        uiApp.addMessage(new ApplicationMessage("UICommentForm.msg.not-checkedout", arg));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(uiExplorer.nodeIsLocked(currentNode)) {        
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }      
      UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
      UIPopupContainer.activate(UICommentForm.class, 750);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }

  static public class OverloadThumbnailActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {      
      UIActionBar uiActionBar = event.getSource();
      UIJCRExplorer uiExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class);
      Node selectedNode = uiExplorer.getRealCurrentNode();
      if(uiActionBar.isRootNode(uiExplorer, selectedNode)) {
        UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.cannot-action-in-rootnode", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      UIThumbnailForm uiThumbnailForm = 
        uiExplorer.createUIComponent(UIThumbnailForm.class, null, null);
      UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
      UIPopupContainer.activate(uiThumbnailForm, 500, 0);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }

  static public class ManageHiddenActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {      
      UIActionBar uiActionBar = event.getSource();
      UIJCRExplorer uiExplorer = uiActionBar.getAncestorOfType(UIJCRExplorer.class);
      Node selectedNode = uiExplorer.getRealCurrentNode();
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class);
      if(uiActionBar.isRootNode(uiExplorer, selectedNode)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.cannot-action-in-rootnode", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(selectedNode.isNodeType("exo:hiddenable")) {
        selectedNode.removeMixin("exo:hiddenable");
        selectedNode.save();
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.removed-hidden-mixin", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiExplorer.updateAjax(event);
        return;
      } else if(selectedNode.canAddMixin("exo:hiddenable")){
        selectedNode.addMixin("exo:hiddenable");
        selectedNode.save();
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.added-hidden-mixin", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiExplorer.updateAjax(event);
        return;
      }
    }
  }

  static public class ChangeTabActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource());
    }
  }
  
  static public class AddSymLinkActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
      Node currentNode = uiExplorer.getCurrentNode();
      if(!PermissionUtil.canRead(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.has-not-add-permission", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(!PermissionUtil.canAddNode(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.has-not-add-permission", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if( uiExplorer.nodeIsLocked(currentNode)) {
        Object[] arg = { currentNode.getPath() };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(!currentNode.isCheckedOut()) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if (currentNode.isNodeType(Utils.EXO_SYMLINK)) {
        Object[] arg = {currentNode.getPath()};
        uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.selected-is-link", arg, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      
      UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
      UISymLinkManager uiSymLinkManager = event.getSource().createUIComponent(UISymLinkManager.class, null, null);
      UIPopupContainer.activate(uiSymLinkManager, 600, 300);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }
}