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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.impl.core.JCRPath;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
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
        @EventConfig(listeners = UISearchResult.OpenFolderActionListener.class),
        @EventConfig(listeners = UISearchResult.SortASCActionListener.class),        
        @EventConfig(listeners = UISearchResult.SortDESCActionListener.class)
    }
)
public class UISearchResult extends UIContainer {

  private QueryResult queryResult_;
  private long searchTime_ = 0; 
  private boolean flag_ = false;
  private boolean isQuickSearch_ = false;
  private UIQueryResultPageIterator uiPageIterator_;
  private List<Node> currentListNodes_ = new ArrayList<Node>();
  private List<Row> currentListRows_ = new ArrayList<Row>();
  private int currentAvailablePage_ = 0;
  private boolean isEndOfIterator_ = false;
  private static String iconType = "";
  private static String iconScore = "";
  static private int PAGE_SIZE = 10;
  
  public UISearchResult() throws Exception {
    uiPageIterator_ = addChild(UIQueryResultPageIterator.class, null, null);
  }

  public void setIsQuickSearch(boolean isQuickSearch) { isQuickSearch_ = isQuickSearch; }

  public void setQueryResults(QueryResult queryResult) throws Exception {
    queryResult_ = queryResult;         
  }  
  
  public long getSearchTime() { return searchTime_; }
  public void setSearchTime(long time) { this.searchTime_ = time; }  
  
  public List getCurrentList() throws Exception { 
    return uiPageIterator_.getCurrentPageData();    
  }

  private void addNode(List<Node> listNodes, Node node, List<Row> listRows, Row r) throws Exception {
    List<Node> checkList = new ArrayList<Node>();
    if (flag_) checkList = currentListNodes_; 
    else checkList = listNodes;
    if (node.getName().equals(Utils.JCR_CONTENT)) {
      if (!checkList.contains(node.getParent())) {
        listNodes.add(node.getParent());
        listRows.add(r);
      }
    } else if (!checkList.contains(node)) {
      listNodes.add(node);
      listRows.add(r);
    }
  }
  
  public Session getSession() throws Exception {
    return getAncestorOfType(UIJCRExplorer.class).getSession();
  }
    
  public Date getDateCreated(Node node) throws Exception{
    if (node.hasProperty("exo:dateCreated")) {
      return node.getProperty("exo:dateCreated").getDate().getTime();
    }
    return new GregorianCalendar().getTime();
  }
  
  public List<Row> getResultList() throws Exception {    
    List<Node> listNodes = new ArrayList<Node>();
    List<Row> listRows = new ArrayList<Row>();    
    long resultListSize = queryResult_.getNodes().getSize();
    if (!queryResult_.getRows().hasNext()) return currentListRows_;    
    if (resultListSize > 100) {
      for (RowIterator iter = queryResult_.getRows(); iter.hasNext();) {
        Row r = iter.nextRow();
        String path = r.getValue("jcr:path").getString();
        JCRPath nodePath = ((SessionImpl)getSession()).getLocationFactory().parseJCRPath(path);
        Node resultNode = (Node)getSession().getItem(nodePath.getAsString(false));
        addNode(listNodes, resultNode, listRows, r);
        if (!iter.hasNext()) isEndOfIterator_ = true;
        if (listNodes.size() == 100) {
          currentListNodes_.addAll(listNodes);
          currentListRows_.addAll(listRows); 
          break;
        }
        if (listNodes.size() < 100 && iter.hasNext()) {
          currentListNodes_.addAll(listNodes);
          currentListRows_.addAll(listRows);
          flag_ = true;
        }
      }
    } else {
      for (RowIterator iter = queryResult_.getRows(); iter.hasNext();) {
        Row r = iter.nextRow();        
        if (!iter.hasNext()) isEndOfIterator_ = true;
        String path = r.getValue("jcr:path").getString();
        JCRPath nodePath = ((SessionImpl)getSession()).getLocationFactory().parseJCRPath(path);
        Node resultNode = (Node)getSession().getItem(nodePath.getAsString(false));
        addNode(listNodes, resultNode, listRows, r);        
      }
      currentListNodes_= listNodes;
      currentListRows_ = listRows;
    }
    return currentListRows_;
  }
  
  public void clearAll() {
    flag_ = false;
    isEndOfIterator_ = false;
    currentListNodes_.clear();
  }
  
  public UIQueryResultPageIterator getUIPageIterator() { return uiPageIterator_; }

  public void updateGrid(boolean flagCheck) throws Exception {
    SearchResultPageList pageList;
    if (flagCheck) {
      pageList = new SearchResultPageList(queryResult_, getResultList(), PAGE_SIZE, isEndOfIterator_);
    } else {
      pageList = new SearchResultPageList(queryResult_, currentListRows_, PAGE_SIZE, isEndOfIterator_);
    }
    currentAvailablePage_ = currentListNodes_.size()/PAGE_SIZE;
    uiPageIterator_.setSearchResultPageList(pageList);
    uiPageIterator_.setPageList(pageList);
  }
  
  public int getCurrentAvaiablePage() { return currentAvailablePage_; }
  
  private static class SearchComparator implements Comparator<Row> {
    public int compare(Row row1, Row row2) {
      try {
        if (iconType.equals("BlueUpArrow") || iconType.equals("BlueDownArrow")) {
          String s1 = row1.getValue("jcr:primaryType").getString();
          String s2 = row2.getValue("jcr:primaryType").getString();
          if (iconType.trim().equals("BlueUpArrow")) { return s2.compareTo(s1); }        
          return s1.compareTo(s2);
        } else if (iconScore.equals("BlueUpArrow") || iconScore.equals("BlueDownArrow")) {
          Long l1 = row1.getValue("jcr:score").getLong();
          Long l2 = row2.getValue("jcr:score").getLong();
          if (iconScore.trim().equals("BlueUpArrow")) { return l2.compareTo(l1); }        
          return l1.compareTo(l2);
        }
      } catch (Exception e) {  
        e.printStackTrace();
      }            
      return 0;
    }        
  }
  
