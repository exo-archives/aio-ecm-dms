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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.cms.link.LinkUtils;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.impl.core.JCRPath;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;
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
  
  private static final String JCR_SYSTEM_PATH = "/jcr:system";

  /**
   * Logger.
   */
  private static final Log LOG  = ExoLogger.getLogger("explorer.search.UISearchResult");
  
  private QueryResult queryResult_;
  private long searchTime_ = 0; 
  private boolean flag_ = false;
  private UIQueryResultPageIterator uiPageIterator_;
  private List<Node> currentListNodes_ = new ArrayList<Node>();
  private List<Row> currentListRows_ = new ArrayList<Row>();
  private int currentAvailablePage_ = 0;
  private boolean isEndOfIterator_ = false;
  private static String iconType = "";
  private static String iconScore = "";
  static private int PAGE_SIZE = 10;
  private List<String> categoryPathList = new ArrayList<String>();
  private String constraintsCondition;
  @Deprecated
  private boolean isTaxonomyNode = false;
  
  private String workspaceName = null;
  private String currentPath = null;
  protected String keyword = "";
  protected RowIterator resultIter = null;
  protected LinkManager linkManager = null;
  protected Session checkSymlinkSession = null;
  protected String linkWorkspace  = null;
  final static private String  CHECK_LINK_MATCH_QUERY1= "select * from nt:base where jcr:path = '$0' and ( contains(*, '$1') or lower(exo:name) like '%$2%' or lower(exo:title) like '%$2%')";
  final static private String  CHECK_LINK_MATCH_QUERY2= "select * from nt:base where jcr:path like '$0/%' and ( contains(*, '$1') or lower(exo:name) like '%$2%' or lower(exo:title) like '%$2%')";
  
  public List<String> getCategoryPathList() { return categoryPathList; }
  public void setCategoryPathList(List<String> categoryPathListItem) {
    categoryPathList = categoryPathListItem; 
  }
  
  public String getConstraintsCondition() { return constraintsCondition; }
  public void setConstraintsCondition(String constraintsConditionItem) {
    constraintsCondition = constraintsConditionItem; 
  }
  
  public UISearchResult() throws Exception {
    uiPageIterator_ = addChild(UIQueryResultPageIterator.class, null, null);
  }

  public void setQueryResults(QueryResult queryResult) throws Exception {
  	queryResult_ = queryResult;
    resultIter = queryResult.getRows();
  }  
  
  public long getSearchTime() { return searchTime_; }
  public void setSearchTime(long time) { this.searchTime_ = time; }  
  
  public List getCurrentList() throws Exception {	  
    return uiPageIterator_.getCurrentPageData();    
  }

  public DateFormat getSimpleDateFormat() {
    Locale locale = Util.getUIPortal().getAncestorOfType(UIPortalApplication.class).getLocale();
    return SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT, locale);
  }
  @Deprecated
  public void setTaxonomyNode(boolean isTaxonomyNode, String workspaceName, String currentPath) {
    this.isTaxonomyNode = isTaxonomyNode;
    this.workspaceName = workspaceName;
    this.currentPath = currentPath;
  }
  @Deprecated
  public boolean isTaxonomyNode() { return isTaxonomyNode; }
  @Deprecated
  public Node getSymlinkNode(Node targetNode) throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    Session session = 
      SessionProviderFactory.createSessionProvider().getSession(workspaceName, repositoryService.getCurrentRepository());
    String queryStatement = 
      "select * from exo:taxonomyLink where jcr:path like '" + currentPath + "/%' " +
      		"and exo:uuid='"+targetNode.getUUID()+"' " +
      		"and exo:workspace='"+targetNode.getSession().getWorkspace().getName()+"' order by exo:primaryType DESC";
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    Query query = queryManager.createQuery(queryStatement, Query.SQL);
    if(query.execute().getNodes().getSize() > 0) return query.execute().getNodes().nextNode();
    return null;
  }
  
  private boolean addNode(List<Node> listNodes, Node node, List<Row> listRows, Row r) throws Exception {
    List<Node> checkList = new ArrayList<Node>();
    if (flag_) checkList = currentListNodes_; 
    else checkList = listNodes;
    if (node.getName().equals(Utils.JCR_CONTENT)) {
      Node parent = node.getParent();
      if (!checkList.contains(parent) && !parent.getPath().startsWith(JCR_SYSTEM_PATH)) {
        listNodes.add(parent);
        listRows.add(r);
        return true;
      }
    } else if (!checkList.contains(node) && !node.getPath().startsWith(JCR_SYSTEM_PATH)) {
      listNodes.add(node);
      listRows.add(r);
      return true;
    }
    return false;
  }
  
  public Session getSession() throws Exception {
    return getAncestorOfType(UIJCRExplorer.class).getTargetSession();
  }
    
  public Date getDateCreated(Node node) throws Exception{
    if (node.hasProperty("exo:dateCreated")) {
      return node.getProperty("exo:dateCreated").getDate().getTime();
    }
    return new GregorianCalendar().getTime();
  }
  
  public Node getNodeByPath(String path) throws Exception {
    try {        
      JCRPath nodePath = ((SessionImpl)getSession()).getLocationFactory().parseJCRPath(path);
      return (Node)getSession().getItem(nodePath.getAsString(false));
    } catch (Exception e) {
      return null;
    }
  }
  
  public List<Row> getResultList() throws Exception {
	    TaxonomyService taxonomyService = getApplicationComponent(TaxonomyService.class);
	    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class);
	    String rootTreePath = nodeHierarchyCreator.getJcrPath(BasePath.TAXONOMIES_TREE_STORAGE_PATH);
	    List<Node> listNodes = new ArrayList<Node>();
	    List<Row> listRows = new ArrayList<Row>();
	    if (resultIter == null) return new ArrayList<Row>();
	    Row r = null;
	    int count = 0;
	    Node resultNode = null;
	    while (resultIter.hasNext()) {    		
	      r = resultIter.nextRow();      
	      String path = r.getValue("jcr:path").getString();
	      try {
	        resultNode = getNodeByPath(path);
	      } catch (Exception e) {
	        LOG.warn("Can't get node by path " + path, e);
	        continue;
	      }
	      if (resultNode != null) {
	      	if ((categoryPathList != null) && (categoryPathList.size() > 0)){
	          for (String categoryPath : categoryPathList) {
	            int index = categoryPath.indexOf("/");
	            List<String> pathCategoriesList = new ArrayList<String>();
	            String searchCategory = rootTreePath + "/" + categoryPath;
	            List<Node> listCategories = taxonomyService.getCategories(resultNode, categoryPath.substring(0, index));
	            for (Node category : listCategories) {
	              pathCategoriesList.add(category.getPath());
	            }
	            if (pathCategoriesList.contains(searchCategory)) {
	            	if (resultNode.isNodeType(Utils.EXO_SYMLINK))
	            		if (checkTargetMatch(resultNode, keyword)) {
	            			if (addNode(listNodes, resultNode, listRows, r) )	count ++;
	            		}
	            	}else {
	            		if (addNode(listNodes, resultNode, listRows, r) )	count ++;
	            	}
	            }//for
          }else {
	        	if (resultNode.isNodeType(Utils.EXO_SYMLINK)) {
	        		if (checkTargetMatch(resultNode, keyword)) {
          			if (addNode(listNodes, resultNode, listRows, r) )	count ++;
          		}
	        	}else {
	        		if (addNode(listNodes, resultNode, listRows, r) )	count ++;
	        	}
	        }
	      } 
	      if (count == 100 ) break;
	    }
	    currentListNodes_.addAll(listNodes);
	    currentListRows_.addAll(listRows);
	    if (!resultIter.hasNext()) isEndOfIterator_ = true;
	    if (listNodes.size() < 100 && !isEndOfIterator_) {
	      flag_ = true;
	    }
	    return currentListRows_;
	  }
  
  public void clearAll() {
		flag_ = false;
		isEndOfIterator_ = false;
		currentListNodes_.clear();
		currentListRows_.clear();
		resultIter = null;		
		checkSymlinkSession= null;
		keyword = "";
  }
  public void setSearchKeyword4LinkCheck(String keyword) {
  	this.keyword = keyword;
  }
  /**
   * Check a symlink/taxonomylink if its target matches with keyword for searching ...link
   * @param linkPath
   * @param keyword
   * @return
   * @Author Nguyen The Vinh from ExoPlatform
   */
  protected boolean checkTargetMatch(Node symlinkNode, String keyword) {
  	String queryStatement = CHECK_LINK_MATCH_QUERY1;
  	Node target=null;
  	
  	if (linkManager==null) {
  		linkManager = getApplicationComponent(LinkManager.class);
  	}
  	try {
  		if (!linkManager.isLink(symlinkNode)) return true;
  		target = linkManager.getTarget(symlinkNode);
  		if (target == null) return false;
  		checkSymlinkSession = target.getSession();
  		queryStatement = StringUtils.replace(queryStatement,"$0", target.getPath());  		
  		queryStatement = StringUtils.replace(queryStatement,"$1", keyword.replaceAll("'", "''"));
    	queryStatement = StringUtils.replace(queryStatement,"$2", keyword.replaceAll("'", "''").toLowerCase());
    	
  		
			QueryManager queryManager = checkSymlinkSession.getWorkspace().getQueryManager();
			Query query = queryManager.createQuery(queryStatement, Query.SQL);
			QueryResult queryResult = query.execute();
			if ( queryResult.getNodes().getSize()>0 ) return true;
			if (!(target.isNodeType("exo:webContent")||target.hasNode("jcr:content")) )return false;
			queryStatement = CHECK_LINK_MATCH_QUERY2;
			queryStatement = StringUtils.replace(queryStatement,"$0", target.getPath());  		
  		queryStatement = StringUtils.replace(queryStatement,"$1", keyword.replaceAll("'", "''"));
    	queryStatement = StringUtils.replace(queryStatement,"$2", keyword.replaceAll("'", "''").toLowerCase());
    	query = queryManager.createQuery(queryStatement, Query.SQL);
    	queryResult = query.execute();
			return queryResult.getNodes().getSize()>0;
		} catch (RepositoryException e) {
			return false;
		}
  }
  public UIQueryResultPageIterator getUIPageIterator() { return uiPageIterator_; }

  public void updateGrid(boolean flagCheck) throws Exception {
    SearchResultPageList pageList;
    pageList = new SearchResultPageList(queryResult_, getResultList(), PAGE_SIZE, isEndOfIterator_);
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
        LOG.error("Cannot compare rows", e);
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
      Node node;
      try {
        node = uiExplorer.getNodeByPath(path, uiExplorer.getTargetSession());
      } catch(AccessDeniedException ace) {
        uiApp.addMessage(new ApplicationMessage("UISearchResult.msg.access-denied", null, 
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      //check if node is viewable
      TemplateService templateService = uiSearchResult.getApplicationComponent(TemplateService.class);
      if (!templateService.isManagedNodeType(node.getPrimaryNodeType().getName(), repository)) {
        uiApp.addMessage(new ApplicationMessage("UISearchResult.msg.not-support", null, 
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      //check if node can be display
      if (!uiExplorer.canDisplayInJCRExplorer(node)) {
        uiApp.addMessage(new ApplicationMessage("UISearchResult.msg.not-display", null, 
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }

      UIPopupWindow uiPopup = uiExplorer.getChildById("ViewSearch");
      if (uiPopup == null) {
        uiPopup = uiExplorer.addChild(UIPopupWindow.class, null, "ViewSearch");
      }
      uiPopup.setResizable(true);
      UIViewSearchResult uiViewSearch = uiPopup.createUIComponent(UIViewSearchResult.class, null, null);
      uiViewSearch.setNode(node);

      uiPopup.setWindowSize(600,750);
      uiPopup.setUIComponent(uiViewSearch);
      uiPopup.setRendered(true);
      uiPopup.setShow(true);
      return;
    }
  }

  static public class OpenFolderActionListener extends EventListener<UISearchResult> {
    public void execute(Event<UISearchResult> event) throws Exception {
      UISearchResult uiSearchResult = event.getSource();
      UIJCRExplorer uiExplorer = uiSearchResult.getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = uiSearchResult.getAncestorOfType(UIApplication.class);
      String path = event.getRequestContext().getRequestParameter(OBJECTID);
      String folderPath = LinkUtils.getParentPath(path);
      Node node = null;
      try {
        node = uiExplorer.getNodeByPath(folderPath, uiExplorer.getTargetSession());
      } catch(AccessDeniedException ace) {
        uiApp.addMessage(new ApplicationMessage("UISearchResult.msg.access-denied", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch(Exception e) {
        LOG.error("Cannot access the node at " + folderPath, e);        
      }
      if (!uiExplorer.canDisplayInJCRExplorer(node)) {
        uiApp.addMessage(new ApplicationMessage("UISearchResult.msg.not-display-parent", null, 
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      uiExplorer.setSelectNode(node.getSession().getWorkspace().getName(), folderPath);
      uiExplorer.updateAjax(event);
    }
  }
  
  static public class SortASCActionListener extends EventListener<UISearchResult> {
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
  
  static public class SortDESCActionListener extends EventListener<UISearchResult> {
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
