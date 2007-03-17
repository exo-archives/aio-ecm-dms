/******************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL. All rights reserved.            *
 * Please look at license.txt in info directory for more license detail.      *
 ******************************************************************************/
package org.exoplatform.services.workflow.impl.bonita;

import hero.interfaces.BnNodeLocal;
import hero.interfaces.BnNodeLocalHome;
import hero.interfaces.BnNodePK;
import hero.interfaces.BnNodeUtil;
import hero.interfaces.BnProjectLocal;
import hero.interfaces.BnProjectLocalHome;
import hero.interfaces.BnProjectUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.ResourceFactory;
import org.exoplatform.services.templates.velocity.ResourceLoaderPlugin;
import org.exoplatform.services.workflow.FileDefinition;
import org.exoplatform.services.workflow.WorkflowFileDefinitionService;
import org.exoplatform.services.workflow.WorkflowServiceContainer;

/**
 * Enables to retrieve .vm files contained by the Business Process Archives
 * 
 * Created by Bull R&D
 * @author Brice Revenant
 * Mar 22, 2006
 */
public class PARResourceLoaderImpl extends ResourceLoaderPlugin {

  /** Reference to the File Definition service */
  private WorkflowFileDefinitionService fileDefinitionService = null;
  
  /** Prefix of the Resource names */
  public static final String RESOURCE_NAME_PREFIX = "par:";
  
  /** Caches created Resources */
  private Map<String, Resource> resources = null;
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.templates.velocity.ResourceLoaderPlugin#canLoadResource(java.lang.String)
   */
  public boolean canLoadResource(String s) {
    
    return s.startsWith(PARResourceLoaderImpl.RESOURCE_NAME_PREFIX);
  }

  /* (non-Javadoc)
   * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getLastModified(org.apache.velocity.runtime.resource.Resource)
   */
  public long getLastModified(Resource resource) {
    
    return 0;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.templates.velocity.ResourceLoaderPlugin#getResource(java.lang.String, int, java.lang.String)
   */
  public Resource getResource(String resourceName,
                              int resourceType,
                              String encoding) 
    throws ResourceNotFoundException, ParseErrorException, Exception {
    
    // Retrieve the Resource from the cache
    Resource resource = resources.get(resourceName);
    
    if(resource == null) {
      // The Resource is not found in the cache. Create a new one.
      String path = resourceName.substring(
        PARResourceLoaderImpl.RESOURCE_NAME_PREFIX.length(),
        resourceName.length());
      resource = ResourceFactory.getResource(path, resourceType);
      resource.setRuntimeServices(rsvc);
      resource.setName(path);
      resource.setEncoding(encoding);
      resource.setResourceLoader(this);
      resource.process();
    }

    return resource;
  }
  
  /* (non-Javadoc)
   * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getResourceStream(java.lang.String)
   */
  public synchronized InputStream getResourceStream(String fileLocation)
    throws ResourceNotFoundException {
    
    InputStream inputStream = null;
    
    try {
      // Retrieve the Task identifier
      String[] fileLocationSplit = fileLocation.split(":");
      String taskId   = fileLocationSplit[0];
      
      // Retrieve the Process Model name
      BnNodeLocalHome nodeHome = BnNodeUtil.getLocalHome();
      BnNodeLocal node = nodeHome.findByPrimaryKey(new BnNodePK(taskId));
      String taskName = node.getName();
      String instanceName = node.getBnProject().getName();
      String processModelName = WorkflowServiceContainerHelper.
        getModelName(instanceName);
      
      // Retrieve the Process Model identifier
      BnProjectLocalHome projectHome = BnProjectUtil.getLocalHome();
      BnProjectLocal project = projectHome.findByName(processModelName);
      String processModelId = project.getId();
      
      // Retrieve the contents of the file from the File Definition service
      FileDefinition fileDefinition = fileDefinitionService.
        retrieve(processModelId);
      String customizedViewName = fileDefinition.getCustomizedView(taskName);
      byte[] customizedViewBytes = fileDefinition.getEntry(customizedViewName);
      
      // Create a Stream from the file contents
      inputStream = new ByteArrayInputStream(customizedViewBytes);
    }
    catch(Exception e) {
      throw new ResourceNotFoundException(
        "Cannot retrieve data in process "
        + fileLocation
        + ". Make sure you have a valid location.");
    }

    return inputStream;
  }
  
  /* (non-Javadoc)
   * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#init(org.apache.commons.collections.ExtendedProperties)
   */
  public void init(ExtendedProperties configuration) {
  }
  
  /* (non-Javadoc)
   * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#isSourceModified(org.apache.velocity.runtime.resource.Resource)
   */
  public boolean isSourceModified(Resource resource) {
    
    return false;
  }
  
  /**
   * Creates a new instance of the Plugin
   * 
   * @param fileDefinitionService reference to the File Definition service
   */
  public PARResourceLoaderImpl(
    WorkflowFileDefinitionService fileDefinitionService)
    throws Exception {
    
    // Cache references to the injected services
    this.fileDefinitionService = fileDefinitionService;
    
    // Create a cache of Resources
    this.resources = new WeakHashMap<String, Resource>();
  }
}
