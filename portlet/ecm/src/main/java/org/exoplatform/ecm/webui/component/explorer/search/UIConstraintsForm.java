/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIPopupAction;
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
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Dec 26, 2006  
 * 4:29:08 PM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl",
    events = {
      @EventConfig(phase=Phase.DECODE, listeners = UIConstraintsForm.CancelActionListener.class),
      @EventConfig(listeners = UIConstraintsForm.SaveActionListener.class),
      @EventConfig(listeners = UIConstraintsForm.AddMetadataTypeActionListener.class)
    }    
)
public class UIConstraintsForm extends UIForm {

  public static final String CONTAINS = "contains" ; 
  public static final String NOT_CONTAINS = "notcontains" ;
  public static final String METADATA_PROPERTY = "metadataProperty" ;
  public static final String OPERATOR = "operator" ;
  final static public String AND_OPERATION = "and" ;
  final static public String OR_OPERATION = "or" ;
  
  public UIConstraintsForm() throws Exception {
    UIFormInputSetWithAction uiInputAct = new UIFormInputSetWithAction("constraints") ;
    uiInputAct.addUIFormInput(new UIFormStringInput(METADATA_PROPERTY, METADATA_PROPERTY, null)) ;
    uiInputAct.setActionInfo(METADATA_PROPERTY, new String[] {"AddMetadataType"}) ;
    addUIComponentInput(uiInputAct) ;
    setActions(new String[] {"Save", "Cancel"}) ;
    List<SelectItemOption<String>> typeOperation = new ArrayList<SelectItemOption<String>>() ;
    typeOperation.add(new SelectItemOption<String>(AND_OPERATION, AND_OPERATION));
    typeOperation.add(new SelectItemOption<String>(OR_OPERATION, OR_OPERATION));
    addUIFormInput(new UIFormSelectBox(OPERATOR, OPERATOR, typeOperation)) ;
    addUIFormInput(new UIFormStringInput(CONTAINS, CONTAINS, null)) ;
    addUIFormInput(new UIFormStringInput(NOT_CONTAINS, NOT_CONTAINS, null)) ;
  }

  static  public class CancelActionListener extends EventListener<UIConstraintsForm> {
    public void execute(Event<UIConstraintsForm> event) throws Exception {
      UIECMSearch uiECMSearch = event.getSource().getParent() ;
      uiECMSearch.removeChild(UIConstraintsForm.class) ;
      uiECMSearch.setRenderedChild(UISimpleSearch.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiECMSearch.getParent()) ;
    }
  }
  
  static public class SaveActionListener extends EventListener<UIConstraintsForm> {
    public void execute(Event<UIConstraintsForm> event) throws Exception {
      UIConstraintsForm uiForm = event.getSource();
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      String operator = uiForm.getUIFormSelectBox(OPERATOR).getValue() ;
      String properties = uiForm.getUIStringInput(METADATA_PROPERTY).getValue() ;
      if(properties == null || properties.length() < 1) {
        uiApp.addMessage(new ApplicationMessage("UIConstraintsForm.msg.properties-required", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String contains = uiForm.getUIStringInput(CONTAINS).getValue() ;
      if(contains == null) contains = "" ;
      else contains = contains.trim() ;
      String notContains = uiForm.getUIStringInput(NOT_CONTAINS).getValue() ;
      if (notContains == null ) notContains = "" ;
      else notContains = notContains.trim(); 
      String advanceQuery = "" ;
      if(properties.indexOf(",") > -1) {
        String[] array = properties.split(",") ;
        for(String property : array) {
          if(advanceQuery.length() > 0) advanceQuery = advanceQuery + operator ;
          advanceQuery = advanceQuery + getQuery(property, contains, notContains) ;
        }
      } else {
        advanceQuery = getQuery(properties, contains, notContains) ;
      }
      UIECMSearch uiECMSearch = uiForm.getParent() ;
      UISimpleSearch uiSimpleSearch = uiECMSearch.getChild(UISimpleSearch.class) ;
      uiSimpleSearch.updateAdvanceConstraint(advanceQuery) ;
      uiForm.reset() ;
      uiECMSearch.setRenderedChild(UISimpleSearch.class) ;
      uiECMSearch.removeChild(UIConstraintsForm.class) ;
    }
    
    private String getQuery(String property, String contains, String notContains) {
      String advanceQuery = "" ;
      if(contains.length() > 0 && notContains.length() > 0) {
        advanceQuery =" contains(" + property.trim() + ", '"+ contains + " -" + notContains + "') " ;
      } else if(contains.length() > 0){
        advanceQuery =" contains(" + property.trim() + ", '"+ contains + "') " ;
      } else if(notContains.length() > 0){
        advanceQuery = " not(contains(" + property.trim() + ", '"+ notContains + "')) " ;
      }
      return advanceQuery ;
    }
  }
  
  static  public class AddMetadataTypeActionListener extends EventListener<UIConstraintsForm> {
    public void execute(Event<UIConstraintsForm> event) throws Exception {
      UISearchContainer uiContainer = event.getSource().getAncestorOfType(UISearchContainer.class) ;
      uiContainer.initMetadataPopup() ;
      uiContainer.getChild(UIECMSearch.class).setRenderedChild(UIConstraintsForm.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }
}
