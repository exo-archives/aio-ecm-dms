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
package org.exoplatform.services.cms.impl;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.component.ComponentPlugin;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Apr 27, 2009  
 * 9:14:45 AM
 */
public class DMSConfiguration implements Startable {
  
  private Map<String, DMSRepositoryConfiguration> dmsConfigMap_ = 
    new HashMap<String, DMSRepositoryConfiguration>();
  
  public DMSRepositoryConfiguration getConfig(String repository) {
    return dmsConfigMap_.get(repository);
  }
  
  public void addPlugin(ComponentPlugin plugin) {
    if(plugin instanceof DMSRepositoryConfiguration) {
      dmsConfigMap_.put(((DMSRepositoryConfiguration)plugin).getRepositoryName(), 
          (DMSRepositoryConfiguration)plugin);
    }
  }
  
  public void initNewRepo(String repository, DMSRepositoryConfiguration plugin) {
    dmsConfigMap_.put(repository, plugin);
  }

  public void start() {
    // TODO Auto-generated method stub
    
  }

  public void stop() {
    // TODO Auto-generated method stub
    
  }
}
