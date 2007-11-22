package org.exoplatform.services.cms.impl;

import java.util.ArrayList;
import java.util.List;

public class ResourceConfig {
  
  private List<Resource> resources = new ArrayList<Resource>(5);
  
  public List getRessources() { return resources ; }
  @SuppressWarnings("unchecked")
  public void setRessources(List resources) { this.resources = resources ; }
  
  static public class Resource { 
    private String name ;
    private String description ;
    
    public String getName() { return name ; }
    public void setName(String name) { this.name = name ; }
    
    public String getDescription() { return this.description ; }
    public void setDescription(String s) {this.description = s ; }
    
  }
  
}
