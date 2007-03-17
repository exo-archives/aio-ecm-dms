package org.exoplatform.services.cms.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.picocontainer.Startable;

public abstract class BaseResourceLoaderService implements Startable{

  protected CmsConfigurationService cmsConfigService_;
  protected ResourceConfig config_;
  protected RepositoryService repositoryService_;
  protected ConfigurationManager cservice_;
  //protected Map localCache_ = new HashMap();
  protected ExoCache resourceCache_ ;

  public BaseResourceLoaderService(ResourceConfig resourceConfig, ConfigurationManager cservice,
      CmsConfigurationService cmsConfigService, RepositoryService repositoryService,CacheService cacheService) throws Exception {
    cmsConfigService_ = cmsConfigService;
    config_ = resourceConfig;
    repositoryService_ = repositoryService;    
    cservice_ = cservice;        
    resourceCache_ = cacheService.getCacheInstance(this.getClass().getName()) ;
  }  
  
  abstract protected String getBasePath(); 
  abstract protected void removeFromCache(String resourceName);

  protected Session getSystemSession(String workspace)
      throws RepositoryException, RepositoryConfigurationException {
    ManageableRepository jcrRepository = repositoryService_.getRepository();
    return jcrRepository.getSystemSession(workspace);
  }

  public void start(){
    try {
      init();
    } catch (Exception e) {
      e.printStackTrace();
    }
  };
  
  public void stop(){};  
  
  protected void init(ResourceConfig resourceConfig) throws Exception {
  	ResourceConfig config  = config_ ;
  	config_ = resourceConfig ;
  	init() ;
  	config_ = config ;
  }
  
  protected void init() throws Exception {
    Session session = null;
    try {
      session = getSystemSession(config_.getWorkspace());
    } catch (RepositoryException re) {
      return;
    }

    String resourcesPath = getBasePath();
    List resources = config_.getRessources();
    if (resources.size() == 0)
      return;

    try {
      String firstResourceName = ((ResourceConfig.Resource) resources.get(0)).getName();
      session.getItem(resourcesPath + "/" + firstResourceName);
      return;
    } catch (PathNotFoundException e) {
    }

    Node root = session.getRootNode();
    Node resourcesHome = (Node) session.getItem(resourcesPath);

    String warPath = cmsConfigService_.getContentLocation() 
        + "/system" + resourcesPath.substring(resourcesPath.lastIndexOf("/")) ;

    for (Iterator iter = resources.iterator(); iter.hasNext();) {
      ResourceConfig.Resource resource = (ResourceConfig.Resource) iter.next();
      String name = resource.getName();
      String path = warPath + "/" + name;
      InputStream in = cservice_.getInputStream(path);
      addResource(resourcesHome, name, in);
    }
    root.save();
    session.save() ;
  }

  private void addResource(Node resourcesHome, String resourceName, InputStream in)
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
    } catch (PathNotFoundException e) {
      contentNode = resourcesHome.addNode(resourceName, "nt:resource");
      contentNode.setProperty("jcr:encoding", "UTF-8");
      contentNode.setProperty("jcr:mimeType", "text/xml");
    }
    contentNode.setProperty("jcr:data", in);
    contentNode.setProperty("jcr:lastModified", new GregorianCalendar());
    resourcesHome.save() ;
  }
  
  protected Node getResourcesHome() throws Exception {
    Session session = null;
    try {
      session = getSystemSession(config_.getWorkspace());
    } catch (RepositoryException re) {
      return null;
    }
    String resourcesPath = getBasePath();
    return (Node) session.getItem(resourcesPath);
  }  
  
  public String getResourceAsText(String resourceName) throws Exception {
    Node resourcesHome = getResourcesHome();
    Node resourceNode = resourcesHome.getNode(resourceName);
    return resourceNode.getProperty("jcr:data").getString();
  }  
  
  public NodeIterator getResources() throws Exception {
    Node resourcesHome = getResourcesHome();
    return resourcesHome.getNodes();
  }

  public boolean hasResources() throws Exception {
    Node resourcesHome = getResourcesHome();
    return resourcesHome.hasNodes();
  }

  public void addResource(String name, String text) throws Exception {
    Node resourcesHome = getResourcesHome();
    InputStream in = new ByteArrayInputStream(text.getBytes());
    addResource(resourcesHome, name, in);
    resourcesHome.save();
  }

  public void removeResource(String resourceName) throws Exception {
    removeFromCache(resourceName);
    Node resourcesHome = getResourcesHome();
    Node resource2remove = resourcesHome.getNode(resourceName);
    resource2remove.remove();
    resourcesHome.save();
  }  

}
