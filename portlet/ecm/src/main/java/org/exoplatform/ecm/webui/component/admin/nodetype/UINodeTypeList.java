/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.nodetype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.component.UIComponentDecorator;
import org.exoplatform.webui.component.UIFormInputInfo;
import org.exoplatform.webui.component.UIPageIterator;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

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
      @EventConfig(listeners = UINodeTypeList.DeleteActionListener.class),
      @EventConfig(listeners = UINodeTypeList.AddActionListener.class),
      @EventConfig(listeners = UINodeTypeList.ImportActionListener.class),
      @EventConfig(listeners = UINodeTypeList.ExportActionListener.class)
    }
)
public class UINodeTypeList extends UIComponentDecorator {

  private UIPageIterator uiPageIterator_ ;
  private List<NodeType> nodeTypeList_ = new ArrayList<NodeType>() ;
  final static public String DRAFTNODETYPE = "jcr:system/jcr:nodetypesDraft" ;
  final static public String[] ACTIONS = {"Add", "Import", "Export"} ;
  final static public String[] CANCEL = {"Cancel"} ;
  final static public String[] TAB_REMOVE = {
    UINodeTypeForm.SUPER_TYPE_TAB, UINodeTypeForm.DEFAULT_PRIMARY_TYPE_TAB,
    UINodeTypeForm.REQUIRED_PRIMARY_TYPE_TAB} ;
  
  public UINodeTypeList() throws Exception {
    uiPageIterator_ = createUIComponent(UIPageIterator.class, null, "UINodeTypeListIterator");
    setUIComponent(uiPageIterator_) ;
    PageList pageList = new ObjectPageList(getAllNodeTypes(), 10) ;
    uiPageIterator_.setPageList(pageList) ;
  }
  
  @SuppressWarnings("unchecked")
  public List getAllNodeTypes() throws Exception{
    List nodeList = new ArrayList<NodeType>(); 
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    CmsConfigurationService cmsConfigService = 
      getApplicationComponent(CmsConfigurationService.class) ;
    Session session = 
      repositoryService.getRepository().getSystemSession(cmsConfigService.getWorkspace()) ;
    NodeTypeManager ntManager = session.getWorkspace().getNodeTypeManager() ;
    NodeTypeIterator nodeTypeIter = ntManager.getAllNodeTypes() ;
    while(nodeTypeIter.hasNext()) {
      nodeList.add(nodeTypeIter.nextNodeType()) ;
    }    
    Collections.sort(nodeList, new NodeTypeNameComparator()) ;
    if(session.getRootNode().hasNode(DRAFTNODETYPE)) {
      Node draftNode = session.getRootNode().getNode(DRAFTNODETYPE) ;
      NodeIterator nodeIter = draftNode.getNodes() ;
      while(nodeIter.hasNext()) {
        nodeList.add(nodeIter.nextNode()) ;
      }
    }
    nodeTypeList_ = nodeList ;    
    return nodeList ;
  }
  
  public UIPageIterator  getUIPageIterator() {  return uiPageIterator_ ; }
  
  public List getNodeTypeList() throws Exception { return uiPageIterator_.getCurrentPageData() ; }
  
  public NodeType getNodeTypeByName(String nodeTypeName) throws Exception {
    for(NodeType node : nodeTypeList_) {
      if(node.getName().equals(nodeTypeName)) return node ;
    }
    return null ;
  }
  
  public String[] getActions() { return ACTIONS ; }
  
  public void refresh(String name) throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    CmsConfigurationService cmsConfigService = 
      getApplicationComponent(CmsConfigurationService.class) ;
    Session session = 
      repositoryService.getRepository().getSystemSession(cmsConfigService.getWorkspace()) ;
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
    PageList pageList = new ObjectPageList(getAllNodeTypes(), 10) ;
    uiPageIterator_.setPageList(pageList);        
  }
  
  static public class NodeTypeNameComparator implements Comparator {
    public int compare(Object o1, Object o2) throws ClassCastException {
      String name1 = ((NodeType) o1).getName() ;
      String name2 = ((NodeType) o2).getName() ;
      return name1.compareToIgnoreCase(name2) ;
    }
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
      String nodeName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      NodeType nodeType = uiList.getNodeTypeByName(nodeName) ;
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
      RepositoryService repositoryService = 
        uiNodeList.getApplicationComponent(RepositoryService.class) ;
      CmsConfigurationService cmsConfigService = 
        uiNodeList.getApplicationComponent(CmsConfigurationService.class) ;
      Session session = 
        repositoryService.getRepository().getSystemSession(cmsConfigService.getWorkspace()) ;
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
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  static public class DeleteActionListener extends EventListener<UINodeTypeList> {
    public void execute(Event<UINodeTypeList> event) throws Exception {
      UINodeTypeList uiNodeList = event.getSource() ;
      RepositoryService repositoryService = 
        uiNodeList.getApplicationComponent(RepositoryService.class) ;
      CmsConfigurationService cmsConfigService = 
        uiNodeList.getApplicationComponent(CmsConfigurationService.class) ;
      Session session = 
        repositoryService.getRepository().getSystemSession(cmsConfigService.getWorkspace()) ;
      String nodeName = event.getRequestContext().getRequestParameter(OBJECTID) ;  
      if(session.getRootNode().hasNode(DRAFTNODETYPE)) {
        Node draftNode = session.getRootNode().getNode(DRAFTNODETYPE) ;
        Node deleteNode = draftNode.getNode(nodeName) ;
        deleteNode.remove() ;
        draftNode.save() ;
        if(!draftNode.hasNodes()) draftNode.remove() ;
        session.save() ;
        uiNodeList.refresh(null);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiNodeList.getParent()) ;
      }
    }
  }
}
