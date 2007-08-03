/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
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
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trong.tran@exoplatform.com
 * Dec 26, 2006  
 * 4:29:08 PM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/explorer/search/UIConstraintsForm.gtmpl",
    events = {
      @EventConfig(phase=Phase.DECODE, listeners = UIConstraintsForm.CancelActionListener.class),
      @EventConfig(listeners = UIConstraintsForm.AddActionListener.class),
      @EventConfig(listeners = UIConstraintsForm.CompareExactlyActionListener.class),
      @EventConfig(listeners = UIConstraintsForm.AddMetadataTypeActionListener.class),
      @EventConfig(listeners = UIConstraintsForm.AddNodeTypeActionListener.class)
    }    
)
public class UIConstraintsForm extends UIForm {

  final static public String OPERATOR = "operator" ;
  final static public String TIME_OPTION = "timeOpt" ;
  final static public String PROPERTY1 = "property1" ; 
  final static public String PROPERTY2 = "property2" ; 
  final static public String PROPERTY3 = "property3" ; 
  final static public String CONTAIN_EXACTLY = "containExactly" ; 
  final static public String CONTAIN = "contain" ;
  final static public String NOT_CONTAIN = "notContain" ;
  final static public String START_TIME = "startTime" ;
  final static public String END_TIME = "endTime" ;
  final static public String DOC_TYPE = "docType" ;
  final static public String AND_OPERATION = "and" ;
  final static public String OR_OPERATION = "or" ;
  final static public String CREATED_DATE = "CREATED" ;
  final static public String MODIFIED_DATE = "MODIFIED" ;
  final static public String EXACTLY_PROPERTY = "exactlyPro" ;
  final static public String CONTAIN_PROPERTY = "containPro" ;
  final static public String NOT_CONTAIN_PROPERTY = "notContainPro" ;
  final static public String DATE_PROPERTY = "datePro" ;
  final static public String NODETYPE_PROPERTY = "nodetypePro" ;
  
  private String virtualDateQuery_ ;
  
