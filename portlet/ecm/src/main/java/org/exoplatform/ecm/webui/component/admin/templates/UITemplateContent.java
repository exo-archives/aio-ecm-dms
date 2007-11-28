/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.templates;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.version.VersionHistory;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.jcr.JCRResourceResolver;
import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.ecm.jcr.model.VersionNode;
import org.exoplatform.ecm.utils.SessionsUtils;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupWindow;
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

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@yahoo.de
 * Oct 03, 2006
 * 9:43:23 AM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UITemplateContent.SaveActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UITemplateContent.ChangeActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UITemplateContent.CancelActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UITemplateContent.RestoreActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UITemplateContent.RefreshActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UITemplateContent.AddPermissionActionListener.class)
    }
)
public class UITemplateContent extends UIForm implements UISelector {

  final static public String FIELD_SELECT_VERSION = "selectVersion" ;
  final static public String FIELD_CONTENT = "content" ;
  final static public String FIELD_NAME = "name" ;
  final static public String FIELD_VIEWPERMISSION = "viewPermission" ;
  final static public String FIELD_ENABLE_VERSION = "enableVersion" ;
  final static public String[] REG_EXPRESSION = {"[", "]", ":", "&", "%"} ;

  private boolean isDialog_  ;
  private boolean isAddNew_ = true ;
  private String nodeTypeName_ ;  
  private List<String> listVersion_ = new ArrayList<String>() ;

  final static public String TEMPLATE_PERMISSION = "TemplatePermission" ;

