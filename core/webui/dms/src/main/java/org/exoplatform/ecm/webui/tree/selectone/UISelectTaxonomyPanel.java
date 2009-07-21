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
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
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
        @EventConfig(listeners = UISelectTaxonomyPanel.SelectActionListener.class)
    }
)
public class UISelectTaxonomyPanel extends UIContainer {
  private UIPageIterator uiPageIterator_;
  public String[] acceptedMimeTypes = {};
  protected Node parentNode;
  private String[] acceptedNodeTypes = {};
  private String[] exceptedNodeTypes = {};
  private boolean allowPublish = false;
  private PublicationService publicationService_ = null;
  private List<String> templates_ = null;
  private String[] defaultExceptedNodeTypes = {};
  
  private static String TAXONOMY_TREE = "taxonomyTree";
  
  public UISelectTaxonomyPanel() throws Exception { 
    uiPageIterator_ = addChild(UIPageIterator.class, null, "UISelectPathIterate");
  }
  
  public UIPageIterator getUIPageIterator() { return uiPageIterator_; }
  
  public boolean isAllowPublish() {
    return allowPublish;
  }

  public void setAllowPublish(boolean allowPublish, PublicationService publicationService, List<String> templates) {
    this.allowPublish = allowPublish;
    publicationService_ = publicationService;
    templates_ = templates;
  }
  
  private void addNodePublish(List<Node> listNode, Node node, PublicationService publicationService) throws Exception {
    if (isAllowPublish()) {
      NodeType nt = node.getPrimaryNodeType();
      if (templates_.contains(nt.getName())) { 
        Node nodecheck = publicationService.getNodePublish(node, null);
        if (nodecheck != null) {
          listNode.add(nodecheck); 
        }
      } else {
        listNode.add(node);
      }
    } else {
      listNode.add(node);
    }
  }
  
  public void setParentNode(Node node) { this.parentNode = node; }
  
  public Node getParentNode() { return parentNode; }

  public String[] getAcceptedNodeTypes() { return acceptedNodeTypes; }

  public void setAcceptedNodeTypes(String[] acceptedNodeTypes) { 
    this.acceptedNodeTypes = acceptedNodeTypes;
  }
  
  public String[] getExceptedNodeTypes() { return exceptedNodeTypes; }

  public void setExceptedNodeTypes(String[] exceptedNodeTypes) { 
    this.exceptedNodeTypes = exceptedNodeTypes;
  }

  public String[] getAcceptedMimeTypes() { return acceptedMimeTypes; }
  public void setAcceptedMimeTypes(String[] acceptedMimeTypes) { this.acceptedMimeTypes = acceptedMimeTypes; }  
  
  public String[] getDefaultExceptedNodeTypes() { return defaultExceptedNodeTypes; }
  
  public void setDefaultExceptedNodeTypes(String[] defaultExceptedNodeTypes) {
    this.defaultExceptedNodeTypes = defaultExceptedNodeTypes;
  }

  public List getSelectableNodes() throws Exception { return uiPageIterator_.getCurrentPageData(); }
  
  public void updateGrid() throws Exception {
    ObjectPageList objPageList = new ObjectPageList(getListSelectableNodes(), 10);
    uiPageIterator_.setPageList(objPageList);
  }
  
  public List<Node> getListSelectableNodes() throws Exception {
    List<Node> list = new ArrayList<Node>();
    if (parentNode == null) return list;
    Node realNode = Utils.getNodeSymLink(parentNode);
    for (NodeIterator iterator = realNode.getNodes();iterator.hasNext();) {
      Node child = iterator.nextNode();
      if(child.isNodeType("exo:hiddenable")) continue;
      if(matchMimeType(Utils.getNodeSymLink(child)) && matchNodeType(Utils.getNodeSymLink(child))) {
        list.add(child);
      }
    }
    List<Node> listNodeCheck = new ArrayList<Node>();
    for (Node node : list) {
      addNodePublish(listNodeCheck, node, publicationService_);
    }
    return listNodeCheck;
  }      

