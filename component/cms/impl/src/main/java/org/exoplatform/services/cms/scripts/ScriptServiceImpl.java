package org.exoplatform.services.cms.scripts;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.codehaus.groovy.control.CompilationFailedException;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.impl.BaseResourceLoaderService;
import org.exoplatform.services.cms.impl.ResourceConfig;
import org.exoplatform.services.jcr.RepositoryService;

public class ScriptServiceImpl extends BaseResourceLoaderService implements ScriptService, EventListener {

  private GroovyClassLoader groovyClassLoader_ ;
  private RepositoryService repositoryService_ ;
  private CmsConfigurationService cmsConfigService_ ;
  List<ScriptPlugin> plugins_ = new ArrayList<ScriptPlugin>() ;

  public ScriptServiceImpl(RepositoryService repositoryService,
      CmsConfigurationService cmsConfigService, ConfigurationManager cservice,CacheService cacheService) throws Exception {    
    super(null, cservice, cmsConfigService, repositoryService,cacheService);
    groovyClassLoader_ = createGroovyClassLoader();
    repositoryService_ = repositoryService ; 
    cmsConfigService_ = cmsConfigService ;
  }

  public void start() {    
    try {
      initPlugins();
      initObserver();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void addScriptPlugin(ComponentPlugin plugin) {
    if(plugin instanceof ScriptPlugin) {			
      plugins_.add((ScriptPlugin)plugin) ;
    }
  }

  private void initPlugins() throws Exception{
    for(ScriptPlugin plugin:plugins_) {
      config_ = plugin.getScripts() ;
      init(config_) ;
    }
  }

  protected String getBasePath() {  
    return cmsConfigService_.getJcrPath(BasePath.CMS_SCRIPTS_PATH);
  }  

  private void initObserver() throws Exception {
    String scriptsPath = getBasePath();
    ObservationManager obsManager = repositoryService_.getRepository(config_.getRepositoty())
                                    .getSystemSession(config_.getWorkspace()).getWorkspace().getObservationManager();
    obsManager.addEventListener(this, Event.PROPERTY_CHANGED, scriptsPath, true, null, null, true);
  }
  
  public void initRepo(String repository) throws Exception {
    
    for(ScriptPlugin plugin:plugins_) {
      ResourceConfig config = plugin.getScripts() ;
      initPlugins(config, repository) ;
      initObserver(config, repository) ;
    }   
  }
  
  private void initObserver(ResourceConfig config, String repository) throws Exception {
    String scriptsPath = getBasePath();
    String defaultRepo = repositoryService_.getDefaultRepository().getConfiguration().getName() ;
    if(config.getRepositoty().equals(defaultRepo)) {
      ObservationManager obsManager = repositoryService_.getRepository(repository)
      .getSystemSession(cmsConfigService_.getWorkspace(repository)).getWorkspace().getObservationManager();
      obsManager.addEventListener(this, Event.PROPERTY_CHANGED, scriptsPath, true, null, null, true);
    }
    
  }
  
  private void initPlugins(ResourceConfig config, String repository) throws Exception {
    Session session = null;
    String defaultRepo = repositoryService_.getDefaultRepository().getConfiguration().getName() ;
    if(config.getRepositoty().equals(defaultRepo)) {
      session = repositoryService_.getRepository(repository).getSystemSession(cmsConfigService_.getWorkspace(repository)) ;
      String resourcesPath = getBasePath();
      List resources = config.getRessources();
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
  }
  public Node getECMScriptHome(String repository) throws Exception {
    Node ecmScriptHome = null ;
    try {     
      ecmScriptHome = getScriptHome(BasePath.ECM_EXPLORER_SCRIPTS, repository) ;
    }catch (Exception e) {
      Session session = repositoryService_.getRepository(repository).getSystemSession(cmsConfigService_.getWorkspace(repository)) ;
      String cbScriptHomePath = cmsConfigService_.getJcrPath(BasePath.ECM_EXPLORER_SCRIPTS) ;
      ecmScriptHome = (Node)session.getItem(cbScriptHomePath) ;      
    }
    if(ecmScriptHome == null) throw new ItemNotFoundException() ;
    return ecmScriptHome ;    
  }        

  public Node getCBScriptHome(String repository) throws Exception {
    Node cbHomeNode = null ;
    try {     
      cbHomeNode = getScriptHome(BasePath.CONTENT_BROWSER_SCRIPTS, repository) ;
    }catch (Exception e) {
      Session session = repositoryService_.getRepository(repository).getSystemSession(cmsConfigService_.getWorkspace()) ;
      String cbScriptHomePath = cmsConfigService_.getJcrPath(BasePath.CONTENT_BROWSER_SCRIPTS) ;
      cbHomeNode = (Node)session.getItem(cbScriptHomePath) ;      
    }
    if(cbHomeNode == null) throw new ItemNotFoundException() ;
    return cbHomeNode ;
  }


  public boolean hasCBScript(String repository) throws Exception {    
    return getCBScriptHome(repository).hasNodes();
  }

  public List<Node> getCBScripts(String repository) throws Exception {
    List<Node> scriptList = new ArrayList<Node>() ;
    Node cbScriptHome = getCBScriptHome(repository) ;
    for(NodeIterator iter = cbScriptHome.getNodes(); iter.hasNext() ;) {
      scriptList.add(iter.nextNode()) ;
    }      
    return scriptList;    
  }

  public Node getECMActionScriptHome(String repository) throws Exception {
    Node actionScriptHome = null ;
    try {     
      actionScriptHome = getScriptHome(BasePath.ECM_ACTION_SCRIPTS, repository) ;
    }catch (Exception e) {
      Session session = repositoryService_.getRepository(repository).getSystemSession(cmsConfigService_.getWorkspace()) ;
      String actionScriptHomePath = cmsConfigService_.getJcrPath(BasePath.ECM_ACTION_SCRIPTS) ;
      actionScriptHome = (Node)session.getItem(actionScriptHomePath) ;      
    }
    if(actionScriptHome == null)
      throw new ItemNotFoundException() ;
    return actionScriptHome;
  }

  public List<Node> getECMActionScripts(String repository) throws Exception {    
    return getScriptList(BasePath.ECM_ACTION_SCRIPTS, repository);
  }

  public Node getECMInterceptorScriptHome(String repository) throws Exception {
    Node interceptorScriptHome = null ;
    try {     
      interceptorScriptHome = getScriptHome(BasePath.ECM_INTERCEPTOR_SCRIPTS, repository) ;
    }catch (Exception e) {
      Session session = repositoryService_.getRepository(repository).getSystemSession(cmsConfigService_.getWorkspace()) ;
      String actionScriptHomePath = cmsConfigService_.getJcrPath(BasePath.ECM_INTERCEPTOR_SCRIPTS) ;
      interceptorScriptHome = (Node)session.getItem(actionScriptHomePath) ;      
    }
    if(interceptorScriptHome == null)
      throw new ItemNotFoundException() ;
    return interceptorScriptHome;

  }
  public List<Node> getECMInterceptorScripts(String repository) throws Exception {    
    return getScriptList(BasePath.ECM_INTERCEPTOR_SCRIPTS, repository);
  }

  public Node getECMWidgetScriptHome(String repository) throws Exception {  
    return getScriptHome(BasePath.ECM_WIDGET_SCRIPTS, repository);
  }    

  public List<Node> getECMWidgetScripts(String repository) throws Exception {    
    return getScriptList(BasePath.ECM_WIDGET_SCRIPTS, repository);
  } 

  public String getBaseScriptPath() throws Exception {   
    return getBasePath() ;
  }

  public String[] getECMCategoriesPath() throws Exception {
    String[] categoriesPath 
    = { cmsConfigService_.getJcrPath(BasePath.ECM_ACTION_SCRIPTS),
        cmsConfigService_.getJcrPath(BasePath.ECM_INTERCEPTOR_SCRIPTS), 
        cmsConfigService_.getJcrPath(BasePath.ECM_WIDGET_SCRIPTS) } ;
    return categoriesPath;
  }

  public String[] getCBCategoriesPath() throws Exception {
    String[] categoriesPath = { cmsConfigService_.getJcrPath(BasePath.CONTENT_BROWSER_SCRIPTS)} ;
    return categoriesPath;
  }

  public String getScriptAsText(String scriptName, String repository) throws Exception {
    return getResourceAsText(scriptName, repository);
  }

  public CmsScript getScript(String scriptName, String repository) throws Exception {
    CmsScript scriptObject = (CmsScript) resourceCache_.get(scriptName);
    if (scriptObject != null) return scriptObject;
    PortalContainer pC = PortalContainer.getInstance();
    scriptObject = (CmsScript) pC.getComponentInstance(scriptName);
    if(scriptObject !=null ) {
      resourceCache_.put(scriptName,scriptObject) ;
      return scriptObject;
    }
    groovyClassLoader_ = createGroovyClassLoader();
    Class scriptClass = groovyClassLoader_.loadClass(scriptName) ;        
    pC.registerComponentImplementation(scriptName, scriptClass); 
    scriptObject = (CmsScript) pC.getComponentInstance(scriptName);
    resourceCache_.put(scriptName, scriptObject) ;
  
    return scriptObject;
  }

  public void addScript(String name, String text, String repository) throws Exception {
    addResource(name, text, repository);
    removeFromCache(name) ;
  }

  public void removeScript(String scriptName, String repository) throws Exception {
    removeResource(scriptName, repository);
    removeFromCache(scriptName) ;
  }    

  private Node getScriptHome(String scriptAlias, String repository) throws Exception {
    String path = cmsConfigService_.getJcrPath(scriptAlias) ;    
    Session session = repositoryService_.getRepository(repository).getSystemSession(cmsConfigService_.getWorkspace(repository)) ;            
    return (Node)session.getItem(path);
  }

  private List<Node> getScriptList(String scriptAlias, String repository) throws Exception {
    List<Node> scriptList = new ArrayList<Node>() ;
    Node scriptHome = getScriptHome(scriptAlias, repository) ;
    for(NodeIterator iter = scriptHome.getNodes(); iter.hasNext() ;) {
      scriptList.add(iter.nextNode()) ;
    }      
    return scriptList;
  }  

  protected void removeFromCache(String scriptName){  
    try{
      Object cachedobject = resourceCache_.get(scriptName);
      if (cachedobject != null) {        
        resourceCache_.remove(scriptName) ;
        PortalContainer pC = PortalContainer.getInstance();
        pC.unregisterComponent(scriptName);
        Class scriptClass = (Class)cachedobject ;
        //groovyClassLoader_.removeFromCache(scriptClass) ;
      }
    }catch (Exception e) {
    }        
  }

  public void onEvent(EventIterator events) {
    while (events.hasNext()) {
      Event event = events.nextEvent();
      String path = null;
      try {
        path = event.getPath();
        Session jcrSession = 
          repositoryService_.getRepository(config_.getRepositoty()).getSystemSession(config_.getWorkspace());
        Property property = (Property) jcrSession.getItem(path);
        if ("jcr:data".equals(property.getName())) {
          Node node = property.getParent();
          //TODO: Script cache need to redesign to support store scripts in diffirence repositories 
          removeFromCache(node.getName());
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private GroovyClassLoader createGroovyClassLoader() {
    ClassLoader parentLoader = Thread.currentThread().getContextClassLoader();    
    return new GroovyClassLoader(parentLoader) {
      protected Class findClass(String className) throws ClassNotFoundException {
        String repository = null ;
        String filename = null ;
        String nodeName = null ;
        if(className.indexOf(":") > -1) {
          String[] array = className.split(":") ;
          repository = array[0] ;
          nodeName = array[1] ;
          filename = array[1].replace('.', File.separatorChar) + ".groovy";
        }else {
          nodeName = className ;
          filename = className.replace('.', File.separatorChar) + ".groovy";
        }
        InputStream in = null;
        try {
          Node scriptsHome = getResourcesHome(repository);
          Node scriptNode = scriptsHome.getNode(nodeName);
          in = scriptNode.getProperty("jcr:data").getStream();
        } catch (Exception e) {
          throw new ClassNotFoundException("Could not read " + nodeName + ": " + e);
        }
        try {
          return parseClass(in, filename);
        } catch (CompilationFailedException e2) {
          throw new ClassNotFoundException("Syntax error in " + filename
              + ": " + e2);
        }
      }
    };
  }

  public Node getScriptNode(String scriptName, String repository) throws Exception {
    try {
      Node scriptHome = getResourcesHome(repository) ;
      return scriptHome.getNode(scriptName) ;      
    }catch (Exception e) {
      e.printStackTrace() ;
      return null ;
    }
  }
}
