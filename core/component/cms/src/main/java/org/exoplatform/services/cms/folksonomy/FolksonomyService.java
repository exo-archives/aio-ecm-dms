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
package org.exoplatform.services.cms.folksonomy;

import java.util.List;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Xuan Hoa Pham
 *          hoapham@exoplatform.com
 * 					phamvuxuanhoa@gmail.com
 * Dec 5, 2006  
 */
public interface FolksonomyService {
  
  /**
   * Add new child node in node baseTagsPath_
   * If there are not node with name in tagNames then create new one
   * Add mixin type MIX_REFERENCEABLE_MIXIN for new node
   * Add new property TAG_CREATED_DATE_PROP, TAG_STATUS_PROP
   * Add new mixin type EXO_FOLKSONOMIZED_MIXIN to current node if not exist
   * Set property EXO_FOLKSONOMY_PROP for current node
   * @param node        current node
   * @param tagNames    Array of node name as child node of exoTagsHomeNode_
   * @param repository
   * @throws Exception
   */
  public void addTag(Node node, String[] tagName, String repository) throws Exception ;
  
  /**
   * Get all node base on path = baseTagsPath_ in repository
   * @param repository      repository name
   * @return ArrayList of Node
   */
  public List<Node> getAllTags(String repository) throws Exception ; 
  
  /**
   * Get node following path in repository
   * @param path          path to node
   * @param repository    repository name
   * @return  node following path
   * @throws Exception
   */
  public Node getTag(String path, String repository) throws Exception ;  
  
  /**
   * Get document list      from repository
   * @param tagPath         path to node in all workspace
   * @param repository      repository name
   * @return ArrayList of node
   * @throws Exception
   */
  public List<Node> getDocumentsOnTag(String tagPath, String repository) throws Exception ;
  
  /**
   * Base on uuid in values in EXO_FOLKSONOMY_PROP property in document node,
   * get all node linked to this document node
   * @param document          document node
   * @param repository        repository name
   * @return                  ArrayList of Node
   * @throws Exception
   */
  public List<Node> getLinkedTagsOfDocument(Node document, String repository) throws Exception ;
  
  
  /**
   * Get HTML_STYLE_PROP property in styleName node in repository
   * @param tagName       name of node
   * @param repository      repository name
   * @return  value of HTML_STYLE_PROP property of styleName node
   * @throws Exception
   */
  public String getTagStyle(String tagName, String repository) throws Exception ;
  
  /**
   * Update property TAG_RATE_PROP, HTML_STYLE_PROP following value tagRate, htmlStyle
   * for node in tagPath in repository
   * @param tagPath     path to node
   * @param tagRate
   * @param htmlStyle
   * @param repository
   * @throws Exception
   */
  public void updateStype(String tagPath, String tagRate, String htmlStyle, String repository) throws Exception ;
  
  /**
   *  Get all node base on path = exoTagStylePath_ in repository
   * @param repository
   * @return ArrayList of Node
   * @throws Exception
   */
  public List<Node> getAllTagStyle(String repository) throws Exception ;
  
  /**
   * Init all TagStylePlugin with session in repository name
   * @param repository     repository name
   */
  public void init(String repository) throws Exception ;
  
}