  protected boolean matchNodeType(Node node) throws Exception {
    if(acceptedNodeTypes == null || acceptedNodeTypes.length == 0) return true;
    for(String nodeType: acceptedNodeTypes) {
      if(node.isNodeType(nodeType)) return true;
    }
    return false;
  }
  
  protected boolean isExceptedNodeType(Node node) throws RepositoryException {
    if(exceptedNodeTypes == null || exceptedNodeTypes.length == 0) return false;
    for(String nodeType: exceptedNodeTypes) {
      if(node.isNodeType(nodeType)) return true;
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
  
  public String getPathTaxonomy() throws Exception {
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class);
    return nodeHierarchyCreator.getJcrPath(BasePath.TAXONOMIES_TREE_STORAGE_PATH);
  }
  
  public String getPathSystemTaxonomy() throws Exception {
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class);
    return nodeHierarchyCreator.getJcrPath(BasePath.EXO_TAXONOMIES_PATH);
  }

  static public class SelectActionListener extends EventListener<UISelectTaxonomyPanel> {
    public void execute(Event<UISelectTaxonomyPanel> event) throws Exception {
      UISelectTaxonomyPanel uiSelectPathPanel = event.getSource();      
      UIOneTaxonomySelector uiTaxonomySelector = uiSelectPathPanel.getParent();      
      UITreeTaxonomyList uiTreeList = uiTaxonomySelector.getChild(UITreeTaxonomyList.class);     
      UIContainer uiTreeSelector = uiSelectPathPanel.getParent();
      String value = event.getRequestContext().getRequestParameter(OBJECTID);
      String taxoTreeName = uiTreeList.getUIFormSelectBox(TAXONOMY_TREE).getValue();  
      Node taxoTreeNode = uiTaxonomySelector.getTaxoTreeNode(taxoTreeName);
      String taxoTreePath = taxoTreeNode.getPath();
      value = value.replace(taxoTreePath, taxoTreeName);

      if(uiTreeSelector instanceof UIOneNodePathSelector) {
        if(!((UIOneNodePathSelector)uiTreeSelector).isDisable()) {
          value = ((UIOneNodePathSelector)uiTreeSelector).getWorkspaceName() + ":" + value ;
        }
      }
      
      String returnField = ((UIBaseNodeTreeSelector)uiTreeSelector).getReturnFieldName();
      ((UISelectable)((UIBaseNodeTreeSelector)uiTreeSelector).getSourceComponent()).doSelect(returnField, value) ;
      
      UIComponent uiOneNodePathSelector = uiSelectPathPanel.getParent();
      if (uiOneNodePathSelector instanceof UIOneNodePathSelector) {
        UIComponent uiComponent = uiOneNodePathSelector.getParent();
        if (uiComponent instanceof UIPopupWindow) {
          ((UIPopupWindow)uiComponent).setShow(false);
          ((UIPopupWindow)uiComponent).setRendered(false);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent);
        }
        UIComponent component = ((UIOneNodePathSelector)uiOneNodePathSelector).getSourceComponent().getParent();
        if (component != null) {
          event.getRequestContext().addUIComponentToUpdateByAjax(component);
          return;
        }
      }
      if (uiOneNodePathSelector instanceof UIOneTaxonomySelector) {
        UIComponent uiComponent = uiOneNodePathSelector.getParent();
        if (uiComponent instanceof UIPopupWindow) {
          ((UIPopupWindow)uiComponent).setShow(false);
          ((UIPopupWindow)uiComponent).setRendered(false);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent);
        }
        UIComponent component = ((UIOneTaxonomySelector)uiOneNodePathSelector).getSourceComponent().getParent();
        if (component != null) {
          event.getRequestContext().addUIComponentToUpdateByAjax(component);
        }
      }
    }
  }
}
