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
  
  public void addTag(Node node, String[] tagName, String repository) throws Exception ;  
  public List<Node> getAllTags(String repository) throws Exception ;   
  public Node getTag(String path, String repository) throws Exception ;  
  public List<Node> getDocumentsOnTag(String tagPath, String repository) throws Exception ;
  public List<Node> getLinkedTagsOfDocument(Node document, String repository) throws Exception ;
  
  public String getTagStyle(String tagName, String repository) throws Exception ;
  public void updateStype(String tagPath, String tagRate, String htmlStyle, String repository) throws Exception ;  
  public List<Node> getAllTagStyle(String repository) throws Exception ;
  public void init(String repository) throws Exception ;
  
}
