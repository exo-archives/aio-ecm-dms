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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.VersionException;

import org.apache.commons.logging.Log;
import org.exoplatform.ecm.webui.component.admin.manager.UIAbstractManager;
import org.exoplatform.ecm.webui.component.admin.manager.UIAbstractManagerComponent;
import org.exoplatform.ecm.webui.component.explorer.UIConfirmMessage;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.ecm.webui.component.explorer.control.filter.CanDeleteNodeFilter;
import org.exoplatform.ecm.webui.component.explorer.control.filter.IsNotLockedFilter;
import org.exoplatform.ecm.webui.component.explorer.control.listener.UIWorkingAreaActionListener;
import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.services.cms.link.LinkUtils;
import org.exoplatform.services.cms.thumbnail.ThumbnailService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupContainer;
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
      @EventConfig(listeners = DeleteManageComponent.DeleteActionListener.class)
    }
)

public class DeleteManageComponent extends UIAbstractManagerComponent {

  private static final Log LOG = ExoLogger.getLogger(DeleteManageComponent.class);
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[]{new IsNotLockedFilter(), new CanDeleteNodeFilter()});
  
  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }
  
  private void processRemoveMultiple(String[] nodePaths, Event<?> event) throws Exception {
    Node node = null;
    String wsName = null;
    String nodePath = null;
    Session session = null;
    Map<String, Node> mapNode = new HashMap<String, Node>();
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
    for (int i = 0; i < nodePaths.length; i++) {
      Matcher matcher = UIWorkingArea.FILE_EXPLORER_URL_SYNTAX.matcher(nodePaths[i]);
      // prepare to remove
      if (matcher.find()) {
        wsName = matcher.group(1);
        nodePath = matcher.group(2);
        try {
          session = uiExplorer.getSessionByWorkspace(wsName);
          // Use the method getNodeByPath because it is link aware
          node = uiExplorer.getNodeByPath(nodePath, session, false);
          // Reset the session to manage the links that potentially change of
          // workspace
          session = node.getSession();
          // Reset the workspace name to manage the links that potentially
          // change of workspace
          wsName = session.getWorkspace().getName();
          mapNode.put(nodePath, node);
        } catch (PathNotFoundException path) {
          uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", null,
              ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        } catch (Exception e) {
          JCRExceptionManager.process(uiApp, e);
        }
      } else {
        throw new IllegalArgumentException("The ObjectId is invalid '" + nodePath + "'");
      }
    }

    String path = null;
    Iterator<String> iterator = mapNode.keySet().iterator();
    while (iterator.hasNext()) {
      path = iterator.next();
      processRemove(path, mapNode.get(path), event, true);
    }
  }
  
  private void removeMixins(Node node) throws Exception {
    NodeType[] mixins = node.getMixinNodeTypes();
    for (NodeType nodeType : mixins) {
      node.removeMixin(nodeType.getName());
    }
  }

  private void processRemove(String nodePath, Node node, Event<?> event, boolean isMultiSelect)
      throws Exception {
    final String virtualNodePath = nodePath;
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    Session session = node.getSession();
    UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
    try {
      uiExplorer.addLockToken(node);
    } catch (Exception e) {
      JCRExceptionManager.process(uiApp, e);
      return;
    }
    Node parentNode = node.getParent();
    uiExplorer.addLockToken(parentNode);
    try {
      if (node.isNodeType(Utils.RMA_RECORD))
        removeMixins(node);
      ThumbnailService thumbnailService = getApplicationComponent(ThumbnailService.class);
      thumbnailService.processRemoveThumbnail(node);
      node.remove();
      parentNode.save();
    } catch (VersionException ve) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.remove-verion-exception", null,
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
    } catch (ReferentialIntegrityException ref) {
      session.refresh(false);
      uiExplorer.refreshExplorer();
      uiApp
          .addMessage(new ApplicationMessage(
              "UIPopupMenu.msg.remove-referentialIntegrityException", null,
              ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
    } catch (ConstraintViolationException cons) {
      session.refresh(false);
      uiExplorer.refreshExplorer();
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.constraintviolation-exception",
          null, ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
    } catch (LockException lockException) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked-other-person", null,
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
    } catch (Exception e) {
      LOG.error("an unexpected error occurs while removing the node", e);
      JCRExceptionManager.process(uiApp, e);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    }
    if (!isMultiSelect)
      uiExplorer.setSelectNode(LinkUtils.getParentPath(virtualNodePath));
  }
  
  private void processRemoveMultiple(String[] nodePaths, String[] wsNames, Event<?> event)
      throws Exception {
    for (int i = 0; i < nodePaths.length; i++) {
      processRemove(nodePaths[i], wsNames[i], event, true);
    }
  }

  private void processRemove(String nodePath, String wsName, Event<?> event, boolean isMultiSelect)
      throws Exception {
    if (wsName == null) {
      wsName = getDefaultWorkspace();
    }
    CopyManageComponent.processCopy(wsName + ":" + nodePath, event, isMultiSelect);
  }

  private String getDefaultWorkspace() {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    return uiExplorer.getCurrentDriveWorkspace();
  }

  public void doDelete(String nodePath, String wsName, Event<?> event) throws Exception {
    UIJCRExplorer uiExplorer = getParent();
    if (nodePath.indexOf(";") > -1) {
      processRemoveMultiple(nodePath.split(";"), wsName.split(";"), event);
    } else {
      processRemove(nodePath, wsName, event, false);
    }
    uiExplorer.updateAjax(event);
    if (!uiExplorer.getPreference().isJcrEnable())
      uiExplorer.getSession().save();
  }

  public void doDelete(String nodePath, Event<?> event) throws Exception {
    UIJCRExplorer uiExplorer = getParent();
    if (nodePath.indexOf(";") > -1) {
      processRemoveMultiple(nodePath.split(";"), event);
    } else {
      String wsName = null;
      Session session = null;
      Matcher matcher = UIWorkingArea.FILE_EXPLORER_URL_SYNTAX.matcher(nodePath);
      UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
      // prepare to remove
      if (matcher.find()) {
        wsName = matcher.group(1);
        nodePath = matcher.group(2);
        try {
          // Use the method getNodeByPath because it is link aware
          session = uiExplorer.getSessionByWorkspace(wsName);
          // Use the method getNodeByPath because it is link aware
          Node node = uiExplorer.getNodeByPath(nodePath, session, false);
          // Reset the session to manage the links that potentially change of
          // workspace
          session = node.getSession();
          // Reset the workspace name to manage the links that potentially
          // change of workspace
          wsName = session.getWorkspace().getName();
          // Use the method getNodeByPath because it is link aware
          node = uiExplorer.getNodeByPath(nodePath, session, false);
          processRemove(nodePath, node, event, false);
        } catch (PathNotFoundException path) {
          uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", null,
              ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        } catch (Exception e) {
          JCRExceptionManager.process(uiApp, e);
          return;
        }
      }
    }
    uiExplorer.updateAjax(event);
    if (!uiExplorer.getPreference().isJcrEnable())
      uiExplorer.getSession().save();
  }
  
  public static void deleteManage(Event<? extends UIComponent> event, UIJCRExplorer uiExplorer) throws Exception {
    UIWorkingArea uiWorkingArea = event.getSource().getParent();
    String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
    UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
    UIConfirmMessage uiConfirmMessage = 
      uiWorkingArea.createUIComponent(UIConfirmMessage.class, null, null);
    if(nodePath.indexOf(";") > -1) {
      uiConfirmMessage.setMessageKey("UIWorkingArea.msg.confirm-delete-multi");
      uiConfirmMessage.setArguments(new String[] {Integer.toString(nodePath.split(";").length)});
    } else {
      uiConfirmMessage.setMessageKey("UIWorkingArea.msg.confirm-delete");
      uiConfirmMessage.setArguments(new String[] {nodePath});
    }
    uiConfirmMessage.setNodePath(nodePath);
    UIPopupContainer.activate(uiConfirmMessage, 500, 180);
    event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
  }
  
  @Override
  public Class<? extends UIAbstractManager> getUIAbstractManagerClass() {
    return null;
  }
  
  public static class DeleteActionListener extends UIWorkingAreaActionListener<DeleteManageComponent> {
    public void processEvent(Event<DeleteManageComponent> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      deleteManage(event, uiExplorer);
    }
  }
  
}
