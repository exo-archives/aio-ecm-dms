package org.exoplatform.services.workflow;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;

/**
 * Plugin to deploy predefined processes.
 * 
 * Created by eXo Platform SAS
 * @author Brice Revenant
 * June 1, 2007
 */
public class PredefinedProcessesPlugin extends BaseComponentPlugin {

  /** Process configuration, as specified in the configuration file */
  private ProcessesConfig processesConfig = null;
  
  /**
   * Plugin constructor.
   * Caches data specified by the container
   * 
   * @param params Initialization data
   * as specifed in the configuration file
   */
  public PredefinedProcessesPlugin(InitParams params) {
    ObjectParameter param = params.getObjectParam("predefined.processes");
    
    if(param != null) {
      // Make sure the Object parameter is specified in the configuration
      this.processesConfig = (ProcessesConfig) param.getObject();
    }
  }
  
  /**
   * Returns data contained by the plugin
   * 
   * @return Processes configuration data
   */
  public ProcessesConfig getProcessesConfig() {
    return this.processesConfig;
  }
}
