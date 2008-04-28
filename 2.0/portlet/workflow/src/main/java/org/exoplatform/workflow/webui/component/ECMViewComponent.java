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
package org.exoplatform.workflow.webui.component;

import java.util.List;
import javax.jcr.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jan 16, 2007  
 * 10:36:21 AM
 */
public interface ECMViewComponent {
  public void setNode(Node node);

  public Node getNode() throws Exception;
  
  public Node getOriginalNode() throws Exception;
  
  public String getNodeType() throws Exception;

  public boolean isNodeTypeSupported();

  public String getTemplatePath() throws Exception;

  public List<Node> getRelations() throws Exception;

  public List<Node> getAttachments() throws Exception;

  public boolean isRssLink() ;

  public String getRssLink() ;

  public List getSupportedLocalise() throws Exception ;
  
  public void setLanguage(String language) ;
  
  public String getLanguage() ;
  
  public Object getComponentInstanceOfType(String className) ;
  
 public String getWebDAVServerPrefix() throws Exception ;
  
  public String getImage(Node node) throws Exception ;
  
  public String getPortalName() ;
  
  public String getRepository() throws Exception ;
  
  public String getWorkspaceName() throws Exception ;
  
  public String getViewTemplate(String nodeTypeName, String templateName) throws Exception ;
  
  public List<Node> getComments() throws Exception;
  public String getDownloadLink(Node node) throws Exception;
  
  public String encodeHTML(String text) throws Exception;
  
  public String getIcons(Node node, String size) throws Exception ;
}