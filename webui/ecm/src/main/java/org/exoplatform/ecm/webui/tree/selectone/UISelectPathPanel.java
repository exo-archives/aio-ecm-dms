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

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 23, 2008  
 */

@ComponentConfig(
    template =  "classpath:groovy/ecm/webui/tree/selectone/UISelectPathPanel.gtmpl",
    events = {
        @EventConfig(listeners = UISelectPathPanel.SelectActionListener.class)
    }
)
public class UISelectPathPanel extends UIContainer {
  private UIPageIterator uiPageIterator_;
  public String[] acceptedMimeTypes = {};
  protected Node parentNode;
  private String[] acceptedNodeTypes = {};


  public UISelectPathPanel() throws Exception { 
    uiPageIterator_ = addChild(UIPageIterator.class, null, "UISelectPathIterate");
  }
  
  public UIPageIterator getUIPageIterator() { return uiPageIterator_; }

  public void setParentNode(Node node) { this.parentNode = node; }
  
  public Node getParentNode() { return parentNode; }

  public String[] getAcceptedNodeTypes() { return acceptedNodeTypes; }

  public void setAcceptedNodeTypes(String[] acceptedNodeTypes) { 
    this.acceptedNodeTypes = acceptedNodeTypes;
  }

  public String[] getAcceptedMimeTypes() { return acceptedMimeTypes; }
  public void setAcceptedMimeTypes(String[] acceptedMimeTypes) { this.acceptedMimeTypes = acceptedMimeTypes; }  

  public List getSelectableNodes() throws Exception { return uiPageIterator_.getCurrentPageData(); }
  
  public void updateGrid() throws Exception {
    ObjectPageList objPageList = new ObjectPageList(getListSelectableNodes(), 10);
    uiPageIterator_.setPageList(objPageList);
  }
  
  public List<Node> getListSelectableNodes() throws Exception {
    List<Node> list = new ArrayList<Node>();
    if(parentNode == null) return list;
    for(NodeIterator iterator = parentNode.getNodes();iterator.hasNext();) {
      Node child = iterator.nextNode();
      if(child.isNodeType("exo:hiddenable")) continue;
      if(matchMimeType(child) && matchNodeType(child)) {
        list.add(child);
      }
    }
    return list;
  }      

  protected boolean matchNodeType(Node node) throws Exception {
    if(acceptedNodeTypes == null || acceptedNodeTypes.length == 0) return true;
    for(String nodeType: acceptedNodeTypes) {
      if(node.isNodeType(nodeType)) 
        return true;
    }
    return false;
  }

  protected boolean matchMimeType(Node node) throws Exception {
    if(acceptedMimeTypes == null || acceptedMimeTypes.length == 0) return true;
    if(!node.isNodeType("nt:file")) return true;
    String mimeType = node.getNode("jcr:content").getProperty("jcr:mimeType").getString();
    for(String type: acceptedMimeTypes) {
      if(type.equalsIgnoreCase(mimeType))
        return true;
    }
    return false;
  }

  static public class SelectActionListener extends EventListener<UISelectPathPanel> {
    public void execute(Event<UISelectPathPanel> event) throws Exception {
      UISelectPathPanel uiDefault = event.getSource() ;
      String value = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIContainer uiTreeSelector = uiDefault.getParent();
      if(uiTreeSelector instanceof UIOneNodePathSelector) {
        if(!((UIOneNodePathSelector)uiTreeSelector).isDisable()) {
          value = ((UIOneNodePathSelector)uiTreeSelector).getWorkspaceName() + ":" + value ;
        }
      } 
      String returnField = ((UIBaseNodeTreeSelector)uiTreeSelector).getReturnFieldName();
      ((UISelectable)((UIBaseNodeTreeSelector)uiTreeSelector).getSourceComponent()).doSelect(returnField, value) ;
      
      UIComponent uiOneNodePathSelector = uiDefault.getParent();
      if (uiOneNodePathSelector instanceof UIOneNodePathSelector) {
        UIComponent uiComponent = uiOneNodePathSelector.getParent();
        if (uiComponent instanceof UIPopupWindow) {
          ((UIPopupWindow)uiComponent).setShow(false);
          ((UIPopupWindow)uiComponent).setRendered(false);
        }
        UIComponent component = ((UIOneNodePathSelector)uiOneNodePathSelector).getSourceComponent().getParent();
        if (component != null) {
          event.getRequestContext().addUIComponentToUpdateByAjax(component);
        }
      }
    }
  }
}
