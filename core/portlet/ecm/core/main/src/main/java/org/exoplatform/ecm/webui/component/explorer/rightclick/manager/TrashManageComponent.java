/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.ecm.webui.component.explorer.rightclick.manager;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.portlet.PortletPreferences;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ecm.webui.component.admin.manager.UIAbstractManager;
import org.exoplatform.ecm.webui.component.admin.manager.UIAbstractManagerComponent;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.ecm.webui.component.explorer.control.filter.IsNotInTrashFilter;
import org.exoplatform.ecm.webui.component.explorer.control.listener.UIWorkingAreaActionListener;
import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.services.cms.documents.TrashService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Anh Vu
 *          anhvurz90@gmail.com
 * Oct 14, 2009  
 * 5:23:30 PM
 */

@ComponentConfig(
	    events = {
	      @EventConfig(listeners = TrashManageComponent.MoveToTrashActionListener.class)
	    }
	)

public class TrashManageComponent extends UIAbstractManagerComponent {

	private static final List<UIExtensionFilter> FILTERS 
				= Arrays.asList(new UIExtensionFilter[]{new IsNotInTrashFilter()});
	
	private final static Log 		LOG = ExoLogger.getLogger(TrashManageComponent.class); 
	
	@UIExtensionFilters
	public List<UIExtensionFilter> getFilters() {
		return FILTERS;
	}
	
	private static void multiMoveToTrash(String[] paths, Event<UIComponent> event) throws Exception {
		for (String path : paths) {
			moveToTrash(path, event);
		}
	}
	
	private static void moveToTrash(String srcPath, Event<UIComponent> event) throws Exception {
		UIWorkingArea uiWorkingArea = ((UIWorkingArea)event.getSource().getParent());
		UIJCRExplorer uiExplorer = uiWorkingArea.getAncestorOfType(UIJCRExplorer.class);
		
	    ExoContainer myContainer = ExoContainerContext.getCurrentContainer();
	    TrashService trashService = (TrashService)myContainer.getComponentInstanceOfType(TrashService.class);
	    
	    UIApplication uiApp = uiWorkingArea.getAncestorOfType(UIApplication.class);
	    Matcher matcher = UIWorkingArea.FILE_EXPLORER_URL_SYNTAX.matcher(srcPath);
	    String wsName = null;
	    Node node = null;
	    if (matcher.find()) {
	      wsName = matcher.group(1);
	      srcPath = matcher.group(2);
	    } else {
	      throw new IllegalArgumentException("The ObjectId is invalid '"+ srcPath + "'");
	    }
	    Session session = uiExplorer.getSessionByWorkspace(wsName);
	    try {
	      // Use the method getNodeByPath because it is link aware
	      node = uiExplorer.getNodeByPath(srcPath, session, false);
	      // Reset the path to manage the links that potentially create virtual path
	      //srcPath = node.getPath();
	      // Reset the session to manage the links that potentially change of workspace
	      //session = node.getSession();
	      // Reset the workspace name to manage the links that potentially change of workspace 
	      //wsName = session.getWorkspace().getName();
	    } catch(PathNotFoundException path) {
	      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
	          null,ApplicationMessage.WARNING));
	      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
	      return;
	    }

	    try {
	    	PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
	        PortletPreferences portletPref = pcontext.getRequest().getPreferences();
	    	String trashHomeNodePath = portletPref.getValue(Utils.TRASH_HOME_NODE_PATH, "");
	    	String trashWorkspace = portletPref.getValue(Utils.TRASH_WORKSPACE, "");
	    	String trashRepository = portletPref.getValue(Utils.TRASH_REPOSITORY, "");
	    	SessionProvider sessionProvider = uiExplorer.getSessionProvider();
	    	trashService.moveToTrash(node, 
	    							 trashHomeNodePath, 
	    							 trashWorkspace, 
	    							 trashRepository, 
	    							 sessionProvider);
	    	uiExplorer.updateAjax(event);
	    } catch (Exception e) {
	    	LOG.error("an unexpected error occurs", e);
	    	JCRExceptionManager.process(uiApp, e);
	    	event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
	    	uiExplorer.updateAjax(event);
	    }
	}
	
	public static void trashManage(Event<UIComponent> event) throws Exception {
		String srcPath = event.getRequestContext().getRequestParameter(OBJECTID);
		if (srcPath.indexOf(';') > -1) {
			multiMoveToTrash(srcPath.split(";"), event);
		} else {
			moveToTrash(srcPath, event);
		}
	}
	
	public static class MoveToTrashActionListener extends UIWorkingAreaActionListener<TrashManageComponent> {
		public void processEvent(Event<TrashManageComponent> event) throws Exception {
			Event<UIComponent> event_ = new Event<UIComponent>(event.getSource(), event.getName(), event.getRequestContext());
			trashManage(event_);
		}
	}
	
	@Override
	public Class<? extends UIAbstractManager> getUIAbstractManagerClass() {
		// TODO Auto-generated method stub
		return null;
	}

}