  public UIConstraintsForm() throws Exception {
    setActions(new String[] {"Add", "Cancel"}) ;
    List<SelectItemOption<String>> typeOperation = new ArrayList<SelectItemOption<String>>() ;
    typeOperation.add(new SelectItemOption<String>(AND_OPERATION, AND_OPERATION));
    typeOperation.add(new SelectItemOption<String>(OR_OPERATION, OR_OPERATION));
    addUIFormInput(new UIFormSelectBox(OPERATOR, OPERATOR, typeOperation)) ;
    
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(EXACTLY_PROPERTY, EXACTLY_PROPERTY, null)) ;
    addUIFormInput(new UIFormStringInput(PROPERTY1, PROPERTY1, null)) ;
    addUIFormInput(new UIFormStringInput(CONTAIN_EXACTLY, CONTAIN_EXACTLY, null)) ;
    
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(CONTAIN_PROPERTY, CONTAIN_PROPERTY, null)) ;
    addUIFormInput(new UIFormStringInput(PROPERTY2, PROPERTY2, null)) ;
    addUIFormInput(new UIFormStringInput(CONTAIN, CONTAIN, null)) ;
    
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(NOT_CONTAIN_PROPERTY, NOT_CONTAIN_PROPERTY, null)) ;
    addUIFormInput(new UIFormStringInput(PROPERTY3, PROPERTY3, null)) ;
    addUIFormInput(new UIFormStringInput(NOT_CONTAIN, NOT_CONTAIN, null)) ;
    
    
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(DATE_PROPERTY, DATE_PROPERTY, null)) ;
    List<SelectItemOption<String>> dateOperation = new ArrayList<SelectItemOption<String>>() ;
    dateOperation.add(new SelectItemOption<String>(CREATED_DATE, CREATED_DATE));
    dateOperation.add(new SelectItemOption<String>(MODIFIED_DATE, MODIFIED_DATE));
    addUIFormInput(new UIFormSelectBox(TIME_OPTION, TIME_OPTION, dateOperation)) ;
    UIFormDateTimeInput uiFromDate = new UIFormDateTimeInput(START_TIME, START_TIME, null) ;
    uiFromDate.setDisplayTime(false) ;
    addUIFormInput(uiFromDate) ;
    UIFormDateTimeInput uiToDate = new UIFormDateTimeInput(END_TIME, END_TIME, null) ;
    uiToDate.setDisplayTime(false) ;
    addUIFormInput(uiToDate) ;
    addUIFormInput(new UIFormStringInput(DOC_TYPE, DOC_TYPE, null)) ;

    addUIFormInput(new UIFormCheckBoxInput<Boolean>(NODETYPE_PROPERTY, NODETYPE_PROPERTY, null)) ;
    addUIFormInput(new UIFormStringInput(DOC_TYPE, DOC_TYPE, null)) ;
  }

  private String getContainQueryString(String property, String type, boolean isContain) {
    String value = getUIStringInput(type).getValue() ;
    if(value == null) return "" ;
    if(value.trim().length() > 0) {
      if(isContain) return " jcr:contains(" + property.trim() + ", '"+ value.trim() + "')" ;
      return " fn:not(jcr:contains(" + property.trim() + ", '"+ value.trim() + "'))" ;
    }
    return "" ;
  }
  
  private String getDateTimeQueryString(String beforeDate, String afterDate, String type) {
    Calendar bfDate = getUIFormDateTimeInput(START_TIME).getCalendar() ;
    if(afterDate != null && afterDate.trim().length() > 0) {
      Calendar afDate = getUIFormDateTimeInput(END_TIME).getCalendar() ;
      if(type.equals(CREATED_DATE)) {
        virtualDateQuery_ = "(documents created from '"+beforeDate+"') and (documents created to '"+afterDate+"')" ;
        return "@exo:dateCreated >= xs:dateTime('"+ISO8601.format(bfDate)+"') and @exo:dateCreated < xs:dateTime('"+ISO8601.format(afDate)+"')" ;
      } else if(type.equals(MODIFIED_DATE)) {
        virtualDateQuery_ = "(documents modified from '"+beforeDate+"') and (documents modified to '"+afterDate+"')" ;
        return "@exo:dateModified >= xs:dateTime('"+ISO8601.format(bfDate)+"') and @exo:dateModified < xs:dateTime('"+ISO8601.format(afDate)+"')" ;
      }
    } else {
      if(type.equals(CREATED_DATE)) {
        virtualDateQuery_ = "(documents created from '"+beforeDate+"')" ;
        return "@exo:dateCreated >= xs:dateTime('"+ISO8601.format(bfDate)+"')" ;
      } else if(type.equals(MODIFIED_DATE)) {
        virtualDateQuery_ = "(documents modified from '"+beforeDate+"')" ;
        return "@exo:dateModified >= xs:dateTime('"+ISO8601.format(bfDate)+"')" ;
      }
    }
    return "" ;
  }
  
  private String getNodeTypeQueryString(String nodeTypes) {
    String advanceQuery = "" ;
    String[] arrNodeTypes = {} ;
    if(nodeTypes.indexOf(",") > -1) arrNodeTypes = nodeTypes.split(",") ;
    if(arrNodeTypes.length > 0) {
      for(String nodeType : arrNodeTypes) {
        if(advanceQuery.length() == 0) advanceQuery = "@jcr:primaryType = '" + nodeType + "'" ;
        else advanceQuery = advanceQuery + " " + OR_OPERATION + " " + "@jcr:primaryType = '" + nodeType + "'" ;
      }
    } else {
      advanceQuery = "@jcr:primaryType = '" + nodeTypes + "'" ;
    }
    return advanceQuery;
  }
  
  private void addConstraint(Event event, int opt) throws Exception {
    UIApplication uiApp = getAncestorOfType(UIApplication.class) ;
    String advanceQuery = "" ;
    String property ;
    virtualDateQuery_ = null ;
    UISimpleSearch uiSimpleSearch = ((UISearchContainer)getParent()).getChild(UISimpleSearch.class) ;
    switch (opt) {
      case 0:
        property = getUIStringInput(PROPERTY1).getValue() ;
        if(property == null || property.length() < 1) {
          uiApp.addMessage(new ApplicationMessage("UIConstraintsForm.msg.properties-required", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        String value = getUIStringInput(CONTAIN_EXACTLY).getValue() ;
        if(value == null || value.trim().length() < 0) {
          uiApp.addMessage(new ApplicationMessage("UIConstraintsForm.msg.exactly-require", null, 
                                                  ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        advanceQuery = "@" + property + " = '" + value.trim() + "'" ;
        break;
      case 1:
        property = getUIStringInput(PROPERTY2).getValue() ; 
        if(property == null || property.length() < 1) {
          uiApp.addMessage(new ApplicationMessage("UIConstraintsForm.msg.properties-required", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        advanceQuery = getContainQueryString(property, CONTAIN, true) ;
        break;
      case 2:
        property = getUIStringInput(PROPERTY3).getValue() ; 
        if(property == null || property.length() < 1) {
          uiApp.addMessage(new ApplicationMessage("UIConstraintsForm.msg.properties-required", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        advanceQuery = getContainQueryString(property, NOT_CONTAIN, false) ;
        break;
      case 3:
        String fromDate = getUIFormDateTimeInput(START_TIME).getValue() ;
        String toDate = getUIFormDateTimeInput(END_TIME).getValue() ;
        if(fromDate == null || fromDate.trim().length() == 0) {
          uiApp.addMessage(new ApplicationMessage("UIConstraintsForm.msg.fromDate-required", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        Calendar bfDate = getUIFormDateTimeInput(START_TIME).getCalendar() ;
        if(toDate != null && toDate.trim().length() >0) {
          Calendar afDate = getUIFormDateTimeInput(END_TIME).getCalendar() ;
          if(bfDate.compareTo(afDate) == 1) {
            uiApp.addMessage(new ApplicationMessage("UIConstraintsForm.msg.date-invalid", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
        }
        String type = getUIFormSelectBox(TIME_OPTION).getValue() ;
        advanceQuery = getDateTimeQueryString(fromDate, toDate, type) ;
        break ;
      case 4:
        property = getUIStringInput(DOC_TYPE).getValue() ;
        if(property == null || property.length() < 1) {
          uiApp.addMessage(new ApplicationMessage("UIConstraintsForm.msg.properties-required", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        advanceQuery = getNodeTypeQueryString(property) ;
        break;
      default:
        break;
    }
    uiSimpleSearch.updateAdvanceConstraint(advanceQuery, getUIFormSelectBox(OPERATOR).getValue(), virtualDateQuery_) ;
  }
  
  private void resetConstraintForm() {
    reset() ;
    getUIFormCheckBoxInput(EXACTLY_PROPERTY).setChecked(false) ;
    getUIFormCheckBoxInput(CONTAIN_PROPERTY).setChecked(false) ;
    getUIFormCheckBoxInput(NOT_CONTAIN_PROPERTY).setChecked(false) ;
    getUIFormCheckBoxInput(DATE_PROPERTY).setChecked(false) ;
    getUIFormCheckBoxInput(NODETYPE_PROPERTY).setChecked(false) ;
  }
  
  static public class AddActionListener extends EventListener<UIConstraintsForm> {
    public void execute(Event<UIConstraintsForm> event) throws Exception {
      UIConstraintsForm uiForm = event.getSource();
      boolean isExactly = uiForm.getUIFormCheckBoxInput(EXACTLY_PROPERTY).isChecked() ;
      boolean isContain = uiForm.getUIFormCheckBoxInput(CONTAIN_PROPERTY).isChecked() ;
      boolean isNotContain = uiForm.getUIFormCheckBoxInput(NOT_CONTAIN_PROPERTY).isChecked() ;
      boolean isDateTime = uiForm.getUIFormCheckBoxInput(DATE_PROPERTY).isChecked() ;
      boolean isNodeType = uiForm.getUIFormCheckBoxInput(NODETYPE_PROPERTY).isChecked() ;
      if(isExactly) uiForm.addConstraint(event, 0) ;
      if(isContain) uiForm.addConstraint(event, 1) ;
      if(isNotContain) uiForm.addConstraint(event, 2) ;
      if(isDateTime) uiForm.addConstraint(event, 3);
      if(isNodeType) uiForm.addConstraint(event, 4) ;
      uiForm.resetConstraintForm() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
  
  static public class AddMetadataTypeActionListener extends EventListener<UIConstraintsForm> {
    public void execute(Event<UIConstraintsForm> event) throws Exception {
      UISearchContainer uiContainer = event.getSource().getParent() ;
      String type = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String popupId = PROPERTY1;
      if(type.equals("1")) popupId = PROPERTY2 ;
      else if(type.equals("2")) popupId = PROPERTY3 ;
      uiContainer.initMetadataPopup(popupId) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }
  
  static public class AddNodeTypeActionListener extends EventListener<UIConstraintsForm> {
    public void execute(Event<UIConstraintsForm> event) throws Exception {
      UISearchContainer uiContainer = event.getSource().getParent() ;
      uiContainer.initNodeTypePopup() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }
  
  static public class CompareExactlyActionListener extends EventListener<UIConstraintsForm> {
    public void execute(Event<UIConstraintsForm> event) throws Exception {
      UIConstraintsForm uiConstraintForm = event.getSource();
      String property = uiConstraintForm.getUIStringInput(PROPERTY1).getValue() ;
      UIJCRExplorer uiExplorer = uiConstraintForm.getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = uiConstraintForm.getAncestorOfType(UIApplication.class) ;
      if(property == null || property.trim().length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIConstraintsForm.msg.properties-null", null, 
                                                 ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String currentPath = uiExplorer.getCurrentNode().getPath() ;
      String statement = "select * from nt:base where " ;
      if(!currentPath.equals("/")) statement = statement + "jcr:path like '"+ currentPath +"/%' AND " ;
      statement = statement + ""+property+" is not null" ;
      QueryManager queryManager = uiExplorer.getSession().getWorkspace().getQueryManager() ;
      Query query = queryManager.createQuery(statement, Query.SQL) ;
      QueryResult result = query.execute() ;
      if(result == null || result.getNodes().getSize() == 0) {
        uiApp.addMessage(new ApplicationMessage("UICompareExactlyForm.msg.not-result-found", null)) ; 
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UISearchContainer uiContainer = uiConstraintForm.getParent() ;
      UICompareExactlyForm uiCompareExactlyForm = 
        uiContainer.createUIComponent(UICompareExactlyForm.class, null, null) ;
      UIPopupAction uiPopup = uiContainer.getChild(UIPopupAction.class);
      uiPopup.getChild(UIPopupWindow.class).setId("ExactlyFormPopup") ;
      uiCompareExactlyForm.init(property, result) ;
      uiPopup.activate(uiCompareExactlyForm, 600, 500) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIConstraintsForm> {
    public void execute(Event<UIConstraintsForm> event) throws Exception {
      UISearchContainer uiSearchContainer = event.getSource().getParent() ;
      event.getSource().setRendered(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSearchContainer) ;
    }
  }
}