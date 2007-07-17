/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Dec 26, 2006 11:39:54 AM 
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/UIGridWithButton.gtmpl",
    events = {
        @EventConfig(listeners = UICBSearchResults.CloseActionListener.class),
        @EventConfig(listeners = UICBSearchResults.ViewActionListener.class),
        @EventConfig(listeners = UICBSearchResults.GotoActionListener.class)
    }
)
public class UICBSearchResults extends UIGrid {
  private static String[] GRID_FIELD = {"name", "path"} ;
  private static String[] GRID_ACTIONS = {"View", "Goto"} ;
  protected Map<String, Node> resultMap_ = new HashMap<String, Node>() ;
  
  public UICBSearchResults() throws Exception { 
    getUIPageIterator().setId("ResultListIterator") ;
    configure("path", GRID_FIELD, GRID_ACTIONS) ;
  }
  private boolean isDocumentTemplate(String nodeType)throws Exception {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String repository = getAncestorOfType(UIBrowseContentPortlet.class).getPreferenceRepository() ;
    return templateService.getDocumentTemplates(repository).contains(nodeType) ;
  }
  static public class CloseActionListener extends EventListener<UICBSearchResults> {
    public void execute(Event<UICBSearchResults> event) throws Exception {
      UICBSearchResults uiResults = event.getSource() ;
      UISearchController uiSearchController = uiResults.getAncestorOfType(UISearchController.class) ;
      uiSearchController.setShowHiddenSearch() ;
    }
  }
  private void getResultData() throws Exception {
    List<ResultData> results = new ArrayList<ResultData>() ;
    for(String nodeName : resultMap_.keySet()) {
      results.add(new ResultData(nodeName, resultMap_.get(nodeName).getPath())) ;
    }
  }
  static public class ViewActionListener extends EventListener<UICBSearchResults> {
    public void execute(Event<UICBSearchResults> event) throws Exception {
      UICBSearchResults uiResults = event.getSource() ;
      String itemPath = event.getRequestContext().getRequestParameter(OBJECTID);
      UIBrowseContainer container = uiResults.getAncestorOfType(UIBrowseContainer.class) ;
      Node node = container.getNodeByPath(itemPath) ;
      NodeType nodeType = node.getPrimaryNodeType() ;
      UISearchController uiSearchController = uiResults.getAncestorOfType(UISearchController.class) ;
      if(uiResults.isDocumentTemplate(nodeType.getName())) {
        UIBrowseContentPortlet cbPortlet = uiResults.getAncestorOfType(UIBrowseContentPortlet.class) ;
        UIPopupAction uiPopupAction = cbPortlet.getChildById("UICBPopupAction") ;
        UIDocumentDetail uiDocument = cbPortlet.createUIComponent(UIDocumentDetail.class, null, null) ;
        uiDocument.setNode(node) ;
        uiPopupAction.activate(uiDocument, 600, 0) ;
        uiPopupAction.getChild(UIPopupWindow.class).setResizable(true) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        return ;
      }
      if(container.isCategories(nodeType)) {
        uiSearchController.setShowHiddenSearch() ;
        if(container.getPortletPreferences().getValue(Utils.CB_TEMPLATE, "").equals("TreeList")) {
          container.setCurrentNode(node) ;
          return ;
        }
        container.changeNode(node) ;
        return ;
      }
    }
  }
  static public class GotoActionListener extends EventListener<UICBSearchResults> {
    public void execute(Event<UICBSearchResults> event) throws Exception {
      UICBSearchResults uiResults = event.getSource() ;
      String itemPath = event.getRequestContext().getRequestParameter(OBJECTID);
      UIBrowseContainer container = uiResults.getAncestorOfType(UIBrowseContainer.class) ;
      Node node = container.getNodeByPath(itemPath) ;      
      Node parentNode = node.getParent() ;
      NodeType nodeType = parentNode.getPrimaryNodeType() ;
      UISearchController uiSearchController = uiResults.getAncestorOfType(UISearchController.class) ;
      if(container.isCategories(nodeType)) {
        uiSearchController.setShowHiddenSearch() ;
        if(container.getPortletPreferences().getValue(Utils.CB_TEMPLATE, "").equals("TreeList")) {
          container.setCurrentNode(parentNode) ;
          return ;
        }
        container.changeNode(parentNode) ;
        return ;
      }
    }
  }
  public String[] getActions() { return new String[] {"Close"} ;}

  public void updateGrid(List<ResultData> result) throws Exception {
    ObjectPageList objPageList = new ObjectPageList(result, 10) ;
    getUIPageIterator().setPageList(objPageList) ;
  } 

  public static class ResultData {
    private String name ;
    private String path ;

    public ResultData(String rName, String rpath) {
      name = rName ;
      path = rpath ;
    }
    
    public String getName() { return name ; }
    public String getPath() { return path ; }
  }
}
