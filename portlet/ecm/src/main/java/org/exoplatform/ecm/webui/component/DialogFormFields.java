/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component;

import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.faces.core.component.UIStringInput;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.scripts.CmsScript;
import org.exoplatform.services.cms.scripts.ScriptService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormDateTimeInput;
import org.exoplatform.webui.component.UIFormMultiValueInputSet;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIFormTextAreaInput;
import org.exoplatform.webui.component.UIFormUploadInput;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.component.validator.EmailAddressValidator;
import org.exoplatform.webui.component.validator.EmptyFieldValidator;
import org.exoplatform.webui.component.validator.NameValidator;
import org.exoplatform.webui.component.validator.NumberFormatValidator;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : nqhungvn
 *          nguyenkequanghung@yahoo.com
 * nov 6, 2006
 * 
 */

@ComponentConfig(
    events = {
        @EventConfig(listeners = DialogFormFields.SaveActionListener.class),
        @EventConfig(listeners = DialogFormFields.OnchangeActionListener.class)
    }
)
@SuppressWarnings("unused")
public class DialogFormFields extends UIForm {
  
  public Map<String, Map> components = new HashMap<String, Map>();
  public Map<String, String> propertiesName_ = new HashMap<String, String>() ;
  private Node node_ = null;
  private Node propertyNode_ = null ;
  private boolean isNotEditNode_ = false ;
  private boolean isNTFile_ = false ;
  private List<String> scriptInterceptor_ = new ArrayList<String>() ; 
  private static final String SEPARATOR = "=";
  private static final String JCR_PATH = "jcrPath" + SEPARATOR;
  private static final String EDITABLE = "editable" + SEPARATOR;
  private static final String ONCHANGE = "onchange" + SEPARATOR;
  private static final String OPTIONS = "options" + SEPARATOR;  
  private static final String TYPE = "type" + SEPARATOR ;
  private static final String VISIBLE = "visible" + SEPARATOR;
  private static final String NODETYPE = "nodetype" + SEPARATOR;
  private static final String MIXINTYPE = "mixintype" + SEPARATOR;
  private static final String VALIDATE = "validate" + SEPARATOR;
  private static final String SELECTOR_ACTION = "selectorAction" + SEPARATOR;
  private static final String SELECTOR_CLASS = "selectorClass" + SEPARATOR;
  private static final String SELECTOR_ICON = "selectorIcon" + SEPARATOR;
  private static final String SELECTOR_PARAMS = "selectorParams" + SEPARATOR;
  private static final String SCRIPT = "script" + SEPARATOR;
  private static final String SCRIPT_PARAMS = "scriptParams" + SEPARATOR;
  private static final String MULTI_VALUES = "multiValues" + SEPARATOR;
  
  public static final  String[]  ACTIONS = {"Save", "Cancel"};
  
  protected Map<String, Object> properties = new HashMap<String, Object>();
  
  public DialogFormFields() throws Exception {}

  public void setNode(Node node) throws Exception { node_ = node ; }
  public Node getNode() { return node_ ; }
  
  public void setPropertyNode(Node node) throws Exception { propertyNode_ = node ; }
  
  public void setInputProperty(String name, Object value) { properties.put(name, value) ; }
  public Object getInputProperty(String name) { return properties.get(name) ; }
  
  public Map<String, Object> getInputProperties() { return properties ; }
  
  public void resetProperties() { properties.clear() ; }
  
  public void setIsNotEditNode(boolean isNotEditNode) { isNotEditNode_ = isNotEditNode ; }
  public void setIsNTFile(boolean isNTFile) { isNTFile_ = isNTFile ; }
  
  public String getPropertyName(String jcrPath) { 
    return jcrPath.substring(jcrPath.lastIndexOf("/") + 1) ; 
  }
  
