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
package org.exoplatform.ecm.webui.component.explorer.popup.admin;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.portlet.PortletPreferences;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.jcr.JCRExceptionManager;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentContainer;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentInfo;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorerPortlet;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UITreeExplorer;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 8, 2006
 * 9:41:56 AM 
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/explorer/popup/admin/UIActionList.gtmpl",
    events = {
        @EventConfig(listeners = UIActionList.ViewActionListener.class),
        @EventConfig(listeners = UIActionList.DeleteActionListener.class, confirm = "UIActionList.msg.confirm-delete-action"),
        @EventConfig(listeners = UIActionList.CloseActionListener.class),
        @EventConfig(listeners = UIActionList.EditActionListener.class)
    }
)
public class UIActionList extends UIContainer {
  
  final static public String[] ACTIONS = {"View", "Edit", "Delete"} ;
  

  public UIActionList() throws Exception {
    addChild(UIPageIterator.class, null, "ActionListIterator");
  }

  public void updateGrid(Node node) throws Exception {
    UIPageIterator uiIterator = getChild(UIPageIterator.class) ;
    ObjectPageList objPageList = new ObjectPageList(getAllActions(node), 10) ;
    uiIterator.setPageList(objPageList) ;
  }

  public String[] getActions() { return ACTIONS ; }

  public boolean hasActions() throws Exception{
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    ActionServiceContainer actionService = getApplicationComponent(ActionServiceContainer.class) ;
    return actionService.hasActions(uiExplorer.getCurrentNode());
  }

  public List<Node> getAllActions(Node node) {
    ActionServiceContainer actionService = getApplicationComponent(ActionServiceContainer.class) ;
    try {
      return actionService.getActions(node);
    } catch(Exception e){
      return new ArrayList<Node>() ;
    }
  }

  public List getListActions() throws Exception {
    UIPageIterator uiIterator = getChild(UIPageIterator.class) ;
    return uiIterator.getCurrentPageData() ; 
  }
  
