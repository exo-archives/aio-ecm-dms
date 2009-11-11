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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.exoplatform.ecm.jcr.model.ClipboardCommand;
import org.exoplatform.ecm.webui.component.admin.manager.UIAbstractManagerComponent;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UISideBar;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.link.NodeLinkAware;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIRightClickPopupMenu;
import org.exoplatform.webui.ext.UIExtension;
import org.exoplatform.webui.ext.UIExtensionManager;

/**
 * Created by The eXo Platform SARL Author : Tran The Trong trongtt@gmail.com
 * July 3, 2006 10:07:15 AM
 */
@ComponentConfigs( {
    @ComponentConfig(template = "app:/groovy/webui/component/explorer/UIWorkingArea.gtmpl"),
    @ComponentConfig(
        type = UIRightClickPopupMenu.class, 
        id = "ECMContextMenu", 
        template = "app:/groovy/webui/component/explorer/UIRightClickPopupMenu.gtmpl"
    )
})
public class UIWorkingArea extends UIContainer {

  /**
   * Logger.
   */
  private static final Log                 LOG                      = ExoLogger.getLogger("dms.UIWorkingArea");

  public static final Pattern              FILE_EXPLORER_URL_SYNTAX = Pattern
                                                                        .compile("([^:/]+):(/.*)");

  public static final String               WS_NAME                  = "workspaceName";

  public static final String               EXTENSION_TYPE           = "org.exoplatform.ecm.dms.UIWorkingArea";

  public static final String               ITEM_CONTEXT_MENU      = "ItemContextMenu";

  public static final String               MULTI_ITEM_CONTEXT_MENU      = "MultiItemContextMenu";

  public static final String               GROUND_CONTEXT_MENU      = "GroundContextMenu";

  public static final String               ITEM_GROUND_CONTEXT_MENU = "ItemGroundContextMenu";
  
  public static final String               MOVE_NODE = "MoveNode";

  public static final String               CREATE_LINK = "CreateLink";

  public static final String               CUSTOM_ACTIONS = "CustomActions";

  public static final String               PERMLINK = "PermlinkContextMenu";

  private List<UIAbstractManagerComponent> managers                 = Collections
                                                                        .synchronizedList(new ArrayList<UIAbstractManagerComponent>());

  private List<ClipboardCommand>           virtualClipboards_       = Collections
                                                                        .synchronizedList(new LinkedList<ClipboardCommand>());

  public UIWorkingArea() throws Exception {
    addChild(UIRightClickPopupMenu.class, "ECMContextMenu", null);
    addChild(UISideBar.class, null, null);
    addChild(UIDocumentWorkspace.class, null, null);
    addChild(UIDrivesArea.class, null, null).setRendered(false);
  }

  private List<UIExtension> getUIExtensionList() {
    UIExtensionManager manager = getApplicationComponent(UIExtensionManager.class);
    return manager.getUIExtensions(EXTENSION_TYPE);
  }
  
  public synchronized void initialize() throws Exception {
    List<UIExtension> extensions = getUIExtensionList();
    if (extensions == null) {
      return;
    }
    managers.clear();
    for (UIExtension extension : extensions) {
      UIComponent component = addUIExtension(extension, null);
      if (component !=null && !managers.contains(component))
        managers.add((UIAbstractManagerComponent)component);
    }
  }
  
  private synchronized UIComponent addUIExtension(UIExtension extension, Map<String, Object> context) throws Exception {
    UIExtensionManager manager = getApplicationComponent(UIExtensionManager.class);
    UIComponent component = manager.addUIExtension(extension, context, this);
    if (component instanceof UIAbstractManagerComponent) {
      // You can access to the given extension and the extension is valid
      UIAbstractManagerComponent uiAbstractManagerComponent = (UIAbstractManagerComponent) component;
      uiAbstractManagerComponent.setUIExtensionName(extension.getName());
      uiAbstractManagerComponent.setUIExtensionCategory(extension.getCategory());
      return component;
    } else if (component != null) {
      // You can access to the given extension but the extension is not valid
      LOG.warn("All the extension '" + extension.getName() + "' of type '" + EXTENSION_TYPE
          + "' must be associated to a component of type " + UIAbstractManagerComponent.class);
      removeChild(component.getClass());
    }
    return null;
  }
  
  List<UIAbstractManagerComponent> getManagers() {
    return managers;
  }

  void unregister(UIAbstractManagerComponent component) {
    managers.remove(component);
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

  public Node getNodeByUUID(String uuid) throws Exception {
    String repository = getAncestorOfType(UIJCRExplorer.class).getRepositoryName();
    ManageableRepository repo = getApplicationComponent(RepositoryService.class).getRepository(
        repository);
    String workspace = repo.getConfiguration().getDefaultWorkspaceName();
    Session session = SessionProviderFactory.createSystemProvider().getSession(workspace, repo);
    return session.getNodeByUUID(uuid);
  }

  public boolean isPreferenceNode(Node node) {
    return getAncestorOfType(UIJCRExplorer.class).isPreferenceNode(node);
  }

  public String getVersionNumber(Node node) throws RepositoryException {
    if (!Utils.isVersionable(node))
      return "-";
    return node.getBaseVersion().getName();
  }

  public boolean isJcrViewEnable() throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    return uiExplorer.getPreference().isJcrEnable();
  }

