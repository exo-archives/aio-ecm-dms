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
package org.exoplatform.ecm.webui.component;

import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.ecm.jcr.ECMNameValidator;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.scripts.CmsScript;
import org.exoplatform.services.cms.scripts.ScriptService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormUploadInput;
import org.exoplatform.webui.form.UIFormWYSIWYGInput;
import org.exoplatform.webui.form.validator.DateTimeValidator;
import org.exoplatform.webui.form.validator.EmailAddressValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.NumberFormatValidator;

/**
 * Created by The eXo Platform SARL
 * Author : nqhungvn
 *          nguyenkequanghung@yahoo.com
 * nov 6, 2006
 * 
 */

@SuppressWarnings("unused")
public class DialogFormFields extends UIForm {

  public Map<String, Map> components = new HashMap<String, Map>();
  public Map<String, String> propertiesName_ = new HashMap<String, String>() ;
  public Map<String, String> fieldNames_ = new HashMap<String, String>() ;

  private boolean isNotEditNode_ = false ;
  private boolean isNTFile_ = false ;
  private boolean isResetMultiField_ = false ;
  private boolean isOnchange_ = false ;
  protected boolean isUpdateSelect_ = false ;
  private boolean isResetForm_ = false ;
  private String workspaceName_ = null ;
  private String storedPath_ = null ;
  protected String repositoryName_ = null ;
  private String nodePath_ ;
  private String childPath_ ;

  private List<String> prevScriptInterceptor_ = new ArrayList<String>() ; 
  private List<String> postScriptInterceptor_ = new ArrayList<String>() ;
  private final String SEPARATOR = "=";
  private final String JCR_PATH = "jcrPath" + SEPARATOR;
  private final String EDITABLE = "editable" + SEPARATOR;
  private final String ONCHANGE = "onchange" + SEPARATOR;
  private final String OPTIONS = "options" + SEPARATOR;
  private final String TYPE = "type" + SEPARATOR ;
  private final String VISIBLE = "visible" + SEPARATOR;
  private final String NODETYPE = "nodetype" + SEPARATOR;
  private final String MIXINTYPE = "mixintype" + SEPARATOR;
  private final String VALIDATE = "validate" + SEPARATOR;
  private final String SELECTOR_ACTION = "selectorAction" + SEPARATOR;
  private final String SELECTOR_CLASS = "selectorClass" + SEPARATOR;
  private final String SELECTOR_ICON = "selectorIcon" + SEPARATOR;
  private final String SELECTOR_PARAMS = "selectorParams" + SEPARATOR;
  private final String WORKSPACE_FIELD = "workspaceField" + SEPARATOR;
  private final String SCRIPT = "script" + SEPARATOR;
  private final String SCRIPT_PARAMS = "scriptParams" + SEPARATOR;
  private final String MULTI_VALUES = "multiValues" + SEPARATOR;
  private final String REPOSITORY = "repository";
  private final String DEFAULT_VALUES = "defaultValues" + SEPARATOR ;

  public static final  String[]  ACTIONS = {"Save", "Cancel"};

  protected Map<String, Object> properties = new HashMap<String, Object>();

  public DialogFormFields() throws Exception {}

  public Node getNode() throws Exception { 
    if(nodePath_ == null) return null ;
    return (Node) getSesssion().getItem(nodePath_) ; 
  }
  public void setNodePath(String nodePath) { nodePath_ = nodePath ; }

  public Session getSesssion() throws Exception {
    return SessionProviderFactory.createSessionProvider().getSession(workspaceName_, getRepository()) ;
  }    

  private ManageableRepository getRepository() throws Exception{         
    RepositoryService repositoryService  = getApplicationComponent(RepositoryService.class) ;      
    return repositoryService.getRepository(repositoryName_);
  }

  public void setChildPath(String childPath) { childPath_ = childPath ; }
  public Node getChildNode() throws Exception { 
    if(childPath_ == null) return null ;
    return (Node) getSesssion().getItem(childPath_) ; 
  }

  public void setInputProperty(String name, Object value) { properties.put(name, value) ; }
  public Object getInputProperty(String name) { return properties.get(name) ; }

  public Map<String, Object> getInputProperties() { return properties ; }

  public void resetProperties() { properties.clear() ; }

  public void setIsResetMultiField(boolean isResetMultiField) { 
    isResetMultiField_ = isResetMultiField ; 
  }

  public void setIsResetForm(boolean isResetForm) { isResetForm_ = isResetForm ; }
  public boolean isResetForm() { return isResetForm_ ; }

