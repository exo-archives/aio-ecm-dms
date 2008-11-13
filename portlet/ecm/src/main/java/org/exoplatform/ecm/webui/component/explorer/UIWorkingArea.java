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
package org.exoplatform.ecm.webui.component.explorer;

import java.util.ArrayList;
import java.util.LinkedList;
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

import org.exoplatform.ecm.jcr.model.ClipboardCommand;
import org.exoplatform.ecm.webui.component.explorer.control.UIActionBar;
import org.exoplatform.ecm.webui.component.explorer.control.UIControl;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIDocumentForm;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIDocumentFormController;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIFolderForm;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIRenameForm;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionContainer;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionForm;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionTypeForm;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UISideBar;
import org.exoplatform.ecm.webui.component.explorer.upload.UIUploadManager;
import org.exoplatform.ecm.webui.popup.UIPopupContainer;
import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
import org.exoplatform.ecm.webui.utils.LockUtil;
import org.exoplatform.ecm.webui.utils.PermissionUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.relations.RelationsService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
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
        @EventConfig(listeners = UIWorkingArea.DeleteActionListener.class),
        @EventConfig(listeners = UIWorkingArea.LockActionListener.class),
        @EventConfig(listeners = UIWorkingArea.UnlockActionListener.class),
        @EventConfig(listeners = UIWorkingArea.CheckInActionListener.class),
        @EventConfig(listeners = UIWorkingArea.CheckOutActionListener.class),
        @EventConfig(listeners = UIWorkingArea.RenameActionListener.class),
        @EventConfig(listeners = UIWorkingArea.CustomActionListener.class),
        @EventConfig(listeners = UIWorkingArea.PasteActionListener.class),
        @EventConfig(listeners = UIWorkingArea.AddFolderActionListener.class),
        @EventConfig(listeners = UIWorkingArea.AddDocumentActionListener.class),
        @EventConfig(listeners = UIWorkingArea.UploadActionListener.class),
        @EventConfig(listeners = UIWorkingArea.MoveNodeActionListener.class, confirm="UIWorkingArea.msg.confirm-move")
      }
  )
})

public class UIWorkingArea extends UIContainer {

  final static private String RELATION_PROP = "exo:relation";
  final static public String WS_NAME = "workspaceName";
  
  private boolean isMultiSelect_ = false;
  private LinkedList<ClipboardCommand> virtualClipboards_ = new LinkedList<ClipboardCommand>();
  private int pasteNum_;
  private boolean isLastPaste_ = false;

  public UIWorkingArea() throws Exception {
    addChild(UIRightClickPopupMenu.class, "ECMContextMenu", null);
    addChild(UISideBar.class, null, null);
    addChild(UIDocumentWorkspace.class, null, null);
  }

  public boolean isShowSideBar() throws Exception {
    UIJCRExplorer jcrExplorer = getParent();
    return jcrExplorer.getPreference().isShowSideBar();
  }

  public void setShowSideBar(boolean b) throws Exception {
    UIJCRExplorer jcrExplorer = getParent();
    jcrExplorer.getPreference().setShowSideBar(b);
  }

  public Node getNodeByUUID(String uuid) throws Exception{    
    String repository = getAncestorOfType(UIJCRExplorer.class).getRepositoryName();    
    ManageableRepository repo = getApplicationComponent(RepositoryService.class).getRepository(repository);
    String workspace = repo.getConfiguration().getDefaultWorkspaceName();
    Session session = SessionProviderFactory.createSystemProvider().getSession(workspace,repo);    
    return session.getNodeByUUID(uuid);
  }

  public boolean isReferenceableNode(Node node) throws Exception {
    NodeType[] nodeTypes = node.getMixinNodeTypes();
    for(NodeType type:nodeTypes) {      
      if(type.getName().equals(Utils.MIX_REFERENCEABLE)) return true;
    }
    return false;
  }

  public boolean isPreferenceNode(Node node) {
    return getAncestorOfType(UIJCRExplorer.class).isPreferenceNode(node);
  }

