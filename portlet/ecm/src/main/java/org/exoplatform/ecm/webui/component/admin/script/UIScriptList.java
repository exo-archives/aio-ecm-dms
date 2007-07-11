/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.script;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.services.cms.scripts.ScriptService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@yahoo.de
 * September 27, 2006
 * 10:37:15 AM
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/UIGridWithButton.gtmpl",
    events = {
        @EventConfig(listeners = UIScriptList.EditActionListener.class),
        @EventConfig(listeners = UIScriptList.DeleteActionListener.class, confirm="UIScriptList.msg.confirm-delete"),
        @EventConfig(listeners = UIScriptList.AddNewActionListener.class)
    }
)

public class UIScriptList extends UIGrid {

  private static String[] BEAN_FIELD = {"name", "path", "baseVersion"} ;
  private static String[] ACTIONS = {"Edit", "Delete"} ;

  public UIScriptList() throws Exception { 
    configure("name", BEAN_FIELD, ACTIONS) ;
  }

  public void updateGrid(List<ScriptData> scriptData) throws Exception {
    ObjectPageList objPageList = new ObjectPageList(scriptData, 10) ;
    getUIPageIterator().setPageList(objPageList) ;
  }

  public String getScriptCategory() throws Exception {
    UIComponent parent = getParent() ;
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    ScriptService scriptService =  getApplicationComponent(ScriptService.class) ;
    Node script = null ;
    if(parent instanceof UIECMScripts) {
      UIECMFilterForm filterForm = parent.findFirstComponentOfType(UIECMFilterForm.class) ;
      String categoryName = 
        filterForm.getUIFormSelectBox(UIECMFilterForm.FIELD_SELECT_SCRIPT).getValue() ;
      script = scriptService.getECMScriptHome(repository).getNode(categoryName) ;
    } else {
      script = scriptService.getCBScriptHome(repository) ;
    }
    String basePath = scriptService.getBaseScriptPath() + "/" ;
    return script.getPath().substring(basePath.length()) ;
  }

  public void refresh() throws Exception {
    UIScriptManager sManager = getAncestorOfType(UIScriptManager.class) ;
    UIComponent parent = getParent() ;
    if(parent instanceof UICBScripts) {
      sManager.getChild(UICBScripts.class).refresh() ;
    } else {
      sManager.getChild(UIECMScripts.class).refresh() ;
    }
  }
  
  public String[] getActions() {return new String[]{"AddNew"} ;}
  
  public Node getScriptNode(String nodeName) throws Exception {
    UIComponent parent = getParent() ;
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    ScriptService scriptService =  getApplicationComponent(ScriptService.class) ;
    Node script = null  ;
    if(parent instanceof UIECMScripts) {
      UIECMFilterForm filterForm = parent.findFirstComponentOfType(UIECMFilterForm.class) ;
      String categoryName = 
        filterForm.getUIFormSelectBox(UIECMFilterForm.FIELD_SELECT_SCRIPT).getValue() ;
      Node category = scriptService.getECMScriptHome(repository).getNode(categoryName) ;
      script = category.getNode(nodeName) ;  
    } else {
      Node cbScript = scriptService.getCBScriptHome(repository) ;
      script = cbScript.getNode(nodeName) ; 
    }
    return script ;
  }

