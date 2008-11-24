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
package org.exoplatform.services.ecm.core;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.core.ManageableRepository;

/*
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 23, 2008  
 */
public class NodeLocation {  
  private String repository;
  private String workspace;
  private String path;
  
  public NodeLocation() { }
  public NodeLocation(final String repository, final String workspace, final String path) {
    this.repository = repository;
    this.workspace = workspace;
    this.path = path; 
  }
  
  public String getRepository() { return repository; }
  public void setRepository(final String repository) { this.repository = repository; }
  
  public String getWorkspace() { return workspace; }
  public void setWorkspace(final String workspace) { this.workspace = workspace; }
  
  public String getPath() { return path; }
  public void setPath(final String path) { this.path = path; }
  
  public static final NodeLocation parse(final String exp) {
    String[] temp = exp.split("::");
    if (temp.length == 3 && temp[2].indexOf("/")>-1) {
      return new NodeLocation(temp[0], temp[1], temp[2]);
    }
    return null ;
  }

  public static final NodeLocation make(final Node node) throws RepositoryException {
    Session session = node.getSession();
    String repository = ((ManageableRepository)session.getRepository()).getConfiguration().getName();
    String workspace = session.getWorkspace().getName();
    String path = node.getPath();
    return new NodeLocation(repository, workspace, path);
  }

  public static final String serialize(final NodeLocation location) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(location.getRepository()).append("::")
    .append(location.getWorkspace()).append("::")
    .append(location.getPath());
    return buffer.toString();
  }
}
