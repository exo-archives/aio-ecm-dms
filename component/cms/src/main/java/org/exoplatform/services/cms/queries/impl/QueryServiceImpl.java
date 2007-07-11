package org.exoplatform.services.cms.queries.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
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
import org.exoplatform.services.cms.queries.QueryService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.picocontainer.Startable;

public class QueryServiceImpl implements QueryService, Startable{
  private static final String[] perms = {PermissionType.READ, PermissionType.ADD_NODE, 
    PermissionType.SET_PROPERTY, PermissionType.REMOVE };
  private String relativePath_;
  private CmsConfigurationService cmsConfig_;
  private List<QueryPlugin> queryPlugins_ = new ArrayList<QueryPlugin> ();
  private RepositoryService repositoryService_;
  private CacheService cacheService_ ;  
  private PortalContainerInfo containerInfo_ ;

  public QueryServiceImpl(RepositoryService repositoryService, CmsConfigurationService cmsConf, 
      InitParams params, PortalContainerInfo containerInfo, CacheService cacheService) throws Exception {
    relativePath_ = params.getValueParam("relativePath").getValue();
    cmsConfig_ = cmsConf;
    repositoryService_ = repositoryService;
    containerInfo_ = containerInfo ;
    cacheService_ = cacheService ;
  }
  
  public void start() {
    for(QueryPlugin queryPlugin : queryPlugins_){
      try{
        queryPlugin.init() ;
      }catch (Exception e) {
        System.out.println("[WARNING] ==> Can not init query plugin '" + queryPlugin.getName() + "'");
        //e.printStackTrace() ;
      }
    }
  }

  public void stop() {
    // TODO Auto-generated method stub    
  }
  
  public void init(String repository) throws Exception {
    for(QueryPlugin queryPlugin : queryPlugins_){
      try{
        queryPlugin.init(repository) ;
      }catch (Exception e) { 
        System.out.println("[WARNING] ==> Can not init query plugin '" + queryPlugin.getName() + "'");
        //e.printStackTrace() ;
      }
    } 
  }
  
  public void setQueryPlugin(QueryPlugin queryPlugin) {
    queryPlugins_.add(queryPlugin) ;
  }
  
  public String getRelativePath() {
    return relativePath_;
  }
  
  public List<Query> getQueries(String userName, String repository) throws Exception {
    List<Query> queries = new ArrayList<Query>();        
    if(userName == null) return queries;    
    Session session = getSession(repository) ;    
    QueryManager manager = session.getWorkspace().getQueryManager();    
    Node usersHome = (Node) session.getItem(cmsConfig_.getJcrPath(BasePath.CMS_USERS_PATH));
    Node userHome = null ;
    if(usersHome.hasNode(userName)) {
      userHome = usersHome.getNode(userName);
    }else{
      userHome = usersHome.addNode(userName);
      if (userHome.canAddMixin("exo:privilegeable")){
        userHome.addMixin("exo:privilegeable");
      }
      ((ExtendedNode)userHome).setPermissions(getPermissions(userName));
      Node query = userHome.addNode(relativePath_) ;
      if (query.canAddMixin("exo:privilegeable")){
        query.addMixin("exo:privilegeable");
      }
      ((ExtendedNode)query).setPermissions(getPermissions(userName));
      usersHome.save() ;
    }
    Node queriesHome = userHome.getNode(relativePath_);
    NodeIterator iter = queriesHome.getNodes();
    while (iter.hasNext()) {
      Node node = iter.nextNode();
      if("nt:query".equals(node.getPrimaryNodeType().getName()))
        queries.add(manager.getQuery(node));
    }
    return queries;
  }
  
  private Map getPermissions(String owner) {
    Map<String, String[]> permissions = new HashMap<String, String[]>();
    permissions.put(owner, perms);     
    permissions.put("any", new String[] {PermissionType.READ});
    permissions.put("*:/admin", perms);
    return permissions;
  } 
  
  public void addQuery(String queryName, String statement, String language, 
                       String userName, String repository) throws Exception {
    if(userName == null) return;
    Session session = getSession(repository) ;
    QueryManager manager = session.getWorkspace().getQueryManager();    
    Query query = manager.createQuery(statement, language);
    String usersHome = cmsConfig_.getJcrPath(BasePath.CMS_USERS_PATH);
    String absPath = usersHome + "/" + userName + "/" + relativePath_ + "/" + queryName;
    query.storeAsNode(absPath);
    session.getItem(usersHome).save();  
  }

