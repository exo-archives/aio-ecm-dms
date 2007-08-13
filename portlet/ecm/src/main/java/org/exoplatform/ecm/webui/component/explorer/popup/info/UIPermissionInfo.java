/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.portlet.PortletPreferences;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.explorer.UIDrivesBrowser;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : nqhungvn
 *          nguyenkequanghung@yahoo.com
 * July 3, 2006
 * 10:07:15 AM
 * Editor : TuanP
 *        phamtuanchip@yahoo.de
 * Oct 13, 20006  
 */

@ComponentConfig(
    lifecycle = UIContainerLifecycle.class,
    events = {
      @EventConfig (listeners = UIPermissionInfo.DeleteActionListener.class, confirm = "UIPermissionInfo.msg.confirm-delete-permission"),
      @EventConfig (listeners = UIPermissionInfo.EditActionListener.class) 
    }
)

public class UIPermissionInfo extends UIContainer {

  public static String[] PERMISSION_BEAN_FIELD = {"usersOrGroups", "read", "addNode", 
    "setProperty", "remove"} ;
  private static String[] PERMISSION_ACTION = {"Edit", "Delete"} ;

  public UIPermissionInfo() throws Exception {
    UIGrid uiGrid = createUIComponent(UIGrid.class, null, "PermissionInfo") ;
    addChild(uiGrid) ;
    uiGrid.getUIPageIterator().setId("PermissionInfoIterator");
    uiGrid.configure("usersOrGroups", PERMISSION_BEAN_FIELD, PERMISSION_ACTION) ;
  }
  private String  getExoOwner(Node node) throws Exception {
    return Utils.getNodeOwner(node) ;
  }
  public void updateGrid() throws Exception {
    UIJCRExplorer uiJCRExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    Node currentNode = uiJCRExplorer.getCurrentNode() ;
    List<PermissionBean> permBeans = new ArrayList<PermissionBean>(); 
    ExtendedNode node = (ExtendedNode) currentNode ;

    List permsList = node.getACL().getPermissionEntries() ;
    Map<String, List<String>> permsMap = new HashMap<String, List<String>>() ;
    Iterator perIter = permsList.iterator() ;
    while(perIter.hasNext()) {
      AccessControlEntry accessControlEntry = (AccessControlEntry)perIter.next() ;
      String currentIdentity = accessControlEntry.getIdentity();
      String currentPermission = accessControlEntry.getPermission();
      List<String> currentPermissionsList = permsMap.get(currentIdentity);
      if(!permsMap.containsKey(currentIdentity)) {
        permsMap.put(currentIdentity, null) ;
      }
      if(currentPermissionsList == null) currentPermissionsList = new ArrayList<String>() ;
      if(!currentPermissionsList.contains(currentPermission)) {
        currentPermissionsList.add(currentPermission) ;
      }
      permsMap.put(currentIdentity, currentPermissionsList) ;
    }
    Set keys = permsMap.keySet(); 
    Iterator keysIter = keys.iterator() ;
    //TODO Utils.getExoOwner(node) has exception return SystemIdentity.SYSTEM
    String owner = SystemIdentity.SYSTEM ;
    if(getExoOwner(node) != null) owner = getExoOwner(node) ;
    PermissionBean permOwnerBean = new PermissionBean();
    permOwnerBean.setUsersOrGroups(owner);
    permOwnerBean.setRead(true) ;
    permOwnerBean.setAddNode(true) ;
    permOwnerBean.setSetProperty(true) ;
    permOwnerBean.setRemove(true) ;
    permBeans.add(permOwnerBean);

    while(keysIter.hasNext()) {
      String userOrGroup = (String) keysIter.next();            
      List<String> permissions = permsMap.get(userOrGroup);      
      PermissionBean permBean = new PermissionBean();
      permBean.setUsersOrGroups(userOrGroup);
      for(String perm : permissions) {
        if(PermissionType.READ.equals(perm)) permBean.setRead(true);
        else if(PermissionType.ADD_NODE.equals(perm)) permBean.setAddNode(true);
        else if(PermissionType.SET_PROPERTY.equals(perm)) permBean.setSetProperty(true);
        else if(PermissionType.REMOVE.equals(perm)) permBean.setRemove(true);
      }
      permBeans.add(permBean);
    }
    UIGrid uiGrid = findFirstComponentOfType(UIGrid.class) ; 
    ObjectPageList objPageList = new ObjectPageList(permBeans, 10) ;
    uiGrid.getUIPageIterator().setPageList(objPageList) ;    
  }
  static public class EditActionListener extends EventListener<UIPermissionInfo> {
    public void execute(Event<UIPermissionInfo> event) throws Exception {
      UIPermissionInfo uicomp = event.getSource() ;
      String name = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRExplorer uiJCRExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      ExtendedNode node = (ExtendedNode)uiJCRExplorer.getCurrentNode() ; 
      UIPermissionForm uiForm = uicomp.getAncestorOfType(UIPermissionManager.class).getChild(UIPermissionForm.class) ;
      uiForm.fillForm(name, node) ;
      uiForm.lockForm(name.equals(uicomp.getExoOwner(node)));
    }
  }
  static public class DeleteActionListener extends EventListener<UIPermissionInfo> {
    public void execute(Event<UIPermissionInfo> event) throws Exception {
      UIPermissionInfo uicomp = event.getSource() ;
      UIJCRExplorer uiJCRExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      ExtendedNode node = (ExtendedNode)uiJCRExplorer.getCurrentNode() ; 
      String name = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class) ;
      if(!uiJCRExplorer.getCurrentNode().isCheckedOut()) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(name.equals(uicomp.getExoOwner(node))) {
        uiApp.addMessage(new ApplicationMessage("UIPermissionInfo.msg.no-permission-remove", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(Utils.hasChangePermissionRight(node)) {
        if(node.canAddMixin("exo:privilegeable")) node.addMixin("exo:privilegeable");
        node.removePermission(name) ;        
        node.save() ;
        if(uiJCRExplorer.getRootNode().equals(node)) {
          if(!Utils.isReadAuthorized(uiJCRExplorer.getCurrentNode())) {
            PortletPreferences prefs_ = uiJCRExplorer.getPortletPreferences();
            prefs_.setValue(Utils.WORKSPACE_NAME,"") ;
            prefs_.setValue(Utils.VIEWS,"") ;
            prefs_.setValue(Utils.JCR_PATH,"") ;
            prefs_.setValue(Utils.DRIVE,"") ;
            prefs_.store() ;
            uiJCRExplorer.setRenderSibbling(UIDrivesBrowser.class) ;
            return ;
          }
        }
        if(!uiJCRExplorer.getPreference().isJcrEnable()) {
          uiJCRExplorer.getSession().save() ;
          uiJCRExplorer.getSession().refresh(false) ;
        }
      } else {
        uiApp.addMessage(new ApplicationMessage("UIPermissionInfo.msg.no-permission-tochange", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIPopupAction uiPopup = uicomp.getAncestorOfType(UIPopupAction.class) ;
      if(!Utils.isReadAuthorized(node)) {
        uiJCRExplorer.setSelectNode(uiJCRExplorer.getRootNode().getPath(), uiJCRExplorer.getSession()) ;
        uiPopup.deActivate() ;
      } else {
        uicomp.updateGrid() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uicomp.getParent()) ;
      }
      uiJCRExplorer.setIsHidePopup(true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
      uiJCRExplorer.updateAjax(event) ;
    }
  }

  public class PermissionBean {    

    private String usersOrGroups ;
    private boolean read ;
    private boolean addNode ;
    private boolean setProperty ;
    private boolean remove ;    

    public String getUsersOrGroups() { return usersOrGroups ; }
    public void setUsersOrGroups(String s) { usersOrGroups = s ; }

    public boolean isAddNode() { return addNode ; }
    public void setAddNode(boolean b) { addNode = b ; }

    public boolean isRead() { return read ; }
    public void setRead(boolean b) { read = b ; }

    public boolean isRemove() { return remove ; }
    public void setRemove(boolean b) { remove = b ; }

    public boolean isSetProperty() { return setProperty ; }
    public void setSetProperty(boolean b) { setProperty = b ; }
  }
}

