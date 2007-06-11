/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.upload;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * May 24, 2007 5:48:57 PM
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/explorer/upload/UIUploadContent.gtmpl",
    events = {@EventConfig(listeners = UIUploadContent.EditActionListener.class)}
)
public class UIUploadContent extends UIContainer {
  
  private String[] arrValues_ ;
  public List<String> externalList_ = new ArrayList<String>() ;
  
  public UIUploadContent() throws Exception {
  }
  
  public Node getUploadedNode() { return ((UIUploadContainer)getParent()).getUploadedNode() ; }
  
  public List<String> getExternalList() throws Exception { 
    if(getUploadedNode().hasNode(Utils.JCR_CONTENT)) {
      for(NodeType nodeType : getUploadedNode().getNode(Utils.JCR_CONTENT).getMixinNodeTypes()) {
        if(nodeType.isMixin() && isExternalUse(nodeType) && !externalList_.contains(nodeType.getName())) {
          externalList_.add(nodeType.getName()) ;
        }
      }
    }
    return externalList_ ; 
  }
  
  private boolean isExternalUse(NodeType nodeType) throws Exception{
    PropertyDefinition def = 
      ((ExtendedNodeType)nodeType).getPropertyDefinitions("exo:internalUse").getAnyDefinition() ;    
    return !def.getDefaultValues()[0].getBoolean() ;
  }
  
  public String[] arrUploadValues() { return arrValues_ ; }
  
  public void setUploadValues(String[] arrValues) { arrValues_ = arrValues ; }
  
  static public class EditActionListener extends EventListener<UIUploadContent> {
    public void execute(Event<UIUploadContent> event) throws Exception {
      UIUploadContainer uiUploadContainer = event.getSource().getParent() ;
      String nodeType = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiUploadContainer.removeChild(UIAddMetadataForm.class) ;
      UIAddMetadataForm uiAddMetadataForm = 
        uiUploadContainer.createUIComponent(UIAddMetadataForm.class, null, null) ;
      uiAddMetadataForm.getChildren().clear() ;
      uiAddMetadataForm.setNodeType(nodeType) ;
      uiAddMetadataForm.setIsNotEditNode(true) ;
      uiAddMetadataForm.setPropertyNode(uiUploadContainer.getEditNode(nodeType)) ;
      uiUploadContainer.addChild(uiAddMetadataForm) ;
      uiUploadContainer.setRenderedChild(UIAddMetadataForm.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiUploadContainer) ;
    }
  }
}
