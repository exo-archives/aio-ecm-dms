/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.script;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.version.VersionHistory;

import org.exoplatform.ecm.jcr.model.VersionNode;
import org.exoplatform.services.cms.scripts.ScriptService;
import org.exoplatform.webui.component.UIComponent;
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
 * phamtuanchip@yahoo.de September 27, 2006 10:27:15 AM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl", 
    events = {
      @EventConfig(listeners = UIScriptForm.SaveActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIScriptForm.ChangeActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIScriptForm.RestoreActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIScriptForm.CancelActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIScriptForm.RefreshActionListener.class)
    }
)
public class UIScriptForm extends UIForm {
  
  final static public String FIELD_SELECT_VERSION = "selectVersion" ;
  final static public String FIELD_SCRIPT_CONTENT = "scriptContent" ;
  final static public String FIELD_SCRIPT_NAME = "scriptName" ;
  final static public String FIELD_ENABLE_VERSION = "enableVersion" ;
  final static public String MIX_VERSIONABLE = "mix:versionable" ;

  private List<String> listVersion = new ArrayList<String>() ;
  private boolean isAddNew_ = true ; 
  
  public UIScriptForm() throws Exception { 
    UIFormSelectBox versions = 
      new UIFormSelectBox(FIELD_SELECT_VERSION , FIELD_SELECT_VERSION, null) ;
    UIFormTextAreaInput contents = 
      new UIFormTextAreaInput(FIELD_SCRIPT_CONTENT , FIELD_SCRIPT_CONTENT, null) ;
    UIFormCheckBoxInput isVersion = 
      new UIFormCheckBoxInput<Boolean>(FIELD_ENABLE_VERSION , FIELD_ENABLE_VERSION, null) ;
    UIFormStringInput scriptName = 
      new UIFormStringInput(FIELD_SCRIPT_NAME, FIELD_SCRIPT_NAME, null) ;
    scriptName.addValidator(EmptyFieldValidator.class) ;
    versions.setOnChange("Change") ;
    versions.setRendered(false) ;    
    isVersion.setRendered(false) ;
    addUIFormInput(versions) ;
    addUIFormInput(contents) ;
    addUIFormInput(isVersion) ;
    addUIFormInput(scriptName) ;
  }

