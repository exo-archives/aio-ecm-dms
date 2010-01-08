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
package org.exoplatform.ecm.webui.component.explorer.control.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.log.Log;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.UIExtensionEventListener;
import org.exoplatform.webui.ext.UIExtensionManager;

/**
 * Created by The eXo Platform SARL
 * Author : Hoang Van Hung
 *          hunghvit@gmail.com
 * Aug 5, 2009  
 */
public abstract class UIWorkingAreaActionListener <T extends UIComponent> extends UIExtensionEventListener<T> {

  private static final Log LOG  = ExoLogger.getLogger(UIWorkingAreaActionListener.class);
  
  private Node getNodeByPath(String nodePath, UIJCRExplorer uiExplorer) throws Exception {
    Matcher matcher = UIWorkingArea.FILE_EXPLORER_URL_SYNTAX.matcher(nodePath);
    String wsName = null;
    if (matcher.find()) {
      wsName = matcher.group(1);
      nodePath = matcher.group(2);
    } else {
      throw new IllegalArgumentException("The ObjectId is invalid '"+ nodePath + "'");
    }
    Session session = uiExplorer.getSessionByWorkspace(wsName);
    return uiExplorer.getNodeByPath(nodePath, session);
  }
  
  public boolean acceptForMultiNode(Event<T> event, String path) {
    Map<String, Object> context = new HashMap<String, Object>();
    UIWorkingArea uiWorkingArea = event.getSource().getAncestorOfType(UIWorkingArea.class);
    UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
    WebuiRequestContext requestContext = event.getRequestContext();
    UIApplication uiApp = requestContext.getUIApplication();
    try {
      context.put(UIWorkingArea.class.getName(), uiWorkingArea);
      context.put(UIJCRExplorer.class.getName(), uiExplorer);
      context.put(UIApplication.class.getName(), uiApp);
      context.put(Node.class.getName(), getNodeByPath(path, uiExplorer));
      context.put(WebuiRequestContext.class.getName(), requestContext);
      UIExtensionManager manager = event.getSource().getApplicationComponent(UIExtensionManager.class);
      return manager.accept(getExtensionType(), event.getName(), context);
    } catch (Exception e) {
      LOG.error("an unexpected error occurs while filter the node", e);
    }
    return false;
  }
  
  @Override
  protected Map<String, Object> createContext(Event<T> event) throws Exception {
    Map<String, Object> context = new HashMap<String, Object>();
    UIWorkingArea uiWorkingArea = event.getSource().getAncestorOfType(UIWorkingArea.class);
    UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
    String nodePath = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
    if (nodePath == null || nodePath.length() == 0 || nodePath.contains(";")) return null;
    // Use the method getNodeByPath because it is link aware
    Node currentNode = getNodeByPath(nodePath, uiExplorer); 
    WebuiRequestContext requestContext = event.getRequestContext();
    UIApplication uiApp = requestContext.getUIApplication();
    context.put(UIWorkingArea.class.getName(), uiWorkingArea);
    context.put(UIJCRExplorer.class.getName(), uiExplorer);
    context.put(UIApplication.class.getName(), uiApp);
    context.put(Node.class.getName(), currentNode);
    context.put(WebuiRequestContext.class.getName(), requestContext);
    return context;
  }
  
  @Override
  public void execute(Event<T> event) throws Exception {
    String nodePath = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
    if (nodePath == null || !nodePath.contains(";")) {
      super.execute(event);
      return;
    }
    processEvent(event);
  }

  @Override
  protected String getExtensionType() {
    return UIWorkingArea.EXTENSION_TYPE;
  }

}