  public void setIsUpdateSelect(boolean isUpdateSelect) { isUpdateSelect_ = isUpdateSelect ; } ;

  public void setIsOnchange(boolean isOnchange) { isOnchange_ = isOnchange ; }

  public void setIsNotEditNode(boolean isNotEditNode) { isNotEditNode_ = isNotEditNode ; }
  public void setIsNTFile(boolean isNTFile) { isNTFile_ = isNTFile ; }

  public void setWorkspace(String workspace) { workspaceName_ = workspace ; }
  public void setStoredPath(String storedPath) { storedPath_ = storedPath ; }

  public String getPropertyName(String jcrPath) { 
    return jcrPath.substring(jcrPath.lastIndexOf("/") + 1) ; 
  }
  public void setRepositoryName(String repositoryName){ repositoryName_ = repositoryName ; }
  public void resetScriptInterceptor(){
    prevScriptInterceptor_.clear() ;
    postScriptInterceptor_.clear() ;
  }

  private String getPropertyValue(String jcrPath) throws Exception {
    if(jcrPath.equals("/node") && getNode() != null) return getNode().getName() ;
    if(getChildNode().hasProperty(getPropertyName(jcrPath))) {
      int valueType = getChildNode().getProperty(getPropertyName(jcrPath)).getType() ;
      switch(valueType) {
      case 1: //String 
        return getChildNode().getProperty(getPropertyName(jcrPath)).getString() ;
      case 2:
        return "" ;
      case 3: // Long    
        return Long.toString(getChildNode().getProperty(getPropertyName(jcrPath)).getLong()) ;
      case 4: // Double
        return Double.toString(getChildNode().getProperty(getPropertyName(jcrPath)).getDouble()) ;
      case 5: //Date
        return getChildNode().getProperty(getPropertyName(jcrPath)).getDate().getTime().toString() ;
      case 6: //Boolean
        return Boolean.toString(getChildNode().getProperty(getPropertyName(jcrPath)).getBoolean()) ;
      case 7: //Name
        return getChildNode().getProperty(getPropertyName(jcrPath)).getName() ;
      case 8: //Path
      case 9: //References
      case 0: //Undifine
        return "" ;
      }
    }
    return "" ;
  }

  public void addActionField(String name, String[] arguments) throws Exception { 
    addActionField(name,null,arguments);
  }

  public void addActionField(String name,String label,String[] arguments) throws Exception {
    HashMap<String,String> parsedArguments = parseArguments(arguments) ;
    String editable = parsedArguments.get(EDITABLE);
    String defaultValue = parsedArguments.get(DEFAULT_VALUES);
    String jcrPath = parsedArguments.get(JCR_PATH);
    String selectorAction = parsedArguments.get(SELECTOR_ACTION);
    String selectorClass = parsedArguments.get(SELECTOR_CLASS);    
    String workspaceField = parsedArguments.get(WORKSPACE_FIELD);
    String selectorIcon = parsedArguments.get(SELECTOR_ICON);
    String multiValues = parsedArguments.get(MULTI_VALUES);
    String validateType = parsedArguments.get(VALIDATE) ;
    String params = parsedArguments.get(SELECTOR_PARAMS) ;
    String[] selectorParams = null;
    if(params != null) {
      selectorParams = params.split(",");
    }
    if(selectorClass != null) { 
      Map<String, String> fieldPropertiesMap = new HashMap<String, String>() ;
      fieldPropertiesMap.put("selectorClass", selectorClass) ;
      fieldPropertiesMap.put("returnField", name) ;
      fieldPropertiesMap.put("selectorIcon", selectorIcon) ;
      fieldPropertiesMap.put("workspaceField", workspaceField) ;
      if(params != null) fieldPropertiesMap.put("selectorParams", params) ;
      components.put(name, fieldPropertiesMap) ;
    }
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    setInputProperty(name, inputProperty) ;
    if("true".equals(multiValues)) {
      renderMultiValuesInput(UIFormStringInput.class,name,label) ;      
      return ;
    }
    UIFormStringInput uiInput = findComponentById(name) ;
    if(uiInput == null) {
      uiInput = new UIFormStringInput(name, name, defaultValue) ;
      if(validateType != null) {
        uiInput.addValidator(getValidator(validateType)) ;
      }
      if(label != null ) {
        uiInput.setLabel(label);
      }
      addUIFormInput(uiInput) ;
    }
    if("false".equals(editable)) uiInput.setEditable(false) ;
    else uiInput.setEditable(true) ;
    propertiesName_.put(name, getPropertyName(jcrPath)) ;
    fieldNames_.put(getPropertyName(jcrPath), name) ;
    if(getNode() != null) {
      if(jcrPath.equals("/node") && (editable.equals("false") || editable.equals("if-null"))) {
        uiInput.setValue(getNode().getName()) ;
        uiInput.setEditable(false) ;
      } else if(getNode().hasProperty(getPropertyName(jcrPath)) && !isUpdateSelect_) {
        uiInput.setValue(getNode().getProperty(getPropertyName(jcrPath)).getValue().getString()) ;
      } 
    }
    if(isNotEditNode_) {
      if(getChildNode() != null) {
        uiInput.setValue(getPropertyValue(jcrPath)) ;
      } else if(getChildNode() == null && jcrPath.equals("/node") && getNode() != null) {
        uiInput.setValue(getNode().getName()) ;
      } else {
        uiInput.setValue(null) ;
      }
    }
    renderField(name) ;
  }

