/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.queries;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.component.view.Util;
import org.exoplatform.services.cms.queries.QueryService;
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
 * Dec 29, 2006  
 * 11:30:17 AM
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/admin/queries/UIQueriesList.gtmpl",
    events = {
        @EventConfig(listeners = UIQueriesList.AddQueryActionListener.class),
        @EventConfig(listeners = UIQueriesList.EditActionListener.class),
        @EventConfig(listeners = UIQueriesList.DeleteActionListener.class, confirm = "UIQueriesList.msg.confirm-delete")
    }
)
public class UIQueriesList extends UIContainer {

  final static public String[] ACTIONS = {"AddQuery"} ;
  final static public String ST_ADD = "AddQueryForm" ;
  final static public String ST_EDIT = "EditQueryForm" ;
  
  public UIQueriesList() throws Exception {
    addChild(UIPageIterator.class, null, "QueriesListIterator");
    updateQueriesGrid() ;
  }

  public String[] getActions() { return ACTIONS ; }
  
  public void updateQueriesGrid() throws Exception {
    PageList pageList = new ObjectPageList(getAllSharedQueries(), 10) ;
    UIPageIterator uiPateIterator = getChild(UIPageIterator.class) ;
    uiPateIterator.setPageList(pageList) ;    
  }
  
  public UIPageIterator getUIPageIterator() { return getChild(UIPageIterator.class) ; }
  
  public List<Node> getAllSharedQueries() throws Exception {
    QueryService queryService = getApplicationComponent(QueryService.class) ;
    return queryService.getSharedQueries() ;
  }
  
  static public class AddQueryActionListener extends EventListener<UIQueriesList> {
    public void execute(Event<UIQueriesList> event) throws Exception {
      UIQueriesManager uiQueriesMan = event.getSource().getParent() ;
      uiQueriesMan.removeChildById(UIQueriesList.ST_EDIT) ;
      uiQueriesMan.initFormPopup(UIQueriesList.ST_ADD) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiQueriesMan) ;
    }
  }
  
  static public class EditActionListener extends EventListener<UIQueriesList> {
    public void execute(Event<UIQueriesList> event) throws Exception {
      UIQueriesManager uiQueriesMan = event.getSource().getParent() ;
      uiQueriesMan.removeChildById(UIQueriesList.ST_ADD) ;
      uiQueriesMan.initFormPopup(UIQueriesList.ST_EDIT ) ;
      String queryName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIQueriesForm uiForm = uiQueriesMan.findFirstComponentOfType(UIQueriesForm.class) ;
      uiForm.update(queryName) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiQueriesMan) ;
    }
  }
  
  static public class DeleteActionListener extends EventListener<UIQueriesList> {
    public void execute(Event<UIQueriesList> event) throws Exception {
      UIQueriesManager uiQueriesMan = event.getSource().getParent() ;
      String userName = Util.getPortalRequestContext().getRemoteUser() ;
      String queryName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      QueryService queryService = event.getSource().getApplicationComponent(QueryService.class) ;
      queryService.removeQuery(queryName, userName) ;
      event.getSource().updateQueriesGrid() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiQueriesMan) ;
    }
  }

}
