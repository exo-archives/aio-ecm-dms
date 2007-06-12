/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Dec 19, 2006 9:05:58 AM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIFormWithTitle.gtmpl",
    events = {
      @EventConfig(listeners = UIDocumentConfig.SaveActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIDocumentConfig.ChangeTemplateOptionActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIDocumentConfig.EditActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIDocumentConfig.AddActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIDocumentConfig.AddPathActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIDocumentConfig.DocSelectActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIDocumentConfig.CancelActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIDocumentConfig.BackActionListener.class)
    }
)
public class UIDocumentConfig extends UIForm implements UISelector{
  final static public String FIELD_PATHSELECT = "path" ;
  final static public String FIELD_DOCSELECT = "doc" ;
  public UIDocumentConfig() throws Exception {
    List<SelectItemOption<String>> Options = new ArrayList<SelectItemOption<String>>() ;
    addChild(new UIFormStringInput(UINewConfigForm.FIELD_REPOSITORY, UINewConfigForm.FIELD_REPOSITORY, null)) ;
    addChild(new UIFormStringInput(UINewConfigForm.FIELD_WORKSPACE, UINewConfigForm.FIELD_WORKSPACE, null)) ;    
    UIFormInputSetWithAction categoryPathSelect = new UIFormInputSetWithAction(FIELD_PATHSELECT) ;
    categoryPathSelect.addUIFormInput(new UIFormStringInput(UINewConfigForm.FIELD_CATEGORYPATH, null, null)) ;
    addUIComponentInput(categoryPathSelect) ;
    UIFormInputSetWithAction documentSelect = new UIFormInputSetWithAction(FIELD_DOCSELECT) ;
    documentSelect.addUIFormInput(new UIFormStringInput(UINewConfigForm.FIELD_DOCNAME, UINewConfigForm.FIELD_DOCNAME, null)) ;
    addUIComponentInput(documentSelect) ;
    addChild(new UIFormSelectBox(UINewConfigForm.FIELD_DETAILBOXTEMP, UINewConfigForm.FIELD_DETAILBOXTEMP, Options)) ;
    addChild(new UIFormCheckBoxInput<Boolean>(UINewConfigForm.FIELD_ENABLECOMMENT, null, null)) ;
    addChild(new UIFormCheckBoxInput<Boolean>(UINewConfigForm.FIELD_ENABLEVOTE, null, null)) ;
    setActions(UINewConfigForm.DEFAULT_ACTION) ;
  }
  
  public List<SelectItemOption<String>> getWorkSpaceOption() throws Exception {
    UIConfigTabPane uiTabPane = getAncestorOfType(UIConfigTabPane.class) ;
    return uiTabPane.getWorkSpaceOption() ;
  }
  
