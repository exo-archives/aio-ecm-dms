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

  private String repository ;
  private List<String> workspaces = new ArrayList<String>(5) ;
  private List<JcrPath> jcrPaths = new ArrayList<JcrPath>(5);

  public String getRepository() { return repository ; }
  public void setRepository(String rp) { repository = rp ; }

  public List<JcrPath> getJcrPaths() {   return jcrPaths; }
  public void setJcrPaths(List<JcrPath> s) {  this.jcrPaths = s; }

  public List<String> getWorkspaces() { return this.workspaces ; }
  public void setWorksapces(List<String> list) { this.workspaces = list ; }

  static public class JcrPath {
    private String alias ;
    private String path ;
    private String nodeType ;
    
    private List<Permission> permissions = new ArrayList<Permission>(4);

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public List<Permission> getPermissions() { return this.permissions ; }
    public void setPermissions(List<Permission> list) { this.permissions = list ; }
    
    public String getNodeType() { return this.nodeType ; }
    public void setNodeType(String nodetype) { this.nodeType = nodetype ; }
    
  }   

  static public class Permission {
    private String identity;
    private String read;
    private String addNode;
    private String setProperty;
    private String remove;

    public String getIdentity() { return identity; }
    public void setIdentity(String identity) { this.identity = identity; }

    public String getAddNode() { return addNode; }
    public void setAddNode(String addNode) { this.addNode = addNode; }

    public String getRead() { return read; }
    public void setRead(String read) { this.read = read; }

    public String getRemove() { return remove; }
    public void setRemove(String remove) { this.remove = remove; }

    public String getSetProperty() { return setProperty; }
    public void setSetProperty(String setProperty) { this.setProperty = setProperty; }

  }

}
