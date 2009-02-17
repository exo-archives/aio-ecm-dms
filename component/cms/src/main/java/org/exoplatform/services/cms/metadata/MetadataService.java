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
package org.exoplatform.services.cms.metadata;

import java.util.List;

import javax.jcr.nodetype.NodeType;


/**
 * Author : Hung Nguyen Quang
 *          nguyenkequanghung@yahoo.com
 */

public interface MetadataService {  
	public List<String> getMetadataList(String repository) throws Exception;
  public List<NodeType> getAllMetadatasNodeType(String repository) throws Exception ;
  public String addMetadata(String nodetype, boolean isDialog, String role, String content, boolean isAddNew, String repository) throws Exception;
  public void removeMetadata(String nodetype, String repository) throws Exception;
  public List<String> getExternalMetadataType(String repository) throws Exception ;
  public String getMetadataTemplate(String name, boolean isDialog, String repository) throws Exception;  
  public String getMetadataPath(String name, boolean isDialog, String repository) throws Exception;
  public String getMetadataRoles(String name, boolean isDialog, String repository) throws Exception;
  public boolean hasMetadata(String name, String repository) throws Exception;
  public void init(String repository) throws Exception ;
}