  public void initForm(PortletPreferences preference, String repository, String workSpace, boolean isAddNew, 
      boolean isEditable) throws Exception {
    String path = preference.getValue(Utils.JCR_PATH, "") ;
    String docName = "" ;
    String hasComment = "true" ;
    String hasVote = "true" ;
    if(isAddNew) setActions(UINewConfigForm.ADD_NEW_ACTION) ;
    else {
      docName = preference.getValue(Utils.CB_DOCUMENT_NAME, "") ;
      isEditable = false ;
      hasComment = preference.getValue(Utils.CB_VIEW_COMMENT, "") ;
      hasVote = preference.getValue(Utils.CB_VIEW_VOTE, "") ;
    }
    UIFormStringInput workSpaceField = getChildById(UINewConfigForm.FIELD_WORKSPACE) ;
    workSpaceField.setValue(workSpace) ;
    workSpaceField.setEditable(false) ;
    UIFormStringInput repositoryField = getChildById(UINewConfigForm.FIELD_REPOSITORY) ;
    repositoryField.setValue(repository) ;
    repositoryField.setEditable(false) ;
    
    UIFormInputSetWithAction categoryPathSelect = getChildById(FIELD_PATHSELECT) ;
    UIFormInputSetWithAction documentSelect = getChildById(FIELD_DOCSELECT) ;
    UIFormStringInput categoryPathField = categoryPathSelect.getChildById(UINewConfigForm.FIELD_CATEGORYPATH) ;
    UIFormStringInput documentNameField = documentSelect.getChildById(UINewConfigForm.FIELD_DOCNAME) ;
    UIFormSelectBox detailtempField = getChildById(UINewConfigForm.FIELD_DETAILBOXTEMP) ;
    UIConfigTabPane uiConfigTabPane = getAncestorOfType(UIConfigTabPane.class) ;
    detailtempField.setOptions(uiConfigTabPane.getBoxTemplateOption()) ;
    UIFormCheckBoxInput enableCommentField = getChildById(UINewConfigForm.FIELD_ENABLECOMMENT) ;
    enableCommentField.setChecked(Boolean.parseBoolean(hasComment)) ;
    UIFormCheckBoxInput enableVoteField = getChildById(UINewConfigForm.FIELD_ENABLEVOTE) ;
    enableVoteField.setChecked(Boolean.parseBoolean(hasVote)) ;
    
    if((isAddNew)||(isEditable)) {
      categoryPathSelect.setActionInfo(UINewConfigForm.FIELD_CATEGORYPATH, new String[] {"AddPath"}) ;
      documentSelect.setActionInfo(UINewConfigForm.FIELD_DOCNAME, new String[] {"DocSelect"}) ;
    } else { 
      categoryPathSelect.setActionInfo(UINewConfigForm.FIELD_CATEGORYPATH, null) ;
      documentSelect.setActionInfo(UINewConfigForm.FIELD_DOCNAME, null) ;
    }
    categoryPathField.setValue(path) ;
    documentNameField.setValue(docName) ;
    categoryPathField.setEditable(isEditable) ;
    documentNameField.setEditable(isEditable) ;
    detailtempField.setEnable(isEditable) ;
    enableCommentField.setEnable(isEditable) ;
    enableVoteField.setEnable(isEditable) ;
  }

  public void editForm(boolean isEditable) {
    UIFormInputSetWithAction categoryPathSelect = getChildById(FIELD_PATHSELECT) ;
    UIFormInputSetWithAction documentSelect = getChildById(FIELD_DOCSELECT) ;
    categoryPathSelect.setActionInfo(UINewConfigForm.FIELD_CATEGORYPATH, new String[] {"AddPath"}) ;
    documentSelect.setActionInfo(UINewConfigForm.FIELD_DOCNAME, new String[] {"DocSelect"}) ;
    UIFormStringInput categoryPathField = categoryPathSelect.getChildById(UINewConfigForm.FIELD_CATEGORYPATH) ;
    UIFormSelectBox detailtemField = getChildById(UINewConfigForm.FIELD_DETAILBOXTEMP) ;
    UIFormStringInput documentNameField =documentSelect.getChildById(UINewConfigForm.FIELD_DOCNAME) ;
    UIFormCheckBoxInput enableCommentField = getChildById(UINewConfigForm.FIELD_ENABLECOMMENT) ;
    UIFormCheckBoxInput enableVoteField = getChildById(UINewConfigForm.FIELD_ENABLEVOTE) ;
    enableCommentField.setEnable(isEditable) ;
    enableVoteField.setEnable(isEditable) ;
    categoryPathField.setEditable(isEditable) ;
    detailtemField.setEnable(isEditable) ;
    documentNameField.setEditable(isEditable) ;
    setActions(UINewConfigForm.NORMAL_ACTION) ;
  }

