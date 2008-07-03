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
package org.exoplatform.ecm.component;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * May 8, 2008 4:25:10 PM
 */
public abstract class UITreeList extends UIContainer {
  
  private List<Node> nodes_ ;
  private String[] arrType_ ;
  private String[] arrMimeType_ ;
  
  public UITreeList() throws Exception {
  }
  
  public void setNodeList(List<Node> nodes) { nodes_ = nodes ;  }
  public List<Node> getNodeList() {return nodes_; } ; 
  
  public void setFilterType(String[] arrType) { arrType_ = arrType ; }
  public String[] getFilterType() { return arrType_ ;};
  
  public void setMimeTypes(String[] arrMimeType) { arrMimeType_ = arrMimeType ; }
  public String[] getMimeTypes() { return arrMimeType_ ; }
}