  private VersionNode getRootVersion(Node node) throws Exception{       
    VersionHistory vH = node.getVersionHistory() ;
    return (vH == null) ? null : new VersionNode(vH.getRootVersion()) ; 
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
// @TODO use comparator and collections for sort
  private List<SelectItemOption<String>> getVersionValues(Node node) throws Exception { 
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    List<VersionNode> children = getRootVersion(node).getChildren() ;
    listVersion.clear() ;
    List<String> versionList = getNodeVersions(children) ;
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

  public void update(Node script) throws Exception{
    reset() ;
    if(script != null) {
      isAddNew_ = false ;
      String scriptContent = script.getProperty("jcr:data").getString() ;
      getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setRendered(true) ;
      boolean isVersioned = script.isNodeType(MIX_VERSIONABLE) ;
      if(isVersioned) {
        getUIFormSelectBox(FIELD_SELECT_VERSION).setRendered(true) ;
        getUIFormSelectBox(FIELD_SELECT_VERSION).setOptions(getVersionValues(script)) ;         
        getUIFormSelectBox(FIELD_SELECT_VERSION).setValue(script.getBaseVersion().getName()) ;
        getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setEnable(false) ;
        getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setChecked(true) ;
        setActions(new String[]{"Save", "Restore", "Refresh", "Cancel"})  ;
      } else {
        getUIFormSelectBox(FIELD_SELECT_VERSION).setRendered(false) ;
        getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setEnable(true) ;
        getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setChecked(false) ;
        setActions( new String[]{"Save", "Refresh", "Cancel"}) ;
      }
      getUIStringInput(FIELD_SCRIPT_CONTENT).setValue(scriptContent) ;
      getUIStringInput(FIELD_SCRIPT_NAME).setValue(script.getName()) ;
      getUIStringInput(FIELD_SCRIPT_NAME).setEditable(false) ;
      return ;
    }
      isAddNew_ = true ;
      getUIFormSelectBox(FIELD_SELECT_VERSION).setRendered(false) ;
      getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setRendered(false) ;
      getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setChecked(false) ;
      getUIStringInput(FIELD_SCRIPT_NAME).setEditable(true) ;
      getUIStringInput(FIELD_SCRIPT_NAME).setValue(null) ;
      getUIFormTextAreaInput(FIELD_SCRIPT_CONTENT).setValue(null) ;
      setActions( new String[]{"Save", "Refresh", "Cancel"}) ;
  } 

  private UIScriptList getCurentList() {
    UIScriptManager sManager = getAncestorOfType(UIScriptManager.class) ;
    UIPopupWindow uiPopupWindow = getParent() ;
    UIComponent parent = uiPopupWindow.getParent() ;
    UIScriptList uiScriptList = null ;
    if(parent instanceof UIECMScripts) {
      uiScriptList = sManager.findComponentById(UIECMScripts.SCRIPTLIST_NAME) ;
    } else if(parent instanceof UICBScripts) {
      uiScriptList = sManager.findComponentById(UICBScripts.SCRIPTLIST_NAME) ; 
    }
    return uiScriptList ;
  }

  static public class SaveActionListener extends EventListener<UIScriptForm> {
    public void execute(Event<UIScriptForm> event) throws Exception {
      UIScriptForm uiForm = event.getSource() ;
      ScriptService scriptService = uiForm.getApplicationComponent(ScriptService.class) ;
      String name = uiForm.getUIStringInput(FIELD_SCRIPT_NAME).getValue() ;
      String content = uiForm.getUIFormTextAreaInput(FIELD_SCRIPT_CONTENT).getValue() ;
      if(content == null) content = "" ;
      UIScriptList curentList = uiForm.getCurentList() ;
      String namePrefix = curentList.getScriptCategory() ;
      boolean isEnableVersioning = 
        uiForm.getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).isChecked() ;
      if(uiForm.isAddNew_ || !isEnableVersioning) { 
        scriptService.addScript(namePrefix + "/" + name, content) ;
      } else {
        Node node = curentList.getScriptNode(name) ; 
        if(!node.isNodeType(MIX_VERSIONABLE)) node.addMixin(MIX_VERSIONABLE) ;
        else node.checkout() ;  
        scriptService.addScript(namePrefix + "/" + name, content) ;
        node.save() ;
        node.checkin() ;
      }
      curentList.refresh() ;
      curentList.setSelectedTab() ;
      UIScriptManager uiScriptManager = uiForm.getAncestorOfType(UIScriptManager.class) ;
      UIPopupWindow uiPopupWindow = uiForm.getAncestorOfType(UIPopupWindow.class) ;
      uiForm.reset() ;
      UIComponent comp = uiPopupWindow.getParent() ;
      if(comp instanceof UIECMScripts ) uiScriptManager.removeECMScripForm() ;
      else uiScriptManager.removeCBScripForm() ;
      uiPopupWindow.setShow(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupWindow) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(curentList) ;
    }
  }

  static public class RestoreActionListener extends EventListener<UIScriptForm> {
    public void execute(Event<UIScriptForm> event) throws Exception {
      UIScriptForm uiForm = event.getSource() ;
      String name = uiForm.getUIStringInput(FIELD_SCRIPT_NAME).getValue() ;
      UIScriptList curentList = uiForm.getCurentList() ;
      Node node = curentList.getScriptNode(name) ; 
      String vesion = uiForm.getUIFormSelectBox(FIELD_SELECT_VERSION).getValue() ;
      String baseVesion = node.getBaseVersion().getName() ;
      if(!vesion.equals(baseVesion)) { 
        /*UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
        Object[] args = {uiForm.getUIStringInput(FIELD_SELECT_VERSION).getValue()} ;
        app.addMessage(new ApplicationMessage("UIScriptForm.msg.version-restored", args)) ;*/
        node.checkout() ;
        node.restore(vesion, true) ;
        curentList.refresh() ;
        curentList.setSelectedTab() ;
      }
      UIScriptManager uiScriptManager = uiForm.getAncestorOfType(UIScriptManager.class) ;
      UIPopupWindow uiPopupWindow = uiForm.getAncestorOfType(UIPopupWindow.class) ;
      UIComponent comp = uiPopupWindow.getParent() ;
      if(comp instanceof UIECMScripts ) uiScriptManager.removeECMScripForm() ;
      else uiScriptManager.removeCBScripForm() ;
      uiPopupWindow.setShow(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupWindow) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(curentList) ;
    }
  }

  static public class ChangeActionListener extends EventListener<UIScriptForm> {
    public void execute(Event<UIScriptForm> event) throws Exception {
      UIScriptForm uiForm = event.getSource();
      String name = uiForm.getUIStringInput(FIELD_SCRIPT_NAME).getValue() ;
      Node node = uiForm.getCurentList().getScriptNode(name)  ;
      String version = uiForm.getUIFormSelectBox(FIELD_SELECT_VERSION).getValue() ; 
      String path = node.getVersionHistory().getVersion(version).getPath() ;           
      VersionNode versionNode = uiForm.getRootVersion(node).findVersionNode(path) ;
      Node frozenNode = versionNode.getVersion().getNode("jcr:frozenNode") ;    
      String scriptContent = frozenNode.getProperty("jcr:data").getString() ;
      uiForm.getUIFormTextAreaInput(FIELD_SCRIPT_CONTENT).setValue(scriptContent) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }

  static public class RefreshActionListener extends EventListener<UIScriptForm> {
    public void execute(Event<UIScriptForm> event) throws Exception {
      UIScriptForm uiForm = event.getSource() ;
      uiForm.update(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }

  static public class CancelActionListener extends EventListener<UIScriptForm> {
    public void execute(Event<UIScriptForm> event) throws Exception {
      UIScriptForm uiForm = event.getSource();
      uiForm.reset() ;
      UIScriptManager uiScriptManager = uiForm.getAncestorOfType(UIScriptManager.class) ;
      UIPopupWindow uiPopupWindow = uiForm.getAncestorOfType(UIPopupWindow.class) ;
      UIComponent comp = uiPopupWindow.getParent() ;
      if(comp instanceof UIECMScripts ) {
        uiScriptManager.removeECMScripForm() ;
      } else {
        uiScriptManager.removeCBScripForm() ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(comp) ;
    }
  }
}