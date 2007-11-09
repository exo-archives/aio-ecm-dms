/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.script;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.ecm.utils.SessionsUtils;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.ecm.webui.component.admin.script.UIScriptList.ScriptData;
import org.exoplatform.services.cms.scripts.ScriptService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;

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
  public static String SCRIPT_PAGE =  "PageIterator" ;

  public UIECMScripts() throws Exception {
    addChild(UIECMFilterForm.class, null, null) ;
    UIScriptList list = addChild(UIScriptList.class, null, SCRIPTLIST_NAME) ;
    list.getUIPageIterator().setId(SCRIPTLIST_NAME + SCRIPT_PAGE) ;
    UIPopupAction uiPopupAction = addChild(UIPopupAction.class,null, "ECMScriptPopupAction") ;
    uiPopupAction.getChild(UIPopupWindow.class).setId("ECMScriptPopupWindow") ;
  }

  private List<SelectItemOption<String>> getECMCategoryOptions() throws Exception {
    List<SelectItemOption<String>> ecmOptions = new ArrayList<SelectItemOption<String>>() ;
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    Node ecmScriptHome = getApplicationComponent(ScriptService.class).getECMScriptHome(repository,SessionsUtils.getSystemProvider()) ;
    NodeIterator categories = ecmScriptHome.getNodes() ;
    while(categories.hasNext()) {
      Node script = categories.nextNode() ;
      ecmOptions.add(new SelectItemOption<String>(script.getName(),script.getName())) ;
    }
    return ecmOptions ;
  }

  public void refresh() throws Exception {
    UIECMFilterForm ecmFilterForm = getChild(UIECMFilterForm.class) ;
    String categoryName = 
      ecmFilterForm.getUIFormSelectBox(UIECMFilterForm.FIELD_SELECT_SCRIPT).getValue() ;  
    if (categoryName == null)  {
      ecmFilterForm.setOptions(getECMCategoryOptions()) ; 
      categoryName = ecmFilterForm.getUIFormSelectBox(UIECMFilterForm.FIELD_SELECT_SCRIPT).getValue() ;  
    }
    UIScriptList uiScriptList = getChildById(SCRIPTLIST_NAME) ;
    uiScriptList.updateGrid(getECMScript(categoryName)) ;
  }
  
  public List<ScriptData> getECMScript(String name) throws Exception {
    List <ScriptData> scriptData = new ArrayList <ScriptData>() ;
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    List<Node> scripts = new ArrayList<Node> () ;
    if(name.equals("action")) {
      scripts = getApplicationComponent(ScriptService.class).getECMActionScripts(repository,SessionsUtils.getSystemProvider()) ;
    }else if(name.equals("widget")){
      scripts = getApplicationComponent(ScriptService.class).getECMWidgetScripts(repository,SessionsUtils.getSessionProvider()) ;
    }else if(name.equals("interceptor")) {
      scripts = getApplicationComponent(ScriptService.class).getECMInterceptorScripts(repository,SessionsUtils.getSystemProvider()) ;
    }
    for(Node scriptNode : scripts) {
      String version = "" ;
      if(scriptNode.isNodeType(Utils.MIX_VERSIONABLE) && !scriptNode.isNodeType(Utils.NT_FROZEN)){
        version = scriptNode.getBaseVersion().getName();
      }
      scriptData.add(new ScriptData(scriptNode.getName(), scriptNode.getPath(), version)) ;
    }
    return scriptData ;
  }
}