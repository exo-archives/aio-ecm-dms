package org.exoplatform.services.cms.actions;

import javax.jcr.observation.EventListener;

public interface ECMEventListener extends EventListener{

  public String getSrcWorkspace();
  public String getRepository();
  
}
