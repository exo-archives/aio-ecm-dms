/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.info ;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * September 19, 2006
 */
@ComponentConfig(
    template =  "app:/groovy/webui/component/explorer/UIViewRelationList.gtmpl",
    events = {@EventConfig(listeners = UIViewRelationList.CloseActionListener.class)}
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
  
  @SuppressWarnings("unused")
  static public class CloseActionListener extends EventListener<UIViewRelationList> {
    public void execute(Event<UIViewRelationList> event) throws Exception {
    }
  }
}
