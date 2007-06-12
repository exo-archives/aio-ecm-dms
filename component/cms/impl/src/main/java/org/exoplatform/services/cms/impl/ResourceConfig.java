package org.exoplatform.services.cms.impl;

import java.util.ArrayList;
import java.util.List;

public class ResourceConfig {
  private String repository ;
  private String workspace;
  private List resources = new ArrayList(5);
  
  public List getRessources() {
    return resources;
  }
  public void setRessources(List resources) {
    this.resources = resources;
  }
  
  public String getRepositoty() { return repository ; }
  public void setRepository(String repo) {repository = repo ; }
  
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
