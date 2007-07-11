package org.exoplatform.services.cms.queries.impl;

import java.util.Iterator;
import java.util.List;

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
import org.exoplatform.services.jcr.config.RepositoryEntry;

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
  } 
  
  public void init() throws Exception {
    Iterator<ObjectParameter> it = params_.getObjectParamIterator() ;       
    String queryPath = cmsConfigService_.getJcrPath(BasePath.QUERIES_PATH);
     while(it.hasNext()){
      QueryData data = (QueryData)it.next().getObject() ;
      if(data.getAutoCreatedInNewRepository()) {
        List<RepositoryEntry> repositories = repositoryService_.getConfig().getRepositoryConfigurations() ;
        for(RepositoryEntry repo : repositories) {
          addQuery(getSession(repo.getName()), data, queryPath) ;
        }
      }else {
        addQuery(getSession(data.getRepository()), data, queryPath) ;
      }
    }
  }
  public void init(String repository) throws Exception {
    Iterator<ObjectParameter> it = params_.getObjectParamIterator() ;       
    String queryPath = cmsConfigService_.getJcrPath(BasePath.QUERIES_PATH);
     while(it.hasNext()){
      QueryData data = (QueryData)it.next().getObject() ;
      if(data.getAutoCreatedInNewRepository() || repository.equals(data.getRepository())) {
        addQuery(getSession(repository), data, queryPath) ;
      }
    }
  }
  
  private Session getSession(String repository) throws Exception {
    return repositoryService_.getRepository(repository)
    .getSystemSession(cmsConfigService_.getWorkspace(repository)) ;
  }
  
  private void addQuery(Session session, QueryData data, String queryPath) throws Exception {
    Node queryHome = (Node)session.getItem(queryPath) ;
    if(!queryHome.hasNode(data.getName())){
      ValueFactory vt = session.getValueFactory() ;
      Node queryNode = queryHome.addNode(data.getName(), "nt:query");
      queryNode.addMixin("mix:sharedQuery") ;
      queryNode.setProperty(STATEMENT, data.getStatement()) ;
      queryNode.setProperty(LANGUAGE, data.getLanguage()) ;
      Value vl = vt.createValue(data.getPermissions()) ;
      Value[] vls = {vl} ;
      queryNode.setProperty(PERMISSIONS, vls) ;
      queryNode.setProperty(CACHED_RESULT, data.getCacheResult()) ;
      queryHome.save() ;
      session.save() ;
    }
  }
}
