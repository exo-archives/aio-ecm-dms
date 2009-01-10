/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.migration;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.services.cms.BasePath;
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
 * Jan 10, 2009
 * 10:31:27 AM 
 */
/**
 * This service allow migrate all drives and views node by add property exo:accessPermissions
 * Mixin nodetype exo:accessPermission will be added to node when found drive or view
 * had property exo:permission
 */
public class PropertiesMigrationService implements Startable {
  
  private static String ACCESS_PERMISSIONS = "exo:accessPermissions".intern() ;
  private static String MIX_ACCESS_PERMISSION = "exo:accessPermission".intern() ;
  private static String PERMISSIONS = "exo:permissions".intern() ;
  private Log log = ExoLogger.getLogger("PROPERTIES MIGRATION") ;
  
  private RepositoryService repositoryService_ ;
  private String baseDrivePath_ ;
  private NodeHierarchyCreator nodeHierarchyCreator_ ;
  private String baseViewPath_ ;

  public PropertiesMigrationService(RepositoryService jcrService, 
      NodeHierarchyCreator nodeHierarchyCreator) throws Exception {
    repositoryService_ = jcrService ;
    nodeHierarchyCreator_ = nodeHierarchyCreator ;
    baseDrivePath_ = nodeHierarchyCreator_.getJcrPath(BasePath.EXO_DRIVES_PATH);
    baseViewPath_ = nodeHierarchyCreator_.getJcrPath(BasePath.CMS_VIEWS_PATH) ;
  }

  public void start() {
    try {
      log.info("PROCESSING MIGRATE PROPERTIES FOR DRIVES");
      migrateDriveProperties();
    } catch(Exception e) {
      e.printStackTrace();
      log.error("MIGRATION FAILED: Can not migrate properties for drives");
    }    
    try {
      log.info("PROCESSING MIGRATE PROPERTIES FOR VIEWS");
      migrateViewProperties();
    } catch(Exception e) {
      e.printStackTrace();
      log.error("MIGRATION FAILED: Can not migrate properties for views");
    }
  }
  
 /**
 * This method allow migrate all drives node which included exo:permission property 
 * @throws Exception
 */  
  private void migrateDriveProperties() throws Exception {
    for(RepositoryEntry repoEntry : repositoryService_.getConfig().getRepositoryConfigurations()) {
      Session session = getSession(repoEntry.getName()) ;    
      Node driveHome = (Node)session.getItem(baseDrivePath_);
      migrateProperties(driveHome.getNodes());
      session.save();
      session.logout();
    }
  }
  /**
   * This method allow migrate all views node which included exo:permission property
   * @throws Exception
   */
  private void migrateViewProperties() throws Exception {
    for(RepositoryEntry repoEntry : repositoryService_.getConfig().getRepositoryConfigurations()) {
      Session session = getSession(repoEntry.getName()) ;
      Node viewHome = (Node)session.getItem(baseViewPath_) ;
      migrateProperties(viewHome.getNodes());
      session.save();
      session.logout();
    }
  }
  
  /**
   * Process migration
   * @param nodeIter Node iterator
   * @throws Exception
   */
  private void migrateProperties(NodeIterator nodeIter) throws Exception {
    int migrateNum = 0;
    while(nodeIter.hasNext()) {
      Node node = nodeIter.nextNode();
      if(!node.hasProperty(ACCESS_PERMISSIONS) && node.hasProperty(PERMISSIONS)) {
        if(node.canAddMixin(MIX_ACCESS_PERMISSION)) {
          node.addMixin(MIX_ACCESS_PERMISSION);
          node.save();
          node.getProperty(ACCESS_PERMISSIONS).setValue(node.getProperty(PERMISSIONS).getString());
        }
      }
      node.save();
      migrateNum++;
    }
    if(migrateNum > 0) {
      log.info("MIGRATED " + migrateNum + " NODES");
    } else {
      log.info("DON'T HAVE ANY NODES TO MIGRATE");
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

  public void stop() {}

}
