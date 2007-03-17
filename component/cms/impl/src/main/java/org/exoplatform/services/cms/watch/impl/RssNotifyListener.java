/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.watch.impl;

import javax.jcr.Node;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Xuan Hoa Pham
 *          hoapham@exoplatform.com
 * 					phamvuxuanhoa@gmail.com
 * Dec 6, 2006  
 */
public class RssNotifyListener implements EventListener {
  
  private Node observedNode_ ;
  
  public RssNotifyListener(Node obNode) {
    observedNode_ = obNode ;
  }

  public void onEvent(EventIterator arg0) { 
    // TODO
    System.err.println("===>>>OnEvent in RssNotifyListener");
  }

}
