package org.exoplatform.services.cms.queries;

import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.jcr.RepositoryService;

public class QueryPlugin extends BaseComponentPlugin {

  private static String STATEMENT = "jcr:statement".intern() ;
  private static String LANGUAGE = "jcr:language".intern() ;
  private static String PERMISSIONS = "exo:permissions".intern() ;
  private static String CACHED_RESULT = "exo:cachedResult".intern() ;
  private InitParams params_ ;
  private CmsConfigurationService cmsConfigService_ ;
  private RepositoryService repositoryService_ ;
  
  public QueryPlugin(RepositoryService repositoryService, 
      InitParams params, CmsConfigurationService cmsConfigService) throws Exception {
    params_ = params ;
    cmsConfigService_ = cmsConfigService ;
    repositoryService_ = repositoryService ;
    
    Session session = null ;
    ValueFactory vt = null ;
    Iterator<ObjectParameter> it = params.getObjectParamIterator() ;       
    String queryPath = cmsConfigService.getJcrPath(BasePath.QUERIES_PATH);
    Node queryHome = null ;
     while(it.hasNext()){
      QueryData data = (QueryData)it.next().getObject() ;
      session = repositoryService.getRepository(data.getRepository())
      .getSystemSession(cmsConfigService.getWorkspace(data.getRepository())) ;
      queryHome = (Node)session.getItem(queryPath) ;
      if(!queryHome.hasNode(data.getName())){
        vt = repositoryService.getRepository(data.getRepository())
        .getSystemSession(cmsConfigService.getWorkspace()).getValueFactory() ;
        Node queryNode = queryHome.addNode(data.getName(), "nt:query");
        queryNode.addMixin("mix:sharedQuery") ;
        queryNode.setProperty(STATEMENT, data.getStatement()) ;
        queryNode.setProperty(LANGUAGE, data.getLanguage()) ;
        Value vl = vt.createValue(data.getPermissions()) ;
        Value[] vls = {vl} ;
        queryNode.setProperty(PERMISSIONS, vls) ;
        queryNode.setProperty(CACHED_RESULT, data.getCacheResult()) ;
        queryHome.save() ;
      }
      session.save() ;
    }
  } 
  
  public void init(String repository) throws Exception {
    Session session = null ;
    ValueFactory vt = null ;
    Iterator<ObjectParameter> it = params_.getObjectParamIterator() ;       
    String queryPath = cmsConfigService_.getJcrPath(BasePath.QUERIES_PATH);
    String defaultRepo = repositoryService_.getDefaultRepository().getConfiguration().getName() ;
    Node queryHome = null ;
     while(it.hasNext()){
      QueryData data = (QueryData)it.next().getObject() ;
      if(data.getRepository().equals(defaultRepo)) {
        session = repositoryService_.getRepository(defaultRepo)
        .getSystemSession(cmsConfigService_.getWorkspace(defaultRepo)) ;
        queryHome = (Node)session.getItem(queryPath) ;
        if(!queryHome.hasNode(data.getName())){
          vt = repositoryService_.getRepository(data.getRepository())
          .getSystemSession(cmsConfigService_.getWorkspace()).getValueFactory() ;
          Node queryNode = queryHome.addNode(data.getName(), "nt:query");
          queryNode.addMixin("mix:sharedQuery") ;
          queryNode.setProperty(STATEMENT, data.getStatement()) ;
          queryNode.setProperty(LANGUAGE, data.getLanguage()) ;
          Value vl = vt.createValue(data.getPermissions()) ;
          Value[] vls = {vl} ;
          queryNode.setProperty(PERMISSIONS, vls) ;
          queryNode.setProperty(CACHED_RESULT, data.getCacheResult()) ;
          queryHome.save() ;
        }
        session.save() ;
      }
    }
  }
}
