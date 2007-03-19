/******************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL. All rights reserved.            *
 * Please look at license.txt in info directory for more license detail.      *
 ******************************************************************************/
package hero.hook;

import hero.interfaces.BnNodeLocal;
import hero.interfaces.BnNodeLocalHome;
import hero.interfaces.BnNodePK;
import hero.interfaces.BnNodeUtil;
import hero.interfaces.Constants;
import hero.interfaces.UserSessionLocal;
import hero.interfaces.UserSessionLocalHome;
import hero.interfaces.UserSessionUtil;
import hero.util.HeroHookException;

/**
 * This Node Hook is invoked when a Deadline occurs on the Publication activity.
 * It moves the document in the JCR.
 * 
 * Created by Bull R&D
 * @author Brice Revenant
 * Mar 21, 2006
 */
public class ContentBackupOnDeadlineHook implements NodeHookI {

  /* (non-Javadoc)
   * @see hero.hook.NodeHookI#getMetadata()
   */
  public String getMetadata() {
    
    // Return Metadata information
    return Constants.Nd.ONDEADLINE;
  }

  public void beforeStart(Object arg0, BnNodeLocal arg1)
      throws HeroHookException {
  }

  public void afterStart(Object arg0, BnNodeLocal arg1)
      throws HeroHookException {
  }

  public void beforeTerminate(Object arg0, BnNodeLocal arg1)
      throws HeroHookException {
  }

  public void afterTerminate(Object arg0, BnNodeLocal arg1)
      throws HeroHookException {
  }

  public void anticipate(Object arg0, BnNodeLocal arg1)
      throws HeroHookException {
  }

  public void onCancel(Object arg0, BnNodeLocal arg1) throws HeroHookException {
  }

  /* (non-Javadoc)
   * @see hero.hook.NodeHookI#onDeadline(java.lang.Object, hero.interfaces.BnNodeLocal)
   */
  public void onDeadline(Object obj, BnNodeLocal node)
      throws HeroHookException {

    UserSessionLocal userSession = null;
    
    try {
      // Retrieve textual information
      String projectName = node.getBnProject().getName();
      String nodeName = node.getName();
      
      // Initialize User Session
      UserSessionLocalHome userSessionHome = UserSessionUtil.getLocalHome();
      userSession = userSessionHome.create();
      
      // Start and terminate the Activity
      userSession.startActivity(projectName, nodeName);
      userSession.terminateActivity(projectName, nodeName);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    finally {
      try {
        userSession.remove();
      }
      catch(Exception ignore) {
      }
    }
  }

  public void onReady(Object arg0, BnNodeLocal arg1) throws HeroHookException {
  }
}
