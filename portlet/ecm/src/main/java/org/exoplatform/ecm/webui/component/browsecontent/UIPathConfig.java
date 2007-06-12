/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.portlet.PortletPreferences;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.views.ManageViewService;
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
import org.exoplatform.webui.form.validator.NumberFormatValidator;

import sun.net.dns.ResolverConfiguration.Options;

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
      @EventConfig(listeners = UIPathConfig.SaveActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPathConfig.EditActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPathConfig.AddActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPathConfig.AddPathActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPathConfig.CancelActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPathConfig.BackActionListener.class)
    }
)
public class UIPathConfig extends UIForm implements UISelector{
  final static public String FIELD_PATHSELECT = "path" ;
  public UIPathConfig()throws Exception {
    List<SelectItemOption<String>> Options = new ArrayList<SelectItemOption<String>>() ;
    addChild(new UIFormStringInput(UINewConfigForm.FIELD_REPOSITORY, UINewConfigForm.FIELD_REPOSITORY, null)) ;
    addChild(new UIFormStringInput(UINewConfigForm.FIELD_WORKSPACE, UINewConfigForm.FIELD_WORKSPACE, null)) ;
    UIFormInputSetWithAction categoryPathSelect = new UIFormInputSetWithAction(FIELD_PATHSELECT) ;
    categoryPathSelect.addUIFormInput(new UIFormStringInput(UINewConfigForm.FIELD_CATEGORYPATH, null, null)) ;
    addUIComponentInput(categoryPathSelect) ;
    addChild(new UIFormCheckBoxInput<Boolean>(UINewConfigForm.FIELD_ENABLEREFDOC, null, null)) ;
    addChild(new UIFormCheckBoxInput<Boolean>(UINewConfigForm.FIELD_ENABLECHILDDOC, null, null)) ;
    addChild(new UIFormCheckBoxInput<Boolean>(UINewConfigForm.FIELD_ENABLETAGMAP, null, null)) ;
    addChild(new UIFormSelectBox(UINewConfigForm.FIELD_TEMPLATE, null, Options)) ;
    addChild(new UIFormCheckBoxInput<Boolean>(UINewConfigForm.FIELD_ENABLETOOLBAR, null, null)) ;
    addChild(new UIFormCheckBoxInput<Boolean>(UINewConfigForm.FIELD_ENABLECOMMENT, null, null)) ;
    addChild(new UIFormCheckBoxInput<Boolean>(UINewConfigForm.FIELD_ENABLEVOTE, null, null)) ;
    addChild(new UIFormStringInput(UINewConfigForm.FIELD_ITEMPERPAGE, null, null).addValidator(NumberFormatValidator.class)) ;
    addChild(new UIFormSelectBox(UINewConfigForm.FIELD_DETAILBOXTEMP, null, Options)) ;
    setActions(UINewConfigForm.DEFAULT_ACTION) ;
  }
  
