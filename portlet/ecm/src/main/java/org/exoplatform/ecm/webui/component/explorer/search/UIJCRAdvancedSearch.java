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
import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorerPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.queries.QueryService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong  
 *          trongtt@gmail.com
 * Oct 2, 2006
 * 1:55:22 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIJCRAdvancedSearch.SaveActionListener.class),
      @EventConfig(listeners = UIJCRAdvancedSearch.SearchActionListener.class),
      @EventConfig(listeners = UIJCRAdvancedSearch.CancelActionListener.class, phase = Phase.DECODE),
      @EventConfig(phase=Phase.DECODE, listeners = UIJCRAdvancedSearch.ChangeOptionActionListener.class)
    }    
)
public class UIJCRAdvancedSearch extends UIForm implements UIPopupComponent {
  public static final String FIELD_NAME = "name" ;
  public static final String FIELD_QUERY = "query" ;
  public static final String FIELD_SELECT_BOX = "selectBox" ;

  private static final String ROOT_SQL_QUERY = "select * from nt:base" ;
  private static final String SQL_QUERY = "select * from nt:base where jcr:path like '$0/%'" ;
  private static final String ROOT_XPATH_QUERY = "//*" ;
  private static final String XPATH_QUERY = "/jcr:root$0//*" ;
  private static final String CHANGE_OPTION = "ChangeOption" ;
  private boolean isEdit_ = false ;
  private String queryPath_ ;

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

  public void update(Query query) throws Exception {
    if(query == null) {
      UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
      Node selectedNode = uiExplorer.getCurrentNode() ;
      String path = selectedNode.getPath() ;
      String queryText = StringUtils.replace(SQL_QUERY, "$0", path) ;
      if ("/".equals(path)) queryText = ROOT_SQL_QUERY  ; 
      getUIStringInput(FIELD_NAME).setValue(null) ;
      getUIStringInput(FIELD_NAME).setEditable(true) ;
      getUIFormSelectBox(FIELD_SELECT_BOX).setOnChange(CHANGE_OPTION) ;
      getUIFormSelectBox(FIELD_SELECT_BOX).setValue("sql") ;
      getUIFormTextAreaInput(FIELD_QUERY).setValue(queryText) ;
    } else {
      String storedPath = query.getStoredQueryPath() ;
      queryPath_ = storedPath ;
      storedPath = storedPath.substring(storedPath.lastIndexOf("/") + 1, storedPath.length()) ;
      getUIStringInput(FIELD_NAME).setValue(storedPath) ;
      getUIStringInput(FIELD_NAME).setEditable(false) ;
      getUIFormSelectBox(FIELD_SELECT_BOX).setOnChange(CHANGE_OPTION) ;
      getUIFormSelectBox(FIELD_SELECT_BOX).setValue(query.getLanguage()) ;
      getUIFormTextAreaInput(FIELD_QUERY).setValue(query.getStatement()) ;
    }
  }

  public void setIsEdit(boolean isEdit) { isEdit_ = isEdit ; }
  public boolean isEdit() { return isEdit_ ; }
  
  public void activate() throws Exception {}
  
  public void deActivate() throws Exception {}

  static  public class CancelActionListener extends EventListener<UIJCRAdvancedSearch> {
    public void execute(Event<UIJCRAdvancedSearch> event) throws Exception {
      UIJCRAdvancedSearch uiJAdvancedSearch = event.getSource() ;
      if(uiJAdvancedSearch.isEdit_) {
        UIPopupWindow uiPopup = uiJAdvancedSearch.getParent() ;
        uiPopup.setShow(false) ;
        uiPopup.setRendered(false) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiJAdvancedSearch.getParent()) ;
      } else {
        uiJAdvancedSearch.getAncestorOfType(UIPopupAction.class).deActivate() ;
        uiJAdvancedSearch.getAncestorOfType(UIJCRExplorer.class).cancelAction() ;
      }
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
      QueryResult queryResult = null ;
      try {
        queryResult = query.execute();
      } catch (Exception e){
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIJCRAdvancedSearch.msg.invalid-queryStatement", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UISearchResult uiSearchResult = uiSearch.getChild(UISearchResult.class) ;
      uiSearchResult.resultMap_.clear() ;
      uiSearchResult.setQueryResults(queryResult) ;
      uiSearchResult.updateGrid() ;
      uiSearch.setRenderedChild(UISearchResult.class) ;
    }
  }

  static  public class ChangeOptionActionListener extends EventListener<UIJCRAdvancedSearch> {
    public void execute(Event<UIJCRAdvancedSearch> event) throws Exception {
      UIJCRAdvancedSearch uiForm = event.getSource() ;     
      if(uiForm.isEdit_) {
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
        return ;
      }
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
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }

  static  public class SaveActionListener extends EventListener<UIJCRAdvancedSearch> {
    public void execute(Event<UIJCRAdvancedSearch> event) throws Exception {
      UIJCRAdvancedSearch uiForm = event.getSource() ;
      String statement = uiForm.getUIFormTextAreaInput(FIELD_QUERY).getValue() ;
      String queryLang = uiForm.getUIFormSelectBox(FIELD_SELECT_BOX).getValue() ;
      if(!uiForm.isEdit_) {
        String repository = uiForm.getAncestorOfType(UIJCRExplorerPortlet.class).getPreferenceRepository() ;
        QueryService queryService = uiForm.getApplicationComponent(QueryService.class) ;
        String name = uiForm.getUIStringInput(FIELD_NAME).getValue() ;
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        if(statement == null || statement.trim().length() ==0) {
          uiApp.addMessage(new ApplicationMessage("UIJCRAdvancedSearch.msg.value-save-null", null, 
                                                  ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        if(name == null || name.trim().length() == 0) {
          uiApp.addMessage(new ApplicationMessage("UIJCRAdvancedSearch.msg.query-name-null", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        String userName = Util.getPortalRequestContext().getRemoteUser() ;
        try {
          queryService.addQuery(name, statement, queryLang, userName, repository) ;        
        } catch(Exception e){
          uiApp.addMessage(new ApplicationMessage("UIJCRAdvancedSearch.msg.save_unSuccessful", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        UIECMSearch uiSearch = uiForm.getParent() ;
        uiSearch.getChild(UISavedQuery.class).updateGrid() ;
        uiForm.update(null) ;
        uiSearch.setRenderedChild(UISavedQuery.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiSearch) ;
      } else {
        UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
        Node queryNode = (Node) uiExplorer.getSession().getItem(uiForm.queryPath_) ;
        queryNode.setProperty("jcr:language", queryLang) ;
        queryNode.setProperty("jcr:statement", statement) ;
        queryNode.save() ;
        if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save() ;
        UISavedQuery uiSavedQuery = uiForm.getAncestorOfType(UISavedQuery.class) ; 
        uiSavedQuery.updateGrid() ;
        uiSavedQuery.removeChildById(UISavedQuery.EDIT_FORM) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiSavedQuery.getParent()) ;
      }
    }
  }
}