  public String StriptHTML(String s) {
    String[] targets = {"<div>", "</div>", "<span>", "</span>"};
    for (String target : targets) {
      s = s.replace(target, "");
    }
    return s; 
  }
  
  static  public class ViewActionListener extends EventListener<UISearchResult> {
    public void execute(Event<UISearchResult> event) throws Exception {
      UISearchResult uiSearchResult = event.getSource();            
      UIJCRExplorer uiExplorer = uiSearchResult.getAncestorOfType(UIJCRExplorer.class);
      String repository = uiExplorer.getRepositoryName();
      String path = event.getRequestContext().getRequestParameter(OBJECTID);
      UIApplication uiApp = uiSearchResult.getAncestorOfType(UIApplication.class);
      Node node = null;
      try {
        node = (Node)uiExplorer.getSession().getItem(path);
      } catch(AccessDeniedException ace) {
        uiApp.addMessage(new ApplicationMessage("UISearchResult.msg.access-denied", null, 
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      TemplateService templateService = uiSearchResult.getApplicationComponent(TemplateService.class);
      if (!templateService.isManagedNodeType(node.getPrimaryNodeType().getName(), repository)) {
        uiApp.addMessage(new ApplicationMessage("UISearchResult.msg.not-support", null, 
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if (uiSearchResult.isQuickSearch_) {
        UIPopupWindow uiPopup = uiExplorer.getChildById("ViewSearch");
        if (uiPopup == null) {
          uiPopup = uiExplorer.addChild(UIPopupWindow.class, null, "ViewSearch");
        }
        uiPopup.setResizable(true);
        UIViewSearchResult uiViewSearch = uiPopup.createUIComponent(UIViewSearchResult.class, null, null);
        uiViewSearch.setNode(node);

        uiPopup.setWindowSize(560,450);
        uiPopup.setUIComponent(uiViewSearch);
        uiPopup.setRendered(true);
        uiPopup.setShow(true);
        return;
      }      
      UIECMSearch uiECMSearch = uiSearchResult.getParent();
      UIViewSearchResult uiView = uiECMSearch.getChild(UIViewSearchResult.class);
      if (uiView == null) uiView = uiECMSearch.addChild(UIViewSearchResult.class, null, null);
      uiView.setNode(node);
      uiECMSearch.setRenderedChild(UIViewSearchResult.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiECMSearch.getParent());
    }
  }

  static  public class OpenFolderActionListener extends EventListener<UISearchResult> {
    public void execute(Event<UISearchResult> event) throws Exception {
      UISearchResult uiSearchResult = event.getSource();
      UIJCRExplorer uiExplorer = uiSearchResult.getAncestorOfType(UIJCRExplorer.class);
      String path = event.getRequestContext().getRequestParameter(OBJECTID);
      String folderPath = path.substring(0, path.lastIndexOf("/"));
      try {
        uiExplorer.getSession().getItem(folderPath);
      } catch(AccessDeniedException ace) {
        UIApplication uiApp = uiSearchResult.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UISearchResult.msg.access-denied", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch(Exception e) {
        e.printStackTrace();
      }      
      uiExplorer.setSelectNode(folderPath, uiExplorer.getSession());
      uiExplorer.updateAjax(event);
    }
  }
  
  static  public class SortASCActionListener extends EventListener<UISearchResult> {
    public void execute(Event<UISearchResult> event) throws Exception {
      UISearchResult uiSearchResult = event.getSource();     
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
      if (objectId.equals("type")) {
        iconType = "BlueDownArrow";
        iconScore = "";
      } else if (objectId.equals("score")) {
        iconScore = "BlueDownArrow";
        iconType = "";
      }
      Collections.sort(uiSearchResult.currentListRows_, new SearchComparator());
      SearchResultPageList pageList = new SearchResultPageList(uiSearchResult.queryResult_, 
          uiSearchResult.currentListRows_, PAGE_SIZE, uiSearchResult.isEndOfIterator_);
      uiSearchResult.currentAvailablePage_ = uiSearchResult.currentListNodes_.size()/PAGE_SIZE;
      uiSearchResult.uiPageIterator_.setSearchResultPageList(pageList);
      uiSearchResult.uiPageIterator_.setPageList(pageList);      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSearchResult.getParent());      
    }
  } 
  
  static  public class SortDESCActionListener extends EventListener<UISearchResult> {
    public void execute(Event<UISearchResult> event) throws Exception {
      UISearchResult uiSearchResult = event.getSource() ;     
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
      if (objectId.equals("type")) {
        iconType = "BlueUpArrow";
        iconScore = "";
      } else if (objectId.equals("score")) {
        iconScore = "BlueUpArrow";
        iconType = "";
      }
      Collections.sort(uiSearchResult.currentListRows_, new SearchComparator());
      SearchResultPageList pageList = new SearchResultPageList(uiSearchResult.queryResult_, 
          uiSearchResult.currentListRows_, PAGE_SIZE, uiSearchResult.isEndOfIterator_);
      uiSearchResult.currentAvailablePage_ = uiSearchResult.currentListNodes_.size()/PAGE_SIZE;
      uiSearchResult.uiPageIterator_.setSearchResultPageList(pageList);
      uiSearchResult.uiPageIterator_.setPageList(pageList);      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSearchResult.getParent());      
    }
  } 
}