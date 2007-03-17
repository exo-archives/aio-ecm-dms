/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.jcr;


/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Jan 31, 2007  
 * 10:36:21 AM
 */
public interface VoteComponent {
  public double getRating() throws Exception;
  public String getVoteTemplate() throws Exception;
}
