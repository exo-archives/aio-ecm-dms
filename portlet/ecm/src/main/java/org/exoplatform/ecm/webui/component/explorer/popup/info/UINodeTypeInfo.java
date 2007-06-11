/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.info;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.OnParentVersionAction;

import org.exoplatform.ecm.jcr.JCRExceptionManager;
import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.jcr.core.ExtendedPropertyType;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * July 3, 2006
 * 10:07:15 AM
 * Editor : phan tuan Oct 27, 2006
 */

@ComponentConfig(
    template = "app:/groovy/webui/component/explorer/popup/info/UINodeTypeInfo.gtmpl",
    events = {@EventConfig(listeners = UINodeTypeInfo.CloseActionListener.class)}
)

public class UINodeTypeInfo extends UIContainer implements UIPopupComponent {
  private Collection nodeTypes ;

  public UINodeTypeInfo() throws Exception {}
  
  public void activate() throws Exception {
    UIJCRExplorer uiJCRExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    Node node = uiJCRExplorer.getCurrentNode() ;
    try {
      NodeType nodetype = node.getPrimaryNodeType() ;
      Collection<NodeType> types = new ArrayList<NodeType>() ;
      types.add(nodetype) ;
      NodeType[] mixins = node.getMixinNodeTypes() ;
      if (mixins != null) {
        List<NodeType> list = Arrays.asList(mixins) ;
        types.addAll(list) ;
      }
      nodeTypes = types ;
    } catch (Exception e) {
      UIApplication uiApp = uiJCRExplorer.getAncestorOfType(UIApplication.class) ;
      JCRExceptionManager.process(uiApp, e);
    }
  }
  
  public void deActivate() throws Exception {}
  
  public String[] getActions() {return new String[] {"Close"} ;}
  
  public String resolveType(int type) {
    return ExtendedPropertyType.nameFromValue(type) ;
  }
  
  public String resolveOnParentVersion(int opv) {
    return OnParentVersionAction.nameFromValue(opv) ;
  }

  public Collection getNodeTypes() { return nodeTypes ;}
  
  static  public class CloseActionListener extends EventListener<UINodeTypeInfo> {
    public void execute(Event<UINodeTypeInfo> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;
    }
  }
}

