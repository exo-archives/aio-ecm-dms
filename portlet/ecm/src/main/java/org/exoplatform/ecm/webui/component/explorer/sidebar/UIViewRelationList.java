/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.sidebar ;

import java.util.ArrayList;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import org.exoplatform.ecm.jcr.JCRExceptionManager;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * September 19, 2006
 */
@ComponentConfig(
    template =  "app:/groovy/webui/component/explorer/sidebar/UIViewRelationList.gtmpl",
    events = {@EventConfig(listeners = UIViewRelationList.ChangeNodeActionListener.class)}
)

public class UIViewRelationList extends UIContainer{

  public UIViewRelationList() throws Exception { }

  public List<Node> getRelations() throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    Session session = uiExplorer.getSession() ;
    List<Node> relations = new ArrayList<Node>() ;
    Value[] vals = null ;
    try {
      vals = uiExplorer.getCurrentNode().getProperty("exo:relation").getValues() ;    
    }catch (Exception e) { return relations ;}
    for(Value val : vals) {
      String uuid = val.getString();
      Node node = session.getNodeByUUID(uuid) ;
      relations.add(node) ;
    }
    return relations ;
  }
  
  public boolean isPreferenceNode(Node node) throws RepositoryException {
    return getAncestorOfType(UIJCRExplorer.class).isPreferenceNode(node) ;
  }

  static public class ChangeNodeActionListener extends EventListener<UIViewRelationList> {
    public void execute(Event<UIViewRelationList> event) throws Exception {
      UIViewRelationList uicomp =  event.getSource() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ; 
      String uri = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String workspaceName = event.getRequestContext().getRequestParameter("workspaceName") ;
      Session session = uiExplorer.getSessionByWorkspace(workspaceName);
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class) ;
      String prefPath = uiExplorer.getPreferencesPath() ;
      if((!prefPath.equals("")) && (uiExplorer.getCurrentWorkspace().equals(workspaceName))) {
        if(!uri.contains(prefPath)) {         
          JCRExceptionManager.process(uiApp,new PathNotFoundException());
          return ;
        }
        if ((".." + prefPath).equals(uri)) {
          if (prefPath.equals(uiExplorer.getCurrentNode().getPath())) {
            uiExplorer.setSelectNode(uiExplorer.getCurrentNode().getParent());
            uiExplorer.updateAjax(event) ;
          }
          return ;
        }
        uiExplorer.setSelectNode(uri, session);
        uiExplorer.updateAjax(event) ;
        return ;
      } 
      if ("../".equals(uri)) {
        if (!"/".equals(uiExplorer.getCurrentNode().getPath())) {
          uiExplorer.setSelectNode(uiExplorer.getCurrentNode().getParent());
          uiExplorer.updateAjax(event) ;
        }
        return ;
      }
      uiExplorer.setSelectNode(uri, session);
      uiExplorer.updateAjax(event) ;
    }
  }
}