  private String getPropertyValue(String jcrPath) throws Exception {
    if(jcrPath.equals("/node") && node_ != null) return node_.getName() ;
    if(propertyNode_.hasProperty(getPropertyName(jcrPath))) {
      int valueType = propertyNode_.getProperty(getPropertyName(jcrPath)).getType() ;
      switch(valueType) {
      case 1: //String 
        return propertyNode_.getProperty(getPropertyName(jcrPath)).getString() ;
      case 2:
        return "" ;
      case 3: // Long    
        return Long.toString(propertyNode_.getProperty(getPropertyName(jcrPath)).getLong()) ;
      case 4: // Double
        return Double.toString(propertyNode_.getProperty(getPropertyName(jcrPath)).getDouble()) ;
      case 5: //Date
        return propertyNode_.getProperty(getPropertyName(jcrPath)).getDate().getTime().toString() ;
      case 6: //Boolean
        return Boolean.toString(propertyNode_.getProperty(getPropertyName(jcrPath)).getBoolean()) ;
      case 7: //Name
        return propertyNode_.getProperty(getPropertyName(jcrPath)).getName() ;
      case 8: //Path
      case 9: //References
      case 0: //Undifine
        return "" ;
      }
    }
    return "" ;
  }
  
  public void addActionField(String name, String[] arguments) throws Exception {
    String editable = "true";
    String defaultValue = "";
    String jcrPath = null;
    String selectorAction = null;
    String selectorClass = null;
    String[] selectorParams = null;
    String selectorIcon = null ;
    String multiValues = null ;
    String validateType = null ;
    for(int i = 0; i < arguments.length; i++) {
      String argument = arguments[i];
      if (argument.startsWith(JCR_PATH)) {
        jcrPath = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(EDITABLE)) {
        editable = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(SELECTOR_ACTION)) {
        selectorAction = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(SELECTOR_CLASS)) {
        selectorClass = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(MULTI_VALUES)) {
        multiValues = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(SELECTOR_ICON)) {
        selectorIcon = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(SELECTOR_PARAMS)) {
        String params = argument.substring(argument.indexOf(SEPARATOR) + 1);
        selectorParams = StringUtils.split(params, ",");
      } else if (argument.startsWith(VALIDATE)) {
        validateType = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else {
        defaultValue = argument;
      }
    }
    if(selectorClass != null) { 
      Map<String, String> fieldPropertiesMap = new HashMap<String, String>() ;
      fieldPropertiesMap.put("selectorClass", selectorClass) ;
      fieldPropertiesMap.put("returnField", name) ;
      fieldPropertiesMap.put("selectorIcon", selectorIcon) ;
      components.put(name, fieldPropertiesMap) ;
    }
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    setInputProperty(name, inputProperty) ;
    if(multiValues != null && multiValues.equals("true")) {
      UIFormMultiValueInputSet uiMulti = createUIComponent(UIFormMultiValueInputSet.class, null, null) ;
      uiMulti.setId(name) ;
      uiMulti.setName(name) ;
      uiMulti.setType(UIFormStringInput.class) ;
      addUIFormInput(uiMulti) ;
      renderField(name) ;
      return ;
    }
    UIFormStringInput uiInput = findComponentById(name) ;
    if(uiInput == null) {
      uiInput = new UIFormStringInput(name, name, defaultValue) ;
      if(validateType != null) {
        if(validateType.equals("name")) {
          uiInput.addValidator(NameValidator.class) ;
        } else if (validateType.equals("email")){
          uiInput.addValidator(EmailAddressValidator.class) ;
        } else if (validateType.equals("number")) {
          uiInput.addValidator(NumberFormatValidator.class) ;
        } else if (validateType.equals("empty")){
          uiInput.addValidator(EmptyFieldValidator.class) ;
        }
      }    
      addUIFormInput(uiInput) ;
    }
    if(editable.equals("false")) uiInput.setEditable(false) ;
    else uiInput.setEditable(true) ;
    propertiesName_.put(name, getPropertyName(jcrPath)) ;
    if(node_ != null) {
      if(jcrPath.equals("/node") && (editable.equals("false") || editable.equals("if-null"))) {
        uiInput.setValue(node_.getName()) ;
        uiInput.setEditable(false) ;
      } else if(node_.hasProperty(getPropertyName(jcrPath))) {
        uiInput.setValue(node_.getProperty(getPropertyName(jcrPath)).getValue().getString()) ;
      }
    }
    if(isNotEditNode_) {
      if(propertyNode_ != null) {
        uiInput.setValue(getPropertyValue(jcrPath)) ;
      } else if(propertyNode_ == null && jcrPath.equals("/node") && node_ != null) {
        uiInput.setValue(node_.getName()) ;
      } else {
        uiInput.setValue(null) ;
      }
    }
    renderField(name) ;
  }
  
  public void addTextField(String name, String[] arguments) throws Exception {
    String editable = "true";
    String type = "text" ;
    String defaultValue = "";
    String jcrPath = null;
    String mixintype = null;
    String multiValues = null ;
    String validateType = null ;
    for(int i = 0; i < arguments.length; i++) {
      String argument = arguments[i];
      if (argument.startsWith(JCR_PATH)) {
        jcrPath = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(EDITABLE)) {
        editable = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(TYPE)){
        type = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(MIXINTYPE)) {
        mixintype = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(MULTI_VALUES)) {
        multiValues = argument.substring(argument.indexOf(SEPARATOR) + 1);        
      } else if (argument.startsWith(VALIDATE)) {
        validateType = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else {
        defaultValue = argument;
      }
    }
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    setInputProperty(name, inputProperty) ;
    String propertyName = jcrPath.substring(jcrPath.lastIndexOf("/") + 1) ;
    if(mixintype != null) inputProperty.setMixintype(mixintype) ;
    properties.put(name, inputProperty) ;
    propertiesName_.put(name, propertyName) ;
    if(multiValues != null && multiValues.equals("true")) {
      UIFormMultiValueInputSet uiMulti ;
      if(node_ == null && propertyNode_ == null) {
        uiMulti = findComponentById(name) ;
        if(uiMulti == null) {
          uiMulti = createUIComponent(UIFormMultiValueInputSet.class, null, null) ;
          uiMulti.setId(name) ;
          uiMulti.setName(name) ;
          uiMulti.setType(UIFormStringInput.class) ;
          addUIFormInput(uiMulti) ;
        }
      } else {
        uiMulti = createUIComponent(UIFormMultiValueInputSet.class, null, null) ;
        uiMulti.setId(name) ;
        uiMulti.setName(name) ;
        uiMulti.setType(UIFormStringInput.class) ;
        addUIFormInput(uiMulti) ;
      }
      List<String> valueList = new ArrayList<String>() ;
      if(propertyNode_ != null) {
        if(propertyNode_.hasProperty(getPropertyName(jcrPath))) {
          Value[] values = propertyNode_.getProperty(getPropertyName(jcrPath)).getValues() ;
          for(Value value : values) {
            valueList.add(value.getString()) ;
          }
          uiMulti.setValue(valueList) ;
        }
      } else if(node_ != null) {
        if(node_.isNodeType("nt:file")) {
          Value[] values = node_.getNode("jcr:content").getProperty(getPropertyName(jcrPath)).getValues() ;
          for(Value value : values) {
            valueList.add(value.getString()) ;
          }
          uiMulti.setValue(valueList) ;
        }
      }
      renderField(name) ;
      return ;
    } 
    UIFormStringInput uiInput = findComponentById(name) ;
    if(uiInput == null) {
      uiInput = new UIFormStringInput(name, name, defaultValue) ;
      if(validateType != null) {
        if(validateType.equals("name")) {
          uiInput.addValidator(NameValidator.class) ;
        } else if (validateType.equals("email")){
          uiInput.addValidator(EmailAddressValidator.class) ;
        } else if (validateType.equals("number")) {
          uiInput.addValidator(NumberFormatValidator.class) ;
        } else if (validateType.equals("empty")){
          uiInput.addValidator(EmptyFieldValidator.class) ;
        }
      }     
      addUIFormInput(uiInput) ;
    }
    if(type.equals("password")) uiInput.setType((short)UIStringInput.PASSWORD) ;
    if(editable.equals("false")) uiInput.setEditable(false) ;
    else uiInput.setEditable(true) ;
    if(node_ != null) {
      if(jcrPath.equals("/node") && (editable.equals("false") || editable.equals("if-null"))) {
        if(node_.getParent().getName().equals("languages")) {
          uiInput.setValue(node_.getParent().getParent().getName()) ;
        } else {
          uiInput.setValue(node_.getName()) ;
        }
        uiInput.setEditable(false) ;
      } else if(node_.hasProperty(propertyName)) {
        uiInput.setValue(node_.getProperty(propertyName).getValue().getString()) ;
      }
    }
    if(isNotEditNode_) {
      if(propertyNode_ != null) {
        uiInput.setValue(getPropertyValue(jcrPath)) ;
      } else if(propertyNode_ == null && jcrPath.equals("/node") && node_ != null) {
        uiInput.setValue(node_.getName()) ;
      } else {
        uiInput.setValue(null) ;
      }
    }
    renderField(name) ;
  }
  
  public void addTextAreaField(String name, String[] arguments) throws Exception {
    String editable = "true";
    String defaultValue = "";
    String jcrPath = null;
    String selectorAction = null;
    String selectorClass = null;
    String multiValues = null ;
    for(int i = 0; i < arguments.length; i++) {
      String argument = arguments[i];
      if (argument.startsWith(JCR_PATH)) {
        jcrPath = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(EDITABLE)) {
        editable = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(SELECTOR_ACTION)) {
        selectorAction = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(MULTI_VALUES)) {
        multiValues = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(SELECTOR_CLASS)) {
        selectorClass = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else {
        defaultValue = argument;
      }
    }
    if(selectorClass != null) { 
      Map<String, String> fieldPropertiesMap = new HashMap<String, String>() ;
      fieldPropertiesMap.put("selectorClass", selectorClass) ;
      fieldPropertiesMap.put("returnField", name) ;
      components.put(name, fieldPropertiesMap) ;
    }
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    setInputProperty(name, inputProperty) ;
    if(multiValues != null && multiValues.equals("true")) {
      UIFormMultiValueInputSet uiMulti = createUIComponent(UIFormMultiValueInputSet.class, null, null) ;
      uiMulti.setId(name) ;
      uiMulti.setName(name) ;
      uiMulti.setType(UIFormTextAreaInput.class) ;
      addUIFormInput(uiMulti) ;
      renderField(name) ;
      return ;
    }
    UIFormTextAreaInput uiTextArea = findComponentById(name) ;
    if(uiTextArea == null) {
      uiTextArea = new UIFormTextAreaInput(name, name, defaultValue) ;
      addUIFormInput(uiTextArea) ;
    }
    if(editable.equals("false")) uiTextArea.setEditable(false) ;
    else uiTextArea.setEditable(false) ;
    propertiesName_.put(name, getPropertyName(jcrPath)) ;
    if(node_ != null) {
      String value = "";
      if(node_.hasProperty(getPropertyName(jcrPath))) {
        value = node_.getProperty(getPropertyName(jcrPath)).getValue().getString() ;
      } else if(node_.isNodeType("nt:file")) {
        Node jcrContentNode = node_.getNode("jcr:content") ;
        if(jcrContentNode.hasProperty(getPropertyName(jcrPath))) {
          if(jcrContentNode.getProperty(getPropertyName(jcrPath)).getDefinition().isMultiple()) {
            Value[] values = jcrContentNode.getProperty(getPropertyName(jcrPath)).getValues() ;
            for(Value v : values) {
              value = value + v.getString() ;
            }
          } else {
            value = jcrContentNode.getProperty(getPropertyName(jcrPath)).getValue().getString() ;
          }
        }
      }
      uiTextArea.setValue(value) ;
    } 
    if(isNotEditNode_) {
      if(propertyNode_ != null) {
        uiTextArea.setValue(getPropertyValue(jcrPath)) ;
      } else if(propertyNode_ == null && jcrPath.equals("/node") && node_ != null) {
        uiTextArea.setValue(node_.getName()) ;
      } else {
        uiTextArea.setValue(null) ;
      }
    }
    renderField(name) ;
  }
  
  public void addWYSIWYGField(String name, String[] arguments) throws Exception {
    String options = null ;
    String defaultValue = "";
    String jcrPath = null;
    String multiValues = null ;
    for(int i = 0; i < arguments.length; i++) {
      String argument = arguments[i];
      if (argument.startsWith(JCR_PATH)) {
        jcrPath = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(OPTIONS)) {
        options = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(MULTI_VALUES)) {
        multiValues = argument.substring(argument.indexOf(SEPARATOR) + 1);        
      } else{
        defaultValue = argument;
      }
    }
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    setInputProperty(name, inputProperty) ;
    if(multiValues != null && multiValues.equals("true")) {
      UIFormMultiValueInputSet uiMulti = createUIComponent(UIFormMultiValueInputSet.class, null, null) ;
      uiMulti.setId(name) ;
      uiMulti.setName(name) ;
      uiMulti.setType(UIFormWYSIWYGInput.class) ;
      addUIFormInput(uiMulti) ;
      renderField(name) ;
      return ;
    }
    UIFormWYSIWYGInput wysiwyg = findComponentById(name) ;
    if(wysiwyg == null) {
      wysiwyg = new UIFormWYSIWYGInput(name, name, defaultValue, options) ;
      addUIFormInput(wysiwyg) ;
    }
    propertiesName_.put(name, getPropertyName(jcrPath)) ;
    if(node_ != null && (node_.isNodeType("nt:file") || isNTFile_)) {
      Node jcrContentNode = node_.getNode("jcr:content") ;
      wysiwyg.setValue(jcrContentNode.getProperty("jcr:data").getValue().getString()) ;
    } else {
      if(node_ != null && node_.hasProperty(getPropertyName(jcrPath))) {
        wysiwyg.setValue(node_.getProperty(getPropertyName(jcrPath)).getValue().getString()) ;
      }
    }
    if(isNotEditNode_) {
      if(propertyNode_ != null) {
        wysiwyg.setValue(getPropertyValue(jcrPath)) ;
      } else if(propertyNode_ == null && jcrPath.equals("/node") && node_ != null) {
        wysiwyg.setValue(node_.getName()) ;
      } else {
        wysiwyg.setValue(null) ;
      }
    }
    renderField(name) ;
  }
  
  public void addSelectBoxField(String name, String[] arguments) throws Exception {
    String jcrPath = null;
    String editable = "true";
    String onchange = "false" ;
    String defaultValue = "" ;
    String options = null;
    String script = null;
    String[] scriptParams = null;
    String multiValues = null ;
    for(int i = 0; i < arguments.length; i++) {
      String argument = arguments[i];
      if (argument.startsWith(JCR_PATH)) {
        jcrPath = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(EDITABLE)) {
        editable = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(OPTIONS)) {
        options = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(SCRIPT)) {
        script = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(SCRIPT_PARAMS)) {
        String params = argument.substring(argument.indexOf(SEPARATOR) + 1);
        scriptParams = StringUtils.split(params, ","); 
      } else if (argument.startsWith(MULTI_VALUES)) {
        multiValues = argument.substring(argument.indexOf(SEPARATOR) + 1);        
      } else if(argument.startsWith(ONCHANGE)) {
        onchange = argument.substring(argument.indexOf(SEPARATOR) + 1) ;
      } else {
        defaultValue = argument;
      } 
    }
    if(multiValues != null && multiValues.equals("true")) {
      UIFormMultiValueInputSet uiMulti = createUIComponent(UIFormMultiValueInputSet.class, null, null) ;
      uiMulti.setId(name) ;
      uiMulti.setName(name) ;
      uiMulti.setType(UIFormSelectBox.class) ;
      addUIFormInput(uiMulti) ;
      renderField(name) ;
      return ;
    }
    List<SelectItemOption<String>> optionsList = null;
    UIFormSelectBox uiSelectBox = findComponentById(name) ;
    if(uiSelectBox == null) {
      uiSelectBox = new UIFormSelectBox(name, name, null);
      addUIFormInput(uiSelectBox) ;
    }
    if (script != null) {
      executeScript(script, uiSelectBox, scriptParams);
    } else if (options != null && options.length() >0) {
       String[] array = options.split(",");
       optionsList = new ArrayList<SelectItemOption<String>>(5);
       for(int i = 0; i < array.length; i++) {
         optionsList.add(new SelectItemOption<String>(array[i].trim(), array[i].trim()));
       }
       uiSelectBox.setOptions(optionsList);
    }
    uiSelectBox.setDefaultValue(defaultValue) ;
    propertiesName_.put(name, getPropertyName(jcrPath)) ;
    if(node_ == null) {
      if (defaultValue != null && defaultValue.length() > 0) {
        uiSelectBox.setDefaultValue(defaultValue);
        uiSelectBox.reset();
      }
    } else if(node_.hasProperty(getPropertyName(jcrPath))){
      uiSelectBox.setValue(node_.getProperty(getPropertyName(jcrPath)).getValue().getString()) ;      
    }
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    setInputProperty(name, inputProperty) ;
    if(editable.equals("false")) uiSelectBox.setDisabled(false) ;
    else uiSelectBox.setEditable(true) ;
    addUIFormInput(uiSelectBox) ;
    if(isNotEditNode_) {
      if(propertyNode_ != null) uiSelectBox.setValue(getPropertyValue(jcrPath)) ;
    }
    if(onchange.equals("true")) uiSelectBox.setOnChange("Onchange") ;
    renderField(name) ;
  }
  public String getSelectBoxFieldValue(String name) {
    UIFormSelectBox uiSelectBox = findComponentById(name) ;
    String value = null ;
    if (uiSelectBox != null) value = uiSelectBox.getValue() ;
    return value ;
  }
  
  public void addUploadField(String name, String[] arguments) throws Exception {
    String editable = "true";
    String jcrPath = null;
    String multiValues = null ;
    for(String argument : arguments) {
      if (argument.startsWith(JCR_PATH)) {
        jcrPath = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(MULTI_VALUES)) {
        multiValues = argument.substring(argument.indexOf(SEPARATOR) + 1);        
      } else if(argument.startsWith(EDITABLE)) {
        editable = argument.substring(argument.indexOf(SEPARATOR) + 1);
      }
    }
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    setInputProperty(name, inputProperty) ;
    setMultiPart(true) ;
    if(multiValues != null && multiValues.equals("true")) {
      UIFormMultiValueInputSet uiMulti = createUIComponent(UIFormMultiValueInputSet.class, null, null) ;
      uiMulti.setId(name) ;
      uiMulti.setName(name) ;
      uiMulti.setType(UIFormUploadInput.class) ;
      addUIFormInput(uiMulti) ;
      renderField(name) ;
      return ;
    }
    UIFormUploadInput uiInputUpload = findComponentById(name) ;
    if(uiInputUpload == null) {
      uiInputUpload = new UIFormUploadInput(name, name) ;
      addUIFormInput(uiInputUpload) ;
    }
    if(editable.equals("false")) uiInputUpload.setEditable(false) ;
    else uiInputUpload.setEditable(false) ;
    propertiesName_.put(name, getPropertyName(jcrPath)) ;
    renderField(name) ;
  }
  
  public void addMixinField(String name, String[] arguments) throws Exception {
    String jcrPath = null;
    String nodetype = null;
    String mixintype = null;
    String defaultValue = "";
    String editable = "true";
    String visible = "true";
    for(int i = 0; i < arguments.length; i++) {
      String argument = arguments[i];
      if (argument.startsWith(JCR_PATH)) {
        jcrPath = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(NODETYPE)) {
        nodetype = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(MIXINTYPE)) {
        mixintype = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(VISIBLE)) {
        visible = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(EDITABLE)) {
        editable = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else {
        defaultValue = argument;
      }
    }
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    if (nodetype != null || mixintype != null) {
      inputProperty.setType(JcrInputProperty.NODE);
      if(nodetype != null) inputProperty.setNodetype(nodetype);
      if(mixintype != null) inputProperty.setMixintype(mixintype);
    }
    setInputProperty(name, inputProperty) ;
    if(node_ != null && visible.equals("if-not-null")) {
      UIFormStringInput uiMixin = findComponentById(name) ;
      if(uiMixin == null) {
        uiMixin = new UIFormStringInput(name, name, defaultValue) ;
        addUIFormInput(uiMixin) ;
      }
      uiMixin.setValue(node_.getName()) ;
      uiMixin.setEditable(false) ;
      renderField(name) ; 
    }
  }
  
  public void addCalendarField(String name, String[] arguments) throws Exception {
    String jcrPath = null;
    String options = null;
    String defaultValue = "";
    String[] arrDate = null;
    String multiValues = null ;
    String visible = "true";
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy") ;
    for(int i = 0; i < arguments.length; i++) {
      String argument = arguments[i];
      if (argument.startsWith(JCR_PATH)) {
        jcrPath = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(OPTIONS)) {
        options = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(VISIBLE)) {
        visible = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(MULTI_VALUES)) {
        multiValues = argument.substring(argument.indexOf(SEPARATOR) + 1);        
      } else {
        defaultValue = argument ;
      }
    }
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    setInputProperty(name, inputProperty) ;
    Date date = new Date() ;
    if(defaultValue.length() > 0) {
      try {
        date = formatter.parse(defaultValue) ;
        if(defaultValue.indexOf("/") > -1) arrDate = defaultValue.split("/") ;
        String[] arrDf = formatter.format(date).split("/") ;
        if(Integer.parseInt(arrDate[0]) != Integer.parseInt(arrDf[0])) date = new Date() ;
      } catch(Exception e) {
        date = new Date() ;
      }
    }
    if(multiValues != null && multiValues.equals("true")) {
      UIFormMultiValueInputSet uiMulti = createUIComponent(UIFormMultiValueInputSet.class, null, null) ;
      uiMulti.setId(name) ;
      uiMulti.setName(name) ;
      uiMulti.setType(UIFormDateTimeInput.class) ;
      addUIFormInput(uiMulti) ;
      renderField(name) ;
      return ;
    } 
    UIFormDateTimeInput uiDateTime = findComponentById(name) ;
    if(uiDateTime == null) {
      uiDateTime = new UIFormDateTimeInput(name, name, date) ;
      addUIFormInput(uiDateTime) ;
    }
    propertiesName_.put(name, getPropertyName(jcrPath)) ;
    if(node_ != null && node_.hasProperty(getPropertyName(jcrPath))) {
      uiDateTime.setDateValue(node_.getProperty(getPropertyName(jcrPath)).getDate().getTime()) ;
    } 
    if(isNotEditNode_) {
      if(propertyNode_ != null) {
        String propertyName = jcrPath.substring(jcrPath.lastIndexOf("/") + 1) ;
        if(propertyNode_.hasProperty(propertyName)) {
          if(!propertyNode_.getProperty(propertyName).getDefinition().isMultiple()) {
            uiDateTime.setDateValue(propertyNode_.getProperty(propertyName).getDate().getTime());
          }
        }
      }
    }
    if(!visible.equals("false")) renderField(name) ;
  }
  
  public void addHiddenField(String name, String[] arguments) throws Exception {
    String jcrPath = null;
    String nodetype = null;
    String mixintype = null;
    String defaultValue = "";
    String editable = "true";
    String visible = "true";
    for(String argument : arguments) {
      if (argument.startsWith(JCR_PATH)) {
        jcrPath = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(NODETYPE)) {
        nodetype = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(MIXINTYPE)) {
        mixintype = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(VISIBLE)) {
        visible = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(EDITABLE)) {
        editable = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else {
        defaultValue = argument;
      }
    }
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    if(defaultValue.length() > 0) {
      inputProperty.setValue(defaultValue) ;
    }
    if (nodetype != null || mixintype != null) {
      inputProperty.setType(JcrInputProperty.NODE);
      if(nodetype != null) inputProperty.setNodetype(nodetype);
      if(mixintype != null) inputProperty.setMixintype(mixintype);
    }
    setInputProperty(name, inputProperty) ;
  }
  
  public void addInterceptor(String scriptPath, String type) {
    if(scriptPath.length() > 0 && type.length() > 0) scriptInterceptor_.add(scriptPath+ ";" + type) ;
  }
  
  private void executeScript(String script, Object o, String[] params) {
    ScriptService scriptService = getApplicationComponent(ScriptService.class) ;
    try {
      CmsScript dialogScript = scriptService.getScript(script);
      if(params != null) dialogScript.setParams(params);
      dialogScript.execute(o);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void renderField(String name) throws Exception {
    UIComponent uiInput = findComponentById(name);;
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    Writer w = context.getWriter() ;
    uiInput.processRender(context) ;
    if(components.get(name) != null) {
      Map fieldPropertiesMap = components.get(name) ;
      String fieldName = (String)fieldPropertiesMap.get("returnField") ;
      String iconClass = "Add16x16Icon" ;
      if(fieldPropertiesMap.get("selectorIcon") != null) {
        iconClass = (String)fieldPropertiesMap.get("selectorIcon") ;
      }

      if(name.equals(fieldName)) {
        w.write("<div class='"+ iconClass +"' style=\"cursor:pointer;\" "
                + "onclick=\"javascript:eXo.webui.UIForm.submitEvent('" 
                + "" + getId() +"','ShowComponent','&objectId="+ fieldName +"' )\"><span></span></div>") ;
      } 
    }
  }
  
  public void begin() throws Exception {
    String portalName = PortalContainer.getInstance().getPortalContainerInfo().getContainerName();
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    context.getJavascriptManager().importJavascript("eXo.ecm.ExoEditor","/ecm/javascript/");
    super.begin();
  }
  
  public String event(String name) {
    StringBuilder b = new StringBuilder("javascript:") ;
    b.append("eXo.ecm.ExoEditor.saveHandler() ;").
      append("eXo.webui.UIForm.submitForm('").append(getId()).append("','").
      append(name).append("', true)");
    return b.toString() ;
  }

  public void storeValue(Event event) throws Exception {}
  public void onchange(Event event) throws Exception {}
  public String getPath() { return null ; }
  
  static  public class SaveActionListener extends EventListener<DialogFormFields> {
    public void execute(Event<DialogFormFields> event) throws Exception {
      DialogFormFields dialogForm = event.getSource() ;
      UIJCRExplorer uiJCRExplorer = dialogForm.getAncestorOfType(UIJCRExplorer.class) ;
      String path = uiJCRExplorer.getCurrentNode().getPath() ;
      if(path.indexOf(":") < 0) {
        path = uiJCRExplorer.getSession().getWorkspace().getName() + ":" + path ;
      }
      if(dialogForm.scriptInterceptor_.size() == 0) {
        dialogForm.storeValue(event) ;
        return ;
      }
      boolean isPrev = false ;
      for(String interceptor : dialogForm.scriptInterceptor_) {
        if(interceptor.indexOf(";") < 0) continue ;
        String scriptPath = interceptor.split(";")[0] ;
        String type = interceptor.split(";")[1] ;
        if(type.equals("prev")) {
          dialogForm.executeScript(scriptPath, path, null) ;
          dialogForm.storeValue(event) ;
          isPrev = true ;
        } else if(type.equals("post") && isPrev) {
          dialogForm.executeScript(scriptPath, dialogForm.getPath(), null) ;
        } else if(type.endsWith("post") && !isPrev) {
          dialogForm.storeValue(event) ;
          dialogForm.executeScript(scriptPath, dialogForm.getPath(), null) ;
        }
      }
    }
  }
  
  static  public class OnchangeActionListener extends EventListener<DialogFormFields> {
    public void execute(Event<DialogFormFields> event) throws Exception {     
      event.getSource().onchange(event) ;
    }
  }
}
