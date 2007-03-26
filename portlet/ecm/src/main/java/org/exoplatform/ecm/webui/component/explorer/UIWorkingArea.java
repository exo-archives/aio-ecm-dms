/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.ecm.jcr.JCRExceptionManager;
import org.exoplatform.ecm.jcr.model.ClipboardCommand;
import org.exoplatform.ecm.webui.component.explorer.control.UIActionBar;
import org.exoplatform.ecm.webui.component.explorer.control.UIControl;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIDocumentForm;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIRenameForm;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionContainer;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionForm;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionTypeForm;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UISideBar;
import org.exoplatform.portal.component.view.Util;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.relations.RelationsService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIRightClickPopupMenu;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * July 3, 2006
 * 10:07:15 AM
 */
@ComponentConfigs({
  @ComponentConfig(
      template =  "app:/groovy/webui/component/explorer/UIWorkingArea.gtmpl"
  ),
  @ComponentConfig(
      type = UIRightClickPopupMenu.class,
      id = "ECMContextMenu",
      template = "system:/groovy/webui/component/UIRightClickPopupMenu.gtmpl",
      events = {
        @EventConfig(listeners = UIWorkingArea.EditDocumentActionListener.class),
        @EventConfig(listeners = UIWorkingArea.CopyActionListener.class),
        @EventConfig(listeners = UIWorkingArea.CutActionListener.class),
        @EventConfig(listeners = UIWorkingArea.SaveActionListener.class),
        @EventConfig(listeners = UIWorkingArea.DeleteActionListener.class),
        @EventConfig(listeners = UIWorkingArea.LockActionListener.class),
        @EventConfig(listeners = UIWorkingArea.UnlockActionListener.class),
        @EventConfig(listeners = UIWorkingArea.CheckInActionListener.class),
        @EventConfig(listeners = UIWorkingArea.CheckOutActionListener.class),
        @EventConfig(listeners = UIWorkingArea.RenameActionListener.class),
        @EventConfig(listeners = UIWorkingArea.CustomActionListener.class),
        @EventConfig(listeners = UIWorkingArea.PasteActionListener.class)
      }
  )
})

public class UIWorkingArea extends UIContainer {
  
  final static public String NT_UNSTRUCTURED = "nt:unstructured" ;
  final static public String NT_FOLDER = "nt:folder" ;
  final static public String EXO_RELATION = "exo:relation" ;
  final static public String NT_FILE = "nt:file" ;
  final static public String EXO_TAXANOMY = "exo:taxonomy" ;
  final static public String MIX_REFERENCEABLE = "mix:referenceable" ;
  final static public String MIX_VERSIONABLE = "mix:versionable" ;
  final static public String NT_RESOURCE = "nt:resource" ;
  final static public String DEFAULT = "default" ;
  final static public String JCR_CONTENT = "jcr:content" ;
  final static public String JCR_MIMETY = "jcr:mimeType" ;
  final static public String EXO_ROLES = "exo:roles" ;
  final static public String MIX_LOCKABLE = "mix:lockable" ;
  final static public String EXO_CATEGORIZED = "exo:categorized" ;
  final static public String EXO_CATEGORY = "exo:category" ;
  final static public String[] NON_EDITABLE_NODETYPES = {NT_UNSTRUCTURED, NT_FOLDER, NT_RESOURCE};

  public UIWorkingArea() throws Exception {
    addChild(UIRightClickPopupMenu.class, "ECMContextMenu", null) ;
    addChild(UISideBar.class, null, null) ;
    addChild(UIDocumentWorkspace.class, null, null) ;
  }

  public boolean isShowSideBar() throws Exception {
    UIJCRExplorer jcrExplorer = getParent() ;
    return jcrExplorer.getPreference().isShowSideBar() ;
  }

  public void setShowSideBar(boolean b) throws Exception {
    UIJCRExplorer jcrExplorer = getParent() ;
    jcrExplorer.getPreference().setShowSideBar(b) ;
  }
  public Node getNodeByUUID(String uuid) throws Exception{
    CmsConfigurationService cmsConfService = getApplicationComponent(CmsConfigurationService.class) ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    Session session = repositoryService.getRepository().getSystemSession(cmsConfService.getWorkspace());
    return session.getNodeByUUID(uuid);
  }
  
