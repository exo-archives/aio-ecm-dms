/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.cms.templates;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.services.jcr.ext.common.SessionProvider;


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
  
  public Node getTemplatesHome(String repository,SessionProvider provider) throws Exception ;
  
  public String getTemplatePath(Node node, boolean isDialog) throws Exception ;
  
  /**
   * Return the public template
   * @param isDialog  the boolean value which specify the type of template
   * @param nodeTypeName  specify the name of node type
   * @param repository  repository value
   * @return  the template path
   * @throws Exception
   */
  public String getTemplatePathByAnonymous(boolean isDialog, String nodeTypeName, String repository) throws Exception;
  
  /**
   * Return the template by user
   * @param isDialog  the boolean value which specify the type of template
   * @param nodeTypeName  specify the name of node type
   * @param userName  the user name
   * @param repository  repository value
   * @return  the template path
   * @throws Exception
   */
  public String getTemplatePathByUser(boolean isDialog, String nodeTypeName, String userName, String repository) throws Exception ;
  public String getTemplatePath(boolean isDialog, String nodeTypeName, String templateName, String repository) throws Exception ;
    
  public String getTemplate(boolean isDialog, String nodeTypeName, String templateName, String repository) throws Exception ;
  
  public String addTemplate(boolean isDialog, String nodeTypeName, String label, boolean isDocumentTemplate, String templateName, 
      String[] roles, String templateFile, String repository) throws Exception;  
  public void removeTemplate(boolean isDialog, String nodeTypeName, String templateName, String repository) throws Exception;
  
  public boolean isManagedNodeType(String nodeTypeName, String repository) throws Exception ; 
  
  public List<String> getDocumentTemplates(String repository) throws Exception ;
  
  public NodeIterator getAllTemplatesOfNodeType(boolean isDialog, String nodeTypeName, String repository,SessionProvider provider) throws Exception;  
  
  public void removeManagedNodeType(String nodeTypeName, String repository) throws Exception ;
  
  public String getTemplateLabel(String nodeTypeName, String repository)  throws Exception ;
  
  public String getTemplateRoles(boolean isDialog, String nodeTypeName, String templateName, String repository) throws Exception ;
  
  public Node getTemplateNode(boolean isDialog, String nodeTypeName, String templateName, String repository, SessionProvider provider) throws Exception ;
  
  public void init(String repository) throws Exception ;
}
