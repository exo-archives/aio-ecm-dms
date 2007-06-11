/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletPreferences;

import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.ecm.jcr.model.Preference;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.control.UIActionBar;
import org.exoplatform.ecm.webui.component.explorer.control.UIControl;
import org.exoplatform.ecm.webui.component.explorer.control.UIViewBar;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.bean.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
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
    events = {
        @EventConfig(listeners = UIDrivesBrowser.SelectDriveActionListener.class),
        @EventConfig(listeners = UIDrivesBrowser.SelectRepoActionListener.class)
    } 

)
public class UIDrivesBrowser extends UIContainer {
  
  private String repoName_ ;
  
  public UIDrivesBrowser() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;
    repoName_ = rService.getDefaultRepository().getConfiguration().getName() ;
  }

  public List<String> getRepositoryList() {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;    
    List<String> repositories = new ArrayList<String>() ;    
    for( RepositoryEntry re : rService.getConfig().getRepositoryConfigurations()) {
      repositories.add(re.getName()) ;
    }
    return repositories ;
  }

  public String getRepository(){return repoName_ ;}
  public void setRepository(String repoName){repoName_ = repoName ;}

  public List<DriveData> getDrives(String repoName) throws Exception {
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    ManageDriveService driveService = getApplicationComponent(ManageDriveService.class) ;
    ManageableRepository repository = rservice.getRepository(repoName) ;  
    Session digitalSession = repository.getSystemSession("digital-assets") ;    
    List<DriveData> driveList = new ArrayList<DriveData>() ;
    List<String> userRoles = Utils.getMemberships() ;
    for(String role : userRoles ){
      List<DriveData> drives = driveService.getAllDriveByPermission(role) ;
      if(drives != null && drives.size() > 0) {
        for(DriveData drive : drives) {
          if(drive.getIcon() != null && drive.getIcon().length() > 0) {
            Node node = (Node) digitalSession.getItem(drive.getIcon()) ;
            Node jcrContentNode = node.getNode(Utils.JCR_CONTENT) ;
            InputStream input = jcrContentNode.getProperty(Utils.JCR_DATA).getStream() ;
            InputStreamDownloadResource dresource = new InputStreamDownloadResource(input, "image") ;
            dresource.setDownloadName(node.getName()) ;
            drive.setIcon(dservice.getDownloadLink(dservice.addDownloadResource(dresource))) ;
          }
          driveList.add(drive) ;
        }
      }
    }
    Collections.sort(driveList) ;
    return driveList ; 
  }
  static  public class SelectRepoActionListener extends EventListener<UIDrivesBrowser> {
    public void execute(Event<UIDrivesBrowser> event) throws Exception {
      String repoName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIDrivesBrowser uiDrivesBrowser = event.getSource() ;
      uiDrivesBrowser.setRepository(repoName) ;      
    }
  }
  static  public class SelectDriveActionListener extends EventListener<UIDrivesBrowser> {
    public void execute(Event<UIDrivesBrowser> event) throws Exception {
      UIDrivesBrowser uiDrive = event.getSource() ;
      String driveName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      RepositoryService rservice = uiDrive.getApplicationComponent(RepositoryService.class) ;
      ManageDriveService dservice = uiDrive.getApplicationComponent(ManageDriveService.class) ;
      ManageViewService vservice = uiDrive.getApplicationComponent(ManageViewService.class) ;
      DriveData drive = (DriveData) dservice.getDriveByName(driveName) ;
      List<String> userRoles = Utils.getMemberships() ;
      Map<String, String> viewMap = new HashMap<String, String>() ;
      String viewList = "";
      for(String role : userRoles ){
        String[] views = drive.getViews().split(",") ;
        for(String viewName : views) {
          viewName = viewName.trim() ;
          Node viewNode = vservice.getViewByName(viewName) ;
          String permiss = viewNode.getProperty("exo:permissions").getString();
          String[] viewPermissions = permiss.split(",") ;
          if(drive.hasPermission(viewPermissions, role)) viewMap.put(viewName, viewName) ;
        }
      }
      for(String viewName : viewMap.values().toArray(new String[]{})) {
        if(viewList.length() > 0) viewList = viewList + "," + viewName ;
        else viewList = viewName ;
      }
      drive.setViews(viewList) ;
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext() ;
      PortletPreferences preferences = context.getRequest().getPreferences() ;
      preferences.setValue(Utils.WORKSPACE_NAME, drive.getWorkspace()) ;
      preferences.setValue(Utils.JCR_PATH, drive.getHomePath()) ;
      preferences.setValue(Utils.VIEWS, drive.getViews()) ;
      preferences.setValue(Utils.DRIVE, drive.getName()) ;
      preferences.setValue(Utils.DRIVE_FOLDER, drive.getAllowCreateFolder()) ;
      preferences.setValue(Utils.REPOSITORY, uiDrive.repoName_) ;
      preferences.store() ;
      UIJCRExplorerPortlet uiParent = uiDrive.getParent() ;
      UIJCRExplorer uiJCRExplorer = uiParent.getChild(UIJCRExplorer.class) ;

      Preference pref = uiJCRExplorer.getPreference() ;
      pref.setShowSideBar(drive.getViewSideBar()) ;
      pref.setShowNonDocumentType(drive.getViewNonDocument()) ;
      pref.setShowPreferenceDocuments(drive.getViewPreferences()) ;
      pref.setEmpty(false) ;

      ManageableRepository repository = rservice.getRepository() ;
      Session session = repository.login(drive.getWorkspace())  ;
      uiJCRExplorer.setSession(session) ;      
      Node node = (Node) session.getItem(drive.getHomePath()) ;
      uiJCRExplorer.getAllClipBoard().clear() ;
      uiJCRExplorer.setRootNode(node) ;
      uiJCRExplorer.refreshExplorer() ;
      List<SelectItemOption<String>> viewOptions = new ArrayList<SelectItemOption<String>>() ;
      String[] arrView = viewList.split(",") ;
      for(String view : arrView) {
        viewOptions.add(new SelectItemOption<String>(view, view)) ;
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

