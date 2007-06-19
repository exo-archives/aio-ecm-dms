/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.drives;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Quang Hung
 *          nguyenkequanghung@yahoo.com
 * Feb 27, 2006
 */
public class ManageDriveServiceImpl implements ManageDriveService, Startable {

  private static String WORKSPACE = "exo:workspace".intern() ;
  private static String PERMISSIONS = "exo:permissions".intern() ;
  private static String VIEWS = "exo:views".intern() ;
  private static String ICON = "exo:icon".intern() ;
  private static String PATH = "exo:path".intern() ;
  private static String VIEW_REFERENCES = "exo:viewPreferences".intern() ;
  private static String VIEW_NON_DOCUMENT = "exo:viewNonDocument".intern() ;
  private static String VIEW_SIDEBAR = "exo:viewSideBar".intern() ;
  private static String ALLOW_CREATE_FOLDER = "exo:allowCreateFolder".intern() ;

  private List<ManageDrivePlugin> drivePlugins_  = new ArrayList<ManageDrivePlugin> ();
  private RepositoryService repositoryService_ ;
  private CmsConfigurationService cmsConfigurationService_ ;

  public ManageDriveServiceImpl(RepositoryService jcrService, 
      CmsConfigurationService cmsConfigurationService ) throws Exception{
    repositoryService_ = jcrService ;
    cmsConfigurationService_ = cmsConfigurationService ;
  }

  public void start() {
    try{
      for(ManageDrivePlugin plugin : drivePlugins_) {
        plugin.init() ;
      }
    }catch(Exception e) {      
    }    
  }

  public void stop() { }

  public void init(String repository) throws Exception {
    for(ManageDrivePlugin plugin : drivePlugins_) {
      plugin.init(repository) ;
    }
  }

  public void setManageDrivePlugin(ManageDrivePlugin drivePlugin) {
    drivePlugins_.add(drivePlugin) ;
  }  

  public Node getDriveHome(String repository) throws Exception {    
    String drivesPath = cmsConfigurationService_.getJcrPath(BasePath.EXO_DRIVES_PATH);
    Session session = getSession(repository) ;
    session.refresh(false) ;
    return (Node) session.getItem(drivesPath);
  }

  public List<DriveData> getAllDrives(String repository) throws Exception {
    Node driveHome = getDriveHome(repository) ;
    NodeIterator itr = driveHome.getNodes() ;
    List<DriveData> driveList = new ArrayList<DriveData>() ;
    while(itr.hasNext()) {
      DriveData data = new DriveData() ;
      Node drive = itr.nextNode() ;
      data.setName(drive.getName()) ;
      data.setWorkspace(drive.getProperty(WORKSPACE).getString()) ;
      data.setHomePath(drive.getProperty(PATH).getString()) ;
      data.setPermissions(drive.getProperty(PERMISSIONS).getString()) ;
      data.setViews(drive.getProperty(VIEWS).getString()) ;
      data.setIcon(drive.getProperty(ICON).getString()) ;
      data.setViewPreferences(Boolean.parseBoolean(drive.getProperty(VIEW_REFERENCES).getString())) ;
      data.setViewNonDocument(Boolean.parseBoolean(drive.getProperty(VIEW_NON_DOCUMENT).getString())) ;
      data.setViewSideBar(Boolean.parseBoolean(drive.getProperty(VIEW_SIDEBAR).getString())) ;
      data.setAllowCreateFolder(drive.getProperty(ALLOW_CREATE_FOLDER).getString()) ;
      driveList.add(data) ;
    }
    return driveList ;    
  }

  public DriveData getDriveByName(String name, String repository) throws Exception{  
    Node driveHome = getDriveHome(repository) ;
    if (driveHome.hasNode(name)){
      Node drive = driveHome.getNode(name) ;
      DriveData data = new DriveData() ;
      data.setName(drive.getName()) ;
      data.setWorkspace(drive.getProperty(WORKSPACE).getString()) ;
      data.setHomePath(drive.getProperty(PATH).getString()) ;
      data.setPermissions(drive.getProperty(PERMISSIONS).getString()) ;
      data.setViews(drive.getProperty(VIEWS).getString()) ;
      data.setIcon(drive.getProperty(ICON).getString()) ;
      data.setViewPreferences(Boolean.parseBoolean(drive.getProperty(VIEW_REFERENCES).getString())) ;
      data.setViewNonDocument(Boolean.parseBoolean(drive.getProperty(VIEW_NON_DOCUMENT).getString())) ;
      data.setViewSideBar(Boolean.parseBoolean(drive.getProperty(VIEW_SIDEBAR).getString())) ;
      data.setAllowCreateFolder(drive.getProperty(ALLOW_CREATE_FOLDER).getString()) ;
      return data ;
    }
    return  null ;    
  }

