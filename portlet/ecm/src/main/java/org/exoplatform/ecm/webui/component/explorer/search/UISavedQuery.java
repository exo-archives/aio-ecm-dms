/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.portal.component.view.Util;
import org.exoplatform.services.cms.queries.QueryService;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPageIterator;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh  
 *          minh.dang@exoplatform.com
 * Jan 4, 2006
 * 16:37:15 
 */
@ComponentConfig(
    template =  "app:/groovy/webui/component/explorer/search/UISavedQuery.gtmpl",
    events = {
        @EventConfig(listeners = UISavedQuery.ExecuteActionListener.class),
        @EventConfig(listeners = UISavedQuery.DeleteActionListener.class)
    }
)

public class UISavedQuery extends UIContainer {

  private List<Node> sharedQueries_ = new ArrayList<Node>() ;

  public UISavedQuery() throws Exception {        
    addChild(UIPageIterator.class, null, "SavedQueryIterator");
    updateGrid() ;  
  }  

  public void updateGrid() throws Exception {
    PageList pageList = new ObjectPageList(getQueries(), 10) ;
    UIPageIterator uiPateIterator = getChild(UIPageIterator.class) ;
    uiPateIterator.setPageList(pageList) ;  
  }

  public UIPageIterator getUIPageIterator() { return getChild(UIPageIterator.class) ; }
  
  public boolean hasQueries() throws Exception {
    QueryService queryService = getApplicationComponent(QueryService.class) ;
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    List<Query> queries = queryService.getQueries(userName);
    if (queries == null || queries.isEmpty()) return false;
    return true;
  }

  public List<Query> getQueries() throws Exception {
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    return getApplicationComponent(QueryService.class).getQueries(userName);
  }
  
  public String getCurrentUserId() { return Util.getPortalRequestContext().getRemoteUser() ;}
  
  public boolean hasSharedQueries() throws Exception {
    OrganizationService organizationService = getApplicationComponent(OrganizationService.class) ;
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    QueryService queryService = getApplicationComponent(QueryService.class) ;
    String userName = context.getRemoteUser() ;
    List<String> roles = new ArrayList<String>() ;
    Collection memberships = 
      organizationService.getMembershipHandler().findMembershipsByUser(userName) ;
    if(memberships != null && memberships.size() > 0){
      Object[] objects = memberships.toArray() ;      
      for(int i = 0 ; i < objects.length ; i ++ ){
        Membership membership = (Membership)objects[i] ;
        String role = membership.getMembershipType() + ":" + membership.getGroupId() ;
        roles.add(role) ;
      } 
    }
    if(roles.size() < 0) return false ;
    sharedQueries_ = queryService.getSharedQueriesByPermissions(roles) ;
    if(queryService.getSharedQueriesByPermissions(roles).size() > 0) return true ;      
    return false ;                
  }
  
  public List<Node> getSharedQueries() { return sharedQueries_ ; }
  
  static public class ExecuteActionListener extends EventListener<UISavedQuery> {
    public void execute(Event<UISavedQuery> event) throws Exception {      
      UISavedQuery uiQuery = event.getSource() ;
      String wsName = uiQuery.getAncestorOfType(UIJCRExplorer.class).getCurrentWorkspace() ;
      UIApplication uiApp = uiQuery.getAncestorOfType(UIApplication.class) ;
      QueryService queryService = uiQuery.getApplicationComponent(QueryService.class) ;
      String queryPath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIECMSearch uiSearch = uiQuery.getParent() ;
      uiSearch.setRenderedChild(UISearchResult.class) ;
      UISearchResult uiSearchResult = uiSearch.getChild(UISearchResult.class); 
      QueryResult queryResult = null ;
      try {
        queryResult = queryService.execute(queryPath, wsName) ;
        uiSearchResult.resultMap_.clear() ;
        uiSearchResult.setQueryResults(queryResult) ;
        uiSearchResult.updateGrid() ;
      } catch(Exception e) {
        uiApp.addMessage(new ApplicationMessage("UISearchResult.msg.query-invalid", null)) ;
        uiSearch.setRenderedChild(UISavedQuery.class) ;
        return ;
      }
    }
  }

  static public class DeleteActionListener extends EventListener<UISavedQuery> {
    public void execute(Event<UISavedQuery> event) throws Exception {      
      UISavedQuery uiQuery = event.getSource() ;
      String userName = Util.getPortalRequestContext().getRemoteUser() ;
      QueryService queryService = uiQuery.getApplicationComponent(QueryService.class) ;      
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      queryService.removeQuery(path, userName) ;
      uiQuery.updateGrid() ;
      uiQuery.setRenderSibbling(UISavedQuery.class) ;
    }
  }
}
