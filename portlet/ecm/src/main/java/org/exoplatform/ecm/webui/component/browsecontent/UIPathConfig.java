/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletPreferences;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.ecm.utils.SessionsUtils;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
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
import org.exoplatform.webui.form.validator.EmptyFieldValidator;
import org.exoplatform.webui.form.validator.NumberFormatValidator;

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
      @EventConfig(phase = Phase.DECODE, listeners = UIPathConfig.BackActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPathConfig.ChangeRepoActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPathConfig.ChangeWorkspaceActionListener.class)
    }
)
public class UIPathConfig extends UIForm implements UISelector{
  final static public String FIELD_PATHSELECT = "path" ;
  protected boolean isEdit_ = false ;
  public UIPathConfig()throws Exception {
    List<SelectItemOption<String>> Options = new ArrayList<SelectItemOption<String>>() ;
    List<SelectItemOption<String>> repositories = new ArrayList<SelectItemOption<String>>() ;
    List<SelectItemOption<String>> workspaces = new ArrayList<SelectItemOption<String>>() ;
    UIFormSelectBox repository = new UIFormSelectBox(UINewConfigForm.FIELD_REPOSITORY, UINewConfigForm.FIELD_REPOSITORY, repositories) ;
    repository.setOnChange("ChangeRepo") ;
    addChild(repository) ;
    UIFormSelectBox workspace = new UIFormSelectBox(UINewConfigForm.FIELD_WORKSPACE, UINewConfigForm.FIELD_WORKSPACE, workspaces) ;
    workspace.setOnChange("ChangeWorkspace") ;
    addChild(workspace) ;
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
    addChild(new UIFormStringInput(UINewConfigForm.FIELD_ITEMPERPAGE, null, null).
        addValidator(NumberFormatValidator.class).addValidator(EmptyFieldValidator.class)) ;
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
  
  private List<SelectItemOption<String>> getRepoOption() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    for(RepositoryEntry repo : repositoryService.getConfig().getRepositoryConfigurations()) {
      options.add(new SelectItemOption<String>(repo.getName(), repo.getName())) ;
    }
    return options ;
  }
  
