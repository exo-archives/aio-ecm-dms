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
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormCheckBoxInput;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIFormTextAreaInput;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

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
      @EventConfig(listeners = UITemplateForm.CancelActionListener.class),
      @EventConfig(listeners = UITemplateForm.ResetActionListener.class),
      @EventConfig(listeners = UITemplateForm.ChangeVersionActionListener.class)
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

  public UITemplateForm() throws Exception {
    UIFormSelectBox versions = new UIFormSelectBox(FIELD_VERSION , FIELD_VERSION, null) ;
    versions.setOnChange("ChangeVersion") ;
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
    return node.canAddMixin("mix:versionable");
  }

  private boolean isVersioned(Node node) throws RepositoryException {          
    return node.isNodeType("mix:versionable");    
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
  
  public void refresh() {
    getUIFormSelectBox(FIELD_VERSION).setRendered(false) ;
    getUIFormTextAreaInput(FIELD_CONTENT).setValue(null) ;
    getUIStringInput(FIELD_NAME).setEditable(true).setValue(null);
    getUIFormSelectBox(FIELD_HOMETEMPLATE).setValue(null) ;
    getUIFormSelectBox(FIELD_HOMETEMPLATE).setDisabled(false) ;
    getUIFormCheckBoxInput(FIELD_ENABLEVERSION).setRendered(false) ;
    template_ = null ;
    selectedVersion_ = null ;
    baseVersion_ = null ;
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
        getUIFormSelectBox(FIELD_VERSION).setOptions(getVersionValues(template_)).setRendered(true) ;
        getUIFormSelectBox(FIELD_VERSION).setValue(baseVersion_.getName()) ;
        getUIFormCheckBoxInput(FIELD_ENABLEVERSION).setChecked(true).setEditable(false) ;
      } else if (canEnableVersionning(template_)) {
        getUIFormSelectBox(FIELD_VERSION).setRendered(false) ;
        getUIFormCheckBoxInput(FIELD_ENABLEVERSION).setChecked(false).setEditable(true) ;   
      }
    }
    if (selectedVersion != null) {      
      template_.restore(selectedVersion.getVersion(), false) ;
      selectedVersion_ = selectedVersion;         
    }
    String content = template_.getProperty("exo:templateFile").getString() ;
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
      uiForm.service_.addTemplate(templateName, content, homeTemplate) ;
      boolean isEnableVersioning = uiForm.getUIFormCheckBoxInput(FIELD_ENABLEVERSION).isChecked() ;
      if(isEnableVersioning) {
        if (uiForm.canEnableVersionning(uiForm.template_) && !uiForm.isVersioned(uiForm.template_)) {              
          uiForm.template_.addMixin("mix:versionable");
          uiForm.template_.save();                    
        } 
        uiForm.template_.checkin();
        uiForm.template_.save();          
      }
      uiForm.refresh();
      if(uiForm.getId().equalsIgnoreCase("ECMTempForm")) {
        UIECMTemplateList uiECMTempList = uiTempContainer.getChild(UIECMTemplateList.class) ;
        uiECMTempList.updateTempListGrid() ;
        uiECMTempList.setRenderSibbling(UIECMTemplateList.class);
      } else {
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
      uiTemplateContainer.removeChild(UIPopupWindow.class) ;
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
      if(uiForm.getId().equalsIgnoreCase("ECMTempForm")) {
        UIECMTemplateList uiECMTempList = uiTempContainer.getChild(UIECMTemplateList.class) ;
        uiECMTempList.updateTempListGrid() ;
      } else {
        UICBTemplateList uiCBTempList = uiTempContainer.getChild(UICBTemplateList.class) ;
        uiCBTempList.updateCBTempListGrid() ;
      }
    }
  }
  
  static  public class ChangeVersionActionListener extends EventListener<UITemplateForm> {
    public void execute(Event<UITemplateForm> event) throws Exception {
      UITemplateForm uiForm = event.getSource() ;
      String version = uiForm.getUIFormSelectBox(FIELD_VERSION).getValue() ;
      String path = uiForm.template_.getVersionHistory().getVersion(version).getPath() ;
      VersionNode selectedVesion = uiForm.getRootVersion(uiForm.template_).findVersionNode(path);
      uiForm.update(null, selectedVesion) ;
      if(uiForm.getId().equalsIgnoreCase("ECMTempForm")) {
        UIECMTemplateList uiECMTempList = uiForm.getParent() ;
        uiECMTempList.updateTempListGrid() ;
      } else {
        UICBTemplateList uiCBTempList = uiForm.getParent() ;
        uiCBTempList.updateCBTempListGrid() ;
      }
    }
  }
}

