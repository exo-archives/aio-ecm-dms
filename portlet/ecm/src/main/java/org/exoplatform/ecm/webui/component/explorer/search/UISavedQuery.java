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
import javax.portlet.PortletPreferences;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentWorkspace;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.queries.QueryService;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupWindow;
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
        @EventConfig(listeners = UISavedQuery.DeleteActionListener.class, confirm = "UISavedQuery.msg.confirm-delete-query"),
        @EventConfig(listeners = UISavedQuery.EditActionListener.class)
    }
)

public class UISavedQuery extends UIContainer implements UIPopupComponent {

  final static public String EDIT_FORM = "EditSavedQueryForm" ;
  
  private List<Node> sharedQueries_ = new ArrayList<Node>() ;
  private boolean isQuickSearch_ = false ;

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
  
  public void initPopupEditForm(Query query) throws Exception {
    removeChildById(EDIT_FORM) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, EDIT_FORM) ;
    uiPopup.setWindowSize(500,0) ;
    UIJCRAdvancedSearch uiJAdvancedSearch = 
      createUIComponent(UIJCRAdvancedSearch.class, null, "EditQueryForm") ;
    uiJAdvancedSearch.setActions(new String[] {"Save", "Cancel"}) ;
    uiPopup.setUIComponent(uiJAdvancedSearch) ;
    uiPopup.setRendered(true) ;
    uiJAdvancedSearch.setIsEdit(true) ;
    uiJAdvancedSearch.setQuery(query) ;
    uiJAdvancedSearch.update(query) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }
  
  public boolean hasQueries() throws Exception {
    QueryService queryService = getApplicationComponent(QueryService.class) ;
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
    String repository = portletPref.getValue(Utils.REPOSITORY, "") ;
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    List<Query> queries = queryService.getQueries(userName, repository);
    if (queries == null || queries.isEmpty()) return false;
    return true;
  }

  public List<Query> getQueries() throws Exception {
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
    String repository = portletPref.getValue(Utils.REPOSITORY, "") ;
    return getApplicationComponent(QueryService.class).getQueries(userName, repository);
  }
  
  public String getCurrentUserId() { return Util.getPortalRequestContext().getRemoteUser() ;}
  
  public boolean hasSharedQueries() throws Exception {
    OrganizationService organizationService = getApplicationComponent(OrganizationService.class) ;
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
    String repository = portletPref.getValue(Utils.REPOSITORY, "") ;
    QueryService queryService = getApplicationComponent(QueryService.class) ;
    String userName = pcontext.getRemoteUser() ;
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
    sharedQueries_ = queryService.getSharedQueriesByPermissions(roles, repository) ;
    if(queryService.getSharedQueriesByPermissions(roles, repository).size() > 0) return true ;      
    return false ;                
  }
  
  public List<Node> getSharedQueries() { return sharedQueries_ ; }
  
  public void activate() throws Exception { }
  
  public void deActivate() throws Exception { }
  
  public void setIsQuickSearch(boolean isQuickSearch) { isQuickSearch_ = isQuickSearch ; }
  
  static public class ExecuteActionListener extends EventListener<UISavedQuery> {
    public void execute(Event<UISavedQuery> event) throws Exception {      
      UISavedQuery uiQuery = event.getSource() ;
      UIJCRExplorer uiExplorer = uiQuery.getAncestorOfType(UIJCRExplorer.class) ;
      PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
      PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
      String repository = portletPref.getValue(Utils.REPOSITORY, "") ;
      String wsName = uiQuery.getAncestorOfType(UIJCRExplorer.class).getCurrentWorkspace() ;
      UIApplication uiApp = uiQuery.getAncestorOfType(UIApplication.class) ;
      QueryService queryService = uiQuery.getApplicationComponent(QueryService.class) ;
      String queryPath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIComponent uiSearch = null;
      UISearchResult uiSearchResult = null ;
      if(uiQuery.isQuickSearch_) {
        uiSearch = uiExplorer.getChild(UIWorkingArea.class).getChild(UIDocumentWorkspace.class) ;
        uiSearchResult = ((UIDocumentWorkspace)uiSearch).getChild(UISearchResult.class) ;
        uiSearchResult.setIsQuickSearch(true) ;
      } else {
        uiSearch = uiQuery.getParent() ;
        ((UIECMSearch)uiSearch).setRenderedChild(UISearchResult.class) ;
        uiSearchResult = ((UIECMSearch)uiSearch).getChild(UISearchResult.class); 
      }
      QueryResult queryResult = null ;
      try {
        queryResult = queryService.execute(queryPath, wsName, repository) ;
        if(queryResult == null || queryResult.getNodes().getSize() ==0) {
          uiApp.addMessage(new ApplicationMessage("UISavedQuery.msg.not-result-found", null)) ; 
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          if(!uiQuery.isQuickSearch_) ((UIECMSearch)uiSearch).setRenderedChild(UISavedQuery.class) ;
          return ;
        }
        uiSearchResult.resultMap_.clear() ;
        uiSearchResult.setQueryResults(queryResult) ;
        uiSearchResult.updateGrid() ;
      } catch(Exception e) {
        uiApp.addMessage(new ApplicationMessage("UISearchResult.msg.query-invalid", null)) ;
        if(!uiQuery.isQuickSearch_) ((UIECMSearch)uiSearch).setRenderedChild(UISavedQuery.class) ;
        return ;
      }
      if(uiQuery.isQuickSearch_) {
        ((UIDocumentWorkspace)uiSearch).setRenderedChild(UISearchResult.class) ;
        UIPopupAction uiPopup = uiExplorer.getChild(UIPopupAction.class) ;
        uiPopup.deActivate() ;
      }
    }
  }

  static public class EditActionListener extends EventListener<UISavedQuery> {
    public void execute(Event<UISavedQuery> event) throws Exception {      
      UISavedQuery uiQuery = event.getSource() ;
      PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
      PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
      String repository = portletPref.getValue(Utils.REPOSITORY, "") ;
      String userName = Util.getPortalRequestContext().getRemoteUser() ;
      QueryService queryService = uiQuery.getApplicationComponent(QueryService.class) ;
      String queryPath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Query query = queryService.getQueryByPath(queryPath, userName, repository) ;
      uiQuery.initPopupEditForm(query) ;
      if(!uiQuery.isQuickSearch_) {
        UIECMSearch uiECSearch = uiQuery.getParent() ;
        uiECSearch.setRenderedChild(UISavedQuery.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiECSearch) ;
      } else {
        event.getRequestContext().addUIComponentToUpdateByAjax(uiQuery.getParent()) ;
      }
    }
  }
  
  static public class DeleteActionListener extends EventListener<UISavedQuery> {
    public void execute(Event<UISavedQuery> event) throws Exception {      
      UISavedQuery uiQuery = event.getSource() ;
      PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
      PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
      String repository = portletPref.getValue(Utils.REPOSITORY, "") ;
      String userName = Util.getPortalRequestContext().getRemoteUser() ;
      QueryService queryService = uiQuery.getApplicationComponent(QueryService.class) ;      
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      queryService.removeQuery(path, userName, repository) ;
      uiQuery.updateGrid() ;
      uiQuery.setRenderSibbling(UISavedQuery.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiQuery) ;
    }
  }
}