package org.exoplatform.services.cms.queries.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

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
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.picocontainer.Startable;

public class QueryServiceImpl implements QueryService, Startable{
  private static final String[] perms = {PermissionType.READ, PermissionType.ADD_NODE, 
    PermissionType.SET_PROPERTY, PermissionType.REMOVE };
  private String relativePath_;  
  private List<QueryPlugin> queryPlugins_ = new ArrayList<QueryPlugin> ();
  private RepositoryService repositoryService_;
  private CacheService cacheService_ ;  
  private PortalContainerInfo containerInfo_ ;
  private OrganizationService organizationService_ ;
  private String baseUserPath_ ;
  private String baseQueriesPath_ ;

  public QueryServiceImpl(RepositoryService repositoryService, CmsConfigurationService cmsConf, 
      InitParams params, PortalContainerInfo containerInfo, CacheService cacheService,OrganizationService organizationService) throws Exception {
    relativePath_ = params.getValueParam("relativePath").getValue();    
    repositoryService_ = repositoryService;
    containerInfo_ = containerInfo ;
    cacheService_ = cacheService ;
    organizationService_ = organizationService ;
    baseUserPath_ = cmsConf.getJcrPath(BasePath.CMS_USERS_PATH);
    baseQueriesPath_ = cmsConf.getJcrPath(BasePath.QUERIES_PATH) ;
  }

  public void start() {
    for(QueryPlugin queryPlugin : queryPlugins_){
      try{
        queryPlugin.init(baseQueriesPath_) ;
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
        queryPlugin.init(repository,baseQueriesPath_) ;
      }catch (Exception e) { 
        System.out.println("[WARNING] ==> Can not init query plugin '" + queryPlugin.getName() + "'");
        //e.printStackTrace() ;
      }
    } 
  }

  public void setQueryPlugin(QueryPlugin queryPlugin) {
    queryPlugins_.add(queryPlugin) ;
  }

  public String getRelativePath() { return relativePath_; }

  public List<Query> getQueries(String userName, String repository,SessionProvider provider) throws Exception {
    List<Query> queries = new ArrayList<Query>();        
    if(userName == null) return queries;    
    Session session = getSession(repository,provider) ;
    QueryManager manager = session.getWorkspace().getQueryManager();           
    Node usersHome = (Node) session.getItem(baseUserPath_);
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

  private Map<String,String[]> getPermissions(String owner) {
    Map<String, String[]> permissions = new HashMap<String, String[]>();
    permissions.put(owner, perms);         
    permissions.put("*:/admin", perms);
    return permissions;
  } 

  public void addQuery(String queryName, String statement, String language, 
      String userName, String repository) throws Exception {
    if(userName == null) return;
    Session session = getSession(repository) ;
    QueryManager manager = session.getWorkspace().getQueryManager();    
    Query query = manager.createQuery(statement, language);    
    String absPath = baseUserPath_ + "/" + userName + "/" + relativePath_ + "/" + queryName;
    query.storeAsNode(absPath);
    session.getItem(baseUserPath_).save();
    session.save();
    session.logout();
  }

  public void removeQuery(String queryPath, String userName, String repository) throws Exception {
    if(userName == null) return;    
    Session session = getSession(repository) ;
    Node queryNode = (Node) session.getItem(queryPath);
    Node queriesHome = queryNode.getParent() ;
    queryNode.remove() ;
    queriesHome.save() ;
    session.save();
    session.logout();
    removeFromCache(queryPath) ;
  }

  public void addSharedQuery(String queryName, String statement, String language, 
      String[] permissions, boolean cachedResult, String repository) throws Exception {
    Session session = getSession(repository);
    ValueFactory vt = session.getValueFactory();
    String queryPath ;
    List<Value> perm = new ArrayList<Value>() ;                 
    for(String permission : permissions) {
      Value vl = vt.createValue(permission) ;
      perm.add(vl) ;
    }
    Value[] vls = perm.toArray(new Value[] {}) ;

    String queriesPath = baseQueriesPath_ ;
    Node queryHome = (Node)session.getItem(baseQueriesPath_) ;
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
      Node newQuery = query.storeAsNode(baseQueriesPath_ + "/" + queryName);
      newQuery.addMixin("mix:sharedQuery") ;
      newQuery.setProperty("exo:permissions", vls) ;
      newQuery.setProperty("exo:cachedResult", cachedResult) ;
      session.getItem(queriesPath).save();
      queryPath = queriesPath ;
    }
    session.logout();
    removeFromCache(queryPath) ;
  }

  public Node getSharedQuery(String queryName, String repository,SessionProvider provider) throws Exception {
    Session session = getSession(repository,provider) ;    
    return (Node)session.getItem(baseQueriesPath_ + "/" + queryName);
  }
  
