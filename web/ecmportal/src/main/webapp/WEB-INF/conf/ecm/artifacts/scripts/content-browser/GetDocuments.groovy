/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

import org.exoplatform.services.cms.scripts.CmsScript ;
import org.exoplatform.services.cms.scripts.DataTransfer ;
import org.exoplatform.services.jcr.RepositoryService;

import javax.jcr.Node;
import javax.jcr.NodeIterator ;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import java.util.ArrayList;
import java.util.List;

public class GetDocuments implements CmsScript {
  
  private RepositoryService repositoryService_ ;
  
  public GetDocuments(RepositoryService repositoryService) {
    repositoryService_ = repositoryService ;
  }
  
  public void execute(Object context){    
    DataTransfer data = (DataTransfer) context ;
    Session session = null ;
    try{      
      String repository = data.getRepository();
      String worksapce = data.getWorkspace() ;
      session = repositoryService_.getRepository(repository).login(workspace) ;
      QueryManager queryManager = session.getWorkspace().getQueryManager();     
      Query query = queryManager.createQuery("/jcr:root//element(*, exo:article)", Query.XPATH); 
      QueryResult queryResult = query.execute();      
      NodeIterator iter = queryResult.getNodes() ;
      List nodeList = new ArrayList() ;
      while(iter.hasNext()) {
        Node node = iter.nextNode() ;
        nodeList.add(node) ;
      }
      data.setContentList(nodeList) ;      
      session.logout();
    } catch (Exception e) {
      //e.printStackTrace() ;
    }
    
  }
  
  public void setParams(String[] params) {}

}