  private Map<String, Object> createContext(Node currentNode) throws Exception {
    Map<String, Object> context = new HashMap<String, Object>();
    WebuiRequestContext requestContext =  WebuiRequestContext.getCurrentInstance() ;
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    UIApplication uiApp = getAncestorOfType(UIApplication.class);
    context.put(Node.class.getName(), currentNode);
    context.put(UIWorkingArea.class.getName(), this);
    context.put(UIApplication.class.getName(), uiApp);
    context.put(UIJCRExplorer.class.getName(), uiExplorer);
    context.put(WebuiRequestContext.class.getName(), requestContext);
    return context;
  }
  
  List<UIComponent> getGroundActionsExtensionList() throws Exception {
    List<UIComponent> uiGroundActionList = new ArrayList<UIComponent>();
    List<UIExtension> uiExtensionList = getUIExtensionList();
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    Node currentNode = uiExplorer.getCurrentNode();
    UIComponent uiAddedActionManage;
    for (UIExtension uiextension : uiExtensionList) {
      if (GROUND_CONTEXT_MENU.equals(uiextension.getCategory())
          || ITEM_GROUND_CONTEXT_MENU.equals(uiextension.getCategory())) {
        uiAddedActionManage = addUIExtension(uiextension, createContext(currentNode));
        if (uiAddedActionManage != null) {
          if (!uiGroundActionList.contains(uiAddedActionManage))
            uiGroundActionList.add(uiAddedActionManage);
        }
      }
    }
    return uiGroundActionList;
  }

  List<UIComponent> getMultiActionsExtensionList() throws Exception {
    List<UIComponent> uiActionList = new ArrayList<UIComponent>();
    List<UIExtension> uiExtensionList = getUIExtensionList();
    UIComponent uiAddedActionManage;
      for (UIExtension uiextension : uiExtensionList) {
        if (ITEM_CONTEXT_MENU.equals(uiextension.getCategory())
            || ITEM_GROUND_CONTEXT_MENU.equals(uiextension.getCategory())
            || MULTI_ITEM_CONTEXT_MENU.equals(uiextension.getCategory())) {
          uiAddedActionManage = addUIExtension(uiextension, null);
          if (uiAddedActionManage != null) {
            if (!uiActionList.contains(uiAddedActionManage))
              uiActionList.add(uiAddedActionManage);
          }
        }
      }  
    return uiActionList;
  }
  
  public String getActionsExtensionList(Node node) throws Exception {
    StringBuffer actionsList = new StringBuffer(1024);
    List<UIExtension> uiExtensionList = getUIExtensionList();
    UIComponent uiAddedActionManage;
    for (UIExtension uiextension : uiExtensionList) {
      if (uiextension.getCategory().startsWith(ITEM_CONTEXT_MENU)
          || ITEM_GROUND_CONTEXT_MENU.equals(uiextension.getCategory())) {
        uiAddedActionManage = addUIExtension(uiextension, createContext(node));
        if (uiAddedActionManage != null) {
          actionsList.append(uiextension.getName()).append(",");
        }
      }
    }
    if (actionsList.length() > 0) {
      return actionsList.substring(0, actionsList.length() - 1);
    }
    return actionsList.toString();
  }

  public synchronized UIComponent getJCRMoveAction() throws Exception {
    UIExtensionManager manager = getApplicationComponent(UIExtensionManager.class);
    return manager.addUIExtension(EXTENSION_TYPE, MOVE_NODE, null, this);
  }

  public synchronized UIComponent getCreateLinkAction() throws Exception {
    UIExtensionManager manager = getApplicationComponent(UIExtensionManager.class);
    return manager.addUIExtension(EXTENSION_TYPE, CREATE_LINK, null, this);
  }

  public synchronized UIComponent getPermlink(Node node) throws Exception {
    UIComponent uicomponent = null;
    List<UIExtension> uiExtensionList = getUIExtensionList();
    for (UIExtension uiextension : uiExtensionList) {
      if (PERMLINK.equals(uiextension.getCategory())) {
        uicomponent = addUIExtension(uiextension, createContext(node));
      }
    }
    return uicomponent;
  }
  
  public synchronized UIComponent getCustomAction() throws Exception {
    UIComponent uicomponent = null;
    List<UIExtension> uiExtensionList = getUIExtensionList();
    for (UIExtension uiextension : uiExtensionList) {
      if (CUSTOM_ACTIONS.equals(uiextension.getCategory())) {
        uicomponent = addUIExtension(uiextension, null);
      }
    }
    return uicomponent;
  }

  private boolean hasPermission(String userName, Value[] roles) throws Exception {
    IdentityRegistry identityRegistry = getApplicationComponent(IdentityRegistry.class);
    if (SystemIdentity.SYSTEM.equalsIgnoreCase(userName)) {
      return true;
    }
    Identity identity = identityRegistry.getIdentity(userName);
    if (identity == null) {
      return false;
    }
    for (int i = 0; i < roles.length; i++) {
      String role = roles[i].getString();
      if ("*".equalsIgnoreCase(role))
        return true;
      MembershipEntry membershipEntry = MembershipEntry.parse(role);
      if (membershipEntry == null)
        return false;
      if (identity.isMemberOf(membershipEntry)) {
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
    List<Node> unsafeActions = actionContainer.getCustomActionsNode(node,
        ActionServiceContainer.READ_PHASE);
    if (unsafeActions == null)
      return new ArrayList<Node>();
    for (Node actionNode : unsafeActions) {
      Value[] roles = actionNode.getProperty(Utils.EXO_ROLES).getValues();
      if (hasPermission(userName, roles))
        safeActions.add(actionNode);
    }
    return safeActions;
  }
}
