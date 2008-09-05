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
package org.exoplatform.ecm.webui.component.explorer ;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemExistsException;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.Workspace;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.VersionException;

import org.exoplatform.ecm.jcr.JCRExceptionManager;
import org.exoplatform.ecm.jcr.model.ClipboardCommand;
import org.exoplatform.ecm.utils.SessionsUtils;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.explorer.control.UIActionBar;
import org.exoplatform.ecm.webui.component.explorer.control.UIControl;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIDocumentForm;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIDocumentFormController;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIRenameForm;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionContainer;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionForm;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionTypeForm;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UISideBar;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.relations.RelationsService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIRightClickPopupMenu;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;

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
      template = "system:/groovy/webui/core/UIRightClickPopupMenu.gtmpl",
      events = {
        @EventConfig(listeners = UIWorkingArea.EditDocumentActionListener.class),
        @EventConfig(listeners = UIWorkingArea.CopyActionListener.class),
        @EventConfig(listeners = UIWorkingArea.CutActionListener.class),
        @EventConfig(listeners = UIWorkingArea.SaveActionListener.class),
        @EventConfig(listeners = UIWorkingArea.DeleteActionListener.class, confirm = "UIWorkingArea.msg.confirm-delete"),
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

  final static private String RELATION_PROP = "exo:relation";
  final static public String WS_NAME = "workspaceName" ;

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
    String repository = getAncestorOfType(UIJCRExplorer.class).getRepositoryName() ;    
    ManageableRepository repo = getApplicationComponent(RepositoryService.class).getRepository(repository);
    String workspace = repo.getConfiguration().getDefaultWorkspaceName() ;
    Session session = SessionsUtils.getSystemProvider().getSession(workspace,repo) ;    
    return session.getNodeByUUID(uuid);
  }

  protected Node getCurrentNode() throws Exception {
    return getAncestorOfType(UIJCRExplorer.class).getCurrentNode() ;
  }

  public boolean isReferenceableNode(Node node) throws Exception {
    NodeType[] nodeTypes = node.getMixinNodeTypes() ;
    for(NodeType type:nodeTypes) {      
      if(type.getName().equals(Utils.MIX_REFERENCEABLE)) return true ;
    }
    return false ;
  }

  public boolean isPreferenceNode(Node node) {
    return getAncestorOfType(UIJCRExplorer.class).isPreferenceNode(node) ;
  }

  public boolean isSameNameSibling(Node node) throws Exception {
    return (node.getPath().endsWith("]")) ? true : false ;
  }

  public boolean isEditable(String nodePath, Session session) throws Exception {
    Node node = (Node)session.getItem(nodePath);    
    return isEditable(node);
  }

  public boolean isEditable(Node node) throws Exception {
    String nodeType = node.getPrimaryNodeType().getName();
    for(String type:Utils.NON_EDITABLE_NODETYPES) {
      if(type.equalsIgnoreCase(nodeType)) return false;
    }       
    return true;
  }

  public boolean isVersionableOrAncestor(Node node) throws RepositoryException {
    if (Utils.isVersionable(node) || isAncestorVersionable(node)) return true;
    return false;
  }

  public String getVersionNumber(Node node) throws RepositoryException {
    if(!Utils.isVersionable(node)) return "-";
    return node.getBaseVersion().getName();
  }

  public boolean isAncestorVersionable(Node node) throws RepositoryException {
    int depth = node.getDepth() - 1;
    Node parent = (Node) node.getAncestor(depth);
    while (parent != null && depth != 0) {
      if (parent.isNodeType(Utils.MIX_VERSIONABLE)) return true;
      depth-- ;
      parent = (Node) node.getAncestor(depth);
    }
    return false;
  }

  private void removeMixins(Node node) throws Exception {
    NodeType[] mixins = node.getMixinNodeTypes() ;
    for(NodeType nodeType : mixins) {
      node.removeMixin(nodeType.getName()) ;
    }
  }

  public boolean isJcrViewEnable() throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    return uiExplorer.getPreference().isJcrEnable();    
  }

  public String getActionsList(Node node) throws Exception {
    if(node == null) return "" ;
    StringBuilder actionsList = new StringBuilder() ;        
    boolean isEditable = isEditable(node) ;    
    boolean isLocked = node.isLocked();
    boolean holdsLock = node.holdsLock();    
    boolean isSameNameSibling = isSameNameSibling(node) ;
    boolean isJcrEnable = isJcrViewEnable();
    boolean isVersionable = Utils.isVersionable(node) ;
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    if(isVersionableOrAncestor(node)) {
      if(node.isCheckedOut()) {
        if(isVersionable) actionsList.append("CheckIn") ;
        if(isEditable) actionsList.append(",EditDocument") ;
        if(holdsLock) actionsList.append(",Unlock") ;
        else if(!isLocked) actionsList.append(",Lock") ;
        if(!isSameNameSibling) {
          actionsList.append(",Copy") ;
          actionsList.append(",Cut") ;
        }
        actionsList.append(",Rename") ;
        if(isJcrEnable) actionsList.append(",Save") ;
        actionsList.append(",Delete") ;          
      } else {
        if(isVersionable) actionsList.append(",CheckOut") ;
        if(holdsLock) actionsList.append(",Unlock") ;
        else if(!isLocked) actionsList.append(",Lock") ;
        if(!isSameNameSibling) actionsList.append(",Copy") ;
        actionsList.append(",Rename") ;          
      }
    } else {
      if(isEditable) actionsList.append(",EditDocument") ;
      if(holdsLock) actionsList.append(",Unlock") ;
      else if(!isLocked) actionsList.append(",Lock") ;
      if(!isSameNameSibling) {
        actionsList.append(",Copy") ;
        actionsList.append(",Cut") ;
      }
      actionsList.append(",Rename") ;
      if(isJcrViewEnable()) actionsList.append(",Save") ;
      actionsList.append(",Delete") ;        
    }
    if(uiExplorer.getAllClipBoard().size() > 0) actionsList.append(",Paste") ;
    return actionsList.toString() ;
  }
  
  private boolean hasPermission(String userName, Value[] roles) throws Exception {
    ConversationRegistry conversationRegistry = getApplicationComponent(ConversationRegistry.class);
    if(SystemIdentity.SYSTEM.equalsIgnoreCase(userName)) {
      return true ;
    }
    Identity identity = conversationRegistry.getState(userName).getIdentity() ;
    if(identity == null) {
      return false ; 
    }        
    for (int i = 0; i < roles.length; i++) {
      String role = roles[i].getString();
      if("*".equalsIgnoreCase(role)) return true ;
      MembershipEntry membershipEntry = MembershipEntry.parse(role) ;
      if(identity.isMemberOf(membershipEntry)) {
        return true ;
      }
    }
    return false ;
  }

  public List<Node> getCustomActions(Node node) throws Exception {
    List<Node> safeActions = new ArrayList<Node>();
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    String userName = context.getRemoteUser() ;
    ActionServiceContainer actionContainer = getApplicationComponent(ActionServiceContainer.class) ;
    List<Node> unsafeActions = actionContainer.getCustomActionsNode(node, ActionServiceContainer.READ_PHASE);
    if(unsafeActions == null) return new ArrayList<Node>() ;
    for(Node actionNode : unsafeActions) {
      Value[] roles = actionNode.getProperty(Utils.EXO_ROLES).getValues();
      if (hasPermission(userName, roles)) safeActions.add(actionNode);
    }
    return safeActions;
  }

  @SuppressWarnings("unused")
  static  public class EditDocumentActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent() ;
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME) ;
      if(wsName == null) wsName = uiExplorer.getCurrentWorkspace() ;
      Session session = uiExplorer.getSessionByWorkspace(wsName) ;
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class) ;
      Node selectedNode = null;
      try {
        selectedNode = uiExplorer.getNodeByPath(nodePath, session) ;
      } catch(PathNotFoundException path) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
            null,ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } catch(AccessDeniedException ace) {
        uiApp.addMessage(new ApplicationMessage("UIDocumentInfo.msg.null-exception", 
            null,ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;        
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
        return;
      }
      Object[] arg = { nodePath } ;
      try{
        ((ExtendedNode) selectedNode).checkPermission(PermissionType.SET_PROPERTY);        
      }catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.has-not-edit-permission",null,ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      boolean isReferenced = false ;
      if(uiExplorer.nodeIsLocked(selectedNode)) {        
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
        return ;
      }      
      if(selectedNode.isNodeType(Utils.EXO_ACTION)) {
        UIActionContainer uiContainer = uiExplorer.createUIComponent(UIActionContainer.class, null, null) ;
        uiExplorer.setIsHidePopup(true) ;
        uiContainer.getChild(UIActionTypeForm.class).setRendered(false) ;
        UIActionForm uiActionForm = uiContainer.getChild(UIActionForm.class) ;
        uiActionForm.createNewAction(uiExplorer.getCurrentNode(), 
            selectedNode.getPrimaryNodeType().getName(), false) ;
        uiActionForm.setWorkspace(wsName) ;
        uiActionForm.setNodePath(nodePath) ;
        UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class) ;
        uiPopupAction.activate(uiContainer, 600, 550) ;
      } else {
        TemplateService tservice = uicomp.getApplicationComponent(TemplateService.class) ;
        String repository = uicomp.getAncestorOfType(UIJCRExplorer.class).getRepositoryName() ;
        List documentNodeType = tservice.getDocumentTemplates(repository) ;
        String nodeType = null ;
        if(selectedNode.hasProperty("exo:presentationType")) {
          nodeType = selectedNode.getProperty("exo:presentationType").getString() ;
        }else {
          nodeType = selectedNode.getPrimaryNodeType().getName() ;
        }
        if(documentNodeType.contains(nodeType)){
          UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class) ;
          UIDocumentFormController uiController = 
            event.getSource().createUIComponent(UIDocumentFormController.class, null, "EditFormController") ;
          UIDocumentForm uiDocumentForm = uiController.getChild(UIDocumentForm.class) ;
          uiDocumentForm.setTemplateNode(nodeType) ;
          uiDocumentForm.setRepositoryName(repository) ;
          uiDocumentForm.setWorkspace(wsName) ;
          uiDocumentForm.setNodePath(nodePath) ;
          uiDocumentForm.addNew(false) ;
          uiController.setRenderedChild(UIDocumentForm.class) ;
          uiPopupAction.activate(uiController, 800, 600) ;
        } else {          
          uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.not-support", arg, 
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          uiExplorer.updateAjax(event) ;
          return ;
        }
      }
    }
  }

  static  public class RenameActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent() ;
      String renameNodePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME) ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.setIsHidePopup(false) ;
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class) ;
      Session session = uiExplorer.getSessionByWorkspace(wsName) ;
      Node renameNode = null;
      try {
        renameNode = (Node)session.getItem(renameNodePath);
      } catch(PathNotFoundException path) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
            null,ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
        return;
      }
      try {
        ((ExtendedNode) renameNode).checkPermission(PermissionType.SET_PROPERTY);        
      } catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-rename-node",null,ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
        return ;
      }
      if(uiExplorer.nodeIsLocked(renameNode)) {
        Object[] arg = { renameNodePath } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;        
        return ;
      }      
      if(renameNode.isNodeType("mix:versionable")) {
        if(!renameNode.isCheckedOut()) {
          uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.is-checked-in", null, 
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          uiExplorer.updateAjax(event) ;
          return ;
        }
      }
      boolean isReferencedNode = false ;
      try {
        if(wsName != null) isReferencedNode = true ;
        UIControl uiControl = uiExplorer.getChild(UIControl.class) ;
        UIActionBar uiActionBar = uiControl.getChild(UIActionBar.class) ;
        UIRenameForm uiRenameForm = uiActionBar.createUIComponent(UIRenameForm.class, null, null) ;
        uiRenameForm.update(renameNode, isReferencedNode) ;
        UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class) ;
        uiPopupAction.activate(uiRenameForm, 700, 0) ;
        uiPopupAction.setRendered(true) ;
      } catch(Exception e) {
        JCRExceptionManager.process(uiApp, e);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
      }
    }
  }

  static  public class CopyActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent() ;
      String srcPath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME) ;      
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class) ;
      Session session = uiExplorer.getSessionByWorkspace(wsName) ;
      try {
        session.getItem(srcPath);
      } catch(PathNotFoundException path) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
            null,ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
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
        clipboard.setWorkspace(wsName) ;
        uiExplorer.getAllClipBoard().add(clipboard) ;                      
        if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save() ;
        uiExplorer.updateAjax(event) ;
      } catch(ConstraintViolationException cons) {
        uiExplorer.getSession().refresh(false) ;
        uiExplorer.refreshExplorer() ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.constraintviolation-exception", 
            null,ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
        return ;              
      } catch(Exception e) {
        e.printStackTrace() ;
        JCRExceptionManager.process(uiApp, e);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
      }
    }
  }

  static  public class CutActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class) ;
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME) ;
      Session session = uiExplorer.getSessionByWorkspace(wsName) ;
      Node selectedNode = null;
      try {
        selectedNode = (Node)session.getItem(nodePath);
      } catch(ConstraintViolationException cons) {
        uiExplorer.getSession().refresh(false) ;
        uiExplorer.refreshExplorer() ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.constraintviolation-exception", 
            null,ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
        return ;        
      } catch(PathNotFoundException path) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
            null,ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } catch (Exception e) {
        e.printStackTrace() ;
        JCRExceptionManager.process(uiApp, e);
        return;
      }
      try{
        ((ExtendedNode) selectedNode).checkPermission(PermissionType.REMOVE);        
      }catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-cut-node",null,ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
        return ;
      }      
      if(uiExplorer.nodeIsLocked(selectedNode)) {        
        Object[] arg = { nodePath } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
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
        clipboard.setWorkspace(wsName) ;
        uiExplorer.getAllClipBoard().add(clipboard) ;                     
        if(!uiExplorer.getPreference().isJcrEnable()) session.save() ;
        uiExplorer.updateAjax(event) ;
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
    }
  }

  static  public class SaveActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME) ;
      Session session = uiExplorer.getSessionByWorkspace(wsName) ;
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class) ;
      try {
        Node node = uiExplorer.getNodeByPath(nodePath, session) ;
        Object[] args = { nodePath };
        if(node.isNew()) {
          uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.unable-save-node",args, 
              ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          uiExplorer.updateAjax(event) ;
          return ;
        }
        node.save(); 
        session.save() ;
        uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.save-node-success", args));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      } catch(PathNotFoundException path) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
            null,ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } catch(Exception e) {
        JCRExceptionManager.process(uiApp, e);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
      }
    }
  }

  static  public class DeleteActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
