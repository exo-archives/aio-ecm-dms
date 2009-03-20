/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.ecm.dms;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.test.BasicTestCase;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Mar 16, 2009  
 * 4:08:30 PM
 */
public abstract class BaseDMSTestCase extends BasicTestCase {

  protected static Log          log = ExoLogger.getLogger("dms.services.test");  

  protected CredentialsImpl     credentials;  

  protected RepositoryService   repositoryService;

  protected StandaloneContainer container;
  
  protected SessionImpl         session;

  protected RepositoryImpl      repository;
  
  protected final String REPO_NAME = "repository".intern();
  protected final String SYSTEM_WS = "system".intern();
  protected final String COLLABORATION_WS = "collaboration".intern();

  public void setUp() throws Exception {
    String containerConf = 
      getClass().getResource("/conf/standalone/test-configuration.xml").toString();
    String loginConf = 
      Thread.currentThread().getContextClassLoader().getResource("login.conf").toString();

    StandaloneContainer.addConfigurationURL(containerConf);
    container = StandaloneContainer.getInstance();
    
    if (System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config", loginConf);

    credentials = new CredentialsImpl("root", "exo".toCharArray());
    repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    repository = (RepositoryImpl) repositoryService.getDefaultRepository();

    session = (SessionImpl) repository.login(credentials, COLLABORATION_WS);  
  }

  protected void checkMixins(String[] mixins, NodeImpl node) {
    try {
      String[] nodeMixins = node.getMixinTypeNames();
      assertEquals("Mixins count is different", mixins.length, nodeMixins.length);

      compareMixins(mixins, nodeMixins);
    } catch (RepositoryException e) {
      fail("Mixins isn't accessible on the node " + node.getPath());
    }
  }

  protected void compareMixins(String[] mixins, String[] nodeMixins) {
    nextMixin: for (String mixin : mixins) {
      for (String nodeMixin : nodeMixins) {
        if (mixin.equals(nodeMixin))
          continue nextMixin;
      }

      fail("Mixin '" + mixin + "' isn't accessible");
    }
  }

  protected String memoryInfo() {
    String info = "";
    info = "free: " + mb(Runtime.getRuntime().freeMemory()) + "M of "
    + mb(Runtime.getRuntime().totalMemory()) + "M (max: "
    + mb(Runtime.getRuntime().maxMemory()) + "M)";
    return info;
  }

  // bytes to Mbytes
  protected String mb(long mem) {
    return String.valueOf(Math.round(mem * 100d / (1024d * 1024d)) / 100d);
  }

  protected String execTime(long from) {
    return Math.round(((System.currentTimeMillis() - from) * 100.00d / 60000.00d)) / 100.00d
    + "min";
  }

}
