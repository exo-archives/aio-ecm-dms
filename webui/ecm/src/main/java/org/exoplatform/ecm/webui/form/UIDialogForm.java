/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.ecm.webui.form;

import java.io.Writer;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.ecm.webui.form.field.UIFormActionField;
import org.exoplatform.ecm.webui.form.field.UIFormCalendarField;
import org.exoplatform.ecm.webui.form.field.UIFormHiddenField;
import org.exoplatform.ecm.webui.form.field.UIFormSelectBoxField;
import org.exoplatform.ecm.webui.form.field.UIFormTextAreaField;
import org.exoplatform.ecm.webui.form.field.UIFormTextField;
import org.exoplatform.ecm.webui.form.field.UIFormUploadField;
import org.exoplatform.ecm.webui.form.field.UIFormWYSIWYGField;
import org.exoplatform.ecm.webui.form.field.UIMixinField;
import org.exoplatform.ecm.webui.utils.DialogFormUtil;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.scripts.CmsScript;
import org.exoplatform.services.cms.scripts.ScriptService;
import org.exoplatform.services.ecm.fckconfig.FCKConfigService;
import org.exoplatform.services.ecm.fckconfig.FCKEditorContext;
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
import org.exoplatform.webui.form.wysiwyg.FCKEditorConfig;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 23, 2008  
 */
@SuppressWarnings("unused")
public class UIDialogForm extends UIForm {

  protected final static String SAVE_ACTION = "Save".intern();
  protected final static String CANCEL_ACTION = "Cancel".intern();
  protected static final  String[]  ACTIONS = { SAVE_ACTION, CANCEL_ACTION };

  public Map<String, Map> components = new HashMap<String, Map>();
  public Map<String, String> propertiesName_ = new HashMap<String, String>() ;
  public Map<String, String> fieldNames_ = new HashMap<String, String>() ;
  protected boolean isUpdateSelect_ = false ;
  protected String repositoryName_ = null ;
  protected boolean isRemovePreference_ = false ;
  protected boolean isShowingComponent_ = false;
  protected Map<String, Object> properties = new HashMap<String, Object>();
  private boolean isNotEditNode_ = false ;
  private boolean isNTFile_ = false ;
  private boolean isResetMultiField_ = false ;
  private boolean isOnchange_ = false ;
  private boolean isResetForm_ = false ;

  private String workspaceName_ = null ; 
  private String storedPath_ = null ;
  private String nodePath_ ;
  private String childPath_ ;
  private List<String> prevScriptInterceptor_ = new ArrayList<String>() ;
  private List<String> postScriptInterceptor_ = new ArrayList<String>() ;  
  private final String REPOSITORY = "repository";

  public UIDialogForm() throws Exception {}

  public void addActionField(String name,String label,String[] arguments) throws Exception {
    UIFormActionField formActionField = new UIFormActionField(name,label,arguments);    
    if(formActionField.useSelector()) {
      components.put(name, formActionField.getSelectorInfo()) ; 
    }
    String jcrPath = formActionField.getJcrPath();
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    setInputProperty(name, inputProperty) ;
    if(formActionField.isMultiValues()) {
      renderMultiValuesInput(UIFormStringInput.class,name,label) ;      
      return ;
    }    
    UIFormStringInput uiInput = findComponentById(name) ;
    if(uiInput == null) {
      uiInput = formActionField.createUIFormInput();            
      addUIFormInput(uiInput) ;
    }    
    String propertyName = getPropertyName(jcrPath);
    uiInput.setEditable(formActionField.isEditable()) ;
    propertiesName_.put(name, propertyName) ;
    fieldNames_.put(propertyName, name) ;
    Node node = getNode();
    if(node != null && !isShowingComponent_ && !isRemovePreference_) {
      if(jcrPath.equals("/node") && (!formActionField.isEditable() || formActionField.isEditableIfNull())) {
        uiInput.setValue(node.getName()) ;
        uiInput.setEditable(false) ;
      } else if(node.hasProperty(propertyName) && !isUpdateSelect_) {
        if(node.getProperty(propertyName).getDefinition().getRequiredType() == 
          PropertyType.REFERENCE) {
          String path = 
            getNodePathByUUID(node.getProperty(propertyName).getValue().getString()) ;
          uiInput.setValue(path) ;
        } else {
          uiInput.setValue(node.getProperty(propertyName).getValue().getString()) ;
        }
      } 
    }
    Node childNode = getChildNode();
    if(isNotEditNode_ && !isShowingComponent_ && !isRemovePreference_) {
      if(childNode != null) {
        uiInput.setValue(propertyName) ;
      } else if(childNode == null && jcrPath.equals("/node") && node != null) {
        uiInput.setValue(node.getName()) ;
      } else {
        uiInput.setValue(null) ;
      }
    }
    renderField(name) ;
  }
  public void addActionField(String name, String[] arguments) throws Exception { 
    addActionField(name,null,arguments);
  }

