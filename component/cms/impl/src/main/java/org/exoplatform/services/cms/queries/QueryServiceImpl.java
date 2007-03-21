package org.exoplatform.services.cms.queries;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.jcr.RepositoryService;

public class QueryServiceImpl implements QueryService{
  
  private String relativePath_;
  private CmsConfigurationService cmsConfig_;
  private String workspace_;
  private QueryPlugin queryPlugin_ ;
  private RepositoryService repositoryService_;
  private CacheService cacheService_ ;  
  private PortalContainerInfo containerInfo_ ;

  public QueryServiceImpl(RepositoryService repositoryService, CmsConfigurationService cmsConf, 
      InitParams params, PortalContainerInfo containerInfo, CacheService cacheService) throws Exception {
    relativePath_ = params.getValueParam("relativePath").getValue();
    workspace_ = params.getValueParam("workspace").getValue();
    cmsConfig_ = cmsConf;
    repositoryService_ = repositoryService;
    containerInfo_ = containerInfo ;
    cacheService_ = cacheService ;
  }
  
  public void setQueryPlugin(QueryPlugin queryPlugin) {
    queryPlugin_ = queryPlugin ;
  }
  
  public String getRelativePath() {
    return relativePath_;
  }
  
  public List<Query> getQueries(String userName) throws Exception {
    List<Query> queries = new ArrayList<Query>();        
    if(userName == null) return queries;    
    Session session = null;
    try {
      session = repositoryService_.getRepository().getSystemSession(workspace_);
    } catch (RepositoryException re) {
      return queries;
    }    
    QueryManager manager = session.getWorkspace().getQueryManager();
    
    Node usersHome = (Node) session.getItem(cmsConfig_.getJcrPath(BasePath.CMS_USERS_PATH));
    Node userHome = usersHome.getNode(userName);
    Node queriesHome = userHome.getNode(relativePath_); 
    
    NodeIterator iter = queriesHome.getNodes();
    while (iter.hasNext()) {
      Node node = iter.nextNode();
      if("nt:query".equals(node.getPrimaryNodeType().getName()))
        queries.add(manager.getQuery(node));
    }
    return queries;
  }

  public Query getQuery(String queryPath) throws Exception {
    Session session = null;
    try {
      session = repositoryService_.getRepository().getSystemSession(workspace_);
    } catch (RepositoryException re) {
      return null;
    }    
    QueryManager manager = session.getWorkspace().getQueryManager();
    Node queryNode = (Node) session.getItem(queryPath);
    return manager.getQuery(queryNode);
  }

  public void addQuery(String queryName, String statement, String language, String userName) throws Exception {
    if(userName == null) return;
    Session session = null;
    try {
      session = repositoryService_.getRepository().getSystemSession(workspace_);
    } catch (RepositoryException re) {
      return;
    }    
    QueryManager manager = session.getWorkspace().getQueryManager();    
    Query query = manager.createQuery(statement, language);
    String usersHome = cmsConfig_.getJcrPath(BasePath.CMS_USERS_PATH);
    String absPath = usersHome + "/" + userName + "/" + relativePath_ + "/" + queryName;
    query.storeAsNode(absPath);
    session.getItem(usersHome).save();  
  }

  public void removeQuery(String queryPath, String userName) throws Exception {
    if(userName == null) return;    
    Session session = null;
    try {
      session = repositoryService_.getRepository().getSystemSession(workspace_);
    } catch (RepositoryException re) {
      return;
    }    
    Node queryNode = (Node) session.getItem(queryPath);
    Node queriesHome = queryNode.getParent() ;
    queryNode.remove() ;
    queriesHome.save() ;
    removeFromCache(queryPath) ;
  }
  
  public void addSharedQuery(String queryName, String statement, String language, 
                             String[] permissions, boolean cachedResult) throws Exception {
    Session session = null;
    ValueFactory vt ;
    String queryPath ;
    List<Value> perm = new ArrayList<Value>() ;
    
    try {
      session = repositoryService_.getRepository().getSystemSession(workspace_);
      vt = repositoryService_.getRepository().getSystemSession(workspace_).getValueFactory() ;
    } catch (RepositoryException re) {
      return;
    } 
    for(String permission : permissions) {
    Value vl = vt.createValue(permission) ;
      perm.add(vl) ;
    }
    Value[] vls = perm.toArray(new Value[] {}) ;
    
    String queriesPath = cmsConfig_.getJcrPath(BasePath.QUERIES_PATH);
    Node queryHome = (Node)session.getItem(queriesPath) ;
    if(queryHome.hasNode(queryName)) {
      Node query = queryHome.getNode(queryName) ;
      query.setProperty("jcr:language", language) ;
      query.setProperty("jcr:statement", statement) ;
      query.setProperty("exo:permissions", vls) ;
      query.setProperty("exo:cachedResult", cachedResult) ;
      query.save() ;
      session.save() ;
      queryPath = query.getPath() ;
    }else {
      QueryManager manager = session.getWorkspace().getQueryManager();    
      Query query = manager.createQuery(statement, language);      
      Node newQuery = query.storeAsNode(queriesPath + "/" + queryName);
      newQuery.addMixin("mix:sharedQuery") ;
      newQuery.setProperty("exo:permissions", vls) ;
      newQuery.setProperty("exo:cachedResult", cachedResult) ;
      session.getItem(queriesPath).save();
      queryPath = queriesPath ;
    }
    removeFromCache(queryPath) ;
  }

