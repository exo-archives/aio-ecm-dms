/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.portal.component.view.Util;
import org.exoplatform.services.cms.queries.QueryService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormInputInfo;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Dec 26, 2006  
 * 4:29:08 PM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/component/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UISimpleSearch.CancelActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UISimpleSearch.SearchActionListener.class),
      @EventConfig(listeners = UISimpleSearch.SaveActionListener.class),
      @EventConfig(listeners = UISimpleSearch.MoreConstraintsActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UISimpleSearch.RemoveConstraintActionListener.class, phase=Phase.DECODE)
    }    
)
public class UISimpleSearch extends UIForm {

  final static public String INPUT_SEARCH = "input" ;
  final static public String CONSTRAINTS = "constraints" ;
  final static public String QUERY_NAME = "queryName" ;
  
  private List<String> constraints_ = new ArrayList<String>() ;
  private String firstOperator_ ;
  private List<String> virtualConstraints_ = new ArrayList<String>() ;
  
  private static final String SQL_QUERY = "select * from nt:base where contains(*, '$1')";
  private static final String OTHER_SQL_QUERY = "select * from nt:base where ";
  private static final String ROOT_PATH_SQL_QUERY = "select * from nt:base where jcr:path like '%/$1' ";
  private static final String PATH_SQL_QUERY = "select * from nt:base where jcr:path like '$0/%/$1' ";
  
  public UISimpleSearch() throws Exception {
    addUIFormInput(new UIFormStringInput(QUERY_NAME, QUERY_NAME, null)) ;
    addUIFormInput(new UIFormStringInput(INPUT_SEARCH, INPUT_SEARCH, null)) ;
    UIFormInputSetWithAction uiInputAct = new UIFormInputSetWithAction("moreConstraints") ;
    uiInputAct.addUIFormInput(new UIFormInputInfo(CONSTRAINTS, CONSTRAINTS, null)) ;
    addUIComponentInput(uiInputAct) ;
    setActions(new String[] {"MoreConstraints", "Search", "Save", "Cancel"}) ;
  }
  
  public void updateAdvanceConstraint(String constraint, String operator, String virtualDateQuery) { 
    if(constraint.length() > 0) {
      if(constraints_.size() == 0) {
        firstOperator_ = operator.toUpperCase() ;
        constraints_.add("(" + constraint + " )") ;
        if(virtualDateQuery != null) virtualConstraints_.add("(" + virtualDateQuery + " )") ;
        else virtualConstraints_.add("(" + constraint + " )") ;
      } else {
        constraints_.add(" "+operator.toUpperCase()+" (" + constraint + " ) ") ;
        if(virtualDateQuery != null) virtualConstraints_.add(" "+operator.toUpperCase()+" (" + virtualDateQuery + " ) ") ;
        else virtualConstraints_.add(" "+operator.toUpperCase()+" (" + constraint + " ) ") ;
      }
    }
    UIFormInputSetWithAction inputInfor = getChildById("moreConstraints") ;
    inputInfor.setIsDeleteOnly(true) ;
    inputInfor.setListInfoField(CONSTRAINTS, virtualConstraints_) ;
    String[] actionInfor = {"RemoveConstraint"} ;
    inputInfor.setActionInfo(CONSTRAINTS, actionInfor) ;
  }
  
  private String getQueryStatement() throws Exception {
    Node currentNode = getAncestorOfType(UIJCRExplorer.class).getCurrentNode() ;
    String statement = "" ;
    String text = getUIStringInput(INPUT_SEARCH).getValue() ;
    if(text != null && constraints_.size() == 0) {
      statement = StringUtils.replace(SQL_QUERY, "$1", text);
      if ("/".equals(currentNode.getPath())) {
        statement = StringUtils.replace(SQL_QUERY, "$1", text);
      } else if(currentNode.getParent().getPath().equals("/")) {
        statement = statement + "and jcr:path like '/%" + text + "'" ;
      } else {
        statement = statement + "and jcr:path like '"+currentNode.getParent().getPath()+"/%/" + text + "'" ;
      }
    } else if(constraints_.size() > 0) {
      if(text == null) {
        statement = StringUtils.replace(OTHER_SQL_QUERY, "$1", text) ;
        if ("/".equals(currentNode.getPath())) {
          statement = StringUtils.replace(OTHER_SQL_QUERY, "$1", text) ;
        } else {
          statement = statement + "jcr:path like '" + currentNode.getPath() + "/%'" ;
          statement = statement + " " + firstOperator_ + " ";
        } 
      } else {
        statement = StringUtils.replace(SQL_QUERY, "$1", text) ;
        if ("/".equals(currentNode.getPath())) {
          statement = StringUtils.replace(SQL_QUERY, "$1", text) ;
        } else {
          statement = statement + "or jcr:path like '"+currentNode.getPath()+"/%/" + text + "'" ;
        } 
        statement = statement + " " + firstOperator_ + " ";
      }
      for(String constraint : constraints_) {
        statement = statement + constraint ;
      }
    }
    return statement ;
  }
  
