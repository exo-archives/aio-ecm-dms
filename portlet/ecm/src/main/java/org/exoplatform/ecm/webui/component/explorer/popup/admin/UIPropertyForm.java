/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.admin;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormCheckBoxInput;
import org.exoplatform.webui.component.UIFormDateTimeInput;
import org.exoplatform.webui.component.UIFormMultiValueInputSet;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIFormUploadInput;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL 
 * Author : phamtuan
 * phamtuanchip@yahoo.de September 13, 2006 10:07:15 AM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl", 
    events = {
      @EventConfig(listeners = UIPropertyForm.SaveActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPropertyForm.ChangeTypeActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPropertyForm.AddActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPropertyForm.RemoveActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPropertyForm.CancelActionListener.class)
    }
)
public class UIPropertyForm extends UIForm {

  final static public String FIELD_PROPERTY = "property" ;
  final static public String FIELD_TYPE = "type" ;
  final static public String FIELD_VALUE = "value" ;

  public UIPropertyForm() throws Exception {
    setMultiPart(true);
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(PropertyType.TYPENAME_STRING, 
                                             Integer.toString(PropertyType.STRING))) ;
    options.add(new SelectItemOption<String>(PropertyType.TYPENAME_BINARY, 
                                             Integer.toString(PropertyType.BINARY))) ;
    options.add(new SelectItemOption<String>(PropertyType.TYPENAME_BOOLEAN, 
                                             Integer.toString(PropertyType.BOOLEAN)));
    options.add(new SelectItemOption<String>(PropertyType.TYPENAME_DATE, 
                                             Integer.toString(PropertyType.DATE))) ;
    options.add(new SelectItemOption<String>(PropertyType.TYPENAME_DOUBLE, 
                                             Integer.toString(PropertyType.DOUBLE))) ;
    options.add(new SelectItemOption<String>(PropertyType.TYPENAME_LONG, 
                                             Integer.toString(PropertyType.LONG))) ;
    options.add(new SelectItemOption<String>(PropertyType.TYPENAME_NAME, 
                                             Integer.toString(PropertyType.NAME))) ;
    options.add(new SelectItemOption<String>(PropertyType.TYPENAME_PATH, 
                                             Integer.toString(PropertyType.PATH))) ;
    options.add(new SelectItemOption<String>(PropertyType.TYPENAME_REFERENCE, 
                                             Integer.toString(PropertyType.REFERENCE))) ;
    options.add(new SelectItemOption<String>(PropertyType.TYPENAME_UNDEFINED, 
                                             Integer.toString(PropertyType.UNDEFINED))) ;
    addUIFormInput(new UIFormStringInput(FIELD_PROPERTY, FIELD_PROPERTY, null)) ;
    UIFormSelectBox uiSelectBox = new UIFormSelectBox(FIELD_TYPE, FIELD_TYPE, options) ; 
    uiSelectBox.setOnChange("ChangeType") ;
    addUIFormInput(uiSelectBox) ;
    initMultiValuesField() ;    
    setActions(new String[]{"Save", "Cancel"}) ;
  }

  private void refresh() throws Exception {
    reset() ;
    getUIFormSelectBox(FIELD_TYPE).setValue(Integer.toString(PropertyType.STRING)) ;
    removeChildById(FIELD_VALUE) ;
    initMultiValuesField() ;
  }

  private void initMultiValuesField() throws Exception{
    UIFormMultiValueInputSet uiFormMValue = 
      createUIComponent(UIFormMultiValueInputSet.class, null, null) ;
    uiFormMValue.setId(FIELD_VALUE) ;
    uiFormMValue.setName(FIELD_VALUE) ;
    uiFormMValue.setType(UIFormStringInput.class) ;
    addUIFormInput(uiFormMValue) ;
  }

  private Value createValue(Object value, int type, ValueFactory valueFactory) throws Exception {
    switch (type) {
    case 2:  {
      UIFormUploadInput inputValue = (UIFormUploadInput)value ;
      byte[] content = inputValue.getUploadData() ;
      return valueFactory.createValue(new ByteArrayInputStream(content)) ;
    }
    case 3:  return valueFactory.createValue((Long.valueOf((String)value))) ;
    case 4:  return valueFactory.createValue((Double.valueOf((String)value))) ;
    case 5:  return valueFactory.createValue((Calendar)value) ;
    case 6:  return valueFactory.createValue(Boolean.parseBoolean(value.toString())) ;
    default: return valueFactory.createValue((String)value) ; 
    }
  }

  private Value[] createValues(List valueList, int type, ValueFactory valueFactory) throws Exception {
    Value[] values = new Value[valueList.size()] ;
    for(int i = 0; i < valueList.size(); i++) {
      values[i] = createValue(valueList.get(i), type, valueFactory) ;     
    }
    return values ;
  }
  
  static public class ChangeTypeActionListener extends EventListener {
    public void execute(Event event) throws Exception {
      UIPropertyForm uiForm = (UIPropertyForm) event.getSource() ;
      int type = Integer.parseInt(uiForm.getUIFormSelectBox(FIELD_TYPE).getValue()) ;
      uiForm.removeChildById(FIELD_VALUE) ;
      UIFormMultiValueInputSet uiFormMultiValue = 
        uiForm.createUIComponent(UIFormMultiValueInputSet.class, null, null) ;
      uiFormMultiValue.setId(FIELD_VALUE) ;
      uiFormMultiValue.setName(FIELD_VALUE) ;
      if(PropertyType.BINARY == type) {        
        uiFormMultiValue.setType(UIFormUploadInput.class) ;        
      } else if(PropertyType.BOOLEAN == type) {
        uiFormMultiValue.setType(UIFormCheckBoxInput.class) ;
      } else if(PropertyType.DATE == type) {
        uiFormMultiValue.setType(UIFormDateTimeInput.class) ;
      } else {
        uiFormMultiValue.setType(UIFormStringInput.class) ;
      }
      uiForm.addUIFormInput(uiFormMultiValue) ;
      UIPropertiesManager uiPropertiesManager = uiForm.getAncestorOfType(UIPropertiesManager.class) ;
      uiPropertiesManager.setRenderedChild(UIPropertyForm.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
  
  @SuppressWarnings("unchecked")
  static public class SaveActionListener extends EventListener<UIPropertyForm> {
    public void execute(Event<UIPropertyForm> event) throws Exception {
      UIPropertyForm uiForm = event.getSource() ;
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
      NodeType nodeType = uiExplorer.getCurrentNode().getPrimaryNodeType() ;   
      if(nodeType.isNodeType("nt:unstructured")) {
        UIFormMultiValueInputSet multiValueInputSet = uiForm.getUIInput(FIELD_VALUE) ;
        ValueFactory valueFactory = uiExplorer.getCurrentNode().getSession().getValueFactory() ;
        String name = uiForm.getUIStringInput(FIELD_PROPERTY).getValue() ;
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        if ((name == null) || (name.length() == 0)) {
          uiApp.addMessage(new ApplicationMessage("UIPropertyForm.msg.name-invalid", null,
                                                  ApplicationMessage.WARNING)) ;
          UIPropertiesManager uiPropertiesManager = uiForm.getAncestorOfType(UIPropertiesManager.class) ;
          uiPropertiesManager.setRenderedChild(UIPropertyForm.class) ;
          return ;
        } 
        int type = Integer.parseInt(uiForm.getUIFormSelectBox(FIELD_TYPE).getValue()) ;        
        NodeType nodetype = uiExplorer.getCurrentNode().getPrimaryNodeType() ;
        List valueList = new ArrayList() ;
        if(type == 6) {
          for(UIComponent child : multiValueInputSet.getChildren()) {
            UIFormCheckBoxInput checkbox = (UIFormCheckBoxInput)child ;
            valueList.add(checkbox.isChecked()) ;
          }
        } else if(type == 5){
          for(UIComponent child : multiValueInputSet.getChildren()) {
            UIFormDateTimeInput dateInput = (UIFormDateTimeInput)child ;
            valueList.add(dateInput.getCalendar()) ;
          }
        } else valueList = multiValueInputSet.getValue() ;
        if(valueList.size() == 0) {
          uiApp.addMessage(new ApplicationMessage("UIPropertyForm.msg.value-invalid", null,
                                                  ApplicationMessage.ERROR)) ;
          UIPropertiesManager uiPropertiesManager = uiForm.getAncestorOfType(UIPropertiesManager.class) ;
          uiPropertiesManager.setRenderedChild(UIPropertyForm.class) ;
          return ;
        } 
        Value[] values = uiForm.createValues(valueList, type, valueFactory) ;
        if(nodetype.canSetProperty(name, values)) {
          uiExplorer.getCurrentNode().setProperty(name, values) ;
        }
        uiForm.refresh() ;
        UIPropertiesManager uiPropertiesManager = uiForm.getAncestorOfType(UIPropertiesManager.class) ;
        uiPropertiesManager.setRenderedChild(UIPropertyTab.class) ;
      }
    }
  }
 
  static public class AddActionListener extends EventListener<UIPropertyForm> {
    public void execute(Event<UIPropertyForm> event) throws Exception {
      UIPropertyForm uiForm = event.getSource() ;
      UIPropertiesManager uiPropertiesManager = uiForm.getAncestorOfType(UIPropertiesManager.class) ;
      uiPropertiesManager.setRenderedChild(UIPropertyForm.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
  
  static public class RemoveActionListener extends EventListener<UIPropertyForm> {
    public void execute(Event<UIPropertyForm> event) throws Exception {
      UIPropertyForm uiForm = event.getSource() ;
      UIPropertiesManager uiPropertiesManager = uiForm.getAncestorOfType(UIPropertiesManager.class) ;
      uiPropertiesManager.setRenderedChild(UIPropertyForm.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
  
  static public class CancelActionListener extends EventListener<UIPropertyForm> {
    public void execute(Event<UIPropertyForm> event) throws Exception {
      UIPropertyForm uiForm = event.getSource() ;
      UIPropertiesManager uiProManager = uiForm.getAncestorOfType(UIPropertiesManager.class) ;
      uiProManager.setRenderedChild(UIPropertyTab.class) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiProManager) ;
    }
  }
}