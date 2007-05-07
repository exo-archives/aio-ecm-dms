/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.admin;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPageIterator;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
        @EventConfig(listeners = UIActionList.DeleteActionListener.class),
        @EventConfig(listeners = UIActionList.CloseActionListener.class),
        @EventConfig(listeners = UIActionList.EditActionListener.class)
    }
)
public class UIActionList extends UIContainer {
  
  final static public String[] ACTIONS = {"Edit","View","Delete"} ;
  

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
      UIActionListContainer uiActionListContainer = event.getSource().getParent() ;
      String actionName = event.getRequestContext().getRequestParameter(OBJECTID) ;
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
      actionService.removeAction(uiExplorer.getCurrentNode(), actionName) ;
      uiActionList.updateGrid(uiExplorer.getCurrentNode()) ;
      uiActionList.setRenderSibbling(UIActionList.class) ;
    }
  }
}
