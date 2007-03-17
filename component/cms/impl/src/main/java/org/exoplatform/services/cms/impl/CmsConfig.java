/*
 * Created on Mar 18, 2005
 */
package org.exoplatform.services.cms.impl;

import java.util.ArrayList;
import java.util.List;


/**
 * @author benjaminmestrallet
 */
public class CmsConfig {

  private List jcrPaths = new ArrayList(5);
  
  public List getJcrPaths() {   return jcrPaths; }
  public void setJcrPaths(List s) {  this.jcrPaths = s; }  
  
  static public class JcrPath {
    private String alias ;
    private String path ;
    private List workspaces = new ArrayList(3);
    
    public String getAlias() {
      return alias;
    }
    public void setAlias(String alias) {
      this.alias = alias;
    }
    public String getPath() {
      return path;
    }
    public void setPath(String path) {
      this.path = path;
    }
    public List getWorkspaces() {
      return workspaces;
    }
    public void setWorkspaces(List workspaces) {
      this.workspaces = workspaces;
    }    
  }   
  
  static public class Workspace {
    private String name;
    private List permissions = new ArrayList(4);

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public List getPermissions() {
      return permissions;
    }

    public void setPermissions(List permissions) {
      this.permissions = permissions;
    }            
    
  }
  
  static public class Permission {
    private String owner;
    private String read;
    private String addNode;
    private String setProperty;
    private String remove;
            
    public String getOwner() {
      return owner;
    }
    public void setOwner(String owner) {
      this.owner = owner;
    }
    public String getAddNode() {
      return addNode;
    }
    public void setAddNode(String addNode) {
      this.addNode = addNode;
    }
    public String getRead() {
      return read;
    }
    public void setRead(String read) {
      this.read = read;
    }
    public String getRemove() {
      return remove;
    }
    public void setRemove(String remove) {
      this.remove = remove;
    }
    public String getSetProperty() {
      return setProperty;
    }
    public void setSetProperty(String setProperty) {
      this.setProperty = setProperty;
    }
    
  }

}
