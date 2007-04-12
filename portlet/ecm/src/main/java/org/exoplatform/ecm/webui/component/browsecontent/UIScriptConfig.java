/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.portlet.PortletPreferences;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.scripts.ScriptService;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormCheckBoxInput;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Dec 19, 2006 9:05:58 AM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/component/UIFormWithTitle.gtmpl",
    events = {
      @EventConfig(listeners = UIScriptConfig.SaveActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIScriptConfig.EditActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIScriptConfig.AddActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIScriptConfig.CancelActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIScriptConfig.BackActionListener.class)
    }
)
public class UIScriptConfig extends UIForm {

  public UIScriptConfig() {
    List<SelectItemOption<String>> Options = new ArrayList<SelectItemOption<String>>() ;
    addChild(new UIFormStringInput(UINewConfigForm.FIELD_WORKSPACE, UINewConfigForm.FIELD_WORKSPACE, null)) ;
    addChild(new UIFormSelectBox(UINewConfigForm.FIELD_SCRIPTNAME, null, Options)) ;
    addChild(new UIFormSelectBox(UINewConfigForm.FIELD_TEMPLATE, null, Options)) ;
    addChild(new UIFormSelectBox(UINewConfigForm.FIELD_DETAILBOXTEMP, null, Options)) ;
    addChild(new UIFormCheckBoxInput<Boolean>(UINewConfigForm.FIELD_ENABLETAGMAP, null, null)) ;
    addChild(new UIFormCheckBoxInput<Boolean>(UINewConfigForm.FIELD_ENABLECOMMENT, null, null)) ;
    addChild(new UIFormCheckBoxInput<Boolean>(UINewConfigForm.FIELD_ENABLEVOTE, null, null)) ;
    setActions(UINewConfigForm.DEFAULT_ACTION) ;
  }

  public void initForm(PortletPreferences preference, String workSpace, boolean isAddNew, 
      boolean isEditable) throws Exception {
    String hasComment = "false" ;
    String hasVote = "false" ;
    String hasTagMap = "false" ;
    if(isAddNew) setActions(UINewConfigForm.ADD_NEW_ACTION) ;
    else {
      isEditable = false ;
      hasComment = preference.getValue(Utils.CB_VIEW_COMMENT, "") ;
      hasVote = preference.getValue(Utils.CB_VIEW_VOTE, "") ;
      hasTagMap = preference.getValue(Utils.CB_VIEW_TAGMAP, "") ;
    }
    UIFormStringInput workSpaceField = getChildById(UINewConfigForm.FIELD_WORKSPACE) ;
    workSpaceField.setValue(workSpace) ;
    workSpaceField.setEditable(false) ;
    UIFormSelectBox scriptField = getChildById(UINewConfigForm.FIELD_SCRIPTNAME) ;
    scriptField.setOptions(getScriptOption()) ;
    scriptField.setEnable(isEditable) ;
    UIFormSelectBox templateField = getChildById(UINewConfigForm.FIELD_TEMPLATE) ;
    templateField.setOptions(getTemplateOption()) ;
    templateField.setEnable(isEditable) ;
    UIFormSelectBox detailtemField = getChildById(UINewConfigForm.FIELD_DETAILBOXTEMP) ;
    UIConfigTabPane uiConfigTabPane = getAncestorOfType(UIConfigTabPane.class) ;
    detailtemField.setOptions(uiConfigTabPane.getBoxTemplateOption()) ;
    detailtemField.setEnable(isEditable) ;
    UIFormCheckBoxInput enableTagMapField = getChildById(UINewConfigForm.FIELD_ENABLETAGMAP) ;
    enableTagMapField.setChecked(Boolean.parseBoolean(hasTagMap)) ;
    enableTagMapField.setEnable(isEditable) ;
    UIFormCheckBoxInput enableCommentField = getChildById(UINewConfigForm.FIELD_ENABLECOMMENT) ;
    enableCommentField.setChecked(Boolean.parseBoolean(hasComment)) ;
    enableCommentField.setEnable(isEditable) ;  
    UIFormCheckBoxInput enableVoteField = getChildById(UINewConfigForm.FIELD_ENABLEVOTE) ;
    enableVoteField.setEnable(isEditable) ; 
    enableVoteField.setChecked(Boolean.parseBoolean(hasVote)) ;
  }

  public void editForm(boolean isEditable) {
    UIFormSelectBox templateField = getChildById(UINewConfigForm.FIELD_TEMPLATE) ;
    templateField.setEnable(isEditable) ;
    UIFormSelectBox scriptField = getChildById(UINewConfigForm.FIELD_SCRIPTNAME) ;
    scriptField.setEnable(isEditable) ;
    UIFormSelectBox detailtemField = getChildById(UINewConfigForm.FIELD_DETAILBOXTEMP) ;
    detailtemField.setEnable(isEditable) ;
    UIFormCheckBoxInput enableTagMapField = getChildById(UINewConfigForm.FIELD_ENABLETAGMAP)  ;
    enableTagMapField.setEnable(isEditable) ;
    setActions(UINewConfigForm.NORMAL_ACTION) ;
    UIFormCheckBoxInput enableCommentField = getChildById(UINewConfigForm.FIELD_ENABLECOMMENT) ;
    enableCommentField.setEnable(isEditable) ;
    UIFormCheckBoxInput enableVoteField = getChildById(UINewConfigForm.FIELD_ENABLEVOTE) ;
    enableVoteField.setEnable(isEditable) ;
  }

