/***************************************************************************
 * Copyright 2001-2009 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.migration;

import java.util.List;

import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jan 8, 2009 2:47:00 PM
 */
/**
 * This service will be used to migrate drives name which contains the invalid characters
 */
public class DriveMigrationService implements Startable {

  final static private String DRIVE_HOME_PATH_ALIAS = "exoDrivesPath";
  
  private Log log = ExoLogger.getLogger("DRIVE MIGRATION") ;
  private ManageDriveService driveService_ ;
  private RepositoryService repositoryService_;
  private String baseDrivePath_ ;
  
  public DriveMigrationService(RepositoryService repositoryService, ManageDriveService driveService,
      NodeHierarchyCreator nodeHierarchyCreatorService) { 
    repositoryService_ = repositoryService ;
    driveService_ = driveService ;
    baseDrivePath_ = nodeHierarchyCreatorService.getJcrPath(DRIVE_HOME_PATH_ALIAS);
  }
  
  public void start() {
    // TODO Auto-generated method stub
    log.info("PROCESSING MIGRATE DRIVES NAME");
    try {
      migrateProcess();
    } catch(Exception e) {
      log.error("MIGRATION FAILED: Can not migrate drives");
    }
  }
/**
 * This method will be used to migrate drives name which included
 * invalid characters like(|)
 * @throws Exception
 */  
  @SuppressWarnings("unchecked")
  public void migrateProcess() throws Exception {
    int migrateNum = 0;
    for(RepositoryEntry repoEntry : repositoryService_.getConfig().getRepositoryConfigurations()) {
      List<DriveData> drives = driveService_.getAllDrives(repoEntry.getName());
      Session session = getSession(repoEntry.getName()) ;    
      for(DriveData drive : drives) {
        String oldDriveName = drive.getName();
        String oldDrivePath = baseDrivePath_ + "/" + oldDriveName;
        if(oldDriveName.indexOf("|") > -1) {
          String newDrivePath = baseDrivePath_ + "/" + oldDriveName.replace("|", ".");
          session.move(oldDrivePath, newDrivePath);
          session.save();
          migrateNum++;
        }
      }
      session.logout();
    }
    if(migrateNum > 0) {
      log.info("MIGRATED " + migrateNum + " DRIVES");
    } else {
      log.info("DON'T HAVE ANY DRIVE TO MIGRATE");
    }
  }
  
  /**
   * Get session from repository in SystemWorkspace name
   * @param repository    repository name
   * @return session
   * @throws Exception
   */
  private Session getSession(String repository) throws Exception{    
    ManageableRepository manaRepository = repositoryService_.getRepository(repository) ;
    return manaRepository.getSystemSession(manaRepository.getConfiguration().getSystemWorkspaceName()) ;          
  }  

  public void stop() { }
}