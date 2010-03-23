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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.exoplatform.ecm.jcr.model.Preference;
import org.exoplatform.ecm.webui.component.explorer.control.UIActionBar;
import org.exoplatform.ecm.webui.component.explorer.control.UIAddressBar;
import org.exoplatform.ecm.webui.component.explorer.control.UIControl;
import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
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
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * July 3, 2006
 * 10:07:15 AM
 */

@ComponentConfig (
      template =  "app:/groovy/webui/component/explorer/UIDrivesBrowser.gtmpl",
      events = {
        @EventConfig(listeners = UIDrivesBrowser.SelectRepoActionListener.class),
        @EventConfig(listeners = UIDrivesBrowser.SelectDriveActionListener.class)
      }
)


public class UIDrivesBrowser extends UIContainer {
  final public static String FIELD_SELECTREPO = "selectRepo" ; 
  private String repoName_;
  private RepositoryService rService;
  public UIDrivesBrowser() throws Exception {
    rService = getApplicationComponent(RepositoryService.class);
    repoName_ = rService.getDefaultRepository().getConfiguration().getName();
  }
  
  public List<String> getRepositoryList() {
    List<String> repositories = new ArrayList<String>();    
    for(RepositoryEntry re : rService.getConfig().getRepositoryConfigurations()) {
      repositories.add(re.getName());
    }
    return repositories;
  }
  
