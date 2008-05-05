/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.ecm.template;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * May 5, 2008  
 */
public interface NodeTemplateService {
  
  final public String TEMPLATE_REGISTRY_PATH = "/exo:registry/exo:services/exo:ecm/exo:templates".intern();
  
  final public String DIALOG_TYPE = "dialog".intern();
  final public String VIEW_TYPE = "view".intern();
  
  public String getTemplatePath(Node node, boolean isDialog) throws Exception ;
  public String getTemplatePathByUser(String nodeTypeName, boolean isDialog, String userName, String repository, SessionProvider sessionProvider) throws Exception ;
  public String getTemplatePath(String nodeTypeName, String templateName, boolean isDialog,String repository, SessionProvider sessionProvider) throws Exception ;
    
  public String getTemplate(String nodeTypeName, String templateName, boolean isDialog, String repository, SessionProvider sessionProvider) throws Exception ;
  
  public String addTemplate(String nodeTypeName, String label, String templateName, boolean isDialog, 
      boolean isDocumentTemplate, ArrayList<String> accessPermission, String templateData, String repository, SessionProvider sessionProvider) throws Exception;  
  
  public void removeTemplate(String nodeTypeName, String templateName, boolean isDialog, String repository, 
                             SessionProvider sessionProvider) throws Exception;
  
  public boolean isManagedNodeType(String nodeTypeName, String repository) throws Exception ;   
  public List<String> getDocumentTemplates(String repository) throws Exception ;       
  
  public void removeManagedNodeType(String nodeTypeName, String repository) throws Exception ;
  
  public Node getTemplateNode(boolean isDialog, String nodeTypeName, String templateName, String repository, SessionProvider provider) throws Exception ;
}
