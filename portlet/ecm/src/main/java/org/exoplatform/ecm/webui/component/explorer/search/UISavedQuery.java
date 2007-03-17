/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.cms.queries.QueryService;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.webui.application.ApplicationMessage;
import org.exoplatform.webui.application.RequestContext;
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
    List<Query> queries = queryService.getQueries();
    if (queries == null || queries.isEmpty()) return false;
    return true;
  }

  public List<Query> getQueries() throws Exception {
    return getApplicationComponent(QueryService.class).getQueries();
  }
  
  public boolean hasSharedQueries() throws Exception {
    OrganizationService organizationService = getApplicationComponent(OrganizationService.class) ;
    RequestContext context = RequestContext.getCurrentInstance() ;
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
  
  public List<PermissionBean> getPermissions(String path) throws Exception {
    List<PermissionBean> permBeans = new ArrayList<PermissionBean>();
    Session session = getAncestorOfType(UIJCRExplorer.class).getSession();
    Node node = (Node)session.getItem(path) ;
    List permsList = ((ExtendedNode)node).getACL().getPermissionEntries() ;
    Map<String, List<String>> permsMap = new HashMap<String, List<String>>() ;
    Iterator perIter = permsList.iterator() ;
    while(perIter.hasNext()) {
      AccessControlEntry accessControlEntry = (AccessControlEntry)perIter.next() ;
      String currentIdentity = accessControlEntry.getIdentity();
      String currentPermission = accessControlEntry.getPermission();
      List<String> currentPermissionsList = permsMap.get(currentIdentity);
      if(!permsMap.containsKey(currentIdentity)) {
        permsMap.put(currentIdentity, null) ;
      }
      if(currentPermissionsList == null) currentPermissionsList = new ArrayList<String>() ;
      if(!currentPermissionsList.contains(currentPermission)) {
        currentPermissionsList.add(currentPermission) ;
      }
      permsMap.put(currentIdentity, currentPermissionsList) ;
    }
    Set keys = permsMap.keySet(); 
    Iterator keysIter = keys.iterator() ;
    while(keysIter.hasNext()) {
      String userOrGroup = (String) keysIter.next();            
      List<String> permissions = permsMap.get(userOrGroup);      
      PermissionBean permBean = new PermissionBean();
      permBean.setUsersOrGroups(userOrGroup);
      for(String perm : permissions) {
        if(PermissionType.READ.equals(perm)) permBean.setRead(true);
        else if(PermissionType.ADD_NODE.equals(perm))  permBean.setAddNode(true);
        else if(PermissionType.SET_PROPERTY.equals(perm))  permBean.setSetProperty(true);
        else if(PermissionType.REMOVE.equals(perm))  permBean.setRemove(true);
      }
      permBeans.add(permBean);
    }
    return permBeans;
  }
  
  static public class ExecuteActionListener extends EventListener<UISavedQuery> {
    public void execute(Event<UISavedQuery> event) throws Exception {      
      UISavedQuery uiQuery = event.getSource() ;
      UIApplication uiApp = uiQuery.getAncestorOfType(UIApplication.class) ;
      QueryService queryService = uiQuery.getApplicationComponent(QueryService.class) ;
      String queryPath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UISearch uiSearch = uiQuery.getParent() ;
      uiSearch.setRenderedChild(UISearchResult.class) ;
      UISearchResult uiSearchResult = uiSearch.getChild(UISearchResult.class); 
      QueryResult queryResult = null ;
      try {
        queryResult = queryService.execute(queryPath) ;
        uiSearchResult.updateGrid(queryResult) ;
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
      QueryService queryService = uiQuery.getApplicationComponent(QueryService.class) ;      
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      queryService.removeQuery(path) ;
      uiQuery.updateGrid() ;
      uiQuery.setRenderSibbling(UISavedQuery.class) ;
    }
  }
  
  public class PermissionBean {    
    private String usersOrGroups;
    private boolean read;
    private boolean addNode;
    private boolean setProperty;
    private boolean remove;    
    
    public String getUsersOrGroups() { return usersOrGroups; }
   
    public void setUsersOrGroups(String usersOrGroups) { this.usersOrGroups = usersOrGroups; }
    
    public boolean isAddNode() { return addNode; }
    
    public void setAddNode(boolean addNode) { this.addNode = addNode; }
    
    public boolean isRead() { return read; }
    
    public void setRead(boolean read) { this.read = read; }
    
    public boolean isRemove() { return remove; }
    
    public void setRemove(boolean remove) { this.remove = remove; }
    
    public boolean isSetProperty() { return setProperty; }
    
    public void setSetProperty(boolean setProperty) { this.setProperty = setProperty; }
  }
}
