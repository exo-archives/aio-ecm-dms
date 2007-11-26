/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.drives.impl;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.drives.ManageDriveService;
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
  private InitParams initParams_ ;
  private String userPath_ ;

  public NewUserListener(ManageDriveService driveService, 
      NodeHierarchyCreator nodeHierarchyCreatorService, 
      InitParams params) throws Exception {
    driveService_ = driveService ;
    initParams_ = params ;
    userPath_ = nodeHierarchyCreatorService.getJcrPath(BasePath.CMS_USERS_PATH) ; 
  }
  
  @SuppressWarnings({"unused", "hiding"})
  public void preSave(User user, boolean isNew) throws Exception { 
    String name = initParams_.getValueParam("name").getValue() + " " + user.getUserName() ;
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
    driveService_.addDrive(name, workspace, permissions, homePath, views, icon, viewPreferences, viewNonDocument, viewSideBar, showHiddenNode, repository, allowCreateFolder) ;
  }
}