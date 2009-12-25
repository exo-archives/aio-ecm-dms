/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.ecm.webui.component.admin.nodetype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.ecm.webui.form.UIFormInputSetWithAction;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIComponentDecorator;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormInputInfo;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Sep 20, 2006
 * 3:28:26 PM 
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/admin/nodetype/UINodeTypeList.gtmpl",
    events = {
      @EventConfig(listeners = UINodeTypeList.ViewActionListener.class),
      @EventConfig(listeners = UINodeTypeList.EditActionListener.class),
      @EventConfig(listeners = UINodeTypeList.DeleteActionListener.class, confirm="UINodeTypeList.msg.confirm-delete"),
      @EventConfig(listeners = UINodeTypeList.AddActionListener.class),
      @EventConfig(listeners = UINodeTypeList.ImportActionListener.class),
      @EventConfig(listeners = UINodeTypeList.ExportActionListener.class)
    }
)
public class UINodeTypeList extends UIComponentDecorator {

  private UIPageIterator uiPageIterator_ ;
  final static public String DRAFTNODETYPE = "jcr:system/jcr:nodetypesDraft" ;
  final static public String[] ACTIONS = {"Add", "Import", "Export"} ;
  final static public String[] CANCEL = {"Cancel"} ;
  final static public String[] TAB_REMOVE = {
    UINodeTypeForm.SUPER_TYPE_TAB, UINodeTypeForm.DEFAULT_PRIMARY_TYPE_TAB,
    UINodeTypeForm.REQUIRED_PRIMARY_TYPE_TAB} ;
  
  public UINodeTypeList() throws Exception {
    uiPageIterator_ = createUIComponent(UIPageIterator.class, null, "UINodeTypeListIterator");
    setUIComponent(uiPageIterator_) ;
  }
  
  @SuppressWarnings("unchecked")
  public List getAllNodeTypes() throws Exception{
    List nodeList = new ArrayList<NodeType>();     
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    ManageableRepository mRepository = getApplicationComponent(RepositoryService.class).getRepository(repository) ;
    NodeTypeManager ntManager = mRepository.getNodeTypeManager() ;    
    NodeTypeIterator nodeTypeIter = ntManager.getAllNodeTypes() ;
    while(nodeTypeIter.hasNext()) {
      nodeList.add(nodeTypeIter.nextNodeType()) ;
    }    
    Collections.sort(nodeList, new Utils.NodeTypeNameComparator()) ;
    Session session = mRepository.getSystemSession(mRepository.getConfiguration().getSystemWorkspaceName()) ;
    if(session.getRootNode().hasNode(DRAFTNODETYPE)) {
      Node draftNode = session.getRootNode().getNode(DRAFTNODETYPE) ;
      NodeIterator nodeIter = draftNode.getNodes() ;
      while(nodeIter.hasNext()) {
        nodeList.add(nodeIter.nextNode()) ;
      }
    }
    session.logout() ;
    return nodeList ;
  }
  
  public UIPageIterator  getUIPageIterator() {  return uiPageIterator_ ; }
  
  public List getNodeTypeList() throws Exception { return uiPageIterator_.getCurrentPageData() ; }
  
  public String[] getActions() { return ACTIONS ; }
  
  public void refresh(String name, int currentPage, List<NodeType> nodeType) throws Exception {
    PageList pageList = new ObjectPageList(nodeType, 10) ;
    uiPageIterator_.setPageList(pageList);
    if(currentPage > uiPageIterator_.getAvailablePage())
      uiPageIterator_.setCurrentPage(currentPage-1);
    else
      uiPageIterator_.setCurrentPage(currentPage);
  }

  public void refresh(String name, int currentPage) throws Exception {
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    ManageableRepository manaRepository = 
      getApplicationComponent(RepositoryService.class).getRepository(repository) ;
    Session session = manaRepository.getSystemSession(manaRepository.getConfiguration().getSystemWorkspaceName()) ;
    if(name != null) {
      if(session.getRootNode().hasNode(DRAFTNODETYPE)) {
        Node draftNode = session.getRootNode().getNode(DRAFTNODETYPE) ;
        if(draftNode.hasNode(name)) {
          Node deleteNode = draftNode.getNode(name) ;
          deleteNode.remove() ;            
          draftNode.save() ;
        }
        if(!draftNode.hasNodes())draftNode.remove() ;
        session.save() ;
      }
    } else {
      session.refresh(true) ;
    }
    session.logout();
    refresh(name, currentPage, getAllNodeTypes());
  }
  
