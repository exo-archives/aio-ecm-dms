package org.exoplatform.services.cms.impl;

import java.util.ArrayList;
import java.util.List;

public class ResourceConfig {
  private boolean autoCreatedInNewRepository ;
  private String repository ;
  private List resources = new ArrayList(5);
  
  public List getRessources() { return resources ; }
  public void setRessources(List resources) { this.resources = resources ; }
  
  public boolean getAutoCreatedInNewRepository() { return autoCreatedInNewRepository ; }
  public void setAutoCreatedInNewRepository(boolean isAuto) { autoCreatedInNewRepository = isAuto ;}
  
  public String getRepositoty() { return repository ; }
  public void setRepository(String repo) {repository = repo ; }
  
  static public class Resource { private String name ;
    public String getName() { return name ; }
    public void setName(String name) { this.name = name ; }
  }
}
