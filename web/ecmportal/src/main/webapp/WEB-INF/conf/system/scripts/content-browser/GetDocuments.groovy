/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

import org.exoplatform.services.cms.scripts.CmsScript ;
import org.exoplatform.services.cms.scripts.DataTransfer ;
import javax.jcr.Node;
import javax.jcr.NodeIterator ;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import java.util.ArrayList;
import java.util.List;

public class GetDocuments implements CmsScript {
  
  public GetDocuments() {}
  
  public void execute(Object context){    
    DataTransfer data = (DataTransfer) context ;
    try{
      Session session = data.getSession() ; 
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
    } catch (Exception e) {
      //e.printStackTrace() ;
    }
    
  }
  
  public void setParams(String[] params) {}

}
