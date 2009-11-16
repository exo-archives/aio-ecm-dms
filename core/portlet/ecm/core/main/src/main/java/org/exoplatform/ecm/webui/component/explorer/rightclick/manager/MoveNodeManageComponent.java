/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
 *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.rightclick.manager;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;

import org.apache.commons.logging.Log;
import org.exoplatform.ecm.webui.component.admin.manager.UIAbstractManager;
import org.exoplatform.ecm.webui.component.admin.manager.UIAbstractManagerComponent;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.ecm.webui.component.explorer.control.filter.IsNotFavouriteFilter;
import org.exoplatform.ecm.webui.component.explorer.control.filter.IsNotTrashHomeNodeFilter;
import org.exoplatform.ecm.webui.component.explorer.control.listener.UIWorkingAreaActionListener;
import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
import org.exoplatform.ecm.webui.utils.PermissionUtil;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;

/**
 * Created by The eXo Platform SARL
 * Author : Hoang Van Hung
 *          hunghvit@gmail.com
 * Aug 6, 2009  
 */

@ComponentConfig(
    events = {
      @EventConfig(listeners = MoveNodeManageComponent.MoveNodeActionListener.class, confirm="UIWorkingArea.msg.confirm-move")
    }
)

public class MoveNodeManageComponent extends UIAbstractManagerComponent {

	private static final List<UIExtensionFilter> FILTERS 
			= Arrays.asList(new UIExtensionFilter[]{ new IsNotTrashHomeNodeFilter() } );
	
	@UIExtensionFilters
	public List<UIExtensionFilter> getFilters() {
		return FILTERS;
	}

  private static final Log LOG = ExoLogger.getLogger(MoveNodeManageComponent.class);
  
  private static void processMultipleSelection(String nodePath, String destPath,
      Event<? extends UIComponent> event) throws Exception {
    UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
    Matcher matcher = UIWorkingArea.FILE_EXPLORER_URL_SYNTAX.matcher(destPath);
    String wsName = null;
    if (matcher.find()) {
      wsName = matcher.group(1);
      destPath = matcher.group(2);
    } else {
      throw new IllegalArgumentException("The ObjectId is invalid '" + destPath + "'");
    }
    Session session = uiExplorer.getSessionByWorkspace(wsName);
    UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
    if (destPath.startsWith(nodePath)) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.bound-move-exception", null,
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    }
    Node destNode;
    try {
      // Use the method getNodeByPath because it is link aware
      destNode = uiExplorer.getNodeByPath(destPath, session);
      // Reset the path to manage the links that potentially create virtual path
      destPath = destNode.getPath();
      // Reset the session to manage the links that potentially change of
      // workspace
      session = destNode.getSession();
    } catch (PathNotFoundException path) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", null,
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    } catch (Exception e) {
      JCRExceptionManager.process(uiApp, e);
      return;
    }
    if (!PermissionUtil.canAddNode(destNode)) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-move-node", null,
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
    }
    if (uiExplorer.nodeIsLocked(destNode)) {
      Object[] arg = { destPath };
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg,
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    }
    if (!destNode.isCheckedOut()) {
      uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    }
    try {
      if (nodePath.indexOf(";") > -1) {
          moveMultiNode(nodePath.split(";"), destNode, event);
      } else {
          moveNode(nodePath, destNode, event);
      }
      session.save();
      uiExplorer.updateAjax(event);
    } catch (AccessDeniedException ace) {
      Object[] arg = { destPath };
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.has-not-add-permission", arg,
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    } catch (LockException lock) {
      Object[] arg = { nodePath };
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg,
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    } catch (ConstraintViolationException constraint) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.move-constraint-exception", null,
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    } catch (Exception e) {
      LOG.error("an unexpected error occurs while selecting the node", e);
      JCRExceptionManager.process(uiApp, e);
      return;
    }
  }
  
  private static void moveNode(String srcPath, Node destNode, Event<?> event) throws Exception {
    UIComponent uiComponent = (UIComponent)event.getSource();
    UIJCRExplorer uiExplorer = uiComponent.getAncestorOfType(UIJCRExplorer.class);
    Matcher matcher = UIWorkingArea.FILE_EXPLORER_URL_SYNTAX.matcher(srcPath);
    String wsName = null;
    UIApplication uiApp = uiComponent.getAncestorOfType(UIApplication.class);
    if (srcPath.indexOf(":/") > -1) {
      String[] arrSrcPath = srcPath.split(":/");
      if (("/" + arrSrcPath[1]).equals(destNode.getPath())) {
        uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.can-not-move-to-itself", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
    }
    if (matcher.find()) {
      wsName = matcher.group(1);
      srcPath = matcher.group(2);
    } else {
      throw new IllegalArgumentException("The ObjectId is invalid '" + srcPath + "'");
    }
    Session srcSession = uiExplorer.getSessionByWorkspace(wsName);
    // Use the method getNodeByPath because it is link aware
    Node selectedNode = uiExplorer.getNodeByPath(srcPath, srcSession, false);
    // Reset the path to manage the links that potentially create virtual path
    srcPath = selectedNode.getPath();
    // Reset the session to manage the links that potentially change of
    // workspace
    srcSession = selectedNode.getSession();

    if (!selectedNode.isCheckedOut()) {
      uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null,
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    }
    uiExplorer.addLockToken(selectedNode);
    String destPath = destNode.getPath();
    String messagePath = destPath;
    if (destPath.endsWith("/")) {
      destPath = destPath + srcPath.substring(srcPath.lastIndexOf("/") + 1);
    } else {
      destPath = destPath + srcPath.substring(srcPath.lastIndexOf("/"));  
    }
    Workspace srcWorkspace = srcSession.getWorkspace();
    Workspace destWorkspace = destNode.getSession().getWorkspace();
    try {
      if (srcWorkspace.equals(destWorkspace)) {
        srcWorkspace.move(srcPath, destPath);
      } else {
        destWorkspace.clone(srcWorkspace.getName(), srcPath, destPath, false);
      }
    } catch (Exception e) {
      Object[] args = { srcPath, messagePath };
      uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.move-problem", args,
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    }
  }

  private static void moveMultiNode(String[] srcPaths, Node destNode, Event<? extends UIComponent> event) throws Exception {
    for (int i = 0; i < srcPaths.length; i++) {
      moveNode(srcPaths[i], destNode, event);
    }
  }

  public static class MoveNodeActionListener extends UIWorkingAreaActionListener<MoveNodeManageComponent> {
    public void processEvent(Event<MoveNodeManageComponent> event) throws Exception {
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      String destPath = event.getRequestContext().getRequestParameter("destInfo");
      MoveNodeManageComponent.processMultipleSelection(nodePath.trim(), destPath.trim(), event);
    }
  }
  @Override
  public Class<? extends UIAbstractManager> getUIAbstractManagerClass() {
    return null;
  }

}
