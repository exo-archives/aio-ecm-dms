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

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.jcr.JCRExceptionManager;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIGrid;
import org.exoplatform.webui.component.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
    events = {@EventConfig (listeners = UIPermissionInfo.DeleteActionListener.class, confirm = "UIPermissionInfo.msg.confirm-delete-permission")}
)

public class UIPermissionInfo extends UIContainer {

  private static String[] PERMISSION_BEAN_FIELD = {"usersOrGroups", "read", "addNode", 
                                                   "setProperty", "remove"} ;
  private static String[] PERMISSION_ACTION = {"Delete"} ;

  public UIPermissionInfo() throws Exception {
    UIGrid uiGrid = createUIComponent(UIGrid.class, null, "PermissionInfo") ;
    addChild(uiGrid) ;
    uiGrid.getUIPageIterator().setId("PermissionInfoIterator");
    uiGrid.configure("usersOrGroups", PERMISSION_BEAN_FIELD, PERMISSION_ACTION) ;
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

  static public class DeleteActionListener extends EventListener<UIPermissionInfo> {
    public void execute(Event<UIPermissionInfo> event) throws Exception {
      UIPermissionInfo uicomp = event.getSource() ;
      UIJCRExplorer uiJCRExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      ExtendedNode node = (ExtendedNode)uiJCRExplorer.getCurrentNode() ; 
      String name = event.getRequestContext().getRequestParameter(OBJECTID) ;
      try {
        node.removePermission(name) ;
        uicomp.updateGrid() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uicomp.getParent()) ;
        node.save() ;
        if(!uiJCRExplorer.getPreference().isJcrEnable()) {
          uiJCRExplorer.getSession().save() ;
          uiJCRExplorer.getSession().refresh(false) ;
        }
        uiJCRExplorer.setIsHidePopup(true) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uicomp.getParent()) ;
        uiJCRExplorer.updateAjax(event) ;
      } catch (Exception e) {
        JCRExceptionManager.process(uicomp.getAncestorOfType(UIApplication.class), e);
      }
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

