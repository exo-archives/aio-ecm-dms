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
import java.util.HashMap;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.AccessDeniedException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.apache.commons.logging.Log;
import org.exoplatform.ecm.jcr.model.Preference;
import org.exoplatform.ecm.utils.text.Text;
import org.exoplatform.ecm.webui.component.explorer.control.UIActionBar;
import org.exoplatform.ecm.webui.component.explorer.control.UIAddressBar;
import org.exoplatform.ecm.webui.component.explorer.control.UIControl;
import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;

@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class
)
public class UIJCRExplorerPortlet extends UIPortletApplication {
  
  /**
   * Logger.
   */
  private static final Log LOG  = ExoLogger.getLogger("explorer.UIJCRExplorerPortlet");
  
  final static public String REPOSITORY         = "repository";

  final static public String CATEGORY_MANDATORY = "categoryMandatoryWhenFileUpload";

  final static public String MAX_SIZE_UPLOAD    = "uploadFileSizeLimitMB";

  final static public String ISDIRECTLY_DRIVE   = "isDirectlyDrive";

  final static public String DRIVE_NAME         = "driveName";

  final static public String USECASE            = "usecase";

  final static public String JAILED             = "jailed";

  final static public String SOCIAL             = "social";

  final static public String SELECTION          = "selection";

  final static public String PERSONAL           = "personal";

  final static public String PARAMETERIZE       = "parameterize";

  final static public String PARAMETERIZE_PATH       = "nodePath";
  
  
  private boolean flagSelect = false;
  
  public UIJCRExplorerPortlet() throws Exception {
    UIJcrExplorerContainer explorerContainer = addChild(UIJcrExplorerContainer.class, null, null);
    explorerContainer.setFlag(false);
    explorerContainer.init();
    addChild(UIJcrExplorerEditContainer.class, null, null).setRendered(false);
  }
  
  public boolean isFlagSelect() {
    return flagSelect;
  }

  public void setFlagSelect(boolean flagSelect) {
    this.flagSelect = flagSelect;
  }
  
  private HashMap<String, String> getElementByContext(WebuiRequestContext context) {
    HashMap<String, String> mapParam = new HashMap<String, String>();
    //In case access by ajax request
    if (context.useAjax()) return mapParam;
    Pattern patternUrl = Pattern.compile("([^/]+)/([^/]+)/(.*)");
    PortalRequestContext pcontext = Util.getPortalRequestContext();
    PortletRequestContext portletReqContext = (PortletRequestContext) context;
    String requestUrl = pcontext.getRequestURI();
    String portalUrl = pcontext.getPortalURI();
    String portletId = portletReqContext.getWindowId();
    String accessViewUrl = requestUrl.replace(portalUrl,"");
    if (!accessViewUrl.contains(portletId)) return mapParam;
    String[] uri = accessViewUrl.split(portletId + "/");
    if (uri == null || uri.length < 2) return mapParam;
    Matcher matcher = patternUrl.matcher(uri[1]);
    if (matcher.find()) {
      mapParam.put("repository", matcher.group(1));
      mapParam.put("drive", matcher.group(2));
      mapParam.put("path", "/" + matcher.group(3));
    } else {
      patternUrl = Pattern.compile("([^/]+)/(.*)");
      matcher = patternUrl.matcher(uri[1]);
      if (matcher.find()) {
        mapParam.put("repository", matcher.group(1));
        mapParam.put("drive", matcher.group(2));
        mapParam.put("path", "/");
      }
    }
    return mapParam;
  }
  
