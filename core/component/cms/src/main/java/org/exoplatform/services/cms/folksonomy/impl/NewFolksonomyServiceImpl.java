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
package org.exoplatform.services.cms.folksonomy.impl;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.cms.folksonomy.NewFolksonomyService;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.cms.link.NodeFinder;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 16, 2009  
 * 10:30:24 AM
 */
public class NewFolksonomyServiceImpl implements NewFolksonomyService {

  private NodeHierarchyCreator nodeHierarchyCreator;
  private LinkManager linkManager;
  private NodeFinder nodeFinder;
  
  public NewFolksonomyServiceImpl(NodeHierarchyCreator nodeHierarchyCreator, 
      LinkManager linkManager, NodeFinder nodeFinder) {
    this.nodeHierarchyCreator = nodeHierarchyCreator;
    this.linkManager = linkManager;
    this.nodeFinder = nodeFinder;
    // TODO Auto-generated constructor stub
  }

  public void addGroupsTag(String[] tagsName, Node documentNode, String repository, String workspace, String[] roles) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void addPrivateTag(String[] tagsName, Node documentNode, String repository, String workspace, String userName) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void addPublicTag(String treePath, String[] tagsName, Node documentNode, String repository, String workspace) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void addSiteTag(String siteName, String treePath, String[] tagsName, Node node, String repository, String workspace) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public List<Node> getAllDocumentsByTag(String tagPath, String repository, String workspace) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Node> getAllGroupTags(String[] roles, String repository, String workspace) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Node> getAllPrivateTags(String userName, String repository, String workspace) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Node> getAllPublicTags(String treePath, String repository, String workspace) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Node> getAllSiteTags(String siteName, String treePath, String repository, String workspace) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Node> getAllTagStyle(String repository, String workspace) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public String getTagStyle(String tagPath, String repository, String workspace) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public void init(String repository) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public Node modifyTagName(String tagPath, String newTagName, String repository, String workspace) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public void removeTag(String tagPath, String repository, String workspace) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void removeTagOfDocument(String tagPath, Node document, String repository, String workspace) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void updateTagStype(String styleName, String tagRate, String htmlStyle, String repository, String workspace) throws Exception {
    // TODO Auto-generated method stub
    
  }

}
