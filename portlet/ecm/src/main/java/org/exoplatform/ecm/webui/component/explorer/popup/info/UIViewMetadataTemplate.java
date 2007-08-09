/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.info;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorerPortlet;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.metadata.MetadataService;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jan 25, 2007  
 * 2:05:40 PM
 */
@ComponentConfig(
    events = {
      @EventConfig(listeners = UIViewMetadataTemplate.EditPropertyActionListener.class),
      @EventConfig(listeners = UIViewMetadataTemplate.CancelActionListener.class)
    }
)
public class UIViewMetadataTemplate extends UIContainer {

  private String documentType_ ;
  
  public UIViewMetadataTemplate() throws Exception {
  }

  public void setTemplateType(String type) {documentType_ = type ;}
  
  public String getViewTemplatePath() {    
    MetadataService metadataService = getApplicationComponent(MetadataService.class) ;
    String repository = getAncestorOfType(UIJCRExplorerPortlet.class).getPreferenceRepository() ;
    try {
      return metadataService.getMetadataPath(documentType_, false, repository) ;
    } catch (Exception e) {
      e.printStackTrace() ;
    } 
    return null ;
  }
  
  public String getTemplate() { return getViewTemplatePath() ; }
  
  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    //return getAncestorOfType(UIJCRExplorer.class).getJCRTemplateResourceResolver();
    getAncestorOfType(UIJCRExplorer.class).newJCRTemplateResourceResolver() ;
    return getAncestorOfType(UIJCRExplorer.class).getJCRTemplateResourceResolver();
  }

  public Node getViewNode(String nodeType) throws Exception { 
    return getAncestorOfType(UIJCRExplorer.class).getViewNode(nodeType) ;
  }
  
  public List<String> getMultiValues(Node node, String name) throws Exception {
    return getAncestorOfType(UIJCRExplorer.class).getMultiValues(node, name) ;
  }
  
  static public class EditPropertyActionListener extends EventListener<UIViewMetadataTemplate> {
    public void execute(Event<UIViewMetadataTemplate> event) throws Exception {
      UIViewMetadataTemplate uiViewTemplate = event.getSource() ;
      String nodeType = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIViewMetadataManager uiMetaManager = uiViewTemplate.getAncestorOfType(UIViewMetadataManager.class) ;
      UIJCRExplorer uiExplorer = uiViewTemplate.getAncestorOfType(UIJCRExplorer.class) ;
      Node currentNode = uiExplorer.getCurrentNode() ;
      if(!Utils.isSetPropertyNodeAuthorized(currentNode)) {
        throw new MessageException(new ApplicationMessage("UIViewMetadataTemplate.msg.access-denied",
                                                          null, ApplicationMessage.WARNING)) ;
      }
      uiMetaManager.initMetadataFormPopup(nodeType) ;
      UIViewMetadataContainer uiContainer = uiViewTemplate.getParent() ;
      uiContainer.setRenderedChild(nodeType) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer.getParent()) ;
    }
  }
  
  static public class CancelActionListener extends EventListener<UIViewMetadataTemplate> {
    public void execute(Event<UIViewMetadataTemplate> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;
    }
  }
}
