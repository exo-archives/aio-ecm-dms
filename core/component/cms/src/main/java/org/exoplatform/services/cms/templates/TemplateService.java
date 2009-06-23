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
  
  static final public String RTL = "rtl";
  static final public String LTR = "ltr";
  /**
   * Return path of default template by giving the following params
   * @param isDialog        boolean
   *                        The boolean value which specify the type of template                  
   * @param nodeTypeName    String
   *                        The name of NodeType
   */
  public String getDefaultTemplatePath(boolean isDialog, String nodeTypeName) ;  
  
  /**
   * Return template home of repository
   * @param repository      String
   *                        The name of repository              
   * @param provider        SessionProvider
   *                        The SessionProvider object is used to managed Sessions
   * @see                   Node                        
   * @throws Exception
   */
  public Node getTemplatesHome(String repository,SessionProvider provider) throws Exception ;
  
  /**
   * Return path template of the specified node
   * @param node            Node
   *                        The specified node
   * @param isDialog        boolean
   *                        The boolean value which specify the type of template
   * @see                   Node                       
   * @throws Exception
   */
  public String getTemplatePath(Node node, boolean isDialog) throws Exception ;
  
   
  /**
   * Return the path public template
   * @param isDialog        boolean
   *                        The boolean value which specify the type of template
   * @param nodeTypeName    String
   *                        The specify name of node type
   * @param repository      String
   *                        The name of repository 
   * @throws Exception
   */
  public String getTemplatePathByAnonymous(boolean isDialog, String nodeTypeName, String repository) throws Exception;
  
  /**
   * Return the template by user
   * @param isDialog        boolean
   *                        The boolean value which specify the type of template
   * @param nodeTypeName    String
   *                        The specify name of node type
   * @param userName        String
   *                        The current user
   * @param repository      String  
   *                        The name of repository
   * @see                   Node
   * @see                   Session                        
   * @throws Exception
   */  
  public String getTemplatePathByUser(boolean isDialog, String nodeTypeName, String userName, String repository) throws Exception ;
  
  /**
   * Return path template of the specified node
   * @param isDialog        boolean
   *                        The boolean value which specify the type of template
   * @param nodeTypeName    String
   *                        The specify name of node type
   * @param templateName    String
   *                        The name of template
   * @param repository      String
   *                        The name of repository
   * @see                   Session
   * @see                   Node                        
   * @throws Exception
   */
  public String getTemplatePath(boolean isDialog, String nodeTypeName, String templateName, String repository) throws Exception ;
  
  /**
   * Return template file of the specified node
   * @param isDialog        boolean
   *                        The boolean value which specify the type of template
   * @param nodeTypeName    String
   *                        The specify name of node type
   * @param templateName    String
   *                        The name of template
   * @param repository      String
   *                        The name of repository
   * @see                   Session
   * @see                   Node                        
   * @throws Exception
   */
  public String getTemplate(boolean isDialog, String nodeTypeName, String templateName, String repository) throws Exception ;
  
  /**
   * Insert a new template into NodeType by giving the following params
   * @param isDialog            boolean
   *                            The boolean value which specify the type of template
   * @param nodeTypeName        String
   *                            The specify name of NodType
   * @param label               String
   *                            The label of the specified template
   * @param isDocumentTemplate  boolean
   *                            The boolean value which yes or no is DocumentTemplate
   * @param templateName        String
   *                            The name of template
   * @param roles               String[]
   *                            The roles of template
   * @param templateFile        String
   *                            The file of template
   * @param repository          String
   *                            The name of repository
   * @see                       Session
   * @see                       Node                            
   * @throws Exception
   */
  public String addTemplate(boolean isDialog, String nodeTypeName, String label, boolean isDocumentTemplate, String templateName, 
      String[] roles, String templateFile, String repository) throws Exception;

  /**
   * Insert a new template into NodeType by giving the following params
   * @param isDialog            boolean
   *                            The boolean value which specify the type of template
   * @param nodeTypeName        String
   *                            The specify name of NodType
   * @param label               String
   *                            The label of the specified template
   * @param isDocumentTemplate  boolean
   *                            The boolean value which yes or no is DocumentTemplate
   * @param templateName        String
   *                            The name of template
   * @param roles               String[]
   *                            The roles of template
   * @param templateFile        String
   *                            The file of template
   * @param repository          String
   *                            The name of repository
   * @param locale              String
   *                            The locale name
   * @see                       Session
   * @see                       Node                            
   * @throws Exception
   */
  public String addTemplateWithLocale(boolean isDialog, String nodeTypeName, String label, boolean isDocumentTemplate, String templateName, 
      String[] roles, String templateFile, String repository, String locale) throws Exception;
  
  /**
   * Remove a template of NodeType by giving the following params
   * @param isDialog          boolean
   *                          The boolean value which specify the type of template
   * @param nodeTypeName      String
   *                          The specify name of NodType
   * @param templateName      String
   *                          The name of template
   * @param repository        String
   *                          The name of repository
   * @see                     Session
   * @see                     Node                                 
   * @throws Exception
   */
  public void removeTemplate(boolean isDialog, String nodeTypeName, String templateName, String repository) throws Exception;
  
  /**
   * Return true is the given repository has nodeTypeName
   * @param nodeTypeName    String
   *                        The name of NodeType
   * @param repository      String
   *                        The name of repository
   * @see                   SessionProvider
   * @see                   Session
   * @see                   Node                        
   * @throws Exception
   */
  public boolean isManagedNodeType(String nodeTypeName, String repository) throws Exception ; 
  
  /**
   * Get all templates is document type of the specified repository  
   * @param repository      String
   *                        The name of repository
   * @see                   Session
   * @see                   Node                               
   * @throws Exception
   */
  public List<String> getDocumentTemplates(String repository) throws Exception ;
  
  /**
   * Return all teamplate of the specified NodeType
   * @param isDialog        boolean        
   *                        The boolean value which specify the type of template
   * @param nodeTypeName    String
   *                        The name of NodeType
   * @param repository      String
   *                        The name of repository
   * @param provider        SessionProvider
   *                        The SessionProvider object is used to managed Sessions
   * @see                   SessionProvider
   * @see                   Node                                              
   * @throws Exception
   */
  public NodeIterator getAllTemplatesOfNodeType(boolean isDialog, String nodeTypeName, String repository,SessionProvider provider) throws Exception;  
  
  /**
   * Removes the NodeType by giving the name of NodeType
   * @param nodeTypeName    String
   *                        The name of NodeType           
   * @param repository      String
   *                        The name of repository
   * @see                   Session
   * @see                   Node                        
   * @throws Exception
   */
  public void removeManagedNodeType(String nodeTypeName, String repository) throws Exception ;
  
  /**
   * Return the label of the specified template by giving the following params
   * @param nodeTypeName    String
   *                        The specified name of NodeType
   * @param repository      String
   *                        The name of repository
   * @see                   SessionProvider
   * @see                   Node                        
   * @throws Exception
   */
  public String getTemplateLabel(String nodeTypeName, String repository)  throws Exception ;
  
  /**
   * Return roles of the specified template by giving the following params
   * @param isDialog        boolean        
   *                        The boolean value which specify the type of template
   * @param nodeTypeName    String
   *                        The name of NodeType
   * @param templateName    String
   *                        The name of teamplate
   * @param repository      String
   *                        The name of repository
   * @see                   Session
   * @see                   Node                        
   * @throws Exception
   */
  public String getTemplateRoles(boolean isDialog, String nodeTypeName, String templateName, String repository) throws Exception ;
  
  /**
   * Return template Node (Name of NodeType, Name of Template) by giving the following params 
   * @param isDialog        boolean        
   *                        The boolean value which specify the type of template
   * @param nodeTypeName    String
   *                        The name of NodeType
   * @param templateName    String
   *                        The name of teamplate
   * @param repository      String
   *                        The name of repository
   * @param provider        SessionProvider
   *                        The SessionProvider object is used to managed Sessions
   * @see                   SessionProvider
   * @see                   Node                              
   * @throws Exception
   */
  public Node getTemplateNode(boolean isDialog, String nodeTypeName, String templateName, String repository, SessionProvider provider) throws Exception ;
  
  /**
   * Return CreationableContent Types to the given node
   * @param node          The specified node
   * @see                 Node              
   * @throws Exception
   */
  public List<String> getCreationableContentTypes(Node node) throws Exception;
  
  /**
   * Get all template that is configed in XML file of specified repository 
   * @param repository      String
   *                        The name of repository
   * @see                   TemplatePlugin                       
   * @throws Exception
   */
  public void init(String repository) throws Exception ;

  /**
   * Get TemplateData
   * @param templateNode    Node
   * @param locale          String
   *                        code of locale
   * @param propertyName    String
   *                        The name of property
   * @param repository      String
   *                        The name of repository
   * @return
   * @throws Exception
   */
  public String getTemplateData(Node templateNode, String locale, String propertyName, String repository) throws Exception; 
  
  /**
   * Remove cache of RTL template
   * @param templatePath String 
   *                     jcr path of template
   * @param resourceId   String
   *                     Resource Id
   * @param repository   String
   *                     Repository name
   * @throws Exception
   */
  public void removeCacheTemplate(String resourceId) throws Exception;
}
