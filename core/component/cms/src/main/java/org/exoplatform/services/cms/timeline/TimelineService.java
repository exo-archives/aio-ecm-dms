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
package org.exoplatform.services.cms.timeline;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 22, 2009  
 * 8:19:26 AM
 */
/**
 * Support to get all documents by time frame
 */
public interface TimelineService {
  
  /**
   * Get all documents of Today
   * @param repository Repository name
   * @param workspace Workspace name
   * @param sessionProvider SessionProvider
   * @param userName Logged in user
   * @return List<Node>
   */
  public List<Node> getDocumentsOfToday(String repository, String workspace, 
      SessionProvider sessionProvider, String userName) throws Exception;
  
  /**
   * Get all documents of Yesterday
   * @param repository Repository name
   * @param workspace Workspace name
   * @param sessionProvider SessionProvider
   * @param userName Logged in user
   * @return List<Node>
   */  
  public List<Node> getDocumentsOfYesterday(String repository, String workspace, 
      SessionProvider sessionProvider, String userName) throws Exception;

  /**
   * Get all documents earlier this week
   * @param repository Repository name
   * @param workspace Workspace name
   * @param sessionProvider SessionProvider
   * @param userName Logged in user
   * @return List<Node>
   */
  public List<Node> getDocumentsOfEarlierThisWeek(String repository, String workspace, 
      SessionProvider sessionProvider, String userName) throws Exception;
  
  /**
   * Get all documents earlier this month
   * @param repository Repository name
   * @param workspace Workspace name
   * @param sessionProvider SessionProvider
   * @param userName Logged in user
   * @return List<Node>
   */
  public List<Node> getDocumentsOfEarlierThisMonth(String repository, String workspace, 
      SessionProvider sessionProvider, String userName) throws Exception;
  
  /**
   * Get all documents earlier this year
   * @param repository Repository name
   * @param workspace Workspace name
   * @param sessionProvider SessionProvider
   * @param userName Logged in user
   * @return List<Node>
   */
  public List<Node> getDocumentsOfEarlierThisYear(String repository, String workspace, 
      SessionProvider sessionProvider, String userName) throws Exception;
}
