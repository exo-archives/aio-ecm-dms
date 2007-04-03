/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.views;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

import org.exoplatform.ecm.jcr.model.VersionNode;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormCheckBoxInput;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIFormTextAreaInput;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * July 3, 2006
 * 10:07:15 AM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl",
    events = {
      @EventConfig(listeners = UITemplateForm.SaveActionListener.class),
      @EventConfig(phase=Phase.DECODE,listeners = UITemplateForm.CancelActionListener.class),
      @EventConfig(phase=Phase.DECODE,listeners = UITemplateForm.ResetActionListener.class),
      @EventConfig(phase=Phase.DECODE,listeners = UITemplateForm.ChangeActionListener.class),
      @EventConfig(phase=Phase.DECODE,listeners = UITemplateForm.RestoreActionListener.class)
    }
)

public class UITemplateForm extends UIForm {
  
  final static private String FIELD_VERSION = "version" ;
  final static private String FIELD_CONTENT = "content" ;
  final static private String FIELD_NAME = "name" ;
  final static private String FIELD_HOMETEMPLATE = "homeTemplate" ;
  final static private String FIELD_ENABLEVERSION = "enableVersion" ;
  
  private Node template_ = null ;
  private List<String> listVersion = new ArrayList<String>() ;
  private ManageViewService service_ ;
  private Version baseVersion_;
  private VersionNode selectedVersion_;
  public boolean isAddNew_ = false ;