  public String getPortalName() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    PortalContainerInfo containerInfo = (PortalContainerInfo) container
        .getComponentInstanceOfType(PortalContainerInfo.class);
    return containerInfo.getContainerName(); 
  }
  
  public String getLabel(String id)  {
    RequestContext context = RequestContext.getCurrentInstance();
    ResourceBundle res = context.getApplicationResourceBundle();
    try {
      return res.getString("Drives.label." + id.replace(" ", ""));
    } catch (MissingResourceException ex) {
      return id;
    }    
  }
  
  public String getGroupLabel(String groupId) {
    RequestContext context = RequestContext.getCurrentInstance();
    ResourceBundle res = context.getApplicationResourceBundle();
    try {
      return res.getString("Drives.label." + groupId.replace(".", ""));
    } catch (MissingResourceException ex) {
      return groupId.replace(".", " / ");
    }
  }  

  public String getRepository() {return repoName_;}
  
  public void setRepository(String repoName) {repoName_ = repoName; }  
  
  @SuppressWarnings("unchecked")
  public List<DriveData> getDrives(String repoName) throws Exception {    
    ManageDriveService driveService = getApplicationComponent(ManageDriveService.class);      
    List<DriveData> driveList = new ArrayList<DriveData>();    
    List<String> userRoles = Utils.getMemberships();    
    List<DriveData> allDrives = driveService.getAllDrives(repoName);
    Set<DriveData> temp = new HashSet<DriveData>();
    String userId = Util.getPortalRequestContext().getRemoteUser();
    if (userId != null) {
      // We will improve ManageDrive service to allow getAllDriveByUser
      for (DriveData driveData : allDrives) {
        String[] allPermission = driveData.getAllPermissions();
        boolean flag = false;
        for (String permission : allPermission) {
          if (permission.equalsIgnoreCase("${userId}")) {
            temp.add(driveData);
            flag = true;
            break;
          }
          if (permission.equalsIgnoreCase("*")) {
            temp.add(driveData);
            flag = true;
            break;
          }
          if (flag)
            continue;
          for (String rolse : userRoles) {
            if (driveData.hasPermission(allPermission, rolse)) {
              temp.add(driveData);
              break;
            }
          }
        }
      }
    } else {
      for (DriveData driveData : allDrives) {
        String[] allPermission = driveData.getAllPermissions();
        for (String permission : allPermission) {
          if (permission.equalsIgnoreCase("*")) {
            temp.add(driveData);
            break;
          }
        }
      }
    }
    
    for(Iterator<DriveData> iterator = temp.iterator();iterator.hasNext();) {
      driveList.add(iterator.next());
    }
    Collections.sort(driveList);
    return driveList; 
  }
  
  public List<DriveData> generalDrives(List<DriveData> driveList) throws Exception {
    List<DriveData> generalDrives = new ArrayList<DriveData>();
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class);
    String userPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_USERS_PATH);
    String groupPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_GROUPS_PATH);
    for(DriveData drive : driveList) {
      if((!drive.getHomePath().startsWith(userPath) && !drive.getHomePath().startsWith(groupPath)) 
          || drive.getHomePath().equals(userPath)) {
        generalDrives.add(drive);
      }
    }
    return generalDrives;
  }
  
  public List<DriveData> groupDrives(List<DriveData> driveList) throws Exception {
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class);
    List<DriveData> groupDrives = new ArrayList<DriveData>();
    String groupPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_GROUPS_PATH);
    List<String> groups = Utils.getGroups();
    for(DriveData drive : driveList) {
      if(drive.getHomePath().startsWith(groupPath)) {
        for(String group : groups) {
          if(drive.getHomePath().equals(groupPath + group)) {
            groupDrives.add(drive);
            break;
          }
        }
        for(String permission : drive.getAllPermissions()) {
          if (permission.indexOf(":/") > 0) {
            String[] arrPer = permission.split(":/");
            if(groups.contains("/" + arrPer[1]) && !groupDrives.contains(drive)) {
              groupDrives.add(drive);
              break;
            }
          }
        }
      } 
    }
    Collections.sort(groupDrives);
    return groupDrives;
  }
  
  public List<DriveData> personalDrives(List<DriveData> driveList) {
    List<DriveData> personalDrives = new ArrayList<DriveData>();
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class);
    String userPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_USERS_PATH);
    for(DriveData drive : driveList) {
      if(drive.getHomePath().startsWith(userPath + "/${userId}/")) {
        personalDrives.add(drive);
      }
    }
    Collections.sort(personalDrives);
    return personalDrives;
  }
  
  static  public class SelectRepoActionListener extends EventListener<UIDrivesBrowser> {
    public void execute(Event<UIDrivesBrowser> event) throws Exception {
      String repoName = event.getRequestContext().getRequestParameter(OBJECTID);
      UIDrivesBrowser uiDrivesBrowser = event.getSource();
      uiDrivesBrowser.setRepository(repoName);  
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDrivesBrowser);
    }
  }
  
  static  public class SelectDriveActionListener extends EventListener<UIDrivesBrowser> {
    public void execute(Event<UIDrivesBrowser> event) throws Exception {
      UIDrivesBrowser uiDrive = event.getSource();
      String driveName = event.getRequestContext().getRequestParameter(OBJECTID);
      RepositoryService rservice = uiDrive.getApplicationComponent(RepositoryService.class);
      ManageDriveService dservice = uiDrive.getApplicationComponent(ManageDriveService.class);
      DriveData drive = dservice.getDriveByName(driveName, uiDrive.repoName_);
      String userId = Util.getPortalRequestContext().getRemoteUser();
      UIApplication uiApp = uiDrive.getAncestorOfType(UIApplication.class);
      List<String> viewList = new ArrayList<String>();
      for(String role : Utils.getMemberships()){
        for(String viewName : drive.getViews().split(",")) {
          if(!viewList.contains(viewName.trim())) {
            Node viewNode = 
              uiDrive.getApplicationComponent(ManageViewService.class).getViewByName(viewName.trim(),
                  uiDrive.repoName_,SessionProviderFactory.createSystemProvider());
            String permiss = viewNode.getProperty("exo:accessPermissions").getString();
            if(permiss.contains("${userId}")) permiss = permiss.replace("${userId}", userId);
            String[] viewPermissions = permiss.split(",");
            if(permiss.equals("*")) viewList.add(viewName.trim());
            if(drive.hasPermission(viewPermissions, role)) viewList.add(viewName.trim());
          }
        }
      }
      if(viewList.isEmpty()) {
        Object[] args = { driveName };
        uiApp.addMessage(new ApplicationMessage("UIDrivesBrowser.msg.no-view-found", args));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      String viewListStr = "";
      for(String viewName : viewList) {
        if(viewListStr.length() > 0) viewListStr = viewListStr + "," + viewName;
        else viewListStr = viewName;
      }
      drive.setViews(viewListStr);
      String homePath = drive.getHomePath();
      if(homePath.contains("${userId}")) homePath = homePath.replace("${userId}", userId);
      UIJCRExplorerPortlet uiParent = uiDrive.getAncestorOfType(UIJCRExplorerPortlet.class);  
      uiParent.setFlagSelect(true);
      UIJcrExplorerContainer explorerContainer = uiParent.getChild(UIJcrExplorerContainer.class);
      UIJCRExplorer uiJCRExplorer = explorerContainer.getChild(UIJCRExplorer.class);

//      Preference pref = new Preference();
      Preference pref = uiJCRExplorer.getPreference();
      pref.setShowSideBar(drive.getViewSideBar());
      pref.setShowNonDocumentType(drive.getViewNonDocument());
      pref.setShowPreferenceDocuments(drive.getViewPreferences());
      pref.setAllowCreateFoder(drive.getAllowCreateFolder()); 
      pref.setShowHiddenNode(drive.getShowHiddenNode());
//      uiJCRExplorer.setPreferences(pref);
      uiJCRExplorer.setDriveData(drive);
      uiJCRExplorer.setIsReferenceNode(false);
      
      SessionProvider provider = SessionProviderFactory.createSessionProvider();                  
      ManageableRepository repository = rservice.getRepository(uiDrive.repoName_);
      try {
        Session session = provider.getSession(drive.getWorkspace(),repository);
        // check if it exists
        // we assume that the path is a real path
        session.getItem(homePath);        
      } catch(AccessDeniedException ace) {
        Object[] args = { driveName };
        uiApp.addMessage(new ApplicationMessage("UIDrivesBrowser.msg.access-denied", args, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;        
      } catch(NoSuchWorkspaceException nosuchWS) {
        Object[] args = { driveName };
        uiApp.addMessage(new ApplicationMessage("UIDrivesBrowser.msg.workspace-not-exist", args, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;        
      } catch(Exception e) {
        JCRExceptionManager.process(uiApp, e);
        return;
      } 
      uiJCRExplorer.clearNodeHistory(homePath);
      uiJCRExplorer.setRepositoryName(uiDrive.repoName_);
      uiJCRExplorer.setWorkspaceName(drive.getWorkspace());
      uiJCRExplorer.setRootPath(homePath);
      uiJCRExplorer.setSelectNode(drive.getWorkspace(), homePath);
      uiJCRExplorer.refreshExplorer();      
      String selectedView = viewList.get(0);
      UIControl uiControl = uiJCRExplorer.getChild(UIControl.class);
      UIActionBar uiActionbar = uiControl.getChild(UIActionBar.class);
      uiActionbar.setTabOptions(selectedView);
      UIAddressBar uiAddressBar = uiControl.getChild(UIAddressBar.class);
      uiAddressBar.setViewList(viewList);
      uiAddressBar.setSelectedViewName(selectedView);
      explorerContainer.setRenderedChild(UIJCRExplorer.class);
      uiParent.setRenderedChild(UIJcrExplorerContainer.class);
    }
  }
}