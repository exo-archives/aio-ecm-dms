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
import javax.jcr.nodetype.ConstraintViolationException;

import org.apache.commons.logging.Log;
import org.exoplatform.ecm.jcr.model.ClipboardCommand;
import org.exoplatform.ecm.webui.component.admin.manager.UIAbstractManager;
import org.exoplatform.ecm.webui.component.admin.manager.UIAbstractManagerComponent;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.ecm.webui.component.explorer.control.filter.CanCutNodeFilter;
import org.exoplatform.ecm.webui.component.explorer.control.filter.IsNotLockedFilter;
import org.exoplatform.ecm.webui.component.explorer.control.filter.IsNotTrashHomeNodeFilter;
import org.exoplatform.ecm.webui.component.explorer.control.listener.UIWorkingAreaActionListener;
import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
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
      @EventConfig(listeners = CutManageComponent.CutActionListener.class)
    }
)

public class CutManageComponent extends UIAbstractManagerComponent {

  private final static Log       LOG  = ExoLogger.getLogger(CutManageComponent.class);

  private static final List<UIExtensionFilter> FILTERS 
  		= Arrays.asList(new UIExtensionFilter[] { new CanCutNodeFilter(), 
  																							new IsNotLockedFilter(),
  																							new IsNotTrashHomeNodeFilter() });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }
  
  private static void processCut(String nodePath, Event<CutManageComponent> event, UIJCRExplorer uiExplorer, boolean isMultiSelect) throws Exception {
    UIWorkingArea uiWorkingArea = uiExplorer.getChild(UIWorkingArea.class);
    UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
    Matcher matcher = UIWorkingArea.FILE_EXPLORER_URL_SYNTAX.matcher(nodePath);
    String wsName = null;
    if (matcher.find()) {
      wsName = matcher.group(1);
      nodePath = matcher.group(2);
    } else {
      throw new IllegalArgumentException("The ObjectId is invalid '"+ nodePath + "'");
    }
    Session session = uiExplorer.getSessionByWorkspace(wsName);
    Node selectedNode;
    try {
      // Use the method getNodeByPath because it is link aware
      selectedNode = uiExplorer.getNodeByPath(nodePath, session, false);
      // Reset the path to manage the links that potentially create virtual path
      nodePath = selectedNode.getPath();
      // Reset the session to manage the links that potentially change of workspace
      session = selectedNode.getSession();
      // Reset the workspace name to manage the links that potentially change of workspace 
      wsName = session.getWorkspace().getName();
    } catch(ConstraintViolationException cons) {
      uiExplorer.getSession().refresh(false);
      uiExplorer.refreshExplorer();
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.constraintviolation-exception", 
          null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;        
    } catch(PathNotFoundException path) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
          null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    } catch (Exception e) {
      LOG.error("an unexpected error occurs while cuting the node", e);
      JCRExceptionManager.process(uiApp, e);
      return;
    }
    try {
      List<ClipboardCommand> clipboards = uiExplorer.getAllClipBoard();
      for(ClipboardCommand command:clipboards) {
        if(command.getSrcPath().equals(nodePath)) {
          clipboards.remove(command);
          break;
        }
      }       
      ClipboardCommand clipboard = new ClipboardCommand();
      clipboard.setType(ClipboardCommand.CUT);
      clipboard.setSrcPath(nodePath);
      clipboard.setWorkspace(wsName);
      uiExplorer.getAllClipBoard().add(clipboard);
      if(isMultiSelect) {
        uiWorkingArea.getVirtualClipboards().add(clipboard);
      } else {
        if(!uiExplorer.getPreference().isJcrEnable()) session.save();
        uiExplorer.updateAjax(event);
      }
    } catch (Exception e) {
      JCRExceptionManager.process(uiApp, e);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
    }
  }

  private static void processMultipleCut(String[] nodePaths, Event<CutManageComponent> event, UIJCRExplorer uiExplorer) throws Exception {
    for(int i=0; i< nodePaths.length; i++) {
      processCut(nodePaths[i], event, uiExplorer, true);
    }
    if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save();
    uiExplorer.updateAjax(event);
  }
  
  public static void cutManage(Event<CutManageComponent> event, UIJCRExplorer uiExplorer) throws Exception {
    UIWorkingArea uiWorkingArea = event.getSource().getParent();
    String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
    if(nodePath.indexOf(";") > -1) {
      uiWorkingArea.getVirtualClipboards().clear();
      processMultipleCut(nodePath.split(";"), event, uiExplorer);
    } else {
      processCut(nodePath, event, uiExplorer, false);
    }
  }
  
  public static class CutActionListener extends UIWorkingAreaActionListener<CutManageComponent> {
    public void processEvent(Event<CutManageComponent> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      cutManage(event, uiExplorer);
    }
  }
  
  @Override
  public Class<? extends UIAbstractManager> getUIAbstractManagerClass() {
    return null;
  }

}
