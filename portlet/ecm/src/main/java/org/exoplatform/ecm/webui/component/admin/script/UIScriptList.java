/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ecm.webui.component.admin.script;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.ecm.webui.popup.UIPopupContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.scripts.ScriptService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIComponentDecorator;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@yahoo.de
 * September 27, 2006
 * 10:37:15 AM
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/admin/script/UIScriptList.gtmpl",
    events = {
        @EventConfig(listeners = UIScriptList.EditActionListener.class),
        @EventConfig(listeners = UIScriptList.DeleteActionListener.class, confirm="UIScriptList.msg.confirm-delete"),
        @EventConfig(listeners = UIScriptList.AddNewActionListener.class)
    }
)

public class UIScriptList extends UIComponentDecorator {

  private UIPageIterator uiPageIterator_ ;
  
  public UIScriptList() throws Exception { 
    uiPageIterator_ = createUIComponent(UIPageIterator.class, null, "ScriptListIterator");
    setUIComponent(uiPageIterator_) ;
  }

  @SuppressWarnings("unchecked")
  public void updateGrid(List<ScriptData> scriptData) throws Exception {
    Collections.sort(scriptData, new ScriptComparator()) ;
    ObjectPageList objPageList = new ObjectPageList(scriptData, 10) ;
    uiPageIterator_.setPageList(objPageList) ;
  }
  
  public UIPageIterator getUIPageIterator() { return uiPageIterator_ ; }
  
  public List getScriptList() throws Exception { return uiPageIterator_.getCurrentPageData() ; }

  public String getScriptCategory() throws Exception {
    UIComponent parent = getParent() ;
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    ScriptService scriptService =  getApplicationComponent(ScriptService.class) ;
    Node script = null ;
    if(parent instanceof UIECMScripts) {
      UIECMFilterForm filterForm = parent.findFirstComponentOfType(UIECMFilterForm.class) ;
      String categoryName = 
        filterForm.getUIFormSelectBox(UIECMFilterForm.FIELD_SELECT_SCRIPT).getValue() ;
      script = scriptService.getECMScriptHome(repository, 
          SessionProviderFactory.createSystemProvider()).getNode(categoryName) ;
    } else {
      script = scriptService.getCBScriptHome(repository, SessionProviderFactory.createSystemProvider()) ;
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
      Node category = scriptService.getECMScriptHome(repository, 
          SessionProviderFactory.createSessionProvider()).getNode(categoryName) ;
      script = category.getNode(nodeName) ;  
    } else {
      Node cbScript = scriptService.getCBScriptHome(repository, 
          SessionProviderFactory.createSystemProvider()) ;
      script = cbScript.getNode(nodeName) ; 
    }
    return script ;
  }
  
  static public class ScriptComparator implements Comparator {
    public int compare(Object o1, Object o2) throws ClassCastException {
      String name1 = ((ScriptData) o1).getName() ;
      String name2 = ((ScriptData) o2).getName() ;
      return name1.compareToIgnoreCase(name2) ;
    }
  }

  static public class AddNewActionListener extends EventListener<UIScriptList> {
    public void execute(Event<UIScriptList> event) throws Exception {
      UIScriptList uiScriptList = event.getSource() ;
      UIScriptManager uiManager = uiScriptList.getAncestorOfType(UIScriptManager.class) ;
      if(uiScriptList.getId().equals(UIECMScripts.SCRIPTLIST_NAME)) {
        UIPopupContainer uiPopup = 
          uiScriptList.getAncestorOfType(UIECMScripts.class).getChild(UIPopupContainer.class) ;
        UIScriptForm uiForm = uiPopup.activate(UIScriptForm.class, 600) ;
        uiForm.setId(UIECMScripts.SCRIPTFORM_NAME ) ;
        uiForm.update(null, true) ;
        uiManager.setRenderedChild(UIECMScripts.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
      } else if(uiScriptList.getId().equals(UICBScripts.SCRIPTLIST_NAME)) {
        UIPopupContainer uiPopup = 
          uiScriptList.getAncestorOfType(UICBScripts.class).getChild(UIPopupContainer.class) ;
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
        UIPopupContainer uiPopup = 
          uiScriptList.getAncestorOfType(UIECMScripts.class).getChild(UIPopupContainer.class) ;
        UIScriptForm uiForm = uiPopup.activate(UIScriptForm.class, 680) ;
        uiForm.setId(UIECMScripts.SCRIPTFORM_NAME ) ;
        uiForm.update(uiScriptList.getScriptNode(scriptName), false) ;
        uiManager.setRenderedChild(UIECMScripts.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
      } else if(uiScriptList.getId().equals(UICBScripts.SCRIPTLIST_NAME)) {
        UIPopupContainer uiPopup = 
          uiScriptList.getAncestorOfType(UICBScripts.class).getChild(UIPopupContainer.class) ;
        UIScriptForm uiForm = uiPopup.activate(UIScriptForm.class, 680) ;
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
      try {
        scriptService.removeScript(namePrefix + "/" + scriptName, repository, 
            SessionProviderFactory.createSessionProvider()) ;
      } catch(AccessDeniedException ace) {
        throw new MessageException(new ApplicationMessage("UIECMAdminControlPanel.msg.access-denied", 
                                                          null, ApplicationMessage.WARNING)) ;
      }
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
      path = scriptParth ;   
      baseVersion = version ;
    }
    public String getName() { return name ; }
    public String getPath() { return path ; }
    public String getBaseVersion() { return baseVersion ; }
  }
}