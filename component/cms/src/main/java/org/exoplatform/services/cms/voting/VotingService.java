/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.voting;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Xuan Hoa
 *          hoa.pham@exoplatform.com
 * Jan 17, 2007  
 */
public interface VotingService {
  
  public void vote(Node document, double rate, String userName, String language) throws Exception ;
  
  public long getVoteTotal(Node node) throws Exception ;
}
