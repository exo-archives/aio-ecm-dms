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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.exoplatform.ecm.jcr.SearchValidator;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentWorkspace;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.ecm.webui.component.explorer.search.UIContentNameSearch;
import org.exoplatform.ecm.webui.component.explorer.search.UIECMSearch;
import org.exoplatform.ecm.webui.component.explorer.search.UISavedQuery;
import org.exoplatform.ecm.webui.component.explorer.search.UISearchResult;
import org.exoplatform.ecm.webui.component.explorer.search.UISimpleSearch;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.metadata.MetadataService;
import org.exoplatform.services.cms.queries.QueryService;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.ext.UIExtensionManager;
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
      @EventConfig(listeners = UIActionBar.SearchActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionBar.SimpleSearchActionListener.class),
      @EventConfig(listeners = UIActionBar.AdvanceSearchActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionBar.SavedQueriesActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIActionBar.ChangeTabActionListener.class, phase = Phase.DECODE)
    }
)

public class UIActionBar extends UIForm {

  /**
   * Logger.
   */
  private static final Log LOG  = ExoLogger.getLogger(UIActionBar.class);
  
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

  public UIComponent getUIAction(String action)  {
    try {
      UIExtensionManager manager = getApplicationComponent(UIExtensionManager.class);
      Map<String, Object> context = new HashMap<String, Object>();
      UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
      Node currentNode = uiExplorer.getCurrentNode();
      context.put(UIJCRExplorer.class.getName(), uiExplorer);
      context.put(Node.class.getName(), currentNode);
      return manager.addUIExtension(ManageViewService.EXTENSION_TYPE, action, context, this);
    } catch (Exception e) {
      LOG.error("An error occurs while checking the action", e);
    }
    return null;
  }
  
  static public class SearchActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIJCRExplorer uiJCRExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      UIPopupContainer UIPopupContainer = uiJCRExplorer.getChild(UIPopupContainer.class);
      UIPopupContainer.activate(UIECMSearch .class, 700);
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

  static public class ChangeTabActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource());
    }
  }
}