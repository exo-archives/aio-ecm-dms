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
package org.exoplatform.services.ecm.taxonomy.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;

/**
 * Created by The eXo Platform SAS
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *			    xxx5669@yahoo.com
 * May 28, 2008  
 */
public class TaxonomyPlugin extends BaseComponentPlugin {

  private List<HierarchicalTaxonomyConfig> taxonomiesConfig_ = new ArrayList<HierarchicalTaxonomyConfig>(3);

  public TaxonomyPlugin(InitParams params) {  
    Iterator<ObjectParameter> iterator = params.getObjectParamIterator();   
    while (iterator.hasNext()) {      
      ObjectParameter object = iterator.next();
      taxonomiesConfig_.add((HierarchicalTaxonomyConfig)object.getObject());
    }    
  }
  
  public List<HierarchicalTaxonomyConfig> getPredefinedTaxonomies() { return taxonomiesConfig_; }
  
}