  static public class AddActionListener extends EventListener<UINodeTypeList> {
    public void execute(Event<UINodeTypeList> event) throws Exception {
      UINodeTypeList uiList = event.getSource() ;
      UINodeTypeManager uiManager = uiList.getParent() ;
      uiManager.initPopup(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  static public class ImportActionListener extends EventListener<UINodeTypeList> {
    public void execute(Event<UINodeTypeList> event) throws Exception {
      UINodeTypeList uiList = event.getSource() ;
      UINodeTypeManager uiManager = uiList.getParent() ;
      uiManager.setImportPopup() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  static public class ExportActionListener extends EventListener<UINodeTypeList> {
    public void execute(Event<UINodeTypeList> event) throws Exception {
      UINodeTypeList uiList = event.getSource() ;
      UINodeTypeManager uiManager = uiList.getParent() ;
      uiManager.setExportPopup() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  static public class ViewActionListener extends EventListener<UINodeTypeList> {
    public void execute(Event<UINodeTypeList> event) throws Exception {
      UINodeTypeList uiList = event.getSource() ;
      String ntName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String repository = uiList.getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
      ManageableRepository manaRepository = 
        uiList.getApplicationComponent(RepositoryService.class).getRepository(repository) ;
      Session session = manaRepository.getSystemSession(manaRepository.getConfiguration().getSystemWorkspaceName()) ;
      NodeTypeManager ntManager = session.getWorkspace().getNodeTypeManager() ;
      NodeType nodeType = ntManager.getNodeType(ntName) ;
      UINodeTypeManager uiManager = uiList.getParent() ;
      uiManager.initPopup(true) ;
      UINodeTypeForm uiForm = uiManager.findFirstComponentOfType(UINodeTypeForm.class) ;
      uiForm.update(nodeType, true) ;
      for(UIComponent uiComp : uiForm.getChildren()) {
        UIFormInputSetWithAction tab = uiForm.getChildById(uiComp.getId()) ;
        for(UIComponent uiChild : tab.getChildren()) {
          if(!(uiChild instanceof UIFormInputInfo)) tab.setActionInfo(uiChild.getName(), null) ;
        }
        if(tab.getId().equals(UINodeTypeForm.NODETYPE_DEFINITION)) {
          tab.setRendered(true) ;
          tab.setActions(new String[] {"Close"}, null) ;
        } else {
          tab.setRendered(false) ;
          tab.setActions(null, null) ;
        }
      }
      uiForm.removeChildTabs(TAB_REMOVE) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  static public class EditActionListener extends EventListener<UINodeTypeList> {
    public void execute(Event<UINodeTypeList> event) throws Exception {
      UINodeTypeList uiNodeList = event.getSource() ;
      String repository = uiNodeList.getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
      ManageableRepository manaRepository = 
        uiNodeList.getApplicationComponent(RepositoryService.class).getRepository(repository) ;
      Session session = manaRepository.getSystemSession(manaRepository.getConfiguration().getSystemWorkspaceName()) ;
      String nodeName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Node draftNodeType = session.getRootNode().getNode(DRAFTNODETYPE + "/" + nodeName) ;
      UINodeTypeManager uiManager = uiNodeList.getParent() ;
      uiManager.initPopup(false) ;
      UINodeTypeForm uiForm = uiManager.findFirstComponentOfType(UINodeTypeForm.class) ;
      uiForm.refresh() ;
      uiForm.removeChildTabs(TAB_REMOVE) ;
      uiForm.updateEdit(draftNodeType, true) ;
      UIFormInputSetWithAction tab = uiForm.getChildById(UINodeTypeForm.NODETYPE_DEFINITION) ;
      String[] actionNames = {UINodeTypeForm.ACTION_SAVE, UINodeTypeForm.ACTION_SAVEDRAFT, 
                              UINodeTypeForm.ACTION_CANCEL} ; 
      tab.setActions(actionNames, null) ;
      tab.setIsView(false) ;
      uiForm.setTabRender(UINodeTypeForm.NODETYPE_DEFINITION) ;
      session.logout() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  static public class DeleteActionListener extends EventListener<UINodeTypeList> {
    public void execute(Event<UINodeTypeList> event) throws Exception {
      UINodeTypeList uiNodeList = event.getSource() ;
      String repository = uiNodeList.getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
      ManageableRepository manaRepository = 
        uiNodeList.getApplicationComponent(RepositoryService.class).getRepository(repository) ;
      Session session = manaRepository.getSystemSession(manaRepository.getConfiguration().getSystemWorkspaceName()) ;
      String nodeName = event.getRequestContext().getRequestParameter(OBJECTID) ;  
      if(session.getRootNode().hasNode(DRAFTNODETYPE)) {
        Node draftNode = session.getRootNode().getNode(DRAFTNODETYPE) ;
        Node deleteNode = draftNode.getNode(nodeName) ;
        deleteNode.remove() ;
        draftNode.save() ;
        if(!draftNode.hasNodes()) draftNode.remove() ;
        session.save() ;
        uiNodeList.refresh(null, uiNodeList.getUIPageIterator().getCurrentPage());
        event.getRequestContext().addUIComponentToUpdateByAjax(uiNodeList.getParent()) ;
      }
      session.logout() ;
    }
  }
}