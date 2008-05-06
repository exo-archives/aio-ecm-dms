
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
package org.exoplatform.services.cms;

import java.util.Map;

import javax.jcr.Node;

/**
 * @author benjaminmestrallet
 */
public interface CmsService {
  
  public static final String NODE = "/node";  
  
  public String storeNode(String workspace, String nodetypeName, String storePath, Map inputProperties,String repository) throws Exception;
  
  public String storeNode(String nodetypeName, Node storeNode, Map inputProperties, boolean isAddNew,String repository) throws Exception;
  
  public String storeNodeByUUID(String nodetypeName, Node storeNode, Map inputProperties, boolean isAddNew,String repository) throws Exception;
  
  public void moveNode(String nodePath, String srcWorkspace, String destWorkspace, String destPath, String repository);  
    
}
