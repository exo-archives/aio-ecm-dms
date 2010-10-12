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

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.Node;
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

import org.apache.commons.logging.Log;
import org.exoplatform.ecm.jcr.model.ClipboardCommand;
import org.exoplatform.ecm.webui.component.explorer.control.UIActionBar;
import org.exoplatform.ecm.webui.component.explorer.control.UIControl;
import org.exoplatform.ecm.webui.component.explorer.control.action.AddDocumentActionComponent;
import org.exoplatform.ecm.webui.component.explorer.control.action.AddFolderActionComponent;
import org.exoplatform.ecm.webui.component.explorer.control.action.EditDocumentActionComponent;
import org.exoplatform.ecm.webui.component.explorer.control.action.UploadActionComponent;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIRenameForm;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UISideBar;
import org.exoplatform.ecm.webui.component.explorer.symlink.UISymLinkManager;
import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
import org.exoplatform.ecm.webui.utils.LockUtil;
import org.exoplatform.ecm.webui.utils.PermissionUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.cms.link.LinkUtils;
import org.exoplatform.services.cms.link.NodeLinkAware;
import org.exoplatform.services.cms.relations.RelationsService;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.cms.thumbnail.ThumbnailService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;
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
import org.exoplatform.webui.core.UIPopupContainer;
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
        @EventConfig(listeners = UIWorkingArea.AddSymLinkActionListener.class),
        @EventConfig(listeners = UIWorkingArea.MoveNodeActionListener.class, confirm="UIWorkingArea.msg.confirm-move"),
        @EventConfig(listeners = UIWorkingArea.CreateLinkActionListener.class)
      }
  )
})

public class UIWorkingArea extends UIContainer {

  /**
   * Logger.
   */
  private static final Log LOG  = ExoLogger.getLogger("dms.UIJcrExplorerContainer");
  public static final Pattern FILE_EXPLORER_URL_SYNTAX = Pattern.compile("([^:/]+):(/.*)");
    
  private static final String RELATION_PROP = "exo:relation";
  public static final String WS_NAME = "workspaceName";
  
  private List<ClipboardCommand> virtualClipboards_ = Collections.synchronizedList(new LinkedList<ClipboardCommand>());

  public UIWorkingArea() throws Exception {
    addChild(UIRightClickPopupMenu.class, "ECMContextMenu", null);
    addChild(UISideBar.class, null, null);
    addChild(UIDocumentWorkspace.class, null, null);
  }

