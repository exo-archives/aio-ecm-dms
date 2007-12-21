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
