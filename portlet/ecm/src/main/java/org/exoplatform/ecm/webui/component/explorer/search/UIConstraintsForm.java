/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.bean.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
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
      @EventConfig(listeners = UIConstraintsForm.SaveActionListener.class),
      @EventConfig(listeners = UIConstraintsForm.CompareExactlyActionListener.class),
      @EventConfig(listeners = UIConstraintsForm.AddMetadataTypeActionListener.class),
      @EventConfig(listeners = UIConstraintsForm.AddNodeTypeActionListener.class)
    }    
)
public class UIConstraintsForm extends UIForm {

  final static public String CONTAIN_OPERATOR = "containOperator" ;
  final static public String NOT_CONTAIN_OPERATOR = "notContainOperator" ;
  final static public String EXACTLY_OPERATOR = "exactlyOperator" ;
  final static public String OPERATOR = "operator" ;
  final static public String TIME_OPTION = "timeOpt" ;
  final static public String CONSTRAINT = "constraint" ;
  final static public String PROPERTY1 = "property1" ; 
  final static public String PROPERTY2 = "property2" ; 
  final static public String PROPERTY3 = "property3" ; 
  final static public String CONTAIN_EXACTLY = "containExactly" ; 
  final static public String CONTAIN = "contain" ;
  final static public String NOT_CONTAIN = "notContain" ;
  final static public String START_TIME = "startTime" ;
  final static public String END_TIME = "endTime" ;
  final static public String DOC_TYPE = "docType" ;
  final static public String AND_OPERATION = "AND" ;
  final static public String OR_OPERATION = "OR" ;
  final static public String CREATED_DATE = "CREATED" ;
  final static public String MODIFIED_DATE = "MODIFIED" ;
  
  private String virtualDateQuery_ ;
  
  public UIConstraintsForm() throws Exception {
    setActions(new String[] {"Save", "Cancel"}) ;
    addUIFormInput(new UIFormRadioBoxInput(CONSTRAINT, "0")) ;

    List<SelectItemOption<String>> typeOperation = new ArrayList<SelectItemOption<String>>() ;
    typeOperation.add(new SelectItemOption<String>(AND_OPERATION, AND_OPERATION));
    typeOperation.add(new SelectItemOption<String>(OR_OPERATION, OR_OPERATION));
    addUIFormInput(new UIFormSelectBox(OPERATOR, OPERATOR, typeOperation)) ;

    addUIFormInput(new UIFormStringInput(PROPERTY1, PROPERTY1, null)) ;
    addUIFormInput(new UIFormSelectBox(EXACTLY_OPERATOR, EXACTLY_OPERATOR, typeOperation)) ;
    addUIFormInput(new UIFormStringInput(CONTAIN_EXACTLY, CONTAIN_EXACTLY, null)) ;
    addUIFormInput(new UIFormStringInput(PROPERTY2, PROPERTY2, null)) ;
    addUIFormInput(new UIFormSelectBox(CONTAIN_OPERATOR, CONTAIN_OPERATOR, typeOperation)) ;
    addUIFormInput(new UIFormStringInput(CONTAIN, CONTAIN, null)) ;
    addUIFormInput(new UIFormStringInput(PROPERTY3, PROPERTY3, null)) ;
    addUIFormInput(new UIFormSelectBox(NOT_CONTAIN_OPERATOR, NOT_CONTAIN_OPERATOR, typeOperation)) ;
    addUIFormInput(new UIFormStringInput(NOT_CONTAIN, NOT_CONTAIN, null)) ;
    
    List<SelectItemOption<String>> dateOperation = new ArrayList<SelectItemOption<String>>() ;
    dateOperation.add(new SelectItemOption<String>(CREATED_DATE, CREATED_DATE));
    dateOperation.add(new SelectItemOption<String>(MODIFIED_DATE, MODIFIED_DATE));
    addUIFormInput(new UIFormSelectBox(TIME_OPTION, TIME_OPTION, dateOperation)) ;
    addUIFormInput(new UIFormDateTimeInput(START_TIME, START_TIME, null)) ;
    addUIFormInput(new UIFormDateTimeInput(END_TIME, END_TIME, null)) ;
    addUIFormInput(new UIFormStringInput(DOC_TYPE, DOC_TYPE, null)) ;
  }

  public String renderConstraintRadioBox(int index) {
    StringBuilder input = new StringBuilder("<input class='radio' type='radio' name='") ;
    input.append(CONSTRAINT).append("' value='").append(index).append("'") ;
    String value = this.<UIFormRadioBoxInput>getUIInput(CONSTRAINT).getValue();
    if(Integer.parseInt(value) == index) input.append(" checked") ;
    input.append(">") ;
    return input.toString() ;
  }
  