  static public class AddNewActionListener extends EventListener<UIScriptList> {
    public void execute(Event<UIScriptList> event) throws Exception {
      UIScriptList uiScriptList = event.getSource() ;
      UIScriptManager uiManager = uiScriptList.getAncestorOfType(UIScriptManager.class) ;
      if(uiScriptList.getId().equals(UIECMScripts.SCRIPTLIST_NAME)) {
        UIPopupAction uiPopup = uiScriptList.getAncestorOfType(UIECMScripts.class).getChild(UIPopupAction.class) ;
        UIScriptForm uiForm = uiPopup.activate(UIScriptForm.class, 600) ;
        uiForm.setId(UIECMScripts.SCRIPTFORM_NAME ) ;
        uiForm.update(null, true) ;
        uiManager.setRenderedChild(UIECMScripts.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
      } else if(uiScriptList.getId().equals(UICBScripts.SCRIPTLIST_NAME)) {
        UIPopupAction uiPopup = uiScriptList.getAncestorOfType(UICBScripts.class).getChild(UIPopupAction.class) ;
        UIScriptForm uiForm = uiPopup.activate(UIScriptForm.class, 600) ;
        uiForm.setId(UICBScripts.SCRIPTFORM_NAME ) ;
        uiForm.update(null, true) ;
        uiManager.setRenderedChild(UICBScripts.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
      }
    }
  }

  static public class EditActionListener extends EventListener<UIScriptList> {
    public void execute(Event<UIScriptList> event) throws Exception {
      UIScriptList uiScriptList = event.getSource() ;
      String scriptName = event.getRequestContext().getRequestParameter(OBJECTID); 
      UIScriptManager uiManager = uiScriptList.getAncestorOfType(UIScriptManager.class) ;      
      if(uiScriptList.getId().equals(UIECMScripts.SCRIPTLIST_NAME)) {
        UIPopupAction uiPopup = uiScriptList.getAncestorOfType(UIECMScripts.class).getChild(UIPopupAction.class) ;
        UIScriptForm uiForm = uiPopup.activate(UIScriptForm.class, 600) ;
        uiForm.setId(UIECMScripts.SCRIPTFORM_NAME ) ;
        uiForm.update(uiScriptList.getScriptNode(scriptName), false) ;
        uiManager.setRenderedChild(UIECMScripts.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
      } else if(uiScriptList.getId().equals(UICBScripts.SCRIPTLIST_NAME)) {
        UIPopupAction uiPopup = uiScriptList.getAncestorOfType(UICBScripts.class).getChild(UIPopupAction.class) ;
        UIScriptForm uiForm = uiPopup.activate(UIScriptForm.class, 600) ;
        uiForm.setId(UICBScripts.SCRIPTFORM_NAME ) ;
        uiForm.update(uiScriptList.getScriptNode(scriptName), false) ;
        uiManager.setRenderedChild(UICBScripts.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
      }
    }
  }

  static public class DeleteActionListener extends EventListener<UIScriptList> {
    public void execute(Event<UIScriptList> event) throws Exception {
      UIScriptList uiScriptList = event.getSource() ; 
      String repository = uiScriptList.getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
      ScriptService scriptService =  uiScriptList.getApplicationComponent(ScriptService.class) ;
      String scriptName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String namePrefix = uiScriptList.getScriptCategory() ;       
      scriptService.removeScript(namePrefix + "/" + scriptName, repository) ;
      uiScriptList.refresh() ;
      UIScriptManager uiManager = uiScriptList.getAncestorOfType(UIScriptManager.class) ;
      if((UIComponent)uiScriptList.getParent() instanceof UIECMScripts) {
        uiManager.setRenderedChild(UIECMScripts.class) ;
      } else  if((UIComponent)uiScriptList.getParent() instanceof UICBScripts){
        uiManager.setRenderedChild(UICBScripts.class) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiScriptList.getParent()) ;
    }
  }
  public static class ScriptData {
    private String name ;
    private String path ;
    private String baseVersion ;

    public ScriptData(String scriptName, String scriptParth, String version) {
      name = scriptName ;
      path = pathTitle(scriptParth, 30) ;   
      baseVersion = version ;
    }

    private String pathTitle(String inputStr, int defauLength) {
      String sortName = inputStr ;
      if(inputStr.length() > defauLength) {
        sortName = "..." + inputStr.substring(inputStr.length() - defauLength, inputStr.length()) ;
      }
      return sortName ;
    }
    public String getName() { return name ; }
    public String getPath() { return path ; }
    public String getBaseVersion() { return baseVersion ; }
  }
}