/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.drives.impl;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 23, 2007 3:09:45 PM
 */
public class NewGroupListener extends GroupEventListener {

  private ManageDriveService driveService_ ;
  private InitParams initParams_ ;
  private String groupsPath_ ;

  final static private String GROUPS_PATH = "groupsPath";
  
  public NewGroupListener(ManageDriveService driveService, 
      NodeHierarchyCreator nodeHierarchyCreatorService, 
      InitParams params) throws Exception {
    driveService_ = driveService ;
    initParams_ = params ;
    groupsPath_ = nodeHierarchyCreatorService.getJcrPath(GROUPS_PATH) ; 
  }
  
  @SuppressWarnings({"unused", "hiding"})
  public void preSave(Group group, boolean isNew) throws Exception { 
    String  groupId = null ;
    String parentId = group.getParentId() ;
    if(parentId == null || parentId.length() == 0) groupId = "/" + group.getGroupName() ;
    else groupId = parentId + "/" + group.getGroupName() ;
    String name = initParams_.getValueParam("name").getValue() + " " + group.getGroupName();
    String repository = initParams_.getValueParam("repository").getValue();
    String workspace = initParams_.getValueParam("workspace").getValue();
    String permissions = initParams_.getValueParam("permissions").getValue();
    String homePath = groupsPath_ + groupId ;
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
