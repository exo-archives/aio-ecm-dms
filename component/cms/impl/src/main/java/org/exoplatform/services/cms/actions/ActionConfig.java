package org.exoplatform.services.cms.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class ActionConfig {
  
  private String workspace;
  private List actions = new ArrayList(5);
  
  public List getActions() { return actions; }
  public void setActions(List actions) { this.actions = actions; }
  
  public String getWorkspace() { return workspace; }
  public void setWorkspace(String workspace) { this.workspace = workspace; }  
  
  static public class Mixin {
    private String name;
    private String properties;
    
    public String getProperties() { return properties; }
    public void setProperties(String properties) { this.properties = properties; }
    
    public Map<String, String> getParsedProperties() {
      Map<String, String> propMap = new HashMap<String, String>(); 
      String[] props = StringUtils.split(this.properties, ";");
      for (int i = 0; i < props.length; i++) {
        String prop = props[i];
        String[] couple = StringUtils.split(prop, "=");
        propMap.put(couple[0], couple[1]);
      }
      return propMap;
    }
    
    
    public String getName() {
      return name;
    }
    public void setName(String name) {
      this.name = name;
    }
    
  }

  static public class Action {
    private String name;
    private String type;    
    private String description;    
    private String srcWorkspace;
    private String srcPath;
    private String lifecyclePhase;
    private String roles;
    private String variables;
    private List mixins = new ArrayList(10);
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getVariables() { return variables; }
    public void setVariables(String variables) { this.variables = variables; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSrcPath() { return srcPath; }
    public void setSrcPath(String srcPath) { this.srcPath = srcPath; }

    public String getSrcWorkspace() { return srcWorkspace; }
    public void setSrcWorkspace(String srcWorkspace) { this.srcWorkspace = srcWorkspace; }

    public String getLifecyclePhase() { return lifecyclePhase; }
    public void setLifecyclePhase(String lifecyclePhase) { this.lifecyclePhase = lifecyclePhase; }

    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }

    public List getMixins() { return mixins; }
    public void setMixins(List mixins) { this.mixins = mixins; }
    
  }

}
