package org.exoplatform.services.cms.actions.impl;

import javax.jcr.observation.EventListener;

public interface ECMEventListener extends EventListener{

  public String getSrcWorkspace();
  public String getRepository();
  
}
