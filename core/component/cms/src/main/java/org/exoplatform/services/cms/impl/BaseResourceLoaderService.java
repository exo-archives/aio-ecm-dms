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
package org.exoplatform.services.cms.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.picocontainer.Startable;

public abstract class BaseResourceLoaderService implements Startable{

  protected NodeHierarchyCreator nodeHierarchyCreator_;

  protected RepositoryService    repositoryService_;

  protected ConfigurationManager cservice_;

  protected ExoCache             resourceCache_;

  /**
   * DMS configuration which used to store informations
   */
  private DMSConfiguration       dmsConfiguration_;

  /**
   * Constructor method
   * Init cservice, nodeHierarchyCreator, repositoryService, cacheService, dmsConfiguration
   * @param cservice                ConfigurationManager
   * @param nodeHierarchyCreator    NodeHierarchyCreator
   * @param repositoryService       RepositoryService
   * @param cacheService            CacheService
   * @param dmsConfiguration        DMSConfiguration
   * @throws Exception
   */
  public BaseResourceLoaderService(ConfigurationManager cservice,
      NodeHierarchyCreator nodeHierarchyCreator, RepositoryService repositoryService,
      CacheService cacheService, DMSConfiguration dmsConfiguration) throws Exception {
    nodeHierarchyCreator_ = nodeHierarchyCreator;
    repositoryService_ = repositoryService;
    cservice_ = cservice;
    resourceCache_ = cacheService.getCacheInstance(this.getClass().getName());
    dmsConfiguration_ = dmsConfiguration;
  }  

  /**
   * get BasePath
   * @return
   */
  abstract protected String getBasePath(); 
  
  /**
   * remove From Cache
   * @param resourceName    String
   *                        The name of resource
   */
  abstract protected void removeFromCache(String resourceName);

  /**
   * {@inheritDoc}
   */
  public void start(){};
  
  /**
   * {@inheritDoc}
   */
  public void stop(){};  

  /**
   * init 
   * @param session           Session
   * @param resourceConfig    ResourceConfig
   * @param location          String
   *                          The code of location
   * @see                     Session
   * @see                     ResourceConfig
   * @throws Exception
   */
  protected void init(Session session, ResourceConfig resourceConfig, String location) throws Exception {                   
    addScripts(session, resourceConfig.getRessources(),location) ;       
  }

  /**
   * add Script with following param
   * @param session       Session       
   * @param resources     List
   * @param location      String
   * @see                 ResourceConfig
   * @throws Exception
   */
  protected void addScripts(Session session, List resources,String location) throws Exception{
    String resourcesPath = getBasePath();
    if (resources.size() == 0) return;
    try {
      String firstResourceName = ((ResourceConfig.Resource) resources.get(0)).getName();
      session.getItem(resourcesPath + "/" + firstResourceName);
      return;
    } catch (PathNotFoundException e) {
    }

    Node root = session.getRootNode();
    Node resourcesHome = (Node) session.getItem(resourcesPath);
    String warPath = location + resourcesPath.substring(resourcesPath.lastIndexOf("/")) ;
    for (Iterator iter = resources.iterator(); iter.hasNext();) {
      ResourceConfig.Resource resource = (ResourceConfig.Resource) iter.next();
      String name = resource.getName();
      String path = warPath + "/" + name;
      InputStream in = cservice_.getInputStream(path);
      addResource(resourcesHome, name, in);
    }
    root.save();    
  }