  public boolean isSameNameSibling(Node node) throws Exception {
    return (node.getPath().endsWith("]")) ? true : false;
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
      depth--;
      parent = (Node) node.getAncestor(depth);
    }
    return false;
  }

  private void removeMixins(Node node) throws Exception {
    NodeType[] mixins = node.getMixinNodeTypes();
    for(NodeType nodeType : mixins) {
      node.removeMixin(nodeType.getName());
    }
  }

  public boolean isJcrViewEnable() throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    return uiExplorer.getPreference().isJcrEnable();    
  }

  public String getActionsList(Node node) throws Exception {
    if(node == null) return "";
    node.refresh(true);
    StringBuilder actionsList = new StringBuilder();        
    boolean isEditable = isEditable(node);    
    boolean isLocked = node.isLocked();
    boolean holdsLock = node.holdsLock();    
    boolean isSameNameSibling = isSameNameSibling(node);
    boolean isJcrEnable = isJcrViewEnable();
    boolean isVersionable = Utils.isVersionable(node);
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    if(isVersionableOrAncestor(node)) {
      if(node.isCheckedOut()) {
        if(isVersionable) actionsList.append("CheckIn");
        if(isEditable) actionsList.append(",EditDocument");
        if(!isSameNameSibling) {
          if(holdsLock) actionsList.append(",Unlock");
          else if(!isLocked) actionsList.append(",Lock");
        }
        if(!isSameNameSibling) {
          actionsList.append(",Copy");
          actionsList.append(",Cut");
        }
        actionsList.append(",Rename");
        if(isJcrEnable) actionsList.append(",Save");
        actionsList.append(",Delete");
      } else {
        if(isVersionable) actionsList.append(",CheckOut");
        if(!isSameNameSibling) {
          if(holdsLock) actionsList.append(",Unlock");
          else if(!isLocked) actionsList.append(",Lock");
        }
        if(!isSameNameSibling) actionsList.append(",Copy");
        actionsList.append(",Rename");
      }
    } else {
      if(isEditable) actionsList.append(",EditDocument");
      if(!isSameNameSibling) {
        if(holdsLock) {
          actionsList.append(",Unlock");
        } else if(!isLocked) {
          actionsList.append(",Lock");
        }
      }
      if(!isSameNameSibling) {
        actionsList.append(",Copy");
        actionsList.append(",Cut");
      }
      actionsList.append(",Rename");
      if(isJcrViewEnable()) actionsList.append(",Save");
      actionsList.append(",Delete");
    }
    if(uiExplorer.getAllClipBoard().size() > 0) actionsList.append(",Paste");
    return actionsList.toString();
  }
  
  private boolean hasPermission(String userName, Value[] roles) throws Exception {
    IdentityRegistry identityRegistry = getApplicationComponent(IdentityRegistry.class);
    if(SystemIdentity.SYSTEM.equalsIgnoreCase(userName)) {
      return true;
    }
    Identity identity = identityRegistry.getIdentity(userName);
    if(identity == null) {
      return false; 
    }        
    for (int i = 0; i < roles.length; i++) {
      String role = roles[i].getString();
      if("*".equalsIgnoreCase(role)) return true;
      MembershipEntry membershipEntry = MembershipEntry.parse(role);
      if(membershipEntry == null) return false;
      if(identity.isMemberOf(membershipEntry)) {
        return true;
      }
    }
    return false;
  }

  public List<Node> getCustomActions(Node node) throws Exception {
    List<Node> safeActions = new ArrayList<Node>();
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    String userName = context.getRemoteUser();
    ActionServiceContainer actionContainer = getApplicationComponent(ActionServiceContainer.class);
    List<Node> unsafeActions = actionContainer.getCustomActionsNode(node, ActionServiceContainer.READ_PHASE);
    if(unsafeActions == null) return new ArrayList<Node>();
    for(Node actionNode : unsafeActions) {
      Value[] roles = actionNode.getProperty(Utils.EXO_ROLES).getValues();
      if (hasPermission(userName, roles)) safeActions.add(actionNode);
    }
    return safeActions;
  }
  
  private void multipleCopy(String[] srcPaths, String[] wsNames, Event event) throws Exception {
    for(int i=0; i< srcPaths.length; i++) {
      processCopy(srcPaths[i], wsNames[i], event);
    }
  }
  
  private void processCopy(String srcPath, String wsName, Event event) throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    UIApplication uiApp = getAncestorOfType(UIApplication.class);
    Session session = uiExplorer.getSessionByWorkspace(wsName);
    try {
      session.getItem(srcPath);
    } catch(PathNotFoundException path) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
          null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    } 
    try {
      List<ClipboardCommand> clipboards = uiExplorer.getAllClipBoard();
      for(ClipboardCommand command:clipboards) {
        if(command.getSrcPath().equals(srcPath)) {
          clipboards.remove(command);
          break;
        }
      }
      ClipboardCommand clipboard = new ClipboardCommand();
      clipboard.setType(ClipboardCommand.COPY);
      clipboard.setSrcPath(srcPath);
      clipboard.setWorkspace(wsName);
      uiExplorer.getAllClipBoard().add(clipboard);
      if(isMultiSelect_) virtualClipboards_.add(clipboard);
      if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save();
      uiExplorer.updateAjax(event);
    } catch(ConstraintViolationException cons) {
      uiExplorer.getSession().refresh(false);
      uiExplorer.refreshExplorer();
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.constraintviolation-exception", 
          null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;              
    } catch(Exception e) {
      e.printStackTrace();
      JCRExceptionManager.process(uiApp, e);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
    }
  }
  
  private void processPasteMultiple(String destPath, Event event) throws Exception {
    pasteNum_ = 0;
    isLastPaste_ = false;
    for(ClipboardCommand clipboard : virtualClipboards_) {
      pasteNum_++;
      if(pasteNum_ == virtualClipboards_.size()) {
        isLastPaste_ = true;
        processPaste(clipboard, destPath, event);
        break;
      }
      processPaste(clipboard, destPath, event);
    }
  }
  
  private void processPaste(ClipboardCommand currentClipboard, String destPath, Event event) throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
    String srcPath = currentClipboard.getSrcPath();
    String type = currentClipboard.getType();
    String srcWorkspace = currentClipboard.getWorkspace();
    if(srcWorkspace == null) srcWorkspace = uiExplorer.getCurrentWorkspace();
    Session session = uiExplorer.getSession();
    if(ClipboardCommand.CUT.equals(type) && srcPath.equals(destPath)) { 
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-cutting", null, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());        
      return; 
    }
    if(destPath.endsWith("/")) {
      destPath = destPath + srcPath.substring(srcPath.lastIndexOf("/") + 1);
    } else {
      destPath = destPath + srcPath.substring(srcPath.lastIndexOf("/"));
    }
    ActionServiceContainer actionContainer = getApplicationComponent(ActionServiceContainer.class);
    try {
      if(ClipboardCommand.COPY.equals(type)) {
        pasteByCopy(session, srcPath, destPath);
        Node selectedNode = (Node)session.getItem(destPath);
        actionContainer.initiateObservation(selectedNode, uiExplorer.getRepositoryName());
      } else {
        if(!srcPath.equals(destPath)) {
          pasteByCut(uiExplorer, session, srcPath, destPath, actionContainer, uiExplorer.getRepositoryName());
        }
      }
    } catch(ConstraintViolationException ce) {       
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.current-node-not-allow-paste", null, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
    } catch(VersionException ve) {       
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.copied-node-in-versioning", null, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
    } catch (ItemExistsException iee){
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.paste-node-same-name", null, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
    } catch (LoginException e){
      if(ClipboardCommand.CUT.equals(type)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.cannot-login-node", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiExplorer.updateAjax(event);
        return;
      }
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.cannot-paste-nodetype", null, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
    } catch(AccessDeniedException ace) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.access-denied", null, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;             
    } catch(LockException locke){      
      Object[] arg = { srcPath };
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.paste-lock-exception", arg, ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());       
    } catch(Exception e) {
      JCRExceptionManager.process(uiApp, e);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
    }
  }
  
  private void pasteByCopy(Session session, String srcPath, String destPath) throws Exception {
    Workspace workspace = session.getWorkspace();           
    workspace.copy(srcPath, destPath);
    Node destNode = (Node) session.getItem(destPath);
    removeReferences(destNode);
  }

  private void pasteByCut(UIJCRExplorer uiExplorer, Session session, String srcPath, 
      String destPath, ActionServiceContainer actionContainer, String repository) throws Exception {
    RelationsService relationsService = 
      uiExplorer.getApplicationComponent(RelationsService.class);
    List<Node> refList = new ArrayList<Node>();
    boolean isReference = false;
    PropertyIterator references = null;
    Node srcNode = (Node)uiExplorer.getSession().getItem(srcPath);
    try {
      references = srcNode.getReferences();
      isReference = true;
    } catch(Exception e) {
      isReference = false;
    }  
    if(isReference && references != null) {
      if(references.getSize() > 0 ) {
        while(references.hasNext()) {
          Property pro = references.nextProperty();
          Node refNode = pro.getParent();
          if(refNode.hasProperty(RELATION_PROP)) {
            relationsService.removeRelation(refNode, srcPath, uiExplorer.getRepositoryName());
            refNode.save();
            refList.add(refNode);
          }
        }
      }
    }
    Workspace workspace = session.getWorkspace();
    try {
      workspace.move(srcPath, destPath);
    } catch(ArrayIndexOutOfBoundsException e) {
      throw new MessageException(new ApplicationMessage("UIPopupMenu.msg.bound-exception", null, 
          ApplicationMessage.WARNING));
    }
    session.save();
    if(!isMultiSelect_ || (isMultiSelect_ && isLastPaste_)) {
      Node desNode = (Node)session.getItem(destPath);
      actionContainer.initiateObservation(desNode, repository);
      for(int i = 0; i < refList.size(); i ++) {
        Node addRef = refList.get(i);
        relationsService.addRelation(addRef, destPath,session.getWorkspace().getName(),uiExplorer.getRepositoryName());
        addRef.save();
      }      
      uiExplorer.getAllClipBoard().clear();
      virtualClipboards_.clear();
      String currentPath = uiExplorer.getCurrentPath();
      if(srcPath.equals(currentPath) || currentPath.startsWith(srcPath)) {
        uiExplorer.setCurrentPath(srcNode.getParent().getPath());
      }
    }
  }
  
  private void removeReferences(Node destNode) throws Exception {
    NodeType[] mixinTypes = destNode.getMixinNodeTypes();
    Session session = destNode.getSession();
    for(int i = 0; i < mixinTypes.length; i ++) {
      if(mixinTypes[i].getName().equals(Utils.EXO_CATEGORIZED) && destNode.hasProperty(Utils.EXO_CATEGORIZED)) {
        Node valueNode = null;
        Value valueAdd = session.getValueFactory().createValue(valueNode);
        destNode.setProperty(Utils.EXO_CATEGORIZED, new Value[] {valueAdd});            
      }            
    }
    destNode.save();      
  }
  
  private void processRemoveMultiple(String[] nodePaths, String[] wsNames, Event event) throws Exception {
    for(int i=0; i< nodePaths.length; i++) {
      processRemove(nodePaths[i], wsNames[i], event);
    }
  }
  
  private void processRemove(String nodePath, String wsName, Event event) throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    Session session = uiExplorer.getSessionByWorkspace(wsName);
    UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
    Node node = null;
    try {
      node = (Node)session.getItem(nodePath);
      String lockToken = LockUtil.getLockToken(node);
      if(lockToken != null) session.addLockToken(lockToken);
    } catch(PathNotFoundException path) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
          null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;      
    } catch (Exception e) {
      JCRExceptionManager.process(uiApp, e);
      return;
    }
    try{
      ((ExtendedNode) node).checkPermission(PermissionType.REMOVE);        
    }catch (Exception e) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-remove-node",null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    }
    if(uiExplorer.nodeIsLocked(node)) {
      Object[] arg = { nodePath };
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
    }      
    Node parentNode = node.getParent();
    if(parentNode.isLocked()) {
      String lockToken1 = LockUtil.getLockToken(parentNode);
      session.addLockToken(lockToken1);
    }
    try {
      if(node.isNodeType(Utils.RMA_RECORD)) removeMixins(node);      
      node.remove();
      parentNode.save();
    } catch(VersionException ve) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.remove-verion-exception", null,
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;    
    } catch(ReferentialIntegrityException ref) {
      uiExplorer.getSession().refresh(false);
      uiExplorer.refreshExplorer();
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.remove-referentialIntegrityException", 
          null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
      /*
      uiExplorer.getSession().refresh(false);
      uiExplorer.refreshExplorer();
      removeMixins(node);
      if(node.hasNodes()) {
        NodeIterator nodeIter = node.getNodes();
        while(nodeIter.hasNext()) {
          Node child = nodeIter.nextNode();
          removeMixins(child);
        }
      }
      try {
        node.remove();
        parentNode.save();
      } catch(Exception eee) {
        uiExplorer.getSession().refresh(false);
        uiExplorer.refreshExplorer();
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.remove-referentialIntegrityException", 
            null,ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiExplorer.updateAjax(event);
        return; 
      }
      uiExplorer.setSelectNode(parentNode);
      uiExplorer.updateAjax(event);
      */
    } catch(ConstraintViolationException cons) {
      uiExplorer.getSession().refresh(false);
      uiExplorer.refreshExplorer();
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.constraintviolation-exception", 
          null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;        
    } catch(Exception e) {  
      e.printStackTrace();
      JCRExceptionManager.process(uiApp, e);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    }
    if(!isMultiSelect_) uiExplorer.setSelectNode(parentNode);
  }
  
  private void processCut(String nodePath, String wsName, Event event) throws Exception {
    UIJCRExplorer uiExplorer = getParent();
    UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
    Session session = uiExplorer.getSessionByWorkspace(wsName);
    Node selectedNode = null;
    try {
      selectedNode = (Node)session.getItem(nodePath);
    } catch(ConstraintViolationException cons) {
      uiExplorer.getSession().refresh(false);
      uiExplorer.refreshExplorer();
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.constraintviolation-exception", 
          null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;        
    } catch(PathNotFoundException path) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
          null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    } catch (Exception e) {
      e.printStackTrace();
      JCRExceptionManager.process(uiApp, e);
      return;
    }
    try{
      ((ExtendedNode) selectedNode).checkPermission(PermissionType.REMOVE);        
    }catch (Exception e) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-cut-node",null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
    }      
    if(uiExplorer.nodeIsLocked(selectedNode)) {        
      Object[] arg = { nodePath };
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
    }      
    try {
      List<ClipboardCommand> clipboards = uiExplorer.getAllClipBoard();
      for(ClipboardCommand command:clipboards) {
        if(command.getSrcPath().equals(nodePath)) {
          clipboards.remove(command);
          break;
        }
      }       
      ClipboardCommand clipboard = new ClipboardCommand();
      clipboard.setType(ClipboardCommand.CUT);
      clipboard.setSrcPath(nodePath);
      clipboard.setWorkspace(wsName);
      uiExplorer.getAllClipBoard().add(clipboard);
      if(isMultiSelect_) {
        virtualClipboards_.add(clipboard);
      } else {
        if(!uiExplorer.getPreference().isJcrEnable()) session.save();
        uiExplorer.updateAjax(event);
      }
    } catch (Exception e) {
      JCRExceptionManager.process(uiApp, e);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
    }
  }
  
  private void processMultipleCut(String[] nodePaths, String[] wsNames, Event event) throws Exception {
    UIJCRExplorer uiExplorer = getParent();
    for(int i=0; i< nodePaths.length; i++) {
      processCut(nodePaths[i], wsNames[i], event);
    }
    if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save();
    uiExplorer.updateAjax(event);
  }
  
  public void doDelete(String nodePath, String wsName, Event event) throws Exception {
    UIJCRExplorer uiExplorer = getParent();
    if(nodePath.indexOf(";") > -1) {
      isMultiSelect_ = true;
      processRemoveMultiple(nodePath.split(";"), wsName.split(";"), event);
    } else {
      isMultiSelect_ = false;
      processRemove(nodePath, wsName, event);
    }
    uiExplorer.updateAjax(event);
    if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save(); 
  }
  
  private void processMultiLock(String[] nodePaths, String[] wsNames, Event event) throws Exception {
    UIJCRExplorer uiExplorer = getParent();
    for(int i=0; i< nodePaths.length; i++) {
      processLock(nodePaths[i], wsNames[i], event);
    }
    if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save();
    uiExplorer.updateAjax(event);
  }
  
  private void processLock(String nodePath, String wsName, Event event) throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
    Session session = uiExplorer.getSessionByWorkspace(wsName);
    Node node = null;
    try {
      node = (Node)session.getItem(nodePath);
    } catch(PathNotFoundException path) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
          null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    } catch (Exception e) {
      JCRExceptionManager.process(uiApp, e);
      return;
    }
    try{
      ((ExtendedNode) node).checkPermission(PermissionType.SET_PROPERTY);        
    }catch (Exception e) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-lock-node",null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
    }
    if(!node.isCheckedOut()){
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.is-checked-in-lock", null, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
    }
    if(node.canAddMixin(Utils.MIX_LOCKABLE)){
      node.addMixin(Utils.MIX_LOCKABLE);
      node.save();
    }
    try {
      Lock lock = node.lock(false, false);
      LockUtil.keepLock(lock);
    } catch(LockException le) {
      le.printStackTrace();
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.cant-lock", null, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
    } catch (Exception e) {
      e.printStackTrace();
      JCRExceptionManager.process(uiApp, e);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);        
    }
  }
  
  private void processMultiUnlock(String[] nodePaths, String[] wsNames, Event event) throws Exception {
    UIJCRExplorer uiExplorer = getParent();
    for(int i=0; i< nodePaths.length; i++) {
      processUnlock(nodePaths[i], wsNames[i], event);
    }
    if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save();
    uiExplorer.updateAjax(event);
  }
  
  private void processUnlock(String nodePath, String wsName, Event event) throws Exception {
    UIJCRExplorer uiExplorer = getParent();
    UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
    Session session = uiExplorer.getSessionByWorkspace(wsName);
    Node node = null;
    try {
      node = (Node)session.getItem(nodePath);
    } catch(PathNotFoundException path) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
          null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    } catch (Exception e) {
      JCRExceptionManager.process(uiApp, e);
      return;
    }    
    try{
      ((ExtendedNode) node).checkPermission(PermissionType.SET_PROPERTY);        
    }catch (Exception e) {        
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-unlock-node",null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
    }    
    try {
      if(node.holdsLock()) {
        String lockToken = LockUtil.getLockToken(node);        
        if(lockToken != null) {
          session.addLockToken(lockToken);
        }
        node.unlock();        
      }
    } catch(LockException le) {
      Object[] args = {node.getName()};
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-unlock-node", args, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
    } catch (Exception e) {
      e.printStackTrace();
      JCRExceptionManager.process(uiApp, e);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
    }    
  }
  
  private void moveNode(String srcPath, String wsName, String destPath, Event event) throws Exception {
    UIJCRExplorer uiExplorer = getParent();
    Workspace workspace = uiExplorer.getSession().getWorkspace();
    Node selectedNode = uiExplorer.getNodeByPath(srcPath, uiExplorer.getSessionByWorkspace(wsName));
    UIApplication uiApp = getAncestorOfType(UIApplication.class);
    if(!selectedNode.isCheckedOut()) {
      uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    }
    if(selectedNode.isLocked()) {
      String lockToken = LockUtil.getLockToken(selectedNode);
      if(lockToken != null) uiExplorer.getSession().addLockToken(lockToken);
    }
    if(destPath.endsWith("/")) {
      destPath = destPath + srcPath.substring(srcPath.lastIndexOf("/") + 1);
    } else {
      destPath = destPath + srcPath.substring(srcPath.lastIndexOf("/"));
    }
    workspace.move(srcPath, destPath);
  }
  
  private void moveMultiNode(String[] srcPaths, String[] wsNames, String destPath, 
      Event event) throws Exception {
    for(int i=0; i< srcPaths.length; i++) {
      moveNode(srcPaths[i], wsNames[i], destPath, event);
    }
  }
  
  @SuppressWarnings("unused")
  static  public class EditDocumentActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent();
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class);
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME);
      if(wsName == null) wsName = uiExplorer.getCurrentWorkspace();
      Session session = uiExplorer.getSessionByWorkspace(wsName);
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class);
      Node selectedNode = null;
      try {
        selectedNode = uiExplorer.getNodeByPath(nodePath, session);
      } catch(PathNotFoundException path) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
            null,ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch(AccessDeniedException ace) {
        uiApp.addMessage(new ApplicationMessage("UIDocumentInfo.msg.null-exception", 
            null,ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;        
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
        return;
      }
      Object[] arg = { nodePath };
      try{
        ((ExtendedNode) selectedNode).checkPermission(PermissionType.SET_PROPERTY);        
      }catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.has-not-edit-permission",null,ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      boolean isReferenced = false;
      if(uiExplorer.nodeIsLocked(selectedNode)) {        
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiExplorer.updateAjax(event);
        return;
      }      
      if(selectedNode.isNodeType(Utils.EXO_ACTION)) {
        UIActionContainer uiContainer = uiExplorer.createUIComponent(UIActionContainer.class, null, null);
        uiExplorer.setIsHidePopup(true);
        uiContainer.getChild(UIActionTypeForm.class).setRendered(false);
        UIActionForm uiActionForm = uiContainer.getChild(UIActionForm.class);
        uiActionForm.createNewAction(uiExplorer.getCurrentNode(), 
            selectedNode.getPrimaryNodeType().getName(), false);
        uiActionForm.setWorkspace(wsName);
        uiActionForm.setNodePath(nodePath);
        UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
        UIPopupContainer.activate(uiContainer, 600, 550);
      } else {
        TemplateService tservice = uicomp.getApplicationComponent(TemplateService.class);
        String repository = uicomp.getAncestorOfType(UIJCRExplorer.class).getRepositoryName();
        List documentNodeType = tservice.getDocumentTemplates(repository);
        String nodeType = null;
        if(selectedNode.hasProperty("exo:presentationType")) {
          nodeType = selectedNode.getProperty("exo:presentationType").getString();
        }else {
          nodeType = selectedNode.getPrimaryNodeType().getName();
        }
        if(documentNodeType.contains(nodeType)){
          UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
          UIDocumentFormController uiController = 
            event.getSource().createUIComponent(UIDocumentFormController.class, null, "EditFormController");
          UIDocumentForm uiDocumentForm = uiController.getChild(UIDocumentForm.class);
          uiDocumentForm.setContentType(nodeType);
          uiDocumentForm.setRepositoryName(repository);
          uiDocumentForm.setWorkspace(wsName);
          uiDocumentForm.setNodePath(nodePath);
          uiDocumentForm.addNew(false);
          uiController.setRenderedChild(UIDocumentForm.class);
          UIPopupContainer.activate(uiController, 800, 600);
        } else {          
          uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.not-support", arg, 
              ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          uiExplorer.updateAjax(event);
          return;
        }
      }
    }
  }

  static  public class RenameActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent();
      String renameNodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME);
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class);
      uiExplorer.setIsHidePopup(false);
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class);
      Session session = uiExplorer.getSessionByWorkspace(wsName);
      Node renameNode = null;
      try {
        renameNode = (Node)session.getItem(renameNodePath);
      } catch(PathNotFoundException path) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
            null,ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
        return;
      }
      try {
        ((ExtendedNode) renameNode).checkPermission(PermissionType.SET_PROPERTY);        
      } catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-rename-node",null,ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiExplorer.updateAjax(event);
        return;
      }
      if(uiExplorer.nodeIsLocked(renameNode)) {
        Object[] arg = { renameNodePath };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());        
        return;
      }      
      if(renameNode.isNodeType("mix:versionable")) {
        if(!renameNode.isCheckedOut()) {
          uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.is-checked-in", null, 
              ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          uiExplorer.updateAjax(event);
          return;
        }
      }
      boolean isReferencedNode = false;
      try {
        if(wsName != null) isReferencedNode = true;
        UIControl uiControl = uiExplorer.getChild(UIControl.class);
        UIActionBar uiActionBar = uiControl.getChild(UIActionBar.class);
        UIRenameForm uiRenameForm = uiActionBar.createUIComponent(UIRenameForm.class, null, null);
        uiRenameForm.update(renameNode, isReferencedNode);
        UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
        UIPopupContainer.activate(uiRenameForm, 700, 0);
        UIPopupContainer.setRendered(true);
      } catch(Exception e) {
        JCRExceptionManager.process(uiApp, e);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiExplorer.updateAjax(event);
      }
    }
  }

  static  public class CopyActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent();
      String srcPath = event.getRequestContext().getRequestParameter(OBJECTID);
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME);      
      if(srcPath.indexOf(";") > -1) {
        uiWorkingArea.isMultiSelect_ = true;
        uiWorkingArea.virtualClipboards_.clear();
        uiWorkingArea.multipleCopy(srcPath.split(";"), wsName.split(";"), event);
      } else {
        uiWorkingArea.isMultiSelect_ = false;
        uiWorkingArea.processCopy(srcPath, wsName, event);
      }
    }
  }

  static  public class CutActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent();
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME);
      if(nodePath.indexOf(";") > -1) {
        uiWorkingArea.isMultiSelect_ = true;
        uiWorkingArea.virtualClipboards_.clear();
        uiWorkingArea.processMultipleCut(nodePath.split(";"), wsName.split(";"), event);
      } else {
        uiWorkingArea.isMultiSelect_ = false;
        uiWorkingArea.processCut(nodePath, wsName, event);
      }
    }
  }

  static  public class SaveActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent();
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class);
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME);
      Session session = uiExplorer.getSessionByWorkspace(wsName);
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class);
      try {
        Node node = uiExplorer.getNodeByPath(nodePath, session);
        Object[] args = { nodePath };
        if(node.isNew()) {
          uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.unable-save-node",args, 
              ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          uiExplorer.updateAjax(event);
          return;
        }
        node.save(); 
        session.save();
        uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.save-node-success", args));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      } catch(PathNotFoundException path) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
            null,ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch(Exception e) {
        JCRExceptionManager.process(uiApp, e);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiExplorer.updateAjax(event);
      }
    }
  }

  static  public class DeleteActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
