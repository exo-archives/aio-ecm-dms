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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.ecm.jcr.JCRExceptionManager;
import org.exoplatform.ecm.jcr.model.Preference;
import org.exoplatform.ecm.utils.SessionsUtils;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.control.UIActionBar;
import org.exoplatform.ecm.webui.component.explorer.control.UIControl;
import org.exoplatform.ecm.webui.component.explorer.control.UIViewBar;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
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
  
  public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ;  
  }

  public String getRepository(){return repoName_ ;}
  public void setRepository(String repoName){repoName_ = repoName ;}

  @SuppressWarnings("unchecked")
  public List<DriveData> getDrives(String repoName) throws Exception {    
    ManageDriveService driveService = getApplicationComponent(ManageDriveService.class) ;      
    List<DriveData> driveList = new ArrayList<DriveData>() ;    
    List<String> userRoles = Utils.getMemberships() ;    
    List<DriveData> allDrives = driveService.getAllDrives(repoName);
    Set<DriveData> temp = new HashSet<DriveData>();
    //We will improve ManageDrive service to allow getAllDriveByUser
    for(DriveData driveData:allDrives) {
      String[] allPermission = driveData.getAllPermissions();
      boolean flag = false;
      for(String permission:allPermission) {
        if(permission.equalsIgnoreCase("${userId}")) {
          temp.add(driveData);
          flag = true;
          break;
        }
        if(flag) continue;
        for(String rolse:userRoles) {
          if(driveData.hasPermission(allPermission,rolse)) {
            temp.add(driveData);
            break;
          }
        }
      }      
    }        
    for(Iterator<DriveData> iterator = temp.iterator();iterator.hasNext();) {
      driveList.add(iterator.next());
    }
    Collections.sort(driveList) ;
    return driveList ; 
  }
  
  public List<DriveData> generalDrives(List<DriveData> driveList) throws Exception {
    List<DriveData> generalDrives = new ArrayList<DriveData>() ;
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class) ;
    String userPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_USERS_PATH) ;
    String groupPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_GROUPS_PATH) ;
    for(DriveData drive : driveList) {
      if((!drive.getHomePath().startsWith(userPath) && !drive.getHomePath().startsWith(groupPath)) 
          || drive.getHomePath().equals(userPath)) {
        generalDrives.add(drive) ;
      }
    }
    return generalDrives ;
  }
  
  public List<DriveData> groupDrives(List<DriveData> driveList) throws Exception {
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class) ;
    List<DriveData> groupDrives = new ArrayList<DriveData>() ;
    String groupPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_GROUPS_PATH) ;
    List<String> groups = Utils.getGroups() ;
    for(DriveData drive : driveList) {
      if(drive.getHomePath().startsWith(groupPath)) {
        for(String group : groups) {
          if(drive.getHomePath().equals(groupPath + group)) {
            groupDrives.add(drive) ;
            break ;
          }
        }
      } 
    }
    Collections.sort(groupDrives) ;
    return groupDrives ;
  }
  
  public List<DriveData> personalDrives(List<DriveData> driveList) {
    List<DriveData> personalDrives = new ArrayList<DriveData>() ;
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class) ;
    String userPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_USERS_PATH) ;
    for(DriveData drive : driveList) {
      if(drive.getHomePath().startsWith(userPath + "/${userId}/")) {
        personalDrives.add(drive) ;
      }
    }
    Collections.sort(personalDrives) ;
    return personalDrives ;
  }
  
  static  public class SelectRepoActionListener extends EventListener<UIDrivesBrowser> {
    public void execute(Event<UIDrivesBrowser> event) throws Exception {
      String repoName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIDrivesBrowser uiDrivesBrowser = event.getSource() ;
      uiDrivesBrowser.setRepository(repoName) ;  
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDrivesBrowser) ;
    }
  }

  static  public class SelectDriveActionListener extends EventListener<UIDrivesBrowser> {
    public void execute(Event<UIDrivesBrowser> event) throws Exception {
      UIDrivesBrowser uiDrive = event.getSource() ;
      String driveName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      RepositoryService rservice = uiDrive.getApplicationComponent(RepositoryService.class) ;
      ManageDriveService dservice = uiDrive.getApplicationComponent(ManageDriveService.class) ;
      DriveData drive = dservice.getDriveByName(driveName, uiDrive.repoName_) ;
      String userId = Util.getPortalRequestContext().getRemoteUser() ;
      UIApplication uiApp = uiDrive.getAncestorOfType(UIApplication.class) ;
      List<String> userRoles = Utils.getMemberships() ;
      Map<String, String> viewMap = new HashMap<String, String>() ;
      String viewList = "";
      for(String role : userRoles){
        String[] views = drive.getViews().split(",") ;
        for(String viewName : views) {
          viewName = viewName.trim() ;
          Node viewNode = 
            uiDrive.getApplicationComponent(ManageViewService.class).getViewByName(viewName, uiDrive.repoName_,SessionsUtils.getSystemProvider()) ;
          String permiss = viewNode.getProperty("exo:permissions").getString();
          if(permiss.contains("${userId}")) permiss = permiss.replace("${userId}", userId) ;
          String[] viewPermissions = permiss.split(",") ;
          if(permiss.equals("*")) viewMap.put(viewName, viewName) ;
          if(drive.hasPermission(viewPermissions, role)) viewMap.put(viewName, viewName) ;
        }
      }
      if(viewMap.isEmpty()) {
        Object[] args = { driveName } ;
        uiApp.addMessage(new ApplicationMessage("UIDrivesBrowser.msg.no-view-found", args)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      for(String viewName : viewMap.values().toArray(new String[]{})) {
        if(viewList.length() > 0) viewList = viewList + "," + viewName ;
        else viewList = viewName ;
      }
      drive.setViews(viewList) ;
      String homePath = drive.getHomePath() ;
      if(homePath.contains("${userId}")) homePath = homePath.replace("${userId}", userId) ;
      UIJCRExplorerPortlet uiParent = uiDrive.getParent() ;      
      UIJCRExplorer uiJCRExplorer = uiParent.getChild(UIJCRExplorer.class) ;

      Preference pref = new Preference();
      pref.setShowSideBar(drive.getViewSideBar()) ;
      pref.setShowNonDocumentType(drive.getViewNonDocument()) ;
      pref.setShowPreferenceDocuments(drive.getViewPreferences()) ;
      pref.setAllowCreateFoder(drive.getAllowCreateFolder()); 
      pref.setShowHiddenNode(drive.getShowHiddenNode()) ;
      uiJCRExplorer.setPreferences(pref);
      uiJCRExplorer.setDriveData(drive) ;
      uiJCRExplorer.setIsReferenceNode(false) ;
      
      SessionProvider provider = SessionsUtils.getSessionProvider() ;                  
      ManageableRepository repository = rservice.getRepository(uiDrive.repoName_) ;
      Session session = provider.getSession(drive.getWorkspace(),repository) ;      
      Node node = null ;
      try {
        node = (Node) session.getItem(homePath) ;        
      } catch(AccessDeniedException ace) {
        Object[] args = { driveName } ;
        uiApp.addMessage(new ApplicationMessage("UIDrivesBrowser.msg.access-denied", args, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;        
      } catch(Exception e) {
        e.printStackTrace() ;
        JCRExceptionManager.process(uiApp, e) ;
        return ;
      } 
      uiJCRExplorer.getAllClipBoard().clear() ;
      uiJCRExplorer.setRepositoryName(uiDrive.repoName_) ;
      uiJCRExplorer.setWorkspaceName(drive.getWorkspace()) ;
      uiJCRExplorer.setRootPath(homePath) ;
      uiJCRExplorer.setSelectNode(node) ;
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