  static  public class SaveActionListener extends EventListener<UISimpleSearch> {
    public void execute(Event<UISimpleSearch> event) throws Exception {
      UISimpleSearch uiSimpleSearch = event.getSource() ;
      UIECMSearch uiECMSearch = uiSimpleSearch.getParent() ;
      UIApplication uiApp = uiSimpleSearch.getAncestorOfType(UIApplication.class) ;
      String userName = Util.getPortalRequestContext().getRemoteUser() ;
      QueryService queryService = uiSimpleSearch.getApplicationComponent(QueryService.class) ;
      String text = uiSimpleSearch.getUIStringInput(INPUT_SEARCH).getValue() ;
      if((text == null) && uiSimpleSearch.constraints_.size() == 0) {
        uiApp.addMessage(new ApplicationMessage("UISimpleSearch.msg.value-save-null", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String queryName = uiSimpleSearch.getUIStringInput(QUERY_NAME).getValue() ;
      if(queryName == null || queryName.trim().length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UISimpleSearch.msg.query-name-null", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      try {
        queryService.addQuery(queryName, uiSimpleSearch.getQueryStatement(), Query.SQL, userName) ;        
      } catch (Exception e){
        uiApp.addMessage(new ApplicationMessage("UISimpleSearch.msg.save-failed", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      uiECMSearch.getChild(UISavedQuery.class).updateGrid() ;
      uiECMSearch.setRenderedChild(UISavedQuery.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiECMSearch.getParent()) ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UISimpleSearch> {
    public void execute(Event<UISimpleSearch> event) throws Exception {
      event.getSource().getAncestorOfType(UIJCRExplorer.class).cancelAction() ;
    }
  }
  
  static  public class RemoveConstraintActionListener extends EventListener<UISimpleSearch> {
    public void execute(Event<UISimpleSearch> event) throws Exception {
      UISimpleSearch uiSimpleSearch = event.getSource() ;
      int intIndex = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      uiSimpleSearch.constraints_.remove(intIndex) ;
      uiSimpleSearch.virtualConstraints_.remove(intIndex) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSimpleSearch.getParent()) ;
    }
  }
  
  static public class SearchActionListener extends EventListener<UISimpleSearch> {
    public void execute(Event<UISimpleSearch> event) throws Exception {
      UISimpleSearch uiSimpleSearch = event.getSource();
      UIApplication uiApp = uiSimpleSearch.getAncestorOfType(UIApplication.class) ;
      String text = uiSimpleSearch.getUIStringInput(INPUT_SEARCH).getValue() ;
      UIJCRExplorer uiExplorer = uiSimpleSearch.getAncestorOfType(UIJCRExplorer.class);
      QueryManager queryManager = uiExplorer.getSession().getWorkspace().getQueryManager() ;
      UIECMSearch uiECMSearch = uiSimpleSearch.getParent() ; 
      UISearchResult uiSearchResult = uiECMSearch.getChild(UISearchResult.class) ;
      uiSearchResult.resultMap_.clear() ;
      if((text == null) && uiSimpleSearch.constraints_.size() == 0) {
        uiApp.addMessage(new ApplicationMessage("UISimpleSearch.msg.value-null", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(text != null) {
        String queryPath = null;
        if ("/".equals(uiExplorer.getCurrentNode().getPath())) {
          queryPath = ROOT_PATH_SQL_QUERY;
        } else if(uiExplorer.getCurrentNode().getParent().getPath().equals("/")) {
          queryPath = StringUtils.replace(PATH_SQL_QUERY, "$0", "");
        } else {
          queryPath = StringUtils.replace(PATH_SQL_QUERY, "$0", uiExplorer.getCurrentNode().getParent().getPath());
        }
        String statementPath = StringUtils.replace(queryPath, "$1", text) ;
        Query pathQuery = queryManager.createQuery(statementPath, Query.SQL);
        QueryResult pathQueryResult = pathQuery.execute() ;
        uiSearchResult.setQueryResults(pathQueryResult) ;
      }
      String statement = uiSimpleSearch.getQueryStatement() ;
      Query query = queryManager.createQuery(statement, Query.SQL);      
      QueryResult queryResult = query.execute();
      uiSearchResult.setQueryResults(queryResult) ;
      uiSearchResult.updateGrid() ;
      uiECMSearch.setRenderedChild(UISearchResult.class) ;
      uiSimpleSearch.constraints_.clear() ;
      uiSimpleSearch.virtualConstraints_.clear() ;
      uiSimpleSearch.reset() ;
    }
  }
  
  static  public class MoreConstraintsActionListener extends EventListener<UISimpleSearch> {
    public void execute(Event<UISimpleSearch> event) throws Exception {
      UIECMSearch uiECMSearch = event.getSource().getParent() ;
      UIConstraintsForm uiConstraintsForm = uiECMSearch.getChild(UIConstraintsForm.class) ;
      if(uiConstraintsForm == null) {
        uiECMSearch.addChild(UIConstraintsForm.class, null, null) ;
      }
      UISearchContainer uiContainer = event.getSource().getAncestorOfType(UISearchContainer.class) ;
      uiECMSearch.setRenderedChild(UIConstraintsForm.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }
}
