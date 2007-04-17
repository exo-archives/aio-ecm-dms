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

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPageIterator;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
public class UISearchResult extends UIContainer {
  public Map<String, Node> resultMap_ = new HashMap<String, Node>() ;
  private boolean isQuickSearch_ = false ;
  
  public UISearchResult() throws Exception {
    addChild(UIPageIterator.class, null, null) ;
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
  
  public List<Node> getResultList() throws Exception {
    List<Node> lists = new ArrayList<Node>() ;
    for(Node node : getNodeIterator()) {
      lists.add(node) ;
    }
    return lists ;
  }
  
  public UIPageIterator  getUIPageIterator() {  return getChild(UIPageIterator.class) ; }
  
  public void updateGrid() throws Exception {
    PageList pageList = new ObjectPageList(getResultList(), 10) ;
    getUIPageIterator().setPageList(pageList) ;
  }
  
  static  public class ViewActionListener extends EventListener<UISearchResult> {
    public void execute(Event<UISearchResult> event) throws Exception {
      UISearchResult uiSearchResult = event.getSource() ;
      UIJCRExplorer uiExplorer = uiSearchResult.getAncestorOfType(UIJCRExplorer.class) ;
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Node node = (Node)uiExplorer.getSession().getItem(path) ;
      TemplateService templateService = uiSearchResult.getApplicationComponent(TemplateService.class) ;
      if(!templateService.isManagedNodeType(node.getPrimaryNodeType().getName())) {
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