//    TODO: Need review this method with remove record.
      UIWorkingArea uicomp = event.getSource().getParent() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME) ;
      Session session = uiExplorer.getSessionByWorkspace(wsName) ;
      UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class) ;
      Node node = null;
      try {
        node = (Node)session.getItem(nodePath);
        String lockToken = Utils.getLockToken(node);
        if(lockToken != null) session.addLockToken(lockToken);
      } catch(PathNotFoundException path) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
            null,ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;      
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
        return;
      }
      try{
        ((ExtendedNode) node).checkPermission(PermissionType.REMOVE);        
      }catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-remove-node",null,ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(uiExplorer.nodeIsLocked(node)) {
        Object[] arg = { nodePath } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
        return ;
      }      
      Node parentNode = node.getParent() ;
      if(parentNode.isLocked()) {
        String lockToken1 = Utils.getLockToken(parentNode);
        session.addLockToken(lockToken1) ;
      }
      try {
        if(node.isNodeType(Utils.RMA_RECORD)) uicomp.removeMixins(node) ;
        node.remove() ;
        parentNode.save() ;
      } catch(VersionException ve) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.remove-verion-exception", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
        return ;    
      } catch(ReferentialIntegrityException ref) {
        uiExplorer.getSession().refresh(false) ;
        uiExplorer.refreshExplorer() ;
        uicomp.removeMixins(node) ;
        if(node.hasNodes()) {
          NodeIterator nodeIter = node.getNodes() ;
          while(nodeIter.hasNext()) {
            Node child = nodeIter.nextNode() ;
            uicomp.removeMixins(child) ;
          }
        }
        try {
          node.remove() ;
          parentNode.save() ;
        } catch(Exception eee) {
          uiExplorer.getSession().refresh(false) ;
          uiExplorer.refreshExplorer() ;
          uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.remove-referentialIntegrityException", 
              null,ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          uiExplorer.updateAjax(event) ;
          return ; 
        }
        uiExplorer.setSelectNode(parentNode) ;
        uiExplorer.updateAjax(event) ;
      } catch(ConstraintViolationException cons) {
        uiExplorer.getSession().refresh(false) ;
        uiExplorer.refreshExplorer() ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.constraintviolation-exception", 
            null,ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
        return ;        
      } catch(LockException le) {
        Object[] arg = { nodePath } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
        return ;
      } catch(Exception e) {  
        e.printStackTrace() ;
        JCRExceptionManager.process(uiApp, e) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.getSession().refresh(false) ;
        uiExplorer.refreshExplorer() ;
      }
      uiExplorer.setSelectNode(parentNode) ;
      uiExplorer.updateAjax(event) ;
      if(!uiExplorer.getPreference().isJcrEnable()) session.save() ;        
    }
  }

  static  public class LockActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME) ;
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class) ;
      Session session = uiExplorer.getSessionByWorkspace(wsName);
      Node node = null;
      try {
        node = (Node)session.getItem(nodePath);
      } catch(PathNotFoundException path) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
            null,ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
        return;
      }
      try{
        ((ExtendedNode) node).checkPermission(PermissionType.SET_PROPERTY);        
      }catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-lock-node",null,ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
        return ;
      }
      if(!node.isCheckedOut()){
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.is-checked-in-lock", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
        return ;
      }
      if(node.canAddMixin(Utils.MIX_LOCKABLE)){
        node.addMixin(Utils.MIX_LOCKABLE);
        node.save();
      }
      try {
        Lock lock = node.lock(false, false);    
        Utils.keepLock(lock);
      } catch(LockException le) {        
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.cant-lock", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
        return ;
      } catch (Exception e) {
        e.printStackTrace() ;
        JCRExceptionManager.process(uiApp, e);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;        
      }
    }
  }

  static  public class UnlockActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME) ;      
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class) ;
      Session session = uiExplorer.getSessionByWorkspace(wsName);
      Node node = null;
      try {
        node = (Node)session.getItem(nodePath);
      } catch(PathNotFoundException path) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
            null,ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } catch (Exception e) {
        e.printStackTrace();
        JCRExceptionManager.process(uiApp, e);
        return;
      }
      try{
        ((ExtendedNode) node).checkPermission(PermissionType.SET_PROPERTY);        
      }catch (Exception e) {        
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-unlock-node",null,ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
        return ;
      }
      try {
        if(node.holdsLock())  {           
          String lockToken = Utils.getLockToken(node);
          if(lockToken != null) {
            session.addLockToken(lockToken);
          }
          node.unlock() ;          
        }
      } catch(LockException le) {        
        Object[] args = {node.getName()} ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-unlock-node", args, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
        return ;
      } catch (Exception e) { 
        e.printStackTrace();
        JCRExceptionManager.process(uiApp, e);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
      }
    }
  }

  static  public class CheckInActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME) ;
      Session session = uiExplorer.getSessionByWorkspace(wsName) ;
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class) ;         
      try {
        Node node = uiExplorer.getNodeByPath(nodePath, session);
        if(uiExplorer.nodeIsLocked(node)) {
          Object[] arg = { nodePath } ;
          uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;     
          return ;
        }
        Node parentNode = node.getParent() ;
        if(parentNode.isLocked()) {
          String lockToken1 = Utils.getLockToken(parentNode);
          session.addLockToken(lockToken1) ;
        }
        node.checkin();
      } catch(PathNotFoundException path) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
            null,ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
      }
    }
  }

  static  public class CheckOutActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME) ;
      Session session = uiExplorer.getSessionByWorkspace(wsName) ;      
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class) ;       
      try {
        Node node = uiExplorer.getNodeByPath(nodePath, session) ;
        if(uiExplorer.nodeIsLocked(node)) {
          Object[] arg = { nodePath } ;
          uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;        
          return ;
        }   
        node.checkout();
      } catch(PathNotFoundException path) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
            null,ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
      }
    }
  }

  static  public class CustomActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent() ;
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String actionName = event.getRequestContext().getRequestParameter("actionName") ;
      String repository = uicomp.getAncestorOfType(UIJCRExplorer.class).getRepositoryName() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME) ;
      Session session = uiExplorer.getSessionByWorkspace(wsName) ;
      ActionServiceContainer actionService = 
        uicomp.getApplicationComponent(ActionServiceContainer.class) ;
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class) ;
      try {
        Node node = uicomp.getAncestorOfType(UIJCRExplorer.class).getNodeByPath(nodePath, session);
        String userId = event.getRequestContext().getRemoteUser() ;
        actionService.executeAction(userId, node, actionName,repository);
        Object[] arg = { actionName } ;
        uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.execute-successfully", arg)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
      } catch (Exception e) {
        e.printStackTrace() ;
        JCRExceptionManager.process(uiApp, e);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
      }
    }
  }

  static public class PasteActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent() ;
      UIJCRExplorer uiExplorer = uiWorkingArea.getAncestorOfType(UIJCRExplorer.class) ;
      String destPath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String realDestPath = destPath ;
      Session session = uiExplorer.getSession() ;
      UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class) ;
      if(uiExplorer.getAllClipBoard().size()<1) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.no-node", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      }
      Node destNode = null;
      try {
        destNode = (Node)session.getItem(destPath);
      } catch(PathNotFoundException path) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
            null,ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
        return;
      }
      if(!Utils.isAddNodeAuthorized(destNode)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-paste-node", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
        return;
      }
      ClipboardCommand currentClipboard = uiExplorer.getAllClipBoard().getLast() ;
      String srcPath = currentClipboard.getSrcPath() ;
      String type = currentClipboard.getType();
      String srcWorkspace = currentClipboard.getWorkspace() ;
      if(srcWorkspace == null) srcWorkspace = uiExplorer.getCurrentWorkspace() ;
      if(ClipboardCommand.CUT.equals(type) && srcPath.equals(realDestPath)) { 
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-cutting", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;        
        return; 
      }
      if(uiExplorer.nodeIsLocked(destNode)) {
        Object[] arg = { destPath } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;        
        return ;
      }
      if(destPath.endsWith("/")) {
        destPath = destPath + srcPath.substring(srcPath.lastIndexOf("/") + 1) ;
      } else {
        destPath = destPath + srcPath.substring(srcPath.lastIndexOf("/")) ;
      }
      ActionServiceContainer actionContainer = 
        event.getSource().getApplicationComponent(ActionServiceContainer.class) ;
      try {
        if(ClipboardCommand.COPY.equals(type)) {
          pasteByCopy(session, srcPath, destPath) ;
          Node selectedNode = (Node)session.getItem(destPath) ;
          actionContainer.initiateObservation(selectedNode, uiExplorer.getRepositoryName()) ;
        } else {
          if(!srcPath.equals(destPath)) {
            pasteByCut(uiExplorer, session, srcPath, destPath, actionContainer, uiExplorer.getRepositoryName()) ;
          }
        }
      } catch(ConstraintViolationException ce) {       
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.current-node-not-allow-paste", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
        return ;
      } catch(VersionException ve) {       
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.copied-node-in-versioning", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
        return ;
      } catch (ItemExistsException iee){
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.paste-node-same-name", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
        return ;
      } catch (LoginException e){
        if(ClipboardCommand.CUT.equals(type)) {
          uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.cannot-login-node", null, 
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          uiExplorer.updateAjax(event) ;
          return ;
        }
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.cannot-paste-nodetype", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
        return ;
      } catch(AccessDeniedException ace) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.access-denied", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
        return ;             
      } catch(LockException locke){      
        Object[] arg = { srcPath } ;
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.paste-lock-exception", arg, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());       
      } catch(Exception e) {
        JCRExceptionManager.process(uiApp, e);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
        return ;
      }
      if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save() ;
      uiExplorer.updateAjax(event) ;
    }

    private void pasteByCopy(Session session, String srcPath, String destPath) throws Exception {
      Workspace workspace = session.getWorkspace();           
      workspace.copy(srcPath, destPath);
      Node destNode = (Node) session.getItem(destPath) ;
      removeReferences(destNode) ;
    }

    private void pasteByCut(UIJCRExplorer uiExplorer, Session session, String srcPath, 
        String destPath, ActionServiceContainer actionContainer, String repository) throws Exception {
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
            if(refNode.hasProperty(RELATION_PROP)) {
              relationsService.removeRelation(refNode, srcPath, uiExplorer.getRepositoryName()) ;
              refNode.save() ;
              refList.add(refNode) ;
            }
          }
        }
      }
      Workspace workspace = session.getWorkspace();
      try {
        workspace.move(srcPath, destPath);
      } catch(ArrayIndexOutOfBoundsException e) {
        throw new MessageException(new ApplicationMessage("UIPopupMenu.msg.bound-exception", null, 
            ApplicationMessage.WARNING)) ;
      }
      session.save() ;
      Node desNode = (Node)session.getItem(destPath) ;
      actionContainer.initiateObservation(desNode, repository) ;
      for(int i = 0; i < refList.size(); i ++) {
        Node addRef = refList.get(i) ;
        relationsService.addRelation(addRef, destPath,session.getWorkspace().getName(),uiExplorer.getRepositoryName()) ;
        addRef.save() ;
      }      
//      for(ClipboardCommand currClip : uiExplorer.getAllClipBoard()) {
//        if(currClip.getSrcPath().equals(srcPath)) {
//          uiExplorer.getAllClipBoard().remove(currClip) ;
//          break ;
//        }
//      }
      uiExplorer.getAllClipBoard().clear();
      String currentPath = uiExplorer.getCurrentPath() ;
      if(srcPath.equals(currentPath) || currentPath.startsWith(srcPath)) {
        uiExplorer.setCurrentPath(srcNode.getParent().getPath()) ;
      }
    }

    private void removeReferences(Node destNode) throws Exception {
      NodeType[] mixinTypes = destNode.getMixinNodeTypes() ;
      Session session = destNode.getSession();
      for(int i = 0; i < mixinTypes.length; i ++) {
        if(mixinTypes[i].getName().equals(Utils.EXO_CATEGORIZED) && destNode.hasProperty(Utils.EXO_CATEGORIZED)) {
          Node valueNode = null ;
          Value valueAdd = session.getValueFactory().createValue(valueNode);
          destNode.setProperty(Utils.EXO_CATEGORIZED, new Value[] {valueAdd}) ;            
        }            
      }
      destNode.save() ;      
    }
  }
}