  private void showDocument(WebuiRequestContext context, HashMap<String, String> map) throws Exception {
    String repositoryName = String.valueOf(map.get("repository"));
    String driveName = String.valueOf(map.get("drive"));
    String path = String.valueOf(map.get("path"));
    if(path.equals("/")) return;
    ArrayList<String> encodeNameArr = new ArrayList<String>();
    for(String name : path.split("/")) {
      if(name.length() > 0) {
        encodeNameArr.add(Text.escapeIllegalJcrChars(name));
      }
    }
    StringBuilder encodedPath = new StringBuilder();
    for(String encodedName : encodeNameArr) {
      encodedPath.append("/").append(encodedName);
    }
    path = encodedPath.toString();
    UIApplication uiApp = findFirstComponentOfType(UIApplication.class);
    ManageDriveService manageDrive = getApplicationComponent(ManageDriveService.class);
    DriveData driveData = null;
    try {
      driveData = manageDrive.getDriveByName(driveName, repositoryName);
      if (driveData == null) throw new PathNotFoundException();
    } catch (PathNotFoundException e) {
      Object[] args = { driveName };
      uiApp.addMessage(new ApplicationMessage("UIDrivesBrowser.msg.drive-not-exist", args, ApplicationMessage.WARNING));
      context.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    }
    RepositoryService rservice = getApplicationComponent(RepositoryService.class);
    String userId = Util.getPortalRequestContext().getRemoteUser();
    List<String> viewList = new ArrayList<String>();
    
    for (String role : Utils.getMemberships()) {
      for (String viewName : driveData.getViews().split(",")) {
        if (!viewList.contains(viewName.trim())) {
          Node viewNode = 
            getApplicationComponent(ManageViewService.class).getViewByName(viewName.trim(),
                repositoryName, SessionProviderFactory.createSystemProvider());
          String permiss = viewNode.getProperty("exo:accessPermissions").getString();
          if (permiss.contains("${userId}")) permiss = permiss.replace("${userId}", userId);
          String[] viewPermissions = permiss.split(",");
          if (permiss.equals("*")) viewList.add(viewName.trim());
          if (driveData.hasPermission(viewPermissions, role)) viewList.add(viewName.trim());
        }
      }
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
    driveData.setViews(viewListStr);
    String homePath = driveData.getHomePath();
    if (homePath.contains("${userId}")) homePath = homePath.replace("${userId}", userId);
    
    setFlagSelect(true);
    Preference pref = new Preference();
    pref.setShowSideBar(driveData.getViewSideBar());
    pref.setShowNonDocumentType(driveData.getViewNonDocument());
    pref.setShowPreferenceDocuments(driveData.getViewPreferences());
    pref.setAllowCreateFoder(driveData.getAllowCreateFolder()); 
    pref.setShowHiddenNode(driveData.getShowHiddenNode());
    
    UIJCRExplorer uiExplorer = findFirstComponentOfType(UIJCRExplorer.class);
    uiExplorer.setPreferences(pref);
    uiExplorer.setDriveData(driveData);
    uiExplorer.setIsReferenceNode(false);
    
    SessionProvider provider = SessionProviderFactory.createSessionProvider();                  
    ManageableRepository repository = rservice.getRepository(repositoryName);
    try {
      Session session = provider.getSession(driveData.getWorkspace(),repository);      
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
    uiExplorer.setRepositoryName(repositoryName);
    uiExplorer.setWorkspaceName(driveData.getWorkspace());
    uiExplorer.setRootPath(homePath);
    path = homePath.concat(path).replaceAll("/+", "/"); 
    UIControl uiControl = uiExplorer.getChild(UIControl.class);
    UIActionBar uiActionbar = uiControl.getChild(UIActionBar.class);
    uiActionbar.setTabOptions(viewList.get(0));
    UIAddressBar uiAddressBar = uiControl.getChild(UIAddressBar.class);
    uiAddressBar.setViewList(viewList);
    uiAddressBar.setSelectedViewName(viewList.get(0));
    uiExplorer.setSelectNode(driveData.getWorkspace(), path);
    UIWorkingArea uiWorkingArea = findFirstComponentOfType(UIWorkingArea.class);
    UIDocumentWorkspace uiDocWorkspace = uiWorkingArea.getChild(UIDocumentWorkspace.class);
    uiDocWorkspace.setRenderedChild(UIDocumentContainer.class) ;
    UIPopupContainer popupAction = getChild(UIPopupContainer.class);
    if (popupAction != null && popupAction.isRendered()) {
      popupAction.deActivate();
      context.addUIComponentToUpdateByAjax(popupAction);
    }
    uiExplorer.refreshExplorer();
    uiExplorer.setRenderSibbling(UIJCRExplorer.class);
  }
  
  public void  processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    UIJcrExplorerContainer explorerContainer = getChild(UIJcrExplorerContainer.class);
    UIJcrExplorerEditContainer editContainer = getChild(UIJcrExplorerEditContainer.class);
    context.getJavascriptManager().importJavascript("eXo.ecm.ECMUtils","/ecm/javascript/");
    context.getJavascriptManager().addJavascript("eXo.ecm.ECMUtils.init('UIJCRExplorerPortlet') ;");
    PortletRequestContext portletReqContext = (PortletRequestContext) context ;
    HashMap<String, String> map = getElementByContext(context);
    if (portletReqContext.getApplicationMode() == PortletMode.VIEW) {
      if (map.size() > 0) {
        showDocument(context, map);
      } else {
        PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
        PortletPreferences portletPref = pcontext.getRequest().getPreferences();
        String usecase =  portletPref.getValue("usecase", "").trim();
        if (usecase.equals(SELECTION)) {
          initwhenSelect(explorerContainer);
        } else {
          initwhenDirect(explorerContainer, editContainer, portletPref);
        }
      }
      explorerContainer.setRendered(true);
      getChild(UIJcrExplorerEditContainer.class).setRendered(false);
    } else if(portletReqContext.getApplicationMode() == PortletMode.HELP) {
      if (LOG.isDebugEnabled()) LOG.debug("\n\n>>>>>>>>>>>>>>>>>>> IN HELP  MODE \n");      
    } else if(portletReqContext.getApplicationMode() == PortletMode.EDIT) {
      explorerContainer.setRendered(false);
      getChild(UIJcrExplorerEditContainer.class).setRendered(true);
    }
    super.processRender(app, context);
  }
  
  public void initwhenSelect(UIJcrExplorerContainer explorerContainer) throws Exception {
    explorerContainer.setFlag(true);
    if (!isFlagSelect()) {
      UIDrivesBrowserContainer browserContainer = explorerContainer.getChild(UIDrivesBrowserContainer.class); 
      if (browserContainer != null) {
        browserContainer.setRendered(true);
      } else explorerContainer.addChild(UIDrivesBrowserContainer.class, null, null).setRendered(true);
      explorerContainer.getChild(UIJCRExplorer.class).setRendered(false);   
    } else {
      UIDrivesBrowserContainer driveBrowserContainer = explorerContainer.getChild(UIDrivesBrowserContainer.class);
      if (driveBrowserContainer == null) {
        explorerContainer.setFlag(false);
        explorerContainer.getChild(UIDrivesBrowserContainer.class).setRendered(false);
        explorerContainer.getChild(UIJCRExplorer.class).setRendered(true);    
      }
    }
  }
  
  public void initwhenDirect(UIJcrExplorerContainer explorerContainer, UIJcrExplorerEditContainer editContainer, PortletPreferences portletPref) throws Exception {
    explorerContainer.setFlag(false);
    String driveName = portletPref.getValue("driveName", "").trim();
    String repository = portletPref.getValue("repository", "").trim();
    List<String> listDriveName = explorerContainer.getDrives();
    for (String driveDataName : listDriveName) {
      if (driveDataName.trim().equals(driveName)) {
        explorerContainer.setFlag(true);
        break;
      }
    }
    UIJCRExplorer uiJCRExplorer = explorerContainer.getChild(UIJCRExplorer.class);
    if (editContainer.getChild(UIJcrExplorerEditForm.class).isFlagSelectRender()) {
      explorerContainer.initExplorer(driveName, portletPref);
      editContainer.getChild(UIJcrExplorerEditForm.class).setFlagSelectRender(false);
      ManageDriveService manageDriveService = getApplicationComponent(ManageDriveService.class);
      DriveData driveData = manageDriveService.getDriveByName(driveName, repository);
      String nodePath = portletPref.getValue("nodePath", "").trim();
      String homePath = (driveData.getHomePath() + nodePath).replaceAll("/+", "/");
      uiJCRExplorer.setSelectNode(driveData.getWorkspace(), homePath);
      uiJCRExplorer.refreshExplorer(); 
    }
    uiJCRExplorer.setRendered(true);
    explorerContainer.getChild(UIDrivesBrowserContainer.class).setRendered(false);
    
    
  }
  
  public String getPreferenceRepository() {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
    String repository = portletPref.getValue(Utils.REPOSITORY, "") ;
    return repository ;
  }
}
