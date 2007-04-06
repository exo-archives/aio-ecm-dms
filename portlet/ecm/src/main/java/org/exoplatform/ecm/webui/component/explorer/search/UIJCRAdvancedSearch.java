/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIPopupAction;
import org.exoplatform.portal.component.view.Util;
import org.exoplatform.services.cms.queries.QueryService;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIFormTextAreaInput;
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
 * Oct 2, 2006
 * 1:55:22 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl",
    events = {
      @EventConfig(listeners = UIJCRAdvancedSearch.SaveActionListener.class),
      @EventConfig(listeners = UIJCRAdvancedSearch.SearchActionListener.class),
      @EventConfig(listeners = UIJCRAdvancedSearch.CancelActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIJCRAdvancedSearch.ChangeOptionActionListener.class)
    }    
)
public class UIJCRAdvancedSearch extends UIForm {
  public static final String FIELD_NAME = "name" ;
  public static final String FIELD_QUERY = "query" ;
  public static final String FIELD_SELECT_BOX = "selectBox" ;

  private static final String ROOT_SQL_QUERY = "select * from nt:base" ;
  private static final String SQL_QUERY = "select * from nt:base where jcr:path like '$0/%'" ;
  private static final String ROOT_XPATH_QUERY = "//*" ;
  private static final String XPATH_QUERY = "/jcr:root$0//*" ;
  private static final String CHANGE_OPTION = "ChangeOption" ;
  
  public UIJCRAdvancedSearch() throws Exception  {
    addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null)) ;
    List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>() ;
    ls.add(new SelectItemOption<String>("SQL", "sql")) ;
    ls.add(new SelectItemOption<String>("xPath", "xpath")) ;
    UIFormSelectBox uiSelectBox = new UIFormSelectBox(FIELD_SELECT_BOX, FIELD_SELECT_BOX, ls) ;
    uiSelectBox.setOnChange("Change") ;
    addUIFormInput(uiSelectBox) ;
    addUIFormInput(new UIFormTextAreaInput(FIELD_QUERY, FIELD_QUERY, null)) ;
    setActions(new String[]{"Search", "Save", "Cancel"}) ;
  }
  
  public void update() throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    Node selectedNode = uiExplorer.getCurrentNode() ;
    String path = selectedNode.getPath() ;
    String queryText = StringUtils.replace(SQL_QUERY, "$0", path) ;
    if ("/".equals(path)) queryText = ROOT_SQL_QUERY  ; 
    getUIFormSelectBox(FIELD_SELECT_BOX).setOnChange(CHANGE_OPTION) ;
    getUIFormSelectBox(FIELD_SELECT_BOX).setValue("sql") ;
    getUIFormTextAreaInput(FIELD_QUERY).setValue(queryText) ;
  }
  
  static  public class CancelActionListener extends EventListener<UIJCRAdvancedSearch> {
    public void execute(Event<UIJCRAdvancedSearch> event) throws Exception {
      event.getSource().getAncestorOfType(UIPopupAction.class).deActivate() ;
      event.getSource().getAncestorOfType(UIJCRExplorer.class).cancelAction() ;
    }
  }
  
  static public class SearchActionListener extends EventListener<UIJCRAdvancedSearch> {
    public void execute(Event<UIJCRAdvancedSearch> event) throws Exception {
      UIJCRAdvancedSearch uiForm = event.getSource() ;
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
      String queryS = uiForm.getUIFormTextAreaInput(FIELD_QUERY).getValue() ;
      String searchType = uiForm.getUIFormSelectBox(FIELD_SELECT_BOX).getValue() ;
      UIECMSearch uiSearch = uiForm.getParent() ;
      QueryManager queryManager = uiExplorer.getSession().getWorkspace().getQueryManager() ;
      Query query = queryManager.createQuery(queryS, searchType);
      QueryResult queryResult = query.execute();
      UISearchResult uiSearchResult = uiSearch.getChild(UISearchResult.class) ;
      uiSearchResult.resultMap_.clear() ;
      uiSearchResult.setQueryResults(queryResult) ;
      uiSearchResult.updateGrid(uiSearchResult.getNodeIterator()) ;
      uiSearch.setRenderedChild(UISearchResult.class) ;
    }
  }

  static  public class ChangeOptionActionListener extends EventListener<UIJCRAdvancedSearch> {
    public void execute(Event<UIJCRAdvancedSearch> event) throws Exception {
      UIJCRAdvancedSearch uiForm = event.getSource() ;     
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
      String  currentPath = uiExplorer.getCurrentNode().getPath() ;
      String queryText = "" ;      
      uiForm.setRenderSibbling(UIJCRAdvancedSearch.class) ;
      String searchType = uiForm.getUIFormSelectBox(FIELD_SELECT_BOX).getValue() ;
      if(searchType.equals(Query.SQL)){
        if ("/".equals(currentPath)) queryText = ROOT_SQL_QUERY ;
        else queryText = StringUtils.replace(SQL_QUERY, "$0", currentPath) ;     
        uiForm.getUIFormTextAreaInput(FIELD_QUERY).setValue(queryText) ;
      } else {
        if ("/".equals(currentPath)) queryText = ROOT_XPATH_QUERY ;
        else queryText = StringUtils.replace(XPATH_QUERY, "$0", currentPath) ;
        uiForm.getUIFormTextAreaInput(FIELD_QUERY).setValue(queryText) ;       
      }
    }
  }
  
  static  public class SaveActionListener extends EventListener<UIJCRAdvancedSearch> {
    public void execute(Event<UIJCRAdvancedSearch> event) throws Exception {
      UIJCRAdvancedSearch uiForm = event.getSource() ;
      QueryService queryService = uiForm.getApplicationComponent(QueryService.class) ;
      String name = uiForm.getUIStringInput(FIELD_NAME).getValue() ;
      String statement = uiForm.getUIFormTextAreaInput(FIELD_QUERY).getValue() ;
      String userName = Util.getUIPortal().getOwner() ;
      queryService.addQuery(name, statement, uiForm.getUIFormSelectBox(FIELD_SELECT_BOX).getValue(), userName) ;
      UIECMSearch uiSearch = uiForm.getParent() ;
      uiSearch.getChild(UISavedQuery.class).updateGrid() ;
      uiSearch.setRenderedChild(UISavedQuery.class) ;
    }
  }
}