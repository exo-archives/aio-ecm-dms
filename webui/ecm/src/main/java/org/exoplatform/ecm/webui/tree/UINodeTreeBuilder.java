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
package org.exoplatform.ecm.webui.tree;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 23, 2008  
 */

@ComponentConfig(
    events = @EventConfig(listeners = UINodeTreeBuilder.ChangeNodeActionListener.class)
)
public class UINodeTreeBuilder extends UIContainer {

  private List<String> acceptedNodeTypes = new ArrayList<String>();
  protected Node rootTreeNode;
  protected Node currentNode;


  public UINodeTreeBuilder() throws Exception {
    UITree tree = addChild(UINodeTree.class, null, UINodeTree.class.getSimpleName()+hashCode()) ;
    tree.setBeanLabelField("name") ;
    tree.setBeanIdField("path") ;    
  }  

  public Node getRootTreeNode() { return rootTreeNode; }
  public final void setRootTreeNode(Node node) throws Exception {
    this.rootTreeNode = node;
    this.currentNode = node;
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    broadcastOnChange(node,requestContext);
  }

  public Node getCurrentNode() { return currentNode; }
  public void setCurrentNode(Node currentNode) { this.currentNode = currentNode; }  

  public List<String> getAcceptedNodeTypes() { return acceptedNodeTypes; }
  public void setAcceptedNodeTypes(List<String> acceptedNodeTypes) {
    this.acceptedNodeTypes = acceptedNodeTypes;
  }

  public void buildTree() throws Exception {    
    NodeIterator sibbling = null ;
    NodeIterator children = null ;    
    UINodeTree tree = getChild(UINodeTree.class) ;
    tree.setSelected(currentNode);    
    if(currentNode.getDepth() > 0) {
      tree.setParentSelected(currentNode.getParent()) ;
      sibbling = currentNode.getParent().getNodes() ;
      children = currentNode.getNodes() ;
    } else {
      tree.setParentSelected(currentNode) ;
      sibbling = currentNode.getNodes() ;
      children = null;
    }
    if(sibbling != null) {
      tree.setSibbling(filfer(sibbling));
    }
    if(children != null) {
      tree.setChildren(filfer(children));
    }
  }

  private List<Node> filfer(final NodeIterator iterator) throws Exception{
    List<Node> list = new ArrayList<Node>();
    if(acceptedNodeTypes.size()>0) {
      for(;iterator.hasNext();) {
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
    for(;iterator.hasNext();) {
      Node sibbling = iterator.nextNode();
      if(sibbling.isNodeType("exo:hiddenable")) continue;            
      list.add(sibbling);                  
    }            
    return list;
  }
  public void processRender(WebuiRequestContext context) throws Exception {
    Writer writer = context.getWriter() ;
    writer.write("<div class=\"Explorer\">") ;
    writer.write("<div class=\"ExplorerTree\">") ;
    writer.write("<div class=\"InnerExplorerTree\">") ;
    buildTree() ;
    super.renderChildren() ;
    writer.write("</div>") ;
    writer.write("</div>") ;
    writer.write("</div>") ;
  }

  public final void changeNode(String path, WebuiRequestContext requestContext) throws Exception {
    String rootPath = rootTreeNode.getPath();
    if(rootPath.equals(path) || !path.startsWith(rootPath)) {
      currentNode = rootTreeNode;
    }else {
      currentNode = (Node)rootTreeNode.getSession().getItem(path);
    }    
    broadcastOnChange(currentNode,requestContext);
  }

  public final void broadcastOnChange(Node node, WebuiRequestContext requestContext) throws Exception {
    UIBaseNodeTreeSelector nodeTreeSelector = getAncestorOfType(UIBaseNodeTreeSelector.class);
    nodeTreeSelector.onChange(node, requestContext);
  }

  static public class ChangeNodeActionListener extends EventListener<UITree> {
    public void execute(Event<UITree> event) throws Exception {
      UINodeTreeBuilder builder = event.getSource().getParent();
      String uri = event.getRequestContext().getRequestParameter(OBJECTID)  ;
      builder.changeNode(uri,event.getRequestContext());
      UIBaseNodeTreeSelector nodeTreeSelector = builder.getAncestorOfType(UIBaseNodeTreeSelector.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(nodeTreeSelector);
    }
  }  
}
