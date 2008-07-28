/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.ecm.webui.tree.selectone;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 23, 2008  
 */

@ComponentConfig(
    template =  "classpath:groovy/ecm/webui/tree/UITreeList.gtmpl",
    events = {
        @EventConfig(listeners = UISelectPathPanel.SelectActionListener.class)
    }
)
public class UISelectPathPanel extends UIContainer {

  private Node parentNode;
  private List<String> acceptedNodeTypes = new ArrayList<String>();

  public UISelectPathPanel() { }
  
  public void setParentNode(Node node) { this.parentNode = node; }
  
  public List<String> getAcceptedNodeTypes() { return acceptedNodeTypes; }

  public void setAcceptedNodeTypes(List<String> acceptedNodeTypes) { 
    this.acceptedNodeTypes = acceptedNodeTypes;
  }
  
  public List<Node> getNodeList() throws Exception {
    List<Node> list = new ArrayList<Node>();
    for(NodeIterator iterator = parentNode.getNodes();iterator.hasNext();) {
      Node sibbling = iterator.nextNode();
      if(sibbling.isNodeType("exo:hiddenable")) continue;
      for(String nodetype: acceptedNodeTypes) {
        if(sibbling.isNodeType(nodetype)) {
          list.add(sibbling);
          break;
        }
      }      
    }
    return list;
  }      

  static public class SelectActionListener extends EventListener<UISelectPathPanel> {
    public void execute(Event<UISelectPathPanel> event) throws Exception {
      UISelectPathPanel uiDefault = event.getSource() ;
      String value = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIOneNodePathSelector uiJCRBrowser = uiDefault.getParent() ;
      String returnField = uiJCRBrowser.getReturnFieldName() ;
//    if(!uiJCRBrowser.isDisable()) 
//    value = uiJCRBrowser.getWorkspace() + ":" + value ;
      ((UISelectable)uiJCRBrowser.getReturnComponent()).doSelect(returnField, value) ;
    }
  }  
}