  public void addTextField(String name, String[] arguments) throws Exception {
    addTextField(name,null,arguments);
  }

  public void addTextField(String name, String label, String[] arguments) throws Exception {
    HashMap<String,String> parsedArguments = parseArguments(arguments) ;       
    String type = parsedArguments.get(TYPE);
    String editable = parsedArguments.get(EDITABLE);
    String defaultValue = parsedArguments.get(DEFAULT_VALUES);
    String jcrPath = parsedArguments.get(JCR_PATH);
    String mixintype = parsedArguments.get(MIXINTYPE);
    String multiValues = parsedArguments.get(MULTI_VALUES);
    String validateType = parsedArguments.get(VALIDATE) ;
    String nodetype = parsedArguments.get(NODETYPE);

    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    setInputProperty(name, inputProperty) ;
    String propertyName = getPropertyName(jcrPath) ;
    if(mixintype != null) inputProperty.setMixintype(mixintype) ;
    if(jcrPath.equals("/node") && nodetype != null ) inputProperty.setNodetype(nodetype);
    properties.put(name, inputProperty) ;
    propertiesName_.put(name, propertyName) ;
    fieldNames_.put(propertyName, name) ;
    if("true".equalsIgnoreCase(multiValues)) {
      UIFormMultiValueInputSet uiMulti ;
      if(getNode() == null && getChildNode() == null) {
        uiMulti = findComponentById(name) ;
        if(uiMulti == null) {
          uiMulti = createUIComponent(UIFormMultiValueInputSet.class, null, null) ;
          uiMulti.setId(name) ;
          uiMulti.setName(name) ;
          uiMulti.setType(UIFormStringInput.class) ;
          uiMulti.setValue(new ArrayList<Value>()) ;
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
      if(getChildNode() != null) {
        if(getChildNode().hasProperty(getPropertyName(jcrPath))) {
          Value[] values = getChildNode().getProperty(getPropertyName(jcrPath)).getValues() ;
          for(Value value : values) {
            valueList.add(value.getString()) ;
          }
          uiMulti.setValue(valueList) ;
        }
      }
      if(getNode() != null) {
        String propertyPath = jcrPath.substring("/node/".length()) ;
        if(getNode().hasProperty(propertyPath)) {
          Value[] values = getNode().getProperty(propertyPath).getValues() ;
          for(Value vl : values) {
            if (vl != null) valueList.add(vl.getString()) ;
          }
        }
        uiMulti.setValue(valueList) ;        
      }
      if(isResetMultiField_) {
        uiMulti.setValue(new ArrayList<Value>()) ;
      }
      renderField(name) ;
      return ;
    } 
    UIFormStringInput uiInput = findComponentById(name) ;
    if(uiInput == null) {
      uiInput = new UIFormStringInput(name, name, defaultValue) ;
      //TODO need use full class name for validate type. 
      if(validateType != null) {
        uiInput.addValidator(getValidator(validateType)) ;
      }     
      if(label != null && label.length()!=0) {
        uiInput.setLabel(label);
      }
      addUIFormInput(uiInput) ;      
    }
    if(uiInput.getValue() == null) uiInput.setValue(defaultValue) ;
    if("password".equals(type)) uiInput.setType(UIFormStringInput.PASSWORD_TYPE) ;
    if("false".equals(editable)) uiInput.setEditable(false) ;
    else uiInput.setEditable(true) ;
    if(getNode() != null) {
      if(jcrPath.equals("/node") && (editable.equals("false") || editable.equals("if-null"))) {
        Node parentNode = getNode().getParent() ;
        if(parentNode != null && parentNode.getName().equals("languages")) {
          uiInput.setValue(getNode().getParent().getParent().getName()) ;
        } else {
          String nameValue =  getNode().getPath().substring(getNode().getPath().lastIndexOf("/") + 1) ;
          uiInput.setValue(nameValue) ;
        }
        uiInput.setEditable(false) ;
      } else if(getNode().hasProperty(propertyName)) {
        uiInput.setValue(getNode().getProperty(propertyName).getValue().getString()) ;
      } 
    }
    if(isNotEditNode_) {
      if(getChildNode() != null) {
        uiInput.setValue(getPropertyValue(jcrPath)) ;
      } else if(getChildNode() == null && jcrPath.equals("/node") && getNode() != null) {
        uiInput.setValue(getNode().getName()) ;
      } else {
        uiInput.setValue(defaultValue) ;
      }
    }
    renderField(name) ;
  }

  public void addTextAreaField(String name, String[] arguments) throws Exception {
    addTextAreaField(name,null,arguments);
  }

  public void addTextAreaField(String name, String label, String[] arguments) throws Exception {
    HashMap<String,String> parsedArguments = parseArguments(arguments) ;
    String editable = parsedArguments.get(EDITABLE);
    String defaultValue = parsedArguments.get(DEFAULT_VALUES);
    String jcrPath = parsedArguments.get(JCR_PATH);
    String selectorAction = parsedArguments.get(SELECTOR_ACTION);
    String selectorClass = parsedArguments.get(SELECTOR_CLASS);
    String multiValues = parsedArguments.get(MULTI_VALUES);
    String validateType = parsedArguments.get(VALIDATE) ;    
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
      renderMultiValuesInput(UIFormDateTimeInput.class,name,label) ;      
      return ;
    }
    UIFormTextAreaInput uiTextArea = findComponentById(name) ;    
    if(uiTextArea == null) {
      uiTextArea = new UIFormTextAreaInput(name, name, defaultValue) ;
      if(validateType != null) {
        uiTextArea.addValidator(getValidator(validateType)) ;
      }
      if(label != null) uiTextArea.setLabel(label) ;
      addUIFormInput(uiTextArea) ;
    }
    if(uiTextArea.getValue() == null) uiTextArea.setValue(defaultValue) ;
    if("false".equals(editable)) uiTextArea.setEditable(false) ;
    else uiTextArea.setEditable(true) ;
    propertiesName_.put(name, getPropertyName(jcrPath)) ;
    fieldNames_.put(getPropertyName(jcrPath), name) ;

    if(getNode() != null) {
      String value = "";
      if(getNode().hasProperty(getPropertyName(jcrPath))) {
        value = getNode().getProperty(getPropertyName(jcrPath)).getValue().getString() ;
      } else if(getNode().isNodeType("nt:file")) {
        Node jcrContentNode = getNode().getNode("jcr:content") ;
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
      if(getChildNode() != null) {
        uiTextArea.setValue(getPropertyValue(jcrPath)) ;
      } else if(getChildNode() == null && jcrPath.equals("/node") && getNode() != null) {
        uiTextArea.setValue(getNode().getName()) ;
      } else {
        uiTextArea.setValue(null) ;
      }
    }
    renderField(name) ;
  }

  public void addWYSIWYGField(String name, String[] arguments) throws Exception {
    addWYSIWYGField(name,null,arguments);
  }

  public void addWYSIWYGField(String name, String label, String[] arguments) throws Exception {
    HashMap<String,String> parsedArguments = parseArguments(arguments) ;
    String options = parsedArguments.get(OPTIONS) ;
    String defaultValue = parsedArguments.get(DEFAULT_VALUES);
    String jcrPath = parsedArguments.get(JCR_PATH);    
    String multiValues = parsedArguments.get(MULTI_VALUES) ;
    String validateType = parsedArguments.get(VALIDATE) ;
    boolean isBasic = false ;
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);       
    setInputProperty(name, inputProperty) ;
    if("true".equalsIgnoreCase(multiValues)) {
      renderMultiValuesInput(UIFormWYSIWYGInput.class,name,label);      
      return ;
    }
    if("basic".equals(options)) 
      isBasic = true ;
    else 
      isBasic = false;        

    UIFormWYSIWYGInput wysiwyg = findComponentById(name) ;
    if(wysiwyg == null) {
      wysiwyg = new UIFormWYSIWYGInput(name, name, defaultValue, isBasic) ;
      if(validateType != null) {
        wysiwyg.addValidator(getValidator(validateType)) ;
      }     
      addUIFormInput(wysiwyg) ;
    }
    if(wysiwyg.getValue() == null) wysiwyg.setValue(defaultValue) ;
    propertiesName_.put(name, getPropertyName(jcrPath)) ;
    fieldNames_.put(getPropertyName(jcrPath), name) ;
    if(getNode() != null && (getNode().isNodeType("nt:file") || isNTFile_)) {
      Node jcrContentNode = getNode().getNode("jcr:content") ;
      wysiwyg.setValue(jcrContentNode.getProperty("jcr:data").getValue().getString()) ;
    } else {
      if(getNode() != null && getNode().hasProperty(getPropertyName(jcrPath))) {
        wysiwyg.setValue(getNode().getProperty(getPropertyName(jcrPath)).getValue().getString()) ;
      }
    }
    if(isNotEditNode_) {
      if(getNode() != null && getNode().hasNode("jcr:content") && getChildNode() != null) {
        Node jcrContentNode = getNode().getNode("jcr:content") ;
        wysiwyg.setValue(jcrContentNode.getProperty("jcr:data").getValue().getString()) ;
      } else {
        if(getChildNode() != null) {
          wysiwyg.setValue(getPropertyValue(jcrPath)) ;
        } else if(getChildNode() == null && jcrPath.equals("/node") && getNode() != null) {
          wysiwyg.setValue(getNode().getName()) ;
        } else {
          wysiwyg.setValue(null) ;
        }
      }
    }
    renderField(name) ;
  }

  public void addSelectBoxField(String name, String[] arguments) throws Exception {
    addSelectBoxField(name,null,arguments) ;
  }

  public void addSelectBoxField(String name, String label, String[] arguments) throws Exception {
    HashMap<String,String> parsedArguments = parseArguments(arguments) ;    
    String multiValues = parsedArguments.get(MULTI_VALUES) ;    
    if("true".equals(multiValues)) {
      renderMultiValuesInput(UIFormSelectBox.class,name,label);
      return ;
    }
    String jcrPath = parsedArguments.get(JCR_PATH);
    String editable = parsedArguments.get(EDITABLE);
    String onchange = parsedArguments.get(ONCHANGE);
    String defaultValue = parsedArguments.get(DEFAULT_VALUES);
    String options = parsedArguments.get(OPTIONS);
    String script = parsedArguments.get(SCRIPT);
    String[] scriptParams = parsedArguments.get(SCRIPT_PARAMS).split(",");
    List<SelectItemOption<String>> optionsList = new ArrayList<SelectItemOption<String>>();
    UIFormSelectBox uiSelectBox = findComponentById(name) ;
    if(uiSelectBox == null || isResetForm_) {
      uiSelectBox = new UIFormSelectBox(name, name, null);
      addUIFormInput(uiSelectBox) ;
      if (script != null) {
        try {
          if("repository".equals(scriptParams[0])) scriptParams[0] = repositoryName_ ;
          executeScript(script, uiSelectBox, scriptParams);
        } catch(Exception e) {
          uiSelectBox.setOptions(optionsList) ;
        }      
      } else if (options != null && options.length() >0) {
        String[] array = options.split(",");
        for(int i = 0; i < array.length; i++) {
          optionsList.add(new SelectItemOption<String>(array[i].trim(), array[i].trim()));
        }
        uiSelectBox.setOptions(optionsList);
      } else {
        uiSelectBox.setOptions(optionsList) ;
      }      
      if(defaultValue != null) uiSelectBox.setValue(defaultValue) ;
    }
    propertiesName_.put(name, getPropertyName(jcrPath)) ;
    fieldNames_.put(getPropertyName(jcrPath), name) ;
    String[] arrNodes = jcrPath.split("/") ;
    Node childNode = null ;
    if(getNode() != null && arrNodes.length == 4) childNode = getNode().getNode(arrNodes[2]) ;
    if(childNode != null) {
      uiSelectBox.setValue(childNode.getProperty(getPropertyName(jcrPath)).getValue().getString()) ;
    } else {
      if(getNode() != null && getNode().hasProperty(getPropertyName(jcrPath))) {
        if(getNode().getProperty(getPropertyName(jcrPath)).getDefinition().isMultiple()) {
          uiSelectBox.setValue(getNode().getProperty(getPropertyName(jcrPath)).getValues().toString()) ;
        } else if("true".equals(onchange) && isOnchange_) {
          uiSelectBox.setValue(uiSelectBox.getValue()) ;
        } else {
          uiSelectBox.setValue(getNode().getProperty(getPropertyName(jcrPath)).getValue().getString()) ;      
        }
      }
    }
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    setInputProperty(name, inputProperty) ;
    if("false".equalsIgnoreCase(editable)) uiSelectBox.setDisabled(false) ;
    else uiSelectBox.setEditable(true) ;
    addUIFormInput(uiSelectBox) ;
    if(isNotEditNode_) {
      if(getChildNode() != null) uiSelectBox.setValue(getPropertyValue(jcrPath)) ; 
    }
    if("true".equalsIgnoreCase(onchange)) uiSelectBox.setOnChange("Onchange") ;
    renderField(name) ;   
  }    

  public String getSelectBoxFieldValue(String name) {
    UIFormSelectBox uiSelectBox = findComponentById(name) ;
    if (uiSelectBox != null) return uiSelectBox.getValue() ;
    return null ;
  }

  public void addUploadField(String name, String[] arguments) throws Exception {
    addUploadField(name,null,arguments) ;
  }

  public void addUploadField(String name,String label,String[] arguments) throws Exception {
    HashMap<String,String> parsedArguments = parseArguments(arguments) ;
    String jcrPath = parsedArguments.get(JCR_PATH);
    String multiValues = parsedArguments.get(MULTI_VALUES) ;    
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    setInputProperty(name, inputProperty) ;
    setMultiPart(true) ;
    if("true".equalsIgnoreCase(multiValues)) {
      renderMultiValuesInput(UIFormUploadInput.class,name,label) ;      
      return ;
    }    
    UIFormUploadInput uiInputUpload = findComponentById(name) ;
    if(uiInputUpload == null) {
      uiInputUpload = new UIFormUploadInput(name, name) ;
      if(label != null) uiInputUpload.setLabel(label) ;
      addUIFormInput(uiInputUpload) ;
    }
    propertiesName_.put(name, getPropertyName(jcrPath)) ;
    fieldNames_.put(getPropertyName(jcrPath), name) ;
    renderField(name) ;
  }

  public void addMixinField(String name, String[] arguments) throws Exception {
    addMixinField(name,null,arguments) ;
  }

  public void addMixinField(String name,String label,String[] arguments) throws Exception {
    HashMap<String,String> parsedArguments = parseArguments(arguments) ;
    String jcrPath = parsedArguments.get(JCR_PATH);
    String nodetype = parsedArguments.get(NODETYPE);
    String mixintype = parsedArguments.get(MIXINTYPE);
    String defaultValue = parsedArguments.get(DEFAULT_VALUES);
    String editable = parsedArguments.get(EDITABLE);
    String visible = parsedArguments.get(VISIBLE);

    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    if (nodetype != null || mixintype != null) {
      inputProperty.setType(JcrInputProperty.NODE);
      if(nodetype != null) inputProperty.setNodetype(nodetype);
      if(mixintype != null) inputProperty.setMixintype(mixintype);
    }
    setInputProperty(name, inputProperty) ;
    if(getNode() != null && "if-not-null".equals(visible)) {
      UIFormStringInput uiMixin = findComponentById(name) ;
      if(uiMixin == null) {
        uiMixin = new UIFormStringInput(name, name, defaultValue) ;
        if(label != null) uiMixin.setLabel(label) ;
        addUIFormInput(uiMixin) ;
      }
      uiMixin.setValue(getNode().getName()) ;
      uiMixin.setEditable(false) ;
      renderField(name) ; 
    }
  }      

  public void addCalendarField(String name, String[] arguments) throws Exception {
    addCalendarField(name,null,arguments) ;
  }

  public void addCalendarField(String name, String label, String[] arguments) throws Exception {
    HashMap<String,String> parsedArguments = parseArguments(arguments) ;
    String jcrPath = parsedArguments.get(JCR_PATH);
    String options = parsedArguments.get(OPTIONS);
    String defaultValue =parsedArguments.get(DEFAULT_VALUES);    
    String multiValues =parsedArguments.get(MULTI_VALUES) ;
    String visible = parsedArguments.get(VISIBLE);
    String validateType = parsedArguments.get(VALIDATE);
    String[] arrDate = null;
    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss") ;    
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    setInputProperty(name, inputProperty) ;
    Date date = new Date() ;
    if(options == null) formatter = new SimpleDateFormat("MM/dd/yyyy") ;
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
      renderMultiValuesInput(UIFormDateTimeInput.class,name,label) ;      
      return ;
    } 
    UIFormDateTimeInput uiDateTime = findComponentById(name) ;
    if(uiDateTime == null) {
      if(options != null && options.equals("displaytime")) {
        uiDateTime = new UIFormDateTimeInput(name, name, date) ;
      } else {
        uiDateTime = new UIFormDateTimeInput(name, name, date, false) ;
      }
      if(label != null) uiDateTime.setLabel(label);
      addUIFormInput(uiDateTime) ;
    }
    if(options != null && options.equals("displaytime")) uiDateTime.setDisplayTime(true) ;
    else uiDateTime.setDisplayTime(false) ;
    if(validateType != null) {      
        uiDateTime.addValidator(getValidator(validateType)) ;      
    }
    propertiesName_.put(name, getPropertyName(jcrPath)) ;
    fieldNames_.put(getPropertyName(jcrPath), name) ;
    if(getNode() != null && getNode().hasProperty(getPropertyName(jcrPath))) {
      uiDateTime.setCalendar(getNode().getProperty(getPropertyName(jcrPath)).getDate()) ;
    } 

    if(isNotEditNode_) {
      if(getChildNode() != null) {
        String propertyName = jcrPath.substring(jcrPath.lastIndexOf("/") + 1) ;
        if(getChildNode().hasProperty(propertyName)) {
          if(getChildNode().getProperty(propertyName).getDefinition().isMultiple()) {
            Value[] values = getChildNode().getProperty(propertyName).getValues() ;
            for(Value value : values) {
              uiDateTime.setCalendar(value.getDate()) ;
            }
          } else {
            uiDateTime.setCalendar(getChildNode().getProperty(propertyName).getValue().getDate());
          }
        }
      } else if(getChildNode() == null && jcrPath.equals("/node") && getNode() != null) {
        uiDateTime.setCalendar(getNode().getProperty(getPropertyName(jcrPath)).getDate());
      } else {
        uiDateTime.setCalendar(new GregorianCalendar()) ;
      }
    }
    if(!"false".equalsIgnoreCase(visible)) renderField(name) ;
  }  

