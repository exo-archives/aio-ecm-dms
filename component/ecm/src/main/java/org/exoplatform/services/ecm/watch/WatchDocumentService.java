/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.ecm.watch;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *			    xxx5669@yahoo.com
 * Jun 2, 2008  
 */
public interface WatchDocumentService {
  final public int NOTIFICATION_BY_EMAIL = 1;
  final public int NOTIFICATION_BY_RSS = 2;
  final public int FULL_NOTIFICATION = 0;
  
  /**
   * Watching the document that is specified by the node by giving a userName, notifyType and
   * sessionProvider object
   * <p>If the document is watching, all thing that changes to it's property will be notified 
   * to user specified by the userName
   * @param documentNode      Specify the document for watching
   * @param userName          The username of current user is votting. It can't be <code>null<code>
   * @param notifyType        Type of notification. Its can be 0, 1 or 2
   * @param sessionProvider   The sessionProvider object is userd to managed Sessions
   * @see                     SessionProvider
   * @see                     Node
   * @throws Exception
   */
  public void watchDocument(Node documentNode, String userName, int notifyType, SessionProvider sessionProvider) throws Exception;
  
  /**
   * This method will gets the type of notification for the specify document
   * <p>If that document is not a exo:watchable document, the value return is -1
   * If notification is notified by email, the value return is 1 
   * If notification is notified by rss, the value return is 2
   * If notification is notified by rss and email, the value return is 0
   * @param documentNode      Specify the document for watching
   * @param userName          The username of current user is votting. It can't be <code>null<code>
   * @see                     Node      
   * @return                  0, 1, 2 or -1
   * @throws Exception
   */
  public int getNotificationType(Node documentNode, String userName) throws Exception;
  
  /**
   * UnWatching the document that is specified by the node by giving a userName, notifyType and
   * sessionProvider object
   * <p>If the document is unwatching, all thing that changes to it's property will not be notified 
   * to user specified by the userName
   * @param documentNode      Specify the document for watching
   * @param userName          The username of current user is votting. It can't be <code>null<code>
   * @param notifyType        Type of notification. Its can be 0, 1 or 2
   * @param sessionProvider   The sessionProvider object is userd to managed Sessions
   * @see                     SessionProvider
   * @see                     Node
   * @throws Exception
   */
  public void unwatchDocument(Node documentNode, String userName, int notificationType, SessionProvider sessionProvider) throws Exception;   
}
