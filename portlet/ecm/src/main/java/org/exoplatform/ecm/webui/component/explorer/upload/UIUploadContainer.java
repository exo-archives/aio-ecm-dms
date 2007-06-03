/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.upload;

import javax.jcr.Item;
import javax.jcr.Node;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIMultiLanguageForm;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIMultiLanguageManager;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * May 25, 2007 8:59:34 AM
 */
@ComponentConfig(
    template = "app:groovy/webui/component/explorer/UITabPaneWithAction.gtmpl",
    events = { 
        @EventConfig(listeners = UIUploadContainer.CloseActionListener.class),
        @EventConfig(listeners = UIUploadContainer.AddMetadataActionListener.class)
    }
)
public class UIUploadContainer extends UIContainer {

  private Node uploadedNode_ ;
  
  public UIUploadContainer() throws Exception {
    addChild(UIUploadContent.class, null, null) ;
  }

  public String[] getActions() {return new String[] {"AddMetadata","Close"} ;}
  
  public Node getEditNode(String nodeType) throws Exception { 
    try {
      Item primaryItem = uploadedNode_.getPrimaryItem() ;
      if(primaryItem == null || !primaryItem.isNode()) return uploadedNode_ ;
      if(primaryItem != null && primaryItem.isNode()) {
        Node primaryNode = (Node) primaryItem ;
        if(primaryNode.isNodeType(nodeType)) return primaryNode ;
      }
    } catch(Exception e) { }
    return uploadedNode_ ;
  }
  
  public void setUploadedNode(Node node) throws Exception { uploadedNode_ = node ; }
  public Node getUploadedNode() { return uploadedNode_ ; }
  
  static public class CloseActionListener extends EventListener<UIUploadContainer> {
    public void execute(Event<UIUploadContainer> event) throws Exception {
      UIUploadManager uiUploadManager = event.getSource().getParent() ;
      UIUploadForm uiUploadForm = uiUploadManager.getChild(UIUploadForm.class) ;
      if(uiUploadForm.isMultiLanguage()) {
        UIMultiLanguageManager uiLanguageManager = uiUploadManager.getAncestorOfType(UIMultiLanguageManager.class) ;
        uiLanguageManager.setRenderedChild(UIMultiLanguageForm.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiLanguageManager) ;
        return ;
      }
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;
    }
  }
  
  static public class AddMetadataActionListener extends EventListener<UIUploadContainer> {
    public void execute(Event<UIUploadContainer> event) throws Exception {
      UIUploadManager uiUploadManager = event.getSource().getParent() ;
      uiUploadManager.initMetadataPopup() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiUploadManager) ;
    }
  }
}
