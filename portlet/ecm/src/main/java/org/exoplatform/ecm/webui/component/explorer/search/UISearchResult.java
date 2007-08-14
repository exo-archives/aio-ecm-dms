/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.QueryResult;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponentDecorator;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Oct 2, 2006
 * 16:37:15 
 * 
 * Edited by : Dang Van Minh
 *             minh.dang@exoplatform.com
 * Jan 5, 2007
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/explorer/search/UISearchResult.gtmpl",
    events = { 
        @EventConfig(listeners = UISearchResult.ViewActionListener.class),
        @EventConfig(listeners = UISearchResult.OpenFolderActionListener.class)
    }
)
public class UISearchResult extends UIComponentDecorator {
  
  public Map<String, Node> resultMap_ = new HashMap<String, Node>() ;
  private boolean isQuickSearch_ = false ;
  private UIPageIterator uiPageIterator_ ;

  public UISearchResult() throws Exception {
    uiPageIterator_ = createUIComponent(UIPageIterator.class, null, null) ;
    setUIComponent(uiPageIterator_) ;
  }

  public void setIsQuickSearch(boolean isQuickSearch) { isQuickSearch_ = isQuickSearch ; }

  public void setQueryResults(QueryResult queryResult) throws Exception {
    if(queryResult != null){
      NodeIterator iter = queryResult.getNodes() ;
      while(iter.hasNext()){
        Node node = iter.nextNode() ;
        resultMap_.put(node.getPath(), node) ;
      }
    }
  }

  public Node[] getNodeIterator() throws Exception { 
    return resultMap_.values().toArray(new Node[]{}) ; 
  }
  
  public List getCurrentList() throws Exception { return uiPageIterator_.getCurrentPageData() ; }
  
  public List<Node> getResultList() throws Exception {
    List<Node> lists = new ArrayList<Node>() ;
    for(Node node : getNodeIterator()) {
      lists.add(node) ;
    }
    List<Node> realList = new ArrayList<Node>() ;
    for(Node node : lists) {
      if(node.getName().equals(Utils.JCR_CONTENT)) {
        if(!lists.contains(node.getParent())) {
          realList.add(node.getParent()) ;
        } 
      } else {
        realList.add(node) ;
      }
    }
    return realList ;
  }

  public UIPageIterator getUIPageIterator() { return uiPageIterator_ ; }

  public void updateGrid() throws Exception {
    PageList pageList = new ObjectPageList(getResultList(), 10) ;
    uiPageIterator_.setPageList(pageList) ;
  }

  public PortletPreferences getPortletPreferences() {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletRequest prequest = pcontext.getRequest() ;
    PortletPreferences portletPref = prequest.getPreferences() ;
    return portletPref ;
  }

  static  public class ViewActionListener extends EventListener<UISearchResult> {
    public void execute(Event<UISearchResult> event) throws Exception {
      UISearchResult uiSearchResult = event.getSource() ;
      String repository = uiSearchResult.getPortletPreferences().getValue(Utils.REPOSITORY, "") ;
      UIJCRExplorer uiExplorer = uiSearchResult.getAncestorOfType(UIJCRExplorer.class) ;
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Node node = (Node)uiExplorer.getSession().getItem(path) ;
      TemplateService templateService = uiSearchResult.getApplicationComponent(TemplateService.class) ;
      if(!templateService.isManagedNodeType(node.getPrimaryNodeType().getName(), repository)) {
        UIApplication uiApp = uiSearchResult.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UISearchResult.msg.not-support", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(uiSearchResult.isQuickSearch_) {
        UIPopupWindow uiPopup = uiExplorer.getChildById("ViewSearch") ;
        if(uiPopup == null) {
          uiPopup = uiExplorer.addChild(UIPopupWindow.class, null, "ViewSearch") ;
        }
        uiPopup.setResizable(true) ;
        UIViewSearchResult uiViewSearch = uiPopup.createUIComponent(UIViewSearchResult.class, null, null) ;
        uiViewSearch.setNode(node) ;

        uiPopup.setWindowSize(560,450) ;
        uiPopup.setUIComponent(uiViewSearch) ;
        uiPopup.setRendered(true) ;
        uiPopup.setShow(true) ;
        return ;
      }      
      UIECMSearch uiECMSearch = uiSearchResult.getParent() ;
      UIViewSearchResult uiView = uiECMSearch.getChild(UIViewSearchResult.class) ;
      if(uiView == null) uiView = uiECMSearch.addChild(UIViewSearchResult.class, null, null) ;
      uiView.setNode(node) ;
      uiECMSearch.setRenderedChild(UIViewSearchResult.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiECMSearch.getParent()) ;
    }
  }

  static  public class OpenFolderActionListener extends EventListener<UISearchResult> {
    public void execute(Event<UISearchResult> event) throws Exception {
      UISearchResult uiSearchResult = event.getSource() ;
      UIJCRExplorer uiExplorer = uiSearchResult.getAncestorOfType(UIJCRExplorer.class) ;
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String folderPath = path.substring(0, path.lastIndexOf("/")) ;
      uiExplorer.setSelectNode(folderPath, uiExplorer.getSession()) ;
      uiExplorer.updateAjax(event) ;
    }
  }
}