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
  
  public void addTag(Node node, String[] tagName, String repository) throws Exception ;  
  public List<Node> getAllTags(String repository) throws Exception ;   
  public Node getTag(String path, String repository) throws Exception ;  
  public List<Node> getDocumentsOnTag(String tagPath, String repository) throws Exception ;
  public List<Node> getLinkedTagsOfDocument(Node document, String repository) throws Exception ;
  
  public String getTagStyle(String tagName, String repository) throws Exception ;
  public void updateStype(String tagPath, String tagRate, String htmlStyle, String repository) throws Exception ;  
  public List<Node> getAllTagStyle(String repository) throws Exception ;
  public void init(String repository) throws Exception ;
  
}
