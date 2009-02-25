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

  /**
   * Create a new node 
   * 
   * @param repository The repository specifying to create
   * @param workspace The workspace specifying to create
   * @param parentPath The path contain this node
   * @param nodetype Type of node 
   * @param jcrItemInputs all properties will be hole on it
   * @param isNew between pick crate new or edition for node
   * @param sessionProvider provide a session
   * @return return the node just created
   * @throws Exception if any error occurs
   */
  public Node addNode(String repository, String workspace,String parentPath,String nodetype,Map<String,JcrItemInput> jcrItemInputs,boolean isNew,SessionProvider sessionProvider) throws Exception ;
  /**
   * Ceate a node on a parent node
   * 
   * @param parent parent node 
   * @param nodetype type of node
   * @param jcrItemInputs all properties will be hole on it
   * @param isNew between pick create new or edition for node
   * @return return the node just created
   * @throws Exception if any error occurs
   */
  public Node addNode(Node parent,String nodetype,Map<String,JcrItemInput> jcrItemInputs,boolean isNew) throws Exception;
  /**
   * move a node from specify a path
   * 
   * @param repository repository specifying
   * @param srcWorkspace source workspace
   * @param srcPath source path, contain source node
   * @param destWorkspace destination workspace
   * @param destPath destination path, contain node was move
   * @param sessionProvider provide a session
   * @return node was move
   * @throws Exception if any error occurs
   */
  public Node moveNode(String repository, String srcWorkspace,String srcPath, String destWorkspace, String destPath, SessionProvider sessionProvider) throws Exception ;
  /**
   * copy a node form specify path
   * 
   * @param repository repository specifying
   * @param srcWorkspace source workspace
   * @param srcPath source path, contain source node
   * @param destWorkspace destination workspace
   * @param destPath destination path, contain destination node
   * @param sessionProvider provide a session
   * @return node was copy
   * @throws Exception if any error occurs
   */
  public Node copyNode(String repository, String srcWorkspace,String srcPath, String destWorkspace, String destPath, SessionProvider sessionProvider) throws Exception ;
  /**
   * set properties for node
   * 
   * @param node the node, it will be set properties
   * @param propertyName name for property
   * @param value value on each items property
   * @param requiredtype require types for properties of the node
   * @param isMultiple type specify is multiable or no
   * @throws Exception if any error occurs
   */
  public void setProperty(Node node, String propertyName, Object value, int requiredtype, boolean isMultiple) throws Exception ;
}
