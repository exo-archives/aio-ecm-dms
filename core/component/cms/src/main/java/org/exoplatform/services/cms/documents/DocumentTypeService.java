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
package org.exoplatform.services.cms.documents;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 6, 2009  
 * 3:38:57 AM
 */

/**
 * Get documents by mime types
 */
public interface DocumentTypeService {

  /**
   * Get all document by mimetype
   * @param workspace The name of workspace will be used to get documents
   * @param repository The name of repository will be used to get documents
   * @param sessionProvider 
   * @param mimeType The mime type of node(For example: image/jpg)
   * @return List<Node> all documents by mime type
   * @throws Exception
   */
  public List<Node> getAllDocumentsByType(String workspace, String repository, 
      SessionProvider sessionProvider, String mimeType) throws Exception;
  
  /**
   * Get all document by array of mimetype
   * @param workspace The name of workspace will be used to get documents
   * @param repository The name of repository will be used to get documents
   * @param sessionProvider 
   * @param mimeTypes The array of mimetype(For example: ["image/jpg", "image/png"])
   * @return List<Node> all documents by mime type
   * @throws Exception
   */
  public List<Node> getAllDocumentsByType(String workspace, String repository, 
      SessionProvider sessionProvider, String[] mimeTypes) throws Exception;
  
  /**
   * Get all document type by user
   * @param workspace The name of workspace will be used to get documents
   * @param repository The name of repository will be used to get documents
   * @param sessionProvider 
   * @param mimeTypes The array of mimetype(For example: ["image/jpg", "image/png"])
   * @param userName The name of current user
   * @return List<Node> all documents by mime type
   * @throws Exception
   */
  public List<Node> getAllDocumentsByUser(String workspace, String repository, 
      SessionProvider sessionProvider, String[] mimeTypes, String userName) throws Exception;
}
