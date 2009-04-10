/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
 *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.taxonomy;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.ReferentialIntegrityException;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.jcr.model.ClipboardCommand;
import org.exoplatform.ecm.webui.component.admin.taxonomy.info.UIPermissionForm;
import org.exoplatform.ecm.webui.component.admin.taxonomy.info.UIPermissionInfo;
import org.exoplatform.ecm.webui.component.admin.taxonomy.info.UIPermissionManager;
import org.exoplatform.services.cms.categories.CategoriesService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hoang Van Hung
 *          hunghvit@gmail.com
 * Apr 7, 2009  
 */

@ComponentConfig(
    template =  "app:/groovy/webui/component/admin/taxonomy/UITaxonomyWorkingArea.gtmpl",
    events = {
        @EventConfig(listeners = UITaxonomyTreeWorkingArea.AddActionListener.class),
        @EventConfig(listeners = UITaxonomyTreeWorkingArea.RemoveActionListener.class, confirm = "UITaxonomyManager.msg.confirm-delete"),
        @EventConfig(listeners = UITaxonomyTreeWorkingArea.CopyActionListener.class),
        @EventConfig(listeners = UITaxonomyTreeWorkingArea.PasteActionListener.class),
        @EventConfig(listeners = UITaxonomyTreeWorkingArea.CutActionListener.class),
        @EventConfig(listeners = UITaxonomyTreeWorkingArea.ViewPermissionActionListener.class)
    }
)

public class UITaxonomyTreeWorkingArea extends UIContainer {
  private UIPageIterator uiPageIterator_;
  private List<Node> taxonomyNodes_ ;
  private ClipboardCommand clipboard_ = new ClipboardCommand() ;
  private String selectedPath_ ;
  
  public UITaxonomyTreeWorkingArea() throws Exception {
    uiPageIterator_ = addChild(UIPageIterator.class, null, "UICategoriesSelect");
  }
  
  public UIPageIterator getUIPageIterator() { return uiPageIterator_; }
  
  public void updateGrid() throws Exception {
    ObjectPageList objPageList = new ObjectPageList(getNodeList(), 10);
    uiPageIterator_.setPageList(objPageList);
  }
  
  public List getListNodes() throws Exception { return uiPageIterator_.getCurrentPageData(); }
  
  public void setNodeList(List<Node> nodes) { taxonomyNodes_ = nodes ;  }
  public List<Node> getNodeList() {return taxonomyNodes_; } ;
  
  private String getRepository() throws Exception {
    UITaxonomyTreeCreateChild uiManager = getParent();
    return uiManager.getRepository();
  }
  
  public boolean isRootNode() throws Exception {
    UITaxonomyTreeCreateChild uiManager = getParent();
    String selectedPath = uiManager.getSelectedPath();
    if (selectedPath == null)
      selectedPath = uiManager.getRootNode().getPath();
    if (selectedPath.equals(uiManager.getRootNode().getPath()))
      return true;
    return false;
  }
  
  public void setSelectedPath(String selectedPath) { selectedPath_ = selectedPath ; }
  
  public void update() throws Exception {
    UITaxonomyTreeCreateChild uiManager = getParent();
    if(selectedPath_ != null) {
      Node selectedTaxonomy = uiManager.getNodeByPath(selectedPath_) ;
      NodeIterator nodeIter = selectedTaxonomy.getNodes() ;
      List<Node> listNodes = new ArrayList<Node>() ;
      while(nodeIter.hasNext()) {
        Node node = nodeIter.nextNode() ;
        listNodes.add(node) ;
      }
      setNodeList(listNodes) ;
    } 
    updateGrid();
  }
  
