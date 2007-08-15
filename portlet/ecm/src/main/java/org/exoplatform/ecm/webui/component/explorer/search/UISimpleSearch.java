/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
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
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Dec 26, 2006  
 * 4:29:08 PM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIForm.gtmpl",
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
  final static public String NODE_PATH = "nodePath" ;
  final static public String FIRST_OPERATOR = "firstOperator" ;
  final static public String OR = "or" ;
  final static public String AND = "and" ;
  
  private List<String> constraints_ = new ArrayList<String>() ;
  private List<String> virtualConstraints_ = new ArrayList<String>() ;
  
  private static final String ROOT_XPATH_QUERY = "//*" ;
  private static final String XPATH_QUERY = "/jcr:root$0//*" ;
  
  public UISimpleSearch() throws Exception {
    addUIFormInput(new UIFormInputInfo(NODE_PATH, NODE_PATH, null)) ;
    addUIFormInput(new UIFormStringInput(INPUT_SEARCH, INPUT_SEARCH, null)) ;
    List<SelectItemOption<String>> operators = new ArrayList<SelectItemOption<String>>() ;
    operators.add(new SelectItemOption<String>(AND, AND)) ;
    operators.add(new SelectItemOption<String>(OR, OR)) ;
    addUIFormInput(new UIFormSelectBox(FIRST_OPERATOR, FIRST_OPERATOR, operators)) ;
    UIFormInputSetWithAction uiInputAct = new UIFormInputSetWithAction("moreConstraints") ;
    uiInputAct.addUIFormInput(new UIFormInputInfo(CONSTRAINTS, CONSTRAINTS, null)) ;
    addUIComponentInput(uiInputAct) ;
    setActions(new String[] {"MoreConstraints", "Search", "Save", "Cancel"}) ;
  }
  
  public List<String> getConstraints() { return constraints_ ; }
  
  public void updateAdvanceConstraint(String constraint, String operator, String virtualDateQuery) { 
    if(constraint.length() > 0) {
      if(constraints_.size() == 0) {
        constraints_.add("(" + constraint + " )") ;
        if(virtualDateQuery != null) virtualConstraints_.add("(" + virtualDateQuery + " )") ;
        else virtualConstraints_.add("(" + constraint + " )") ;
      } else {
        constraints_.add(" "+operator.toLowerCase()+" (" + constraint + " ) ") ;
        if(virtualDateQuery != null) virtualConstraints_.add(" "+operator.toLowerCase()+" (" + virtualDateQuery + " ) ") ;
        else virtualConstraints_.add(" "+operator.toLowerCase()+" (" + constraint + " ) ") ;
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
      if ("/".equals(currentNode.getPath())) {
        statement = ROOT_XPATH_QUERY + "[(jcr:contains(.,'"+text+"'))" ;
      } else {
        statement = StringUtils.replace(XPATH_QUERY, "$0", currentNode.getPath()) + "[(jcr:contains(.,'"+text+"'))" ;
      }
      statement = statement + "]" ;
    } else if(constraints_.size() > 0) {
      if(text == null) {
        if ("/".equals(currentNode.getPath())) {
          statement = ROOT_XPATH_QUERY + "[(" ;
        } else {
          statement = StringUtils.replace(XPATH_QUERY, "$0", currentNode.getPath()) + "[(";
        } 
      } else {
        String operator = getUIFormSelectBox(FIRST_OPERATOR).getValue() ;
        if ("/".equals(currentNode.getPath())) {
          statement = ROOT_XPATH_QUERY + "[(jcr:contains(.,'"+text+"'))" ;
        } else {
          statement = StringUtils.replace(XPATH_QUERY, "$0", currentNode.getPath()) + "[(jcr:contains(.,'"+text+"'))" ;
        } 
        statement = statement + " " + operator + " (";
      }
      for(String constraint : constraints_) {
        statement = statement + constraint ;
      }
      statement = statement + ")]" ;
    }
    return statement ;
  }
  
  static  public class SaveActionListener extends EventListener<UISimpleSearch> {
    public void execute(Event<UISimpleSearch> event) throws Exception {
      UISimpleSearch uiSimpleSearch = event.getSource() ;
      UIApplication uiApp = uiSimpleSearch.getAncestorOfType(UIApplication.class) ;
      String text = uiSimpleSearch.getUIStringInput(INPUT_SEARCH).getValue() ;
      if((text == null) && uiSimpleSearch.constraints_.size() == 0) {
        uiApp.addMessage(new ApplicationMessage("UISimpleSearch.msg.value-save-null", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UISearchContainer uiSearchContainer = uiSimpleSearch.getParent() ;
      uiSearchContainer.initSaveQueryPopup(uiSimpleSearch.getQueryStatement(), true, Query.XPATH) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSearchContainer) ;
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
      if(uiSimpleSearch.constraints_.size() > 0) {
        String newFirstConstraint = null;
        String newFirstVirtaulConstraint = null;
        if(uiSimpleSearch.constraints_.get(0).contains(OR)) {
          newFirstConstraint = uiSimpleSearch.constraints_.get(0).substring(3, uiSimpleSearch.constraints_.get(0).length()) ;
          newFirstVirtaulConstraint = uiSimpleSearch.virtualConstraints_.get(0).substring(3, uiSimpleSearch.constraints_.get(0).length()) ;
          uiSimpleSearch.constraints_.set(0, newFirstConstraint) ;
          uiSimpleSearch.virtualConstraints_.set(0, newFirstVirtaulConstraint) ;
        } else if(uiSimpleSearch.constraints_.get(0).contains(AND)) {
          newFirstConstraint = uiSimpleSearch.constraints_.get(0).substring(4, uiSimpleSearch.constraints_.get(0).length()) ;
          newFirstVirtaulConstraint = uiSimpleSearch.virtualConstraints_.get(0).substring(4, uiSimpleSearch.constraints_.get(0).length()) ;
          uiSimpleSearch.constraints_.set(0, newFirstConstraint) ;
          uiSimpleSearch.virtualConstraints_.set(0, newFirstVirtaulConstraint) ;
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSimpleSearch.getParent()) ;
    }
  }
  
  static public class SearchActionListener extends EventListener<UISimpleSearch> {
    public void execute(Event<UISimpleSearch> event) throws Exception {
      UISimpleSearch uiSimpleSearch = event.getSource();
      String text = uiSimpleSearch.getUIStringInput(INPUT_SEARCH).getValue() ;
      UIJCRExplorer uiExplorer = uiSimpleSearch.getAncestorOfType(UIJCRExplorer.class);
      Node currentNode = uiExplorer.getCurrentNode() ;
      QueryManager queryManager = uiExplorer.getSession().getWorkspace().getQueryManager() ;
      UIECMSearch uiECMSearch = uiSimpleSearch.getAncestorOfType(UIECMSearch.class) ; 
      UISearchResult uiSearchResult = uiECMSearch.getChild(UISearchResult.class) ;
      uiSearchResult.resultMap_.clear() ;
      if((text == null) && uiSimpleSearch.constraints_.size() == 0) {
        UIApplication uiApp = uiSimpleSearch.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UISimpleSearch.msg.value-null", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String statement = uiSimpleSearch.getQueryStatement() ;
      Query query = queryManager.createQuery(statement, Query.XPATH);      
      QueryResult queryResult = query.execute();
      uiSearchResult.setQueryResults(queryResult) ;
      uiSearchResult.updateGrid() ;
      uiECMSearch.setRenderedChild(UISearchResult.class) ;
      uiSimpleSearch.constraints_.clear() ;
      uiSimpleSearch.virtualConstraints_.clear() ;
      uiSimpleSearch.reset() ;
      uiSimpleSearch.getUIFormInputInfo(UISimpleSearch.NODE_PATH).setValue(currentNode.getPath()) ;
    }
  }
  
  static  public class MoreConstraintsActionListener extends EventListener<UISimpleSearch> {
    public void execute(Event<UISimpleSearch> event) throws Exception {
      UISearchContainer uiSearchContainer = event.getSource().getParent() ;
      UIConstraintsForm uiConstraintsForm = uiSearchContainer.getChild(UIConstraintsForm.class) ;
      if(uiConstraintsForm.isRendered()) uiConstraintsForm.setRendered(false) ;
      else uiConstraintsForm.setRendered(true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSearchContainer) ;
    }
  }
}