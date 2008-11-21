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

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import javax.imageio.ImageIO;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cms.impl.ImageUtils;
import org.exoplatform.services.cms.thumbnail.ThumbnailService;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 10, 2008 1:58:10 PM
 */
public class ThumbnailServiceImpl implements ThumbnailService, Startable {

  final private static String JCR_CONTENT = "jcr:content".intern();
  final private static String JCR_MIMETYPE = "jcr:mimeType".intern();
  final private static String JCR_DATA = "jcr:data".intern();
  final private static String NT_FILE = "nt:file".intern();
  
  private boolean isEnableThumbnail_ = false;
  private String smallSize_;
  private String mediumSize_;
  private String bigSize_;
  private String mimeTypes_;
  
  public ThumbnailServiceImpl(InitParams initParams) throws Exception {
    smallSize_ = initParams.getValueParam("smallSize").getValue();
    mediumSize_ = initParams.getValueParam("mediumSize").getValue();
    bigSize_ = initParams.getValueParam("bigSize").getValue();
    mimeTypes_ = initParams.getValueParam("mimetypes").getValue();
    isEnableThumbnail_ = Boolean.parseBoolean(initParams.getValueParam("enable").getValue());
  }

  public List<Node> getFlowImages(Node node) throws Exception {
    NodeIterator nodeIter = node.getNodes();
    List<Node> listNodes = new ArrayList<Node>();
    Node thumbnailNode = null;
    while(nodeIter.hasNext()) {
      Node childNode = nodeIter.nextNode();
      thumbnailNode = addThumbnailNode(childNode);
      if(thumbnailNode != null && thumbnailNode.isNodeType(EXO_THUMBNAIL) && 
          thumbnailNode.hasProperty(BIG_SIZE)) {
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
      if(childNode.getPrimaryNodeType().getName().equals(NT_FILE)) {
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
      contentNode = childNode.getNode(JCR_CONTENT);
      if(contentNode.getProperty(JCR_MIMETYPE).getString().equals(jcrMimeType)) {
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

  public InputStream getThumbnailImage(Node node, String thumbnailType) throws Exception {
    Node thumbnailNode = addThumbnailNode(node);
    if(thumbnailNode != null && thumbnailNode.hasProperty(thumbnailType)) {
      return thumbnailNode.getProperty(thumbnailType).getStream();
    }
    return null;
  }
  
  public void addThumbnailImage(Node thumbnailNode, BufferedImage image, String propertyName) throws Exception {
    if(!thumbnailNode.isNodeType(EXO_THUMBNAIL) && thumbnailNode.canAddMixin(EXO_THUMBNAIL)) {
      thumbnailNode.addMixin(EXO_THUMBNAIL);
    }
    if(propertyName.equals(SMALL_SIZE)) parseImageSize(thumbnailNode, image, smallSize_, SMALL_SIZE);
    else if(propertyName.equals(MEDIUM_SIZE)) parseImageSize(thumbnailNode, image, mediumSize_, MEDIUM_SIZE);
    else if(propertyName.equals(BIG_SIZE)) parseImageSize(thumbnailNode, image, bigSize_, BIG_SIZE);
  }
 
  public void createSpecifiedThumbnail(Node node, BufferedImage image, String propertyName) throws Exception {
    addThumbnailImage(addThumbnailNode(node), image, propertyName);
  }
  
  public void createThumbnailImage(Node node, BufferedImage image, String mimeType) throws Exception {
    Node thumbnailNode = addThumbnailNode(node);
    if(thumbnailNode != null) {
      if(mimeType.startsWith("image")) processImage2Image(thumbnailNode, image);
      thumbnailNode.setProperty(THUMBNAIL_LAST_MODIFIED, new GregorianCalendar());
      thumbnailNode.save();
    }
  }
  
  public void processThumbnailList(List<Node> listNodes, String type) throws Exception {
    for(Node node : listNodes) {
      Node thumbnailNode = addThumbnailNode(node);
      if(thumbnailNode != null && !thumbnailNode.hasProperty(THUMBNAIL_LAST_MODIFIED) && 
          node.isNodeType(NT_FILE)) {
        Node contentNode = node.getNode(JCR_CONTENT);
        if(contentNode.getProperty(JCR_MIMETYPE).getString().startsWith("image")) {
          BufferedImage image = ImageIO.read(contentNode.getProperty(JCR_DATA).getStream());
          addThumbnailImage(thumbnailNode, image, type);
        }
      }
    }
  }
  
  public List<String> getMimeTypes() {
    return Arrays.asList(mimeTypes_.split(";"));
  }
  
  public void start() { }
  
  public void stop() { }
  
  public Node addThumbnailNode(Node node) throws Exception {
    Node parentNode = node.getParent();
    Node thumbnailFolder = null;
    try {
      thumbnailFolder = parentNode.getNode(ThumbnailService.EXO_THUMBNAILS_FOLDER);
    } catch(PathNotFoundException e) {
      thumbnailFolder = parentNode.addNode(ThumbnailService.EXO_THUMBNAILS_FOLDER);
      if(thumbnailFolder.canAddMixin(ThumbnailService.HIDDENABLE_NODETYPE)) {
        thumbnailFolder.addMixin(ThumbnailService.HIDDENABLE_NODETYPE);
      }
    }
    parentNode.save();
    String identifier = ((NodeImpl) node).getInternalIdentifier();
    Node thumbnailNode = null;
    try {
      thumbnailNode = thumbnailFolder.getNode(identifier);
    } catch(PathNotFoundException path) {
      thumbnailNode = thumbnailFolder.addNode(identifier);
    }
    thumbnailFolder.save();
    return thumbnailNode;
  }
  
  public Node getThumbnailNode(Node node) throws Exception {
    Node parentNode = node.getParent();
    try {
      Node thumbnailFolder = parentNode.getNode(EXO_THUMBNAILS_FOLDER);
      return thumbnailFolder.getNode(((NodeImpl) node).getInternalIdentifier());
    } catch(Exception e) {
      return null;
    }
  }
  
  public void processRemoveThumbnail(Node showingNode) throws Exception {
    Node parentNode = showingNode.getParent();
    if(parentNode.hasNode(EXO_THUMBNAILS_FOLDER)) {
      Node thumbnailFolder = parentNode.getNode(EXO_THUMBNAILS_FOLDER);
      thumbnailFolder.save();
      try {
        thumbnailFolder.getNode(((NodeImpl) showingNode).getInternalIdentifier()).remove();
      } catch(PathNotFoundException path) {
        return;
      }
    }
  }
  
  private void processImage2Image(Node node, BufferedImage image) throws Exception {
    parseImageSize(node, image, smallSize_, SMALL_SIZE);
    parseImageSize(node, image, mediumSize_, MEDIUM_SIZE);
    parseImageSize(node, image, bigSize_, BIG_SIZE);
  }
  
  private void createThumbnailImage(Node thumbnailNode, BufferedImage image, int width, int height, 
      String propertyName) throws Exception {
    InputStream thumbnailStream = ImageUtils.scaleImage(image, width, height);
    thumbnailNode.setProperty(propertyName, thumbnailStream);
    thumbnailNode.setProperty(THUMBNAIL_LAST_MODIFIED, new GregorianCalendar());
    thumbnailNode.save();
    thumbnailStream.close();
  }
  
  private void parseImageSize(Node node, BufferedImage image, String size, String propertyName) throws Exception {
    if(size.indexOf("x") > -1) {
      String[] imageSize = size.split("x");
      int width = Integer.parseInt(imageSize[0]);
      int height = Integer.parseInt(imageSize[1]);
      createThumbnailImage(node, image, width, height, propertyName);
    }
  }
}