  public void removeQuery(String queryPath, String userName, String repository) throws Exception {
    if(userName == null) return;    
    Session session = getSession(repository) ;
    Node queryNode = (Node) session.getItem(queryPath);
    Node queriesHome = queryNode.getParent() ;
    queryNode.remove() ;
    queriesHome.save() ;
    removeFromCache(queryPath) ;
  }
  
  public void addSharedQuery(String queryName, String statement, String language, 
                             String[] permissions, boolean cachedResult, String repository) throws Exception {
    Session session = getSession(repository);
    ValueFactory vt ;
    String queryPath ;
    List<Value> perm = new ArrayList<Value>() ;
    try {      
      vt = repositoryService_.getRepository(repository)
                             .getSystemSession(cmsConfig_.getWorkspace(repository)).getValueFactory() ;
    } catch (RepositoryException re) {
      re.printStackTrace() ;
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

  public Node getSharedQuery(String queryName, String repository) throws Exception {
    Session session = getSession(repository) ;
    String queriesPath = cmsConfig_.getJcrPath(BasePath.QUERIES_PATH);
    return (Node)session.getItem(queriesPath + "/" + queryName);
  }
  
  public List<Node> getSharedQueries(String queryType, List permissions, String repository) throws Exception {
    Session session = getSession(repository);
    List<Node> queries = new ArrayList<Node>() ;    
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
  
  public List<Node> getSharedQueries(String repository) throws Exception {
    Session session = getSession(repository);
    List<Node> queries = new ArrayList<Node>() ;
    Node sharedQueryHome = (Node) session.getItem(cmsConfig_.getJcrPath(BasePath.QUERIES_PATH));
    NodeIterator iter = sharedQueryHome.getNodes();
    while (iter.hasNext()) {
      Node node = iter.nextNode();
      if("nt:query".equals(node.getPrimaryNodeType().getName()))
        queries.add(node);
    }
    return queries ;
  }
  
  public Query getQueryByPath(String queryPath, String userName, String repository) throws Exception {
    List<Query> queries = getQueries(userName, repository) ;
    for(Query query : queries) {
      if(query.getStoredQueryPath().equals(queryPath)) return query ;
    }
    return null ;
  }
  
  public void removeSharedQuery(String queryName, String repository) throws Exception {
    Session session = getSession(repository) ;
    String queriesPath = cmsConfig_.getJcrPath(BasePath.QUERIES_PATH);
    session.getItem(queriesPath + "/" + queryName).remove();
    session.save() ;
  }
  
  public List<Node> getSharedQueriesByPermissions(List permissions, String repository) throws Exception {
    List<Node> queries = getSharedQueries(repository) ;
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


  public QueryResult execute(String queryPath, String workspace, String repository) throws Exception {
    Session session = null;
    try {
      session = repositoryService_.getRepository(repository).getSystemSession(workspace);
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
        if (result != null) return result ; 
        Query query = getQuery(queryNode, workspace, repository) ;
        result = query.execute() ;
        queryCache.put(key, result) ;
        return result ;      
      }
    }
    Query query = getQuery(queryNode, workspace, repository) ;
    return query.execute() ;
  }
  
  private Query getQuery(Node query, String workspace, String repository) throws Exception {
    Session session = repositoryService_.getRepository(repository).getSystemSession(workspace);
    QueryManager manager = session.getWorkspace().getQueryManager();
    return manager.getQuery(query);
  }
  private void removeFromCache(String queryPath) throws Exception {
    ExoCache queryCache = cacheService_.getCacheInstance(QueryServiceImpl.class.getName()) ;
    String portalName = containerInfo_.getContainerName() ;
    String key = portalName + queryPath ;
    QueryResult result = (QueryResult)queryCache.get(key) ;
    if (result != null) queryCache.remove(key) ;
  }
  
  private Session getSession(String repository) {
    try {
      return repositoryService_.getRepository(repository).getSystemSession(cmsConfig_.getWorkspace(repository));
    } catch (Exception re) {
      return null;
    }
  }  
}
