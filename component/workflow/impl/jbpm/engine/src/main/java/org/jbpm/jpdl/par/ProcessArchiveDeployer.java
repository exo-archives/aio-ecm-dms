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
package org.jbpm.jpdl.par;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.db.JbpmSession;
import org.jbpm.db.JbpmSessionFactory;
import org.jbpm.graph.def.ProcessDefinition;

/**
 * deploys process archives.
 */
public class ProcessArchiveDeployer {
  
  JbpmSessionFactory jbpmSessionFactory = null;
  
  // constructors /////////////////////////////////////////////////////////////
  
  public ProcessArchiveDeployer() {
    jbpmSessionFactory = JbpmSessionFactory.buildJbpmSessionFactory();
  }
  
  public ProcessArchiveDeployer(JbpmSessionFactory jbpmSessionFactory) {
    this.jbpmSessionFactory = jbpmSessionFactory;
  }

  // deployer methods /////////////////////////////////////////////////////////

  public static void deployResource(String parResource) {
    deployResource(parResource, JbpmSessionFactory.getInstance());
  }

  public static void deployResource(String parResource, JbpmSessionFactory jbpmSessionFactory) {
    ProcessDefinition processDefinition = ProcessDefinition.parseParResource(parResource);
    deployProcessDefinition(processDefinition, jbpmSessionFactory);
  }

  public static void deployZipInputStream(ZipInputStream zipInputStream) {
    deployZipInputStream(zipInputStream, JbpmSessionFactory.getInstance());
  }

  public static void deployZipInputStream(ZipInputStream zipInputStream, JbpmSessionFactory jbpmSessionFactory) {
    ProcessDefinition processDefinition = ProcessDefinition.parseParZipInputStream(zipInputStream);
    deployProcessDefinition(processDefinition, jbpmSessionFactory);
  }

  public static void deployProcessDefinition(ProcessDefinition processDefinition) {
    deployProcessDefinition(processDefinition, JbpmSessionFactory.getInstance());
  }

  public static void deployProcessDefinition(ProcessDefinition processDefinition, JbpmSessionFactory jbpmSessionFactory) {
    JbpmSession jbpmSession = jbpmSessionFactory.openJbpmSession();
    try {
      log.debug("starting transaction to deploy process "+processDefinition);
      jbpmSession.beginTransaction();
      
      // assign the version number
      String processDefinitionName = processDefinition.getName();
      // if the process definition has a name (process versioning only applies to named process definitions)
      if (processDefinitionName!=null) {
        // find the current latest process definition
        ProcessDefinition previousLatestVersion = jbpmSession.getGraphSession().findLatestProcessDefinition(processDefinitionName);
        // if there is a current latest process definition
        if (previousLatestVersion!=null) {
          // take the next version number
          processDefinition.setVersion( previousLatestVersion.getVersion()+1 );
        } else {
          // start from 1
          processDefinition.setVersion(1);
        }
      }
      
      // save the process definition in the database
      jbpmSession.getGraphSession().saveProcessDefinition(processDefinition);
      
      log.debug("flushing...");
      jbpmSession.getSession().flush();
      
      log.debug("committing transaction to deploy process "+processDefinition);
      jbpmSession.commitTransaction();
    } finally {
      jbpmSession.close();
    }
  }
  
  public static void main(String[] args) {
    if ( (args!=null)
         && (args.length>0) ) {
      for (int i=0; i<args.length; i++) {
        try {
          deployZipInputStream(new ZipInputStream(new FileInputStream(new File(args[i]))));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  private static final Log log = LogFactory.getLog(ProcessArchiveDeployer.class);
}
