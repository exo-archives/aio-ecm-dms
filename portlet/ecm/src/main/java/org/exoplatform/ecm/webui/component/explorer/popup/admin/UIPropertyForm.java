/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ecm.webui.component.explorer.popup.admin;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.ecm.jcr.ECMNameValidator;
import org.exoplatform.ecm.jcr.JCRExceptionManager;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Created by The eXo Platform SARL 
 * Author : phamtuan
 * phamtuanchip@yahoo.de September 13, 2006 10:07:15 AM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl", 
    events = {
      @EventConfig(listeners = UIPropertyForm.SaveActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPropertyForm.ChangeTypeActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPropertyForm.AddActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPropertyForm.RemoveActionListener.class)
    }
)
public class UIPropertyForm extends UIForm {

  final static public String FIELD_PROPERTY = "name" ;
  final static public String FIELD_TYPE = "type" ;
  final static public String FIELD_VALUE = "value" ;
  final static public String FIELD_NAMESPACE = "namespace" ;

  private String repositoryName_ ;
  
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
    List<SelectItemOption<String>> nsOptions = new ArrayList<SelectItemOption<String>>() ;
    addUIFormInput(new UIFormSelectBox(FIELD_NAMESPACE,FIELD_NAMESPACE, nsOptions)) ;
    addUIFormInput(new UIFormStringInput(FIELD_PROPERTY, FIELD_PROPERTY, null).addValidator(ECMNameValidator.class)) ;
    UIFormSelectBox uiSelectBox = new UIFormSelectBox(FIELD_TYPE, FIELD_TYPE, options) ; 
    uiSelectBox.setOnChange("ChangeType") ;
    addUIFormInput(uiSelectBox) ;
    initMultiValuesField() ;    
    setActions(new String[]{"Save"}) ;
  }

  public List<SelectItemOption<String>> getNamespaces() throws Exception {
    List<SelectItemOption<String>> namespaceOptions = new ArrayList<SelectItemOption<String>>() ; 
    String[] namespaces = 
      getApplicationComponent(RepositoryService.class).getRepository(repositoryName_).getNamespaceRegistry().getPrefixes() ;
    for(String namespace : namespaces){
      namespaceOptions.add(new SelectItemOption<String>(namespace, namespace)) ;
    }
    return namespaceOptions;    
  }

  public void refresh() throws Exception {
    reset() ;
    getUIFormSelectBox(FIELD_TYPE).setValue(Integer.toString(PropertyType.STRING)) ;
    removeChildById(FIELD_VALUE) ;
    initMultiValuesField() ;
  }

  public void setRepositoryName(String repositoryName) { repositoryName_ = repositoryName ; }
  
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
    case 2:  return valueFactory.createValue(new ByteArrayInputStream((byte[])value)) ;
    case 3:  return valueFactory.createValue((Long.valueOf((String)value))) ;
    case 4:  return valueFactory.createValue((Double.valueOf((String)value))) ;
    case 5:  return valueFactory.createValue((GregorianCalendar)value) ;
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

  protected void lockForm(boolean isLock) {
    if(isLock) setActions(new String[]{}) ;
    else setActions(new String[]{"Save"}) ;
    getUIStringInput(FIELD_PROPERTY).setEditable(!isLock) ;
    getUIFormSelectBox(FIELD_TYPE).setEnable(!isLock) ;
    getUIFormSelectBox(FIELD_NAMESPACE).setEnable(!isLock) ;
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
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if(!uiExplorer.getCurrentNode().isCheckedOut()) {
        uiApp.addMessage(new ApplicationMessage("UIPropertyForm.msg.node-checkedin", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      NodeType nodeType = uiExplorer.getCurrentNode().getPrimaryNodeType() ;   
      if(nodeType.isNodeType(Utils.NT_UNSTRUCTURED)) {
        UIFormMultiValueInputSet multiValueInputSet = uiForm.getUIInput(FIELD_VALUE) ;
        ValueFactory valueFactory = uiExplorer.getCurrentNode().getSession().getValueFactory() ;
        String namespace = uiForm.getUIFormSelectBox(FIELD_NAMESPACE).getValue() ;
        String name = namespace + ":" + uiForm.getUIStringInput(FIELD_PROPERTY).getValue() ;
        if(uiExplorer.getCurrentNode().hasProperty(name)) {
          Object[] args = { name } ;
          uiApp.addMessage(new ApplicationMessage("UIPropertyForm.msg.propertyName-exist", args, 
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          UIPropertiesManager uiPropertiesManager = uiForm.getAncestorOfType(UIPropertiesManager.class) ;
          uiPropertiesManager.setRenderedChild(UIPropertyForm.class) ;
          return ;
        }
//      if ((name == null) || (name.trim().length() == 0)) {
//      uiApp.addMessage(new ApplicationMessage("UIPropertyForm.msg.name-invalid", null, 
//      ApplicationMessage.WARNING)) ;
//      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
//      UIPropertiesManager uiPropertiesManager = uiForm.getAncestorOfType(UIPropertiesManager.class) ;
//      uiPropertiesManager.setRenderedChild(UIPropertyForm.class) ;
//      return ;
//      } 
        int type = Integer.parseInt(uiForm.getUIFormSelectBox(FIELD_TYPE).getValue()) ;        
        NodeType nodetype = uiExplorer.getCurrentNode().getPrimaryNodeType() ;
        List valueList = new ArrayList() ;
        if(type == 6) {
          for(UIComponent child : multiValueInputSet.getChildren()) {
            UIFormCheckBoxInput checkbox = (UIFormCheckBoxInput)child ;
            valueList.add(checkbox.isChecked()) ;
          }
        } else if(type == 5) {
          for(UIComponent child : multiValueInputSet.getChildren()) {
            UIFormDateTimeInput dateInput = (UIFormDateTimeInput)child ;
            valueList.add(dateInput.getCalendar()) ;
          }
        } else if(type == 2) {
          for(UIComponent child : multiValueInputSet.getChildren()) {
            UIFormUploadInput binaryInput = (UIFormUploadInput)child ;
            if(binaryInput.getUploadData() != null) {
              byte[] content = binaryInput.getUploadData() ;    
              valueList.add(content) ;
            }
          }
        } else {
          valueList = multiValueInputSet.getValue() ;
        }
        if(valueList.size() == 0) {
          uiApp.addMessage(new ApplicationMessage("UIPropertyForm.msg.value-invalid", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          UIPropertiesManager uiPropertiesManager = uiForm.getAncestorOfType(UIPropertiesManager.class) ;
          uiPropertiesManager.setRenderedChild(UIPropertyForm.class) ;
          return ;
        } 
        Value[] values = {} ;
        try {
          values = uiForm.createValues(valueList, type, valueFactory) ;
        } catch(NullPointerException ne) {
          ne.printStackTrace() ;
          uiApp.addMessage(new ApplicationMessage("UIPropertyForm.msg.propertyValu-null", null, 
                                                  ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        } catch(NumberFormatException nume) {
          uiApp.addMessage(new ApplicationMessage("UIPropertyForm.msg.number-format-exception", null, 
                                                  ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        } catch(Exception e) {
          JCRExceptionManager.process(uiApp, e) ;
        }
        if(nodetype.canSetProperty(name, values)) {
          uiExplorer.getCurrentNode().setProperty(name, values) ;
          uiExplorer.getCurrentNode().save() ;
          uiExplorer.getSession().save() ;
        }
        uiForm.refresh() ;
        UIPropertiesManager uiPropertiesManager = uiForm.getAncestorOfType(UIPropertiesManager.class) ;
        uiPropertiesManager.setRenderedChild(UIPropertyTab.class) ;
        return ;
      }
      uiApp.addMessage(new ApplicationMessage("UIPropertyForm.msg.can-not-add", null)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      UIPropertiesManager uiPropertiesManager = uiForm.getAncestorOfType(UIPropertiesManager.class) ;
      uiPropertiesManager.setRenderedChild(UIPropertyForm.class) ;
      return ;
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
}