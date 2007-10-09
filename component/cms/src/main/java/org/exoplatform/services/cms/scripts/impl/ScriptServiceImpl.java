package org.exoplatform.services.cms.scripts.impl;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.codehaus.groovy.control.CompilationFailedException;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.impl.BaseResourceLoaderService;
import org.exoplatform.services.cms.impl.ResourceConfig;
import org.exoplatform.services.cms.scripts.CmsScript;
import org.exoplatform.services.cms.scripts.ScriptService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.hibernate.stat.SessionStatisticsImpl;

public class ScriptServiceImpl extends BaseResourceLoaderService implements ScriptService, EventListener {

  private GroovyClassLoader groovyClassLoader_ ;
  private RepositoryService repositoryService_ ;
  private CmsConfigurationService cmsConfigService_ ;
  List<ScriptPlugin> plugins_ = new ArrayList<ScriptPlugin>() ;

  public ScriptServiceImpl(RepositoryService repositoryService, ConfigurationManager cservice,
      CmsConfigurationService cmsConfigService,CacheService cacheService) throws Exception {    
    super(cservice, cmsConfigService, repositoryService, cacheService);
    groovyClassLoader_ = createGroovyClassLoader();
    repositoryService_ = repositoryService ; 
    cmsConfigService_ = cmsConfigService ;
  }

