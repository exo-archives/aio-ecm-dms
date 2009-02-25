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
package org.exoplatform.services.ecm.view;

import java.util.List;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Apr 28, 2008  
 */
public interface ViewManagerService {
  
  final public String VIEW_REGISTRY_GROUP = "/exo:registry/exo:services/exo:ecm/exo:views/views".intern();
  final public String TEMPLATE_REGISTRY_GROUP = "/exo:registry/exo:services/exo:ecm/exo:views/templates".intern();
  
  public void addView(ViewEntry viewEntry,String repository, SessionProvider sessionProvider) throws Exception ;
  public ViewEntry getViewByName(String viewName, String repository, SessionProvider provider) throws Exception;
  public List<String> getButtons() throws Exception;     
  
  public void removeView(String viewName, String repository, SessionProvider sessionProvider) throws Exception;         
  public List<ViewEntry> getAllViews(String repository, SessionProvider sessionProvider) throws Exception;
    
  public String addTemplate(String name, String content, String homeAlias, String repository)throws Exception ;
  public void removeTemplate(String templatePath, String repository) throws Exception  ;
  
}