  public void addCalendarField(String name, String label, String[] arguments) throws Exception {
    UIFormCalendarField calendarField = new UIFormCalendarField(name,label,arguments);    
    String jcrPath = calendarField.getJcrPath();
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    setInputProperty(name, inputProperty) ;    
    if(calendarField.isMultiValues()) {
      renderMultiValuesInput(UIFormDateTimeInput.class,name,label) ;      
      return ;
    } 
    UIFormDateTimeInput uiDateTime = findComponentById(name) ;
    if(uiDateTime == null) {
      uiDateTime = calendarField.createUIFormInput();      
      addUIFormInput(uiDateTime) ;
    }
    uiDateTime.setDisplayTime(calendarField.isDisplayTime()) ;
    String propertyName = getPropertyName(jcrPath);
    propertiesName_.put(name, propertyName) ;
    fieldNames_.put(propertyName, name) ;
    Node  node = getNode();
    if(node != null && node.hasProperty(propertyName) && !isShowingComponent_ && !isRemovePreference_) {
      uiDateTime.setCalendar(node.getProperty(propertyName).getDate()) ;
    } 
    Node childNode = getChildNode();
    if(isNotEditNode_ && !isShowingComponent_ && !isRemovePreference_) {
      if(childNode != null) {        
        if(childNode.hasProperty(propertyName)) {
          if(childNode.getProperty(propertyName).getDefinition().isMultiple()) {
            Value[] values = childNode.getProperty(propertyName).getValues() ;
            //TODO this code is bad. A datetime input can show only for 1 value
            for(Value value : values) {
              uiDateTime.setCalendar(value.getDate()) ;
            }
          } else {
            uiDateTime.setCalendar(childNode.getProperty(propertyName).getValue().getDate());
          }
        }
      } else if(childNode == null && jcrPath.equals("/node") && node != null) {
        uiDateTime.setCalendar(node.getProperty(propertyName).getDate());
      } else {
        uiDateTime.setCalendar(new GregorianCalendar()) ;
      }
    }
    if(calendarField.isVisible()) renderField(name) ;
  }    

  public void addCalendarField(String name, String[] arguments) throws Exception {
    addCalendarField(name,null,arguments) ;
  }