  public static class AddActionListener extends EventListener<UITaxonomyTreeWorkingArea> {
    public void execute(Event<UITaxonomyTreeWorkingArea> event) throws Exception {
      UITaxonomyTreeWorkingArea uiTreeWorkingArea = event.getSource() ;
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ; 
      UITaxonomyTreeCreateChild uiTaxonomyTreeCreateChild = uiTreeWorkingArea.getParent() ;
      uiTaxonomyTreeCreateChild.initPopup(path) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyTreeCreateChild) ;
    }
  }
  
  public static class RemoveActionListener extends EventListener<UITaxonomyTreeWorkingArea> {
    public void execute(Event<UITaxonomyTreeWorkingArea> event) throws Exception {
      UITaxonomyTreeWorkingArea uiTreeWorkingArea = event.getSource();   
      UITaxonomyTreeCreateChild uiTaxonomyTreeCreateChild = uiTreeWorkingArea.getParent() ;
      UIApplication uiApp = uiTreeWorkingArea.getAncestorOfType(UIApplication.class) ;
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;  
      Node selectedNode = uiTaxonomyTreeCreateChild.getNodeByPath(path) ;
      try {
        uiTreeWorkingArea.setSelectedPath(selectedNode.getParent().getPath()) ;
        uiTreeWorkingArea.getApplicationComponent(CategoriesService.class).removeTaxonomyNode(path, uiTreeWorkingArea.getRepository()) ;        
      } catch(ReferentialIntegrityException ref) {
        Object[] arg = { path } ;
        uiApp.addMessage(new ApplicationMessage("UITaxonomyWorkingArea.msg.reference-exception", arg, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;        
      } catch(Exception e) {
        e.printStackTrace() ;
        Object[] arg = { path } ;
        uiApp.addMessage(new ApplicationMessage("UITaxonomyWorkingArea.msg.path-error", arg, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(uiTaxonomyTreeCreateChild.getChildById("TaxonomyPopupCreateChild") != null) {
        uiTaxonomyTreeCreateChild.removeChildById("TaxonomyPopupCreateChild") ;
      }
      uiTreeWorkingArea.update() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyTreeCreateChild) ;
    }
  }
  
  public static class CopyActionListener extends EventListener<UITaxonomyTreeWorkingArea> {
    public void execute(Event<UITaxonomyTreeWorkingArea> event) throws Exception {
      UITaxonomyTreeWorkingArea uiManager = event.getSource() ;
      String realPath = event.getRequestContext().getRequestParameter(OBJECTID);            
      uiManager.clipboard_ = new ClipboardCommand() ;
      uiManager.clipboard_.setType(ClipboardCommand.COPY) ;
      uiManager.clipboard_.setSrcPath(realPath);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }

  public static class PasteActionListener extends EventListener<UITaxonomyTreeWorkingArea> {
    public void execute(Event<UITaxonomyTreeWorkingArea> event) throws Exception {
      UITaxonomyTreeWorkingArea uiWorkingArea = event.getSource() ;
      UITaxonomyTreeCreateChild uiTaxonomyTreeCreateChild = uiWorkingArea.getParent() ;
      String realPath = event.getRequestContext().getRequestParameter(OBJECTID);
      UIApplication uiApp = uiWorkingArea.getAncestorOfType(UIApplication.class) ;
      String type = uiWorkingArea.clipboard_.getType();
      String srcPath = uiWorkingArea.clipboard_.getSrcPath();
      if(type == null || srcPath == null) {
        uiApp.addMessage(new ApplicationMessage("UITaxonomyWorkingArea.msg.can-not-paste", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;        
      }
      if(type.equals(ClipboardCommand.CUT) && realPath.equals(srcPath)) {
        Object[] arg = { realPath } ;
        uiApp.addMessage(new ApplicationMessage("UITaxonomyWorkingArea.msg.node-is-cutting", arg, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;        
      }
      if(srcPath == null){
        Object[] arg = { realPath } ;
        uiApp.addMessage(new ApplicationMessage("UITaxonomyWorkingArea.msg.no-taxonomy-selected", arg, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String destPath = realPath + srcPath.substring(srcPath.lastIndexOf("/"));      
      Node realNode = uiTaxonomyTreeCreateChild.getNodeByPath(realPath) ;
      if(realNode.hasNode(srcPath.substring(srcPath.lastIndexOf("/") + 1))) {
        Object[] args = {srcPath.substring(srcPath.lastIndexOf("/") + 1)} ;
        uiApp.addMessage(new ApplicationMessage("UITaxonomyForm.msg.exist", args,
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      CategoriesService categoriesService = 
        uiWorkingArea.getApplicationComponent(CategoriesService.class) ;
      try {
        categoriesService.moveTaxonomyNode(srcPath, destPath, type, uiWorkingArea.getRepository()) ;        
        uiTaxonomyTreeCreateChild.update(realPath) ;
      } catch(Exception e) {
        uiApp.addMessage(new ApplicationMessage("UITaxonomyWorkingArea.msg.referential-integrity", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaxonomyTreeCreateChild) ;
    }
  }
  
  public static class CutActionListener extends EventListener<UITaxonomyTreeWorkingArea> {
    public void execute(Event<UITaxonomyTreeWorkingArea> event) throws Exception {
      UITaxonomyTreeWorkingArea uiManager = event.getSource() ;
      String realPath = event.getRequestContext().getRequestParameter(OBJECTID);       
      uiManager.clipboard_.setType(ClipboardCommand.CUT) ;
      uiManager.clipboard_.setSrcPath(realPath);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  public static class ViewPermissionActionListener extends EventListener<UITaxonomyTreeWorkingArea> {
    public void execute(Event<UITaxonomyTreeWorkingArea> event) throws Exception {
      UITaxonomyTreeWorkingArea uiManager = event.getSource();
      UITaxonomyTreeCreateChild uiTaxonomyTreeCreateChild = uiManager.getParent();
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;  
      UIPopupContainer uiPopupContainer = uiTaxonomyTreeCreateChild.initPopupPermission(UITaxonomyManager.PERMISSION_ID_POPUP);
      UIPermissionManager uiPerMan = uiPopupContainer.createUIComponent(UIPermissionManager.class, null, null);
      uiPerMan.getChild(UIPermissionInfo.class).setCurrentNode(uiTaxonomyTreeCreateChild.getNodeByPath(path));
      uiPerMan.getChild(UIPermissionForm.class).setCurrentNode(uiTaxonomyTreeCreateChild.getNodeByPath(path));
      uiPopupContainer.activate(uiPerMan, 650,550);
      uiPopupContainer.setRendered(true);
      uiPerMan.checkPermissonInfo(uiTaxonomyTreeCreateChild.getNodeByPath(path));
    }
  }
}