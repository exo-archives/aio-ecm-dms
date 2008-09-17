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

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
import org.exoplatform.ecm.webui.utils.LockUtil;
import org.exoplatform.services.cms.categories.CategoriesService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 18, 2006
 * 2:28:18 PM 
 */
@ComponentConfig(
    lifecycle = UIContainerLifecycle.class,
    events = {
      @EventConfig(listeners = UICategoriesAddedList.DeleteActionListener.class, confirm="UICategoriesAddedList.msg.confirm-delete")
    }
)
public class UICategoriesAddedList extends UIContainer implements UISelectable{

  private static String[] CATE_BEAN_FIELD = {"path"} ;
  private static String[] ACTION = {"Delete"} ;
  final public static int DISPLAY_CHARS = 100;
  final public static int ITEMS_PER_PAGES = 10;

  public UICategoriesAddedList() throws Exception {
    UIGrid uiGrid = addChild(UIGrid.class, null, "CateAddedList") ;
    uiGrid.setDisplayedChars(DISPLAY_CHARS);
    uiGrid.getUIPageIterator().setId("CategoriesListIterator");
    uiGrid.configure("path", CATE_BEAN_FIELD, ACTION) ;
  }
  
  public void updateGrid(List<Node> nodes) throws Exception {
    UIGrid uiGrid = getChild(UIGrid.class) ;   
    if(nodes == null) nodes = new ArrayList<Node>() ;
    ObjectPageList objPageList = new ObjectPageList(nodes, ITEMS_PER_PAGES) ;
    uiGrid.getUIPageIterator().setPageList(objPageList) ;
  }
  
  @SuppressWarnings("unused")
  public void doSelect(String selectField, Object value) throws Exception {
    UIJCRExplorer uiJCRExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    CategoriesService categoriesService = getApplicationComponent(CategoriesService.class) ;
    try {
      Node currentNode = uiJCRExplorer.getCurrentNode();
      if(currentNode.isLocked()) {
        String lockToken = LockUtil.getLockToken(currentNode);
        if(lockToken != null) uiJCRExplorer.getSession().addLockToken(lockToken);
      }
      categoriesService.addCategory(currentNode, value.toString(), uiJCRExplorer.getRepositoryName()) ;
      uiJCRExplorer.getCurrentNode().save() ;
      uiJCRExplorer.getSession().save() ;
      updateGrid(categoriesService.getCategories(uiJCRExplorer.getCurrentNode(), uiJCRExplorer.getRepositoryName())) ;
      setRenderSibbling(UICategoriesAddedList.class) ;
    } catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  static public class DeleteActionListener extends EventListener<UICategoriesAddedList> {
    public void execute(Event<UICategoriesAddedList> event) throws Exception {
      UICategoriesAddedList uiAddedList = event.getSource() ;
      UICategoryManager uiManager = uiAddedList.getParent() ;
      UIApplication uiApp = uiAddedList.getAncestorOfType(UIApplication.class) ;
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRExplorer uiExplorer = uiAddedList.getAncestorOfType(UIJCRExplorer.class) ;
      CategoriesService categoriesService = 
        uiAddedList.getApplicationComponent(CategoriesService.class) ;
      try {
        categoriesService.removeCategory(uiExplorer.getCurrentNode(), nodePath, uiExplorer.getRepositoryName()) ;
        uiAddedList.updateGrid(categoriesService.getCategories(uiExplorer.getCurrentNode(), uiExplorer.getRepositoryName())) ;
      } catch(AccessDeniedException ace) {
        throw new MessageException(new ApplicationMessage("UICategoriesAddedList.msg.access-denied",
                                   null, ApplicationMessage.WARNING)) ;
      } catch(Exception e) {
        JCRExceptionManager.process(uiApp, e) ;
      }
      uiManager.setRenderedChild("UICategoriesAddedList") ;
    }
  }
}