  static public class ViewActionListener extends EventListener<UIActionList> {
    public void execute(Event<UIActionList> event) throws Exception {
      UIActionList uiActionList = event.getSource() ;
      String actionName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      UIActionManager uiActionManager = uiExplorer.findFirstComponentOfType(UIActionManager.class) ;
      ActionServiceContainer actionService = uiActionList.getApplicationComponent(ActionServiceContainer.class);      
      
      Node node = actionService.getAction(uiExplorer.getCurrentNode(),actionName);
      String nodeTypeName = node.getPrimaryNodeType().getName() ;
      String userName = event.getRequestContext().getRemoteUser() ;
      TemplateService templateService = uiActionList.getApplicationComponent(TemplateService.class) ;
      UIApplication uiApp = uiActionList.getAncestorOfType(UIApplication.class) ;
      String repository = 
        uiActionList.getAncestorOfType(UIJCRExplorerPortlet.class).getPreferenceRepository() ;
      try {
        String path = templateService.getTemplatePathByUser(false, nodeTypeName, userName, repository);
        if(path == null) {
          Object[] args = {actionName} ;
          uiApp.addMessage(new ApplicationMessage("UIActionList.msg.template-null", args, 
                                                  ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      } catch(PathNotFoundException path) {
        Object[] args = {actionName} ;
        uiApp.addMessage(new ApplicationMessage("UIActionList.msg.template-empty", args, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } catch(AccessDeniedException ace) {
        uiApp.addMessage(new ApplicationMessage("UIActionList.msg.access-denied", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } catch(Exception e) {
        JCRExceptionManager.process(uiApp, e) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(uiActionManager.getChild(UIActionViewContainer.class) != null) {
        uiActionManager.removeChild(UIActionViewContainer.class) ;
      }
      UIActionViewContainer uiActionViewContainer = 
        uiActionManager.createUIComponent(UIActionViewContainer.class, null, null) ;
      UIActionViewTemplate uiViewTemplate = 
        uiActionViewContainer.createUIComponent(UIActionViewTemplate.class, null, null) ;
      uiViewTemplate.setTemplateNode(node) ;
      uiActionViewContainer.addChild(uiViewTemplate) ;
      uiActionManager.addChild(uiActionViewContainer) ;
      uiActionManager.setRenderedChild(UIActionViewContainer.class) ;
    }
  }

  static public class EditActionListener extends EventListener<UIActionList> {
    public void execute(Event<UIActionList> event) throws Exception {
      UIActionList uiActionList = event.getSource() ;
      UIJCRExplorer uiExplorer = uiActionList.getAncestorOfType(UIJCRExplorer.class) ;
      UIActionListContainer uiActionListContainer = uiActionList.getParent() ;
      String actionName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      TemplateService templateService = uiActionList.getApplicationComponent(TemplateService.class) ;
      String userName = event.getRequestContext().getRemoteUser() ;
      String repository = 
        uiActionList.getAncestorOfType(UIJCRExplorerPortlet.class).getPreferenceRepository() ;
      Node currentNode = uiExplorer.getCurrentNode() ;
      ActionServiceContainer actionService = uiActionList.getApplicationComponent(ActionServiceContainer.class);
      Node selectedAction = null ;
      try {
        selectedAction = actionService.getAction(currentNode,actionName);
      } catch(PathNotFoundException path) {
        uiExplorer.getSession().refresh(false) ;
        UIDocumentContainer uiDocumentContainer = uiExplorer.findFirstComponentOfType(UIDocumentContainer.class) ;
        UIDocumentInfo uiDocumentInfo = uiDocumentContainer.getChild(UIDocumentInfo.class) ;
        uiDocumentInfo.updatePageListData();
        if(uiExplorer.isShowViewFile()) uiDocumentInfo.setRendered(false) ;
        else uiDocumentInfo.setRendered(true) ;
        if(uiExplorer.getPreference().isShowSideBar()) {
          UITreeExplorer treeExplorer = uiExplorer.findFirstComponentOfType(UITreeExplorer.class);
          treeExplorer.buildTree();
        }
        selectedAction = actionService.getAction(currentNode,actionName);
      }
      String nodeTypeName = selectedAction.getPrimaryNodeType().getName() ;
      UIApplication uiApp = uiActionList.getAncestorOfType(UIApplication.class) ;
      try {
        templateService.getTemplatePathByUser(true, nodeTypeName, userName, repository);
      } catch(PathNotFoundException path) {
        Object[] args = {actionName} ;
        uiApp.addMessage(new ApplicationMessage("UIActionList.msg.template-empty", args, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } catch(Exception e) {
        JCRExceptionManager.process(uiApp, e) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      uiActionListContainer.initEditPopup(selectedAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionListContainer) ;
    }
  }
  
  static public class CloseActionListener extends EventListener<UIActionList> {
    public void execute(Event<UIActionList> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.setIsHidePopup(false) ;
      uiExplorer.cancelAction() ;
    }
  }

  static public class DeleteActionListener extends EventListener<UIActionList> {
    public void execute(Event<UIActionList> event) throws Exception {
      UIActionList uiActionList = event.getSource() ;
      UIJCRExplorer uiExplorer = uiActionList.getAncestorOfType(UIJCRExplorer.class) ;
      ActionServiceContainer actionService = uiActionList.getApplicationComponent(ActionServiceContainer.class) ;
      String actionName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIActionListContainer uiActionListContainer = uiActionList.getParent() ;
      UIPopupWindow uiPopup = uiActionListContainer.getChildById("editActionPopup") ;
      UIApplication uiApp = uiActionList.getAncestorOfType(UIApplication.class) ;
      if(uiPopup != null && uiPopup.isShow()) {
        uiApp.addMessage(new ApplicationMessage("UIActionList.msg.remove-popup-first", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(uiPopup != null && uiPopup.isRendered()) uiActionListContainer.removeChildById("editActionPopup") ;
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext() ;
      PortletPreferences preferences = context.getRequest().getPreferences() ;
      try {
        actionService.removeAction(uiExplorer.getCurrentNode(), actionName, 
                                   preferences.getValue(Utils.REPOSITORY, "")) ;
      } catch(AccessDeniedException ace) {
        uiApp.addMessage(new ApplicationMessage("UIActionList.msg.access-denied", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIActionManager uiActionManager = uiExplorer.findFirstComponentOfType(UIActionManager.class) ;
      uiActionManager.removeChild(UIActionViewContainer.class) ;
      uiActionList.updateGrid(uiExplorer.getCurrentNode()) ;
      uiActionManager.setRenderedChild(UIActionListContainer.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionManager) ;
    }
  }
}
