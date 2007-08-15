/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.portlet.PortletPreferences;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.utils.SessionsUtils;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentWorkspace;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
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
 
  private UIPageIterator uiPageIterator_ ;
  private List<Node> sharedQueries_ = new ArrayList<Node>() ;  
  private List<Query> privateQueries = new ArrayList<Query>() ;
  
  private boolean isQuickSearch_ = false ;

  public UISavedQuery() throws Exception {        
    uiPageIterator_ = addChild(UIPageIterator.class, null, "SavedQueryIterator");
    updateGrid() ;  
  }  

  public void updateGrid() throws Exception {
    PageList pageList = new ObjectPageList(queryList(), 10) ;
    uiPageIterator_.setPageList(pageList) ;  
  }
  
  public List<Object> queryList() throws Exception {
    List<Object> objectList = new ArrayList<Object>() ;
    if(hasSharedQueries()) {
      for(Node node : getSharedQueries()) {
        objectList.add(node) ;
      }
    }
    if(hasQueries()) {
      for(Query query : getQueries()) {
        objectList.add(query) ;
      }
    }
    return objectList ;
  }

  public UIPageIterator getUIPageIterator() { return uiPageIterator_ ; }
  
  public List getQueryList() throws Exception { return uiPageIterator_.getCurrentPageData() ; }
  
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
    try {
      privateQueries = queryService.getQueries(getCurrentUserId(), repository,SessionsUtils.getSessionProvider());
      return !privateQueries.isEmpty() ;    
    } catch(AccessDeniedException ace) {
      return privateQueries.isEmpty() ;
    }
  }
  
  public List<Query> getQueries() throws Exception { return privateQueries ; }
  
  public String getCurrentUserId() { return Util.getPortalRequestContext().getRemoteUser() ;}
  
  public boolean hasSharedQueries() throws Exception {    
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
    String repository = portletPref.getValue(Utils.REPOSITORY, "") ;
    QueryService queryService = getApplicationComponent(QueryService.class) ;
    String userId = pcontext.getRemoteUser() ;    
    SessionProvider provider = SessionsUtils.getSystemProvider() ;    
    sharedQueries_ = queryService.getSharedQueries(userId, repository,provider) ;
    return !sharedQueries_.isEmpty();
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
        queryResult = queryService.execute(queryPath, wsName, repository,SessionsUtils.getSystemProvider()) ;
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
      Query query = queryService.getQueryByPath(queryPath, userName, repository,SessionsUtils.getSystemProvider()) ;
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