/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.script;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.utils.SessionsUtils;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.admin.script.UIScriptList.ScriptData;
import org.exoplatform.services.cms.scripts.ScriptService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
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
  public static String SCRIPT_PAGE =  "PageIterator" ;

  public UICBScripts() throws Exception {
    UIScriptList list = addChild(UIScriptList.class, null, SCRIPTLIST_NAME) ;
    list.getUIPageIterator().setId(SCRIPTLIST_NAME + SCRIPT_PAGE) ;
    UIPopupAction uiPopupAction = addChild(UIPopupAction.class,null, "BCScriptPopupAction") ;
    uiPopupAction.getChild(UIPopupWindow.class).setId("BCScriptPopupWindow") ;
  }

  public void refresh () throws Exception {
    UIScriptList uiScriptList = getChildById(SCRIPTLIST_NAME) ;
    uiScriptList.updateGrid(getCBScript()) ;
  }

  public List<ScriptData> getCBScript() throws Exception {
    List <ScriptData> scriptData = new ArrayList <ScriptData>() ;
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
    String repository =  portletPref.getValue(Utils.REPOSITORY, "") ;
    ScriptService scriptService = getApplicationComponent(ScriptService.class) ;
    Node cbScripts = scriptService.getCBScriptHome(repository,SessionsUtils.getSystemProvider()) ;
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