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
import org.exoplatform.services.jcr.core.ManageableRepository;
/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Quang Hung
 *          nguyenkequanghung@yahoo.com
 * Feb 27, 2006
 */
public class ManageDriveServiceImpl implements ManageDriveService {

  private static String WORKSPACE = "exo:workspace".intern() ;
  private static String PERMISSIONS = "exo:permissions".intern() ;
  private static String VIEWS = "exo:views".intern() ;
  private static String ICON = "exo:icon".intern() ;
  private static String PATH = "exo:path".intern() ;
  private static String VIEW_REFERENCES = "exo:viewPreferences".intern() ;
  private static String VIEW_NON_DOCUMENT = "exo:viewNonDocument".intern() ;
  private static String VIEW_SIDEBAR = "exo:viewSideBar".intern() ;

  private ManageDrivePlugin drivePlugin_ ;
  private RepositoryService jcrService_ ;
  private CmsConfigurationService cmsConfigurationService_ ;
  private Session session_ ;
  
  public ManageDriveServiceImpl(RepositoryService jcrService, 
                                CmsConfigurationService cmsConfigurationService ) throws Exception{
    jcrService_ = jcrService ;
    cmsConfigurationService_ = cmsConfigurationService ;
    ManageableRepository jcrRepository = jcrService_.getRepository();
    session_ = jcrRepository.getSystemSession(cmsConfigurationService_.getWorkspace());
  }

  public void setManageDrivePlugin(ManageDrivePlugin drivePlugin) {
    drivePlugin_ = drivePlugin ;
  }  

  public Node getDriveHome() throws Exception {    
    String drivesPath = cmsConfigurationService_.getJcrPath(BasePath.EXO_DRIVES_PATH);
    session_.refresh(false) ;
    return (Node) session_.getItem(drivesPath);
  }

  public List<DriveData> getAllDrives() throws Exception {
    Node driveHome = getDriveHome() ;
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
      driveList.add(data) ;
    }
    return driveList ;    
  }

  public DriveData getDriveByName(String name) throws Exception{  
    Node driveHome = getDriveHome() ;
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
      return data ;
    }
    return  null ;    
  }

  public void addDrive(String name, String workspace, String permissions, String homePath, 
                        String views, String icon, boolean viewReferences, boolean viewNonDocument,
                        boolean viewSideBar ) throws Exception{
    drivePlugin_.addDrive(name, workspace, permissions, homePath, views, icon, viewReferences, 
                          viewNonDocument, viewSideBar) ;
  }

  public List<DriveData> getAllDriveByPermission(String permission) throws Exception {
    List<DriveData> driveByPermission = new ArrayList<DriveData>() ;
    try{
      List<DriveData> driveList = getAllDrives();    
      for(int i = 0; i < driveList.size(); i ++) {
        DriveData drive = driveList.get(i) ;
        if(drive.hasPermission(permission))
          driveByPermission.add(drive) ;
      }
    }catch(Exception e) {
      e.printStackTrace() ;
    }    
    return driveByPermission ;
  }

  public void removeDrive(String driveName) throws Exception {
    Node driveHome = getDriveHome() ;
    if(driveHome.hasNode(driveName)){
      driveHome.getNode(driveName).remove() ;
      driveHome.save() ;
    }
  }  
}
