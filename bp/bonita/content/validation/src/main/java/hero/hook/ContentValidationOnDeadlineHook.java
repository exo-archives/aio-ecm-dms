/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

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
 * Mar 14, 2006
 */
public class ContentValidationOnDeadlineHook implements NodeHookI {

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
