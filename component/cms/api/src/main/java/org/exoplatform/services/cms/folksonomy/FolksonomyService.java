/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.folksonomy;

import java.util.List;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Xuan Hoa Pham
 *          hoapham@exoplatform.com
 * 					phamvuxuanhoa@gmail.com
 * Dec 5, 2006  
 */
public interface FolksonomyService {
  
  public void addTag(Node node, String[] tagName) throws Exception ;  
  public List<Node> getAllTags() throws Exception ;   
  public Node getTag(String path) throws Exception ;  
  public List<Node> getDocumentsOnTag(String tagPath) throws Exception ;
  public List<Node> getLinkedTagsOfDocument(Node document) throws Exception ;
  
  public String getTagStyle(String tagName) throws Exception ;
  public void updateStype(String tagPath, String tagRate, String htmlStyle) throws Exception ;  
  public List<Node> getAllTagStyle() throws Exception ;
}