  private String getContainQueryString(String properties, String type, boolean isContain) {
    String value = getUIStringInput(type).getValue() ;
    String operator = getUIFormSelectBox(CONTAIN_OPERATOR).getValue() ;
    if(!isContain) operator = getUIFormSelectBox(NOT_CONTAIN_OPERATOR).getValue() ;
    if(value == null) value = "" ;
    else value = value.trim() ;
    String advanceQuery = "" ;
    if(properties.indexOf(",") > -1) {
      String[] array = properties.split(",") ;
      for(String property : array) {
        if(advanceQuery.length() > 0) advanceQuery = advanceQuery + operator ;
        advanceQuery = advanceQuery + getContainQuery(property, value, isContain) ;
      }
    } else {
      advanceQuery = getContainQuery(properties, value, isContain) ;
    }
    return advanceQuery ;
  }
  
  private String getDateTimeQueryString(String beforeDate, String afterDate, String type) {
    Calendar bfDate = getUIFormDateTimeInput(START_TIME).getCalendar() ;
    Calendar afDate = getUIFormDateTimeInput(END_TIME).getCalendar() ;
    if(type.equals(CREATED_DATE)) {
      virtualDateQuery_ = "(documents created before '"+beforeDate+"') AND (after '"+afterDate+"')" ;
      return "(jcr:created > '"+ISO8601.format(bfDate)+"') AND (jcr:created < '"+ISO8601.format(afDate)+"')" ;
    } else if(type.equals(MODIFIED_DATE)) {
      virtualDateQuery_ = "documents modified before '"+beforeDate+"' AND after '"+afterDate+"'" ;
      return "(jcr:lastModified > '"+ISO8601.format(bfDate)+"') AND (jcr:lastModified < '"+ISO8601.format(afDate)+"')" ;
    }
    return "" ;
  }
  
  private String getContainQuery(String property, String value, boolean isContain) {
    if(value.length() > 0) {
      if(isContain) return " contains(" + property.trim() + ", '"+ value.trim() + "')" ;
      return " not(contains(" + property.trim() + ", '"+ value.trim() + "'))" ;
    }
    return "";
  }
  
  private String getExactlyQueryString(String value, String properties) {
    String operator = getUIFormSelectBox(EXACTLY_OPERATOR).getValue() ;
    String advanceQuery = "" ;
    String[] arrProperties = {} ;
    if(properties.indexOf(",") > -1) arrProperties = properties.split(",") ;
    if(arrProperties.length > 0) {
      for(String pro : arrProperties) {
        if(advanceQuery.length() == 0) advanceQuery = "(" + pro + " = '" + value.trim() + "')" ;
        else advanceQuery = advanceQuery + " " + operator + " " + "("+ pro +" = '" + value.trim() + "')" ;
      }
    } else {
      advanceQuery = "" + properties + " = '" + value.trim() + "'" ;
    }
    return advanceQuery;
  }
  
  private String getNodeTypeQueryString(String nodeTypes) {
    String advanceQuery = "" ;
    String[] arrNodeTypes = {} ;
    if(nodeTypes.indexOf(",") > -1) arrNodeTypes = nodeTypes.split(",") ;
    if(arrNodeTypes.length > 0) {
      for(String nodeType : arrNodeTypes) {
        if(advanceQuery.length() == 0) advanceQuery = "(jcr:primaryType = '" + nodeType + "')" ;
        else advanceQuery = advanceQuery + " " + OR_OPERATION + " " + "(jcr:primaryType = '" + nodeType + "')" ;
      }
    } else {
      advanceQuery = "(jcr:primaryType = '" + nodeTypes + "')" ;
    }
    return advanceQuery;
  }
  
