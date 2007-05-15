/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.script;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.services.cms.scripts.ScriptService;
import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.component.UIGrid;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
        @EventConfig(listeners = UIScriptList.AddNewActionListener.class),
        @EventConfig(listeners = UIScriptList.ShowPageActionListener.class )
    }
)

public class UIScriptList extends UIGrid {

  private static String[] BEAN_FIELD = {"name", "path", "baseVersion"} ;
  private static String[] ACTIONS = {"Edit", "Delete"} ;

  public UIScriptList() throws Exception { 
    getUIPageIterator().setId("ScriptListIterator") ;
    configure("name", BEAN_FIELD, ACTIONS) ;
  }

  public void updateGrid(List<ScriptData> scriptData) throws Exception {
    ObjectPageList objPageList = new ObjectPageList(scriptData, 10) ;
    getUIPageIterator().setPageList(objPageList) ;
  }

  public String getScriptCategory() throws Exception {
    UIComponent parent = getParent() ;
    ScriptService scriptService =  getApplicationComponent(ScriptService.class) ;
    Node script = null ;
    if(parent instanceof UIECMScripts) {
      UIECMFilterForm filterForm = parent.findFirstComponentOfType(UIECMFilterForm.class) ;
      String categoryName = 
        filterForm.getUIFormSelectBox(UIECMFilterForm.FIELD_SELECT_SCRIPT).getValue() ;
      script = scriptService.getECMScriptHome().getNode(categoryName) ;
    } else {
      script = scriptService.getCBScriptHome() ;
    }
    String basePath = scriptService.getBaseScriptPath() + "/" ;
    return script.getPath().substring(basePath.length()) ;
  }

  public void refresh() throws Exception {
    UIScriptManager sManager = getAncestorOfType(UIScriptManager.class) ;
    UICBScripts uiCBScripts = sManager.getChild(UICBScripts.class) ;
    UIECMScripts uiECMScripts = sManager.getChild(UIECMScripts.class) ;
    UIComponent parent = getParent() ;
    if(parent instanceof UICBScripts) {
      uiCBScripts.refresh() ;
    } else {
      uiECMScripts.refresh() ;
    }
  }

  public void setSelectedTab() {
    UIComponent parent = getParent() ;
    UIScriptManager sManager = getAncestorOfType(UIScriptManager.class) ;
    UICBScripts uiCBScripts = sManager.getChild(UICBScripts.class) ;
    UIECMScripts uiECMScripts = sManager.getChild(UIECMScripts.class) ;
    if(parent instanceof UICBScripts) {
      uiCBScripts.setRendered(true) ;
      uiECMScripts.setRendered(false) ;
    } else {
      uiECMScripts.setRendered(true) ;
      uiCBScripts.setRendered(false) ;
    }
  }

  public String[] getActions() {return new String[]{"AddNew"} ;}

  public Node getScriptNode(String nodeName) throws Exception {
    UIComponent parent = getParent() ;
    ScriptService scriptService =  getApplicationComponent(ScriptService.class) ;
    Node script = null  ;
    if(parent instanceof UIECMScripts) {
      UIECMFilterForm filterForm = parent.findFirstComponentOfType(UIECMFilterForm.class) ;
      String categoryName = 
        filterForm.getUIFormSelectBox(UIECMFilterForm.FIELD_SELECT_SCRIPT).getValue() ;
      Node category = scriptService.getECMScriptHome().getNode(categoryName) ;
      script = category.getNode(nodeName) ;  
    } else {
      Node cbScript = scriptService.getCBScriptHome() ;
      script = cbScript.getNode(nodeName) ; 
    }
    return script ;
  }

