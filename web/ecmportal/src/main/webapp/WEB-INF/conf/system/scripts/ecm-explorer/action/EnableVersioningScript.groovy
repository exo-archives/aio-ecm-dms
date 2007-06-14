/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.cms.scripts.CmsScript;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Xuan Hoa
 *          hoa.pham@exoplatform.com
 * Jan 9, 2007  
 */
public class EnableVersioningScript implements CmsScript {
  
  private RepositoryService repositoryService_;    
  
  public EnableVersioningScript(RepositoryService repositoryService) {
    repositoryService_ = repositoryService;    
  }
  
  public void execute(Object context) {
    Map variables = (Map) context;
    String nodePath = (String)variables.get("nodePath") ;
    String workspace = (String)variables.get("srcWorkspace") ;
    try {
      Session session = repositoryService_.getRepository().getSystemSession(workspace) ;
      Node addedNode = (Node) session.getItem(nodePath);
      if(addedNode.canAddMixin("mix:versionable")) {
        addedNode.addMixin("mix:versionable") ;
        addedNode.save() ;
        session.save() ;
      } 
    } catch (Exception e) {     
      e.printStackTrace() ;
    }    
  }

  public void setParams(String[] params) {}

}