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
package org.exoplatform.ecm.webui.component.explorer.search;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.QueryResult;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
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
public class UISearchResult extends UIContainer {

  private QueryResult queryResult_;
  private long searchTime_ = 0; 
  private boolean flag_ = false ;
  private boolean isQuickSearch_ = false ;
  private UIQueryResultPageIterator uiPageIterator_ ;
  private List<Node> currentListNodes_ = new ArrayList<Node>() ;
  private int currentAvailablePage_ = 0 ;
  private boolean isEndOfIterator_ = false ;
  
  static private int PAGE_SIZE = 10 ;

  public UISearchResult() throws Exception {
    uiPageIterator_ = addChild(UIQueryResultPageIterator.class, null, null) ;
  }

  public void setIsQuickSearch(boolean isQuickSearch) { isQuickSearch_ = isQuickSearch ; }

  public void setQueryResults(QueryResult queryResult) throws Exception {
    queryResult_ = queryResult ;         
  }  
  
  public long getSearchTime() { return searchTime_ ; }
  public void setSearchTime(long time) { this.searchTime_ = time; }  
  
  public List getCurrentList() throws Exception { 
    return uiPageIterator_.getCurrentPageData() ;    
  }

  private void addNode(List<Node> listNodes, Node node) throws Exception {
    List<Node> checkList = new ArrayList<Node>() ;
    if(flag_) checkList = currentListNodes_ ; 
    else checkList = listNodes ;
    if(node.getName().equals(Utils.JCR_CONTENT)) {
      if(!checkList.contains(node.getParent())) {
        listNodes.add(node.getParent()) ;
      }
    } else if(!checkList.contains(node)){
      listNodes.add(node) ;
    }
  }
  
  @SuppressWarnings("unchecked")
  public List<Node> getResultList() throws Exception {
    List<Node> listNodes = new ArrayList<Node>() ;    
    long resultListSize = queryResult_.getNodes().getSize() ;
    if(!queryResult_.getNodes().hasNext()) return currentListNodes_ ;
    if(resultListSize > 100) {
      for(NodeIterator iter = queryResult_.getNodes();iter.hasNext();) {
        Node node = iter.nextNode() ;
        addNode(listNodes, node) ;
        if(!iter.hasNext()) isEndOfIterator_ = true ;
        if(listNodes.size() == 100) {
          currentListNodes_.addAll(listNodes) ;
          break ;
        }
        if(listNodes.size() < 100 && !iter.hasNext()) currentListNodes_.addAll(listNodes) ;
        flag_ = true ;
      }
    } else {
      for(NodeIterator iter = queryResult_.getNodes();iter.hasNext();) {
        Node node = iter.nextNode() ;
        if(!iter.hasNext()) isEndOfIterator_ = true ;
        addNode(listNodes, node) ;
      }
      currentListNodes_= listNodes ;
    }
    return currentListNodes_ ;
  }

  public void clearAll() {
    flag_ = false ;
    isEndOfIterator_ = false ;
    currentListNodes_.clear() ;
  }
  
  public UIQueryResultPageIterator getUIPageIterator() { return uiPageIterator_ ; }

  public void updateGrid() throws Exception {
    SearchResultPageList pageList = new SearchResultPageList(queryResult_, getResultList(), PAGE_SIZE, isEndOfIterator_) ;
    currentAvailablePage_ = currentListNodes_.size()/PAGE_SIZE ;
    uiPageIterator_.setSearchResultPageList(pageList) ;
    uiPageIterator_.setPageList(pageList) ;
  }
  
  public int getCurrentAvaiablePage() { return currentAvailablePage_ ; }
   
  static  public class ViewActionListener extends EventListener<UISearchResult> {
    public void execute(Event<UISearchResult> event) throws Exception {
      UISearchResult uiSearchResult = event.getSource() ;            
      UIJCRExplorer uiExplorer = uiSearchResult.getAncestorOfType(UIJCRExplorer.class) ;
      String repository = uiExplorer.getRepositoryName() ;
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIApplication uiApp = uiSearchResult.getAncestorOfType(UIApplication.class) ;
      Node node = null ;
      try {
        node = (Node)uiExplorer.getSession().getItem(path) ;
      } catch(AccessDeniedException ace) {
        uiApp.addMessage(new ApplicationMessage("UISearchResult.msg.access-denied", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      TemplateService templateService = uiSearchResult.getApplicationComponent(TemplateService.class) ;
      if(!templateService.isManagedNodeType(node.getPrimaryNodeType().getName(), repository)) {
        uiApp.addMessage(new ApplicationMessage("UISearchResult.msg.not-support", null, 
                                                ApplicationMessage.WARNING)) ;
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
      try {
        uiExplorer.getSession().getItem(folderPath);
      } catch(AccessDeniedException ace) {
        UIApplication uiApp = uiSearchResult.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UISearchResult.msg.access-denied", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } catch(Exception e) {
        e.printStackTrace() ;
      }
      uiExplorer.setSelectNode(folderPath, uiExplorer.getSession()) ;
      uiExplorer.updateAjax(event) ;
    }
  }
}