  public List<SelectItemOption<String>> getWorkSpaceOption() throws Exception {
    UIConfigTabPane uiTabPane = getAncestorOfType(UIConfigTabPane.class) ;
    return uiTabPane.getWorkSpaceOption() ;
  }
  public PortletPreferences getPortletPreferences() {    
    return getAncestorOfType(UIBrowseContentPortlet.class).getPortletPreferences() ;
  }
  public void initForm(PortletPreferences preference, String repository, String workSpace, boolean isAddNew, 
                       boolean isEditable) throws Exception {
    String path = preference.getValue(Utils.JCR_PATH, "") ;
    String hasToolBar = "true" ;
    String hasRefDoc ="true" ; 
    String hasChildDoc = "true" ;
    String hasTagMap = "true" ;
    String hasComment = "true" ;
    String hasVote = "true" ;
    String itemPerPage = "20" ;
    String template = preference.getValue(Utils.CB_TEMPLATE, "") ;
    try {
      Integer.parseInt(preference.getValue(Utils.CB_NB_PER_PAGE, "")) ;
      itemPerPage = (preference.getValue(Utils.CB_NB_PER_PAGE, "")) ;
    }
    catch (Exception  e) {}
    if(isAddNew) { setActions(UINewConfigForm.ADD_NEW_ACTION) ;
    } else {
      hasToolBar = preference.getValue(Utils.CB_VIEW_TOOLBAR, "") ;
      hasRefDoc = preference.getValue(Utils.CB_REF_DOCUMENT, "") ;
      hasChildDoc = preference.getValue(Utils.CB_CHILD_DOCUMENT, "") ;
      hasTagMap = preference.getValue(Utils.CB_VIEW_TAGMAP, "") ;
      hasComment = preference.getValue(Utils.CB_VIEW_COMMENT, "") ;
      hasVote = preference.getValue(Utils.CB_VIEW_VOTE, "") ;
      isEditable = false ;
    }
    UIFormStringInput workSpaceField = getChildById(UINewConfigForm.FIELD_WORKSPACE) ;
    workSpaceField.setValue(workSpace) ;
    workSpaceField.setEditable(false) ;
    UIFormStringInput repositoryField = getChildById(UINewConfigForm.FIELD_REPOSITORY) ;
    repositoryField.setValue(repository) ;
    repositoryField.setEditable(false) ;
    UIFormInputSetWithAction categoryPathSelect = getChildById(FIELD_PATHSELECT) ;
    if((isAddNew)||(isEditable)) {
      categoryPathSelect.setActionInfo(UINewConfigForm.FIELD_CATEGORYPATH, new String[] {"AddPath"}) ;
      
    } else categoryPathSelect.setActionInfo(UINewConfigForm.FIELD_CATEGORYPATH, null) ;
    UIFormStringInput categoryPathField = categoryPathSelect.getChildById(UINewConfigForm.FIELD_CATEGORYPATH) ;
    UIFormSelectBox templateField = getChildById(UINewConfigForm.FIELD_TEMPLATE) ;
    templateField.setOptions(getTemplateOption()) ;
    templateField.setValue(template) ;
    UIFormStringInput numbPerPageField = getChildById(UINewConfigForm.FIELD_ITEMPERPAGE) ;
    UIFormSelectBox detailtemField = getChildById(UINewConfigForm.FIELD_DETAILBOXTEMP) ;
    UIConfigTabPane uiConfigTabPane = getAncestorOfType(UIConfigTabPane.class) ;
    detailtemField.setOptions(uiConfigTabPane.getBoxTemplateOption()) ;
    UIFormCheckBoxInput enableToolBarField = getChildById(UINewConfigForm.FIELD_ENABLETOOLBAR)  ;
    enableToolBarField.setChecked( Boolean.parseBoolean(hasToolBar)) ;
    UIFormCheckBoxInput enableRefDocField = getChildById(UINewConfigForm.FIELD_ENABLEREFDOC)  ;
    enableRefDocField.setChecked( Boolean.parseBoolean(hasRefDoc)) ;
    UIFormCheckBoxInput enableChildDocField = getChildById(UINewConfigForm.FIELD_ENABLECHILDDOC)  ;
    enableChildDocField.setChecked(Boolean.parseBoolean(hasChildDoc)) ;
    UIFormCheckBoxInput enableTagMapField = getChildById(UINewConfigForm.FIELD_ENABLETAGMAP) ;
    enableTagMapField.setChecked(Boolean.parseBoolean(hasTagMap)) ;
    enableTagMapField.setEnable(isEditable) ;
    UIFormCheckBoxInput enableCommentField = getChildById(UINewConfigForm.FIELD_ENABLECOMMENT) ;
    enableCommentField.setChecked(Boolean.parseBoolean(hasComment)) ;
    UIFormCheckBoxInput enableVoteField = getChildById(UINewConfigForm.FIELD_ENABLEVOTE) ;
    enableVoteField.setChecked(Boolean.parseBoolean(hasVote)) ;
    categoryPathField.setValue(path) ;
    categoryPathField.setEditable(isEditable) ;
    templateField.setEnable(isEditable) ;
    numbPerPageField.setValue(itemPerPage) ;
    numbPerPageField.setEditable(isEditable) ;
    detailtemField.setEnable(isEditable) ;
    enableToolBarField.setEnable(isEditable) ;
    enableRefDocField.setEnable(isEditable) ;
    enableCommentField.setEnable(isEditable) ;
    enableVoteField.setEnable(isEditable) ;
    enableChildDocField.setEnable(isEditable) ;
  }