  public void start() {    
    try {
      initPlugins();      
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
    Session session = null ;
    String scriptsPath = getBasePath();
    for(ScriptPlugin plugin : plugins_) {
      if(plugin.getAutoCreateInNewRepository()) {
        List<RepositoryEntry> repositories = repositoryService_.getConfig().getRepositoryConfigurations() ;                
        for(RepositoryEntry repo : repositories) {
          session = repositoryService_.getRepository(repo.getName()).getSystemSession(repo.getDefaultWorkspaceName());          
          Iterator<ObjectParameter> iter = plugin.getScriptIterator() ;
          while(iter.hasNext()) {
            init(session,(ResourceConfig) iter.next().getObject()) ;            
          }
          ObservationManager obsManager = session.getWorkspace().getObservationManager();
          obsManager.addEventListener(this, Event.PROPERTY_CHANGED, scriptsPath, true, null, null, true);
          session.save();
          session.logout();
        }
        return ;        
      }
      String repository = plugin.getInitRepository() ;
      if(repository == null) {
        repository = repositoryService_.getDefaultRepository().getConfiguration().getName();
      }
      ManageableRepository mRepository = repositoryService_.getRepository(repository) ;
      session = mRepository.getSystemSession(mRepository.getConfiguration().getDefaultWorkspaceName()) ;          
      Iterator<ObjectParameter> iter = plugin.getScriptIterator() ;
      while(iter.hasNext()) {
        init(session,(ResourceConfig) iter.next().getObject()) ;            
      }
      ObservationManager obsManager = session.getWorkspace().getObservationManager();
      obsManager.addEventListener(this, Event.PROPERTY_CHANGED, scriptsPath, true, null, null, true);
      session.save();
      session.logout();
    }
  }

  protected String getBasePath() { return cmsConfigService_.getJcrPath(BasePath.CMS_SCRIPTS_PATH); }    

  public void initRepo(String repository) throws Exception {
    ManageableRepository mRepository = repositoryService_.getRepository(repository) ;
    String scriptsPath = getBasePath();
    Session session = mRepository.getSystemSession(mRepository.getConfiguration().getDefaultWorkspaceName()) ;
    for(ScriptPlugin plugin : plugins_) {
      if(!plugin.getAutoCreateInNewRepository()) continue ;
      Iterator<ObjectParameter> iter = plugin.getScriptIterator() ;                           
      while(iter.hasNext()) {
        init(session,(ResourceConfig) iter.next().getObject()) ;            
      }      
      ObservationManager obsManager = session.getWorkspace().getObservationManager();
      obsManager.addEventListener(this, Event.PROPERTY_CHANGED, scriptsPath, true, null, null, true);

    }
    session.save();
    session.logout();
  }
  
  public Node getECMScriptHome(String repository,SessionProvider provider) throws Exception {
    Session session = getSession(repository,provider);
    return getNodeByAlias(BasePath.ECM_EXPLORER_SCRIPTS,session);        
  }        

  public Node getCBScriptHome(String repository,SessionProvider provider) throws Exception {
    Session session = getSession(repository,provider);
    return getNodeByAlias(BasePath.CONTENT_BROWSER_SCRIPTS,session);    
  }


//  public boolean hasCBScript(String repository) throws Exception {    
//    return getCBScriptHome(repository).hasNodes();
//  }

  public List<Node> getCBScripts(String repository,SessionProvider provider) throws Exception {
    List<Node> scriptList = new ArrayList<Node>() ;
    Node cbScriptHome = getCBScriptHome(repository,provider) ;
    for(NodeIterator iter = cbScriptHome.getNodes(); iter.hasNext() ;) {
      scriptList.add(iter.nextNode()) ;
    }      
    return scriptList;    
  }
  
  public List<Node> getECMActionScripts(String repository,SessionProvider provider) throws Exception {
    Session session = getSession(repository,provider);
    return getScriptList(BasePath.ECM_ACTION_SCRIPTS, session);
  }
  
  public List<Node> getECMInterceptorScripts(String repository,SessionProvider provider) throws Exception {
    Session session = getSession(repository,provider);
    return getScriptList(BasePath.ECM_INTERCEPTOR_SCRIPTS, session);
  }

  public List<Node> getECMWidgetScripts(String repository,SessionProvider provider) throws Exception {
    Session session = getSession(repository,provider);
    return getScriptList(BasePath.ECM_WIDGET_SCRIPTS,session);
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
    ExoContainer container = ExoContainerContext.getCurrentContainer() ;
    scriptObject = (CmsScript) container.getComponentInstance(scriptName);
    if(scriptObject !=null ) {
      resourceCache_.put(scriptName,scriptObject) ;
      return scriptObject;
    }
    groovyClassLoader_ = createGroovyClassLoader();
    Class scriptClass = groovyClassLoader_.loadClass(scriptName) ;        
    container.registerComponentImplementation(scriptName, scriptClass); 
    scriptObject = (CmsScript) container.getComponentInstance(scriptName);
    resourceCache_.put(scriptName, scriptObject) ;

    return scriptObject;
  }

  public void addScript(String name, String text, String repository,SessionProvider provider) throws Exception {
    addResource(name, text, repository,provider);
    removeFromCache(name) ;
  }

  public void removeScript(String scriptName, String repository,SessionProvider provider) throws Exception {
    removeResource(scriptName, repository,provider);
    removeFromCache(scriptName) ;
  }    

  private Node getScriptHome(String scriptAlias, Session session) throws Exception {
    String path = cmsConfigService_.getJcrPath(scriptAlias) ;               
    return (Node)session.getItem(path);
  }

  private List<Node> getScriptList(String scriptAlias,Session session) throws Exception {
    List<Node> scriptList = new ArrayList<Node>() ;
    Node scriptHome = getScriptHome(scriptAlias,session) ;
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
        ExoContainer container = ExoContainerContext.getCurrentContainer();
        container.unregisterComponent(scriptName);
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
      Session jcrSession = null ;
      try {
        path = event.getPath();
        List<RepositoryEntry> repositories = repositoryService_.getConfig().getRepositoryConfigurations() ;
        for(RepositoryEntry repo : repositories) {
          try {
            jcrSession = repositoryService_.getRepository(repo.getName())
            .getSystemSession(cmsConfigService_.getWorkspace(repo.getName()));
            Property property = (Property) jcrSession.getItem(path);
            if ("jcr:data".equals(property.getName())) {
              Node node = property.getParent();
              //TODO: Script cache need to redesign to support store scripts in diffirence repositories 
              removeFromCache(node.getName());             
            }
            jcrSession.logout();
          }catch (Exception e) { 
            jcrSession.logout();
            continue ;
          }
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
        SessionProvider provider = SessionProvider.createSystemProvider() ;
        try {
          Node scriptsHome = getResourcesHome(repository,provider);
          Node scriptNode = scriptsHome.getNode(nodeName);
          in = scriptNode.getProperty("jcr:data").getStream();
          provider.close();
        } catch (Exception e) {
          provider.close();
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

  public Node getScriptNode(String scriptName, String repository,SessionProvider provider) throws Exception {
    try {
      Node scriptHome = getResourcesHome(repository,provider) ;
      return scriptHome.getNode(scriptName) ;      
    }catch (Exception e) {
      e.printStackTrace() ;
      return null ;
    }
  }
  
  private Session getSession(String repository,SessionProvider provider) throws Exception {
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository);
    String systemWokspace = manageableRepository.getConfiguration().getSystemWorkspaceName();
    return provider.getSession(systemWokspace,manageableRepository);
  }
  
  private Node getNodeByAlias(String alias,Session session) throws Exception {
    String path = cmsConfigService_.getJcrPath(alias) ;
    return (Node)session.getItem(path);
  }
  
}
