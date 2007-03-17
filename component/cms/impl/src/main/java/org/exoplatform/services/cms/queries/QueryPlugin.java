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
  
  public QueryPlugin(RepositoryService repositoryService, 
      InitParams params, CmsConfigurationService cmsConfigService) throws Exception {
    Session session = repositoryService.getRepository().getSystemSession(cmsConfigService.getWorkspace()) ; 
    Iterator<ObjectParameter> it = params.getObjectParamIterator() ;       
    String queryPath = cmsConfigService.getJcrPath(BasePath.QUERIES_PATH);
    Node queryHome = (Node)session.getItem(queryPath) ;
    ValueFactory vt = repositoryService.getRepository()
    .getSystemSession(cmsConfigService.getWorkspace()).getValueFactory() ;
     while(it.hasNext()){
      QueryData data = (QueryData)it.next().getObject() ;
      if(!queryHome.hasNode(data.getName())){
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
    }
    session.save() ;
  }  
}
