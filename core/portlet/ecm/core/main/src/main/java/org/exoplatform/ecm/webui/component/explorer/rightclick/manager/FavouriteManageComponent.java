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
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ecm.webui.component.admin.manager.UIAbstractManager;
import org.exoplatform.ecm.webui.component.admin.manager.UIAbstractManagerComponent;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.ecm.webui.component.explorer.control.filter.CanSetPropertyFilter;
import org.exoplatform.ecm.webui.component.explorer.control.filter.IsCheckedOutFilter;
import org.exoplatform.ecm.webui.component.explorer.control.filter.IsNotFavouriteFilter;
import org.exoplatform.ecm.webui.component.explorer.control.filter.IsNotLockedFilter;
import org.exoplatform.ecm.webui.component.explorer.control.filter.IsNotTrashHomeNodeFilter;
import org.exoplatform.ecm.webui.component.explorer.control.listener.UIWorkingAreaActionListener;
import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
import org.exoplatform.ecm.webui.utils.PermissionUtil;
import org.exoplatform.services.cms.documents.FavoriteService;
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
 * Author : Nguyen Anh Vu
 *          anhvurz90@gmail.com
 * Oct 14, 2009  
 */

@ComponentConfig(
	    events = {
	      @EventConfig(listeners = FavouriteManageComponent.AddToFavouriteActionListener.class)
	    }
	)

public class FavouriteManageComponent extends UIAbstractManagerComponent {
	
	private static final List<UIExtensionFilter> FILTERS 
				= Arrays.asList(new UIExtensionFilter[]{new IsNotFavouriteFilter(),
																						 		new IsNotLockedFilter(),
																						 		new IsCheckedOutFilter(),
																						 		new CanSetPropertyFilter(),
																						 		new IsNotTrashHomeNodeFilter() });
	  
	private final static Log       LOG  = ExoLogger.getLogger(FavouriteManageComponent.class);
	  
	@UIExtensionFilters
	public List<UIExtensionFilter> getFilters() {
		return FILTERS;
	}
	
	private static void addToFavourite(String srcPath, Event<FavouriteManageComponent> event) throws Exception {
    UIWorkingArea uiWorkingArea = event.getSource().getParent();
    UIJCRExplorer uiExplorer = uiWorkingArea.getAncestorOfType(UIJCRExplorer.class);
    
    ExoContainer myContainer = ExoContainerContext.getCurrentContainer();
    FavoriteService favoriteService = (FavoriteService)myContainer.getComponentInstanceOfType(FavoriteService.class);
    
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
      srcPath = node.getPath();
      // Reset the session to manage the links that potentially change of workspace
      session = node.getSession();
      // Reset the workspace name to manage the links that potentially change of workspace 
      wsName = session.getWorkspace().getName();
    } catch(PathNotFoundException path) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
          null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    }

    try {
      uiExplorer.addLockToken(node);
    } catch (Exception e) {
      JCRExceptionManager.process(uiApp, e);
      return;
    }
    
    try {
    	if (!node.isCheckedOut()) 
    		throw new VersionException("node is locked, can't add favourite to node:" + node.getPath());
    	if (!PermissionUtil.canSetProperty(node))
    		throw new AccessDeniedException("access denied, can't add favourite to node:" + node.getPath());
    	favoriteService.addFavorite(node, session.getUserID());
    } catch (LockException e) {
    	LOG.error("node is locked, can't add favourite to node:" + node.getPath());
    	JCRExceptionManager.process(uiApp, e);
    	event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
    	uiExplorer.updateAjax(event);
    } catch (VersionException e) {
    	LOG.error("node is checked in, can't add favourite to node:" + node.getPath());
    	JCRExceptionManager.process(uiApp, e);
    	event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
    	uiExplorer.updateAjax(event);
    } catch (AccessDeniedException e) {
    	LOG.error("access denied, can't add favourite to node:" + node.getPath());
    	JCRExceptionManager.process(uiApp, e);
    	event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
    	uiExplorer.updateAjax(event);
    } catch (Exception e) {
        LOG.error("an unexpected error occurs", e);
        JCRExceptionManager.process(uiApp, e);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiExplorer.updateAjax(event);
    }
	}
			

	public static class AddToFavouriteActionListener extends UIWorkingAreaActionListener<FavouriteManageComponent> {

	  private void multiAddToFavourite(String[] paths, Event<FavouriteManageComponent> event) throws Exception {
	    for (String path : paths) {
	      if (acceptForMultiNode(event, path)) {
	        addToFavourite(path, event);
	      }
	    }
	  }
	  
	  private void favouriteManage(Event<FavouriteManageComponent> event) throws Exception {
	    String srcPath = event.getRequestContext().getRequestParameter(OBJECTID);
	    if (srcPath.indexOf(';') > -1) {
	      multiAddToFavourite(srcPath.split(";"), event);
	    } else {
	      addToFavourite(srcPath, event);
	    }
	  }
	  
    public void processEvent(Event<FavouriteManageComponent> event) throws Exception {
        favouriteManage(event);
      }
	}
	
	@Override
	public Class<? extends UIAbstractManager> getUIAbstractManagerClass() {
		// TODO Auto-generated method stub
		return null;
	}

}
