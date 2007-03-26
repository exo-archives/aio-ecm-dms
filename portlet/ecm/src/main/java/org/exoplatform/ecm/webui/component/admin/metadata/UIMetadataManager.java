/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.metadata;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.ecm.webui.component.UIECMPermissionBrowser;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Sep 19, 2006
 * 11:45:11 AM 
 */
@ComponentConfig(
    lifecycle = UIContainerLifecycle.class
)
public class UIMetadataManager extends UIContainer {
  
  public String METADATA_POPUP = "MetadataPopup" ;
  final static public String VIEW_METADATA_POPUP = "ViewMetadataPopup" ;
  final static public String PERMISSION_POPUP = "PermissionPopup" ;
  
  public UIMetadataManager() throws Exception {
    addChild(UIMetadataList.class, null, null) ;
  }
  
  public void initPopup(boolean isAddNew) throws Exception {
    if(isAddNew) METADATA_POPUP = "MetadataPopupAddNew" ;
    else METADATA_POPUP =  "MetadataPopupEdit" ;
    removeChildById(METADATA_POPUP) ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    CmsConfigurationService cmsConfigService = getApplicationComponent(CmsConfigurationService.class) ;
    Session session = 
      repositoryService.getRepository().getSystemSession(cmsConfigService.getWorkspace()) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, METADATA_POPUP);
    uiPopup.setWindowSize(650, 450);
    UIMetadataList uiMetaList = getChild(UIMetadataList.class) ;
    UIMetadataForm uiMetaForm = createUIComponent(UIMetadataForm.class, null, null) ;
    
    NodeTypeManager ntManager = session.getWorkspace().getNodeTypeManager() ;
    NodeTypeIterator mixinNodetype = ntManager.getMixinNodeTypes() ;
    List<String> mapping = new ArrayList<String>() ;
    while (mixinNodetype.hasNext()) {
      NodeType nt = mixinNodetype.nextNodeType() ;
      if(!uiMetaList.getMetadatas().contains(nt.getName())) {
        mapping.add(nt.getName()) ;
      }
    }
    if(mapping.size() > 0) {
      uiMetaForm.update(mapping) ;
    } else {
      UIApplication uiApp = uiMetaList.getAncestorOfType(UIApplication.class) ;
      uiApp.addMessage(new ApplicationMessage("UIMetadataManager.msg.already-mapped", null));
      return;
    }
    uiPopup.setUIComponent(uiMetaForm) ;
    uiPopup.setRendered(true);
    uiPopup.setShow(true) ;
        
  }
  
  public void initViewPopup(String metadataName) throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    CmsConfigurationService cmsConfigService = 
      getApplicationComponent(CmsConfigurationService.class) ;
    Session session = 
      repositoryService.getRepository().getSystemSession(cmsConfigService.getWorkspace()) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, VIEW_METADATA_POPUP);
    uiPopup.setShow(true) ;
    uiPopup.setWindowSize(600, 500);    
    uiPopup.setRendered(true);    
    NodeTypeManager ntManager = session.getWorkspace().getNodeTypeManager();
    NodeType nodeType = ntManager.getNodeType(metadataName) ;
    UIMetadataView uiView = uiPopup.createUIComponent(UIMetadataView.class, null, null) ;
    uiView.setMetadata(nodeType) ;
    uiPopup.setUIComponent(uiView) ;
  }
  
  public void initPopupPermission() throws Exception {
    removeChildById(PERMISSION_POPUP) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, PERMISSION_POPUP);
    uiPopup.setWindowSize(560, 300);
    UIECMPermissionBrowser uiECMPermission = 
      createUIComponent(UIECMPermissionBrowser.class, null, null) ;
    uiPopup.setUIComponent(uiECMPermission);
    UIMetadataForm uiForm = findFirstComponentOfType(UIMetadataForm.class) ;
    uiECMPermission.setComponent(uiForm, null) ;
    uiPopup.setRendered(true) ;
    uiPopup.setShow(true) ;
  }
}
