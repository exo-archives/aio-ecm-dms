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
package org.exoplatform.services.cms.thumbnail;

import java.io.InputStream;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 10, 2008 1:59:21 PM
 */
/**
 * This service will be support to create thumbnail for node
 * Get image and any file type in node
 */
public interface ThumbnailService {
/**
 * Return all nt:file node at current node
 * @param node Current node
 * @return List<Node>
 * @throws RepositoryException
 */
  public List<Node> getAllFileInNode(Node node) throws RepositoryException;
  
/**
 * Return the list of node in the current node with mimetype specified
 * @param node Current node
 * @param jcrMimeType Mime type of node will be retrieve
 * @return List<Node>
 * @throws RepositoryException
 */  
  public List<Node> getFileNodesByType(Node node, String jcrMimeType) throws RepositoryException;
/**
 * Return a list image in node
 * @param node Current node
 * @return List<Node>
 * @throws RepositoryException
 */  
  public List<Node> getImages(Node node) throws RepositoryException;
/**
 * To setup status of node is allow thumbnail or not
 * @param isEnable
 */  
  public void setEnableThumbnail(boolean isEnable);
/**
 * Return the status of node is enable thumbnail or not
 * @return Boolean value
 */  
  public boolean isEnableThumbnail();
/**
 * Create thumbnail for node with default size:
 * Small size, medium size, big size
 * @param node
 * @throws Exception
 */  
  public void createThumbnail(Node node) throws Exception;
/**
 * Return the data of thumbnail with specified type
 * @param node
 * @param thumbnailType Type of thumbnail will be return (small, medium, big or specified if has)
 * @return InputStream data
 * @throws Exception
 */  
  public InputStream getThumbnail(Node node, String thumbnailType) throws Exception;
/**
 * Create a thumbnail for node with size specified 
 * @param node 
 * @param width Width of thumbnail image
 * @param height Height of thumbnail image
 * @throws Exception
 */  
  public void createThumbnail(Node node, String width, String height) throws Exception;
}
