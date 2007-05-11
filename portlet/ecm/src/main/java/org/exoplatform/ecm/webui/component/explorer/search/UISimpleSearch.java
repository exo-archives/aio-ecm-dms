/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
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
    template =  "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl",
    events = {
      @EventConfig(listeners = UISimpleSearch.CancelActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UISimpleSearch.SearchActionListener.class),
      @EventConfig(listeners = UISimpleSearch.MoreConstraintsActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UISimpleSearch.RemoveConstraintActionListener.class, phase=Phase.DECODE)
    }    
)
public class UISimpleSearch extends UIForm {

  final static public String INPUT_SEARCH = "input" ;
  final static public String CONSTRAINTS = "constraints" ;
  
  private List<String> constraints_ = new ArrayList<String>() ;
  private String firstOperator_ ;
  
  private static final String SQL_QUERY = "select * from nt:base where contains(*, '$1')";
  private static final String OTHER_SQL_QUERY = "select * from nt:base where ";
  
  public UISimpleSearch() throws Exception {
    addUIFormInput(new UIFormStringInput(INPUT_SEARCH, INPUT_SEARCH, null)) ;
    UIFormInputSetWithAction uiInputAct = new UIFormInputSetWithAction("moreConstraints") ;
    uiInputAct.addUIFormInput(new UIFormInputInfo(CONSTRAINTS, CONSTRAINTS, null)) ;
    addUIComponentInput(uiInputAct) ;
    setActions(new String[] {"MoreConstraints", "Search", "Cancel"}) ;
  }

  public void updateAdvanceConstraint(String constraint, String operator) { 
    if(constraint.length() > 0) {
      if(constraints_.size() == 0) {
        firstOperator_ = operator.toUpperCase() ;
        constraints_.add("(" + constraint + " )") ;
      } else {
        constraints_.add(" "+operator.toUpperCase()+" (" + constraint + " ) ") ;
      }
    }
    UIFormInputSetWithAction inputInfor = getChildById("moreConstraints") ;
    inputInfor.setIsDeleteOnly(true) ;
    inputInfor.setListInfoField(CONSTRAINTS, constraints_) ;
    String[] actionInfor = {"RemoveConstraint"} ;
    inputInfor.setActionInfo(CONSTRAINTS, actionInfor) ;
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
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSimpleSearch.getParent()) ;
    }
  }
  
  static public class SearchActionListener extends EventListener<UISimpleSearch> {
    public void execute(Event<UISimpleSearch> event) throws Exception {
      UISimpleSearch uiForm = event.getSource();
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      String text = uiForm.getUIStringInput(INPUT_SEARCH).getValue() ;
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class);
      QueryManager queryManager = uiExplorer.getSession().getWorkspace().getQueryManager() ;
      String statement = null ;
      if((text == null) && uiForm.constraints_.size() == 0) {
        uiApp.addMessage(new ApplicationMessage("UISimpleSearch.msg.value-null", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(text != null && uiForm.constraints_.size() == 0) {
        statement = StringUtils.replace(SQL_QUERY, "$1", text) ;
      } else if(uiForm.constraints_.size() > 0) {
        if(text == null) {
          statement = StringUtils.replace(OTHER_SQL_QUERY, "$1", text) ;
        } else {
          statement = StringUtils.replace(SQL_QUERY, "$1", text) ;
          statement = statement + " " + uiForm.firstOperator_ + " ";
        }
        for(String constraint : uiForm.constraints_) {
          statement = statement + constraint ;
        }
      }
      Query query = queryManager.createQuery(statement, Query.SQL);                
      QueryResult queryResult = query.execute();
      UIECMSearch uiECMSearch = uiForm.getParent() ; 
      UISearchResult uiSearchResult = uiECMSearch.getChild(UISearchResult.class) ;
      uiSearchResult.resultMap_.clear() ;
      uiSearchResult.setQueryResults(queryResult) ;
      uiSearchResult.updateGrid() ;
      uiECMSearch.setRenderedChild(UISearchResult.class) ;
      uiForm.constraints_.clear() ;
      uiForm.reset() ;
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
