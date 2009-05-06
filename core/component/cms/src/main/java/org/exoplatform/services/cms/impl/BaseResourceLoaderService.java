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

  public BaseResourceLoaderService(ConfigurationManager cservice,
      NodeHierarchyCreator nodeHierarchyCreator, RepositoryService repositoryService,
      CacheService cacheService, DMSConfiguration dmsConfiguration) throws Exception {
    nodeHierarchyCreator_ = nodeHierarchyCreator;
    repositoryService_ = repositoryService;
    cservice_ = cservice;
    resourceCache_ = cacheService.getCacheInstance(this.getClass().getName());
    dmsConfiguration_ = dmsConfiguration;
  }  

  abstract protected String getBasePath(); 
  abstract protected void removeFromCache(String resourceName);

  public void start(){};  
  public void stop(){};  

  protected void init(Session session, ResourceConfig resourceConfig, String location) throws Exception {                   
    addScripts(session, resourceConfig.getRessources(),location) ;       
  }

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

  public String getResourceAsText(String resourceName, String repository) throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    Node resourcesHome = getResourcesHome(repository,sessionProvider);
    Node resourceNode = resourcesHome.getNode(resourceName);
    String text = resourceNode.getProperty("jcr:data").getString();
    sessionProvider.close();
    return text;
  }  

  public NodeIterator getResources(String repository,SessionProvider sessionProvider) throws Exception {
    Node resourcesHome = getResourcesHome(repository,sessionProvider);
    return resourcesHome.getNodes();
  }

  public boolean hasResources(String repository,SessionProvider sessionProvider) throws Exception {    
    Node resourcesHome = getResourcesHome(repository,sessionProvider);
    return resourcesHome.hasNodes();
  }

  public void addResource(String name, String text, String repository,SessionProvider provider) throws Exception {
    Node resourcesHome = getResourcesHome(repository,provider);
    InputStream in = new ByteArrayInputStream(text.getBytes());
    addResource(resourcesHome, name, in);
    resourcesHome.save();
  }

  public void removeResource(String resourceName, String repository,SessionProvider provider) throws Exception {
    removeFromCache(resourceName);
    Node resourcesHome = getResourcesHome(repository,provider);
    Node resource2remove = resourcesHome.getNode(resourceName);
    resource2remove.remove();
    resourcesHome.save();
  }  
}