  public Node getSharedQuery(String queryName) throws Exception {
    Session session = null;
    try {
      session = repositoryService_.getRepository().getSystemSession(workspace_);
    } catch (RepositoryException re) {
      return null;
    }
    String queriesPath = cmsConfig_.getJcrPath(BasePath.QUERIES_PATH);
    return (Node)session.getItem(queriesPath + "/" + queryName);
  }
  
  public List<Node> getSharedQueries(String queryType, List permissions) throws Exception {
    Session session = null;
    List<Node> queries = new ArrayList<Node>() ;    
    try {
      session = repositoryService_.getRepository().getSystemSession(workspace_);
    } catch (RepositoryException re) {
      return null;
    }
    String queriesPath = cmsConfig_.getJcrPath(BasePath.QUERIES_PATH);
    Node queriesHome = (Node)session.getItem(queriesPath) ;
    NodeIterator iter = queriesHome.getNodes() ;
    while (iter.hasNext()) {
      Node query = iter.nextNode() ;
      if(query.getProperty("jcr:language").getString().equals(queryType)){
        List applyPermissions = getPermissions(query) ;
        for(int i = 0; i < permissions.size(); i++) {
          if(hasPermission(permissions.get(i).toString(), applyPermissions)) {
            queries.add(query) ;
            break ;
          }          
        }
      }
    }
    return queries;
  }
  
  public List<Node> getSharedQueries() throws Exception {
    Session session = null;
    List<Node> queries = new ArrayList<Node>() ;
    try {
      session = repositoryService_.getRepository().getSystemSession(workspace_);
    } catch (RepositoryException re) {
      return queries;
    }    
    Node sharedQueryHome = (Node) session.getItem(cmsConfig_.getJcrPath(BasePath.QUERIES_PATH));
    NodeIterator iter = sharedQueryHome.getNodes();
    while (iter.hasNext()) {
      Node node = iter.nextNode();
      if("nt:query".equals(node.getPrimaryNodeType().getName()))
        queries.add(node);
    }
    return queries ;
  }
  
  public void removeSharedQuery(String queryName) throws Exception {
    Session session = null;
    try {
      session = repositoryService_.getRepository().getSystemSession(workspace_);
    } catch (RepositoryException re) {
      return ;
    }
    String queriesPath = cmsConfig_.getJcrPath(BasePath.QUERIES_PATH);
    session.getItem(queriesPath + "/" + queryName).remove();
    session.save() ;
  }
  
  public List<Node> getSharedQueriesByPermissions(List permissions) throws Exception {
    List<Node> queries = getSharedQueries() ;
    List<Node> result = new ArrayList<Node>() ;
    for(Node query : queries) {
      List applyPermissions = getPermissions(query) ;
      for(int i = 0; i < permissions.size(); i++) {
        if(hasPermission(permissions.get(i).toString(), applyPermissions)) {
          result.add(query) ;
          break ;
        }          
      }
    }
    return result ;
  }
  
  private List getPermissions(Node query) throws Exception {
    List permissions = new ArrayList() ;
    Value[] values = query.getProperty("exo:permissions").getValues() ;
    for(Value value : values) {
      permissions.add(value.getString()) ;
    }
    return permissions ;
  }
  
  private boolean hasPermission(String permission, List permissions) {
    if(permission == null) return false ;
    if(permission.indexOf(":/") > -1) {
      String[] array = StringUtils.split(permission , ":/") ;
      if(array == null || array.length < 2) return false ;
      if( permissions.indexOf("*:/"+array[1]) > -1) return true ;	
    }    
    return permissions.contains(permission) ;
  }

  public QueryResult execute(String queryPath) throws Exception {
    Session session = null;
    try {
      session = repositoryService_.getRepository().getSystemSession(workspace_);
    } catch (RepositoryException re) {
      return null;
    }
    Node queryNode = (Node)session.getItem(queryPath) ;
    if(queryNode.hasProperty("exo:cachedResult")){
      if(queryNode.getProperty("exo:cachedResult").getBoolean()) {
        ExoCache queryCache = cacheService_.getCacheInstance(QueryServiceImpl.class.getName()) ;
        String portalName = containerInfo_.getContainerName() ;
        String key = portalName + queryPath ;
        QueryResult result = (QueryResult)queryCache.get(key) ;
        if (result != null) 
          return result ; 
        Query query = getQuery(queryPath) ;
        result = query.execute() ;
        queryCache.put(key, result) ;
        return result ;      
      }
    }
    Query query = getQuery(queryPath) ;
    return query.execute() ;
  }
  
  private void removeFromCache(String queryPath) throws Exception {
    ExoCache queryCache = cacheService_.getCacheInstance(QueryServiceImpl.class.getName()) ;
    String portalName = containerInfo_.getContainerName() ;
    String key = portalName + queryPath ;
    QueryResult result = (QueryResult)queryCache.get(key) ;
    if (result != null) queryCache.remove(key) ;
  }
}