  private void addConstraint(Event event, int opt) throws Exception {
    UIApplication uiApp = getAncestorOfType(UIApplication.class) ;
    String advanceQuery = "" ;
    String properties ;
    virtualDateQuery_ = null ;
    UIECMSearch uiECMSearch = getParent() ;
    UISimpleSearch uiSimpleSearch = uiECMSearch.getChild(UISimpleSearch.class) ;
    switch (opt) {
      case 0:
        properties = getUIStringInput(PROPERTY1).getValue() ;
        if(properties == null || properties.length() < 1) {
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
        advanceQuery = getExactlyQueryString(value, properties) ;
        break;
      case 1:
        properties = getUIStringInput(PROPERTY2).getValue() ; 
        if(properties == null || properties.length() < 1) {
          uiApp.addMessage(new ApplicationMessage("UIConstraintsForm.msg.properties-required", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        advanceQuery = getContainQueryString(properties, CONTAIN, true) ;
        break;
      case 2:
        properties = getUIStringInput(PROPERTY3).getValue() ; 
        if(properties == null || properties.length() < 1) {
          uiApp.addMessage(new ApplicationMessage("UIConstraintsForm.msg.properties-required", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        advanceQuery = getContainQueryString(properties, NOT_CONTAIN, false) ;
        break;
      case 3:
        Calendar bfDate = getUIFormDateTimeInput(START_TIME).getCalendar() ;
        Calendar afDate = getUIFormDateTimeInput(END_TIME).getCalendar() ;
        if(bfDate.compareTo(afDate) == 1) {
          uiApp.addMessage(new ApplicationMessage("UIConstraintsForm.msg.date-invalid", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        String fromDate = getUIFormDateTimeInput(START_TIME).getValue() ;
        String toDate = getUIFormDateTimeInput(END_TIME).getValue() ;
        String type = getUIFormSelectBox(TIME_OPTION).getValue() ;
        advanceQuery = getDateTimeQueryString(fromDate, toDate, type) ;
        break ;
      case 4:
        properties = getUIStringInput(DOC_TYPE).getValue() ;
        if(properties == null || properties.length() < 1) {
          uiApp.addMessage(new ApplicationMessage("UIConstraintsForm.msg.properties-required", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        advanceQuery = getNodeTypeQueryString(properties) ;
        break;
      default:
        break;
    }
    uiSimpleSearch.updateAdvanceConstraint(advanceQuery, getUIFormSelectBox(OPERATOR).getValue(), virtualDateQuery_) ;
    uiECMSearch.removeChild(UIConstraintsForm.class) ;
    uiECMSearch.setRenderedChild(UISimpleSearch.class) ;
  }
  
  static public class SaveActionListener extends EventListener<UIConstraintsForm> {
    public void execute(Event<UIConstraintsForm> event) throws Exception {
      UIConstraintsForm uiForm = event.getSource();
      UISearchContainer uiContainer = uiForm.getAncestorOfType(UISearchContainer.class) ;
      int opt = Integer.parseInt(uiForm.<UIFormRadioBoxInput>getUIInput(CONSTRAINT).getValue()) ;
      switch (opt) {
        case 0:
          uiForm.addConstraint(event, 0) ;
          break;
        case 1:
          uiForm.addConstraint(event, 1) ;
          break;
        case 2:
          uiForm.addConstraint(event, 2) ;
          break;
        case 3:
          uiForm.addConstraint(event, 3);
          break;
        case 4:
          uiForm.addConstraint(event, 4) ;
          break;          
        default:
          break;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }
  
  static public class AddMetadataTypeActionListener extends EventListener<UIConstraintsForm> {
    public void execute(Event<UIConstraintsForm> event) throws Exception {
      UIConstraintsForm uiConstraintsForm = event.getSource();
      UISearchContainer uiContainer = uiConstraintsForm.getAncestorOfType(UISearchContainer.class) ;
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
      UIConstraintsForm uiConstraintsForm = event.getSource();
      UISearchContainer uiContainer = uiConstraintsForm.getAncestorOfType(UISearchContainer.class) ;
      uiContainer.initNodeTypePopup() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }
  
  static public class CompareExactlyActionListener extends EventListener<UIConstraintsForm> {
    public void execute(Event<UIConstraintsForm> event) throws Exception {
      UIConstraintsForm uiConstraintForm = event.getSource();
      String properties = uiConstraintForm.getUIStringInput(PROPERTY1).getValue() ;
      UIApplication uiApp = uiConstraintForm.getAncestorOfType(UIApplication.class) ;
      if(properties == null || properties.trim().length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIConstraintsForm.msg.properties-null", null, 
                                                 ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UISearchContainer uiContainer = uiConstraintForm.getAncestorOfType(UISearchContainer.class) ;
      UIPopupAction uiPopup = uiContainer.getChild(UIPopupAction.class);
      uiPopup.getChild(UIPopupWindow.class).setId("ExactlyFormPopup") ;
      uiPopup.activate(UICompareExactlyForm.class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIConstraintsForm> {
    public void execute(Event<UIConstraintsForm> event) throws Exception {
      UIECMSearch uiECMSearch = event.getSource().getParent() ;
      uiECMSearch.removeChild(UIConstraintsForm.class) ;
      uiECMSearch.setRenderedChild(UISimpleSearch.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiECMSearch.getParent()) ;
    }
  }
}