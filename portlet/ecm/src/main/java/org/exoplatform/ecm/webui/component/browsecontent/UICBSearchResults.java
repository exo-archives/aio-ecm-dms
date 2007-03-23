/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.webui.component.UIGrid;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
        @EventConfig(listeners = UICBSearchResults.ViewActionListener.class)
    }
)
public class UICBSearchResults extends UIGrid {
  private static String[] GRID_FIELD = {"name", "path"} ;
  private static String[] GRID_ACTIONS = {"View"} ;
  public UICBSearchResults() throws Exception { 
    getUIPageIterator().setId("ResultListIterator") ;
    configure("path", GRID_FIELD, GRID_ACTIONS) ;
  }

  static public class CloseActionListener extends EventListener<UICBSearchResults> {
    public void execute(Event<UICBSearchResults> event) throws Exception {
      UICBSearchResults uiResults = event.getSource() ;
      UISearchController uiSearchController = uiResults.getAncestorOfType(UISearchController.class) ;
      uiSearchController.setShowHiddenSearch() ;
    }
  }

  static public class ViewActionListener extends EventListener<UICBSearchResults> {
    public void execute(Event<UICBSearchResults> event) throws Exception {
      UICBSearchResults uiResults = event.getSource() ;
      String itemPath = event.getRequestContext().getRequestParameter(OBJECTID);
      UIBrowseContainer container = uiResults.getAncestorOfType(UIBrowseContainer.class) ;
      Node node = container.getNodeByPath(itemPath) ;
      UISearchController uiSearchController = uiResults.getAncestorOfType(UISearchController.class) ;
      uiSearchController.setShowHiddenSearch() ;  
      UICBSearchForm uiForm = uiSearchController.getChild(UICBSearchForm.class) ;      
      if(uiForm.isDocumentType) {
        container.viewDocument(node, true, true) ;
        return ;
      } 
      if(container.getPortletPreferences().getValue(Utils.CB_TEMPLATE, "").equals("TreeList")) {
        container.selectNode(node) ;
        return ;
      }
      container.changeNode(node) ;
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