  public void addHiddenField(String name, String[] arguments) throws Exception {
    HashMap<String,String> parsedArguments = parseArguments(arguments) ;
    String jcrPath = parsedArguments.get(JCR_PATH);
    String nodetype = parsedArguments.get(NODETYPE);
    String mixintype = parsedArguments.get(MIXINTYPE);
    String defaultValue = parsedArguments.get(DEFAULT_VALUES);
    String editable = parsedArguments.get(EDITABLE);
    String visible = parsedArguments.get(VISIBLE);    
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
    if(scriptPath.length() > 0 && type.length() > 0){
      if(type.equals("prev")){
        prevScriptInterceptor_.add(scriptPath + ";" + type) ;
      } else if(type.equals("post")){
        postScriptInterceptor_.add(scriptPath + ";" + type) ;
      }
    } 
  }

  private HashMap<String,String> parseArguments(String[] arguments) {
    HashMap<String,String> map = new HashMap<String,String>() ;    
    for(String argument:arguments) {
      String value = null;
      if(argument.indexOf(SEPARATOR)>0) {
        value = argument.substring(argument.indexOf(SEPARATOR)+1) ;
      }else {
        value = argument;
        map.put(DEFAULT_VALUES,value) ; continue;
      }      
      if (argument.startsWith(JCR_PATH)) {        
        map.put(JCR_PATH,value); continue;
      } else if (argument.startsWith(EDITABLE)) {       
        map.put(EDITABLE,value); continue;
      } else if (argument.startsWith(SELECTOR_ACTION)) {        
        map.put(SELECTOR_ACTION,value) ; continue;
      } else if (argument.startsWith(SELECTOR_CLASS)) {        
        map.put(SELECTOR_CLASS,value); continue;
      } else if (argument.startsWith(MULTI_VALUES)) {        
        map.put(MULTI_VALUES,value); continue;
      } else if (argument.startsWith(SELECTOR_ICON)) {        
        map.put(SELECTOR_ICON,value); continue;
      } else if (argument.startsWith(SELECTOR_PARAMS)) {               
        map.put(SELECTOR_PARAMS,value); continue;
      }else if (argument.startsWith(WORKSPACE_FIELD)) {        
        map.put(WORKSPACE_FIELD,value); continue;
      } else if (argument.startsWith(VALIDATE)) {       
        map.put(VALIDATE,value); continue;
      } else if(argument.startsWith(DEFAULT_VALUES)) {       
        map.put(DEFAULT_VALUES,value); continue;
      } else if(argument.startsWith(OPTIONS)){        
        map.put(OPTIONS,value);  continue;
      }else if(argument.startsWith(SCRIPT)) {        
        map.put(SCRIPT,value); continue;
      }else if(argument.startsWith(SCRIPT_PARAMS)) {        
        map.put(SCRIPT_PARAMS,value); continue;
      }else if(argument.startsWith(VISIBLE)){        
        map.put(VISIBLE,value); continue;
      }else if(argument.startsWith(TYPE)){
        map.put(TYPE,value) ; continue;
      } else if(argument.startsWith(ONCHANGE)){
        map.put(ONCHANGE,value); continue;
      }else {
        map.put(DEFAULT_VALUES,argument);
      }      
    }
    return map;
  }

