package org.exoplatform.services.templates.velocity.impl;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.ResourceFactory;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.templates.velocity.ResourceLoaderPlugin;

public class JCRResourceLoaderImpl extends ResourceLoaderPlugin {
  protected ExoCache jcrcache_  ;
  private RepositoryService repositoryService_;
  private PortalContainerInfo containerInfo_;
  private CmsConfigurationService cmsConfig_;
  
  public JCRResourceLoaderImpl(RepositoryService service, CacheService cacheService, 
      CmsConfigurationService cmsConfig, PortalContainerInfo containerInfo) throws Exception {
    jcrcache_ = cacheService.getCacheInstance(JCRResourceLoaderImpl.class.getName()) ;
    repositoryService_ = service;
    containerInfo_ = containerInfo;
    cmsConfig_ = cmsConfig;
  }
  
  public void init( ExtendedProperties configuration) {
  }
  
  public synchronized InputStream getResourceStream(String templateName) throws ResourceNotFoundException {
    try { 
      ManageableRepository repository = repositoryService_.getRepository(); 
      Session session = repository.getSystemSession(cmsConfig_.getWorkspace());
      Node node = (Node)session.getItem(templateName) ;
      NodeType type = node.getPrimaryNodeType() ;
      if("exo:template".equals(type.getName())) {    
        return node.getProperty("exo:templateFile").getStream() ;
      } 
    } catch (Exception ex) {
      throw new ResourceNotFoundException("Error: " +  ex.getMessage()) ;
    }
    throw new ResourceNotFoundException("Cannot retrieve data for node " + templateName + 
                                        "Make sure you have a valid path and node type is exo:template") ; 
  }
  
  public boolean isSourceModified(Resource resource) {
    return false ;
  }
  
  public long getLastModified(Resource resource) {
    return 0;       
  }
  
  public Resource getResource(String resourceName, int resourceType, String encoding )
  throws ResourceNotFoundException, ParseErrorException, Exception {
    String portalName = containerInfo_.getContainerName() ;
    String key = portalName + resourceName ;
    Resource resource = (Resource)jcrcache_.get(key);
    if( resource != null) {
      return resource ;
    }  
    //remove jcr: 
    String path = resourceName.substring(4 , resourceName.length()) ;
    resource = ResourceFactory.getResource(path, resourceType);  
    if(rsvc != null) {
      resource.setRuntimeServices( rsvc );
    }else {
      VelocityServiceImpl vcservice = (VelocityServiceImpl)PortalContainer.getInstance()
                                     .getComponentInstancesOfType(VelocityServiceImpl.class).get(0);
      resource.setRuntimeServices( vcservice );
    }    
    resource.setName( path );
    resource.setEncoding( encoding );
    resource.setResourceLoader(this);
    resource.process() ;
    jcrcache_.put(key, resource) ;
    return resource;
  }
  public void removeFromCache(String resourceName) {
    try{
      String portalName = containerInfo_.getContainerName() ;
      String key = portalName + resourceName ;
      Object cachedObject = jcrcache_.get(key);
      if(cachedObject != null)
        jcrcache_.remove(key) ;
    }catch(Exception e){      
    }
  }
  
  public boolean cachedResource(String resourceName) {
    try{
      String portalName = containerInfo_.getContainerName() ;
      String key = portalName + resourceName ;
      Object cachedObject = jcrcache_.get(key);
        if(cachedObject != null)
          return true ; 
        return false ;
    }catch(Exception e) {
      return false ;
    }    
  }
  public boolean canLoadResource(String s)   { return s.startsWith("jcr:") ; }
}