  public List<Node> getSharedQueries(String repository,SessionProvider provider) throws Exception {
    Session session = getSession(repository,provider);
    List<Node> queries = new ArrayList<Node>() ;
    Node sharedQueryHome = (Node) session.getItem(baseQueriesPath_);
    NodeIterator iter = sharedQueryHome.getNodes();
    while (iter.hasNext()) {
      Node node = iter.nextNode();
      if("nt:query".equals(node.getPrimaryNodeType().getName()))
        queries.add(node);
    }
    return queries ;
  }
  
  public List<Node> getSharedQueries(String userId, String repository, SessionProvider provider) throws Exception {
    List<Node> sharedQueries = new ArrayList<Node>();
    for(Node query:getSharedQueries(repository,provider)) {
      if(canUseQuery(userId,query)) {
        sharedQueries.add(query) ;
      }
    }
    return sharedQueries;
  }  
  public List<Node> getSharedQueries(String queryType, String userId, String repository, SessionProvider provider) throws Exception {
    List<Node> resultList = new ArrayList<Node>() ;
    String language = null ;    
    for(Node queryNode: getSharedQueries(repository,provider)) {
      language = queryNode.getProperty("jcr:language").getString() ;
      if(!queryType.equalsIgnoreCase(language)) continue ;
      if(canUseQuery(userId,queryNode)) {
        resultList.add(queryNode) ;
      }
    }
    return resultList;
  }


  public Query getQueryByPath(String queryPath, String userName, String repository,SessionProvider provider) throws Exception {
    List<Query> queries = getQueries(userName, repository,provider) ;
    for(Query query : queries) {
      if(query.getStoredQueryPath().equals(queryPath)) return query ;
    }
    return null ;
  }

  public void removeSharedQuery(String queryName, String repository) throws Exception {
    Session session = getSession(repository) ;    
    session.getItem(baseQueriesPath_ + "/" + queryName).remove();
    session.save() ;
    session.logout();
  }
  
  public QueryResult execute(String queryPath, String workspace, String repository,SessionProvider provider) throws Exception {
    Session session = getSession(repository,provider);    
    Node queryNode = (Node)session.getItem(queryPath) ;    
    if(queryNode.hasProperty("exo:cachedResult")){
      if(queryNode.getProperty("exo:cachedResult").getBoolean()) {
        ExoCache queryCache = cacheService_.getCacheInstance(QueryServiceImpl.class.getName()) ;
        String portalName = containerInfo_.getContainerName() ;
        String key = portalName + queryPath ;
        QueryResult result = (QueryResult)queryCache.get(key) ;
        if (result != null) return result ;
        Session querySession = getSession(repository,workspace,provider) ;
        Query query = querySession.getWorkspace().getQueryManager().getQuery(queryNode) ;
        result = query.execute() ;
        queryCache.put(key, result) ;
        return result ;      
      }
    }
    Session querySession = getSession(repository,workspace,provider) ;
    Query query = querySession.getWorkspace().getQueryManager().getQuery(queryNode) ;
    QueryResult result = query.execute();    
    return result;
  }

  private void removeFromCache(String queryPath) throws Exception {
    ExoCache queryCache = cacheService_.getCacheInstance(QueryServiceImpl.class.getName()) ;
    String portalName = containerInfo_.getContainerName() ;
    String key = portalName + queryPath ;
    QueryResult result = (QueryResult)queryCache.get(key) ;
    if (result != null) queryCache.remove(key) ;
  }

  //TODO need to use SystemProvider
  private Session getSession(String repository) throws Exception {
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository) ;
    String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName() ;
    return manageableRepository.getSystemSession(workspace);    
  }

  private Session getSession(String repository,SessionProvider provider) throws Exception {
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository) ;
    String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName() ;
    return provider.getSession(workspace,manageableRepository) ;
  }

  private Session getSession(String repository,String workspace,SessionProvider provider) throws Exception {
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository) ;
    return provider.getSession(workspace,manageableRepository) ;
  }
  
  private boolean canUseQuery(String userId,Node queryNode) throws Exception{    
    Value[] values = queryNode.getProperty("exo:permissions").getValues() ;
    for(Value value : values) {
      String accessPermission = value.getString() ;
      if(hasMembership(userId,accessPermission)) {
        return true ;
      }
    }
    return false ;
  }

  //TODO :this method will be removed when MembershipHandler support
  private boolean hasMembership(String userId, String roleExpression) {
    if("*".equals(roleExpression))
      return true;
    String membershipType = roleExpression.substring(0, roleExpression.indexOf(":"));
    String groupName = roleExpression.substring(roleExpression.indexOf(":") + 1);
    try {
      MembershipHandler membershipHandler = organizationService_.getMembershipHandler() ;
      if ("*".equals(membershipType)) {
        // Determine if there exists at least one membership
        return !membershipHandler.findMembershipsByUserAndGroup( userId,groupName).isEmpty();
      } 
      // Determine if there exists the membership of specified type
      return membershipHandler.findMembershipByUserGroupAndType(userId,groupName,membershipType) != null;      
    }
    catch(Exception e) {            
    }  
    return false ;
  }   
}