//    TODO: Need review this method with remove record.
      UIWorkingArea uiWorkingArea = event.getSource().getParent();
      UIJCRExplorer uiExplorer = uiWorkingArea.getAncestorOfType(UIJCRExplorer.class);
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME);
      UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
      UIConfirmMessage uiConfirmMessage = 
        uiWorkingArea.createUIComponent(UIConfirmMessage.class, null, null);
      if(nodePath.indexOf(";") > -1) {
        uiConfirmMessage.setMessageKey("UIWorkingArea.msg.confirm-delete-multi");
        uiConfirmMessage.setArguments(new String[] {Integer.toString(nodePath.split(";").length)});
      } else {
        uiConfirmMessage.setMessageKey("UIWorkingArea.msg.confirm-delete");
        uiConfirmMessage.setArguments(new String[] {nodePath});
      }
      uiConfirmMessage.setNodePath(nodePath);
      uiConfirmMessage.setWorkspaceName(wsName);
      UIPopupContainer.activate(uiConfirmMessage, 500);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }

  static  public class LockActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent();
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME);
      if(nodePath.indexOf(";") > -1) {
        uiWorkingArea.isMultiSelect_ = true;
        uiWorkingArea.processMultiLock(nodePath.split(";"), wsName.split(";"), event);
      } else {
        uiWorkingArea.isMultiSelect_ = false;
        uiWorkingArea.processLock(nodePath, wsName, event);
      }
    }
  }

  static  public class UnlockActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent();
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME);      
      if(nodePath.indexOf(";") > -1) {
        uiWorkingArea.isMultiSelect_ = true;
        uiWorkingArea.processMultiUnlock(nodePath.split(";"), wsName.split(";"), event);
      } else {
        uiWorkingArea.isMultiSelect_ = false;
        uiWorkingArea.processUnlock(nodePath, wsName, event);
      }
    }
  }

  static  public class CheckInActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent();
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class);
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME);
      Session session = uiExplorer.getSessionByWorkspace(wsName);
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class);         
      Node node = uiExplorer.getNodeByPath(nodePath, session);
      try{
        ((ExtendedNode) node).checkPermission(PermissionType.SET_PROPERTY);        
      } catch (Exception e) {        
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-checkin-node", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      try {
        if(uiExplorer.nodeIsLocked(node)) {
          Object[] arg = { nodePath };
          uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
              ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());     
          return;
        }
        Node parentNode = node.getParent();
        if(parentNode.isLocked()) {
          String lockToken = LockUtil.getLockToken(parentNode);
          session.addLockToken(lockToken);
        }
        node.checkin();
      } catch(PathNotFoundException path) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
            null,ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiExplorer.updateAjax(event);
      }
    }
  }

  static  public class CheckOutActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent();
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class);
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME);
      Session session = uiExplorer.getSessionByWorkspace(wsName);      
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class);       
      Node node = uiExplorer.getNodeByPath(nodePath, session);
      try{
        ((ExtendedNode) node).checkPermission(PermissionType.SET_PROPERTY);        
      } catch (Exception e) {        
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-checkout-node", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      try {
        if(uiExplorer.nodeIsLocked(node)) {
          Object[] arg = { nodePath };
          uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
              ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());        
          return;
        }   
        node.checkout();
      } catch(PathNotFoundException path) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
            null,ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiExplorer.updateAjax(event);
      }
    }
  }

  static  public class CustomActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent();
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      String actionName = event.getRequestContext().getRequestParameter("actionName");
      String repository = uicomp.getAncestorOfType(UIJCRExplorer.class).getRepositoryName();
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class);
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME);
      Session session = uiExplorer.getSessionByWorkspace(wsName);
      ActionServiceContainer actionService = 
        uicomp.getApplicationComponent(ActionServiceContainer.class);
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
      try {
        Node node = uicomp.getAncestorOfType(UIJCRExplorer.class).getNodeByPath(nodePath, session);
        String userId = event.getRequestContext().getRemoteUser();
        actionService.executeAction(userId, node, actionName,repository);
        Object[] arg = { actionName };
        uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.execute-successfully", arg));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiExplorer.updateAjax(event);
      } catch (Exception e) {
        e.printStackTrace();
        JCRExceptionManager.process(uiApp, e);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiExplorer.updateAjax(event);
      }
    }
  }

  static public class PasteActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent();
      UIJCRExplorer uiExplorer = uiWorkingArea.getAncestorOfType(UIJCRExplorer.class);
      String destPath = event.getRequestContext().getRequestParameter(OBJECTID);
      if(destPath == null) destPath = uiExplorer.getCurrentPath();
      Session session = uiExplorer.getSession();
      UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
      if(uiExplorer.getAllClipBoard().size()<1) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.no-node", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      Node destNode = null;
      try {
        destNode = (Node)session.getItem(destPath);
      } catch(PathNotFoundException path) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
            null,ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
        return;
      }
      if(!PermissionUtil.canRead(destNode)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-paste-node", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiExplorer.updateAjax(event);
        return;
      }
      if(uiExplorer.nodeIsLocked(destNode)) {
        Object[] arg = { destPath };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());        
        return;
      }
      if(uiWorkingArea.isMultiSelect_) {
        uiWorkingArea.processPasteMultiple(destPath, event);
      } else {
        uiWorkingArea.processPaste(uiExplorer.getAllClipBoard().getLast(), destPath, event);
      }
      if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save();
      uiExplorer.updateAjax(event);
    }
  }
  
  static public class AddFolderActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent();
      UIJCRExplorer uiExplorer = uiWorkingArea.getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
      Node currentNode = uiExplorer.getCurrentNode();      
      if(!PermissionUtil.canRead(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.has-not-add-permission", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(uiExplorer.nodeIsLocked(currentNode)) {
        Object[] arg = { uiExplorer.getCurrentNode().getPath() };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } 
      if(!currentNode.isCheckedOut()) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
      UIPopupContainer.activate(UIFolderForm.class, 600);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }
  
  static public class AddDocumentActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent();
      UIJCRExplorer uiExplorer = uiWorkingArea.getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
      Node currentNode = uiExplorer.getCurrentNode();      
      if(!PermissionUtil.canRead(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.has-not-add-permission", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(uiExplorer.nodeIsLocked(currentNode)){
        Object[] arg = { currentNode.getPath() };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(!currentNode.isCheckedOut()) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }     
      UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
      UIDocumentFormController uiController = 
        event.getSource().createUIComponent(UIDocumentFormController.class, null, null);
      uiController.setCurrentNode(uiExplorer.getCurrentNode());
      uiController.setRepository(uiExplorer.getRepositoryName());
      if(uiController.getListFileType().size() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.empty-file-type", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      uiController.init();
      UIPopupContainer.activate(uiController, 800, 600);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }
  
  static public class UploadActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent();
      UIJCRExplorer uiExplorer = uiWorkingArea.getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
      Node currentNode = uiExplorer.getCurrentNode();
      if(!PermissionUtil.canRead(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.has-not-add-permission", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(uiExplorer.nodeIsLocked(currentNode)) {
        Object[] arg = { currentNode.getPath() };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(!currentNode.isCheckedOut()) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }      
      UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
      UIUploadManager uiUploadManager = event.getSource().createUIComponent(UIUploadManager.class, null, null);
      UIPopupContainer.activate(uiUploadManager, 600, 500);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }
  
  static public class MoveNodeActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent();
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME);
      String[] destInfo = event.getRequestContext().getRequestParameter("destInfo").split(";");
      UIJCRExplorer uiExplorer = uiWorkingArea.getParent();
      UIApplication uiApp = uiWorkingArea.getAncestorOfType(UIApplication.class);
      Node destNode = null;
      if(destInfo[0].startsWith(nodePath)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.bound-move-exception", 
            null,ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      try {
        destNode = (Node)uiExplorer.getSession().getItem(destInfo[0]);
      } catch(PathNotFoundException path) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
            null,ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
        return;
      }
      if(!PermissionUtil.canRead(destNode)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-move-node", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiExplorer.updateAjax(event);
        return;
      }
      if(uiExplorer.nodeIsLocked(destNode)) {
        Object[] arg = { destInfo[0] };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());        
        return;
      }
      try {
        if(nodePath.indexOf(";") > -1) {
          uiWorkingArea.isMultiSelect_ = true;
          uiWorkingArea.moveMultiNode(nodePath.split(";"), wsName.split(";"), destInfo[0], event);
        } else {
          uiWorkingArea.isMultiSelect_ = false;
          uiWorkingArea.moveNode(nodePath, wsName, destInfo[0], event);
        }
        uiExplorer.getSession().save();
        uiExplorer.updateAjax(event);
      } catch(AccessDeniedException ace) {
        Object[] arg = { destInfo[0] };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.has-not-add-permission", arg, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());        
        return;        
      } catch(LockException lock) {
        Object[] arg = { nodePath };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());        
        return;
      } catch(ConstraintViolationException constraint) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.move-constraint-exception", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());        
        return;       
      } catch(Exception e) {
        e.printStackTrace();
        JCRExceptionManager.process(uiApp, e);
        return;
      }
    }
  }
}
