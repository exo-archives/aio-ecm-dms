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
package org.exoplatform.services.ecm.core;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Apr 28, 2008  
 */
public class JcrItemInput {

  public static final int PROPERTY = 0;
  public static final int NODE = 1;    

  private String itemPath;
  private int itemType = PROPERTY;
  private String primaryNodeType;
  private String mixinNodeType;
  private Object propertyValue;  

  public String getItemPath() { return itemPath; }
  public void setItemPath(String path) { this.itemPath = path; }

  public String getPrimaryNodeType() { return primaryNodeType; }
  public void setPrimaryNodeType(String nodetype) { this.primaryNodeType = nodetype;}

  public String[] getMixinNodeTypes() { return mixinNodeType.split(","); }
  public void setMixinNodeType(String mixintype) { this.mixinNodeType = mixintype; }

  public int getItemType() { return itemType; }
  public void setItemType(int type) { this.itemType = type; }

  public void setPropertyValue(Object value) { this.propertyValue = value; }
  public Object getPropertyValue() { return propertyValue; }
  
}