  private List<SelectItemOption<String>> getWorkSpaceOption(String repository) throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    String[] workspaceNames = getApplicationComponent(RepositoryService.class)
    .getRepository(repository).getWorkspaceNames() ;
    for(String workspace:workspaceNames) {
      System.out.println("\n\nworkspce====>" + workspace + "\n\n");
      options.add(new SelectItemOption<String>(workspace,workspace)) ;
    }   
    return options ;
  }
  
  public void initForm(PortletPreferences preference, String repository, 
      String workSpace, boolean isAddNew) throws Exception {
    String path = preference.getValue(Utils.JCR_PATH, "") ;
    String hasToolBar = "true" ;
    String hasRefDoc ="true" ; 
    String hasChildDoc = "true" ;
    String hasTagMap = "true" ;
    String hasComment = "true" ;
    String hasVote = "true" ;
    String itemPerPage = "20" ;
    String template = "" ;
    System.out.println("\n\nGo here to run linh tinh\n\n");
    UIFormSelectBox repositoryField = getChildById(UINewConfigForm.FIELD_REPOSITORY) ;
    repositoryField.setOptions(getRepoOption()) ;
    repositoryField.setValue(repository) ;
    UIFormSelectBox workSpaceField = getChildById(UINewConfigForm.FIELD_WORKSPACE) ;
    workSpaceField.setOptions(getWorkSpaceOption(repository)) ;
    workSpaceField.setValue(workSpace) ;
    UIFormInputSetWithAction categoryPathSelect = getChildById(FIELD_PATHSELECT) ;
    UIFormStringInput categoryPathField = categoryPathSelect.getChildById(UINewConfigForm.FIELD_CATEGORYPATH) ;
    categoryPathField.setEditable(false) ;
    UIFormSelectBox templateField = getChildById(UINewConfigForm.FIELD_TEMPLATE) ;
    UIFormStringInput numbPerPageField = getChildById(UINewConfigForm.FIELD_ITEMPERPAGE) ;
    UIFormSelectBox detailtemField = getChildById(UINewConfigForm.FIELD_DETAILBOXTEMP) ;
    UIConfigTabPane uiConfigTabPane = getAncestorOfType(UIConfigTabPane.class) ;
    UIFormCheckBoxInput enableToolBarField = getChildById(UINewConfigForm.FIELD_ENABLETOOLBAR)  ;
    UIFormCheckBoxInput enableRefDocField = getChildById(UINewConfigForm.FIELD_ENABLEREFDOC)  ;
    UIFormCheckBoxInput enableChildDocField = getChildById(UINewConfigForm.FIELD_ENABLECHILDDOC)  ;
    UIFormCheckBoxInput enableTagMapField = getChildById(UINewConfigForm.FIELD_ENABLETAGMAP) ;
    UIFormCheckBoxInput enableCommentField = getChildById(UINewConfigForm.FIELD_ENABLECOMMENT) ;
    UIFormCheckBoxInput enableVoteField = getChildById(UINewConfigForm.FIELD_ENABLEVOTE) ;
    if(isEdit_) {
      categoryPathSelect.setActionInfo(UINewConfigForm.FIELD_CATEGORYPATH, new String[] {"AddPath"}) ;
      if(isAddNew) {
        templateField.setOptions(getTemplateOption(repository)) ;
        detailtemField.setOptions(uiConfigTabPane.getBoxTemplateOption(repository)) ;
        enableToolBarField.setChecked( Boolean.parseBoolean(hasToolBar)) ;
        enableRefDocField.setChecked( Boolean.parseBoolean(hasRefDoc)) ;
        enableChildDocField.setChecked(Boolean.parseBoolean(hasChildDoc)) ;
        enableTagMapField.setChecked(Boolean.parseBoolean(hasTagMap)) ;
        enableCommentField.setChecked(Boolean.parseBoolean(hasComment)) ;
        enableVoteField.setChecked(Boolean.parseBoolean(hasVote)) ;
        numbPerPageField.setValue(itemPerPage) ;
        setActions(UINewConfigForm.ADD_NEW_ACTION) ;        
      }else {
        setActions(UINewConfigForm.NORMAL_ACTION) ;
      }
    } else {
      categoryPathSelect.setActionInfo(UINewConfigForm.FIELD_CATEGORYPATH, null) ;
      setActions(UINewConfigForm.DEFAULT_ACTION) ;
      repository = preference.getValue(Utils.REPOSITORY, "") ;
      template = preference.getValue(Utils.CB_TEMPLATE, "") ;
      hasToolBar = preference.getValue(Utils.CB_VIEW_TOOLBAR, "") ;
      hasRefDoc = preference.getValue(Utils.CB_REF_DOCUMENT, "") ;
      hasChildDoc = preference.getValue(Utils.CB_CHILD_DOCUMENT, "") ;
      hasTagMap = preference.getValue(Utils.CB_VIEW_TAGMAP, "") ;
      hasComment = preference.getValue(Utils.CB_VIEW_COMMENT, "") ;
      hasVote = preference.getValue(Utils.CB_VIEW_VOTE, "") ;
      itemPerPage = (preference.getValue(Utils.CB_NB_PER_PAGE, "")) ;
      templateField.setOptions(getTemplateOption(repository)) ;
      templateField.setValue(template) ;
      detailtemField.setOptions(uiConfigTabPane.getBoxTemplateOption(repository)) ;
      enableToolBarField.setChecked( Boolean.parseBoolean(hasToolBar)) ;
      enableRefDocField.setChecked( Boolean.parseBoolean(hasRefDoc)) ;
      enableChildDocField.setChecked(Boolean.parseBoolean(hasChildDoc)) ;
      enableTagMapField.setChecked(Boolean.parseBoolean(hasTagMap)) ;
      enableCommentField.setChecked(Boolean.parseBoolean(hasComment)) ;
      enableVoteField.setChecked(Boolean.parseBoolean(hasVote)) ;
      categoryPathField.setValue(path) ;
      numbPerPageField.setValue(itemPerPage) ;
    }
    //categoryPathField.setEditable(isEdit_) ;
    templateField.setEnable(isEdit_) ;
    detailtemField.setEnable(isEdit_) ;
    enableToolBarField.setEnable(isEdit_) ;
    enableRefDocField.setEnable(isEdit_) ;
    enableCommentField.setEnable(isEdit_) ;
    enableVoteField.setEnable(isEdit_) ;
    enableTagMapField.setEnable(isEdit_) ;
    numbPerPageField.setEditable(isEdit_) ;
    enableChildDocField.setEnable(isEdit_) ;
    repositoryField.setEnable(isEdit_) ;
    workSpaceField.setEnable(isEdit_) ;
  }

  public List<SelectItemOption<String>> getTemplateOption(String repository) throws Exception {
    List<SelectItemOption<String>> Options = new ArrayList<SelectItemOption<String>>() ;
    ManageViewService viewService = 
      (ManageViewService)PortalContainer.getComponent(ManageViewService.class) ;
    List<Node> scriptTemplates = 
      viewService.getAllTemplates(BasePath.CB_PATH_TEMPLATES, repository, SessionsUtils.getSystemProvider()) ;
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
    UIConfigTabPane uiConfigTabPane = getAncestorOfType(UIConfigTabPane.class) ;
    UIPopupWindow uiPopupWindow = uiConfigTabPane.getChild(UIPopupWindow.class) ;
    uiPopupWindow.setShow(false) ;
    isEdit_ = true ;
    uiConfigTabPane.setNewConfig(true) ;
  }

  public static class SaveActionListener extends EventListener<UIPathConfig>{
    public void execute(Event<UIPathConfig> event) throws Exception {
      UIPathConfig uiForm = event.getSource() ;
      UIBrowseContentPortlet uiBrowseContentPortlet = uiForm.getAncestorOfType(UIBrowseContentPortlet.class) ;
      UIBrowseContainer uiBCContainer = 
        uiBrowseContentPortlet.findFirstComponentOfType(UIBrowseContainer.class) ;
      PortletPreferences prefs = uiBCContainer.getPortletPreferences();
      UIFormSelectBox workSpaceField = uiForm.getChildById(UINewConfigForm.FIELD_WORKSPACE) ;
      String workSpace = workSpaceField.getValue() ;
      UIFormSelectBox repositoryField = uiForm.getChildById(UINewConfigForm.FIELD_REPOSITORY) ;
      String repository = repositoryField.getValue() ;
      UIFormInputSetWithAction categoryPathSelect = uiForm.getChildById(FIELD_PATHSELECT) ;
      UIFormStringInput categoryPathField = categoryPathSelect.getChildById(UINewConfigForm.FIELD_CATEGORYPATH) ;
      String jcrPath = categoryPathField.getValue() ;
      if((jcrPath == null) || (jcrPath.trim().length() == 0)) {
        UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UIPathConfig.msg.require-path", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(app.getUIPopupMessages()) ;
        return ;
      } 
      Session session = uiBCContainer.getSession(repository, workSpace) ;
      Node node = (Node) session.getItem(jcrPath) ;
      if(node == null) {
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
      prefs.setValue(Utils.JCR_PATH, jcrPath) ;
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
      uiBCContainer.setShowDocumentDetail(false) ;
      uiBCContainer.loadPortletConfig(prefs) ;
      uiForm.isEdit_ = false ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiConfigTabPane.setIsChangeValue(false) ;
      uiConfigTabPane.setNewConfig(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiConfigTabPane);
    }
  }  

  public static class AddActionListener extends EventListener<UIPathConfig>{
    public void execute(Event<UIPathConfig> event) throws Exception {
      UIPathConfig uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiConfigTabPane.setIsChangeValue(false) ;
      uiConfigTabPane.setNewConfig(true) ;
      uiConfigTabPane.showNewConfigForm(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiConfigTabPane);
    }
  }
  public static class CancelActionListener extends EventListener<UIPathConfig>{
    public void execute(Event<UIPathConfig> event) throws Exception {
      UIPathConfig uiForm = event.getSource() ;
      uiForm.isEdit_ = false ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiConfigTabPane.setIsChangeValue(false) ;
      uiConfigTabPane.setNewConfig(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiConfigTabPane);
    }
  }
  public static class BackActionListener extends EventListener<UIPathConfig>{
    public void execute(Event<UIPathConfig> event) throws Exception {
      UIPathConfig uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiForm.isEdit_ =  false ;
      uiConfigTabPane.setIsChangeValue(false) ;      
      uiConfigTabPane.setNewConfig(true);
      uiConfigTabPane.showNewConfigForm(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiConfigTabPane);
    }
  }

  public static class EditActionListener extends EventListener<UIPathConfig>{
    public void execute(Event<UIPathConfig> event) throws Exception {
      UIPathConfig uiForm = event.getSource() ;
      uiForm.isEdit_ = true ;
      uiForm.getAncestorOfType(UIConfigTabPane.class).setNewConfig(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }
  static public class AddPathActionListener extends EventListener<UIPathConfig> {
    public void execute(Event<UIPathConfig> event) throws Exception {
      UIPathConfig uiForm  = event.getSource() ;
      UIConfigTabPane uiConfig = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      String repo = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_REPOSITORY).getValue() ;
      String workSpace = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_WORKSPACE).getValue() ;
      uiConfig.initPopupPathSelect(uiForm, repo, workSpace) ;
      uiForm.isEdit_ = true ;
      uiConfig.setNewConfig(true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiConfig);
    }
  }
  
  public static class ChangeRepoActionListener extends EventListener<UIPathConfig>{
    public void execute(Event<UIPathConfig> event) throws Exception {
      UIPathConfig uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiConfigTabPane.setIsChangeValue(true) ;
      String repoName = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_REPOSITORY).getValue() ;
      uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_WORKSPACE).setOptions(uiForm.getWorkSpaceOption(repoName)) ;
      UIFormInputSetWithAction categoryPathSelect = uiForm.getChildById(FIELD_PATHSELECT) ;
      UIFormStringInput categoryPathField = categoryPathSelect.getChildById(UINewConfigForm.FIELD_CATEGORYPATH) ;
      categoryPathField.setValue("/") ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }

  }
  
  public static class ChangeWorkspaceActionListener extends EventListener<UIPathConfig>{
    public void execute(Event<UIPathConfig> event) throws Exception {
      UIPathConfig uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiConfigTabPane.setIsChangeValue(true) ;
      UIFormInputSetWithAction categoryPathSelect = uiForm.getChildById(FIELD_PATHSELECT) ;
      UIFormStringInput categoryPathField = categoryPathSelect.getChildById(UINewConfigForm.FIELD_CATEGORYPATH) ;
      categoryPathField.setValue("/") ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }
}