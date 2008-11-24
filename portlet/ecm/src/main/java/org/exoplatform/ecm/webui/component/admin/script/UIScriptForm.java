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
package org.exoplatform.ecm.webui.component.admin.script;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.version.VersionHistory;

import org.exoplatform.ecm.jcr.model.VersionNode;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.ecm.webui.component.admin.script.UIScriptList.ScriptData;
import org.exoplatform.ecm.webui.popup.UIPopupComponent;
import org.exoplatform.ecm.webui.popup.UIPopupContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.scripts.ScriptService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SARL 
 * Author : pham tuan
 * phamtuanchip@yahoo.de September 27, 2006 10:27:15 AM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl", 
    events = {
      @EventConfig(listeners = UIScriptForm.SaveActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIScriptForm.ChangeActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIScriptForm.RestoreActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIScriptForm.CancelActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIScriptForm.RefreshActionListener.class)
    }
)
public class UIScriptForm extends UIForm implements UIPopupComponent {

  final static public String FIELD_SELECT_VERSION = "selectVersion" ;
  final static public String FIELD_SCRIPT_CONTENT = "scriptContent" ;
  final static public String FIELD_SCRIPT_NAME = "scriptName" ;
  final static public String FIELD_ENABLE_VERSION = "enableVersion" ;
  final static public String SCRIPT_FILE_TYPE = ".groovy" ;

  private List<String> listVersion = new ArrayList<String>() ;
  private boolean isAddNew_ = true ; 

