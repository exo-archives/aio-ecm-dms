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
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

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
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
/**
 * Created by The eXo Platform SARL
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *          xxx5669@gmail.com
 * 4 févr. 09  
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/webui/component/explorer/UIJCRExporerPortlet.gtmpl"
)
public class UIJcrExplorerContainer extends UIContainer {
  private boolean flag = false;
  
  public UIJcrExplorerContainer() throws Exception {
  }
  
  public String getUserAgent() {
    PortletRequestContext requestContext = PortletRequestContext.getCurrentInstance();
    PortletRequest portletRequest = requestContext.getRequest();
    return portletRequest.getProperty("User-Agent");
  }
  
  public boolean isFlag() {
    return flag;
  }
  
  public void setFlag(boolean flag) {
    this.flag = flag;
  }

  public void init() throws Exception {
    PortletPreferences portletPref = getPreference();
    String isDirectlyDrive =  portletPref.getValue("isDirectlyDrive", "").trim();
    if (isDirectlyDrive.equals("true")) {
      if (getChild(UIJCRExplorer.class) == null) addChild(UIJCRExplorer.class, null, null);
      initExplorerPreference(portletPref);
      if (getChild(UIDrivesBrowserContainer.class) == null) addChild(UIDrivesBrowserContainer.class, null, null).setRendered(false);
      String driveName = portletPref.getValue("driveName", "").trim();
      List<DriveData> listDriver = getDrives(portletPref);
      for (DriveData driveData : listDriver) {
        if (driveData.getName().trim().equals(driveName)) {
          flag = true;
          break;
        }
      }
      if (flag) {
        initExplorer(driveName, portletPref);
      }
    } else {
      flag = true;
      if (getChild(UIDrivesBrowserContainer.class) == null) addChild(UIDrivesBrowserContainer.class, null, null);
      if (getChild(UIJCRExplorer.class) == null) addChild(UIJCRExplorer.class, null, null).setRendered(false);
      initExplorerPreference(portletPref);
    }
  }
  
  private void initExplorerPreference(PortletPreferences portletPref) {
  	UIJCRExplorer uiExplorer = getChild(UIJCRExplorer.class);
  	if (uiExplorer != null) {
  		Preference pref = uiExplorer.getPreference();
  		if (pref == null) {
  			pref = new Preference();
  		}
  		pref.setNodesPerPage(Integer.parseInt(portletPref.getValue(
  				Preference.NODES_PER_PAGE, "10")));
  		uiExplorer.setPreferences(pref);
  	}
  }  
  
  private PortletPreferences getPreference() {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
    return pcontext.getRequest().getPreferences();
  }
  
  public List<String> getDrives() throws Exception {
    List<DriveData> driveList = new ArrayList<DriveData>();
    driveList = getDrives(getPreference());
    List<DriveData> listDriveAll = new ArrayList<DriveData>();
    List<String> listDriveNameAll = new ArrayList<String>();
    List<DriveData> generalDrives = generalDrives(driveList);
    List<DriveData> groupDrives = groupDrives(driveList);
    List<DriveData> personalDrives = personalDrives(driveList);
    listDriveAll.addAll(generalDrives);
    listDriveAll.addAll(groupDrives);
    listDriveAll.addAll(personalDrives);
    for (DriveData driveData : listDriveAll) {
      listDriveNameAll.add(driveData.getName());
    }
    return listDriveNameAll;
  }
  
  @SuppressWarnings("unchecked")
  public List<DriveData> getDrives(PortletPreferences portletPref) throws Exception {
    ManageDriveService driveService = getApplicationComponent(ManageDriveService.class);      
    List<DriveData> driveList = new ArrayList<DriveData>();    
    List<String> userRoles = Utils.getMemberships();    
    List<DriveData> allDrives = driveService.getAllDrives(portletPref.getValue(Utils.REPOSITORY, ""));
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
          if(flag) continue;
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
    
    for (Iterator<DriveData> iterator = temp.iterator(); iterator.hasNext();) {
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
  
  public void initExplorer(String driveName, PortletPreferences portletPref) throws Exception {
    try {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      RepositoryService rservice = getApplicationComponent(RepositoryService.class);
      ManageDriveService dservice = getApplicationComponent(ManageDriveService.class);
      DriveData drive = dservice.getDriveByName(driveName, portletPref.getValue(Utils.REPOSITORY, ""));
      String userId = Util.getPortalRequestContext().getRemoteUser();
      UIApplication uiApp = getApplicationComponent(UIApplication.class);
      List<String> viewList = new ArrayList<String>();
      for (String role : Utils.getMemberships()) {
        for (String viewName : drive.getViews().split(",")) {
          if (!viewList.contains(viewName.trim())) {
            Node viewNode = 
              getApplicationComponent(ManageViewService.class).getViewByName(viewName.trim(),
                  portletPref.getValue(Utils.REPOSITORY, ""), SessionProviderFactory.createSystemProvider());
            String permiss = viewNode.getProperty("exo:accessPermissions").getString();
            if (permiss.contains("${userId}")) permiss = permiss.replace("${userId}", userId);
            String[] viewPermissions = permiss.split(",");
            if (permiss.equals("*")) viewList.add(viewName.trim());
            if (drive.hasPermission(viewPermissions, role)) viewList.add(viewName.trim());
          }
        }
      }
      if (viewList.isEmpty()) {
  //      Object[] args = { driveName };
  //      uiApp.addMessage(new ApplicationMessage("UIDrivesBrowser.msg.no-view-found", args));
  //      context.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      String viewListStr = "";
      List<SelectItemOption<String>> viewOptions = new ArrayList<SelectItemOption<String>>();
      ResourceBundle res = context.getApplicationResourceBundle();
      String viewLabel = null;
      for (String viewName : viewList) {
        try {
          viewLabel = res.getString("Views.label." + viewName) ; 
        } catch (MissingResourceException e) {
          viewLabel = viewName;
        }        
        viewOptions.add(new SelectItemOption<String>(viewLabel, viewName));
        if(viewListStr.length() > 0) viewListStr = viewListStr + "," + viewName;
        else viewListStr = viewName;
      }
      drive.setViews(viewListStr);
      String homePath = drive.getHomePath();
      if (homePath.contains("${userId}")) homePath = homePath.replace("${userId}", userId);
      UIJCRExplorer uiJCRExplorer = getChild(UIJCRExplorer.class);
  
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
      ManageableRepository repository = rservice.getRepository(portletPref.getValue(Utils.REPOSITORY, ""));
      try {
        Session session = provider.getSession(drive.getWorkspace(),repository);      
        // check if it exists
        // we assume that the path is a real path
        session.getItem(homePath);        
      } catch(AccessDeniedException ace) {
        Object[] args = { driveName };
        uiApp.addMessage(new ApplicationMessage("UIDrivesBrowser.msg.access-denied", args, 
            ApplicationMessage.WARNING));
        context.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;        
      } catch(NoSuchWorkspaceException nosuchWS) {
        Object[] args = { driveName };
        uiApp.addMessage(new ApplicationMessage("UIDrivesBrowser.msg.workspace-not-exist", args, 
            ApplicationMessage.WARNING));
        context.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;        
      } catch(Exception e) {
        JCRExceptionManager.process(uiApp, e);
        return;
      } 
      uiJCRExplorer.getAllClipBoard().clear();
      uiJCRExplorer.setRepositoryName(portletPref.getValue(Utils.REPOSITORY, ""));
      uiJCRExplorer.setWorkspaceName(drive.getWorkspace());
      uiJCRExplorer.setRootPath(homePath);
      uiJCRExplorer.setSelectNode(drive.getWorkspace(), homePath);
      uiJCRExplorer.refreshExplorer();      
      UIControl uiControl = uiJCRExplorer.getChild(UIControl.class);
      UIActionBar uiActionbar = uiControl.getChild(UIActionBar.class);
      UIAddressBar uiAddressBar = uiControl.getChild(UIAddressBar.class);
      uiAddressBar.setViewList(viewList);
      uiAddressBar.setSelectedViewName(viewList.get(0));
      uiActionbar.setTabOptions(viewList.get(0));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
} 
