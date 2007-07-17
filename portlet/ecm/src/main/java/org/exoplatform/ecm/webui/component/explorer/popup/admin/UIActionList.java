/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.admin;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.portlet.PortletPreferences;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorerPortlet;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
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
      String actionName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      UIActionManager uiActionManager = uiExplorer.findFirstComponentOfType(UIActionManager.class) ;
      Node node = uiExplorer.getCurrentNode().getNode(actionName) ;
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
      Node selectedNode = currentNode.getNode(actionName) ;
      String nodeTypeName = selectedNode.getPrimaryNodeType().getName() ;
      String dialogPath = templateService.getTemplatePathByUser(true, nodeTypeName, userName, repository);
      if(dialogPath == null || dialogPath.trim().length() == 0) {
        UIApplication uiApp = uiActionList.getAncestorOfType(UIApplication.class) ;
        Object[] args = {actionName} ;
        uiApp.addMessage(new ApplicationMessage("UIActionList.msg.template-empty", args, 
                         ApplicationMessage.WARNING)) ;
        return ;
      }
      uiActionListContainer.initEditPopup(actionName) ;
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
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext() ;
      PortletPreferences preferences = context.getRequest().getPreferences() ;
      actionService.removeAction(uiExplorer.getCurrentNode(), actionName, 
                                 preferences.getValue(Utils.REPOSITORY, "")) ;
      UIActionManager uiActionManager = uiExplorer.findFirstComponentOfType(UIActionManager.class) ;
      uiActionManager.removeChild(UIActionViewContainer.class) ;
      uiActionList.updateGrid(uiExplorer.getCurrentNode()) ;
      uiActionManager.setRenderedChild(UIActionListContainer.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionManager) ;
    }
  }
}