  public UITemplateContent() throws Exception { 
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    UIFormSelectBox versions = 
      new UIFormSelectBox(FIELD_SELECT_VERSION, FIELD_SELECT_VERSION, options) ;
    versions.setOnChange("Change") ;
    versions.setRendered(false) ;    
    addUIFormInput(versions) ;
    addUIFormInput(new UIFormTextAreaInput(FIELD_CONTENT, FIELD_CONTENT, null)) ;
    addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null)) ;
    UIFormCheckBoxInput isVersion = 
      new UIFormCheckBoxInput<Boolean>(FIELD_ENABLE_VERSION , FIELD_ENABLE_VERSION, null) ;
    isVersion.setRendered(false) ;
    addUIFormInput(isVersion) ;   
    UIFormInputSetWithAction uiActionTab = new UIFormInputSetWithAction("UITemplateContent") ;
    uiActionTab.addUIFormInput(new UIFormStringInput(FIELD_VIEWPERMISSION, FIELD_VIEWPERMISSION, null).setEditable(false)) ;
    uiActionTab.setActionInfo(FIELD_VIEWPERMISSION, new String[] {"AddPermission"}) ;
    addUIComponentInput(uiActionTab) ;
  }

  public void setIsDialog(boolean b) { isDialog_ = b ;}

  public void setNodeTypeName (String nodeType) {nodeTypeName_ = nodeType ;}

  public void update(String templateName) throws Exception {
    if(templateName != null) {
      isAddNew_ = false ;
      TemplateService templateService = getApplicationComponent(TemplateService.class) ;
      String repository = getRepository() ;
      String templateContent = templateService.getTemplate(isDialog_, nodeTypeName_, templateName, repository) ;
      Node template = 
        templateService.getTemplateNode(isDialog_, nodeTypeName_, templateName, repository,SessionsUtils.getSessionProvider()) ;      
      getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setRendered(true) ;
      String templateRole = 
        templateService.getTemplateRoles(isDialog_, nodeTypeName_, templateName, repository) ;
      boolean isVersioned = template.isNodeType(Utils.MIX_VERSIONABLE) ;
      if(isVersioned) {
        getUIFormSelectBox(FIELD_SELECT_VERSION).setRendered(true) ;
        getUIFormSelectBox(FIELD_SELECT_VERSION).setOptions(getVersionValues(template)) ;         
        getUIFormSelectBox(FIELD_SELECT_VERSION).setValue(template.getBaseVersion().getName()) ;
        getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setEnable(false) ;
        getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setChecked(true) ;
        setActions(new String[]{"Save", "Restore", "Refresh", "Cancel"}) ;
      } else {
        getUIFormSelectBox(FIELD_SELECT_VERSION).setRendered(false) ;
        getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setEnable(true) ;
        getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setChecked(false) ;
        setActions( new String[]{"Save", "Refresh", "Cancel"}) ;
      }
      String content = Utils.encodeHTML(templateContent)  ;
      getUIStringInput(FIELD_CONTENT).setValue(content) ;
      getUIStringInput(FIELD_NAME).setValue(template.getName()) ;
      getUIStringInput(FIELD_NAME).setEditable(false) ;
      getUIStringInput(FIELD_VIEWPERMISSION).setValue(templateRole) ;
      return ;
    } 
    isAddNew_ = true ;
    getUIFormSelectBox(FIELD_SELECT_VERSION).setRendered(false) ;
    getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).setRendered(false) ;
    getUIStringInput(FIELD_NAME).setEditable(true) ;
    setActions( new String[]{"Save", "Refresh", "Cancel"}) ;
  } 



  private void refresh() throws Exception {
    UIViewTemplate uiViewTemplate = getAncestorOfType(UIViewTemplate.class) ;
    uiViewTemplate.refresh() ;
    UIComponent parent = getParent() ;
    if(parent instanceof UIDialogTab) {
      uiViewTemplate.setRenderedChild(UIDialogTab.class) ;
    } else {
      uiViewTemplate.setRenderedChild(UIViewTab.class) ;
    }
    update(null) ; 
    reset() ;
  }

  private VersionNode getRootVersion(Node node) throws Exception{       
    VersionHistory vH = node.getVersionHistory() ;
    if(vH != null) return new VersionNode(vH.getRootVersion()) ; 
    return null ;
  }
  private List<String> getNodeVersions(List<VersionNode> children) throws Exception {         
    List<VersionNode> child = new ArrayList<VersionNode>() ;
    for(VersionNode version : children) {
      listVersion_.add(version.getName()) ;
      child = version.getChildren() ;
      if(!child.isEmpty()) getNodeVersions(child) ; 
    }           
    return listVersion_ ;
  }

  private List<SelectItemOption<String>> getVersionValues(Node node) throws Exception { 
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    List<VersionNode> children = getRootVersion(node).getChildren() ;
    listVersion_.clear() ;
    List<String> versionList = getNodeVersions(children) ;
    for(int i = 0; i < versionList.size(); i++) {
      for(int j = i + 1; j < versionList.size(); j ++) {
        if( Integer.parseInt(versionList.get(j)) < Integer.parseInt(versionList.get(i))) {
          String temp = versionList.get(i) ;
          versionList.set(i, versionList.get(j)) ;
          versionList.set(j, temp) ;
        }
      }
      options.add(new SelectItemOption<String>(versionList.get(i), versionList.get(i))) ;
    }
    return options ;
  }

  @SuppressWarnings("unused")
  public void updateSelect(String selectField, String value) {
    getUIStringInput(FIELD_VIEWPERMISSION).setValue(value) ;
    UITemplatesManager uiManager = getAncestorOfType(UITemplatesManager.class) ;
    uiManager.removeChildById(getId() + TEMPLATE_PERMISSION) ;
  }
  
  private String getRepository() {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
    return portletPref.getValue(Utils.REPOSITORY, "") ;
  }
  static public class RestoreActionListener extends EventListener<UITemplateContent> {
    public void execute(Event<UITemplateContent> event) throws Exception {
      UITemplateContent uiForm = event.getSource() ;
      UITemplatesManager uiManager = uiForm.getAncestorOfType(UITemplatesManager.class) ;
      String name = uiForm.getUIStringInput(FIELD_NAME).getValue() ;
      TemplateService templateService = uiForm.getApplicationComponent(TemplateService.class) ;
      Node node = templateService.getTemplateNode(uiForm.isDialog_,  uiForm.nodeTypeName_, 
          name, uiForm.getRepository(),SessionsUtils.getSessionProvider()) ;
      String vesion = uiForm.getUIFormSelectBox(FIELD_SELECT_VERSION).getValue() ;
      String baseVesion = node.getBaseVersion().getName() ;
      UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
      if(vesion.equals(baseVesion)) return ;
      node.checkout() ;
      node.restore(vesion, true) ;
      Object[] args = {uiForm.getUIStringInput(FIELD_SELECT_VERSION).getValue()} ;
      app.addMessage(new ApplicationMessage("UITemplateContent.msg.version-restored", args)) ; 
      uiForm.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(app.getUIPopupMessages()) ;
    }
  }

  static public class SaveActionListener extends EventListener<UITemplateContent> {
    public void execute(Event<UITemplateContent> event) throws Exception {
      UITemplateContent uiForm = event.getSource() ;
      String repository = uiForm.getRepository() ;
      UITemplatesManager uiManager = uiForm.getAncestorOfType(UITemplatesManager.class) ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      String name = uiForm.getUIStringInput(FIELD_NAME).getValue() ;
      if(name == null || name.trim().length() == 0) {
        Object[] args = { FIELD_NAME } ;
        uiApp.addMessage(new ApplicationMessage("ECMNameValidator.msg.empty-input", args, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(!Utils.isNameValid(name, UITemplateContent.REG_EXPRESSION)){
        uiApp.addMessage(new ApplicationMessage("UITemplateContent.msg.name-invalid", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String content = uiForm.getUIStringInput(FIELD_CONTENT).getValue() ;      
      if(content == null) content = "" ;
      UIFormInputSetWithAction permField = uiForm.getChildById("UITemplateContent") ;
      String role = permField.getUIStringInput(FIELD_VIEWPERMISSION).getValue() ;      
      if((role == null) || (role.trim().length() == 0)) {
        uiApp.addMessage(new ApplicationMessage("UITemplateContent.msg.roles-invalid", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIViewTemplate uiViewTemplate = uiForm.getAncestorOfType(UIViewTemplate.class) ;
      if(uiForm.getId().equals(UIDialogTab.DIALOG_FORM_NAME)) {
        UIDialogTab uiDialogTab = uiViewTemplate.getChild(UIDialogTab.class) ;
        if(uiDialogTab.getListDialog().contains(name) && uiForm.isAddNew_) {
          Object[] args = { name } ;
          uiApp.addMessage(new ApplicationMessage("UITemplateContent.msg.name-exist", args, 
                                                  ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      } else if(uiForm.getId().equals(UIViewTab.VIEW_FORM_NAME)) {
        UIViewTab uiViewTab = uiViewTemplate.getChild(UIViewTab.class) ;
        if(uiViewTab.getListView().contains(name) && uiForm.isAddNew_) {
          Object[] args = { name } ;
          uiApp.addMessage(new ApplicationMessage("UITemplateContent.msg.name-exist", args, 
                                                  ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      TemplateService templateService = uiForm.getApplicationComponent(TemplateService.class) ;
      boolean isEnableVersioning = 
        uiForm.getUIFormCheckBoxInput(FIELD_ENABLE_VERSION).isChecked() ;
      String path = null ;
      if(uiForm.isAddNew_ || !isEnableVersioning){
        path = templateService.addTemplate(uiForm.isDialog_, uiForm.nodeTypeName_, null, false, name, 
            new String[] {role},  content, repository) ;
      } else {
        Node node = 
          templateService.getTemplateNode(uiForm.isDialog_, uiForm.nodeTypeName_, name, repository,SessionsUtils.getSessionProvider()) ;
        if(!node.isNodeType(Utils.MIX_VERSIONABLE)) node.addMixin(Utils.MIX_VERSIONABLE) ;
        else node.checkout() ;            
        path = templateService.addTemplate(uiForm.isDialog_, uiForm.nodeTypeName_, null, false, name, 
            new String[] {role},  content, repository) ;
        node.save() ;
        node.checkin() ;
      }
      uiForm.refresh() ;
      JCRResourceResolver resourceResolver = new JCRResourceResolver(null, "exo:templateFile") ;
      org.exoplatform.groovyscript.text.TemplateService groovyService = 
        uiForm.getApplicationComponent(org.exoplatform.groovyscript.text.TemplateService.class) ;
      if(path != null) groovyService.invalidateTemplate(path, resourceResolver) ;
      uiForm.isAddNew_ = true ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }

  static public class ChangeActionListener extends EventListener<UITemplateContent> {
    public void execute(Event<UITemplateContent> event) throws Exception {
      UITemplateContent uiForm = event.getSource() ;
      UITemplatesManager uiManager = uiForm.getAncestorOfType(UITemplatesManager.class) ;
      String name = uiForm.getUIStringInput(FIELD_NAME).getValue() ;
      TemplateService templateService = uiForm.getApplicationComponent(TemplateService.class) ;
      Node node = templateService.getTemplateNode(uiForm.isDialog_, uiForm.nodeTypeName_, 
          name, uiForm.getRepository(),SessionsUtils.getSessionProvider()) ;
      String version = uiForm.getUIFormSelectBox(FIELD_SELECT_VERSION).getValue() ; 
      String path = node.getVersionHistory().getVersion(version).getPath() ;           
      VersionNode versionNode = uiForm.getRootVersion(node).findVersionNode(path) ;
      Node frozenNode = versionNode.getVersion().getNode(Utils.JCR_FROZEN) ;
      String content = frozenNode.getProperty(Utils.EXO_TEMPLATEFILE).getString() ;
      uiForm.getUIFormTextAreaInput(FIELD_CONTENT).setValue(content) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }

  static public class AddPermissionActionListener extends EventListener<UITemplateContent> {
    public void execute(Event<UITemplateContent> event) throws Exception {
      UITemplateContent uiTempContent = event.getSource() ;
      UITemplatesManager uiManager = uiTempContent.getAncestorOfType(UITemplatesManager.class) ;
      UIViewTemplate uiViewTemp = uiTempContent.getAncestorOfType(UIViewTemplate.class) ;
      uiTempContent.removeChild(UIPopupWindow.class) ;
      String membership = uiTempContent.getUIStringInput(FIELD_VIEWPERMISSION).getValue() ;
      uiManager.initPopupPermission(uiTempContent.getId(), membership) ;
      if(uiTempContent.getId().equals(UIDialogTab.DIALOG_FORM_NAME)) {
        uiViewTemp.setRenderedChild(UIDialogTab.class) ;
      } else if(uiTempContent.getId().equals(UIViewTab.VIEW_FORM_NAME)) {
        uiViewTemp.setRenderedChild(UIViewTab.class) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }

  static public class RefreshActionListener extends EventListener<UITemplateContent> {
    public void execute(Event<UITemplateContent> event) throws Exception {
      UITemplateContent uiForm = event.getSource() ;
      UITemplatesManager uiManager = uiForm.getAncestorOfType(UITemplatesManager.class) ;
      if(!uiForm.isAddNew_) {
        uiForm.update(uiForm.getUIStringInput(UITemplateContent.FIELD_NAME).getValue()) ;
        return ;
      }
      uiForm.update(null) ;
      uiForm.reset() ;
      uiForm.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }

  static public class CancelActionListener extends EventListener<UITemplateContent> {
    public void execute(Event<UITemplateContent> event) throws Exception {
      UITemplateContent uiTemplateContent = event.getSource() ;
      UITemplatesManager uiManager = uiTemplateContent.getAncestorOfType(UITemplatesManager.class) ;
      uiManager.removeChildById(UIDialogTab.DIALOG_FORM_NAME + TEMPLATE_PERMISSION) ;
      uiManager.removeChildById(UIViewTab.VIEW_FORM_NAME + TEMPLATE_PERMISSION) ;
      uiTemplateContent.reset() ;
      uiManager.removeChild(UIPopupWindow.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
}