  public void editForm(boolean isEditable) {   
    UIFormInputSetWithAction categoryPathSelect = getChildById(FIELD_PATHSELECT) ;
    categoryPathSelect.setActionInfo(UINewConfigForm.FIELD_CATEGORYPATH, new String[] {"AddPath"}) ;
    UIFormStringInput categoryPathField = categoryPathSelect.getChildById(UINewConfigForm.FIELD_CATEGORYPATH) ;
    UIFormSelectBox templateField = getChildById(UINewConfigForm.FIELD_TEMPLATE) ;
    templateField.setValue(getPortletPreferences().getValue(Utils.CB_TEMPLATE, "")) ;
    templateField.setEnable(isEditable) ;
    UIFormStringInput numbPerPageField = getChildById(UINewConfigForm.FIELD_ITEMPERPAGE) ;
    numbPerPageField.setEditable(isEditable) ;
    UIFormSelectBox detailtemField = getChildById(UINewConfigForm.FIELD_DETAILBOXTEMP) ;
    detailtemField.setEnable(isEditable) ;
    UIFormCheckBoxInput enableToolBarField = getChildById(UINewConfigForm.FIELD_ENABLETOOLBAR)  ;
    UIFormCheckBoxInput enableRefDocField = getChildById(UINewConfigForm.FIELD_ENABLEREFDOC)  ;
    enableToolBarField.setEnable(isEditable) ;
    UIFormCheckBoxInput enableChildDocField = getChildById(UINewConfigForm.FIELD_ENABLECHILDDOC)  ;
    UIFormCheckBoxInput enableTagMapField = getChildById(UINewConfigForm.FIELD_ENABLETAGMAP)  ;
    enableTagMapField.setEnable(isEditable) ;
    UIFormCheckBoxInput enableCommentField = getChildById(UINewConfigForm.FIELD_ENABLECOMMENT) ;
    UIFormCheckBoxInput enableVoteField = getChildById(UINewConfigForm.FIELD_ENABLEVOTE) ;
    categoryPathField.setEditable(isEditable) ;
    enableRefDocField.setEnable(isEditable) ;
    enableChildDocField.setEnable(isEditable) ;
    enableCommentField.setEnable(isEditable) ;
    enableVoteField.setEnable(isEditable) ;
    setActions(UINewConfigForm.NORMAL_ACTION) ;
  }

  public List<SelectItemOption<String>> getTemplateOption() throws Exception {
    List<SelectItemOption<String>> Options = new ArrayList<SelectItemOption<String>>() ;
    String repository = getAncestorOfType(UIBrowseContentPortlet.class).getPreferenceRepository() ;
    ManageViewService viewService = 
      (ManageViewService)PortalContainer.getComponent(ManageViewService.class) ;
    List<Node> scriptTemplates = viewService.getAllTemplates(BasePath.CB_PATH_TEMPLATES, repository) ;
    for(Node template:scriptTemplates) {
      Options.add(new SelectItemOption<String>(template.getName(),template.getName())) ;
    }
    return Options ;
  }

  @SuppressWarnings("unused")
  public void updateSelect(String selectField, String value) {
    UIFormInputSetWithAction categoryPathSelect = getChildById(FIELD_PATHSELECT) ;
    UIFormStringInput categoryPathField = categoryPathSelect.getChildById(UINewConfigForm.FIELD_CATEGORYPATH) ;
    categoryPathField.setValue(value) ;
    UIConfigTabPane uiConfig = getAncestorOfType(UIConfigTabPane.class) ;
    UIPopupWindow uiPopupWindow = uiConfig.getChild(UIPopupWindow.class) ;
    uiPopupWindow.setShow(false) ;
  }

