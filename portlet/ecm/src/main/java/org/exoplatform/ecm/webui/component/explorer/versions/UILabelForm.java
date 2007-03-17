/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.versions;

import javax.jcr.Node;

import org.exoplatform.ecm.jcr.model.VersionNode;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Design : nqhungvn
 *          nguyenkequanghung@yahoo.com
 * Implement: lxchiati
 *            lebienthuy@gmail.com
 * July 3, 2006
 * 10:07:15 AM
 */

@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template =  "system:/groovy/webui/component/UIFormWithTitle.gtmpl",
  events = {
    @EventConfig(listeners = UILabelForm.SaveActionListener.class),
    @EventConfig(listeners = UILabelForm.CancelActionListener.class)    
  }
)

public class UILabelForm extends UIForm {
  
  private static  String FIELD_LABEL = "label" ;
  
  public UILabelForm() throws Exception {
    addUIFormInput(new UIFormStringInput(FIELD_LABEL , FIELD_LABEL , null));   
  }
  
  @SuppressWarnings("unused")
  static  public class SaveActionListener extends EventListener<UILabelForm> {
    public void execute(Event<UILabelForm> event) throws Exception {
      UILabelForm uiLabelForm = event.getSource();
      String label = uiLabelForm.getUIStringInput(FIELD_LABEL).getValue();    
      UIVersionInfo uiVersionInfo = uiLabelForm.getParent();
      VersionNode currentVersion = uiVersionInfo.getCurrentVersionNode();
      UIJCRExplorer uiExplorer = uiLabelForm.getAncestorOfType(UIJCRExplorer.class) ;
      Node  currentNode = uiExplorer.getCurrentNode() ;   
      currentNode.getVersionHistory().addVersionLabel(currentVersion.getName(), label, true) ;  
      uiLabelForm.reset() ;
      uiLabelForm.setRendered(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiVersionInfo) ;
    }
  }  
 
  @SuppressWarnings("unused")
  static  public class CancelActionListener extends EventListener<UILabelForm> {
    public void execute(Event<UILabelForm> event) throws Exception {
      UILabelForm uiLabelForm = event.getSource();
      uiLabelForm.reset() ;
      uiLabelForm.setRendered(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiLabelForm.getParent()) ;
    }
  }
}