  static public class AddNewActionListener extends EventListener<UIScriptList> {
    public void execute(Event<UIScriptList> event) throws Exception {
      UIScriptList uiScriptList = event.getSource() ;
      UIComponent parent = uiScriptList.getParent() ;
      UIScriptManager sManager = uiScriptList.getAncestorOfType(UIScriptManager.class) ;
      UICBScripts uiCBScripts = sManager.getChild(UICBScripts.class) ;
      UIECMScripts uiECMScripts = sManager.getChild(UIECMScripts.class) ;
      if(parent instanceof UICBScripts) {
        uiCBScripts.setRendered(true) ;
        UIScriptForm CBScriptForm = uiCBScripts.findFirstComponentOfType(UIScriptForm.class) ;
        if(CBScriptForm == null) CBScriptForm = 
          uiCBScripts.createUIComponent(UIScriptForm.class, null,UICBScripts.SCRIPTFORM_NAME) ;
        CBScriptForm.update(null, true) ;
        uiCBScripts.initPopup(CBScriptForm) ;
        uiECMScripts.setRendered(false) ;
      } else {
        uiECMScripts.setRendered(true) ;
        UIScriptForm uiECMScriptForm =  uiECMScripts.findFirstComponentOfType(UIScriptForm.class) ;
        if(uiECMScriptForm == null) uiECMScriptForm = 
          uiCBScripts.createUIComponent(UIScriptForm.class, null,UIECMScripts.SCRIPTFORM_NAME) ;
        uiECMScriptForm.update(null, true) ;
        uiECMScripts.initPopup(uiECMScriptForm) ;
        uiCBScripts.setRendered(false) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiScriptList.getParent()) ;
    }
  }

  static public class EditActionListener extends EventListener<UIScriptList> {
    public void execute(Event<UIScriptList> event) throws Exception {
      UIScriptList uiScriptList = event.getSource() ;
      String scriptName = event.getRequestContext().getRequestParameter(OBJECTID); 
      UIScriptManager sManager = uiScriptList.getAncestorOfType(UIScriptManager.class) ;
      UICBScripts uiCBScripts = sManager.getChild(UICBScripts.class) ;
      UIECMScripts uiECMScripts = sManager.getChild(UIECMScripts.class) ;
      if(uiScriptList.getId().equals(UICBScripts.SCRIPTLIST_NAME)){
        uiCBScripts.setRendered(true) ;
        UIScriptForm ScriptForm = sManager.findComponentById(UICBScripts.SCRIPTFORM_NAME) ;
        if(ScriptForm == null) {
          ScriptForm = uiCBScripts.createUIComponent(UIScriptForm.class, null,UICBScripts.SCRIPTFORM_NAME) ;
        } 
        ScriptForm.update(uiScriptList.getScriptNode(scriptName), false) ;
        uiCBScripts.initPopup(ScriptForm) ;
        uiECMScripts.setRendered(false) ;
      } else {
        uiECMScripts.setRendered(true) ;
        UIScriptForm ScriptForm = sManager.findComponentById(UIECMScripts.SCRIPTFORM_NAME) ;
        if(ScriptForm == null) {
          ScriptForm = uiCBScripts.createUIComponent(UIScriptForm.class, null,UIECMScripts.SCRIPTFORM_NAME) ;
        } 
        ScriptForm.update(uiScriptList.getScriptNode(scriptName), false) ;
        uiECMScripts.initPopup(ScriptForm) ;
        uiCBScripts.setRendered(false) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiScriptList.getParent()) ;
    }
  }

  static public class DeleteActionListener extends EventListener<UIScriptList> {
    public void execute(Event<UIScriptList> event) throws Exception {
      UIScriptList uiScriptList = event.getSource() ;      
      ScriptService scriptService =  uiScriptList.getApplicationComponent(ScriptService.class) ;
      String scriptName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String namePrefix = uiScriptList.getScriptCategory() ;       
      scriptService.removeScript(namePrefix + "/" + scriptName) ;
      uiScriptList.refresh() ;
      uiScriptList.setSelectedTab() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiScriptList.getParent()) ;
    }
  }
  
  static  public class ShowPageActionListener extends EventListener<UIScriptList> {
    public void execute(Event<UIScriptList> event) throws Exception {
      System.out.println("\n\nGo here\n\n");
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getParent()) ;
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