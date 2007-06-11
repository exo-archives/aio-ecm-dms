/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.admin;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.portal.component.view.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Nov 15, 2006 10:08:29 AM 
 */

@ComponentConfig()

public class UIActionViewTemplate extends UIContainer {

  private String documentType_ ;
  private Node node_ ;

  public void setTemplateNode(Node node) throws Exception { 
    node_ = node ;
    documentType_ = node.getPrimaryNodeType().getName() ;
  }

  public String getViewTemplatePath(){    
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    try {
      return templateService.getTemplatePathByUser(false, documentType_, userName) ;
    } catch (Exception e) {
      return null ;
    }         
  }

  public String getTemplate() { return getViewTemplatePath() ;}

  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return getAncestorOfType(UIJCRExplorer.class).getJCRTemplateResourceResolver() ;
  }

  public Node getNode() {return node_ ;}
}