  /**
   * add Resource
   * @param resourcesHome     Node
   * @param resourceName      String
   * @param in                InputStream
   * @throws Exception
   */
  public void addResource(Node resourcesHome, String resourceName, InputStream in)
  throws Exception {
    Node contentNode = null;
    if(resourceName.lastIndexOf("/")>-1) {
      String realParenPath = StringUtils.substringBeforeLast(resourceName,"/") ;
      Node parentResource = resourcesHome.getNode(realParenPath) ;
      resourcesHome = parentResource ;
      resourceName = StringUtils.substringAfterLast(resourceName,"/") ;
    }        
    try {
      contentNode = resourcesHome.getNode(resourceName);
      if(!contentNode.isCheckedOut()) contentNode.checkout() ;
    } catch (PathNotFoundException e) {
      contentNode = resourcesHome.addNode(resourceName, "nt:resource");
      contentNode.setProperty("jcr:encoding", "UTF-8");
      contentNode.setProperty("jcr:mimeType", "text/xml");
    }
    contentNode.setProperty("jcr:data", in);
    contentNode.setProperty("jcr:lastModified", new GregorianCalendar());
    resourcesHome.save() ;
  }

  /**
   * get ResourcesHome
   * @param repository        String  
   *                          The name of repository
   * @param sessionProvider   SessionProvider
   * @see                     SessionProvider
   * @see                     DMSRepositoryConfiguration
   * @see                     ManageableRepository
   * @return
   * @throws Exception
   */
  protected Node getResourcesHome(String repository,SessionProvider sessionProvider) throws Exception {    
    ManageableRepository manageableRepository = null ;
    if(repository == null) {
      manageableRepository = repositoryService_.getDefaultRepository();
    }else {
      manageableRepository = repositoryService_.getRepository(repository) ;
    }
    DMSRepositoryConfiguration dmsRepoConfig = dmsConfiguration_.getConfig(manageableRepository.getConfiguration().getName());
    Session session = sessionProvider.getSession(dmsRepoConfig.getSystemWorkspace(), manageableRepository);     
    String resourcesPath = getBasePath();
    return (Node) session.getItem(resourcesPath);
  }  

  /**
   * get Resource As Text
   * @param resourceName    String
   * @param repository      String
   *                        The name of repository
   * @see                                          
   * @return                SessionProvider
   * @throws Exception
   */
  public String getResourceAsText(String resourceName, String repository) throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    Node resourcesHome = getResourcesHome(repository,sessionProvider);
    Node resourceNode = resourcesHome.getNode(resourceName);
    String text = resourceNode.getProperty("jcr:data").getString();
    sessionProvider.close();
    return text;
  }  

  /**
   * get Resources
   * @param repository          String  
   *                            The name of repository
   * @param sessionProvider     SessionProvider
   * @see                       SessionProvider       
   * @return
   * @throws Exception
   */
  public NodeIterator getResources(String repository,SessionProvider sessionProvider) throws Exception {
    Node resourcesHome = getResourcesHome(repository,sessionProvider);
    return resourcesHome.getNodes();
  }

  /**
   * Check has Resources
   * @param repository        String  
   *                          The name of repository
   * @param sessionProvider   SessionProvider
   * @see                     SessionProvider
   * @return
   * @throws Exception
   */
  public boolean hasResources(String repository,SessionProvider sessionProvider) throws Exception {    
    Node resourcesHome = getResourcesHome(repository,sessionProvider);
    return resourcesHome.hasNodes();
  }
  
  /**
   * add Resource
   * @param name          String  
   *                      The name of resource
   * @param text          String
   * @param repository    String  
   *                      The name of repository
   * @param provider      SessionProvider
   * @see                 SessionProvider
   * @throws Exception
   */
  public void addResource(String name, String text, String repository,SessionProvider provider) throws Exception {
    Node resourcesHome = getResourcesHome(repository,provider);
    InputStream in = new ByteArrayInputStream(text.getBytes());
    addResource(resourcesHome, name, in);
    resourcesHome.save();
  }

  /**
   * remove Resource
   * @param resourceName    String  
   *                        The name of resource
   * @param repository      String  
   *                        The name of repository
   * @param provider        SessionProvider
   * @see                   SessionProvider
   * @throws Exception
   */
  public void removeResource(String resourceName, String repository,SessionProvider provider) throws Exception {
    removeFromCache(resourceName);
    Node resourcesHome = getResourcesHome(repository,provider);
    Node resource2remove = resourcesHome.getNode(resourceName);
    resource2remove.remove();
    resourcesHome.save();
  }  
}