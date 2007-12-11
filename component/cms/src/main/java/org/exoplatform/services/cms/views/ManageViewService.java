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
package org.exoplatform.services.cms.views;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Quang Hung
 *          nguyenkequanghung@yahoo.com
 * Feb 27, 2006
 */
public interface ManageViewService {

  public void addView(String name, String permissions, String template, List tabs, String repository)throws Exception ;
  public Node getViewByName(String viewName, String repository, SessionProvider provider) throws Exception;
  public List getButtons() throws Exception;     
  
  public void removeView(String viewName, String repository) throws Exception;         
  public List<ViewConfig> getAllViews(String repository) throws Exception;
  
  public boolean hasView(String name, String repository) throws Exception ;
  public Node getTemplateHome(String homeAlias, String repository, SessionProvider provider) throws Exception;
  public List<Node> getAllTemplates(String homeAlias, String repository,SessionProvider provider) throws Exception;
  public Node getTemplate(String path, String repository,SessionProvider provider) throws Exception; 
  public String addTemplate(String name, String content, String homeAlias, String repository)throws Exception ;
  public void removeTemplate(String templatePath, String repository) throws Exception  ;

  public void addTab(Node view, String name, String buttons) throws Exception ; 
  public void init(String repository) throws Exception ;

}
