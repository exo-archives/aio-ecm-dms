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
package org.exoplatform.services.cms.relations;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;
/**
 * @author monica franceschini
 */
public interface RelationsService {

  public boolean hasRelations(Node node) throws Exception;

  public List<Node> getRelations(Node node, String repository, SessionProvider provider) throws Exception;

  public void removeRelation(Node node, String relationPath, String repository) throws Exception;

  public void addRelation(Node node, String relationPath, String workspaceName,String repository) throws Exception; 
  
  public void init(String repository) throws Exception ;
  
  //public void addRelation(Node node, String relationPath, boolean replaceAll) throws Exception; 
}