  public UITemplateForm() throws Exception {
    UIFormSelectBox versions = new UIFormSelectBox(FIELD_VERSION , FIELD_VERSION, null) ;
    versions.setOnChange("Change") ;
    versions.setRendered(false) ;
    addUIFormInput(versions) ;
    addUIFormInput(new UIFormTextAreaInput(FIELD_CONTENT, FIELD_CONTENT, null)) ;
    addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null)) ;
    List<SelectItemOption<String>> typeList = new ArrayList<SelectItemOption<String>>() ;
    addUIFormInput(new UIFormSelectBox(FIELD_HOMETEMPLATE, FIELD_HOMETEMPLATE, typeList)) ;
    UIFormCheckBoxInput enableVersion = new UIFormCheckBoxInput<Boolean>(FIELD_ENABLEVERSION, FIELD_ENABLEVERSION, null) ;
    enableVersion.setRendered(false) ;
    addUIFormInput(enableVersion) ;
    setActions(new String[]{"Save", "Reset", "Cancel"}) ;
    service_ = getApplicationComponent(ManageViewService.class) ;
  }

  public void updateOptionList() throws Exception {
    List<SelectItemOption<String>> typeList = new ArrayList<SelectItemOption<String>>() ;
    if(getId().equalsIgnoreCase("ECMTempForm")) {              
      Node ecmTemplateHome = service_.getTemplateHome(BasePath.ECM_EXPLORER_TEMPLATES) ; 
      typeList.add(new SelectItemOption<String>(ecmTemplateHome.getName(),ecmTemplateHome.getPath())) ;
    }else {        
      Node cbTemplateHome = service_.getTemplateHome(BasePath.CONTENT_BROWSER_TEMPLATES) ;
      NodeIterator iter = cbTemplateHome.getNodes() ;
      while(iter.hasNext()) {
        Node template = iter.nextNode() ;
        typeList.add(new SelectItemOption<String>(template.getName(),template.getPath())) ;
      }
    }
    getUIFormSelectBox(FIELD_HOMETEMPLATE).setOptions(typeList) ;
  }
  
  public boolean canEnableVersionning(Node node) throws Exception {
    return node.canAddMixin(Utils.MIX_VERSIONABLE);
  }

  private boolean isVersioned(Node node) throws RepositoryException {          
    return node.isNodeType(Utils.MIX_VERSIONABLE);    
  }

  private VersionNode getRootVersion(Node node) throws Exception{       
    VersionHistory vH = node.getVersionHistory() ;
    return (vH != null) ? new VersionNode(vH.getRootVersion()) : null ;
  }

  private List<String> getNodeVersions(List<VersionNode> children) throws Exception {         
    List<VersionNode> child = new ArrayList<VersionNode>() ;
    for(VersionNode vNode : children){
      listVersion.add(vNode.getName());
      child = vNode.getChildren() ;
      if (!child.isEmpty()) getNodeVersions(child) ; 
    }           
    return listVersion ;
  }

  private List<SelectItemOption<String>> getVersionValues(Node node) throws Exception { 
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    List<VersionNode> children = getRootVersion(node).getChildren() ;
    listVersion.clear() ;
    List<String> versionList = getNodeVersions(children) ;
    for(int i = 0; i < versionList.size(); i++) {
      for(int j = i + 1; j < versionList.size(); j ++) {
        if( Integer.parseInt(versionList.get(j)) < Integer.parseInt(versionList.get(i))) {
          String temp = versionList.get(i) ;
          versionList.set(i, versionList.get(j))  ;
          versionList.set(j, temp) ;
        }
      }
      options.add(new SelectItemOption<String>(versionList.get(i), versionList.get(i))) ;
    }
    return options ;
  }
  
  public void refresh() throws Exception {
    UIFormSelectBox versionField = getUIFormSelectBox(FIELD_VERSION) ;
    if(isAddNew_) {
      versionField.setRendered(false) ;
      getUIFormTextAreaInput(FIELD_CONTENT).setValue(null) ;
      getUIStringInput(FIELD_NAME).setEditable(true).setValue(null);
      getUIFormSelectBox(FIELD_HOMETEMPLATE).setValue(null) ;
      getUIFormSelectBox(FIELD_HOMETEMPLATE).setDisabled(false) ;
      getUIFormCheckBoxInput(FIELD_ENABLEVERSION).setRendered(false) ;
      template_ = null ;
      selectedVersion_ = null ;
      baseVersion_ = null ;
      return ;
    }    
    update(template_.getPath(), null) ;
  }

  public void update(String templatePath, VersionNode selectedVersion) throws Exception {
    if(templatePath != null) {
      template_ = service_.getTemplate(templatePath) ;
      getUIStringInput(FIELD_NAME).setValue(template_.getName()) ;
      getUIStringInput(FIELD_NAME).setEditable(false) ;
      String value = templatePath.substring(0, templatePath.lastIndexOf("/")) ;
      getUIFormSelectBox(FIELD_HOMETEMPLATE).setValue(value) ;
      getUIFormSelectBox(FIELD_HOMETEMPLATE).setDisabled(false) ;
      getUIFormCheckBoxInput(FIELD_ENABLEVERSION).setRendered(true) ;
      if (isVersioned(template_)) {
        baseVersion_ = template_.getBaseVersion() ;
        List<SelectItemOption<String>> options = getVersionValues(template_) ;
        getUIFormSelectBox(FIELD_VERSION).setOptions(options).setRendered(true) ;
        getUIFormSelectBox(FIELD_VERSION).setValue(baseVersion_.getName()) ;
        getUIFormCheckBoxInput(FIELD_ENABLEVERSION).setChecked(true).setEnable(false) ;
        if(options.size()>1) setActions(new String[]{"Save", "Reset", "Restore", "Cancel"}) ;
        else setActions(new String[]{"Save", "Reset", "Cancel"}) ;
      } else if (canEnableVersionning(template_)) {
        getUIFormSelectBox(FIELD_VERSION).setRendered(false) ;
        getUIFormCheckBoxInput(FIELD_ENABLEVERSION).setChecked(false).setEditable(true) ;   
      }
    }
    if(selectedVersion != null) {      
      template_.restore(selectedVersion.getVersion(), false) ;
      selectedVersion_ = selectedVersion;
      Object[] args = {getUIStringInput(FIELD_VERSION).getValue()} ;
      UIApplication app = getAncestorOfType(UIApplication.class) ;
      app.addMessage(new ApplicationMessage("UITemplateForm.msg.version-restored", args)) ;
    }
    String content = template_.getProperty(Utils.EXO_TEMPLATE).getString() ;
    getUIFormTextAreaInput(FIELD_CONTENT).setValue(content) ;
  } 
  
  static  public class SaveActionListener extends EventListener<UITemplateForm> {
    public void execute(Event<UITemplateForm> event) throws Exception {
      UITemplateForm uiForm = event.getSource() ;
      String templateName = uiForm.getUIStringInput(FIELD_NAME).getValue() ;
      String content = uiForm.getUIFormTextAreaInput(FIELD_CONTENT).getValue() ;
      String homeTemplate = uiForm.getUIFormSelectBox(FIELD_HOMETEMPLATE).getValue() ;
      UITemplateContainer uiTempContainer = uiForm.getAncestorOfType(UITemplateContainer.class) ;
      if(homeTemplate == null) {
        String tempPath = uiForm.template_.getPath() ;
        homeTemplate = tempPath.substring(0, tempPath.lastIndexOf("/")) ;
      }
      boolean isEnableVersioning = uiForm.getUIFormCheckBoxInput(FIELD_ENABLEVERSION).isChecked() ;
      if(uiForm.isAddNew_ || !isEnableVersioning){
        uiForm.service_.addTemplate(templateName, content, homeTemplate) ;
      } else {
        if(isEnableVersioning) {
          if(!uiForm.template_.isNodeType(Utils.MIX_VERSIONABLE)) uiForm.template_.addMixin(Utils.MIX_VERSIONABLE);
          else uiForm.template_.checkout() ;
          uiForm.service_.addTemplate(templateName, content, homeTemplate) ;
          uiForm.template_.save() ;
          uiForm.template_.checkin() ;
        }
      }
      uiForm.refresh();
      if(uiForm.getId().equalsIgnoreCase(UIECMTemplateList.ST_ECMTempForm)) {
        UIECMTemplateList uiECMTempList = uiTempContainer.getChild(UIECMTemplateList.class) ;
        uiECMTempList.updateTempListGrid() ;
        uiECMTempList.setRenderSibbling(UIECMTemplateList.class);
      } 
      if(uiForm.getId().equalsIgnoreCase(UICBTemplateList.ST_CBTempForm)) {
        UICBTemplateList uiCBTempList = uiTempContainer.getChild(UICBTemplateList.class) ;
        uiCBTempList.updateCBTempListGrid() ;
        uiCBTempList.setRenderSibbling(UICBTemplateList.class);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTempContainer) ;
    }
  }

  static  public class CancelActionListener extends EventListener<UITemplateForm> {
    public void execute(Event<UITemplateForm> event) throws Exception {
      UITemplateForm uiForm = event.getSource() ;
      uiForm.refresh() ;
      UITemplateContainer uiTemplateContainer = uiForm.getAncestorOfType(UITemplateContainer.class) ;
      if(uiForm.isAddNew_) {
          uiTemplateContainer.removeChildById(UIECMTemplateList.ST_ECMTempForm + "Add") ;
          uiTemplateContainer.removeChildById(UICBTemplateList.ST_CBTempForm + "Add") ;
       } else {
         uiTemplateContainer.removeChildById(UIECMTemplateList.ST_ECMTempForm + "Edit") ;
         uiTemplateContainer.removeChildById(UICBTemplateList.ST_CBTempForm + "Edit") ;
       }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTemplateContainer) ;
    }
  }
  
  static  public class ResetActionListener extends EventListener<UITemplateForm> {
    public void execute(Event<UITemplateForm> event) throws Exception {
      UITemplateForm uiForm = event.getSource() ;
      UITemplateContainer uiTempContainer = uiForm.getAncestorOfType(UITemplateContainer.class) ;
      if (uiForm.selectedVersion_ != null) { 
        if (!uiForm.selectedVersion_.equals(uiForm.baseVersion_)) {
          uiForm.template_.restore(uiForm.baseVersion_, true);
          uiForm.template_.checkout();
        }
      }
      uiForm.refresh() ;
      if(uiForm.getId().equalsIgnoreCase(UIECMTemplateList.ST_ECMTempForm)) {
        UIECMTemplateList uiECMTempList = uiTempContainer.getChild(UIECMTemplateList.class) ;
        uiECMTempList.updateTempListGrid() ;
      } 
      if(uiForm.getId().equalsIgnoreCase(UICBTemplateList.ST_CBTempForm)) {
        UICBTemplateList uiCBTempList = uiTempContainer.getChild(UICBTemplateList.class) ;
        uiCBTempList.updateCBTempListGrid() ;
      }
    }
  }
  
  static  public class ChangeActionListener extends EventListener<UITemplateForm> {
    public void execute(Event<UITemplateForm> event) throws Exception {
      UITemplateForm uiForm = event.getSource() ;
      String version = uiForm.getUIFormSelectBox(FIELD_VERSION).getValue() ;
      String path = uiForm.template_.getVersionHistory().getVersion(version).getPath() ;
      VersionNode versionNode = uiForm.getRootVersion(uiForm.template_).findVersionNode(path);
      Node frozenNode = versionNode.getVersion().getNode(Utils.JCR_FROZEN) ;
      String content = frozenNode.getProperty(Utils.EXO_TEMPLATE).getString() ;
      uiForm.getUIFormTextAreaInput(FIELD_CONTENT).setValue(content) ;
      UITemplateContainer uiTempContainer = uiForm.getAncestorOfType(UITemplateContainer.class) ;
      if(uiForm.getId().equalsIgnoreCase(UIECMTemplateList.ST_ECMTempForm)) {
        UIECMTemplateList uiECMTempList = uiTempContainer.getChild(UIECMTemplateList.class) ;
        uiECMTempList.updateTempListGrid() ;
      } 
      if(uiForm.getId().equalsIgnoreCase(UICBTemplateList.ST_CBTempForm)) {
        UICBTemplateList uiCBTempList = uiTempContainer.getChild(UICBTemplateList.class) ;
        uiCBTempList.updateCBTempListGrid() ;
      }
    }
  }
  
  static  public class RestoreActionListener extends EventListener<UITemplateForm> {
    public void execute(Event<UITemplateForm> event) throws Exception {
      UITemplateForm uiForm = event.getSource() ;
      String version = uiForm.getUIFormSelectBox(FIELD_VERSION).getValue() ;
      String path = uiForm.template_.getVersionHistory().getVersion(version).getPath() ;
      VersionNode selectedVesion = uiForm.getRootVersion(uiForm.template_).findVersionNode(path);
      if(uiForm.baseVersion_.getName().equals(selectedVesion.getName())) return ;
      uiForm.update(null, selectedVesion) ;
      UITemplateContainer uiTempContainer = uiForm.getAncestorOfType(UITemplateContainer.class) ;
      if(uiForm.getId().equalsIgnoreCase(UIECMTemplateList.ST_ECMTempForm)) {
        UIECMTemplateList uiECMTempList = uiTempContainer.getChild(UIECMTemplateList.class) ;
        uiECMTempList.updateTempListGrid() ;
      } 
      if(uiForm.getId().equalsIgnoreCase(UICBTemplateList.ST_CBTempForm)) {
        UICBTemplateList uiCBTempList = uiTempContainer.getChild(UICBTemplateList.class) ;
        uiCBTempList.updateCBTempListGrid() ;
      }
      
      uiForm.refresh() ;
      UITemplateContainer uiTemplateContainer = uiForm.getAncestorOfType(UITemplateContainer.class) ;
      if(uiForm.isAddNew_) {
          uiTemplateContainer.removeChildById(UIECMTemplateList.ST_ECMTempForm + "Add") ;
          uiTemplateContainer.removeChildById(UICBTemplateList.ST_CBTempForm + "Add") ;
       } else {
         uiTemplateContainer.removeChildById(UIECMTemplateList.ST_ECMTempForm + "Edit") ;
         uiTemplateContainer.removeChildById(UICBTemplateList.ST_CBTempForm + "Edit") ;
       }
    }
  }
}