  @SuppressWarnings("unused")
  public void updateSelect(String selectField, String value) {
    UIConfigTabPane uiConfig = getAncestorOfType(UIConfigTabPane.class) ;
    if(uiConfig.getChildById(UIConfigTabPane.PATH_SELECTOR) != null ) {
      UIFormInputSetWithAction categoryPathSelect = getChildById(FIELD_PATHSELECT) ;
      UIFormStringInput categoryPathField = categoryPathSelect.getChildById(UINewConfigForm.FIELD_CATEGORYPATH) ;
      categoryPathField.setValue(value) ;
      UIFormInputSetWithAction documentSelect = getChildById(FIELD_DOCSELECT) ;
      UIFormStringInput documentNameField = documentSelect.getChildById(UINewConfigForm.FIELD_DOCNAME) ;
      documentNameField.setValue("") ;
    } 
    if(uiConfig.getChildById(UIConfigTabPane.DOCUMENT_SELECTOR) != null ) {
      UIFormInputSetWithAction categoryPathSelect = getChildById(FIELD_PATHSELECT) ;
      UIFormStringInput categoryPathField = categoryPathSelect.getChildById(UINewConfigForm.FIELD_CATEGORYPATH) ;  
      String path = categoryPathField.getValue() ;
      value = value.substring(path.length()) ;
      UIFormInputSetWithAction documentSelect = getChildById(FIELD_DOCSELECT) ;
      UIFormStringInput documentNameField = documentSelect.getChildById(UINewConfigForm.FIELD_DOCNAME) ;
      documentNameField.setValue(value) ;
    }
    uiConfig.getChild(UIPopupWindow.class).setShow(false) ;
  }

  public static class SaveActionListener extends EventListener<UIDocumentConfig>{
    public void execute(Event<UIDocumentConfig> event) throws Exception {
      UIDocumentConfig uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      UIBrowseContentPortlet uiBrowseContentPortlet = uiForm.getAncestorOfType(UIBrowseContentPortlet.class) ;
      UIBrowseContainer container = uiBrowseContentPortlet.findFirstComponentOfType(UIBrowseContainer.class) ;
      PortletPreferences prefs = container.getPortletPreferences();
      String workSpace = uiForm.getUIStringInput(UINewConfigForm.FIELD_WORKSPACE).getValue() ;
      String repository = uiForm.getUIStringInput(UINewConfigForm.FIELD_REPOSITORY).getValue() ;
      UIFormInputSetWithAction categoryPathSelect = uiForm.getChildById(FIELD_PATHSELECT) ;
      UIFormStringInput categoryPathField = categoryPathSelect.getChildById(UINewConfigForm.FIELD_CATEGORYPATH) ;
      String jcrPatth = categoryPathField.getValue() ;
      if((jcrPatth == null) ||(jcrPatth.trim().length() == 0)) {
        UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UIDocumentConfig.msg.require-path", null)) ;
        return ;
      } 
      if(container.getNodeByPath(jcrPatth) == null) {
        UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UIDocumentConfig.msg.invalid-path", null)) ;
        return ;
      }
      UIFormInputSetWithAction documentSelect = uiForm.getChildById(FIELD_DOCSELECT) ;
      UIFormStringInput documentField = documentSelect.getChildById(UINewConfigForm.FIELD_DOCNAME) ;
      String docName = documentField.getValue() ;
      if((docName == null) ||(docName.trim().length() == 0)) {
        UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UIDocumentConfig.msg.require-doc", null)) ;
        return ;
      } 
      try{
        container.getNodeByPath(jcrPatth + Utils.SLASH + docName) ;
      } catch (Exception e) {
        UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UIDocumentConfig.msg.invalid-doc", null)) ;
        return ;
      }
      String boxTemplate = uiForm.getUIStringInput(UINewConfigForm.FIELD_DETAILBOXTEMP).getValue() ;
      boolean hasComment = uiForm.getUIFormCheckBoxInput(UINewConfigForm.FIELD_ENABLECOMMENT).isChecked() ;
      boolean hasVote = uiForm.getUIFormCheckBoxInput(UINewConfigForm.FIELD_ENABLEVOTE).isChecked() ;
      prefs.setValue(Utils.CB_USECASE, Utils.CB_USE_DOCUMENT) ;
      prefs.setValue(Utils.REPOSITORY, repository) ;
      prefs.setValue(Utils.WORKSPACE_NAME, workSpace) ;
      prefs.setValue(Utils.JCR_PATH, jcrPatth) ;
      prefs.setValue(Utils.CB_DOCUMENT_NAME, docName) ;
      prefs.setValue(Utils.CB_TEMPLATE, "DocumentView") ;
      prefs.setValue(Utils.CB_BOX_TEMPLATE, boxTemplate) ;
      prefs.setValue(Utils.CB_VIEW_TOOLBAR,String.valueOf(hasComment || hasVote)) ;
      prefs.setValue(Utils.CB_VIEW_COMMENT, String.valueOf(hasComment)) ;    
      prefs.setValue(Utils.CB_VIEW_VOTE, String.valueOf(hasVote)) ;    
      prefs.store() ; 
      uiForm.reset() ;
      uiConfigTabPane.getCurrentConfig() ;
      container.loadPortletConfig(container.getPortletPreferences()) ;
      container.setShowDocumentDetail(true) ;
      container.setShowDocumentList(false) ;
    }
  }  

