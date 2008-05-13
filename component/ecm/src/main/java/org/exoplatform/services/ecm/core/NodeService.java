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

import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Apr 25, 2008  
 */
public interface NodeService {

  public static final String NODE = "/node" ;

  public Node addNode(String repository, String workspace,String parentPath,String nodetype,Map<String,JcrItemInput> jcrItemInputs,boolean isNew,SessionProvider sessionProvider) throws Exception ;

  public Node addNode(Node parent,String nodetype,Map<String,JcrItemInput> jcrItemInputs,boolean isNew) throws Exception;

  public Node moveNode(String repository, String srcWorkspace,String srcPath, String destWorkspace, String destPath, SessionProvider sessionProvider) throws Exception ;

  public Node copyNode(String repository, String srcWorkspace,String srcPath, String destWorkspace, String destPath, SessionProvider sessionProvider) throws Exception ;
  
  public void setProperty(Node node, String propertyName, Object value, int requiredtype, boolean isMultiple) throws Exception ;
}