  public static class SaveActionListener extends EventListener<UIPathConfig>{
    public void execute(Event<UIPathConfig> event) throws Exception {
      UIPathConfig uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      UIBrowseContentPortlet uiBrowseContentPortlet = uiForm.getAncestorOfType(UIBrowseContentPortlet.class) ;
      UIBrowseContainer container = 
        uiBrowseContentPortlet.findFirstComponentOfType(UIBrowseContainer.class) ;
      PortletPreferences prefs = container.getPortletPreferences();
      UIFormStringInput workSpaceField = uiForm.getChildById(UINewConfigForm.FIELD_WORKSPACE) ;
      String workSpace = workSpaceField.getValue() ;
      UIFormStringInput repositoryField = uiForm.getChildById(UINewConfigForm.FIELD_REPOSITORY) ;
      String repository = repositoryField.getValue() ;
      UIFormInputSetWithAction categoryPathSelect = uiForm.getChildById(FIELD_PATHSELECT) ;
      UIFormStringInput categoryPathField = categoryPathSelect.getChildById(UINewConfigForm.FIELD_CATEGORYPATH) ;
      String jcrPatth = categoryPathField.getValue() ;
      if((jcrPatth == null) ||(jcrPatth.trim().length() == 0)) {
        UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UIPathConfig.msg.require-path", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(app.getUIPopupMessages()) ;
        return ;
      } 
      if(container.getNodeByPath(jcrPatth) == null) {
        UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UIPathConfig.msg.invalid-path", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(app.getUIPopupMessages()) ;
        return ;
      }
      String template = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_TEMPLATE).getValue() ;
      String itemPerPage = uiForm.getUIStringInput(UINewConfigForm.FIELD_ITEMPERPAGE).getValue() ;
      if(Integer.parseInt(itemPerPage) <= 0) {
        UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UIPathConfig.msg.invalid-value", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(app.getUIPopupMessages()) ;
        return ;
      }
      /*try{
        Integer.parseInt(itemPerPage) ;
      } catch(Exception e){
        UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UIPathConfig.msg.invalid-value", null)) ;
        return ;
      }*/
      String boxTemplate = uiForm.getUIStringInput(UINewConfigForm.FIELD_DETAILBOXTEMP).getValue() ;
      boolean hasChildDoc = uiForm.getUIFormCheckBoxInput(UINewConfigForm.FIELD_ENABLECHILDDOC).isChecked() ;
      boolean hasRefDoc = uiForm.getUIFormCheckBoxInput(UINewConfigForm.FIELD_ENABLEREFDOC).isChecked() ;
      boolean hasToolBar = uiForm.getUIFormCheckBoxInput(UINewConfigForm.FIELD_ENABLETOOLBAR).isChecked() ;
      boolean hasTagMap = uiForm.getUIFormCheckBoxInput(UINewConfigForm.FIELD_ENABLETAGMAP).isChecked() ;
      boolean hasComment = uiForm.getUIFormCheckBoxInput(UINewConfigForm.FIELD_ENABLECOMMENT).isChecked() ;
      boolean hasVote = uiForm.getUIFormCheckBoxInput(UINewConfigForm.FIELD_ENABLEVOTE).isChecked() ;
      prefs.setValue(Utils.CB_USECASE, Utils.CB_USE_FROM_PATH) ;
      prefs.setValue(Utils.REPOSITORY, repository) ;
      prefs.setValue(Utils.WORKSPACE_NAME, workSpace) ;
      prefs.setValue(Utils.JCR_PATH, jcrPatth) ;
      prefs.setValue(Utils.CB_NB_PER_PAGE, itemPerPage) ;
      prefs.setValue(Utils.CB_TEMPLATE, template) ;
      prefs.setValue(Utils.CB_BOX_TEMPLATE, boxTemplate) ;    
      prefs.setValue(Utils.CB_REF_DOCUMENT, String.valueOf(hasRefDoc)) ;
      prefs.setValue(Utils.CB_CHILD_DOCUMENT, String.valueOf(hasChildDoc)) ;    
      prefs.setValue(Utils.CB_VIEW_TOOLBAR, String.valueOf(hasToolBar)) ;    
      prefs.setValue(Utils.CB_VIEW_TAGMAP, String.valueOf(hasTagMap)) ; 
      prefs.setValue(Utils.CB_VIEW_COMMENT, String.valueOf(hasComment)) ; 
      prefs.setValue(Utils.CB_VIEW_VOTE, String.valueOf(hasVote)) ; 
      prefs.store() ; 
      uiForm.reset() ;
      uiConfigTabPane.getCurrentConfig() ;
      container.setCurrentNode(null) ;
      container.setSelectedTab(null) ;
      container.loadPortletConfig(prefs) ;
    }
  }  

  public static class AddActionListener extends EventListener<UIPathConfig>{
    public void execute(Event<UIPathConfig> event) throws Exception {
      UIPathConfig uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiConfigTabPane.loadNewConfig(true);
    }
  }
  public static class CancelActionListener extends EventListener<UIPathConfig>{
    public void execute(Event<UIPathConfig> event) throws Exception {
      UIPathConfig uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiConfigTabPane.getCurrentConfig() ;
    }
  }
  public static class BackActionListener extends EventListener<UIPathConfig>{
    public void execute(Event<UIPathConfig> event) throws Exception {
      UIPathConfig uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiConfigTabPane.loadNewConfig(false);
    }
  }
  public static class EditActionListener extends EventListener<UIPathConfig>{
    public void execute(Event<UIPathConfig> event) throws Exception {
      UIPathConfig uiForm = event.getSource() ;
      uiForm.editForm(true) ; 
    }
  }
  static public class AddPathActionListener extends EventListener<UIPathConfig> {
    public void execute(Event<UIPathConfig> event) throws Exception {
      UIPathConfig uiForm  = event.getSource() ;
      UIConfigTabPane uiConfig = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      String workSpace = uiForm.getUIStringInput(UINewConfigForm.FIELD_WORKSPACE).getValue() ;
      String repo = uiForm.getUIStringInput(UINewConfigForm.FIELD_REPOSITORY).getValue() ;
      uiConfig.initPopupPathSelect(uiForm, repo, workSpace) ;
    }
  }
}