  public List<ClipboardCommand> getVirtualClipboards() {
    return virtualClipboards_;
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
    return node.getIndex() > 1;
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
    if (depth < 1) return false;
    Node parent = null;
    try {
    	parent = (Node) node.getAncestor(depth);
    } catch (ClassCastException ex) {
    	parent = (Node) node.getAncestor(--depth);
    }
    while (true) {
      if (parent.isNodeType(Utils.MIX_VERSIONABLE)) return true;
      if (--depth == 0) return false;
      parent = (Node) node.getAncestor(depth);
			if (parent == null) return false;
    }
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
    StringBuilder actionsList = new StringBuilder();        
    Node realNode = (node instanceof NodeLinkAware) ? ((NodeLinkAware) node).getRealNode() : node;
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
        actionsList.append(",Rename");
        if(isJcrEnable) actionsList.append(",Save");
        actionsList.append(",Delete");
      } else {
        if(isVersionable) actionsList.append(",CheckOut");
        if(!isSameNameSibling) {
          if(holdsLock) actionsList.append(",Unlock");
          else if(!isLocked) actionsList.append(",Lock");
        }
        actionsList.append(",Rename");
      }
      actionsList.append(",Copy");
      actionsList.append(",Cut");
      if (!realNode.isNodeType(Utils.EXO_SYMLINK)) actionsList.append(",AddSymLink");
    } else {
      if(isEditable) actionsList.append(",EditDocument");
      if(!isSameNameSibling) {
        if(holdsLock) {
          actionsList.append(",Unlock");
        } else if(!isLocked) {
          actionsList.append(",Lock");
        }
      }
      actionsList.append(",Copy");
      actionsList.append(",Cut");
      actionsList.append(",Rename");
      if(isJcrViewEnable()) actionsList.append(",Save");
      actionsList.append(",Delete");
      if (!realNode.isNodeType(Utils.EXO_SYMLINK)) actionsList.append(",AddSymLink");
    }
    if (!uiExplorer.getAllClipBoard().isEmpty()) actionsList.append(",Paste");
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
    if (node instanceof NodeLinkAware) {
      NodeLinkAware nodeLA = (NodeLinkAware) node;
      try {
        node = nodeLA.getTargetNode().getRealNode();
      } catch (Exception e) {
        // The target of the link is not reachable
      }
    }    
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
  
  private void multipleCopy(String[] srcPaths, Event<?> event) throws Exception {
    for(int i=0; i< srcPaths.length; i++) {
      processCopy(srcPaths[i], event, true);
    }
  }
  
  private String getDefaultWorkspace() {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    return uiExplorer.getCurrentDriveWorkspace();            
  }
  
  private void processCopy(String srcPath, Event<?> event, boolean isMultiSelect) throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    UIApplication uiApp = getAncestorOfType(UIApplication.class);
    Matcher matcher = FILE_EXPLORER_URL_SYNTAX.matcher(srcPath);
    String wsName = null;
    if (matcher.find()) {
      wsName = matcher.group(1);
      srcPath = matcher.group(2);
    } else {
      throw new IllegalArgumentException("The ObjectId is invalid '"+ srcPath + "'");
    }
    Session session = uiExplorer.getSessionByWorkspace(wsName);
    try {
      // Use the method getNodeByPath because it is link aware
      Node node = uiExplorer.getNodeByPath(srcPath, session, false);
      // Reset the path to manage the links that potentially create virtual path
      srcPath = node.getPath();
      // Reset the session to manage the links that potentially change of workspace
      session = node.getSession();
      // Reset the workspace name to manage the links that potentially change of workspace 
      wsName = session.getWorkspace().getName();
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
      if(isMultiSelect) getVirtualClipboards().add(clipboard);
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
      LOG.error("an unexpected error occurs", e);
      JCRExceptionManager.process(uiApp, e);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
    }
  }
  
  private void processPasteMultiple(String destPath, Event<?> event) throws Exception {
    int pasteNum = 0;
    List<ClipboardCommand> virtualClipboards = getVirtualClipboards();
    for(ClipboardCommand clipboard : virtualClipboards) {
      pasteNum++;
      if(pasteNum == virtualClipboards.size()) {
        processPaste(clipboard, destPath, event, true, true);
        break;
      }
      processPaste(clipboard, destPath, event, true, false);
    }
  }
  
  public void processPaste(ClipboardCommand currentClipboard, String destPath, 
                            Event<?> event) throws Exception {
    processPaste(currentClipboard, destPath, event, false, true);
  }
  
  private void processPaste(ClipboardCommand currentClipboard, String destPath, 
                           Event<?> event, boolean isMultiSelect, 
                           boolean isLastPaste) throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
    String srcPath = currentClipboard.getSrcPath();
    String type = currentClipboard.getType();
    String srcWorkspace = currentClipboard.getWorkspace();
    Session srcSession = uiExplorer.getSessionByWorkspace(srcWorkspace);
    // Use the method getNodeByPath because it is link aware
    Node srcNode = uiExplorer.getNodeByPath(srcPath, srcSession, false);
    // Reset the path to manage the links that potentially create virtual path
    srcPath = srcNode.getPath();
    // Reset the session to manage the links that potentially change of workspace
    srcSession = srcNode.getSession();
    // Reset the workspace name to manage the links that potentially change of workspace 
    srcWorkspace = srcSession.getWorkspace().getName();
    Matcher matcher = FILE_EXPLORER_URL_SYNTAX.matcher(destPath);
    String destWorkspace = null;
    if (matcher.find()) {
      destWorkspace = matcher.group(1);
      destPath = matcher.group(2);
    } else {
      throw new IllegalArgumentException("The ObjectId is invalid '"+ destPath + "'");
    }
    Session destSession = uiExplorer.getSessionByWorkspace(destWorkspace);
    
    // Use the method getNodeByPath because it is link aware
    Node destNode = uiExplorer.getNodeByPath(destPath, destSession);
    // Reset the path to manage the links that potentially create virtual path
    destPath = destNode.getPath();
    // Reset the session to manage the links that potentially change of workspace
    destSession = destNode.getSession();    
    if(ClipboardCommand.CUT.equals(type) && srcPath.equals(destPath)) { 
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-cutting", null, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());        
      return; 
    }
    // Make destination path without index on final name
    if (!"/".equals(destPath))
    	destPath = destPath.concat("/");
    destPath = destPath.concat(srcNode.getName());

    ActionServiceContainer actionContainer = getApplicationComponent(ActionServiceContainer.class);
    try {
      if(ClipboardCommand.COPY.equals(type)) {
        pasteByCopy(destSession, srcWorkspace, srcPath, destPath);
        Node selectedNode = (Node) destSession.getItem(destPath);
        actionContainer.initiateObservation(selectedNode, uiExplorer.getRepositoryName());
      } else {
        pasteByCut(currentClipboard, uiExplorer, destSession, srcWorkspace, srcPath, destPath, actionContainer, uiExplorer.getRepositoryName(), isMultiSelect, isLastPaste);
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
  
  private void pasteByCopy(Session session, String srcWorkspaceName, String srcPath, String destPath) throws Exception {
    Workspace workspace = session.getWorkspace();
    if (workspace.getName().equals(srcWorkspaceName)) {
      workspace.copy(srcPath, destPath);
      Node destNode = (Node) session.getItem(destPath);
      removeReferences(destNode);
    } else {
      try {
        if (LOG.isDebugEnabled()) LOG.debug("Copy to another workspace");
        workspace.copy(srcWorkspaceName, srcPath, destPath);
      } catch (Exception e) {
        LOG.error("an unexpected error occurs while pasting the node", e);
        if (LOG.isDebugEnabled()) LOG.debug("Copy to other workspace by clone");
        try {
          workspace.clone(srcWorkspaceName, srcPath, destPath, false);  
        } catch (Exception f) {
          LOG.error("an unexpected error occurs while pasting the node", f);
        }
      }
    }
  }

 /**
  * Update clipboard after CUT node. Detain PathNotFoundException with cutting same name sibling node
  * @param clipboardCommands
  * @param mapClipboard
  * @throws Exception
  */
  private static void updateClipboard(List<ClipboardCommand> clipboardCommands, Map<ClipboardCommand, Node> mapClipboard) throws Exception {
    Node srcNode;
    for (ClipboardCommand clipboard : clipboardCommands) {
      if (ClipboardCommand.CUT.equals(clipboard.getType())) {
        srcNode = mapClipboard.get(clipboard);
        srcNode.refresh(true);
        clipboard.setSrcPath(srcNode.getPath());
      }
    }
  }
  
  /**
   * Put data from clipboard to Map<Clipboard, Node>. After cutting node, we keep data to update clipboard by respective node
   * @param clipboardCommands
   * @param uiExplorer
   * @return
   * @throws Exception
   */
  private static Map<ClipboardCommand, Node> parseToMap(List<ClipboardCommand> clipboardCommands, UIJCRExplorer uiExplorer) throws Exception {
    String srcPath;
    String type;
    String srcWorkspace;
    Node srcNode;
    Session srcSession;
    Map<ClipboardCommand, Node> mapClipboard = new HashMap<ClipboardCommand, Node>();
    for (ClipboardCommand clipboard : clipboardCommands) {
      srcPath = clipboard.getSrcPath();
      type = clipboard.getType();
      srcWorkspace = clipboard.getWorkspace();
      if (ClipboardCommand.CUT.equals(type)) {
        srcSession = uiExplorer.getSessionByWorkspace(srcWorkspace);
        // Use the method getNodeByPath because it is link aware
        srcNode = uiExplorer.getNodeByPath(srcPath, srcSession, false);
        clipboard.setSrcPath(srcNode.getPath());
        mapClipboard.put(clipboard, srcNode);
      }
    }
    return mapClipboard;
  }
  
  private void pasteByCut(ClipboardCommand currentClipboard, UIJCRExplorer uiExplorer, Session session, String srcWorkspace, String srcPath, 
      String destPath, ActionServiceContainer actionContainer, String repository,
      boolean isMultiSelect, boolean isLastPaste) throws Exception {
    Workspace workspace = session.getWorkspace();
    if (workspace.getName().equals(srcWorkspace)) {
      if (srcPath.equals(destPath)) return;
    }
    UIWorkingArea uiWorkingArea = uiExplorer.getChild(UIWorkingArea.class);
    List<ClipboardCommand> allClipboard = uiExplorer.getAllClipBoard();
    List<ClipboardCommand> virtualClipboard = uiWorkingArea.getVirtualClipboards();
    Map<ClipboardCommand, Node> mapAllClipboardNode = parseToMap(allClipboard, uiExplorer);
    Map<ClipboardCommand, Node> mapVirtualClipboardNode = parseToMap(virtualClipboard, uiExplorer);
    
    RelationsService relationsService = 
      uiExplorer.getApplicationComponent(RelationsService.class);
    List<Node> refList = new ArrayList<Node>();
    boolean isReference = false;
    PropertyIterator references = null;
    Node srcNode = (Node)uiExplorer.getSessionByWorkspace(srcWorkspace).getItem(srcPath);
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
    
    if (workspace.getName().equals(srcWorkspace)) {
      try {
        workspace.move(srcPath, destPath);
      } catch(ArrayIndexOutOfBoundsException e) {
        throw new MessageException(new ApplicationMessage("UIPopupMenu.msg.bound-exception", null, 
            ApplicationMessage.WARNING));
      }
      if(!isMultiSelect || (isMultiSelect && isLastPaste)) {
        Node desNode = null;
        try {
          desNode = (Node) session.getItem(destPath);
        } catch (PathNotFoundException pathNotFoundException) {
          uiExplorer.setCurrentPath(LinkUtils.getParentPath(uiExplorer.getCurrentPath()));
          desNode = uiExplorer.getCurrentNode();
        } catch (ItemNotFoundException itemNotFoundException) {
          uiExplorer.setCurrentPath(LinkUtils.getParentPath(uiExplorer.getCurrentPath()));
          desNode = uiExplorer.getCurrentNode();
        }
		
		if (!session.itemExists(uiExplorer.getCurrentPath())) {
          uiExplorer.setCurrentPath(LinkUtils.getAncestorPath(uiExplorer.getCurrentPath(), 0));
        }
		
        if (!(desNode.getPath().equals(uiExplorer.getCurrentPath())))
          actionContainer.initiateObservation(desNode, repository);
        for(int i = 0; i < refList.size(); i ++) {
          Node addRef = refList.get(i);
          relationsService.addRelation(addRef, destPath,session.getWorkspace().getName(),uiExplorer.getRepositoryName());
          addRef.save();
        }
        getVirtualClipboards().clear();
        String currentPath = uiExplorer.getCurrentPath();
        Node currentNode = uiExplorer.getCurrentNode();
        String realCurrentPath = currentNode.getPath(); 
        if(srcWorkspace.equals(currentNode.getSession().getWorkspace().getName()) && (srcPath.equals(realCurrentPath) || realCurrentPath.startsWith(srcPath))) {
          uiExplorer.setCurrentPath(LinkUtils.getParentPath(currentPath));
        }
      }
    } else {
      workspace.clone(srcWorkspace, srcPath, destPath, false);
      if(!isMultiSelect || (isMultiSelect && isLastPaste)) {
        getVirtualClipboards().clear();
      }
    }
    session.save();
    uiExplorer.getAllClipBoard().remove(currentClipboard);
    updateClipboard(uiWorkingArea.getVirtualClipboards(), mapVirtualClipboardNode);
    updateClipboard(uiExplorer.getAllClipBoard(), mapAllClipboardNode);
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
  
  private void processRemoveMultiple(String[] nodePaths, Event<?> event) throws Exception {
    Node node = null;
    String wsName = null;
    String nodePath = null;
    Session session = null;
    Map<String, Node> mapNode = new HashMap <String, Node>();
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
    for(int i=0; i< nodePaths.length; i++) {
      Matcher matcher = FILE_EXPLORER_URL_SYNTAX.matcher(nodePaths[i]);
      //prepare to remove
      if (matcher.find()) {
        wsName = matcher.group(1);
        nodePath = matcher.group(2);
        try {
          session = uiExplorer.getSessionByWorkspace(wsName);
          // Use the method getNodeByPath because it is link aware
          node = uiExplorer.getNodeByPath(nodePath, session, false);
          // Reset the session to manage the links that potentially change of workspace
          session = node.getSession();
          // Reset the workspace name to manage the links that potentially change of workspace 
          wsName = session.getWorkspace().getName();
          mapNode.put(nodePath, node);
        } catch(PathNotFoundException path) {
          uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
              null,ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        } catch (Exception e) {
          JCRExceptionManager.process(uiApp, e);
        }
      } else {
        throw new IllegalArgumentException("The ObjectId is invalid '"+ nodePath + "'");
      }    
    }
    
    String path = null;
    Iterator<String> iterator = mapNode.keySet().iterator(); 
    while (iterator.hasNext()) {
      path = iterator.next();
      processRemove(path, mapNode.get(path), event,true);
    }
  }
  
  private void processRemoveMultiple(String[] nodePaths, String[] wsNames, Event<?> event) throws Exception {
    for(int i=0; i< nodePaths.length; i++) {
      processRemove(nodePaths[i], wsNames[i], event);
    }
  }
  
  private void processRemove(String nodePath, String wsName, Event<?> event) throws Exception {
    if (wsName == null) {
      wsName = getDefaultWorkspace();        
    }
    doDelete(wsName.concat(":").concat(nodePath), event);
  }
  
  private void processRemove(String nodePath, Node node, Event<?> event, boolean isMultiSelect) throws Exception {
    final String virtualNodePath = nodePath;
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    Session session = node.getSession();
    UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
    try {
      uiExplorer.addLockToken(node);
    } catch(PathNotFoundException path) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
          null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;      
    } catch (Exception e) {
      JCRExceptionManager.process(uiApp, e);
      return;
    }
    if (!PermissionUtil.canRemoveNode(node)) {
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
    uiExplorer.addLockToken(parentNode);
    try {
      if(node.isNodeType(Utils.RMA_RECORD)) removeMixins(node);      
      ActionServiceContainer actionService = getApplicationComponent(ActionServiceContainer.class);
      actionService.removeAction(node, getAncestorOfType(UIJCRExplorer.class).getRepositoryName());
      ThumbnailService thumbnailService = getApplicationComponent(ThumbnailService.class);
      thumbnailService.processRemoveThumbnail(node);
      node.remove();
      parentNode.save();
    } catch(VersionException ve) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.remove-verion-exception", null,
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;    
    } catch(ReferentialIntegrityException ref) {
      session.refresh(false);
      uiExplorer.refreshExplorer();
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.remove-referentialIntegrityException", 
          null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
    } catch(ConstraintViolationException cons) {
      session.refresh(false);
      uiExplorer.refreshExplorer();
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.constraintviolation-exception", 
          null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;        
    } catch(LockException lockException) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked-other-person", null,
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
    } catch(Exception e) {  
      LOG.error("an unexpected error occurs while removing the node", e);
      JCRExceptionManager.process(uiApp, e);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    }
    if(!isMultiSelect) uiExplorer.setSelectNode(LinkUtils.getParentPath(virtualNodePath));
  }
  
  private void processCut(String nodePath, Event<?> event, boolean isMultiSelect) throws Exception {
    UIJCRExplorer uiExplorer = getParent();
    UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
    Matcher matcher = FILE_EXPLORER_URL_SYNTAX.matcher(nodePath);
    String wsName = null;
    if (matcher.find()) {
      wsName = matcher.group(1);
      nodePath = matcher.group(2);
    } else {
      throw new IllegalArgumentException("The ObjectId is invalid '"+ nodePath + "'");
    }
    Session session = uiExplorer.getSessionByWorkspace(wsName);
    Node selectedNode;
    try {
      // Use the method getNodeByPath because it is link aware
      selectedNode = uiExplorer.getNodeByPath(nodePath, session, false);
      // Reset the path to manage the links that potentially create virtual path
      nodePath = selectedNode.getPath();
      // Reset the session to manage the links that potentially change of workspace
      session = selectedNode.getSession();
      // Reset the workspace name to manage the links that potentially change of workspace 
      wsName = session.getWorkspace().getName();
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
      LOG.error("an unexpected error occurs while cuting the node", e);
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
      if(isMultiSelect) {
        getVirtualClipboards().add(clipboard);
      } else {
        if(!uiExplorer.getPreference().isJcrEnable()) session.save();
        uiExplorer.updateAjax(event);
      }
    } catch (Exception e) {
      JCRExceptionManager.process(uiApp, e);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
    }
  }
  
  private void processMultipleCut(String[] nodePaths, Event<?> event) throws Exception {
    UIJCRExplorer uiExplorer = getParent();
    for(int i=0; i< nodePaths.length; i++) {
      processCut(nodePaths[i], event, true);
    }
    if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save();
    uiExplorer.updateAjax(event);
  }
  
  public void doDelete(String nodePath, Event<?> event) throws Exception {
    UIJCRExplorer uiExplorer = getParent();
    if(nodePath.indexOf(";") > -1) {
      processRemoveMultiple(nodePath.split(";"), event);
    } else {
      String wsName = null;
      Session session = null;
      Matcher matcher = FILE_EXPLORER_URL_SYNTAX.matcher(nodePath);
      UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
      //prepare to remove
      if (matcher.find()) {
        wsName = matcher.group(1);
        nodePath = matcher.group(2);
        try {
          // Use the method getNodeByPath because it is link aware
          session = uiExplorer.getSessionByWorkspace(wsName);
          // Use the method getNodeByPath because it is link aware
          Node node = uiExplorer.getNodeByPath(nodePath, session, false);
          // Reset the session to manage the links that potentially change of workspace
          session = node.getSession();
          // Reset the workspace name to manage the links that potentially change of workspace 
          wsName = session.getWorkspace().getName();
          // Use the method getNodeByPath because it is link aware
          node = uiExplorer.getNodeByPath(nodePath, session, false);
          // If node has taxonomy
          TaxonomyService taxonomyService = uiExplorer.getApplicationComponent(TaxonomyService.class);
          List<Node> listTaxonomyTrees = taxonomyService.getAllTaxonomyTrees(uiExplorer.getRepositoryName());
          List<Node> listExistedTaxonomy = taxonomyService.getAllCategories(node);
          for (Node existedTaxonomy : listExistedTaxonomy) {
            for (Node taxonomyTrees : listTaxonomyTrees) {
              if(existedTaxonomy.getPath().contains(taxonomyTrees.getPath())) {
                taxonomyService.removeCategory(node, taxonomyTrees.getName(), 
                    existedTaxonomy.getPath().substring(taxonomyTrees.getPath().length()));
                break;
              }
            }
          }
          processRemove(nodePath, node, event, false);
        } catch(PathNotFoundException path) {
          uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
              null,ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        } catch (Exception e) {
          JCRExceptionManager.process(uiApp, e);
          return;
        }
      }
    }
    uiExplorer.updateAjax(event);
    if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save(); 
  }
  
  private void processMultiLock(String[] nodePaths, Event<?> event) throws Exception {
    UIJCRExplorer uiExplorer = getParent();
    for(int i=0; i< nodePaths.length; i++) {
      processLock(nodePaths[i], event);
    }
    if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save();
    uiExplorer.updateAjax(event);
  }
  
  public void doDelete(String nodePath, String wsName, Event<?> event) throws Exception {
    UIJCRExplorer uiExplorer = getParent();
    if(nodePath.indexOf(";") > -1) {
      processRemoveMultiple(nodePath.split(";"), wsName.split(";"), event);
    } else {
      processRemove(nodePath, wsName, event);
    }
    uiExplorer.updateAjax(event);
    if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save(); 
  }
  
  private void processLock(String nodePath, Event<?> event) throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
    Matcher matcher = FILE_EXPLORER_URL_SYNTAX.matcher(nodePath);
    String wsName = null;
    if (matcher.find()) {
      wsName = matcher.group(1);
      nodePath = matcher.group(2);
    } else {
      throw new IllegalArgumentException("The ObjectId is invalid '"+ nodePath + "'");
    }
    Session session = uiExplorer.getSessionByWorkspace(wsName);
    Node node;
    try {
      // Use the method getNodeByPath because it is link aware
      node = uiExplorer.getNodeByPath(nodePath, session);
      // Reset the path to manage the links that potentially create virtual path
      nodePath = node.getPath();
      // Reset the session to manage the links that potentially change of workspace
      session = node.getSession();
      // Reset the workspace name to manage the links that potentially change of workspace 
      wsName = session.getWorkspace().getName();
    } catch(PathNotFoundException path) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
          null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    } catch (Exception e) {
      JCRExceptionManager.process(uiApp, e);
      return;
    }
    if (!PermissionUtil.canSetProperty(node)) {
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
      LOG.error("an unexpected error occurs while locking the node", e);
      JCRExceptionManager.process(uiApp, e);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);        
    }
  }
  
  private void processMultiUnlock(String[] nodePaths, Event<?> event) throws Exception {
    UIJCRExplorer uiExplorer = getParent();
    for(int i=0; i< nodePaths.length; i++) {
      processUnlock(nodePaths[i], event);
    }
    if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save();
    uiExplorer.updateAjax(event);
  }
  
  private void processUnlock(String nodePath, Event<?> event) throws Exception {
    UIJCRExplorer uiExplorer = getParent();
    UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
    Matcher matcher = FILE_EXPLORER_URL_SYNTAX.matcher(nodePath);
    String wsName = null;
    if (matcher.find()) {
      wsName = matcher.group(1);
      nodePath = matcher.group(2);
    } else {
      throw new IllegalArgumentException("The ObjectId is invalid '"+ nodePath + "'");
    }
    Session session = uiExplorer.getSessionByWorkspace(wsName);
    Node node;
    try {
      // Use the method getNodeByPath because it is link aware
      node = uiExplorer.getNodeByPath(nodePath, session);
      // Reset the path to manage the links that potentially create virtual path
      nodePath = node.getPath();
      // Reset the session to manage the links that potentially change of workspace
      session = node.getSession();
      // Reset the workspace name to manage the links that potentially change of workspace 
      wsName = session.getWorkspace().getName();
    } catch(PathNotFoundException path) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
          null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    } catch (Exception e) {
      JCRExceptionManager.process(uiApp, e);
      return;
    }
    if (!PermissionUtil.canSetProperty(node)) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-unlock-node",null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;        
    }
    if(!node.isCheckedOut()){
      Object[] args = {node.getName()};
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-unlock-node-is-checked-in", args, 
          ApplicationMessage.WARNING));
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
        node.removeMixin(Utils.MIX_LOCKABLE);
        node.getSession().save();
        //remove lock from Cache
        LockUtil.removeLock(node);
      }
    } catch(LockException le) {
      Object[] args = {node.getName()};
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-unlock-node", args, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;
    } catch(VersionException versionException) {
      Object[] args = {node.getName()};
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-unlock-node-is-checked-in", args, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
      return;  
    } catch (Exception e) {
      LOG.error("an unexpected error occurs while unloking the node", e);
      JCRExceptionManager.process(uiApp, e);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiExplorer.updateAjax(event);
    }    
  }
  
  private void moveNode(String srcPath, Node selectedNode, Node destNode, Event<?> event) throws Exception {
    UIJCRExplorer uiExplorer = getParent();
    Matcher matcher = FILE_EXPLORER_URL_SYNTAX.matcher(srcPath);
    String wsName = null;
    Session srcSession;
    UIApplication uiApp = getAncestorOfType(UIApplication.class);
    if(srcPath.indexOf(":/") > -1 || (selectedNode != null)) {
      String[] arrSrcPath = srcPath.split(":/");
      if((srcPath.contains(":/") && ("/" + arrSrcPath[1]).equals(destNode.getPath())) || (selectedNode != null && selectedNode.equals(destNode))) {
        uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.can-not-move-to-itself", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
    }
    if (selectedNode == null) {
      if (matcher.find()) {
        wsName = matcher.group(1);
        srcPath = matcher.group(2);
      } else {
        throw new IllegalArgumentException("The ObjectId is invalid '"+ srcPath + "'");
      }    
      srcSession = uiExplorer.getSessionByWorkspace(wsName);
      // Use the method getNodeByPath because it is link aware
      selectedNode = uiExplorer.getNodeByPath(srcPath, srcSession, false);
      // Reset the path to manage the links that potentially create virtual path
      srcPath = selectedNode.getPath();
      // Reset the session to manage the links that potentially change of workspace
    }
    srcSession = selectedNode.getSession();
    if(!selectedNode.isCheckedOut()) {
      uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    }
    uiExplorer.addLockToken(selectedNode);
    String destPath = destNode.getPath();
    String messagePath = destPath;
    destPath = destPath.concat("/").concat(selectedNode.getName());
    Workspace srcWorkspace = srcSession.getWorkspace();
    Workspace destWorkspace = destNode.getSession().getWorkspace();
    try {
      if (srcWorkspace.equals(destWorkspace)) {
        srcWorkspace.move(srcPath, destPath);        
      } else {
        destWorkspace.clone(srcWorkspace.getName(), srcPath, destPath, false);
      }
    } catch(Exception e) {
      Object[] args = { srcPath, messagePath };
      uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.move-problem", args, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    }
  }
  
  private void moveMultiNode(String[] srcPaths, Node destNode, 
      Event<?> event) throws Exception {
    Node node = null;
    String wsName = null;
    String nodePath = null;
    Session session = null;
    Map<String, Node> mapNode = new HashMap <String, Node>();
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
    for(int i=0; i< srcPaths.length; i++) {
      Matcher matcher = FILE_EXPLORER_URL_SYNTAX.matcher(srcPaths[i]);
      //prepare to remove
      if (matcher.find()) {
        wsName = matcher.group(1);
        nodePath = matcher.group(2);
        try {
          session = uiExplorer.getSessionByWorkspace(wsName);
          // Use the method getNodeByPath because it is link aware
          node = uiExplorer.getNodeByPath(nodePath, session, false);
          // Reset the session to manage the links that potentially change of workspace
          session = node.getSession();
          // Reset the workspace name to manage the links that potentially change of workspace 
          wsName = session.getWorkspace().getName();
          mapNode.put(nodePath, node);
        } catch(PathNotFoundException path) {
          uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
              null,ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        } catch (Exception e) {
          JCRExceptionManager.process(uiApp, e);
        }
      } else {
        throw new IllegalArgumentException("The ObjectId is invalid '"+ nodePath + "'");
      }    
    }
    
    String path = null;
    Iterator<String> iterator = mapNode.keySet().iterator(); 
    while (iterator.hasNext()) {
      path = iterator.next();
      node = mapNode.get(path);
      node.refresh(true);
      moveNode(node.getPath(), node, destNode, event);
    }
  }
  
  private void createMultiLink(String[] srcPaths, Node destNode, 
      Event<?> event) throws Exception {
    for(int i=0; i< srcPaths.length; i++) {
      createLink(srcPaths[i], destNode, event);
    }    
  }
  
  private void createLink(String srcPath, Node destNode, Event<?> event) throws Exception {
    UIJCRExplorer uiExplorer = getParent();
    Matcher matcher = FILE_EXPLORER_URL_SYNTAX.matcher(srcPath);
    String wsName = null;
    if (matcher.find()) {
      wsName = matcher.group(1);
      srcPath = matcher.group(2);
    } else {
      throw new IllegalArgumentException("The ObjectId is invalid '"+ srcPath + "'");
    }    
    Session session = uiExplorer.getSessionByWorkspace(wsName);
    Node selectedNode = uiExplorer.getNodeByPath(srcPath, session, false);
    UIApplication uiApp = getAncestorOfType(UIApplication.class);
    LinkManager linkManager = getApplicationComponent(LinkManager.class);
    if(linkManager.isLink(destNode)) {
      Object[] args = { destNode.getPath() };
      uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.dest-node-is-link", args, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    }
    if(linkManager.isLink(selectedNode)) {
      Object[] args = { srcPath };
      uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.selected-is-link", args, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    }
    try {
      linkManager.createLink(destNode, Utils.EXO_SYMLINK, selectedNode, 
          selectedNode.getName() + ".lnk");
    } catch(Exception e) {
      Object[] args = { srcPath, destNode.getPath() };
      uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.create-link-problem", args, 
          ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    }
  }
  
  private void processMultipleSelection(String nodePath, boolean isLink, 
      String destPath, Event<?> event) throws Exception {
    UIJCRExplorer uiExplorer = getParent();
    Matcher matcher = FILE_EXPLORER_URL_SYNTAX.matcher(destPath);
    String wsName = null;
    if (matcher.find()) {
      wsName = matcher.group(1);
      destPath = matcher.group(2);
    } else {
      throw new IllegalArgumentException("The ObjectId is invalid '"+ destPath + "'");
    }    
    Session session = uiExplorer.getSessionByWorkspace(wsName);    
    UIApplication uiApp = getAncestorOfType(UIApplication.class);
    if(destPath.startsWith(nodePath)) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.bound-move-exception", 
          null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    }
    Node destNode;
    try {
      // Use the method getNodeByPath because it is link aware
      destNode = uiExplorer.getNodeByPath(destPath, session);
      // Reset the path to manage the links that potentially create virtual path
      destPath = destNode.getPath();
      // Reset the session to manage the links that potentially change of workspace
      session = destNode.getSession();
    } catch(PathNotFoundException path) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
          null,ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    } catch (Exception e) {
      JCRExceptionManager.process(uiApp, e);
      return;
    }
    if(!PermissionUtil.canAddNode(destNode)) {
      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-move-node", null, 
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
    if(!destNode.isCheckedOut()) {
      uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    }
    try {
      if(nodePath.indexOf(";") > -1) {
        if(isLink) createMultiLink(nodePath.split(";"), destNode, event);
        else moveMultiNode(nodePath.split(";"), destNode, event);
      } else {
        if(isLink) createLink(nodePath, destNode, event);
        else moveNode(nodePath, null, destNode, event);
      }
      session.save();
      uiExplorer.updateAjax(event);
    } catch(AccessDeniedException ace) {
      Object[] arg = { destPath };
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
      LOG.error("an unexpected error occurs while selecting the node", e);
      JCRExceptionManager.process(uiApp, e);
      return;
    }
  }
  
  static public class EditDocumentActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent();
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class);
      Matcher matcher = FILE_EXPLORER_URL_SYNTAX.matcher(nodePath);
      String wsName = null;
      if (matcher.find()) {
        wsName = matcher.group(1);
        nodePath = matcher.group(2);
      } else {
        throw new IllegalArgumentException("The ObjectId is invalid '"+ nodePath + "'");
      }
      Session session = uiExplorer.getSessionByWorkspace(wsName);
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class);
      Node selectedNode = null;
      try {
        // Use the method getNodeByPath because it is link aware
        if (!uiExplorer.getCurrentPath().equals(nodePath)){
          uiExplorer.setCurrentPath(nodePath);
          selectedNode = uiExplorer.getCurrentNode();
        } else{
          selectedNode = uiExplorer.getNodeByPath(nodePath, session);
        }
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
      if (!PermissionUtil.canSetProperty(selectedNode)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.has-not-edit-permission",null,ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;        
      }
      Object[] arg = { nodePath };
      if (uiExplorer.nodeIsLocked(selectedNode)) {        
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }      
      if (!selectedNode.isCheckedOut()) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin",null,ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;        
      }
      EditDocumentActionComponent.editDocument(event, uicomp, uiExplorer, selectedNode, uiApp);
    }
  }

  static public class RenameActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent();
      String renameNodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      Matcher matcher = FILE_EXPLORER_URL_SYNTAX.matcher(renameNodePath);
      String wsName = null;
      if (matcher.find()) {
        wsName = matcher.group(1);
        renameNodePath = matcher.group(2);
      } else {
        throw new IllegalArgumentException("The ObjectId is invalid '"+ renameNodePath + "'");
      }
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class);
      uiExplorer.setIsHidePopup(false);
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class);
      Session session = uiExplorer.getSessionByWorkspace(wsName);
      Node renameNode = null;
      try {
        // Use the method getNodeByPath because it is link aware
        renameNode = uiExplorer.getNodeByPath(renameNodePath, session, false);
        // Reset the session to manage the links that potentially change of workspace
        session = renameNode.getSession();
        // Reset the workspace name to manage the links that potentially change of workspace 
        wsName = renameNode.getSession().getWorkspace().getName();
      } catch(PathNotFoundException path) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
            null,ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
        return;
      }
      if (!PermissionUtil.canSetProperty(renameNode)) {
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
      if(!renameNode.isCheckedOut()) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.is-checked-in", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiExplorer.updateAjax(event);
        return;
      }
      try {
        UIControl uiControl = uiExplorer.getChild(UIControl.class);
        UIActionBar uiActionBar = uiControl.getChild(UIActionBar.class);
        UIRenameForm uiRenameForm = uiActionBar.createUIComponent(UIRenameForm.class, null, null);
        uiRenameForm.update(renameNode);
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

  static public class CopyActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent();
      String srcPath = event.getRequestContext().getRequestParameter(OBJECTID);
      if(srcPath.indexOf(";") > -1) {
        uiWorkingArea.getVirtualClipboards().clear();
        uiWorkingArea.multipleCopy(srcPath.split(";"), event);
      } else {
        uiWorkingArea.processCopy(srcPath, event, false);
      }
    }
  }

  static public class CutActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent();
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      if(nodePath.indexOf(";") > -1) {
        uiWorkingArea.getVirtualClipboards().clear();
        uiWorkingArea.processMultipleCut(nodePath.split(";"), event);
      } else {
        uiWorkingArea.processCut(nodePath, event, false);
      }
    }
  }

  static public class SaveActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent();
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class);
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      Matcher matcher = FILE_EXPLORER_URL_SYNTAX.matcher(nodePath);
      String wsName = null;
      if (matcher.find()) {
        wsName = matcher.group(1);
        nodePath = matcher.group(2);
      } else {
        throw new IllegalArgumentException("The ObjectId is invalid '"+ nodePath + "'");
      }
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
        node.getSession().save();
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

  static public class DeleteActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent();
      UIJCRExplorer uiExplorer = uiWorkingArea.getAncestorOfType(UIJCRExplorer.class);
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
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
      UIPopupContainer.activate(uiConfirmMessage, 500, 180);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }

  static public class LockActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent();
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      if(nodePath.indexOf(";") > -1) {
        uiWorkingArea.processMultiLock(nodePath.split(";"), event);
      } else {
        uiWorkingArea.processLock(nodePath, event);
      }
    }
  }

  static public class UnlockActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent();
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      if(nodePath.indexOf(";") > -1) {
        uiWorkingArea.processMultiUnlock(nodePath.split(";"), event);
      } else {
        uiWorkingArea.processUnlock(nodePath, event);
      }
    }
  }

  static public class CheckInActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent();
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class);
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      Matcher matcher = FILE_EXPLORER_URL_SYNTAX.matcher(nodePath);
      String wsName = null;
      if (matcher.find()) {
        wsName = matcher.group(1);
        nodePath = matcher.group(2);
      } else {
        throw new IllegalArgumentException("The ObjectId is invalid '"+ nodePath + "'");
      }
      Session session = uiExplorer.getSessionByWorkspace(wsName);
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class);         
      // Use the method getNodeByPath because it is link aware
      Node node = uiExplorer.getNodeByPath(nodePath, session);
      // Reset the path to manage the links that potentially create virtual path
      nodePath = node.getPath();
      // Reset the session to manage the links that potentially change of workspace
      session = node.getSession();
      // Reset the workspace name to manage the links that potentially change of workspace 
      wsName = session.getWorkspace().getName();  
      if (!PermissionUtil.canSetProperty(node)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-checkin-node", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;        
      }
      if(uiExplorer.nodeIsLocked(node)) {
        Object[] arg = { nodePath };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());     
        return;
      }
      try {
        Node parentNode = node.getParent();
        uiExplorer.addLockToken(parentNode);
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

  static public class CheckOutActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent();
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class);
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      Matcher matcher = FILE_EXPLORER_URL_SYNTAX.matcher(nodePath);
      String wsName = null;
      if (matcher.find()) {
        wsName = matcher.group(1);
        nodePath = matcher.group(2);
      } else {
        throw new IllegalArgumentException("The ObjectId is invalid '"+ nodePath + "'");
      }
      Session session = uiExplorer.getSessionByWorkspace(wsName);      
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class);       
      // Use the method getNodeByPath because it is link aware
      Node node = uiExplorer.getNodeByPath(nodePath, session);
      // Reset the path to manage the links that potentially create virtual path
      nodePath = node.getPath();
      // Reset the session to manage the links that potentially change of workspace
      session = node.getSession();
      // Reset the workspace name to manage the links that potentially change of workspace 
      wsName = session.getWorkspace().getName();  
      if (!PermissionUtil.canSetProperty(node)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.can-not-checkout-node", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;        
      }
      if(uiExplorer.nodeIsLocked(node)) {
        Object[] arg = { nodePath };
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", arg, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());        
        return;
      }   
      try {
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

  static public class CustomActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uicomp = event.getSource().getParent();
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      String actionName = event.getRequestContext().getRequestParameter("actionName");
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class);
      String repository = uiExplorer.getRepositoryName();
      String wsName = event.getRequestContext().getRequestParameter(WS_NAME);
      Session session = uiExplorer.getSessionByWorkspace(wsName);
      ActionServiceContainer actionService = 
        uicomp.getApplicationComponent(ActionServiceContainer.class);
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
      try {
        Node node = uiExplorer.getNodeByPath(nodePath, session);
        String userId = event.getRequestContext().getRemoteUser();
        actionService.executeAction(userId, node, actionName, repository);
        Object[] arg = { actionName };
        uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.execute-successfully", arg));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiExplorer.updateAjax(event);
      } catch (Exception e) {
        LOG.error("an unexpected error occurs while calling custom action on the node", e);;
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
      String nodePath = null;
      Session session = null;
      if (destPath != null) {
        Matcher matcher = FILE_EXPLORER_URL_SYNTAX.matcher(destPath);
        String wsName = null;
        if (matcher.find()) {
          wsName = matcher.group(1);
          nodePath = matcher.group(2);
          session = uiExplorer.getSessionByWorkspace(wsName);
        } else {
          throw new IllegalArgumentException("The ObjectId is invalid '"+ destPath + "'");
        }        
      }
      UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
      if(uiExplorer.getAllClipBoard().size()<1) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.no-node", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      Node destNode;
      try {
        // Use the method getNodeByPath because it is link aware
        destNode = destPath == null ? uiExplorer.getCurrentNode() : uiExplorer.getNodeByPath(nodePath, session);
        // Reset the session to manage the links that potentially change of workspace
        session = destNode.getSession();
        if (destPath == null) {
          destPath = session.getWorkspace().getName() + ":" + destNode.getPath();
        }
      } catch(PathNotFoundException path) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
            null,ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
        return;
      }
      if(!PermissionUtil.canAddNode(destNode)) {
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
      if(!destNode.isCheckedOut()) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      
      try {
        if(uiWorkingArea.getVirtualClipboards().isEmpty()) {
          uiWorkingArea.processPaste(uiExplorer.getAllClipBoard().getLast(), destPath, event);
        } else {
          uiWorkingArea.processPasteMultiple(destPath, event);
        } 
      } catch (PathNotFoundException pe) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.cannot-readsource", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(!uiExplorer.getPreference().isJcrEnable()) session.save();
      uiExplorer.updateAjax(event);
    }
  }
  
  static public class AddFolderActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent();
      UIJCRExplorer uiExplorer = uiWorkingArea.getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
      Node currentNode = uiExplorer.getCurrentNode();
      if(!PermissionUtil.canAddNode(currentNode)) {
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
      AddFolderActionComponent.addFolder(event, uiExplorer);
    }
  }
  
  static public class AddDocumentActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent();
      UIJCRExplorer uiExplorer = uiWorkingArea.getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
      Node currentNode = uiExplorer.getCurrentNode();      
      if(!PermissionUtil.canAddNode(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.has-not-add-permission", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(!PermissionUtil.canAddNode(currentNode)) {
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
      AddDocumentActionComponent.addDocument(event, uiExplorer, uiApp);
    }
  }
  
  static public class UploadActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent();
      UIJCRExplorer uiExplorer = uiWorkingArea.getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
      Node currentNode = uiExplorer.getCurrentNode();
      if(!PermissionUtil.canAddNode(currentNode)) {
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
      UploadActionComponent.upload(event, uiExplorer);
    }
  }
  
  static public class MoveNodeActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent();
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      String destPath = event.getRequestContext().getRequestParameter("destInfo");
      uiWorkingArea.processMultipleSelection(nodePath.trim(), false, destPath.trim(), event);
    }
  }
  
  static public class CreateLinkActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent();
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      String destPath = event.getRequestContext().getRequestParameter("destInfo");
      uiWorkingArea.processMultipleSelection(nodePath.trim(), true, destPath.trim(), event);
    }
  }  
  
  static public class AddSymLinkActionListener extends EventListener<UIRightClickPopupMenu> {
    public void execute(Event<UIRightClickPopupMenu> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent();
      UIJCRExplorer uiExplorer = uiWorkingArea.getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
      String srcPath = event.getRequestContext().getRequestParameter(OBJECTID);
      Node currentNode = uiExplorer.getCurrentNode();
      
      if(!PermissionUtil.canAddNode(currentNode)) {
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
      
      LinkManager linkManager = uiWorkingArea.getApplicationComponent(LinkManager.class);
      String symLinkName;
      try {
        if((srcPath != null) && (srcPath.indexOf(";") > -1)) {
          String[] nodePaths = srcPath.split(";");
          for(int i=0; i< nodePaths.length; i++) {
            Matcher matcher = FILE_EXPLORER_URL_SYNTAX.matcher(nodePaths[i]);
            String wsName = null;
            if (matcher.find()) {
              wsName = matcher.group(1);
              nodePaths[i] = matcher.group(2);
            } else {
              throw new IllegalArgumentException("The ObjectId is invalid '"+ nodePaths[i] + "'");
            }            
            Session userSession = uiExplorer.getSessionByWorkspace(wsName);
            // Use the method getNodeByPath because it is link aware
            Node selectedNode = uiExplorer.getNodeByPath(nodePaths[i], userSession, false);
            // Reset the path to manage the links that potentially create virtual path
            nodePaths[i] = selectedNode.getPath();
            if(linkManager.isLink(selectedNode)) {
              Object[] args = { selectedNode.getPath() };
              uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.selected-is-link", args, 
                  ApplicationMessage.WARNING));
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
              continue;
            }
            try {
              if (selectedNode.getName().indexOf(".lnk") > -1) symLinkName = selectedNode.getName(); 
              else symLinkName = selectedNode.getName() + ".lnk";
              linkManager.createLink(currentNode, Utils.EXO_SYMLINK, selectedNode, symLinkName);
            } catch(Exception e) {
              Object[] arg = {selectedNode.getPath(), currentNode.getPath()};
              uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.create-link-problem", arg, 
                  ApplicationMessage.WARNING));
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
              return;
            }
          }
          uiExplorer.updateAjax(event);
        } else {
          if(srcPath != null) {
            Matcher matcher = FILE_EXPLORER_URL_SYNTAX.matcher(srcPath);
            String wsName = null;
            if (matcher.find()) {
              wsName = matcher.group(1);
              srcPath = matcher.group(2);
            } else {
              throw new IllegalArgumentException("The ObjectId is invalid '"+ srcPath + "'");
            }
            Session userSession = uiExplorer.getSessionByWorkspace(wsName);            
            // Use the method getNodeByPath because it is link aware
            Node selectedNode = uiExplorer.getNodeByPath(srcPath, userSession, false);
            // Reset the path to manage the links that potentially create virtual path
            srcPath = selectedNode.getPath();
            // Reset the session to manage the links that potentially change of workspace
            userSession = selectedNode.getSession();
            if(linkManager.isLink(selectedNode)) {
              Object[] args = { selectedNode.getPath() };
              uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.selected-is-link", args, 
                  ApplicationMessage.WARNING));
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
              return;
            }
            if (selectedNode.getName().indexOf(".lnk") > -1) symLinkName = selectedNode.getName(); 
            else symLinkName = selectedNode.getName() + ".lnk";
            try {
              linkManager.createLink(currentNode, Utils.EXO_SYMLINK, selectedNode, symLinkName);
            } catch(Exception e) {
              Object[] arg = {selectedNode.getPath(), currentNode.getPath()};
              uiApp.addMessage(new ApplicationMessage("UIWorkingArea.msg.create-link-problem", arg, 
                  ApplicationMessage.WARNING));
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
              return;
            }
            uiExplorer.updateAjax(event);
          } else {
            UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
            UISymLinkManager uiSymLinkManager = event.getSource().createUIComponent(UISymLinkManager.class, null, null);
            UIPopupContainer.activate(uiSymLinkManager, 600, 300);
            event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
          }
        }
      } catch (AccessControlException ace) {        
        uiApp.addMessage(new ApplicationMessage("UISymLinkForm.msg.repository-exception", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (AccessDeniedException ade) {        
        uiApp.addMessage(new ApplicationMessage("UISymLinkForm.msg.repository-exception", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch(NumberFormatException nume) {
        uiApp.addMessage(new ApplicationMessage("UISymLinkForm.msg.numberformat-exception", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch(ConstraintViolationException cve) {
        uiApp.addMessage(new ApplicationMessage("UISymLinkForm.msg.cannot-save", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch(ItemExistsException iee) {
        uiApp.addMessage(new ApplicationMessage("UISymLinkForm.msg.item-exists-exception", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch(Exception e) {
        LOG.error("an unexpected error occurs while adding a symlink to the node", e);
        uiApp.addMessage(new ApplicationMessage("UISymLinkForm.msg.cannot-save", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
    }
  }  
}
