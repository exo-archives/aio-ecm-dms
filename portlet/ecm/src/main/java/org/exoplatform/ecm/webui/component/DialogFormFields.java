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

import java.io.InputStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.ecm.jcr.CronExpressionValidator;
import org.exoplatform.ecm.jcr.ECMNameValidator;
import org.exoplatform.ecm.jcr.RepeatCountValidator;
import org.exoplatform.ecm.jcr.RepeatIntervalValidator;
import org.exoplatform.ecm.utils.SessionsUtils;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.portal.webui.container.UIContainer;
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
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormInputBase;
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
  protected boolean isRemovePreference_ = false ;
  private String workspaceName_ = null ;
  private String storedPath_ = null ;
  protected String repositoryName_ = null ;
  private String nodePath_ ;
  private String childPath_ ;
  private String rootPath_;
  protected boolean isShowingComponent_ = false;
  private boolean dataRemoved_ = false;

  private List<String> prevScriptInterceptor_ = new ArrayList<String>() ; 
  private List<String> postScriptInterceptor_ = new ArrayList<String>() ;
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
  private static final String WORKSPACE_FIELD = "workspaceField" + SEPARATOR;
  private static final String SCRIPT = "script" + SEPARATOR;
  private static final String SCRIPT_PARAMS = "scriptParams" + SEPARATOR;
  private static final String MULTI_VALUES = "multiValues" + SEPARATOR;
  private static final String REPOSITORY = "repository";
  private static final String ROOTPATH = "rootPath" + SEPARATOR;

  public static final  String[]  ACTIONS = {"Save", "Cancel"};

  protected Map<String, Object> properties = new HashMap<String, Object>();

  public DialogFormFields() throws Exception {}

  public Node getNode() throws Exception { 
    if(nodePath_ == null) return null ;
    return (Node) getSesssion().getItem(nodePath_) ; 
  }
  public void setNodePath(String nodePath) { nodePath_ = nodePath ; }
  
  public String getNodePath() { return nodePath_; }
  
  public boolean dataRemoved() { return dataRemoved_; }
  
  public void setDataRemoved(boolean dataRemoved) { dataRemoved_ = dataRemoved; }
  
  public Session getSesssion() throws Exception {
    return SessionsUtils.getSessionProvider().getSession(workspaceName_, getRepository()) ;
  }

  private ManageableRepository getRepository() throws Exception{         
    RepositoryService repositoryService  = getApplicationComponent(RepositoryService.class) ;      
    return repositoryService.getRepository(repositoryName_);
  }
  public void setRootPath(String rootPath){rootPath_ = rootPath;}
  public String getRootPath(){return rootPath_;}
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
  
  public String getImage(Node node, String nodeTypeName) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource dresource ;
    Node imageNode = node.getNode(nodeTypeName) ;    
    InputStream input = imageNode.getProperty(Utils.JCR_DATA).getStream() ;
    dresource = new InputStreamDownloadResource(input, "image") ;
    dresource.setDownloadName(node.getName()) ;
    return dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
  }

  private List<String> getPropertyValues(String jcrPath, Node node) throws Exception {
    String propertyName = getPropertyName(jcrPath) ;    
    if(node == null || !node.hasProperty(propertyName)) return null ;   
    Property property = node.getProperty(propertyName) ;    
    int valueType = property.getType();
    boolean isMultiple = property.getDefinition().isMultiple() ;
    if(!isMultiple) return null;    
    Value[] values = property.getValues();
    List<String> convertedValues = new ArrayList<String>() ;
    switch(valueType) {
    case PropertyType.STRING: //String      
      for(Value value:values) {
        convertedValues.add(value.getString()) ;
      }
      break ;    
    case PropertyType.LONG: // Long    
      for(Value value:values) {
        convertedValues.add(Long.toString(value.getLong())) ;
      }
      break;
    case PropertyType.DOUBLE: // Double
      for(Value value:values) {
        convertedValues.add(Double.toString(value.getDouble())) ;
      }
      break;
    case PropertyType.DATE: //Date
      for(Value value:values) {
        convertedValues.add(value.getDate().toString()) ;
      }
      break;
    case PropertyType.BOOLEAN: //Boolean
      for(Value value:values) {
        convertedValues.add(Boolean.toString(value.getBoolean())) ;
      }
      break;              
    case PropertyType.REFERENCE :
      Session session = node.getSession();
      for(Value value:values) {
        try {
          String nodePath = session.getNodeByUUID(value.getString()).getPath() ;          
          convertedValues.add(nodePath.substring(1)) ; 
        } catch (Exception e) {
          continue;
        }        
      }
      break;      
    }
    return convertedValues;
  }    
  
  private String getNodePathByUUID(String uuid) throws Exception{
    String[] workspaces = getRepository().getWorkspaceNames() ;
    Node node = null ;
    for(String ws : workspaces) {
      try{
        node = SessionsUtils.getSystemProvider().getSession(ws, getRepository()).getNodeByUUID(uuid) ;
        return ws + ":" + node.getPath() ;
      } catch(Exception e) {
        continue;
      }      
    }
    return null;
  }

  public void addActionField(String name, String[] arguments) throws Exception { 
    addActionField(name,null,arguments);
  }

  public void addActionField(String name,String label,String[] arguments) throws Exception {
    String editable = "true";
    String defaultValue = "";
    String jcrPath = null;
    String selectorAction = null;
    String selectorClass = null;
    String[] selectorParams = null;
    String workspaceField = null ;
    String selectorIcon = null ;
    String multiValues = null ;
    String validateType = null ;
    String params = null ;
    String rootPath = null;
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
        params = argument.substring(argument.indexOf(SEPARATOR) + 1);
        selectorParams = StringUtils.split(params, ",");
      }else if (argument.startsWith(WORKSPACE_FIELD)) {
        workspaceField = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(VALIDATE)) {
        validateType = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if(argument.startsWith(ROOTPATH)){
      rootPath = argument.substring(argument.indexOf(SEPARATOR) + 1);
      }else {
        defaultValue = argument;
      }
    }    
    if(selectorClass != null) { 
      Map<String, String> fieldPropertiesMap = new HashMap<String, String>() ;
      fieldPropertiesMap.put("selectorClass", selectorClass) ;
      fieldPropertiesMap.put("returnField", name) ;
      fieldPropertiesMap.put("selectorIcon", selectorIcon) ;
      fieldPropertiesMap.put("workspaceField", workspaceField) ;
      if(rootPath != null) fieldPropertiesMap.put("rootPath", rootPath) ;
      if(params != null) fieldPropertiesMap.put("selectorParams", params) ;
      components.put(name, fieldPropertiesMap) ;
    }
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    setInputProperty(name, inputProperty) ;
    if(multiValues != null && multiValues.equals("true")) {      
      UIFormMultiValueInputSet uiMulti ;      
      uiMulti = createUIComponent(UIFormMultiValueInputSet.class, null, null) ;
      uiMulti.setId(name) ;
      uiMulti.setName(name) ;
      uiMulti.setType(UIFormStringInput.class) ;              
      List<String> values = getPropertyValues(jcrPath,getNode()) ;      
      if(values != null) {        
        uiMulti.setValue(values) ;                
      }                                                          
      addUIFormInput(uiMulti) ;
      renderField(name) ;      
      return ;
    }    
    UIFormStringInput uiInput = findComponentById(name) ;
    if(uiInput == null) {
      uiInput = new UIFormStringInput(name, name, defaultValue) ;
      if(validateType != null) {
        if(validateType.equals("name")) {
          uiInput.addValidator(ECMNameValidator.class) ;
        } else if (validateType.equals("email")){
          uiInput.addValidator(EmailAddressValidator.class) ;
        } else if (validateType.equals("number")) {
          uiInput.addValidator(NumberFormatValidator.class) ;
        } else if (validateType.equals("empty")){
          uiInput.addValidator(MandatoryValidator.class) ;
        }        
      }
      if(label != null && label.length() != 0) {
        uiInput.setLabel(label);
      }
      addUIFormInput(uiInput) ;
    }
    if(editable.equals("false")) uiInput.setEditable(false) ;
    else uiInput.setEditable(true) ;
    propertiesName_.put(name, getPropertyName(jcrPath)) ;
    properties.put(name,inputProperty) ;
    fieldNames_.put(getPropertyName(jcrPath), name) ;
    if(getNode() != null && !isRemovePreference_) {
      if(jcrPath.equals("/node") && (editable.equals("false") || editable.equals("if-null"))) {
        uiInput.setValue(getNode().getName()) ;
        uiInput.setEditable(false) ;
      } else if(getNode().hasProperty(getPropertyName(jcrPath)) && !isUpdateSelect_) {
        if(getNode().getProperty(getPropertyName(jcrPath)).getDefinition().getRequiredType() == 
          PropertyType.REFERENCE) {
          String path = 
            getNodePathByUUID(getNode().getProperty(getPropertyName(jcrPath)).getValue().getString()) ;
          uiInput.setValue(path) ;
        } else {
          uiInput.setValue(getNode().getProperty(getPropertyName(jcrPath)).getValue().getString()) ;
        }
      } 
    }
    if(isNotEditNode_ && !isRemovePreference_) {
      if(getChildNode() != null) {        
        uiInput.setValue(getPropertyValue(jcrPath)) ;        
      } else if(getChildNode() == null && jcrPath.equals("/node") && getNode() != null) {
        uiInput.setValue(getNode().getName()) ;
      } else {
        if (!isUpdateSelect_) {
          uiInput.setValue(null);
        } else {
          isUpdateSelect_ =  false;
        }
        
      }
    }
    renderField(name) ;
  }

  public void addTextField(String name, String[] arguments) throws Exception {
    addTextField(name,null,arguments);
  }

  public void addTextField(String name, String label, String[] arguments) throws Exception {
    String editable = "true";
    String type = "text" ;
    String defaultValue = "";
    String jcrPath = null;
    String mixintype = null;
    String multiValues = null ;
    String validateType = null ;
    String nodetype = null;
    String rootPath = null;
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
      } else if(argument.startsWith(NODETYPE)){
        nodetype = argument.substring(argument.indexOf(SEPARATOR) + 1) ;
      } else if(argument.startsWith(ROOTPATH)){
      rootPath = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else {
            defaultValue = argument;
      }
    }
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    setInputProperty(name, inputProperty) ;
    String propertyName = getPropertyName(jcrPath) ;
    if(mixintype != null) inputProperty.setMixintype(mixintype) ;
    if(jcrPath.equals("/node") && nodetype != null ) inputProperty.setNodetype(nodetype);
    properties.put(name, inputProperty) ;
    propertiesName_.put(name, propertyName) ;
    fieldNames_.put(propertyName, name) ;
    if(multiValues != null && multiValues.equals("true")) {
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
        if(validateType.equals("name")) {
          uiInput.addValidator(ECMNameValidator.class) ;
        } else if (validateType.equals("email")){
          uiInput.addValidator(EmailAddressValidator.class) ;
        } else if (validateType.equals("number")) {
          uiInput.addValidator(NumberFormatValidator.class) ;
        } else if (validateType.equals("empty")){
          uiInput.addValidator(MandatoryValidator.class) ;
        } else if(validateType.equals("cronExpressionValidator")) {
          uiInput.addValidator(CronExpressionValidator.class) ;
        } else if(validateType.equals("repeatCountValidator")) {
          uiInput.addValidator(RepeatCountValidator.class) ;
        } else if(validateType.equals("repeatIntervalValidator")) {
          uiInput.addValidator(RepeatIntervalValidator.class) ;
        }
      }     
      if(label != null && label.length()!=0) {
        uiInput.setLabel(label);
      }
      addUIFormInput(uiInput) ;      
    }
    if(uiInput.getValue() == null) uiInput.setValue(defaultValue) ;
    if(type.equals("password")) uiInput.setType(UIFormStringInput.PASSWORD_TYPE) ;
    if(editable.equals("false")) uiInput.setEditable(false) ;
    else uiInput.setEditable(true) ;
    if(getNode() != null && !isShowingComponent_ && !isRemovePreference_) {      
      String[] arrNodes = jcrPath.split("/") ;      
      
//    update by quangld      
      if (arrNodes.length == 4) {
        Node childNode = null;        
        childNode = getNode().getNode(arrNodes[2]);
        if (childNode != null) {
          uiInput.setValue(childNode.getProperty(propertyName).getValue().getString());
        }
      } else {
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
    }
    if(isNotEditNode_ && !isShowingComponent_ && !isRemovePreference_) {
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
    String editable = "true";
    String defaultValue = "";
    String jcrPath = null;
    String selectorAction = null;
    String selectorClass = null;
    String multiValues = null ;
    String validateType = null ;
    String rootPath = null;
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
      } else if (argument.startsWith(VALIDATE)) {
        validateType = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if(argument.startsWith(ROOTPATH)){
        rootPath = argument.substring(argument.indexOf(SEPARATOR) + 1);
      }else {
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
      if(validateType != null) {
        if (validateType.equals("empty")){
          uiTextArea.addValidator(MandatoryValidator.class) ;
        }
      }     
      addUIFormInput(uiTextArea) ;
    }
    if(uiTextArea.getValue() == null) uiTextArea.setValue(defaultValue) ;
    if(editable.equals("false")) uiTextArea.setEditable(false) ;
    else uiTextArea.setEditable(true) ;
    propertiesName_.put(name, getPropertyName(jcrPath)) ;
    fieldNames_.put(getPropertyName(jcrPath), name) ;

    if(getNode() != null && !isShowingComponent_ && !isRemovePreference_) {
      String value = "";
      if(getNode().hasProperty(getPropertyName(jcrPath))) {
        value = getNode().getProperty(getPropertyName(jcrPath)).getValue().getString() ;
      } else if(getNode().isNodeType(Utils.NT_FILE)) {
        Node jcrContentNode = getNode().getNode(Utils.JCR_CONTENT) ;
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
    if(isNotEditNode_ && !isShowingComponent_ && !isRemovePreference_) {
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
    String options = null ;
    String defaultValue = "";
    String jcrPath = null;
    boolean isBasic = false ;
    String multiValues = null ;
    String validateType = null ;
    String rootPath = null;
    for(int i = 0; i < arguments.length; i++) {
      String argument = arguments[i];
      if (argument.startsWith(JCR_PATH)) {
        jcrPath = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(OPTIONS)) {
        options = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(MULTI_VALUES)) {
        multiValues = argument.substring(argument.indexOf(SEPARATOR) + 1);      
      } else if (argument.startsWith(VALIDATE)) {
        validateType = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if(argument.startsWith(ROOTPATH)){
        rootPath = argument.substring(argument.indexOf(SEPARATOR) + 1);
      }else {
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
    if(options != null) {
      if(options.equals("basic")) isBasic = true ;
      else isBasic = false ;
    }
    UIFormWYSIWYGInput wysiwyg = findComponentById(name) ;
    if(wysiwyg == null) {
      wysiwyg = new UIFormWYSIWYGInput(name, name, defaultValue, isBasic) ;
      if(validateType != null) {
        if (validateType.equals("empty")){
          wysiwyg.addValidator(MandatoryValidator.class) ;
        }
      }     
      addUIFormInput(wysiwyg) ;
    }
    if(wysiwyg.getValue() == null) wysiwyg.setValue(defaultValue) ;
    propertiesName_.put(name, getPropertyName(jcrPath)) ;
    fieldNames_.put(getPropertyName(jcrPath), name) ;
    if(!isShowingComponent_ && !isRemovePreference_) {
      if(getNode() != null && (getNode().isNodeType("nt:file") || isNTFile_)) {
        Node jcrContentNode = getNode().getNode("jcr:content") ;
        wysiwyg.setValue(jcrContentNode.getProperty("jcr:data").getValue().getString()) ;
      } else {
        if(getNode() != null && getNode().hasProperty(getPropertyName(jcrPath))) {
          wysiwyg.setValue(getNode().getProperty(getPropertyName(jcrPath)).getValue().getString()) ;
        }
      }
    }
    if(isNotEditNode_ && !isShowingComponent_ && !isRemovePreference_) {
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
    String jcrPath = null;
    String editable = "true";
    String onchange = "false" ;
    String defaultValue = "" ;
    String options = null;
    String script = null;
    String[] scriptParams = null;
    String multiValues = null ;
    String rootPath = null;
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
      }else if(argument.startsWith(ROOTPATH)){
      rootPath = argument.substring(argument.indexOf(SEPARATOR) + 1);
      }else {
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
    List<SelectItemOption<String>> optionsList = new ArrayList<SelectItemOption<String>>();
    UIFormSelectBox uiSelectBox = findComponentById(name) ;
    if(uiSelectBox == null || isResetForm_) {
      uiSelectBox = new UIFormSelectBox(name, name, null);
      addUIFormInput(uiSelectBox) ;
      if (script != null) {
        try {
          if(scriptParams[0].equals("repository")) scriptParams[0] = repositoryName_ ;
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
        } else if(onchange.equals("true") && isOnchange_) {
          uiSelectBox.setValue(uiSelectBox.getValue()) ;
        } else {
          uiSelectBox.setValue(getNode().getProperty(getPropertyName(jcrPath)).getValue().getString()) ;      
        }
      }
    }
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    setInputProperty(name, inputProperty) ;
    if(editable.equals("false")) uiSelectBox.setDisabled(false) ;
    else uiSelectBox.setEditable(true) ;
    addUIFormInput(uiSelectBox) ;
    if(isNotEditNode_) {
      if(getChildNode() != null) uiSelectBox.setValue(getPropertyValue(jcrPath)) ; 
    }
    if(onchange.equals("true")) uiSelectBox.setOnChange("Onchange") ;
    renderField(name) ;
  }

  public String getSelectBoxFieldValue(String name) {
    UIFormSelectBox uiSelectBox = findComponentById(name) ;
    if (uiSelectBox != null) return uiSelectBox.getValue() ;
    return null ;
  }

  public void addUploadField(String name, String[] arguments) throws Exception {
    String jcrPath = null;
    String multiValues = null ;
    for(String argument : arguments) {
      if (argument.startsWith(JCR_PATH)) {
        jcrPath = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if (argument.startsWith(MULTI_VALUES)) {
        multiValues = argument.substring(argument.indexOf(SEPARATOR) + 1);        
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
    propertiesName_.put(name, getPropertyName(jcrPath)) ;
    fieldNames_.put(getPropertyName(jcrPath), name) ;
    renderField(name) ;
  }

  public void addMixinField(String name, String[] arguments) throws Exception {
    String jcrPath = null;
    String nodetype = null;
    String mixintype = null;
    String defaultValue = "";
    String editable = "true";
    String visible = "true";
    String rootPath = null;
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
      } else if(argument.startsWith(ROOTPATH)){
      rootPath = argument.substring(argument.indexOf(SEPARATOR) + 1);
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
    if(getNode() != null && visible.equals("if-not-null")) {
      UIFormStringInput uiMixin = findComponentById(name) ;
      if(uiMixin == null) {
        uiMixin = new UIFormStringInput(name, name, defaultValue) ;
        addUIFormInput(uiMixin) ;
      }
      uiMixin.setValue(getNode().getName()) ;
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
    String validateType = null ;
    String rootPath = null;
    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss") ;
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
      } else if (argument.startsWith(VALIDATE)) {
        validateType = argument.substring(argument.indexOf(SEPARATOR) + 1);
      } else if(argument.startsWith(ROOTPATH)){
      rootPath = argument.substring(argument.indexOf(SEPARATOR) + 1);
      }  else {
        defaultValue = argument ;
      }
    }
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
      if(options != null && options.equals("displaytime")) {
        uiDateTime = new UIFormDateTimeInput(name, name, date) ;
      } else {
        uiDateTime = new UIFormDateTimeInput(name, name, date, false) ;
      }
      addUIFormInput(uiDateTime) ;
    }
    if(options != null && options.equals("displaytime")) uiDateTime.setDisplayTime(true) ;
    else uiDateTime.setDisplayTime(false) ;
    if(validateType != null) {
      if(validateType.equals("datetime")) {
        uiDateTime.addValidator(DateTimeValidator.class) ;
      }
    }
    propertiesName_.put(name, getPropertyName(jcrPath)) ;
    fieldNames_.put(getPropertyName(jcrPath), name) ;
    if(getNode() != null && getNode().hasProperty(getPropertyName(jcrPath)) && 
        !isShowingComponent_ && !isRemovePreference_) {
      uiDateTime.setCalendar(getNode().getProperty(getPropertyName(jcrPath)).getDate()) ;
    } 

    if(isNotEditNode_ && !isShowingComponent_ && !isRemovePreference_) {
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
    if(!visible.equals("false")) renderField(name) ;
  }
  
  public void addHiddenField(String name, String[] arguments) throws Exception {
    String jcrPath = null;
    String nodetype = null;
    String mixintype = null;
    String defaultValue = "";
    String editable = "true";
    String visible = "true";
    String rootPath = null;
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
      } else if(argument.startsWith(ROOTPATH)){
      rootPath = argument.substring(argument.indexOf(SEPARATOR) + 1);     
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
    if(scriptPath.length() > 0 && type.length() > 0){
      if(type.equals("prev")){
        prevScriptInterceptor_.add(scriptPath + ";" + type) ;
      } else if(type.equals("post")){
        postScriptInterceptor_.add(scriptPath + ";" + type) ;
      }
    } 
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
        w.write("<a style=\"cursor:pointer;\" "
            + "onclick=\"javascript:eXo.webui.UIForm.submitEvent('" 
            + "" + getId() +"','RemoveReference','&objectId="+ fieldName +"' )\"><img class='ActionIcon Remove16x16Icon' src=\"/eXoResources/skin/DefaultSkin/background/Blank.gif\" /></a>") ;
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
  
  
  //update by quangld
  public void removeComponent(String name) {
    if (!properties.isEmpty() && properties.containsKey(name)) {
      properties.remove(name);
      String jcrPath = propertiesName_.get(name);
      propertiesName_.remove(name);
      fieldNames_.remove(jcrPath);
      removeChildById(name);
    }
  }   
  
}