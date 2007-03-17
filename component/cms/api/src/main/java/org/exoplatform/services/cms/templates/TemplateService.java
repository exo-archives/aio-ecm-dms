/*
 * Created on Mar 1, 2005
 */
package org.exoplatform.services.cms.templates;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;


/**
 * @author benjaminmestrallet
 */
public interface TemplateService {  

  static final public String DIALOGS = "dialogs".intern();
  static final public String VIEWS = "views".intern();
  
  static final public String DEFAULT_DIALOG = "dialog1".intern();
  static final public String DEFAULT_VIEW = "view1".intern();
  
  static final String[] UNDELETABLE_TEMPLATES = {DEFAULT_DIALOG, DEFAULT_VIEW};  
  
  static final public String DEFAULT_DIALOGS_PATH = "/" + DIALOGS + "/" + DEFAULT_DIALOG;
  static final public String DEFAULT_VIEWS_PATH = "/" + VIEWS + "/" + DEFAULT_VIEW;
    
  static final public String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
  static final public String EXO_TEMPLATE = "exo:template".intern() ;
  static final public String EXO_ROLES_PROP = "exo:roles".intern() ;
  static final public String EXO_TEMPLATE_FILE_PROP = "exo:templateFile".intern() ;  
  static final public String DOCUMENT_TEMPLATE_PROP = "isDocumentTemplate".intern() ;  
  static final public String TEMPLATE_LABEL = "label".intern() ;
  
  public String getDefaultTemplatePath(boolean isDialog, String nodeTypeName) ;  
  
  public Node getTemplatesHome() throws Exception ;
  
  public String getTemplatePath(boolean isDialog, String nodeTypeName) throws Exception ;
  public String getTemplatePath(boolean isDialog, String nodeTypeName, String templateName) throws Exception ;
  
  public String getTemplate(boolean isDialog, String nodeTypeName) throws Exception ;
  public String getTemplate(boolean isDialog, String nodeTypeName, String templateName) throws Exception ;
  
  public void addTemplate(boolean isDialog, String nodeTypeName, String label, boolean isDocumentTemplate, String templateName, 
      String[] roles, String templateFile) throws Exception;  
  public void removeTemplate(boolean isDialog, String nodeTypeName, String templateName) throws Exception;
  
  public boolean isManagedNodeType(String nodeTypeName) throws Exception ; 
  
  public List getDocumentTemplates() throws Exception ;
  
  public NodeIterator getAllTemplatesOfNodeType(boolean isDialog, String nodeTypeName) throws Exception;  
  
  public void removeManagedNodeType(String nodeTypeName) throws Exception ;
  
  public String getTemplateLabel(String nodeTypeName)  throws Exception ;
  
  public String getTemplateRoles(boolean isDialog, String nodeTypeName, String templateName) throws Exception ;
  
  public Node getTemplateNode(boolean isDialog, String nodeTypeName, String templateName) throws Exception ;
}
