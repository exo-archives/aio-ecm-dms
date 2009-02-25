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
package org.exoplatform.services.ecm.view.impl;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Apr 28, 2008  
 */
public class TemplateConfig {
  
  private String name ;
  private String group ;
  private String type ;
  private String templatePath ;    

  public TemplateConfig(){} 
  
  public String getName() { return this.name ; }
  public void setName(String name) { this.name = name ; }
  
  public String getTemplateGroup() { return this.group ; }
  public void setTemplateGroup(String group) { this.group = group ; }
  
  public String getConfPath() { return this.templatePath; }
  public void setConfPath(String confPath) { this.templatePath = confPath ; }
  
  public String getTemplateType() {return this.type ; }
  public void setTemplateType(String type) { this.type = type ; }
  
  
}


