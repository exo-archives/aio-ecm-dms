package org.exoplatform.services.cms.scripts;

import java.util.ArrayList;
import java.util.List;

public class ResourceConfig {
  
  private boolean autoCreate ;
  private String repository ;
  private String workspace;
  private List resources = new ArrayList(5);
  
  public boolean getAutoCreate() { return this.autoCreate ; }
  public void setAutoCreate(boolean isAuto) { this.autoCreate = isAuto ; }
  
  public String getRepository() { return repository; }
  public void setRepository(String rp) { this.repository = rp ; }
  
  public List getRessources() {
    return resources;
  }
  public void setRessources(List resources) {
    this.resources = resources;
  }
  
  public String getWorkspace() {
    return workspace;
  }
  public void setWorkspace(String workspace) {
    this.workspace = workspace;
  }  

  static public class Resource {
    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

  }

}
