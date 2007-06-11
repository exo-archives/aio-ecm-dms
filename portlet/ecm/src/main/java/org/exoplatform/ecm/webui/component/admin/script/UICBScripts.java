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

@ComponentConfig( lifecycle = UIContainerLifecycle.class )

public class UICBScripts extends UIContainer {
  public static String SCRIPTLIST_NAME = "CBScriptList" ;
  public static String SCRIPTFORM_NAME = "CBScriptForm" ;
  
  public UICBScripts() throws Exception {
    addChild(UIScriptList.class, null, SCRIPTLIST_NAME) ;
  }

  public void refresh () throws Exception {
    UIScriptList uiScriptList = getChildById(SCRIPTLIST_NAME) ;
    uiScriptList.updateGrid(getCBScript()) ;
  }

  public void initPopup(UIComponent uiComp) throws Exception {
    UIPopupWindow uiPopup = getChild(UIPopupWindow.class) ;
    if(uiPopup == null) {
      uiPopup = addChild(UIPopupWindow.class, null, "UICBScriptsPopup") ;     
      uiPopup.setUIComponent(uiComp) ;
      uiPopup.setWindowSize(600, 0) ;
      uiPopup.setShow(true) ;
      return ;
    }
    uiPopup.setRendered(true) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }
  
  public List<ScriptData> getCBScript() throws Exception {
    List <ScriptData> scriptData = new ArrayList <ScriptData>() ;
    ScriptService scriptService = getApplicationComponent(ScriptService.class) ;
    Node cbScripts = scriptService.getCBScriptHome() ;
    NodeIterator nodeList = cbScripts.getNodes() ;
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