  @SuppressWarnings("unused")
  public static class ChangeTemplateOptionActionListener extends EventListener<UIDocumentConfig>{
    public void execute(Event<UIDocumentConfig> event) throws Exception {
    }
  }  

  public static class AddActionListener extends EventListener<UIDocumentConfig>{
    public void execute(Event<UIDocumentConfig> event) throws Exception {
      UIDocumentConfig uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiConfigTabPane.loadNewConfig(true) ;
    }
  }
  
  public static class CancelActionListener extends EventListener<UIDocumentConfig>{
    public void execute(Event<UIDocumentConfig> event) throws Exception {
      UIDocumentConfig uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiConfigTabPane.getCurrentConfig() ;
    }
  }
  public static class BackActionListener extends EventListener<UIDocumentConfig>{
    public void execute(Event<UIDocumentConfig> event) throws Exception {
      UIDocumentConfig uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiConfigTabPane.loadNewConfig(false) ;
    }
  }
  public static class EditActionListener extends EventListener<UIDocumentConfig>{
    public void execute(Event<UIDocumentConfig> event) throws Exception {
      UIDocumentConfig uiForm = event.getSource() ;
      uiForm.editForm(true) ;
    }
  }
  public static class AddPathActionListener extends EventListener<UIDocumentConfig> {
    public void execute(Event<UIDocumentConfig> event) throws Exception {
      UIDocumentConfig uiForm  = event.getSource() ;
      UIConfigTabPane uiConfig = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      String workSpace = uiForm.getUIStringInput(UINewConfigForm.FIELD_WORKSPACE).getValue() ;
      String repo = uiForm.getUIStringInput(UINewConfigForm.FIELD_REPOSITORY).getValue() ;
      uiConfig.initPopupPathSelect(uiForm, repo, workSpace) ;
    }
  }
  
  public static class DocSelectActionListener extends EventListener<UIDocumentConfig> {
    public void execute(Event<UIDocumentConfig> event) throws Exception {
      UIDocumentConfig uiForm  = event.getSource() ;
      UIFormInputSetWithAction categoryPathSelect = uiForm.getChildById(FIELD_PATHSELECT) ;
      UIFormStringInput categoryPathField = categoryPathSelect.getChildById(UINewConfigForm.FIELD_CATEGORYPATH) ;
      String workspace = uiForm.getUIStringInput(UINewConfigForm.FIELD_WORKSPACE).getValue() ;
      String repo = uiForm.getUIStringInput(UINewConfigForm.FIELD_REPOSITORY).getValue() ;
      String jcrPatth = categoryPathField.getValue() ;
      if((jcrPatth == null)||(jcrPatth.trim().length() == 0)) {
        UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UIDocumentConfig.msg.require-path", null)) ;
        return ;
      }
      UIBrowseContentPortlet uiBrowseContentPortlet = uiForm.getAncestorOfType(UIBrowseContentPortlet.class) ;
      UIBrowseContainer container = uiBrowseContentPortlet.findFirstComponentOfType(UIBrowseContainer.class) ;
      try{
        container.getNodeByPath(jcrPatth) ;
      } catch (Exception e) {
        UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UIDocumentConfig.msg.invalid-path", null)) ;
        return ;
      }
      UIConfigTabPane uiConfig = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiConfig.initPopupDocumentSelect(uiForm, repo, workspace, jcrPatth) ;
    }
  }

}


