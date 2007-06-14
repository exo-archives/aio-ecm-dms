/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

import java.util.Map;
import org.exoplatform.services.cms.scripts.CmsScript;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Xuan Hoa
 *          hoa.pham@exoplatform.com
 * Jan 9, 2007  
 */
public class AutoVersioningScript implements CmsScript{

  private RepositoryService repositoryService_;  
  
  public AutoVersioningScript(RepositoryService repositoryService) {
    repositoryService_ = repositoryService;    
  }
  
  public void execute(Object context) {
    Map variables = (Map) context;
    String nodePath = (String)variables.get("nodePath") ;
    String workspace = (String)variables.get("srcWorkspace") ;
    String srcPath = (String)variables.get("srcPath") ;
    String actionName = (String)variables.get("actionName") ;
    try{
      Session session = repositoryService_.getRepository().getSystemSession(workspace) ;
      Node srcNode = (Node)session.getItem(srcPath) ;
      Node actionNode = srcNode.getNode(actionName) ;      
      String lifecycle = actionNode.getProperty("exo:lifecyclePhase").getString() ;      
       // if action's lifecycle is: modify, auto versioning document was versionabled that modify       
      if("modify".equals(lifecycle)) {
        Property changedProp = session.getItem(nodePath) ;
        Node modifiedNode = changedProp.getParent() ;
        if(!modifiedNode.isNodeType("mix:versionable")) return ;
        if(!modifiedNode.isCheckedOut()) modifiedNode.checkout() ;
        modifiedNode.checkin() ;
        modifiedNode.checkout() ;
        session.save();
        session.refresh(true) ;        
      }
      // with others lifecycle, auto versioning node that contain action's node.
      else if("add".equals(lifecycle)||"remove".equals(lifecycle)||
              "schedule".equals(lifecycle)) {
        if(!srcNode.isNodeType("mix:versionable")) {
          if(srcNode.canAddMixin("mix:versionable")) {
            srcNode.addMixin("mix:versionable") ;
            session.save() ;
            srcNode.checkin() ;
            srcNode.checkout() ;
            return;
          }             
          return ;
        }
        if(!srcNode.isCheckedOut()) 
          srcNode.checkout() ;
        srcNode.checkin() ;
        srcNode.checkout() ;
        session.save() ;
      }
    }catch (Exception e) {    
      e.printStackTrace() ;
    }       
  }

  public void setParams(String[] arg0) {
    
  }

}
