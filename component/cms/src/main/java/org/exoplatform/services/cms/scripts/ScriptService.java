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
package org.exoplatform.services.cms.scripts;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

public interface ScriptService {
  
  /**
   * This method will get node for ECM Explorer Scripts by giving the following params: repository, 
   * provider
   * @param repository    String
   *                      The name of repository
   * @param provider      SessionProvider
   * @see                 Node
   * @see                 NodeHierarchyCreator
   * @see                 SessionProvider                     
   * @return              Node
   * @throws Exception
   */
  public Node getECMScriptHome(String repository,SessionProvider provider) throws Exception ;  
  
  /**
   * This method will get node for Content Browser Scripts by giving the following params: repository,
   * provider
   * @param repository    String
   *                      The name of reporsitory        
   * @param provider
   * @see                 Node
   * @see                 NodeHierarchyCreator
   * @see                 SessionProvider                     
   * @return              Node
   * @throws Exception
   */
  public Node getCBScriptHome(String repository,SessionProvider provider) throws Exception ;    
  
  /**
   * This method will get all node for ECM Action Scripts by giving the following params: repository,
   * provider
   * @param repository    String
   *                      The name of reporsitory        
   * @param provider
   * @see                 Node
   * @see                 NodeHierarchyCreator
   * @see                 SessionProvider                     
   * @return              Node
   * @throws Exception
   */
  public List<Node> getECMActionScripts(String repository,SessionProvider provider) throws Exception ;
  
  /**
   * This method will get all node for ECM Interceptor Scripts by giving the following params: repository,
   * provider
   * @param repository    String
   *                      The name of reporsitory        
   * @param provider
   * @see                 Node
   * @see                 NodeHierarchyCreator
   * @see                 SessionProvider                     
   * @return              List<Node>
   * @throws Exception
   */
  public List<Node> getECMInterceptorScripts(String repository,SessionProvider provider) throws Exception;
  
  /**
   * This method will get all node for ECM Widget Scripts by giving the following params: repository,
   * provider
   * @param repository    String
   *                      The name of reporsitory        
   * @param provider
   * @see                 Node
   * @see                 NodeHierarchyCreator
   * @see                 SessionProvider                     
   * @return              List<Node>
   * @throws Exception
   */
  public List<Node> getECMWidgetScripts(String repository,SessionProvider provider) throws Exception ;
  
  /**
   * This method will get script by giving the following params: repository, provider
   * @param scriptPath    String
   *                      The path of script
   * @param repository    String
   *                      The name of repository
   * @see                 CmsScript                     
   * @return              CmsScript
   * @throws Exception
   */
  public CmsScript getScript(String scriptPath, String repository) throws Exception;
  
  /**
   * This method will get base path of script
   * @see                 NodeHierarchyCreator  
   * @return              String
   * @throws Exception
   */
  public String getBaseScriptPath() throws Exception ;  
  
  /**
   * This method will get script by giving the following params: scriptPath, repository  
   * @param scriptPath    String
   *                      The path of script
   * @param repository    String
   *                      The name of repository
   * @see                 Node                     
   * @return              String
   * @throws Exception
   */
  public String getScriptAsText(String scriptPath, String repository) throws Exception;        
  
  /**
   * This method will add script by giving the following params: name, text, repository, provider 
   * @param name          String
   *                      The name of script
   * @param text          String
   * @param repository    String
   *                      The name of repository      
   * @param provider      SessionProvider
   * @see                 InputStream
   * @see                 Node
   * @see                 SessionProvider
   * @throws Exception
   */
  public void addScript(String name, String text, String repository,SessionProvider provider) throws Exception;
  
  /**
   * This method will remove script by giving the following params: name, text, repository, provider 
   * @param scriptPath    String
   *                      The path of script
   * @param repository    String
   *                      The name of repository      
   * @param provider      SessionProvider
   * @see                 Node
   * @see                 SessionProvider
   * @throws Exception
   */
  public void removeScript(String scriptPath, String repository, SessionProvider provider) throws Exception;
  
  /**
   * This method will get script node by giving the following params: scriptName, repository, provider  
   * @param scriptName    String
   *                      The name of script  
   * @param repository    String
   *                      The name of repository 
   * @param provider      SessionProvider
   * @see                 Node
   * @see                 SessionProvider
   * @return              Node
   * @throws Exception
   */
  public Node getScriptNode(String scriptName, String repository,SessionProvider provider) throws Exception; 
  
  /**
   * This method will init repository by giving the following params: repository 
   * @param repository    String
   *                      The name of repository
   * @see                 ManageableRepository
   * @see                 ObservationManager
   * @see                 Session                       
   * @throws Exception
   */
  public void initRepo(String repository) throws Exception ;
}