  public void addDrive(String name, String workspace, String permissions, String homePath, 
      String views, String icon, boolean viewReferences, boolean viewNonDocument, 
      boolean viewSideBar, String repository, String allowCreateFolder) throws Exception {
    String drivesPath = cmsConfigurationService_.getJcrPath(BasePath.EXO_DRIVES_PATH);
    Session session = repositoryService_.getRepository(repository)
    .getSystemSession(cmsConfigurationService_.getWorkspace(repository)) ;
    Node driveHome = (Node)session.getItem(drivesPath) ;
    if (!driveHome.hasNode(name)){
      Node driveNode = driveHome.addNode(name, "exo:drive");
      driveNode.setProperty(WORKSPACE, workspace) ;
      driveNode.setProperty(PERMISSIONS, permissions) ;
      driveNode.setProperty(PATH, homePath) ;      
      driveNode.setProperty(VIEWS, views) ;
      driveNode.setProperty(ICON, icon) ;
      driveNode.setProperty(VIEW_REFERENCES, Boolean.toString(viewReferences)) ;
      driveNode.setProperty(VIEW_NON_DOCUMENT, Boolean.toString(viewNonDocument)) ;
      driveNode.setProperty(VIEW_SIDEBAR, Boolean.toString(viewSideBar)) ;
      driveNode.setProperty(ALLOW_CREATE_FOLDER, allowCreateFolder) ;
      driveHome.save() ;
    }else{
      Node driveNode = driveHome.getNode(name);
      driveNode.setProperty(WORKSPACE, workspace) ;
      driveNode.setProperty(PERMISSIONS, permissions) ;
      driveNode.setProperty(PATH, homePath) ;      
      driveNode.setProperty(VIEWS, views) ;
      driveNode.setProperty(ICON, icon) ;
      driveNode.setProperty(VIEW_REFERENCES, Boolean.toString(viewReferences)) ;
      driveNode.setProperty(VIEW_NON_DOCUMENT, Boolean.toString(viewNonDocument)) ;
      driveNode.setProperty(VIEW_SIDEBAR, Boolean.toString(viewSideBar)) ;
      driveNode.setProperty(ALLOW_CREATE_FOLDER, allowCreateFolder) ;
      driveNode.save() ;
    }
    session.save() ;
  }

  public List<DriveData> getAllDriveByPermission(String permission, String repository) throws Exception {
    List<DriveData> driveByPermission = new ArrayList<DriveData>() ;
    try{
      List<DriveData> driveList = getAllDrives(repository);    
      for(int i = 0; i < driveList.size(); i ++) {
        DriveData drive = driveList.get(i) ;
        if(drive.hasPermission(drive.getAllPermissions(), permission)){
          driveByPermission.add(drive) ;
        } 
      }
    }catch(Exception e) {
      e.printStackTrace() ;
    }    
    return driveByPermission ;
  }

  public void removeDrive(String driveName, String repository) throws Exception {
    Node driveHome = getDriveHome(repository) ;
    if(driveHome.hasNode(driveName)){
      driveHome.getNode(driveName).remove() ;
      driveHome.save() ;
    }
  } 

  private Session getSession(String repository) {
    try {
      String workspace = cmsConfigurationService_.getWorkspace(repository) ;
      return repositoryService_.getRepository(repository).getSystemSession(workspace) ;      
    }catch(Exception e) {      
    }
    return null ;
  }

  public boolean isUsedView(String viewName, String repository) throws Exception {
    Node driveHome = getDriveHome(repository) ;
    NodeIterator iter = driveHome.getNodes() ;
    while(iter.hasNext()) {
      Node drive = iter.nextNode() ;
      String[] views = drive.getProperty("exo:views").getString().split(",") ;
      for(String view : views) {
        if(viewName.equals(view)) return true ;
      }
    }
    return false;
  }
}
