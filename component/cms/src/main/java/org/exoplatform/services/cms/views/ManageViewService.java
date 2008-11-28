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
package org.exoplatform.services.cms.views;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Quang Hung
 *          nguyenkequanghung@yahoo.com
 * Feb 27, 2006
 */
public interface ManageViewService {

  /**
   * Inserts a new view by giving the following params
   * @param name          String  
   *                      The name of view
   * @param permissions   String
   *                      who can access the view
   * @param template      String
   *                      The name of template
   * @param tabs          List
   *                      tabs list
   * @param repository    String
   *                      The name of repository
   * @see                 Node  
   * @throws Exception
   */
  public void addView(String name, String permissions, String template, List tabs, String repository)throws Exception ;
  
  /**
   * Return specify view depend on Name by giving the following params 
   * @param viewName      String
   *                      The name of view
   * @param repository    String
   *                      The name of repository
   * @param provider      SessionProvider
   *                      The SessionProvider object is used to managed Sessions
   * @see                 Node
   * @see                 SessionProvider                          
   * @throws Exception
   */
  public Node getViewByName(String viewName, String repository, SessionProvider provider) throws Exception;
  
  /**
   * Return all string of buttons
   * @throws Exception
   */
  public List getButtons() throws Exception;     
  
  /**
   * Removes the view by giving the following params
   * @param viewName      String
   *                      The name of view
   * @param repository    String
   *                      The name of repository
   * @see                 Node
   * @see                 Session                       
   * @throws Exception
   */
  public void removeView(String viewName, String repository) throws Exception;
  
  /**
   * Return all views of the repository is configed in XML file by giving the following params
   * @param repository    String
   *                      The name of repository
   * @see                 ViewConfig
   * @see                 Node
   * @see                 Session                         
   * @throws Exception
   */
  public List<ViewConfig> getAllViews(String repository) throws Exception;
  
  /**
   * Returns true is the given repository has view by giving the following params
   * @param name          String
   *                      The name of repository
   * @param repository    String
   *                      The name of repository
   * @see                 Node
   * @see                 Session                     
   * @throws Exception
   */
  public boolean hasView(String name, String repository) throws Exception ;
  
  /**
   * Get teamplate Node that has path by giving the following params   
   * @param homeAlias     String
   * @param repository    String
   * @param provider      SessionProvider
   *                      The SessionProvider object is used to managed Sessions
   * @see                 SessionProvider
   * @see                 NodeHierarchyCreator
   * @see                 Node                     
   * @throws Exception
   */
  public Node getTemplateHome(String homeAlias, String repository, SessionProvider provider) throws Exception;
  
  /**
   * Gets all node that has template path to the given node
   * @param homeAlias     String
   *                      Alias of template home
   * @param repository    String
   *                      The name of repository
   * @param provider      SessionProvider
   *                      The SessionProvider object is used to managed Sessions
   * @see                 SessionProvider
   * @see                 NodeHierarchyCreator
   * @see                 Node           
   * @throws Exception
   */
  public List<Node> getAllTemplates(String homeAlias, String repository,SessionProvider provider) throws Exception;
  
  /**
   * Return node that has path of the repository
   * @param path          String
   *                      The path of template
   * @param repository    String
   *                      The name of repository
   * @param provider      SessionProvider
   *                      The SessionProvider object is used to managed Sessions  
   * @see                 SessionProvider
   * @see                 NodeHierarchyCreator
   * @see                 Node                      
   * @throws Exception
   */
  public Node getTemplate(String path, String repository,SessionProvider provider) throws Exception;
  
  /**
   * Inserts a new template for node by specified path
   * @param name            String
   *                        The name of new template         
   * @param content         String
   *                        The property of template
   * @param homeAlias       String
   *                        The path of specified node
   * @param repository      String
   *                        The name of repository
   * @see                   Session
   * @see                   Node                       
   * @throws Exception
   */
  public String addTemplate(String name, String content, String homeAlias, String repository)throws Exception ;
  
  /**
   * Removes the template to the given node by specified the templatePath params
   * @param templatePath    String
   *                        The path of template
   * @param repository      String
   *                        The name of repository
   * @see                   Node
   * @see                   Session                        
   * @throws Exception
   */
  public void removeTemplate(String templatePath, String repository) throws Exception  ;

  /**
   * Insert new tab to the givin view node by specified the following params
   * @param view            Node
   *                        Specify the node wants to add a tab
   * @param name            String
   *                        The name of tab
   * @param buttons         String
   *                        The buttons of tab
   * @see                   Node                        
   * @throws Exception
   */
  public void addTab(Node view, String name, String buttons) throws Exception ;
  
  /**
   * Get all template that is configed in XML file of specified repository 
   * @param repository      String
   *                        The name of repository
   * @see                   ManageViewPlugin                       
   * @throws Exception
   */
  public void init(String repository) throws Exception ;

}