  private Class getValidator(String validatorType) throws ClassNotFoundException {
    if(validatorType.equals("name")) {
      return ECMNameValidator.class ;
    } else if (validatorType.equals("email")){
      return EmailAddressValidator.class ;
    } else if (validatorType.equals("number")) {
      return NumberFormatValidator.class;
    } else if (validatorType.equals("empty")){
      return MandatoryValidator.class ;
    }else if(validatorType.equals("datetime")) {
      return DateTimeValidator.class;
    }else {
      ClassLoader cl = Thread.currentThread().getContextClassLoader() ;
      return cl.loadClass(validatorType);
    }
  }

  private void renderMultiValuesInput(Class type, String name,String label) throws Exception{
    UIFormMultiValueInputSet uiMulti = createUIComponent(UIFormMultiValueInputSet.class, null, null) ;
    uiMulti.setId(name) ;
    uiMulti.setName(name) ;
    uiMulti.setType(type) ;
    addUIFormInput(uiMulti) ;
    if(label != null) uiMulti.setLabel(label) ;
    renderField(name) ;
  }

  private void executeScript(String script, Object o, String[] params) throws Exception{
    ScriptService scriptService = getApplicationComponent(ScriptService.class) ;
    try {
      CmsScript dialogScript = scriptService.getScript(script, repositoryName_);
      if(params != null) {
        if(params.equals(REPOSITORY)) params = new String[] { repositoryName_ } ; 
        dialogScript.setParams(params);
      }
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
        w.write("<a style=\"cursor:pointer;\" "
            + "onclick=\"javascript:eXo.webui.UIForm.submitEvent('" 
            + "" + getId() +"','ShowComponent','&objectId="+ fieldName +"' )\"><img class='ActionIcon "+ iconClass +"' src=\"/eXoResources/skin/DefaultSkin/background/Blank.gif\" /></a>") ;
      } 
    }
  }

  public Node storeValue(Event event) throws Exception { return null ; }
  public void onchange(Event event) throws Exception {}

  static  public class SaveActionListener extends EventListener<DialogFormFields> {
    public void execute(Event<DialogFormFields> event) throws Exception {
      DialogFormFields dialogForm = event.getSource() ;
      String path = dialogForm.storedPath_ + "&workspaceName=" + dialogForm.workspaceName_ + 
      "&repository=" + dialogForm.repositoryName_;
      for(String interceptor : dialogForm.prevScriptInterceptor_) {
        String scriptPath = interceptor.split(";")[0] ;
        String type = interceptor.split(";")[1] ;
        if(type.equals("prev")) {
          dialogForm.executeScript(scriptPath, path, null) ;          
        } 
      }
      Node newNode = dialogForm.storeValue(event) ;
      if(newNode == null) return ;      
      path = newNode.getPath() + "&workspaceName=" + newNode.getSession().getWorkspace().getName() +
      "&repository=" + dialogForm.repositoryName_;
      for(String interceptor : dialogForm.postScriptInterceptor_) {
        String scriptPath = interceptor.split(";")[0] ;
        String type = interceptor.split(";")[1] ;
        if(type.equals("post")) {
          dialogForm.executeScript(scriptPath, path, null) ;          
        } 
      }      
    }
  }

  static  public class OnchangeActionListener extends EventListener<DialogFormFields> {
    public void execute(Event<DialogFormFields> event) throws Exception {      
      event.getSource().isOnchange_ = true ;
      event.getSource().onchange(event) ;
    }
  }
}