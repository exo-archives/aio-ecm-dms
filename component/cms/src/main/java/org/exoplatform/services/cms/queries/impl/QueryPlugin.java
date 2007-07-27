package org.exoplatform.services.cms.queries.impl;

import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;

public class QueryPlugin extends BaseComponentPlugin {

  private static String STATEMENT = "jcr:statement".intern() ;
  private static String LANGUAGE = "jcr:language".intern() ;
  private static String PERMISSIONS = "exo:permissions".intern() ;
  private static String CACHED_RESULT = "exo:cachedResult".intern() ;

  private InitParams params_ ;
  private boolean autoCreateInNewRepository_ = false;  
  private String repository_ ;  
  private RepositoryService repositoryService_ ;

  public QueryPlugin(RepositoryService repositoryService, InitParams params) throws Exception {
    params_ = params ;    
    repositoryService_ = repositoryService ;
    ValueParam autoInitParam = params.getValueParam("autoCreateInNewRepository") ;    
    if(autoInitParam !=null) {
      autoCreateInNewRepository_ = Boolean.parseBoolean(autoInitParam.getValue()) ;
    }
    ValueParam param = params.getValueParam("repository") ;
    if(param !=null) {
      repository_ = param.getValue();
    }        
  } 

  public void init(String basedQueriesPath) throws Exception {
    Iterator<ObjectParameter> it = params_.getObjectParamIterator() ; 
    Session session = null ;
    if(autoCreateInNewRepository_) {      
      for(RepositoryEntry entry:repositoryService_.getConfig().getRepositoryConfigurations()) {
        session = getSession(entry.getName()) ;
        Node queryHomeNode = (Node)session.getItem(basedQueriesPath);
        while(it.hasNext()) {
          QueryData data = (QueryData)it.next().getObject() ;
          addQuery(queryHomeNode,data) ;
        }
        queryHomeNode.save();
        session.save();
        session.logout();
      }
    } else {
      session = getSession(repository_) ;
      Node queryHomeNode = (Node)session.getItem(basedQueriesPath);
      while(it.hasNext()) {
        QueryData data = (QueryData)it.next().getObject() ;
        addQuery(queryHomeNode,data) ;
      }
      queryHomeNode.save();
      session.save();
      session.logout();      
    }   
  }
  
  public void init(String repository,String baseQueriesPath) throws Exception {          
    if(!autoCreateInNewRepository_) return ; 
    Iterator<ObjectParameter> it = params_.getObjectParamIterator() ;
    Session session = getSession(repository) ;
    Node queryHomeNode = (Node)session.getItem(baseQueriesPath) ;
    while(it.hasNext()){
      QueryData data = (QueryData)it.next().getObject() ;      
        addQuery(queryHomeNode, data) ;
      }
    queryHomeNode.save();
    session.save();
    session.logout();    
  }
  
  private Session getSession(String repository) throws Exception {
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository) ;
    String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName() ;
    return manageableRepository.getSystemSession(workspace) ;    
  }

  private void addQuery(Node queryHome, QueryData data) throws Exception {
    if(queryHome.hasNode(data.getName())) return ;    
    ValueFactory vt = queryHome.getSession().getValueFactory() ;
    Node queryNode = queryHome.addNode(data.getName(), "nt:query");
    queryNode.addMixin("mix:sharedQuery") ;
    queryNode.setProperty(STATEMENT, data.getStatement()) ;
    queryNode.setProperty(LANGUAGE, data.getLanguage()) ;
    Value vl = vt.createValue(data.getPermissions()) ;
    Value[] vls = {vl} ;
    queryNode.setProperty(PERMISSIONS, vls) ;
    queryNode.setProperty(CACHED_RESULT, data.getCacheResult()) ;        
  }
}
