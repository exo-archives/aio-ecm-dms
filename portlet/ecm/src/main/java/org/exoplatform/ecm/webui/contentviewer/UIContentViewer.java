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
package org.exoplatform.ecm.webui.contentviewer;

import javax.jcr.Node;

import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Sep 24, 2008
 */

@ComponentConfig(lifecycle = Lifecycle.class)
public class UIContentViewer extends UIBaseNodePresentation {

  private Node                contentNode;

  private JCRResourceResolver resourceResolver;
  
  private String repository;
  
  private String workspace;

  public void processRender(WebuiRequestContext context) throws Exception {    
    super.processRender(context);
  }

  @Override
  public Node getNode() throws Exception {
    return contentNode;
  }

  @Override
  public Node getOriginalNode() throws Exception {
    return getNode();
  }

  @Override
  public String getRepositoryName() throws Exception {
    return repository;
  }

  public String getWorkspaceName() throws Exception {
    return workspace;
  }
  
  @Override
  public String getTemplatePath() throws Exception {
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    return templateService.getTemplatePath(getNode(), false);
  }

  public String getTemplate() {
    try {
      return getTemplatePath();
    } catch (Exception e) {
      return null;
    }
  }

  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    try {                                    
        resourceResolver = new JCRResourceResolver(repository, workspace, "exo:templateFile");      
    } catch (Exception e) {
      e.printStackTrace();
    }   
    return resourceResolver;
  }

  public String getNodeType() throws Exception {
    return contentNode.getPrimaryNodeType().getName();
  }

  public boolean isNodeTypeSupported() {
    return false;
  }

  public void setNode(Node node) {
    this.contentNode = node;
  }
  
  public void setRepository(String repository) {
    this.repository = repository;    
  }
  
  public void setWorkspace(String workspace) {
    this.workspace = workspace;
  }

}
