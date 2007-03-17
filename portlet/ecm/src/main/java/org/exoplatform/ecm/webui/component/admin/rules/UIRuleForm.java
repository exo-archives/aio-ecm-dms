/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.rules;

import java.util.ArrayList;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.version.VersionHistory;
import org.exoplatform.ecm.jcr.model.VersionNode;
import org.exoplatform.services.cms.rules.RuleService;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormCheckBoxInput;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIFormTextAreaInput;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.component.validator.EmptyFieldValidator;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL 
 * Author : pham tuan
 * phamtuanchip@yahoo.de September 22, 2006 04:27:15 PM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl", 
    events = {
      @EventConfig(listeners = UIRuleForm.SaveActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIRuleForm.ChangeActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIRuleForm.RestoreActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIRuleForm.CancelActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIRuleForm.RefreshActionListener.class)
    }
)

public class UIRuleForm extends UIForm {
  
  final static public String FIELD_SELECT_VERSION = "selectVersion" ;
  final static public String FIELD_RULE_CONTENT = "ruleContent" ;
  final static public String FIELD_RULE_NAME = "ruleName" ;
  final static public String FIELD_ENABLE_VERSION = "enableVersion" ;
  final static public String MIX_VERSIONABLE = "mix:versionable" ;
  
  private List<String> listVersion = new ArrayList<String>() ;
  private boolean isAddNew_ = false ; 
  
  public UIRuleForm() throws Exception { 
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    UIFormSelectBox versions = 
      new UIFormSelectBox(FIELD_SELECT_VERSION , FIELD_SELECT_VERSION, options) ;
    UIFormTextAreaInput contents = 
      new UIFormTextAreaInput(FIELD_RULE_CONTENT , FIELD_RULE_CONTENT, null) ;
    UIFormCheckBoxInput isVersion = 
      new UIFormCheckBoxInput<Boolean>(FIELD_ENABLE_VERSION , FIELD_ENABLE_VERSION, null) ;
    UIFormStringInput rule = new UIFormStringInput(FIELD_RULE_NAME, FIELD_RULE_NAME, null) ;
    rule.addValidator(EmptyFieldValidator.class) ;
    versions.setOnChange("Change") ;
    versions.setRendered(false) ;    
    isVersion.setRendered(false) ;
    addUIFormInput(versions) ;
    addUIFormInput(contents ) ;
    addUIFormInput(isVersion) ;
    addUIFormInput(rule) ;
  }
  
  private List<String> getNodeVersions(List<VersionNode> children) throws Exception {         
    List<VersionNode> child = new ArrayList<VersionNode>() ;
    for(int i = 0; i < children.size(); i ++){
      listVersion.add(children.get(i).getName());
      child = children.get(i).getChildren() ;
      if(!child.isEmpty()) getNodeVersions(child) ; 
    }           
    return listVersion ;
  }

  private VersionNode getRootVersion(Node node) throws Exception{       
    VersionHistory vH = node.getVersionHistory() ;
    return (vH == null) ? null : new VersionNode(vH.getRootVersion()) ; 
  }
  
  private List<SelectItemOption<String>> getVersionValues(Node node) throws Exception { 
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    List<VersionNode> children = getRootVersion(node).getChildren() ;
    listVersion.clear() ;
    List<String> versionList = getNodeVersions(children) ;
//    @TODO use java.util.Comparator and java.util.Collections for sort 
    for(int i = 0; i < versionList.size(); i++) {
      for(int j = i + 1; j < versionList.size(); j ++) {
        if(Integer.parseInt(versionList.get(j)) < Integer.parseInt(versionList.get(i))) {
          String temp = versionList.get(i) ;
          versionList.set(i, versionList.get(j))  ;
          versionList.set(j, temp) ;
        }
      }
      options.add(new SelectItemOption<String>(versionList.get(i), versionList.get(i))) ;
    }
    return options ;
  }

  public void update(String ruleName) throws Exception{
    reset() ;
    if(ruleName != null) {
      isAddNew_ = false ;
      getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setRendered(true) ;
      RuleService ruleService = getApplicationComponent(RuleService.class) ;
      Node node = ruleService.getRuleNode(ruleName) ;
      String ruleText = ruleService.getRuleAsText(ruleName) ;
      boolean isVersioned = node.isNodeType(MIX_VERSIONABLE) ;
      if(isVersioned) {
        getUIFormSelectBox(FIELD_SELECT_VERSION).setRendered(true) ;
        getUIFormSelectBox(FIELD_SELECT_VERSION).setOptions(getVersionValues(node)) ;         
        getUIFormSelectBox(FIELD_SELECT_VERSION).setValue(node.getBaseVersion().getName()) ;
        getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setEnable(false) ;
        getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setChecked(true) ;
        setActions(new String[]{"Save", "Restore", "Refresh", "Cancel"})  ;
      } else {
        getUIFormSelectBox(FIELD_SELECT_VERSION).setRendered(false) ;
        getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setEnable(true) ;
        getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setChecked(false) ;
        setActions( new String[]{"Save", "Refresh", "Cancel"}) ;
      }
      getUIStringInput(FIELD_RULE_CONTENT).setValue(ruleText) ;
      getUIStringInput(FIELD_RULE_NAME).setValue(ruleName) ;
      getUIStringInput(FIELD_RULE_NAME).setEditable(false) ;
      return ;
    }
    isAddNew_ = true ;
    getUIFormSelectBox(FIELD_SELECT_VERSION).setRendered(false) ;
    getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setRendered(false) ;
    getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setChecked(false) ;
    getUIStringInput(FIELD_RULE_NAME).setEditable(true) ;
    getUIStringInput(FIELD_RULE_NAME).setValue(null) ;
    getUIFormTextAreaInput(FIELD_RULE_CONTENT).setValue(null) ;
    setActions( new String[]{"Save", "Refresh", "Cancel"}) ;
  }  
  
