package org.exoplatform.services.workflow.impl.jbpm;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.workflow.WorkflowServiceContainer;
import org.picocontainer.Startable;

public class SchedulerService implements Startable{

  private ExoScheduler exoScheduler;

  public SchedulerService(){
  }

  public void start() {
   String containerName = PortalContainer.getInstance().getPortalContainerInfo().getContainerName();
   exoScheduler = new ExoScheduler(containerName);
   exoScheduler.start();
  }

  public void stop() {
    exoScheduler.stop(); 
  }
  
}
