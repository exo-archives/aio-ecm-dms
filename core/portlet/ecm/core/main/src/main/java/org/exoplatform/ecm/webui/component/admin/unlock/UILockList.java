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
package org.exoplatform.ecm.webui.component.admin.unlock;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponentDecorator;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Dec 29, 2006  
 * 11:30:17 AM
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/admin/unlock/UILockList.gtmpl",
    events = {
        @EventConfig(listeners = UILockList.ManageLockActionListener.class)
    }
)
public class UILockList extends UIComponentDecorator {
  final static public String[] ACTIONS = {};
  final static public String ST_EDIT = "EditUnLockForm";
  private UIPageIterator uiPageIterator_;
  
  private static final String LOCK_QUERY = "select * from nt:base where jcr:mixinTypes = 'mix:lockable' order by exo:dateCreated DESC";
  
  public UILockList() throws Exception {
    uiPageIterator_ = createUIComponent(UIPageIterator.class, null, "LockListIterator");
    setUIComponent(uiPageIterator_) ;
  }

  public String[] getActions() { return ACTIONS ; }
  
  public void updateLockedNodesGrid(int currentPage) throws Exception {
    PageList pageList = new ObjectPageList(getAllLockedNodes(), 10);
    uiPageIterator_.setPageList(pageList) ;
    if(currentPage > getUIPageIterator().getAvailablePage())
      uiPageIterator_.setCurrentPage(currentPage-1);
    else
      uiPageIterator_.setCurrentPage(currentPage);
  }
  
  public UIPageIterator getUIPageIterator() { return uiPageIterator_ ; }
  
  public List getLockedNodeList() throws Exception { return uiPageIterator_.getCurrentPageData(); } 
  
  public List<Node> getAllLockedNodes() throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageRepository = repositoryService.getCurrentRepository();
    
    List<Node> listLockedNodes = new ArrayList<Node>();
    QueryManager queryManager = null;
    Session session = null;
    String queryStatement = LOCK_QUERY;
    Query query = null;
    QueryResult queryResult = null;
    for(RepositoryEntry repo : repositoryService.getConfig().getRepositoryConfigurations() ) {
      for(WorkspaceEntry ws : repo.getWorkspaceEntries()) {
        session = SessionProviderFactory.createSystemProvider().getSession(ws.getName(), manageRepository);
        queryManager = session.getWorkspace().getQueryManager();
        query = queryManager.createQuery(queryStatement, Query.SQL);
        queryResult = query.execute();    
        for(NodeIterator iter = queryResult.getNodes(); iter.hasNext();) {          
          Node itemNode = iter.nextNode();
          listLockedNodes.add(itemNode);
        }
      }
    }
        
    return listLockedNodes;
  }
  
  static public class ManageLockActionListener extends EventListener<UILockList> {
    public void execute(Event<UILockList> event) throws Exception {
      UIUnLockManager uiUnLockManager = event.getSource().getParent();
      uiUnLockManager.initFormPopup(UILockList.ST_EDIT);
      String queryPath = event.getRequestContext().getRequestParameter(OBJECTID);
      UIUnLockForm uiForm = uiUnLockManager.findFirstComponentOfType(UIUnLockForm.class);
      uiForm.update(queryPath);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiUnLockManager);
    }
  }  
}