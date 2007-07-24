/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

import java.util.Map;

import javax.jcr.Session;
import javax.jcr.Node;

import org.exoplatform.services.cms.scripts.CmsScript;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * May 29, 2007 10:01:09 AM
 */
public class AddMetadataScript implements CmsScript {
  
  private RepositoryService repositoryService_ ;
  
  public AddMetadataScript(RepositoryService repositoryService) {
    repositoryService_ = repositoryService ;
  }
  
  public void execute(Object context) {
    Map variables = (Map) context;   
    String metadataName = (String)context.get("exo:mixinMetadata") ;
    String srcWorkspace = (String)context.get("srcWorkspace") ;
    String nodePath = (String)context.get("nodePath") ;
    Session session = null ;
    try {
      session = repositoryService_.getRepository().login(srcWorkspace);
      Node node = (Node) session.getItem(nodePath);
      if(node.canAddMixin(metadataName)) {
        node.addMixin(metadataName) ;
        node.save() ;
        session.save() ;
        session.logout();
      } else {
        System.out.println("\n\nCan not add mixin\n\n");
        session.logout();
      }
    } catch(Exception e) {
      if(session != null) {
        session.logout();        
      }
      e.printStackTrace() ;
    }
  }

  public void setParams(String[] params) {}

}