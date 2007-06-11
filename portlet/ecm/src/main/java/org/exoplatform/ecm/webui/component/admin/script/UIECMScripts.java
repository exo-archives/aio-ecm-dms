/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.script;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.admin.script.UIScriptList.ScriptData;
import org.exoplatform.services.cms.scripts.ScriptService;
import org.exoplatform.webui.bean.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@yahoo.de
 * September 27, 2006
 * 09:22:15 AM
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)

public class UIECMScripts extends UIContainer {
  public static String SCRIPTLIST_NAME = "ECMScriptList" ;
  public static String SCRIPTFORM_NAME = "ECMScriptForm" ;
  
  public UIECMScripts() throws Exception {
    addChild(UIECMFilterForm.class, null, null) ;
    addChild(UIScriptList.class, null, SCRIPTLIST_NAME) ;
  }

  private List<SelectItemOption<String>>getECMCategoryOptions() throws Exception {
    List<SelectItemOption<String>> ecmOptions = new ArrayList<SelectItemOption<String>>() ;
    ScriptService scriptService = getApplicationComponent(ScriptService.class) ;
    Node ecmScriptHome = scriptService.getECMScriptHome() ;
    NodeIterator categories = ecmScriptHome.getNodes() ;
    while(categories.hasNext()) {
      Node script = categories.nextNode() ;
      ecmOptions.add(new SelectItemOption<String>(script.getName(),script.getName())) ;
    }
    return ecmOptions ;
  }

  public void refresh () throws Exception {
    UIECMFilterForm ecmFilterForm = getChild(UIECMFilterForm.class) ;
    ecmFilterForm.setOptions(getECMCategoryOptions()) ;
    String categoryName = 
      ecmFilterForm.getUIFormSelectBox(UIECMFilterForm.FIELD_SELECT_SCRIPT).getValue() ;    
    UIScriptList uiScriptList = getChildById(SCRIPTLIST_NAME) ;
    uiScriptList.updateGrid(getECMScript(categoryName)) ;
  }

  public void initPopup(UIComponent uiRuleForm) throws Exception {
    UIPopupWindow uiPopup = getChild(UIPopupWindow.class) ;
    if(uiPopup == null) {
      uiPopup = addChild(UIPopupWindow.class, null, "UIECMScriptsPopup") ;     
      uiPopup.setUIComponent(uiRuleForm) ;
      uiPopup.setWindowSize(600, 0) ;
      uiPopup.setShow(true) ;
      return ;
    }
    uiPopup.setRendered(true) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }
  
  public List<ScriptData> getECMScript(String name) throws Exception {
    List <ScriptData> scriptData = new ArrayList <ScriptData>() ;
    ScriptService scriptService = getApplicationComponent(ScriptService.class) ;
    Node ecmCategory = scriptService.getECMScriptHome().getNode(name) ;
    NodeIterator nodeList = ecmCategory.getNodes() ;
    ScriptData script ;
    while(nodeList.hasNext()) {
      Node node = nodeList.nextNode() ;
      String version = "" ;
      if(node.isNodeType(Utils.MIX_VERSIONABLE) && !node.isNodeType(Utils.NT_FROZEN)){
        version = node.getBaseVersion().getName();
      }
      script = new ScriptData(node.getName(), node.getPath(), version) ;
      scriptData.add(script) ;
    }
    return scriptData ;
  }
}