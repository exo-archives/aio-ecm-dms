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
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * June 29, 2007  
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
      if("modify".equals(lifecycle)) {
        String propertyName = nodePath.substring(nodePath.lastIndexOf("/") + 1, nodePath.length()) ;
        if(!propertyName.equals("jcr:isCheckedOut")) {
          Property changedProp = session.getItem(nodePath) ;
          Node modifiedNode = changedProp.getParent() ;
          if(!modifiedNode.isNodeType("mix:versionable")) return ;
          if(!modifiedNode.isCheckedOut()) modifiedNode.checkout() ;
          modifiedNode.checkin() ;
          modifiedNode.checkout() ;
          session.save();
          session.refresh(true) ;        
        }
      } else if("add".equals(lifecycle)||"remove".equals(lifecycle)|| "schedule".equals(lifecycle)) {
        Node currentNode = (Node)session.getItem(nodePath) ;
        if(!currentNode.isNodeType("mix:versionable")) {
          if(currentNode.canAddMixin("mix:versionable")) {
            currentNode.addMixin("mix:versionable") ;
            session.save() ;
            currentNode.checkin() ;
            currentNode.checkout() ;
            return;
          }             
          return ;
        }
        if(!currentNode.isCheckedOut()) currentNode.checkout() ;
        currentNode.checkin() ;
        currentNode.checkout() ;
        session.save() ;
      }
    } catch (Exception e) {    
      e.printStackTrace() ;
    }       
  }

  public void setParams(String[] arg0) {
  }
}
