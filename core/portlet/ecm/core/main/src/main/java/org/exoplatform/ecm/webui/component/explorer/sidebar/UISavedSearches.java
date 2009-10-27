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
package org.exoplatform.ecm.webui.component.explorer.sidebar;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.exoplatform.ecm.webui.component.explorer.UIDocumentWorkspace;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.ecm.webui.component.explorer.search.UISearchResult;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.queries.QueryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 23, 2009  
 * 4:01:53 AM
 */
@ComponentConfig(
    template =  "app:/groovy/webui/component/explorer/sidebar/UISavedSearches.gtmpl",
    events = {
        @EventConfig(listeners = UISavedSearches.ExecuteActionListener.class)
    }
)
public class UISavedSearches extends UIComponent {

  private List<Node> sharedQueries_ = new ArrayList<Node>();  
  private List<Query> privateQueries = new ArrayList<Query>();
  
  public UISavedSearches() throws Exception {
    // TODO Auto-generated constructor stub
  }
  
  public List<Object> queryList() throws Exception {
    List<Object> objectList = new ArrayList<Object>();
    if(hasSharedQueries()) {
      for(Node node : getSharedQueries()) {
        objectList.add(node);
      }
    }
    if(hasQueries()) {
      for(Query query : getQueries()) {
        objectList.add(query);
      }
    }
    return objectList;
  }
  
  
  public String getCurrentUserId() { return Util.getPortalRequestContext().getRemoteUser();}
  
  public boolean hasQueries() throws Exception {
    QueryService queryService = getApplicationComponent(QueryService.class);
    try {
      privateQueries = queryService.getQueries(getCurrentUserId(), 
          getRepositoryName(), SessionProviderFactory.createSessionProvider());
      return !privateQueries.isEmpty();    
    } catch(AccessDeniedException ace) {
      return privateQueries.isEmpty();
    }
  }
  
  public List<Query> getQueries() throws Exception { return privateQueries; }
  
  public boolean hasSharedQueries() throws Exception {    
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
    QueryService queryService = getApplicationComponent(QueryService.class);
    String userId = pcontext.getRemoteUser();    
    SessionProvider provider = SessionProviderFactory.createSystemProvider();    
    sharedQueries_ = queryService.getSharedQueries(userId, getRepositoryName(),provider);
    return !sharedQueries_.isEmpty();
  }
  
  public List<Node> getSharedQueries() { return sharedQueries_; }  
  
  private String getRepositoryName() {
    return getAncestorOfType(UIJCRExplorer.class).getRepositoryName();
  }
  
  static public class ExecuteActionListener extends EventListener<UISavedSearches> {
    public void execute(Event<UISavedSearches> event) throws Exception {
      UISavedSearches uiSavedSearches = event.getSource();
      UIJCRExplorer uiExplorer = uiSavedSearches.getAncestorOfType(UIJCRExplorer.class);
      String wsName = uiSavedSearches.getAncestorOfType(UIJCRExplorer.class).getCurrentWorkspace();
      UIApplication uiApp = uiSavedSearches.getAncestorOfType(UIApplication.class);
      QueryService queryService = uiSavedSearches.getApplicationComponent(QueryService.class);
      String queryPath = event.getRequestContext().getRequestParameter(OBJECTID);
      UIComponent uiSearch = uiExplorer.getChild(UIWorkingArea.class).getChild(UIDocumentWorkspace.class);
      UISearchResult uiSearchResult = ((UIDocumentWorkspace)uiSearch).getChild(UISearchResult.class);
      QueryResult queryResult = null;
      try {
        queryResult = queryService.execute(queryPath, wsName, uiExplorer.getRepositoryName(),
            SessionProviderFactory.createSystemProvider(), uiSavedSearches.getCurrentUserId());
      } catch(Exception e) {
        uiApp.addMessage(new ApplicationMessage("UISearchResult.msg.query-invalid", null, 
                                                ApplicationMessage.WARNING));
        return;
      } finally {
        if(queryResult == null || queryResult.getNodes().getSize() ==0) {
          uiApp.addMessage(new ApplicationMessage("UISavedQuery.msg.not-result-found", null)); 
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }        
        uiSearchResult.clearAll();
        uiSearchResult.setQueryResults(queryResult);
        uiSearchResult.updateGrid(true);
      }
    }
  }

}
