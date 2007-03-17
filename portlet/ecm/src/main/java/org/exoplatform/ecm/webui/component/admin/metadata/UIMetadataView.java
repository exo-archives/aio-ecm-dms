/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.metadata;

import javax.jcr.nodetype.NodeType;

import org.exoplatform.services.jcr.core.ExtendedPropertyType;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Sep 20, 2006
 * 8:59:13 AM 
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/admin/metadata/UIMetadataView.gtmpl",
    events = @EventConfig(listeners = UIMetadataView.CancelActionListener.class)
)
public class UIMetadataView extends UIContainer{

  private NodeType metadataType_  ;

  public UIMetadataView() throws Exception {
  }

  public void  setMetadata(NodeType nodetype) { metadataType_ = nodetype ; }
  public NodeType getMetadata() { return metadataType_ ; }

  public String resolveType(int type) { return ExtendedPropertyType.nameFromValue(type); }

  static public class CancelActionListener extends EventListener<UIMetadataView> {
    public void execute(Event<UIMetadataView> event) throws Exception {
      UIMetadataView uiView = event.getSource() ;
      UIMetadataManager uiManager = uiView.getAncestorOfType(UIMetadataManager.class) ;
      uiManager.removeChildById(UIMetadataManager.VIEW_METADATA_POPUP) ;
      Class[] childrenToRender = {UIMetadataList.class, UIPopupWindow.class} ;
      uiManager.setRenderedChildrenOfTypes(childrenToRender) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }

}