  public List getAllClipBoard() {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    return uiExplorer.getAllClipBoard() ;
  }
  
  protected Node getCurrentNode() {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    return uiExplorer.getCurrentNode() ;
  }
  
  public boolean isReferenceableNode(Node node) throws Exception {
    NodeType[] nodeTypes = node.getMixinNodeTypes() ;
    for(NodeType type:nodeTypes) {      
      if(type.getName().equals(MIX_REFERENCEABLE)) return true ;
    }
    return false ;
  }
  
  public boolean isPreferenceNode(Node node) throws RepositoryException {
    return (getCurrentNode().hasNode(node.getName())) ? false : true ;
  }

  public boolean isSameNameSibling(Node node) throws Exception {
    return (node.getPath().endsWith("]")) ? true : false ;
  }

  public boolean isEditable(String nodePath) throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    boolean isEdit = true;
    Node childNode ;    
    if(nodePath.indexOf(";") > -1) {
      childNode = uiExplorer.getNodeByPath(nodePath) ; 
    } else {
      Node currentNode =  uiExplorer.getCurrentNode() ;
      String name = nodePath.substring(nodePath.lastIndexOf("/") + 1) ;
      childNode = currentNode.getNode(name) ;
    }      
    String nodeType = childNode.getPrimaryNodeType().getName();
    for (int i = 0; i < NON_EDITABLE_NODETYPES.length; i++) {
      String nonEditableType = NON_EDITABLE_NODETYPES[i];
      if (nonEditableType.equals(nodeType)) return false;
    }    
    return isEdit;
  }

  public boolean isVersionableOrAncestor(Node node) throws RepositoryException {
    if (isVersionable(node) || isAncestorVersionable(node)) return true;
    return false;
  }

  public boolean isEditable(Node node) throws Exception {
    String nodeType = node.getPrimaryNodeType().getName();
    for (int i = 0; i < NON_EDITABLE_NODETYPES.length; i++) {
      String nonEditableType = NON_EDITABLE_NODETYPES[i];
      if (nonEditableType.equals(nodeType)) return false;
    }
    return true;
  }

  public boolean isReadAuthorized(ExtendedNode node) throws RepositoryException {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    return uiExplorer.isReadAuthorized(node);
  }

  public String getNodeOwner() throws RepositoryException { 
    return ((ExtendedNode) getCurrentNode()).getACL().getOwner(); 
  }

  public boolean isVersionable(Node node) throws RepositoryException {
    return node.isNodeType(MIX_VERSIONABLE) && !node.isNodeType("nt:frozenNode");
  }

  public String getVersionNumber(Node node) throws RepositoryException {
    if(!isVersionable(node)) return "-";
    return node.getBaseVersion().getName();
  }

  public boolean isAncestorVersionable(Node node) throws RepositoryException {
    int depth = node.getDepth() - 1;
    Node parent = (Node) node.getAncestor(depth);
    while (parent != null && depth != 0) {
      if (parent.isNodeType(MIX_VERSIONABLE)) return true;
      depth-- ;
      parent = (Node) node.getAncestor(depth);
    }
    return false;
  }
  
  public boolean hasEditPermissions(Node editNode){
    try {
      editNode.getSession().checkPermission(editNode.getPath(), "add_node,set_property");
    } catch(Exception e) {
      return false ;
    } 
    return true;
  }
  
  public String getNodePath(Node node) throws Exception {
    String nodePath = node.getPath() ;
    if(isPreferenceNode(node)) {
      String preferenceWS = node.getSession().getWorkspace().getName() ;
      nodePath = preferenceWS + ";" + nodePath ;
    }
    return nodePath ;
  }
  
  public boolean hasRemovePermissions(Node curNode){
    try {
      curNode.getSession().checkPermission(curNode.getPath(), "remove");
    } catch(Exception e) {
      return false ;
    } 
    return true;
  }
  
  public boolean isJcrViewEnable() throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    if(uiExplorer.getPreference().isJcrEnable()) return true ;
    return false ;
  }
  
  private String getList(Node node, String path) throws Exception {
    StringBuilder actionsList = new StringBuilder() ;
    if(isReadAuthorized((ExtendedNode)node)) {
      if(isVersionableOrAncestor(node)) {
        if(node.isCheckedOut()) {
          if(isVersionable(node)) actionsList.append("CheckIn") ;
          if(isEditable(path) && hasEditPermissions(node)) actionsList.append(",EditDocument") ;
          if(node.holdsLock() && hasEditPermissions(node)) actionsList.append(",Unlock") ;
          else if(!node.isLocked() && hasEditPermissions(node)) actionsList.append(",Lock") ;
          if(!isSameNameSibling(node)) {
            actionsList.append(",Copy") ;
            if(hasRemovePermissions(node)) actionsList.append(",Cut") ;
          }
          if(hasEditPermissions(node)) actionsList.append(",Rename") ;
          if(isJcrViewEnable()) actionsList.append(",Save") ;
          if(hasRemovePermissions(node)) actionsList.append(",Delete") ;
        } else {
          if(isVersionable(node)) actionsList.append(",CheckOut") ;
          if(node.holdsLock() && hasEditPermissions(node)) actionsList.append(",Unlock") ;
          else if(!node.isLocked() && hasEditPermissions(node)) actionsList.append(",Lock") ;
          if(!isSameNameSibling(node)) actionsList.append(",Copy") ;
          if(hasEditPermissions(node)) actionsList.append(",Rename") ; 
        }
      } else {
        if(isEditable(path) && hasEditPermissions(node)) actionsList.append(",EditDocument") ;
        if(node.holdsLock() && hasEditPermissions(node)) {
          actionsList.append(",Unlock") ;
        } else if(!node.isLocked() && hasEditPermissions(node)) {
          actionsList.append(",Lock") ;
        }
        if(!isSameNameSibling(node)) {
          actionsList.append(",Copy") ;
          if(hasRemovePermissions(node)) actionsList.append(",Cut") ;
        }
        if(hasEditPermissions(node)) actionsList.append(",Rename") ;
        if(isJcrViewEnable()) actionsList.append(",Save") ;
        if(hasRemovePermissions(node)) actionsList.append(",Delete") ;
      }
    }
    return actionsList.toString() ;
  }
  
  public String getActionsList(Node node) throws Exception {
    String actionsList = null ;
    String preferenceWS = node.getSession().getWorkspace().getName() ;
    String path = node.getPath() ;
    if(isPreferenceNode(node)) path = preferenceWS + ";" + path ;
    actionsList = getList(node, path) ;
    if(getAllClipBoard().size() > 0) actionsList = actionsList + "," + "Paste" ;
    return actionsList ;
    
  }
  
  @SuppressWarnings("unused")
  static  public class EditDocumentActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent() ;
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class) ;
      Node selectedNode ;
      Session session ; 
      boolean isReferenced = false ;
      if(uiExplorer.nodeIsLocked(nodePath)) {
        Object[] arg = { nodePath } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }
      if(nodePath.indexOf(";") > -1) {
        isReferenced = true ;
        String[] array = nodePath.split(";") ;
        if(!uiExplorer.getSession().getWorkspace().getName().equals(array[0])) {
          RepositoryService repositoryService = uicomp.getApplicationComponent(RepositoryService.class) ;
          session = repositoryService.getRepository().login(array[0]) ;
        } else {
          session = uiExplorer.getSession() ;
        } 
        selectedNode = (Node)session.getItem(array[1]) ;          
      } else {
        selectedNode = uiExplorer.getCurrentNode();
        nodePath = nodePath.substring(nodePath.lastIndexOf("/") + 1) ;
        selectedNode = selectedNode.getNode(nodePath);          
      }
      if(selectedNode.isNodeType("exo:action")) {
        UIActionContainer uiContainer = uiExplorer.createUIComponent(UIActionContainer.class, null, null) ;
        uiExplorer.setIsHidePopup(true) ;
        uiContainer.getChild(UIActionTypeForm.class).setRendered(false) ;
        UIActionForm uiActionForm = uiContainer.getChild(UIActionForm.class) ;
        uiActionForm.createNewAction(uiExplorer.getCurrentNode(), 
                                     selectedNode.getPrimaryNodeType().getName(), false) ;
        uiActionForm.setNode(selectedNode) ;
        UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class) ;
        uiPopupAction.activate(uiContainer, 600, 550) ;
      } else {
        TemplateService tservice = uicomp.getApplicationComponent(TemplateService.class) ;
        List documentNodeType = tservice.getDocumentTemplates() ;
        String nodeType = selectedNode.getPrimaryNodeType().getName() ;
        if(documentNodeType.contains(nodeType)){
          UIDocumentForm uiDocumentForm = 
            uiExplorer.createUIComponent(UIDocumentForm.class, null, null) ;
          uiDocumentForm.setTemplateNode(nodeType) ;
          uiDocumentForm.setNode(selectedNode) ;
          uiDocumentForm.editDocument(selectedNode) ;
          uiDocumentForm.setContentNode(selectedNode) ;
          UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class) ;
          uiPopupAction.activate(uiDocumentForm, 600, 550) ;
        } else {
          Object[] arg = { nodePath } ;
          uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.not-support", arg)) ;
        }
      }
    }
  }

  static  public class RenameActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent() ;
      String renameNodePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class) ;
      if(uiExplorer.nodeIsLocked(renameNodePath)) {
        Object[] arg = { renameNodePath } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }
      Node renameNode ;
      boolean isReferencedNode = false ;
      try {
        if(renameNodePath.indexOf(";") > -1) {
          isReferencedNode = true ;
          renameNode = uiExplorer.getNodeByPath(renameNodePath) ;
        } else {
          renameNode = (Node)uiExplorer.getSession().getItem(renameNodePath) ;
        }
        UIControl uiControl = uiExplorer.getChild(UIControl.class) ;
        UIActionBar uiActionBar = uiControl.getChild(UIActionBar.class) ;
        UIRenameForm uiRenameForm = uiActionBar.createUIComponent(UIRenameForm.class, null, null) ;
        uiRenameForm.update(renameNode, isReferencedNode) ;
        UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class) ;
        uiPopupAction.activate(uiRenameForm, 700, 0) ;
        uiPopupAction.setRendered(true) ;
      } catch(Exception e) {
        JCRExceptionManager.process(uiApp, e);
      }
    }
  }

  static  public class CopyActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent() ;
      String srcPath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class) ;
      if(uiExplorer.nodeIsLocked(srcPath)) {
        Object[] arg = { srcPath } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }
      try {
        List<ClipboardCommand> clipboards = uiExplorer.getAllClipBoard() ;
        for(ClipboardCommand command:clipboards) {
          if(command.getSrcPath().equals(srcPath)) {
            clipboards.remove(command) ;
            break ;
          }
        }       
        ClipboardCommand clipboard = new ClipboardCommand() ;
        clipboard.setType(ClipboardCommand.COPY) ;
        clipboard.setSrcPath(srcPath) ;
        uiExplorer.getAllClipBoard().add(clipboard) ;                      
//        Object[] args = { srcPath };
//        uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.node-copied", args));
        if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save() ;
        uiExplorer.updateAjax(event) ;
      } catch(Exception e) {
        JCRExceptionManager.process(uiApp, e);
      }
    }
  }

  static  public class CutActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class) ;
      if(uiExplorer.nodeIsLocked(nodePath)) {
        Object[] arg = { nodePath } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }      
      try {
        List<ClipboardCommand> clipboards = uiExplorer.getAllClipBoard() ;
        for(ClipboardCommand command:clipboards) {
          if(command.getSrcPath().equals(nodePath)) {
            clipboards.remove(command) ;
            break ;
          }
        }       
        ClipboardCommand clipboard = new ClipboardCommand() ;
        clipboard.setType(ClipboardCommand.CUT) ;
        clipboard.setSrcPath(nodePath) ;
        uiExplorer.getAllClipBoard().add(clipboard) ;                     
        if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save() ;
        uiExplorer.updateAjax(event) ;
        //Object[] args = { nodePath };
        //uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.node-cut", args));
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
      }
    }
  }

  static  public class SaveActionListener extends EventListener<UIDocumentInfo> {
    public void execute(Event<UIDocumentInfo> event) throws Exception {
      UIDocumentInfo uicomp = event.getSource().getParent() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Node node = null ;
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class) ;
      try {
        if(nodePath.indexOf(";") > -1) {
          node = uiExplorer.getNodeByPath(nodePath) ;
        } else {
          node = (Node)uiExplorer.getSession().getItem(nodePath);
        }
        Object[] args = { nodePath };
        if(node.isNew()) {
          uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.unable-save-node",args));
          return ;
        }
        node.save(); 
        uiExplorer.getSession().save() ;
        uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.save-node-success", args));
      } catch(Exception e) {
        JCRExceptionManager.process(uiApp, e);
      }
    }
  }
  
  static  public class DeleteActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class) ;
      if(uiExplorer.nodeIsLocked(nodePath)) {
        Object[] arg = { nodePath } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }    
      Node node ;
      try {        
        if ("/".equals(nodePath)) {
          Object[] arg = { nodePath } ;
          uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.remove-root", arg));
          return;
        }
        if(nodePath.indexOf(";") > -1 ) {
          node = uiExplorer.getNodeByPath(nodePath) ;
          Node parentNode = node.getParent() ;
          node.remove();
          parentNode.getSession().save() ;
        } else {
          String name = nodePath.substring(nodePath.lastIndexOf("/") + 1) ;
          Node parentNode = uiExplorer.getCurrentNode() ;
          node = parentNode.getNode(name);
          node.remove();
          parentNode.save() ;          
        } 
        if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save() ;        
//        Object[] args = { nodePath };
//        uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.node-remove-success", args));
//        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp) ;
        uiExplorer.updateAjax(event) ;
      } catch(Exception e) {
        JCRExceptionManager.process(uiApp, e);
      }
    }
  }

  static  public class LockActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      String name = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class) ;
      Node node ;
      try {
        if(name.indexOf(";") > -1) {
          node = uiExplorer.getNodeByPath(name) ;
        } else {
          name = name.substring(name.lastIndexOf("/") + 1) ;
          node = uiExplorer.getCurrentNode().getNode(name);
        }      
        if(!node.isNodeType(MIX_LOCKABLE)) {
          node.addMixin(MIX_LOCKABLE);
          node.save();
        }
        node.lock(true, true); 
        if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save() ;
      } catch(Exception e) {
        e.printStackTrace() ;
        JCRExceptionManager.process(uiApp, e);
      }
    }
  }

  static  public class UnlockActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      String name = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class) ;
      Node node ;
      try {
        if(name.indexOf(";") > -1) {
          node = uiExplorer.getNodeByPath(name) ;
        } else {
          name = name.substring(name.lastIndexOf("/") + 1) ;
          node = uiExplorer.getCurrentNode().getNode(name);
        }
        node.unlock();  
        if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save() ;
      } catch (Exception e) {
        e.printStackTrace() ;
        JCRExceptionManager.process(uiApp, e);
      }
    }
  }

  static  public class CheckInActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class) ;
      if(uiExplorer.nodeIsLocked(nodePath)) {
        Object[] arg = { nodePath } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }    
      Node node ;
      try {
        if(nodePath.indexOf(";") > -1) {
          node = uiExplorer.getNodeByPath(nodePath) ;
        } else {
          node = (Node)uiExplorer.getSession().getItem(nodePath);
        }
        node.checkin();
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
      }
    }
  }

  static  public class CheckOutActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class) ;
      if(uiExplorer.nodeIsLocked(nodePath)) {
        Object[] arg = { nodePath } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }    
      Node node ;
      try {
        if(nodePath.indexOf(";") > -1) {
          node = uiExplorer.getNodeByPath(nodePath) ;
        } else {
          node = (Node)uiExplorer.getSession().getItem(nodePath);
        }
        node.checkout();
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
      }
    }
  }

  static  public class CustomActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent() ;
      String nodeName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class) ;
      nodeName = nodeName.substring(nodeName.lastIndexOf("/") + 1) ;
      String actionName = event.getRequestContext().getActionParameterName() ;
      ActionServiceContainer actionService = 
        uicomp.getApplicationComponent(ActionServiceContainer.class) ;
      try {
        Node node = uicomp.getCurrentNode().getNode(nodeName);
        String userId = Util.getUIPortal().getOwner() ;
        actionService.executeAction(userId, node, actionName, new HashMap());
        Object[] args = { actionName };
        uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.custom-action-success", args));        
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
      }
    }
  }
  
  static public class PasteActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      String destPath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class) ;
      ClipboardCommand currentClipboard = 
        ((LinkedList<ClipboardCommand>)uicomp.getAllClipBoard()).getLast() ;
      String srcPath = currentClipboard.getSrcPath() ;
      String type = currentClipboard.getType();
      String srcWorkspace = null ;
      if(srcPath.indexOf(";") > -1 ) {
        String[] array = srcPath.split(";") ;
        srcWorkspace = array[0].trim() ;
        srcPath = array[1].trim() ;
      } 
      if(destPath.indexOf(";") > -1 ) {
        String[] array = destPath.split(";") ;
        srcWorkspace = array[0].trim() ;
        destPath = array[1].trim() ;
      }
      if(uiExplorer.nodeIsLocked(destPath)) {
        Object[] arg = { destPath } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg)) ;
        return ;
      }
      destPath = destPath + srcPath.substring(srcPath.lastIndexOf("/")) ;
      try {
        if (ClipboardCommand.COPY.equals(type)) {
          pasteByCopy(uiExplorer, srcWorkspace, srcPath, destPath) ;
        } else {
          pasteByCut(uiExplorer, srcWorkspace, srcPath, destPath) ;
        }
        if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save() ;
//        Object[] args = { srcPath, destPath };
//        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-pasted", args));
        uiExplorer.updateAjax(event) ;
      } catch(Exception e) {       
        JCRExceptionManager.process(uiApp, e);
      }
    }
    
    private void pasteByCopy(UIJCRExplorer uiExplorer, String srcWorkspace, 
                             String srcPath, String destPath) throws Exception {
      Session session = uiExplorer.getSession() ;
      Workspace workspace = session.getWorkspace();
      if(srcWorkspace != null) workspace.copy(srcWorkspace, srcPath, destPath);
      else workspace.copy(srcPath, destPath);
      Node destNode = (Node) session.getItem(destPath) ;
      removeReferences(destNode, session) ;
    }
    
    private void pasteByCut(UIJCRExplorer uiExplorer, String srcWorkspace, 
                            String srcPath, String destPath) throws Exception {
      Session session = uiExplorer.getSession() ;
      Workspace workspace = session.getWorkspace();
      if(srcWorkspace != null) {
        workspace.copy(srcWorkspace, srcPath, destPath);
        Node destNode = (Node) session.getItem(destPath) ;
        removeReferences(destNode, session) ;
        RepositoryService repositoryService = 
          uiExplorer.getApplicationComponent(RepositoryService.class) ;
        Session srcSession = repositoryService.getRepository().login(srcWorkspace) ;
        srcSession.getItem(srcPath).remove() ;
        srcSession.save() ;
      } else {
        RelationsService relationsService = 
          uiExplorer.getApplicationComponent(RelationsService.class) ;
        List<Node> refList = new ArrayList<Node>() ;
        boolean isReference = false ;
        PropertyIterator references = null;
        Node srcNode = (Node)uiExplorer.getSession().getItem(srcPath) ;
        try {
          references = srcNode.getReferences() ;
          isReference = true ;
        } catch(Exception e) {
          isReference = false ;
        }  
        if(isReference && references != null) {
          if(references.getSize() > 0 ) {
            while(references.hasNext()) {
              Property pro = references.nextProperty() ;
              Node refNode = pro.getParent() ;
              relationsService.removeRelation(refNode, srcPath) ;
              refNode.save() ;
              refList.add(refNode) ;
            }
          }
        }            
        session.move(srcPath, destPath);
        session.save() ;
        for(int i = 0; i < refList.size(); i ++) {
          Node addRef = refList.get(i) ;
          relationsService.addRelation(addRef, destPath) ;
          addRef.save() ;
        }
      }
      for(ClipboardCommand currClip : uiExplorer.getAllClipBoard()) {
        if(currClip.getSrcPath().equals(srcPath)) {
          uiExplorer.getAllClipBoard().remove(currClip) ;
          break ;
        }
      }
    }
    
    private void removeReferences(Node destNode, Session session) throws Exception {
      NodeType[] mixinTypes = destNode.getMixinNodeTypes() ;
      for(int i = 0; i < mixinTypes.length; i ++) {
        if(mixinTypes[i].getName().equals(EXO_CATEGORIZED) && destNode.hasProperty(EXO_CATEGORIZED)) {
          Node valueNode = null ;
          Value valueAdd = session.getValueFactory().createValue(valueNode);
          destNode.setProperty(EXO_CATEGORIZED, new Value[] {valueAdd}) ;            
        }            
      }
      destNode.save() ;
    }
  }
}