/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.watch;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Xuan Hoa
 *          hoapham@exoplatform.com
 * Nov 30, 2006  
 */
public interface WatchDocumentService {
  
  final public int NOTIFICATION_BY_EMAIL = 1 ;
  final public int NOTIFICATION_BY_RSS = 2 ;
  final public int FULL_NOTIFICATION = 0 ;
  
  public void watchDocument(Node documentNode, String userName, int notifyType) throws Exception ;
  
  public int getNotificationType(Node documentNode, String userName) throws Exception ;
  
  public void unwatchDocument(Node documentNode, String userName, int notificationType) throws Exception ;   
  
}
