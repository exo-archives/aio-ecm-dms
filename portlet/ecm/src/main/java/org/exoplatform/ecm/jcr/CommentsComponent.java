/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.jcr;

import java.util.List;

import javax.jcr.Node;


/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Jan 31, 2007  
 * 10:36:21 AM
 */
public interface CommentsComponent {
  public String getCommentTemplate() throws Exception;
  public List<Node> getComments() throws Exception;
}
