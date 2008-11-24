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
package org.exoplatform.services.cms.categories.impl;

import java.util.ArrayList;
import java.util.List;

public class TaxonomyConfig {
  
	private List<Taxonomy> taxonomies = new ArrayList<Taxonomy>() ;
  
	public List<Taxonomy> getTaxonomies() { return this.taxonomies ; }	
	public void setTaxonomies(List<Taxonomy> taxonomies) { this.taxonomies = taxonomies ;}
	
	static public class Taxonomy {
		
		private String path ;
		private String name ;
		private String description ;
		
		public String getPath() { return this.path ; }
		public void setPath(String path) { this.path = path ;}
		
		public String getName() {return this.name ; }
		public void setName(String name) { this.name = name ; }
		
		public String getDescription()  { return this.description ; }
		public void setDescription(String description) { this.description = description ; }
	}
}
