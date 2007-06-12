/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.metadata;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.nodetype.NodeType;

import org.exoplatform.ecm.webui.component.UIECMPermissionBrowser;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Sep 19, 2006
 * 11:45:11 AM 
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIMetadataManager extends UIContainer {
  
  public String METADATA_POPUP = "MetadataPopupEdit" ;
  final static public String VIEW_METADATA_POPUP = "ViewMetadataPopup" ;
  final static public String PERMISSION_POPUP = "PermissionPopup" ;
  public List<String> metadatasDeleted = new ArrayList<String>() ;
  
  public UIMetadataManager() throws Exception {
    addChild(UIMetadataList.class, null, null) ;
  }
  
  public void initPopup() throws Exception {
    removeChildById(METADATA_POPUP) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, METADATA_POPUP);
    uiPopup.setWindowSize(650, 450);
    UIMetadataForm uiMetaForm = createUIComponent(UIMetadataForm.class, null, null) ;
    uiPopup.setUIComponent(uiMetaForm) ;
    uiPopup.setRendered(true);
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;    
  }
  
  public void initViewPopup(String metadataName) throws Exception {
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, VIEW_METADATA_POPUP);
    uiPopup.setShow(true) ;
    uiPopup.setWindowSize(600, 500);
    uiPopup.setRendered(true);
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    ExtendedNodeTypeManager ntManager = repositoryService.getRepository(repository).getNodeTypeManager() ;
    NodeType nodeType = ntManager.getNodeType(metadataName) ;
    UIMetadataView uiView = uiPopup.createUIComponent(UIMetadataView.class, null, null) ;
    uiView.setMetadata(nodeType) ;
    uiPopup.setUIComponent(uiView) ;
    uiPopup.setResizable(true) ;
  }
  
  public void initPopupPermission(String membership) throws Exception {
    removeChildById(PERMISSION_POPUP) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, PERMISSION_POPUP);
    uiPopup.setWindowSize(560, 300);
    UIECMPermissionBrowser uiECMPermission = 
      createUIComponent(UIECMPermissionBrowser.class, null, "MetadataPermission") ;
    if(membership != null && membership.indexOf(":/") > -1) {
      String[] arrMember = membership.split(":/") ;
      uiECMPermission.setCurrentPermission("/" + arrMember[1]) ;
    }
    uiPopup.setUIComponent(uiECMPermission);
    UIMetadataForm uiForm = findFirstComponentOfType(UIMetadataForm.class) ;
    uiECMPermission.setComponent(uiForm, null) ;
    uiPopup.setRendered(true) ;
    uiPopup.setShow(true) ;
  }
}
