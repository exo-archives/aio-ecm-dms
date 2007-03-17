/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.exoplatform.ecm.jcr.model.Preference;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.control.UIActionBar;
import org.exoplatform.ecm.webui.component.explorer.control.UIControl;
import org.exoplatform.ecm.webui.component.explorer.control.UIViewBar;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UITreeExplorer;
import org.exoplatform.portal.component.view.Util;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.model.SelectItemOption;
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
 */
@ComponentConfig(
    template =  "app:/groovy/webui/component/explorer/UIDrivesBrowser.gtmpl",
    events = @EventConfig(listeners = UIDrivesBrowser.SelectDriveActionListener.class) 
)
public class UIDrivesBrowser extends UIContainer {

  public UIDrivesBrowser() throws Exception {
  }

  public List<String> getDrives() throws Exception {
    List<String> driveList = new ArrayList<String>() ;
    OrganizationService oservice = getApplicationComponent(OrganizationService.class) ;
    String username = Util.getUIPortal().getOwner() ;
    Collection memberships = oservice.getMembershipHandler().findMembershipsByUser(username) ;
    ManageDriveService dservice = getApplicationComponent(ManageDriveService.class) ;
    if(memberships == null || memberships.size() < 0) return driveList ;
    Object[] objects = memberships.toArray() ;
    for(int i = 0 ; i < objects.length ; i ++ ){
      Membership membership = (Membership)objects[i] ;
      String role = membership.getMembershipType() + ":" + membership.getGroupId() ;
      List wsByPermission = new ArrayList() ;
      wsByPermission = dservice.getAllDriveByPermission(role) ;
      if(wsByPermission != null && wsByPermission.size() > 0) {
        for(int j = 0; j < wsByPermission.size(); j ++) {
          DriveData data = (DriveData)wsByPermission.get(j) ;
          driveList.add(data.getName() + "," + data.getWorkspace()) ;
        }
        Collections.sort(driveList) ;
      }
    }
    return driveList ; 
  }

  static  public class SelectDriveActionListener extends EventListener<UIDrivesBrowser> {
    public void execute(Event<UIDrivesBrowser> event) throws Exception {
      UIDrivesBrowser uiDrive = event.getSource() ;
      String driveName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      RepositoryService rservice = uiDrive.getApplicationComponent(RepositoryService.class) ;
      ManageDriveService dservice = uiDrive.getApplicationComponent(ManageDriveService.class) ;
      DriveData drive = (DriveData) dservice.getDriveByName(driveName) ;
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext() ;
      PortletRequest request = context.getRequest() ; 
      PortletPreferences preferences = request.getPreferences() ;
      preferences.setValue(Utils.WORKSPACE_NAME, drive.getWorkspace()) ;
      preferences.setValue(Utils.JCR_PATH, drive.getHomePath()) ;
      preferences.setValue(Utils.VIEWS, drive.getViews()) ;
      preferences.setValue(Utils.DRIVE, drive.getName()) ;
      preferences.store() ;

      UIJCRExplorerPortlet uiParent = uiDrive.getParent() ;
      UIJCRExplorer uiJCRExplorer = uiParent.getChild(UIJCRExplorer.class) ;
      
      Preference pref = uiJCRExplorer.getPreference() ;
      pref.setShowSideBar(true) ;
      pref.setShowNonDocumentType(drive.getViewNonDocument()) ;
      pref.setShowPreferenceDocuments(drive.getViewPreferences()) ;
      pref.setEmpty(false) ;
      
      ManageableRepository repository = rservice.getRepository() ;
      Session session = repository.getSystemSession(drive.getWorkspace()) ;
      uiJCRExplorer.setSession(session) ;      
      Node node = (Node) session.getItem(drive.getHomePath()) ;
      uiJCRExplorer.getAllClipBoard().clear() ;
      uiJCRExplorer.findFirstComponentOfType(UITreeExplorer.class).setTreeRoot(node) ;
      uiJCRExplorer.setSelectNode(node) ;
      uiJCRExplorer.refreshExplorer() ;
      
      String[] arrView = drive.getViews().split(",") ;
      List<SelectItemOption<String>> viewOptions = 
        new ArrayList<SelectItemOption<String>> (arrView.length) ;
      for(int i = 0; i < arrView.length; i ++) {
        viewOptions.add(new SelectItemOption<String>(arrView[i].trim(), arrView[i].trim())) ;
      }
      UIControl uiControl = uiJCRExplorer.getChild(UIControl.class) ;
      UIActionBar uiActionbar = uiControl.getChild(UIActionBar.class) ;
      UIViewBar uiViewBar = uiControl.getChild(UIViewBar.class) ;
      uiViewBar.setViewOptions(viewOptions) ;
      uiActionbar.setTabOptions(arrView[0].trim()) ;
      uiParent.setRenderedChild(UIJCRExplorer.class) ;
    }
  }
}

