/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.admin;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.jcr.JCRExceptionManager;
import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.cms.relations.RelationsService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 18, 2006
 * 2:28:18 PM 
 */
@ComponentConfig(
    lifecycle = UIContainerLifecycle.class,
    events = @EventConfig(listeners = UIRelationsAddedList.DeleteActionListener.class)
)
public class UIRelationsAddedList extends UIContainer implements UISelector {

  private static String[] RELATE_BEAN_FIELD = {"path"} ;
  private static String[] ACTION = {"Delete"} ;

  public UIRelationsAddedList() throws Exception {
    UIGrid uiGrid = addChild(UIGrid.class, null, "RelateAddedList") ;
    uiGrid.getUIPageIterator().setId("RelateListIterator");
    uiGrid.configure("path", RELATE_BEAN_FIELD, ACTION) ;
  }
  
  public void updateGrid (List<Node> nodes) throws Exception {
    UIGrid uiGrid = getChildById("RelateAddedList") ;   
    if(nodes == null) nodes = new ArrayList<Node>() ;
    ObjectPageList objPageList = new ObjectPageList(nodes, 10) ;
    uiGrid.getUIPageIterator().setPageList(objPageList) ;
  }
  
  @SuppressWarnings("unused")
  public void updateSelect(String selectField, String value) {
    UIJCRExplorer uiJCRExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    RelationsService relateService = getApplicationComponent(RelationsService.class) ;
    try {
      relateService.addRelation(uiJCRExplorer.getCurrentNode(), value, uiJCRExplorer.getSession()) ;
      updateGrid(relateService.getRelations(uiJCRExplorer.getCurrentNode(), uiJCRExplorer.getSession())) ;
      setRenderSibbling(UIRelationsAddedList.class) ;
    } catch(Exception e) {
      e.printStackTrace() ;
    }
  }

  static public class DeleteActionListener extends EventListener<UIRelationsAddedList> {
    public void execute(Event<UIRelationsAddedList> event) throws Exception {
      UIRelationsAddedList uiAddedList = event.getSource() ;
      UIRelationManager uiManager = uiAddedList.getParent() ;
      UIApplication uiApp = uiAddedList.getAncestorOfType(UIApplication.class) ;
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      RelationsService relationService = 
        uiAddedList.getApplicationComponent(RelationsService.class) ;
      UIJCRExplorer uiExplorer = uiAddedList.getAncestorOfType(UIJCRExplorer.class) ;
      try {
        relationService.removeRelation(uiExplorer.getCurrentNode(), nodePath, uiExplorer.getSession()) ;
        uiAddedList.updateGrid(relationService.getRelations(uiExplorer.getCurrentNode(),uiExplorer.getSession())) ;
      } catch(Exception e) {
        JCRExceptionManager.process(uiApp, e) ;
      }
      uiManager.setRenderedChild("UIRelationsAddedList") ;
    }
  }  
}
