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

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;

import org.apache.commons.logging.Log;
import org.exoplatform.ecm.webui.component.admin.manager.UIAbstractManager;
import org.exoplatform.ecm.webui.component.admin.manager.UIAbstractManagerComponent;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.ecm.webui.component.explorer.control.filter.CanSetPropertyFilter;
import org.exoplatform.ecm.webui.component.explorer.control.filter.IsCheckedOutFilter;
import org.exoplatform.ecm.webui.component.explorer.control.filter.IsHoldsLockFilter;
import org.exoplatform.ecm.webui.component.explorer.control.filter.IsNotSameNameSiblingFilter;
import org.exoplatform.ecm.webui.component.explorer.control.listener.UIWorkingAreaActionListener;
import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
import org.exoplatform.ecm.webui.utils.LockUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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
      @EventConfig(listeners = UnlockManageComponent.UnlockActionListener.class)
    }
)
public class UnlockManageComponent extends UIAbstractManagerComponent {
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[]{new CanSetPropertyFilter(), new IsNotSameNameSiblingFilter(), new IsHoldsLockFilter(), new IsCheckedOutFilter()});
  
  private static final Log LOG  = ExoLogger.getLogger(UnlockManageComponent.class);
  
  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }
  
  private static void processUnlock(String nodePath, Event<?> event, UIJCRExplorer uiExplorer) throws Exception {
    UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
    Matcher matcher = UIWorkingArea.FILE_EXPLORER_URL_SYNTAX.matcher(nodePath);
    String wsName = null;
    if (matcher.find()) {
      wsName = matcher.group(1);
      nodePath = matcher.group(2);
    } else {
      throw new IllegalArgumentException("The ObjectId is invalid '"+ nodePath + "'");
    }
    Session session = uiExplorer.getSessionByWorkspace(wsName);
    Node node;
    try {
      // Use the method getNodeByPath because it is link aware
      node = uiExplorer.getNodeByPath(nodePath, session);
      // Reset the path to manage the links that potentially create virtual path
      nodePath = node.getPath();
      // Reset the session to manage the links that potentially change of workspace
      session = node.getSession();
      // Reset the workspace name to manage the links that potentially change of workspace 
      wsName = session.getWorkspace().getName();
    } catch(PathNotFoundException path) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
          null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    } catch (Exception e) {
      JCRExceptionManager.process(uiApp, e);
      return;
    }
    
    try {
      if(node.holdsLock()) {
        String lockToken = LockUtil.getLockToken(node);        
        if(lockToken != null) {
          session.addLockToken(lockToken);
        }
        node.unlock();   
        node.removeMixin(Utils.MIX_LOCKABLE);
        node.getSession().save(); 
      }
    } catch(LockException le) {
      Object[] args = {node.getName()};
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-unlock-node", args, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
    } catch(VersionException versionException) {
      Object[] args = {node.getName()};
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-unlock-node-is-checked-in", args, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;  
    } catch (Exception e) {
      LOG.error("an unexpected error occurs while unloking the node", e);
      JCRExceptionManager.process(uiApp, e);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
    }    
  }
  
  public static void unlockManage(Event<UnlockManageComponent> event, UIJCRExplorer uiExplorer)
      throws Exception {
    String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
    if(nodePath.indexOf(";") > -1) {
      processMultiUnlock(nodePath.split(";"), event, uiExplorer);
    } else {
      processUnlock(nodePath, event, uiExplorer);
    }
  }
  
  private static void processMultiUnlock(String[] nodePaths, Event<?> event, UIJCRExplorer uiExplorer) throws Exception {
    for(int i=0; i< nodePaths.length; i++) {
      processUnlock(nodePaths[i], event, uiExplorer);
    }
    if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save();
    uiExplorer.updateAjax(event);
  }
  
  public static class UnlockActionListener extends UIWorkingAreaActionListener<UnlockManageComponent> {
    public void processEvent(Event<UnlockManageComponent> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      unlockManage(event, uiExplorer);
    }
  }
  
  @Override
  public Class<? extends UIAbstractManager> getUIAbstractManagerClass() {
    return null;
  }

}
