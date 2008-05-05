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
public class JcrInputProperty {
  
  public static final int PROPERTY = 0;
  public static final int NODE = 1;
  public static final int SINGLE_VALUE = 0;
  public static final int MULTI_VALUE = 1;
  public static final int BYTE_VALUE = 2;
  
  private String jcrPath;
  private int type = PROPERTY;
  private String primaryNodeType;
  private String mixinNodeType;
  private Object value;
  private int valueType = 0;
  
  public String getJcrPath() { return jcrPath; }
  public void setJcrPath(String jcrPath) { this.jcrPath = jcrPath; }
  
  public String getPrimaryNodeType() { return primaryNodeType; }
  public void setPrimaryNodeType(String nodetype) { this.primaryNodeType = nodetype;}
   
  public String[] getMixinNodeType() { return mixinNodeType.split(","); }
  public void setMixinNodeType(String mixintype) { this.mixinNodeType = mixintype; }
  
  public int getType() { return type; }
  public void setType(int type) { this.type = type; }
    
  public void setValue(Object value) { this.value = value; }
  public Object getValue() { return value; }
  
  public void setValueType(int type){ valueType = type ; }
  public int  getValueType(){ return valueType ; }
}
