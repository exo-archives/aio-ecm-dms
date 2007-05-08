/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.ecm.webui.component.explorer.UIPopupAction;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormDateTimeInput;
import org.exoplatform.webui.component.UIFormRadioBoxInput;
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
      @EventConfig(listeners = UIConstraintsForm.SelectPropertyActionListener.class),
      @EventConfig(listeners = UIConstraintsForm.CompareExactlyActionListener.class),
      @EventConfig(listeners = UIConstraintsForm.AddMetadataTypeActionListener.class)
    }    
)
public class UIConstraintsForm extends UIForm {

  public static final String OPERATOR = "operator" ;
  public static final String TIME_OPTION = "timeOpt" ;
  public static final String CONSTRAINT = "constraint" ;
  public static final String PROPERTY1 = "property1" ; 
  public static final String PROPERTY2 = "property2" ; 
  public static final String PROPERTY3 = "property3" ; 
  public static final String CONTAIN_EXACTLY = "containExactly" ; 
  public static final String CONTAIN = "contain" ;
  public static final String NOT_CONTAIN = "notContain" ;
  public static final String START_TIME = "startTime" ;
  public static final String END_TIME = "endTime" ;
  public static final String DOC_NAME = "docName" ;
  public static final String DOC_TYPE = "docType" ;
  
  public static final String METADATA_PROPERTY = "metadataProperty" ;
  final static public String AND_OPERATION = "and" ;
  final static public String OR_OPERATION = "or" ;
  final static public String[] CONSTRAINT_LABEL = {"Property", "Property", "Property", "", "Document Name", "Document Type"} ;
  
  public UIConstraintsForm() throws Exception {
    setActions(new String[] {"Save", "Cancel"}) ;
    addUIFormInput(new UIFormRadioBoxInput(CONSTRAINT, "0")) ;

    List<SelectItemOption<String>> typeOperation = new ArrayList<SelectItemOption<String>>() ;
    typeOperation.add(new SelectItemOption<String>(AND_OPERATION, AND_OPERATION));
    typeOperation.add(new SelectItemOption<String>(OR_OPERATION, OR_OPERATION));
    addUIFormInput(new UIFormSelectBox(OPERATOR, OPERATOR, typeOperation)) ;

    addUIFormInput(new UIFormStringInput(PROPERTY1, PROPERTY1, null)) ;
    addUIFormInput(new UIFormStringInput(CONTAIN_EXACTLY, CONTAIN_EXACTLY, null)) ;
    addUIFormInput(new UIFormStringInput(PROPERTY2, PROPERTY2, null)) ;
    addUIFormInput(new UIFormStringInput(CONTAIN, CONTAIN, null)) ;
    addUIFormInput(new UIFormStringInput(PROPERTY3, PROPERTY3, null)) ;
    addUIFormInput(new UIFormStringInput(NOT_CONTAIN, NOT_CONTAIN, null)) ;
    
    List<SelectItemOption<String>> dateOperation = new ArrayList<SelectItemOption<String>>() ;
    dateOperation.add(new SelectItemOption<String>("Created", "Created"));
    dateOperation.add(new SelectItemOption<String>("Modified", "Modified"));
    addUIFormInput(new UIFormSelectBox(TIME_OPTION, TIME_OPTION, dateOperation)) ;
    
    addUIFormInput(new UIFormDateTimeInput(START_TIME, START_TIME, null)) ;
    addUIFormInput(new UIFormDateTimeInput(END_TIME, END_TIME, null)) ;
    addUIFormInput(new UIFormStringInput(DOC_NAME, DOC_NAME, null)) ;
    addUIFormInput(new UIFormStringInput(DOC_TYPE, DOC_TYPE, null)) ;
    
//    UIFormInputSetWithAction uiInputAct = new UIFormInputSetWithAction("constraints") ;
//    uiInputAct.addUIFormInput(new UIFormStringInput(METADATA_PROPERTY, METADATA_PROPERTY, null)) ;
//    uiInputAct.addUIFormInput(new UIFormStringInput(METADATA_PROPERTY, METADATA_PROPERTY, null)) ;
//    
//    uiInputAct.setActionInfo(METADATA_PROPERTY, new String[] {"AddMetadataType"}) ;
//    addUIComponentInput(uiInputAct) ;
//    addUIFormInput(new UIFormStringInput(CONTAINS, CONTAINS, null)) ;
//    addUIFormInput(new UIFormStringInput(NOT_CONTAINS, NOT_CONTAINS, null)) ;
  }

  public String renderConstraintRadioBox(int index) {
    StringBuilder input = new StringBuilder("<input class='radio' type='radio' name='") ;
    input.append(CONSTRAINT).append("' value='").append(index).append("'") ;
    String value = this.<UIFormRadioBoxInput>getUIInput(CONSTRAINT).getValue();
    if(Integer.parseInt(value) == index) input.append(" checked") ;
    input.append(">").append(CONSTRAINT_LABEL[index]) ;
    return input.toString() ;
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
      int opt = Integer.parseInt(uiForm.<UIFormRadioBoxInput>getUIInput(CONSTRAINT).getValue()) ;
      switch (opt) {
        case 0:
          break;
        case 1:
          break;
        case 2:
          break;
        case 3:
          break;
        default:
          break;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource()) ;
    }
  }
  
  static public class AddMetadataTypeActionListener extends EventListener<UIConstraintsForm> {
    public void execute(Event<UIConstraintsForm> event) throws Exception {
      UIConstraintsForm test = event.getSource();
      UISearchContainer uiContainer = test.getAncestorOfType(UISearchContainer.class) ;
      UIPopupAction uiPopup = uiContainer.getChild(UIPopupAction.class);
      uiPopup.activate(UISelectPropertyForm.class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }
  
  static public class SelectPropertyActionListener extends EventListener<UIConstraintsForm> {
    public void execute(Event<UIConstraintsForm> event) throws Exception {
      UIConstraintsForm test = event.getSource();
      UISearchContainer uiContainer = test.getAncestorOfType(UISearchContainer.class) ;
      UIPopupAction uiPopup = uiContainer.getChild(UIPopupAction.class);
      uiPopup.activate(UISelectPropertyForm.class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }
  
  static public class CompareExactlyActionListener extends EventListener<UIConstraintsForm> {
    public void execute(Event<UIConstraintsForm> event) throws Exception {
      UIConstraintsForm test = event.getSource();
      UISearchContainer uiContainer = test.getAncestorOfType(UISearchContainer.class) ;
      UIPopupAction uiPopup = uiContainer.getChild(UIPopupAction.class);
      uiPopup.activate(UICompareExactlyForm.class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }
}
