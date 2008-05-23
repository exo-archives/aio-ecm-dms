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
package org.exoplatform.services.ecm.taxonomy;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * May 6, 2008  
 */
public interface TaxonomyManagerService {
  
  public void addTaxonomy(String parentPath, String childName, String repository, SessionProvider sessionProvider) throws Exception  ;
  public void removeTaxonomy(String realPath, String repository, SessionProvider sessionProvider) throws Exception ;   
  public void copyTaxonomy(String srcPath, String destPath, String repository, SessionProvider sessionProvider) throws Exception ;
  public void cutTaxonomy(String srcPath, String destPath, String repository, SessionProvider sessionProvider) throws Exception ;    
  
  public List<Node> getCategories(Node node, SessionProvider sessionProvider) throws Exception;
  public void removeCategory(Node node, String categoryPath, SessionProvider sessionProvider) throws Exception;
  
  public void addCategory(Node node, String categoryPath, SessionProvider sessionProvider) throws Exception;   
  public void addCategory(Node node, String categoryPath, boolean replaceAll, SessionProvider sessionProvider) throws Exception;
  
}
