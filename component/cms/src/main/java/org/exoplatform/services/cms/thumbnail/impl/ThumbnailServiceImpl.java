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
package org.exoplatform.services.cms.thumbnail.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.exoplatform.services.cms.thumbnail.ThumbnailService;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 10, 2008 1:58:10 PM
 */
public class ThumbnailServiceImpl implements ThumbnailService {

  public static String EXO_THUMBNAIL = "exo:thumbnail";
  public static String SMALL_SIZE = "exo:smallSize";
  public static String MEDIUM_SIZE = "exo:mediumSize";
  public static String BIG_SIZE = "exo:bigSize";
  public static String SPECIFIED_SIZE = "exo:specifiedSize";
  
  private boolean isEnableThumbnail_ = false;
  
  public List<Node> getImages(Node node) throws RepositoryException {
    List<Node> fileListNodes = getAllFileInNode(node);
    List<Node> listNodes = new ArrayList<Node>();
    Node contentNode = null;
    for(Node childNode : fileListNodes) {
      contentNode = childNode.getNode("jcr:content");
      if(contentNode.getProperty("jcr:mimeType").getString().startsWith("image")) {
        listNodes.add(childNode);
      }
    }
    return listNodes;
  }

  public List<Node> getAllFileInNode(Node node) throws RepositoryException {
    List<Node> fileListNodes = new ArrayList<Node>();
    NodeIterator nodeIter = node.getNodes();
    Node childNode = null;
    while(nodeIter.hasNext()) {
      childNode = nodeIter.nextNode();
      if(childNode.getPrimaryNodeType().getName().equals("nt:file")) {
        fileListNodes.add(childNode);
      }
    }
    return fileListNodes;
  }
  
  public List<Node> getFileNodesByType(Node node, String jcrMimeType) throws RepositoryException {
    List<Node> fileListNodes = getAllFileInNode(node);
    List<Node> listNodes = new ArrayList<Node>();
    Node contentNode = null;
    for(Node childNode : fileListNodes) {
      contentNode = childNode.getNode("jcr:content");
      if(contentNode.getProperty("jcr:mimeType").getString().equals(jcrMimeType)) {
        listNodes.add(childNode);
      }
    }
    return listNodes;
  }

  public boolean isEnableThumbnail() {
    return isEnableThumbnail_;
  }

  public void setEnableThumbnail(boolean isEnable) {
    isEnableThumbnail_ = isEnable;
  }

  public void createThumbnail(Node node) throws Exception {
    if(!node.isNodeType(EXO_THUMBNAIL)) {
      if(node.canAddMixin(EXO_THUMBNAIL)) {
        node.addMixin(EXO_THUMBNAIL);
      }
    }
    
  }

  public InputStream getThumbnail(Node node, String thumbnailType) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public void createThumbnail(Node node, String width, String height) throws Exception {
    if(!node.isNodeType(EXO_THUMBNAIL)) {
      if(node.canAddMixin(EXO_THUMBNAIL)) {
        node.addMixin(EXO_THUMBNAIL);
      }
    }
  }
}
