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
package org.exoplatform.services.cms.categories;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

public interface CategoriesService {
	
	public void addTaxonomyPlugin(ComponentPlugin plugin) ;
	public Node getTaxonomyHomeNode(String repository,SessionProvider provider) throws Exception ;		
	public void addTaxonomy(String parentPath,String childName, String repository) throws Exception  ;
	public void removeTaxonomyNode(String realPath, String repository) throws Exception ;		
	public void moveTaxonomyNode(String srcPath, String destPath, String type, String repository) throws Exception ;		
	
	public boolean hasCategories(Node node) throws Exception;
	public List<Node> getCategories(Node node, String repository) throws Exception;
	public void removeCategory(Node node, String categoryPath, String repository) throws Exception;
	
  public void addMultiCategory(Node node, String[] arrCategoryPath, String repository) throws Exception;
  
	public void addCategory(Node node, String categoryPath, String repository) throws Exception;
  public Session getSession(String repository) throws Exception;
	public void addCategory(Node node, String categoryPath, boolean replaceAll, String repository) throws Exception;
  
  public void init(String repository) throws Exception ;
  
}
