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
    permissions = permissions.concat(","+ user.getUserName());
    String homePath = userPath_ + "/" + user.getUserName() ;
    String views = initParams_.getValueParam("views").getValue();
    String icon = initParams_.getValueParam("icon").getValue();
    boolean viewPreferences = Boolean.parseBoolean(initParams_.getValueParam("viewPreferences").getValue());
    boolean viewNonDocument = Boolean.parseBoolean(initParams_.getValueParam("viewNonDocument").getValue());
    boolean viewSideBar = Boolean.parseBoolean(initParams_.getValueParam("viewSideBar").getValue());
    boolean showHiddenNode = Boolean.parseBoolean(initParams_.getValueParam("showHiddenNode").getValue());
    String allowCreateFolder = initParams_.getValueParam("allowCreateFolder").getValue();
    //Only user can access private drive
    driveService_.addDrive(user.getUserName() + "|" + PRIVATE, workspace, user.getUserName(), homePath + "/" + PRIVATE, views, icon, 
        viewPreferences, viewNonDocument, viewSideBar, showHiddenNode, repository, allowCreateFolder) ;
    //User and everyone can see public drive for user
    driveService_.addDrive(user.getUserName() + "|" + PUBLIC, workspace, permissions, homePath + "/" + PUBLIC, views, icon, 
        viewPreferences, viewNonDocument, viewSideBar, showHiddenNode, repository, allowCreateFolder) ;
  }
  
  public void preDelete(User user) throws Exception {
    ManageableRepository repository = jcrService_.getCurrentRepository() ;
    driveService_.removeDrive(user.getUserName() + "|" + PRIVATE, repository.getConfiguration().getName()) ;
    driveService_.removeDrive(user.getUserName() + "|" + PUBLIC, repository.getConfiguration().getName()) ;
  }
}