  public List<SelectItemOption<String>> getWorkSpaceOption() throws Exception {
    UIConfigTabPane uiTabPane = getAncestorOfType(UIConfigTabPane.class) ;
    return uiTabPane.getWorkSpaceOption() ;
  }
  private List<SelectItemOption<String>> getTemplateOption() throws Exception {
    List<SelectItemOption<String>> Options = new ArrayList<SelectItemOption<String>>() ;
    ManageViewService viewService = 
      (ManageViewService)PortalContainer.getComponent(ManageViewService.class) ;
    List<Node> scriptTemplates = viewService.getAllTemplates(BasePath.CB_SCRIPT_TEMPLATES) ;
    for(Node template:scriptTemplates) {
      Options.add(new SelectItemOption<String>(template.getName(),template.getName())) ;
    }
    return Options ;
  }

  private List<SelectItemOption<String>> getScriptOption() throws Exception {
    List<SelectItemOption<String>> Options = new ArrayList<SelectItemOption<String>>() ;
    ScriptService scriptService = (ScriptService)PortalContainer.getComponent(ScriptService.class) ;
    Node cbScripts = scriptService.getCBScriptHome() ;
    NodeIterator nodeList = cbScripts.getNodes() ;
    while(nodeList.hasNext()) {
      Node node = nodeList.nextNode() ;
      Options.add(new SelectItemOption<String>(node.getName(), node.getName())) ;
    }
    return Options ;
  }

  public static class SaveActionListener extends EventListener<UIScriptConfig>{
    public void execute(Event<UIScriptConfig> event) throws Exception {
      UIScriptConfig uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      UIBrowseContentPortlet uiBrowseContentPortlet = uiForm.getAncestorOfType(UIBrowseContentPortlet.class) ;
      PortletPreferences prefs = uiBrowseContentPortlet.getPortletPreferences();
      String workSpace = uiForm.getUIStringInput(UINewConfigForm.FIELD_WORKSPACE).getValue() ;
      String scriptName = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_SCRIPTNAME).getValue() ;
      String fullScriptName = workSpace + Utils.SEMI_COLON + scriptName ;
      String template = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_TEMPLATE).getValue() ;
      String boxTemplate = uiForm.getUIStringInput(UINewConfigForm.FIELD_DETAILBOXTEMP).getValue() ;
      boolean hasTagMap = uiForm.getUIFormCheckBoxInput(UINewConfigForm.FIELD_ENABLETAGMAP).isChecked() ;
      boolean hasComment = uiForm.getUIFormCheckBoxInput(UINewConfigForm.FIELD_ENABLECOMMENT).isChecked() ;
      boolean hasVote = uiForm.getUIFormCheckBoxInput(UINewConfigForm.FIELD_ENABLEVOTE).isChecked() ;
      prefs.setValue(Utils.CB_USECASE, Utils.CB_USE_SCRIPT) ;
      prefs.setValue(Utils.WORKSPACE_NAME, workSpace) ;
      prefs.setValue(Utils.CB_SCRIPT_NAME, fullScriptName) ;
      prefs.setValue(Utils.CB_TEMPLATE, template) ;
      prefs.setValue(Utils.CB_BOX_TEMPLATE, boxTemplate) ; 
      prefs.setValue(Utils.CB_VIEW_TAGMAP, String.valueOf(hasTagMap)) ; 
      prefs.setValue(Utils.CB_VIEW_COMMENT,String.valueOf(hasComment)) ;    
      prefs.setValue(Utils.CB_VIEW_VOTE,String.valueOf(hasVote)) ;   
      prefs.store() ; 
      uiForm.reset() ;
      uiConfigTabPane.getCurrentConfig() ;
      UIBrowseContainer container = 
        uiBrowseContentPortlet.findFirstComponentOfType(UIBrowseContainer.class) ;
      container.loadPortletConfig(prefs) ;
    }
  }  

  public static class AddActionListener extends EventListener<UIScriptConfig>{
    public void execute(Event<UIScriptConfig> event) throws Exception {
      UIScriptConfig uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiConfigTabPane.loadNewConfig(true);
    }
  }
  public static class CancelActionListener extends EventListener<UIScriptConfig>{
    public void execute(Event<UIScriptConfig> event) throws Exception {
      UIScriptConfig uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiConfigTabPane.getCurrentConfig() ;
    }
  }
  public static class BackActionListener extends EventListener<UIScriptConfig>{
    public void execute(Event<UIScriptConfig> event) throws Exception {
      UIScriptConfig uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiConfigTabPane.loadNewConfig(false);
    }
  }
  public static class EditActionListener extends EventListener<UIScriptConfig>{
    public void execute(Event<UIScriptConfig> event) throws Exception {
      UIScriptConfig uiForm = event.getSource() ;
      uiForm.editForm(true) ; 
    }
  }
}
