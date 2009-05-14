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

package org.exoplatform.ecm.application;

import java.util.Iterator;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.logging.Log;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.lock.LockManager;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 */
public class LockManagerListener implements HttpSessionListener {

  protected static Log log = ExoLogger.getLogger("dms:LockManagerListener");
  
  @SuppressWarnings("unused")
  public void sessionCreated(HttpSessionEvent arg0) {

  }

  @SuppressWarnings("unchecked")
  public void sessionDestroyed(HttpSessionEvent event) {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    try {      
      HttpSession httpSession = event.getSession();
      log.info("Removing the locks of all locked nodes");
      Map<String,String> lockedNodes = 
        (Map<String,String>)httpSession.getAttribute(LockManager.class.getName());
      if(lockedNodes == null || lockedNodes.values().isEmpty()) return;      
      String portalContainerName = event.getSession().getServletContext().getServletContextName() ;
      RootContainer rootContainer = RootContainer.getInstance() ;
      PortalContainer portalContainer = rootContainer.getPortalContainer(portalContainerName) ;
      PortalContainer.setInstance(portalContainer);
      RepositoryService repositoryService = 
        (RepositoryService)portalContainer.getComponentInstanceOfType(RepositoryService.class);
      String key = null, nodePath = null, repoName = null,workspaceName = null, lockToken= null ;
      String[] temp = null, location = null ;
      Session session = null;      
      for(Iterator<String> iter = lockedNodes.keySet().iterator(); iter.hasNext();) {                
        try {
          //The key structrure is built in org.exoplatform.ecm.webui.utils.LockUtil.createLockKey() method
          key = iter.next();          
          temp = key.split(":/:");
          nodePath = temp[1];
          location = temp[0].split("/::/");
          repoName = location[0]; 
          workspaceName = location[1] ;
          session = 
            sessionProvider.getSession(workspaceName,repositoryService.getRepository(repoName));
          lockToken = lockedNodes.get(key);
          session.addLockToken(lockToken);
          Node node = (Node)session.getItem(nodePath);
          node.unlock();
          node.removeMixin("mix:lockable");
          node.save();
        } catch (Exception e) {
          log.error("Error while unlocking the locked nodes",e);
        } finally {
          if(session != null) session.logout();
        }
      }
    } catch(Exception ex) {
      log.error("Error during the time unlocking the locked nodes",ex);
    } finally {      
      sessionProvider.close();
    }
  }     
}
