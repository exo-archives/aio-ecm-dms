/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.workflow.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.workflow.WorkflowFormsService;
import org.exoplatform.services.workflow.WorkflowServiceContainer;
import org.exoplatform.services.workflow.impl.jbpm.WorkflowServiceContainerImpl;
import org.exoplatform.test.BasicTestCase;
import org.jbpm.db.JbpmSession;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.jpdl.par.ProcessArchiveDeployer;




/**
 * Created y the eXo platform team
 * User: Benjamin Mestrallet
 * Date: 10 mai 2004
 */
public abstract class BaseTest extends BasicTestCase {

  protected static final String PROCESS_PATH = "file:./src/conf/processes/";
  protected WorkflowServiceContainer workflowServiceContainer;
  protected WorkflowFormsService workflowFormsService;

  public BaseTest(String name) {
    super(name);
  }

  public void setUp() {
    workflowServiceContainer = (WorkflowServiceContainer) PortalContainer.
        getInstance().getComponentInstanceOfType(WorkflowServiceContainer.class);
    workflowFormsService = (WorkflowFormsService) PortalContainer.
        getInstance().getComponentInstanceOfType(WorkflowFormsService.class);
  }

  protected void deployProcess(String process, String[] files) throws IOException{
    URL url = new URL(PROCESS_PATH + process);
    InputStream is = url.openStream();    
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlInputStream(is);
    
    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        String file = files[i];
        url = new URL(PROCESS_PATH + file);
        processDefinition.getFileDefinition().addFile(file, url.openStream());        
      }
    }    
    JbpmSession session = ((WorkflowServiceContainerImpl) workflowServiceContainer).openSession();
    ProcessArchiveDeployer.deployProcessDefinition(processDefinition, session.getJbpmSessionFactory());
  }

}
