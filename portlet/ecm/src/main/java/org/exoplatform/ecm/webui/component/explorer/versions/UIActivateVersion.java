/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.versions;

import javax.jcr.Node;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : trongtt
 *          trongtt@gmail.com
 * Oct 16, 2006
 * 14:07:15 
 */

@ComponentConfig(
    type = UIActivateVersion.class,
    template = "app:/groovy/webui/component/explorer/versions/UIActivateVersion.gtmpl",
    events = {                
        @EventConfig(listeners = UIActivateVersion.EnableVersionActionListener.class),
        @EventConfig(listeners = UIActivateVersion.CancelActionListener.class)
    }
)

public class UIActivateVersion extends UIContainer implements UIPopupComponent {
  
  public UIActivateVersion() throws Exception {}

  public void activate() throws Exception {}
  public void deActivate() throws Exception {}

  static  public class EnableVersionActionListener extends EventListener<UIActivateVersion> {
    public void execute(Event<UIActivateVersion> event) throws Exception {
      UIActivateVersion uiActivateVersion = event.getSource();
      UIJCRExplorer uiExplorer = uiActivateVersion.getAncestorOfType(UIJCRExplorer.class) ;
      Node currentNode = uiExplorer.getCurrentNode() ;
      if(currentNode.isNodeType("rma:filePlan")){
        UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.does-not-support-versioning",null,ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiExplorer.updateAjax(event) ;
        return ;
      }
      currentNode.addMixin(Utils.MIX_VERSIONABLE);
      currentNode.save() ;
      uiExplorer.getSession().save();   
      uiExplorer.getSession().refresh(true) ;      
      uiExplorer.updateAjax(event) ;
    }
  }

  static  public class CancelActionListener extends EventListener<UIActivateVersion> {
    public void execute(Event<UIActivateVersion> event) throws Exception {      
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;
    }
  }
}