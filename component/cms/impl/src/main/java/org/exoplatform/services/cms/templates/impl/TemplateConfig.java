/*
 * Created on Mar 1, 2005
 */
package org.exoplatform.services.cms.templates.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * @author benjaminmestrallet
 */
public class TemplateConfig {

  private List nodeTypes = new ArrayList(5);
  private List templates = new ArrayList(3);
  private String location = new String("war");
  
  public List getNodeTypes() {   return this.nodeTypes; }
  public void setNodeTypes(List s) {  this.nodeTypes = s; }    
  
  public List getTemplates() {   return this.templates; }
  public void setTemplates(List s) {  this.templates = s; }
  
  public String getLocation() { return location; }
  public void setLocation(String location) { this.location = location; }
  
  static public class Template {
    
    private String templateFile;
    private String roles;
    
    public String[] getParsedRoles() {
      return StringUtils.split(this.roles, ";");
    }
    public String getRoles() {
      return roles;
    }
    public void setRoles(String roles) {
      this.roles = roles;
    }
    
    public String getTemplateFile() {
      return templateFile;
    }
    public void setTemplateFile(String templateFile) {
      this.templateFile = templateFile;
    }    
  }
  
  static public class NodeType {
    
    private String nodetypeName;
    private String label;
    private boolean documentTemplate ;
    private List referencedDialog;
    private List referencedView;    
    
    public NodeType(){
      referencedDialog = new ArrayList();
      referencedView = new ArrayList();
      documentTemplate = false ;
    }
    
    public String getNodetypeName() { return nodetypeName ; }
    public void setNodetypeName(String s) { nodetypeName = s ; }
    
    public String getLabel() { return label ; }
    public void setLabel(String s) { label = s ; }
    
    public List getReferencedDialog() {
      return referencedDialog;
    }
    public void setReferencedDialog(List referencedDialog) {
      this.referencedDialog = referencedDialog;
    }
    
    public List getReferencedView() {
      return referencedView;
    }
    public void setReferencedView(List referencedView) {
      this.referencedView = referencedView;
    }
    
    public boolean getDocumentTemplate() { 
      return this.documentTemplate ; 
    }
    
    public void setDocumentTemplate( boolean b) {
     this.documentTemplate = b ; 
    }
  }
  
}
