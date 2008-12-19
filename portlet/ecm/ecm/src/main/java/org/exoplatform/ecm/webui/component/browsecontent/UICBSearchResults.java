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
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
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
    template = "app:/groovy/webui/component/browse/UICBSearchResults.gtmpl",
    events = {
        @EventConfig(listeners = UICBSearchResults.CloseActionListener.class),
        @EventConfig(listeners = UICBSearchResults.ViewActionListener.class),
        @EventConfig(listeners = UICBSearchResults.GotoActionListener.class)
    }
)
public class UICBSearchResults extends UIContainer {
  protected Map<String, Node> resultMap_ = new HashMap<String, Node>() ;
  private UIPageIterator uiPageIterator_ ;

  public UICBSearchResults() throws Exception { 
    uiPageIterator_ = addChild(UIPageIterator.class, null, null) ;
  }
  
  public List getCurrentList() throws Exception { 
    return uiPageIterator_.getCurrentPageData() ;    
  }
  
  public UIPageIterator getUIPageIterator() { return uiPageIterator_ ; }
  
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
  protected void getResultData() throws Exception {
    List<ResultData> results = new ArrayList<ResultData>() ;
    for(String nodeName : resultMap_.keySet()) {
      results.add(new ResultData(Utils.formatNodeName(nodeName), Utils.formatNodeName(resultMap_.get(nodeName).getPath()))) ;
    }
  }
  static public class ViewActionListener extends EventListener<UICBSearchResults> {
    public void execute(Event<UICBSearchResults> event) throws Exception {
      UICBSearchResults uiResults = event.getSource() ;
      String itemPath = event.getRequestContext().getRequestParameter(OBJECTID);
      UIBrowseContainer container = uiResults.getAncestorOfType(UIBrowseContainer.class) ;
      Node node = container.getNodeByPath(itemPath) ;
      UIApplication uiApp = uiResults.getAncestorOfType(UIApplication.class) ;
      if(node == null) {
        uiApp.addMessage(new ApplicationMessage("UICBSearchResults.msg.node-removed", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return ;
      }
      NodeType nodeType = node.getPrimaryNodeType() ;
      UISearchController uiSearchController = uiResults.getAncestorOfType(UISearchController.class) ;
      if(uiResults.isDocumentTemplate(nodeType.getName())) {
        UIBrowseContentPortlet cbPortlet = uiResults.getAncestorOfType(UIBrowseContentPortlet.class) ;
        UIPopupAction uiPopupAction = cbPortlet.getChildById("UICBPopupAction") ;
        UIDocumentDetail uiDocument =  uiPopupAction.activate(UIDocumentDetail.class, 600) ;// cbPortlet.createUIComponent(UIDocumentDetail.class, null, null) ;
        uiDocument.setNode(node) ;
        UIPopupWindow uiPopup  = uiPopupAction.getChildById("UICBPopupWindow") ;
        uiPopup.setResizable(true) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        return ;
      }
      if(container.isCategories(nodeType)) {
        uiSearchController.setShowHiddenSearch() ;
        if(container.getPortletPreferences().getValue(Utils.CB_TEMPLATE, "").equals("TreeList")) {
          container.getChild(UICategoryTree.class).buildTree(itemPath) ;
          container.setCurrentNodePath(itemPath) ;
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
      UIApplication uiApp = uiResults.getAncestorOfType(UIApplication.class) ;
      if(node == null) {
        uiApp.addMessage(new ApplicationMessage("UICBSearchResults.msg.node-removed", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return ;
      }
      Node parentNode = null ;
      if(node.getPath().equals(container.getRootNode().getPath())) parentNode = node ;
      else parentNode = node.getParent() ;
      NodeType nodeType = parentNode.getPrimaryNodeType() ;
      UISearchController uiSearchController = uiResults.getAncestorOfType(UISearchController.class) ;
      if(container.isCategories(nodeType)) {
        uiSearchController.setShowHiddenSearch() ;
        if(container.getPortletPreferences().getValue(Utils.CB_TEMPLATE, "").equals("TreeList")) {
          container.getChild(UICategoryTree.class).buildTree(parentNode.getPath()) ;
          container.setCurrentNodePath(parentNode.getPath()) ;
        }
        container.changeNode(parentNode) ;
        return ;
      }
      if(uiResults.isDocumentTemplate(parentNode.getPrimaryNodeType().getName())) {
        UIBrowseContentPortlet cbPortlet = uiResults.getAncestorOfType(UIBrowseContentPortlet.class) ;
        UIPopupAction uiPopupAction = cbPortlet.getChildById("UICBPopupAction") ;
        UIDocumentDetail uiDocument =  uiPopupAction.activate(UIDocumentDetail.class, 600) ;// cbPortlet.createUIComponent(UIDocumentDetail.class, null, null) ;
        uiDocument.setNode(parentNode) ;
        UIPopupWindow uiPopup  = uiPopupAction.getChildById("UICBPopupWindow") ;
        uiPopup.setResizable(true) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        return ;
      }
    }
  }
  
  public String[] getActions() { return new String[] {"Close"} ;}

  public void updateGrid(List<ResultData> result) throws Exception {
    ObjectPageList objPageList = new ObjectPageList(result, 10) ;
    uiPageIterator_.setPageList(objPageList) ;
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
