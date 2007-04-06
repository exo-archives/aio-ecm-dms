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
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIGrid;
import org.exoplatform.webui.component.lifecycle.UIContainerLifecycle;
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
    lifecycle = UIContainerLifecycle.class,
    events = { @EventConfig(listeners = UISearchResult.ViewActionListener.class) }
)
public class UISearchResult extends UIContainer {
  private static String[] RESULT_BEAN_FIELD = {"name", "shortcutPath"} ;
  private static String[] VIEW_ACTION = {"View"} ;
  public Map<String, Node> resultMap_ = new HashMap<String, Node>() ;
  
  public UISearchResult() throws Exception {
    UIGrid uiGrid = addChild(UIGrid.class, null, null) ;
    uiGrid.getUIPageIterator().setId("UISearchIterator") ;
    uiGrid.configure("path", RESULT_BEAN_FIELD, VIEW_ACTION) ;
  }
  
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
  
  public void updateGrid(Node[] arrNodes) throws Exception {
    List<ResultObject> results = new ArrayList<ResultObject>() ;    
    for(Node node : arrNodes) {
      ResultObject temp = new ResultObject(node.getName(), node.getPath()) ;
      results.add(temp) ;
    }
    ObjectPageList objPageList = new ObjectPageList(results, 10) ;
    getChild(UIGrid.class).getUIPageIterator().setPageList(objPageList) ;
  }
  
  static  public class ViewActionListener extends EventListener<UISearchResult> {
    public void execute(Event<UISearchResult> event) throws Exception {
      UISearchResult uiSearchResult = event.getSource() ;
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiSearchResult.getAncestorOfType(UIJCRExplorer.class).setSelectNode(path) ;
      uiSearchResult.getAncestorOfType(UIJCRExplorer.class).updateAjax(event) ;
    }
  }
  
  public static class ResultObject {
    private String name ;
    private String path ;
    private String shortcutPath ;
    
    public ResultObject(String s, String p){
      name = s ;
      path = p ;
      if(p.length() > 40) shortcutPath = p.substring(0, 30) + "..." ;
      else shortcutPath = p ;
    }
    
    public String getName () { return name ; }
    public String getPath () { return path ; }
    public String getShortcutPath () { return shortcutPath ; } 
  }  
}