  public void addHiddenField(String name, String[] arguments) throws Exception {
    UIFormHiddenField formHiddenField = new UIFormHiddenField(name,null,arguments);
    String jcrPath = formHiddenField.getJcrPath();
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    String defaultValue = formHiddenField.getDefaultValue();
    if(defaultValue != null && defaultValue.length() > 0) {
      inputProperty.setValue(defaultValue) ;
    }
    String nodetype = formHiddenField.getNodeType();
    String mixintype = formHiddenField.getMixinTypes();
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
        prevScriptInterceptor_.add(scriptPath) ;
      } else if(type.equals("post")){
        postScriptInterceptor_.add(scriptPath) ;
      }
    } 
  }

  public void addMixinField(String name,String label,String[] arguments) throws Exception {
    UIMixinField mixinField = new UIMixinField(name,label,arguments);
    String jcrPath = mixinField.getJcrPath();
    String nodetype = mixinField.getNodeType();
    String mixintype = mixinField.getMixinTypes();
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    if (nodetype != null || mixintype != null) {
      inputProperty.setType(JcrInputProperty.NODE);
      if(nodetype != null) inputProperty.setNodetype(nodetype);
      if(mixintype != null) inputProperty.setMixintype(mixintype);
    }
    setInputProperty(name, inputProperty) ;
    Node node = getNode();
    if(node != null && mixinField.isVisibleIfNotNull()) {
      UIFormStringInput uiMixin = findComponentById(name) ;
      if(uiMixin == null) {
        uiMixin = mixinField.createUIFormInput();        
        addUIFormInput(uiMixin) ;
      }
      uiMixin.setValue(node.getName()) ;
      uiMixin.setEditable(false) ;
      renderField(name) ; 
    }
  }
  public void addMixinField(String name, String[] arguments) throws Exception {
    addMixinField(name,null,arguments) ;
  }

  public void addSelectBoxField(String name, String label, String[] arguments) throws Exception {
    UIFormSelectBoxField formSelectBoxField = new UIFormSelectBoxField(name,label,arguments);           
    if(formSelectBoxField.isMultiValues()) {
      renderMultiValuesInput(UIFormSelectBox.class,name,label);
      return ;
    }
    String jcrPath = formSelectBoxField.getJcrPath();
    String editable = formSelectBoxField.getEditable();
    String onchange = formSelectBoxField.getOnchange();
    String defaultValue = formSelectBoxField.getDefaultValue();
    String options = formSelectBoxField.getOptions();
    String script = formSelectBoxField.getGroovyScript();
    List<SelectItemOption<String>> optionsList = new ArrayList<SelectItemOption<String>>();
    UIFormSelectBox uiSelectBox = findComponentById(name) ;
    if(uiSelectBox == null || isResetForm_) {
      uiSelectBox = new UIFormSelectBox(name, name, null);
      addUIFormInput(uiSelectBox) ;
      if (script != null) {
        try {
          String[] scriptParams = formSelectBoxField.getScriptParams();
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
    Node node = getNode();
    String propertyName = getPropertyName(jcrPath);
    //TODO code is smell.
    if(node != null && arrNodes.length == 4) childNode = node.getNode(arrNodes[2]) ;
    if(childNode != null) {
      uiSelectBox.setValue(childNode.getProperty(propertyName).getValue().getString()) ;
    } else {
      if(node != null && node.hasProperty(propertyName)) {
        if(node.getProperty(propertyName).getDefinition().isMultiple()) {
          uiSelectBox.setValue(node.getProperty(propertyName).getValues().toString()) ;
        } else if(formSelectBoxField.isOnchange() && isOnchange_) {
          uiSelectBox.setValue(uiSelectBox.getValue()) ;
        } else {
          uiSelectBox.setValue(node.getProperty(propertyName).getValue().getString()) ;      
        }
      }
    }
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    setInputProperty(name, inputProperty) ;    
    uiSelectBox.setEditable(formSelectBoxField.isEditable()) ;
    addUIFormInput(uiSelectBox) ;
    if(isNotEditNode_) {      
      Node child = getChildNode();
      if(child != null) 
        uiSelectBox.setValue(DialogFormUtil.getPropertyValueAsString(child,propertyName)) ; 
    }
    if(formSelectBoxField.isOnchange()) uiSelectBox.setOnChange("Onchange") ;
    renderField(name) ;   
  }

  public void addSelectBoxField(String name, String[] arguments) throws Exception {
    addSelectBoxField(name,null,arguments) ;
  }

  public void addTextAreaField(String name, String label, String[] arguments) throws Exception {
    UIFormTextAreaField formTextAreaField = new UIFormTextAreaField(name,label,arguments);            
    if(formTextAreaField.useSelector()) {
      components.put(name, formTextAreaField.getSelectorInfo()) ;
    }    
    String jcrPath = formTextAreaField.getJcrPath();
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    setInputProperty(name, inputProperty) ;
    if(formTextAreaField.isMultiValues()) {
      renderMultiValuesInput(UIFormDateTimeInput.class,name,label) ;      
      return ;
    }
    UIFormTextAreaInput uiTextArea = findComponentById(name) ;    
    if(uiTextArea == null) {
      uiTextArea = formTextAreaField.createUIFormInput();      
      addUIFormInput(uiTextArea) ;
    }
    if(uiTextArea.getValue() == null) uiTextArea.setValue(formTextAreaField.getDefaultValue()) ;    
    uiTextArea.setEditable(formTextAreaField.isEditable()) ;
    String propertyName = getPropertyName(jcrPath);
    propertiesName_.put(name, propertyName) ;
    fieldNames_.put(propertyName, name) ;
    Node node = getNode();
    if(node != null && !isShowingComponent_ && !isRemovePreference_) {
      String value = "";
      if(node.hasProperty(propertyName)) {
        value = node.getProperty(propertyName).getValue().getString() ;
      } else if(node.isNodeType("nt:file")) {
        Node jcrContentNode = node.getNode("jcr:content") ;
        if(jcrContentNode.hasProperty(propertyName)) {
          if(jcrContentNode.getProperty(propertyName).getDefinition().isMultiple()) {
            Value[] values = jcrContentNode.getProperty(propertyName).getValues() ;
            for(Value v : values) {
              value = value + v.getString() ;
            }
          } else {
            value = jcrContentNode.getProperty(propertyName).getValue().getString() ;
          }
        }
      }
      uiTextArea.setValue(value) ;
    } 
    if(isNotEditNode_ && !isShowingComponent_ && !isRemovePreference_) {
      Node childNode = getChildNode();
      if(node != null && node.hasNode("jcr:content") && childNode != null) {
        Node jcrContentNode = node.getNode("jcr:content") ;
        uiTextArea.setValue(jcrContentNode.getProperty("jcr:data").getValue().getString()) ;
      } else {
        if(childNode != null) {
          uiTextArea.setValue(propertyName) ;
        } else if(childNode == null && jcrPath.equals("/node") && node != null) {
          uiTextArea.setValue(node.getName()) ;
        } else {
          uiTextArea.setValue(null) ;
        }
      }
    }
    renderField(name) ;
  }

  public void addTextAreaField(String name, String[] arguments) throws Exception {
    addTextAreaField(name,null,arguments);
  }
  public void addTextField(String name, String label, String[] arguments) throws Exception {
    UIFormTextField formTextField = new UIFormTextField(name,label,arguments);
    String jcrPath = formTextField.getJcrPath();
    String mixintype = formTextField.getMixinTypes();
    String nodetype = formTextField.getNodeType();
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    setInputProperty(name, inputProperty) ;
    String propertyName = getPropertyName(jcrPath) ;
    if(mixintype != null) inputProperty.setMixintype(mixintype) ;
    if(jcrPath.equals("/node") && nodetype != null ) inputProperty.setNodetype(nodetype);
    properties.put(name, inputProperty) ;
    propertiesName_.put(name, propertyName) ;
    fieldNames_.put(propertyName, name) ;
    Node node = getNode();
    Node childNode = getChildNode();
    if(formTextField.isMultiValues()) {
      UIFormMultiValueInputSet uiMulti ;
      if(node == null &&childNode == null) {
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
      if(childNode != null) {
        if(childNode.hasProperty(propertyName)) {
          Value[] values = childNode.getProperty(propertyName).getValues() ;
          for(Value value : values) {
            valueList.add(value.getString()) ;
          }
          uiMulti.setValue(valueList) ;
        }
      }
      if(node != null && !isShowingComponent_ && !isRemovePreference_) {
        String propertyPath = jcrPath.substring("/node/".length()) ;
        if(node.hasProperty(propertyPath)) {
          Value[] values = node.getProperty(propertyPath).getValues() ;
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
      uiInput = formTextField.createUIFormInput();
      addUIFormInput(uiInput) ;      
    }
    if(uiInput.getValue() == null) uiInput.setValue(formTextField.getDefaultValue()) ;       
    else uiInput.setEditable(true) ;
    if(getNode() != null && !isShowingComponent_ && !isRemovePreference_) {
      if(jcrPath.equals("/node") && (!formTextField.isEditable() || formTextField.isEditableIfNull())) {
        Node parentNode = node.getParent() ;
        //TODO code is smell
        if(parentNode != null && parentNode.getName().equals("languages")) {
          uiInput.setValue(node.getParent().getParent().getName()) ;
        } else {
          String nameValue =  node.getPath().substring(node.getPath().lastIndexOf("/") + 1) ;
          uiInput.setValue(nameValue) ;
        }
        uiInput.setEditable(false) ;
      } else if(node.hasProperty(propertyName)) {
        uiInput.setValue(node.getProperty(propertyName).getValue().getString()) ;
      } 
    }
    if(isNotEditNode_ && !isShowingComponent_ && !isRemovePreference_) {
      if(childNode != null) {
        uiInput.setValue(propertyName) ;
      } else if(childNode == null && jcrPath.equals("/node") && node != null) {
        uiInput.setValue(node.getName()) ;
      } else {
        uiInput.setValue(formTextField.getDefaultValue()) ;
      }
    }
    renderField(name) ;
  }

  public void addTextField(String name, String[] arguments) throws Exception {
    addTextField(name,null,arguments);
  } ;

  public void addUploadField(String name,String label,String[] arguments) throws Exception {
    UIFormUploadField formUploadField = new UIFormUploadField(name,label,arguments);
    String jcrPath = formUploadField.getJcrPath();
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);
    setInputProperty(name, inputProperty) ;
    setMultiPart(true) ;
    if(formUploadField.isMultiValues()) {
      renderMultiValuesInput(UIFormUploadInput.class,name,label) ;      
      return ;
    }    
    UIFormUploadInput uiInputUpload = findComponentById(name) ;
    if(uiInputUpload == null) {
      uiInputUpload = formUploadField.createUIFormInput();      
      addUIFormInput(uiInputUpload) ;
    }
    String propertyName = getPropertyName(jcrPath);
    propertiesName_.put(name, propertyName) ;
    fieldNames_.put(propertyName, name) ;
    renderField(name) ;
  }

  public void addUploadField(String name, String[] arguments) throws Exception {
    addUploadField(name,null,arguments) ;
  }
  public void addWYSIWYGField(String name, String label, String[] arguments) throws Exception {
    UIFormWYSIWYGField formWYSIWYGField = new UIFormWYSIWYGField(name,label,arguments);
    String jcrPath = formWYSIWYGField.getJcrPath();
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(jcrPath);       
    setInputProperty(name, inputProperty) ;
    if(formWYSIWYGField.isMultiValues()) {
      //TODO need add FCKEditorConfig for the service
      renderMultiValuesInput(UIFormWYSIWYGInput.class,name,label);      
      return ;
    }            
    UIFormWYSIWYGInput wysiwyg = findComponentById(name) ;
    if(wysiwyg == null) {
      wysiwyg = formWYSIWYGField.createUIFormInput();      
    }                 
    /**
     * Broadcast some info about current node by FCKEditorConfig Object
     * FCKConfigService used to allow add custom config for fckeditor from service
     * */
    FCKEditorConfig config = new FCKEditorConfig();
    FCKEditorContext editorContext = new FCKEditorContext();
    if(repositoryName_ != null) {        
      config.put("repositoryName",repositoryName_);
      editorContext.setRepository(repositoryName_);
    }
    if(workspaceName_ != null) {
      config.put("workspaceName",workspaceName_);
      editorContext.setWorkspace(workspaceName_);
    }
    if(nodePath_ != null) {
      config.put("jcrPath",nodePath_);
      editorContext.setCurrentNodePath(nodePath_);
    }else {
      config.put("jcrPath",storedPath_);
      editorContext.setCurrentNodePath(storedPath_);                                  
    }
    FCKConfigService fckConfigService = getApplicationComponent(FCKConfigService.class);
    editorContext.setPortalName(Util.getUIPortal().getName());
    editorContext.setSkinName(Util.getUIPortalApplication().getSkin());
    fckConfigService.processFCKEditorConfig(config,editorContext);      
    wysiwyg.setFCKConfig(config);    
    addUIFormInput(wysiwyg) ;
    if(wysiwyg.getValue() == null) wysiwyg.setValue(formWYSIWYGField.getDefaultValue()) ;
    String propertyName = getPropertyName(jcrPath);
    propertiesName_.put(name, propertyName) ;
    fieldNames_.put(propertyName, name) ;
    Node node = getNode();

    if(!isShowingComponent_ && !isRemovePreference_) {
      if(node != null && (node.isNodeType("nt:file") || isNTFile_)) {
        Node jcrContentNode = node.getNode("jcr:content") ;
        wysiwyg.setValue(jcrContentNode.getProperty("jcr:data").getValue().getString()) ;
      } else {
        if(node != null && node.hasProperty(propertyName)) {
          wysiwyg.setValue(node.getProperty(propertyName).getValue().getString()) ;
        }
      }
    }
    if(isNotEditNode_ && !isShowingComponent_ && !isRemovePreference_) {
      Node childNode = getChildNode();
      if(node != null && node.hasNode("jcr:content") && childNode != null) {
        Node jcrContentNode = node.getNode("jcr:content") ;
        wysiwyg.setValue(jcrContentNode.getProperty("jcr:data").getValue().getString()) ;
      } else {
        if(childNode != null) {
          wysiwyg.setValue(propertyName) ;
        } else if(childNode == null && jcrPath.equals("/node") && node != null) {
          wysiwyg.setValue(node.getName()) ;
        } else {
          wysiwyg.setValue(null) ;
        }
      }
    }
    renderField(name) ;
  }

  public void addWYSIWYGField(String name, String[] arguments) throws Exception {
    addWYSIWYGField(name,null,arguments);
  }

  @Override
  public void processAction(WebuiRequestContext context) throws Exception {       
    String action =  context.getRequestParameter(UIForm.ACTION) ;    
    if(SAVE_ACTION.equalsIgnoreCase(action)) {
      executePreSaveEventInterceptor();
      super.processAction(context);
      String nodePath = (String)context.getAttribute("nodePath");
      if(nodePath != null) {
        executePostSaveEventInterceptor(nodePath); 
     }
      prevScriptInterceptor_.clear();
      postScriptInterceptor_.clear();
    }else {
      super.processAction(context); 
    }    
  } 

  private void executePreSaveEventInterceptor() throws Exception {
    if(prevScriptInterceptor_.size()>0) {
      Map<String,JcrInputProperty> maps = DialogFormUtil.prepareMap(this.getChildren(),getInputProperties());    
      for(String interceptor : prevScriptInterceptor_) {              
        this.executeScript(interceptor, maps, null) ;                
      } 
    }           
  }

  private void executePostSaveEventInterceptor(String nodePath) throws Exception {
    if(postScriptInterceptor_.size()>0) {
      String path = nodePath + "&workspaceName=" + this.workspaceName_ + "&repository=" + this.repositoryName_;
      for(String interceptor : postScriptInterceptor_) {              
        this.executeScript(interceptor, path, null) ;                
      }      
    }    
  }

  public Node getChildNode() throws Exception { 
    if(childPath_ == null) return null ;
    return (Node) getSesssion().getItem(childPath_) ; 
  }

  public Map<String, Object> getInputProperties() { return properties ; }

  public Object getInputProperty(String name) { return properties.get(name) ; }

  public Node getNode() throws Exception { 
    if(nodePath_ == null) return null ;
    return (Node) getSesssion().getItem(nodePath_) ; 
  }

  public String getPropertyName(String jcrPath) { 
    return jcrPath.substring(jcrPath.lastIndexOf("/") + 1) ; 
  }

  public String getSelectBoxFieldValue(String name) {
    UIFormSelectBox uiSelectBox = findComponentById(name) ;
    if (uiSelectBox != null) return uiSelectBox.getValue() ;
    return null ;
  }

  public Session getSesssion() throws Exception {
    return SessionProviderFactory.createSessionProvider().getSession(workspaceName_, getRepository()) ;
  }

  public boolean isResetForm() { return isResetForm_ ; }

  public void onchange(Event event) throws Exception {}

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

  public void resetProperties() { properties.clear() ; }

  public void setChildPath(String childPath) { childPath_ = childPath ; }

  public void setInputProperty(String name, Object value) { properties.put(name, value) ; }

  public void setIsNotEditNode(boolean isNotEditNode) { isNotEditNode_ = isNotEditNode ; }    

  public void setIsNTFile(boolean isNTFile) { isNTFile_ = isNTFile ; }

  public void setIsOnchange(boolean isOnchange) { isOnchange_ = isOnchange ; }

  public void setIsResetForm(boolean isResetForm) { isResetForm_ = isResetForm ; }

  public void setIsResetMultiField(boolean isResetMultiField) { 
    isResetMultiField_ = isResetMultiField ; 
  }

  public void setIsUpdateSelect(boolean isUpdateSelect) { isUpdateSelect_ = isUpdateSelect ; }      

  public void setNodePath(String nodePath) { nodePath_ = nodePath ; }

  public void setRepositoryName(String repositoryName){ repositoryName_ = repositoryName ; }  

  public void setStoredPath(String storedPath) { storedPath_ = storedPath ; }

  public void setWorkspace(String workspace) { workspaceName_ = workspace ; }  

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

  private String getNodePathByUUID(String uuid) throws Exception{
    String[] workspaces = getRepository().getWorkspaceNames() ;
    Node node = null ;
    for(String ws : workspaces) {
      try{
        node = SessionProviderFactory.createSystemProvider().getSession(ws, getRepository()).getNodeByUUID(uuid) ;
        return ws + ":" + node.getPath() ;
      } catch(Exception e) {
        continue ;
      }      
    }
    return null;
  }  

  private ManageableRepository getRepository() throws Exception{         
    RepositoryService repositoryService  = getApplicationComponent(RepositoryService.class) ;      
    return repositoryService.getRepository(repositoryName_);
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

  static  public class OnchangeActionListener extends EventListener<UIDialogForm> {
    public void execute(Event<UIDialogForm> event) throws Exception {      
      event.getSource().isOnchange_ = true ;
      event.getSource().onchange(event) ;
    }
  }  
}