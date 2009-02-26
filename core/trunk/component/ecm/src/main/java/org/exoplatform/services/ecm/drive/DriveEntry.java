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
package org.exoplatform.services.ecm.drive;

import java.util.ArrayList;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Apr 28, 2008  
 */
public class DriveEntry {

  private String name ;  
  private String group ;  
  private ArrayList<String> accessPermissions ;
  private String repository ;  
  private String workspace ;
  private String homePath ;
  private String iconPath ;
  private ArrayList<String> views ;

  //TODO should move to userSetting
  private boolean showReference ;
  private boolean showNonDocument ;
  private boolean showSideBar ;
  private boolean showHiddenNode ;
  private String allowCreateFolder ;

  public  DriveEntry(){}

  public String getName() { return name ; }
  public void setName(String name) { this.name = name ; }

  public String getGroup() { return group ; }
  public void setGroup(String group) { this.group = group; }

  public String getRepository() { return repository ; }
  public void setRepository(String rp) { repository = rp ; }

  public String getWorkspace() { return workspace ; }
  public void setWorkspace(String ws) { workspace = ws ; }

  public ArrayList<String> getAccessPermissions() { return this.accessPermissions ; }
  public void setAcessPermissions(ArrayList<String> accessPermissions) { this.accessPermissions = accessPermissions ; }

  public String getHomePath() { return homePath ; }
  public void setHomePath(String path) { homePath = path ; }

  public String getIconPath() { return iconPath ; }
  public void setIconPath(String ico) { iconPath = ico ; }

  public String getAllowCreateFolder() { return allowCreateFolder ; }
  public void setAllowCreateFolder(String allowCreateFolder) { this.allowCreateFolder = allowCreateFolder ; }

  public ArrayList<String> getViews() { return views ; }
  public void setViews(ArrayList<String> v) { views = v ; }

  public boolean getShowPreferences() { return showReference ; }
  public void setShowPreferences(boolean b) { showReference = b ; }

  public boolean getShowNonDocument() { return showNonDocument ; }
  public void setShowNonDocument(boolean b) { showNonDocument = b ; }

  public boolean getShowSideBar() { return showSideBar ; }
  public void setShowSideBar(boolean b) { showSideBar = b ; }

  public boolean getShowHiddenNode() { return showHiddenNode ; }
  public void setShowHiddenNode(boolean b) { showHiddenNode = b ; }

}
