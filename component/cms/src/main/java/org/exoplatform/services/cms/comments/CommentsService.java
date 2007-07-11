/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.comments;

import java.util.List;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Xuan Hoa
 *          hoa.pham@exoplatform.com
 * Jan 22, 2007  
 */
public interface CommentsService {
  
  public void addComment(Node document,String commentor, String email,String site, String comment,String language) throws Exception ;
  public List<Node> getComments(Node document,String language) throws Exception ;
  
}
