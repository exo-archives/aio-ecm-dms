/******************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL. All rights reserved.            *
 * Please look at license.txt in info directory for more license detail.      *
 ******************************************************************************/

package hero.hook;

import hero.interfaces.BnNodeLocal;
import hero.interfaces.Constants;
import hero.interfaces.ProjectSessionLocal;
import hero.interfaces.ProjectSessionLocalHome;
import hero.interfaces.ProjectSessionUtil;
import hero.util.HeroHookException;

/**
 * This Node Hook gets the user name who started the Instance and puts it
 * in "initiator" attribute. It is used to print the initiator name into a
 * Form component.
 * 
 * Created by Bull R&D
 * @author Fouad Allaoui
 * Apr 21, 2006
 */
public class PayRaiseUserNameHook implements NodeHookI {
    
  /** Name of the Property which represents the process initiator user name */
  public static final String PROCESS_INITIATOR_USER_NAME = "initiator";
	
  /* (non-Javadoc)
   * @see hero.hook.NodeHookI#getMetadata()
   */
  public String getMetadata() {

    // Return Metadata information
    return Constants.Nd.ONREADY;
  }

  /* (non-Javadoc)
   * @see hero.hook.NodeHookI#beforeStart(java.lang.Object, hero.interfaces.BnNodeLocal)
   */
  public void beforeStart(Object arg0, BnNodeLocal arg1)
      throws HeroHookException {  
    // TODO Auto-generated method stub
      
  }

  /* (non-Javadoc)
   * @see hero.hook.NodeHookI#afterStart(java.lang.Object, hero.interfaces.BnNodeLocal)
   */
  public void afterStart(Object arg0, BnNodeLocal arg1)
      throws HeroHookException { 	 
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see hero.hook.NodeHookI#beforeTerminate(java.lang.Object, hero.interfaces.BnNodeLocal)
   */
  public void beforeTerminate(Object arg0, BnNodeLocal arg1)
      throws HeroHookException {
    // TODO Auto-generated method stub
	  
  }

  /* (non-Javadoc)
   * @see hero.hook.NodeHookI#afterTerminate(java.lang.Object, hero.interfaces.BnNodeLocal)
   */
  public void afterTerminate(Object arg0, BnNodeLocal arg1)
      throws HeroHookException {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see hero.hook.NodeHookI#anticipate(java.lang.Object, hero.interfaces.BnNodeLocal)
   */
  public void anticipate(Object arg0, BnNodeLocal arg1)
      throws HeroHookException {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see hero.hook.NodeHookI#onCancel(java.lang.Object, hero.interfaces.BnNodeLocal)
   */
  public void onCancel(Object arg0, BnNodeLocal arg1) throws HeroHookException {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see hero.hook.NodeHookI#onDeadline(java.lang.Object, hero.interfaces.BnNodeLocal)
   */
  public void onDeadline(Object arg0, BnNodeLocal arg1)
      throws HeroHookException {
    // TODO Auto-generated method stub

  }
  
  /* (non-Javadoc)
   * @see hero.hook.NodeHookI#onReady(java.lang.Object, hero.interfaces.BnNodeLocal)
   */
  public void onReady(Object obj, BnNodeLocal node) throws HeroHookException {
      
    ProjectSessionLocal projectSession = null;
      
    try {
      // Initialize Project Session                   
      String projectName = node.getBnProject().getName();
      ProjectSessionLocalHome projectSessionHome = 
        ProjectSessionUtil.getLocalHome();
      projectSession = projectSessionHome.create();
      projectSession.initProject(projectName);

      // Get the eXo initiator user name of the current process
      String user_Name = projectSession.getCreator();

      // Set the PROCESS_INITIATOR_USER_NAME variable with the right value 
      projectSession.setProperty(PROCESS_INITIATOR_USER_NAME, user_Name);
    }
    catch(Exception e) {
      // TODO Use logging system instead
      e.printStackTrace();
    }
    finally {
      try {
        projectSession.remove();
      }
      catch(Exception ignore) {
      }
    }
  }
}
