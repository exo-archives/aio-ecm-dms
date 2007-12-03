/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.drives.impl;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 23, 2007 3:09:21 PM
 */
public class NewUserListener extends UserEventListener {
  
  private ManageDriveService driveService_ ;
  private RepositoryService jcrService_;
  private InitParams initParams_ ;
  private String userPath_ ;
  final static String PRIVATE = "Private" ;
  final static String PUBLIC = "Public" ;

  public NewUserListener(RepositoryService jcrService,
      ManageDriveService driveService, 
      NodeHierarchyCreator nodeHierarchyCreatorService, 
      InitParams params) throws Exception {
    jcrService_ = jcrService ;
    driveService_ = driveService ;
    initParams_ = params ;
    userPath_ = nodeHierarchyCreatorService.getJcrPath(BasePath.CMS_USERS_PATH) ; 
  }
  
  @SuppressWarnings({"unused", "hiding"})
  public void preSave(User user, boolean isNew) throws Exception { 
    String repository = initParams_.getValueParam("repository").getValue();
    String workspace = initParams_.getValueParam("workspace").getValue();
    String permissions = initParams_.getValueParam("permissions").getValue();
    String homePath = userPath_ + "/" + user.getUserName() ;
    String views = initParams_.getValueParam("views").getValue();
    String icon = initParams_.getValueParam("icon").getValue();
    boolean viewPreferences = Boolean.parseBoolean(initParams_.getValueParam("viewPreferences").getValue());
    boolean viewNonDocument = Boolean.parseBoolean(initParams_.getValueParam("viewNonDocument").getValue());
    boolean viewSideBar = Boolean.parseBoolean(initParams_.getValueParam("viewSideBar").getValue());
    boolean showHiddenNode = Boolean.parseBoolean(initParams_.getValueParam("showHiddenNode").getValue());
    String allowCreateFolder = initParams_.getValueParam("allowCreateFolder").getValue();
    driveService_.addDrive(user.getUserName() + "|" + PRIVATE, workspace, permissions, homePath + "/" + PRIVATE, views, icon, 
        viewPreferences, viewNonDocument, viewSideBar, showHiddenNode, repository, allowCreateFolder) ;
    driveService_.addDrive(user.getUserName() + "|" + PUBLIC, workspace, permissions, homePath + "/" + PUBLIC, views, icon, 
        viewPreferences, viewNonDocument, viewSideBar, showHiddenNode, repository, allowCreateFolder) ;
  }
  
  public void preDelete(User user) throws Exception {
    ManageableRepository repository = jcrService_.getCurrentRepository() ;
    driveService_.removeDrive(user.getUserName() + "|" + PRIVATE, repository.getConfiguration().getName()) ;
    driveService_.removeDrive(user.getUserName() + "|" + PUBLIC, repository.getConfiguration().getName()) ;
  }
}