  static public class SaveActionListener extends EventListener<UIRuleForm> {
    public void execute(Event<UIRuleForm> event) throws Exception {
      UIRuleForm uiForm = event.getSource() ;
      RuleService ruleService = uiForm.getApplicationComponent(RuleService.class) ;
      String name = uiForm.getUIStringInput(FIELD_RULE_NAME).getValue() ;
      String content = uiForm.getUIFormTextAreaInput(FIELD_RULE_CONTENT).getValue() ;
      if(content == null) content = "" ;
      boolean isEnableVersioning = 
        uiForm.getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).isChecked() ;
      if(uiForm.isAddNew_ || !isEnableVersioning) {
        ruleService.addRule(name, content) ;
      } else {
        Node node = ruleService.getRuleNode(name) ;
        if(!node.isNodeType(MIX_VERSIONABLE)) node.addMixin(MIX_VERSIONABLE) ;
        else node.checkout() ;  
        ruleService.addRule(name, content) ;
        node.save() ;
        node.checkin() ;
      }
      UIRuleManager uiRuleManager = uiForm.getAncestorOfType(UIRuleManager.class) ;
      uiRuleManager.refresh() ;
      uiForm.reset() ;
      uiRuleManager.removeChild(UIPopupWindow.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiRuleManager) ;
    }
  }
  
  static public class RestoreActionListener extends EventListener<UIRuleForm> {
    public void execute(Event<UIRuleForm> event) throws Exception {
      UIRuleForm uiForm = event.getSource() ;
      UIRuleManager uiRuleManager = uiForm.getAncestorOfType(UIRuleManager.class) ;
      RuleService ruleService = uiForm.getApplicationComponent(RuleService.class) ;
      String name = uiForm.getUIStringInput(FIELD_RULE_NAME).getValue();
      Node node = ruleService.getRuleNode(name); 
      String vesion = uiForm.getUIFormSelectBox(FIELD_SELECT_VERSION).getValue() ;
      String baseVesion = node.getBaseVersion().getName();
      if(!vesion.equals(baseVesion)) { 
        /*UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
        Object[] args = {uiForm.getUIStringInput(FIELD_SELECT_VERSION).getValue()} ;
        app.addMessage(  new ApplicationMessage("UIRuleForm.msg.version-restored", args)) ;*/
        node.checkout() ;
        node.restore(vesion, true) ;
        uiRuleManager.removeChild(UIPopupWindow.class) ;
        uiRuleManager.refresh() ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiRuleManager) ;
    }
  }
  
  static public class ChangeActionListener extends EventListener<UIRuleForm> {
    public void execute(Event<UIRuleForm> event) throws Exception {
     UIRuleForm uiForm = event.getSource();
     RuleService ruleService = uiForm.getApplicationComponent(RuleService.class) ;
     String name = uiForm.getUIStringInput(FIELD_RULE_NAME).getValue() ;
     Node node = ruleService.getRuleNode(name) ;
     String version = uiForm.getUIFormSelectBox(FIELD_SELECT_VERSION).getValue() ; 
     String path = node.getVersionHistory().getVersion(version).getPath() ;           
     VersionNode versionNode = uiForm.getRootVersion(node).findVersionNode(path) ;
     Node frozenNode = versionNode.getVersion().getNode("jcr:frozenNode") ;    
     String rule = frozenNode.getProperty("jcr:data").getString() ;
     uiForm.getUIFormTextAreaInput(FIELD_RULE_CONTENT).setValue(rule) ;
     event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
  
  static public class RefreshActionListener extends EventListener<UIRuleForm> {
    public void execute(Event<UIRuleForm> event) throws Exception {
     UIRuleForm uiForm = event.getSource() ;
     uiForm.update(null) ;
     event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
  
  static public class CancelActionListener extends EventListener<UIRuleForm> {
    public void execute(Event<UIRuleForm> event) throws Exception {
     UIRuleManager uiRuleManager = event.getSource().getAncestorOfType(UIRuleManager.class) ;
     uiRuleManager.removeChild(UIPopupWindow.class) ;
     event.getRequestContext().addUIComponentToUpdateByAjax(uiRuleManager) ;
    }
  }
}