  public UIScriptForm() throws Exception { 
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    UIFormSelectBox versions = 
      new UIFormSelectBox(FIELD_SELECT_VERSION , FIELD_SELECT_VERSION, options) ;
    UIFormTextAreaInput contents = 
      new UIFormTextAreaInput(FIELD_SCRIPT_CONTENT , FIELD_SCRIPT_CONTENT, null) ;
    contents.addValidator(MandatoryValidator.class) ;
    UIFormCheckBoxInput isVersion = 
      new UIFormCheckBoxInput<Boolean>(FIELD_ENABLE_VERSION , FIELD_ENABLE_VERSION, null) ;
    UIFormStringInput scriptName = 
      new UIFormStringInput(FIELD_SCRIPT_NAME, FIELD_SCRIPT_NAME, null) ;
    scriptName.addValidator(MandatoryValidator.class) ;
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
    return (vH == null) ? null : new VersionNode(vH.getRootVersion(), node.getSession()) ; 
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
//@TODO use comparator and collections for sort
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

  public void update(Node script, boolean isAddNew) throws Exception{
    isAddNew_ = isAddNew ;
    if(script != null) {
      String scriptContent = script.getProperty("jcr:data").getString() ;
      getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setRendered(true) ;
      boolean isVersioned = script.isNodeType(Utils.MIX_VERSIONABLE) ;
      if(isVersioned) {
        getUIFormSelectBox(FIELD_SELECT_VERSION).setRendered(true) ;
        getUIFormSelectBox(FIELD_SELECT_VERSION).setOptions(getVersionValues(script)) ;         
        getUIFormSelectBox(FIELD_SELECT_VERSION).setValue(script.getBaseVersion().getName()) ;
        getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setEnable(true) ;
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
    if(!isAddNew_) {
      getUIFormTextAreaInput(FIELD_SCRIPT_CONTENT).setValue(null) ;
      return ;
    }
    getUIFormSelectBox(FIELD_SELECT_VERSION).setRendered(false) ;
    getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setRendered(false) ;
    getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setChecked(false) ;
    getUIStringInput(FIELD_SCRIPT_NAME).setEditable(true) ;
    getUIStringInput(FIELD_SCRIPT_NAME).setValue(null) ;
    getUIFormTextAreaInput(FIELD_SCRIPT_CONTENT).setValue(null) ;
    setActions( new String[]{"Save", "Refresh", "Cancel"}) ;
  } 

  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  static public class SaveActionListener extends EventListener<UIScriptForm> {
    public void execute(Event<UIScriptForm> event) throws Exception {
      UIScriptForm uiForm = event.getSource() ;
      String repository = uiForm.getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
      ScriptService scriptService = uiForm.getApplicationComponent(ScriptService.class) ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      String name = uiForm.getUIStringInput(FIELD_SCRIPT_NAME).getValue() ;
      String content = uiForm.getUIFormTextAreaInput(FIELD_SCRIPT_CONTENT).getValue() ;
      if(content == null) content = "" ;
      if(name == null || name.trim().length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIScriptForm.msg.name-null", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String[] arrFilterChar = {"&", "$", "@", ":","]", "'", "[", "*", "%", "!"} ;
      for(String filterChar : arrFilterChar) {
        if(name.indexOf(filterChar) > -1) {
          uiApp.addMessage(new ApplicationMessage("UIScriptForm.msg.fileName-invalid", null, 
                                                  ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      if(!name.contains(SCRIPT_FILE_TYPE)) name = name + SCRIPT_FILE_TYPE ;
      UIScriptList curentList = null ;
      UIScriptManager uiManager = uiForm.getAncestorOfType(UIScriptManager.class) ;
      List<String> listScript = new ArrayList<String>() ;
      List<ScriptData> scriptDatas = new ArrayList<ScriptData>() ;
      String namePrefix = null ;
      if(uiForm.getId().equals(UIECMScripts.SCRIPTFORM_NAME)) {
        curentList = uiManager.findComponentById(UIECMScripts.SCRIPTLIST_NAME);
        UIECMScripts uiEScripts = uiManager.getChild(UIECMScripts.class) ;
        namePrefix = curentList.getScriptCategory() ; 
        String subNamePrefix = 
          namePrefix.substring(namePrefix.lastIndexOf("/") + 1, namePrefix.length()) ;
        scriptDatas = uiEScripts.getECMScript(subNamePrefix) ;
      } else if(uiForm.getId().equals(UICBScripts.SCRIPTFORM_NAME)){
        curentList = uiManager.findComponentById(UICBScripts.SCRIPTLIST_NAME);
        UICBScripts uiCBScripts = uiManager.getChild(UICBScripts.class) ;
        namePrefix = curentList.getScriptCategory() ;
        scriptDatas = uiCBScripts.getCBScript() ;
      }
      for(ScriptData data : scriptDatas) {
        listScript.add(data.getName()) ;
      }
      if(listScript.contains(name) && uiForm.isAddNew_) {
        Object[] args = { name } ;
        uiApp.addMessage(new ApplicationMessage("UIScriptForm.msg.name-exist", args, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      boolean isEnableVersioning = 
        uiForm.getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).isChecked() ;
      if(uiForm.isAddNew_ || !isEnableVersioning) { 
        try {
          scriptService.addScript(namePrefix + "/" + name, content, repository, 
              SessionProviderFactory.createSessionProvider()) ;
        } catch(AccessDeniedException ace) {
          uiApp.addMessage(new ApplicationMessage("UIECMAdminControlPanel.msg.access-denied", null, 
                                                  ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;          
        }
      } else {
        Node node = curentList.getScriptNode(name) ; 
        if(!node.isNodeType(Utils.MIX_VERSIONABLE)) node.addMixin(Utils.MIX_VERSIONABLE) ;
        else node.checkout() ;  
        scriptService.addScript(namePrefix + "/" + name, content, repository, 
            SessionProviderFactory.createSessionProvider()) ;
        node.save() ;
        node.checkin() ;
      }
      uiForm.reset() ;
      UIPopupContainer uiPopupAction = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      curentList.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(curentList.getParent()) ;
    }
  }

  static public class RestoreActionListener extends EventListener<UIScriptForm> {
    public void execute(Event<UIScriptForm> event) throws Exception {
      UIScriptForm uiForm = event.getSource() ;
      String name = uiForm.getUIStringInput(FIELD_SCRIPT_NAME).getValue() ;
      UIScriptList uiScriptList = null ;
      UIScriptManager uiManager = uiForm.getAncestorOfType(UIScriptManager.class) ;
      if(uiForm.getId().equals(UIECMScripts.SCRIPTFORM_NAME)) {
        uiScriptList = uiManager.findComponentById(UIECMScripts.SCRIPTLIST_NAME);
      } else if(uiForm.getId().equals(UICBScripts.SCRIPTFORM_NAME)){
        uiScriptList = uiManager.findComponentById(UICBScripts.SCRIPTLIST_NAME);
      }
      Node node = uiScriptList.getScriptNode(name) ; 
      String vesion = uiForm.getUIFormSelectBox(FIELD_SELECT_VERSION).getValue() ;
      String baseVesion = node.getBaseVersion().getName() ;
      if(!vesion.equals(baseVesion)) { 
        node.checkout() ;
        node.restore(vesion, true) ;
        uiScriptList.refresh() ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiScriptList) ;
      UIPopupContainer uiPopupAction = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class ChangeActionListener extends EventListener<UIScriptForm> {
    public void execute(Event<UIScriptForm> event) throws Exception {
      UIScriptForm uiForm = event.getSource();
      String name = uiForm.getUIStringInput(FIELD_SCRIPT_NAME).getValue() ;
      UIScriptList uiScript = null ;
      UIScriptManager uiManager = uiForm.getAncestorOfType(UIScriptManager.class) ;
      if(uiForm.getId().equals(UIECMScripts.SCRIPTFORM_NAME)) {
        uiScript = uiManager.findComponentById(UIECMScripts.SCRIPTLIST_NAME) ;
      } else if(uiForm.getId().equals(UICBScripts.SCRIPTFORM_NAME)) {
        uiScript = uiManager.findComponentById(UICBScripts.SCRIPTLIST_NAME) ;
      }
      Node node = uiScript.getScriptNode(name)  ;
      String version = uiForm.getUIFormSelectBox(FIELD_SELECT_VERSION).getValue() ; 
      String path = node.getVersionHistory().getVersion(version).getPath() ;           
      VersionNode versionNode = uiForm.getRootVersion(node).findVersionNode(path) ;
      Node frozenNode = versionNode.getVersion().getNode(Utils.JCR_FROZEN) ;    
      String scriptContent = frozenNode.getProperty(Utils.JCR_DATA).getString() ;
      uiForm.getUIFormTextAreaInput(FIELD_SCRIPT_CONTENT).setValue(scriptContent) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupContainer.class)) ;
    }
  }

  static public class RefreshActionListener extends EventListener<UIScriptForm> {
    public void execute(Event<UIScriptForm> event) throws Exception {
      UIScriptForm uiForm = event.getSource() ;
      String sciptName = uiForm.getUIStringInput(UIScriptForm.FIELD_SCRIPT_NAME).getValue() ;
      if(uiForm.isAddNew_) {
        uiForm.update(null, true) ;
      } else {
        UIScriptManager uiScriptManager = uiForm.getAncestorOfType(UIScriptManager.class);
        UIScriptList uiScriptList ;
        if(uiForm.getId().equals(UIECMScripts.SCRIPTFORM_NAME)) {
          uiScriptList = uiScriptManager.findComponentById(UIECMScripts.SCRIPTLIST_NAME) ; 
        } else {
          uiScriptList = uiScriptManager.findComponentById(UICBScripts.SCRIPTLIST_NAME) ;
        }
        Node script = uiScriptList.getScriptNode(sciptName) ;  
        uiForm.update(script, false) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupContainer.class)) ;
    }
  }

  static public class CancelActionListener extends EventListener<UIScriptForm> {
    public void execute(Event<UIScriptForm> event) throws Exception {
      UIScriptForm uiForm = event.getSource();
      uiForm.reset() ;
      UIPopupContainer uiPopupAction = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
}