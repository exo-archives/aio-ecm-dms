/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.control ;

import java.util.Set;

import org.exoplatform.ecm.webui.component.explorer.UIDocumentInfo;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentWorkspace;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.validator.EmptyFieldValidator;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@exoplatform.com
 * Aug 2, 2006
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/explorer/control/UIAddressBar.gtmpl",
    events = {
      @EventConfig(listeners = UIAddressBar.ChangeNodeActionListener.class),
      @EventConfig(listeners = UIAddressBar.BackActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIAddressBar.HistoryActionListener.class, phase = Phase.DECODE)
    }
)

public class UIAddressBar extends UIForm {
  final static public String FIELD_ADDRESS = "address" ; 
  
  public UIAddressBar() throws Exception {
    addUIFormInput(new UIFormStringInput(FIELD_ADDRESS, FIELD_ADDRESS, null).
                   addValidator(EmptyFieldValidator.class)) ;
  }

  public Set<String> getHistory() {
    UIJCRExplorer uiJCRExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    return uiJCRExplorer.getAddressPath() ;
  }

  static public class BackActionListener extends EventListener<UIAddressBar> {
    public void execute(Event<UIAddressBar> event) throws Exception {
      UIAddressBar uiAddressBar = event.getSource() ;
      UIJCRExplorer uiExplorer = uiAddressBar.getAncestorOfType(UIJCRExplorer.class) ;
      try {        
        uiAddressBar.getAncestorOfType(UIJCRExplorer.class).
          getChild(UIWorkingArea.class).getChild(UIDocumentWorkspace.class).setRenderedChild(UIDocumentInfo.class) ;
        String previousNode = uiExplorer.rewind() ;
        uiExplorer.setBackNode(previousNode) ;
        uiExplorer.updateAjax(event) ;
      } catch (Exception e) {
        UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIJCRExplorer.msg.no-node-history",
                                                null, ApplicationMessage.WARNING)) ;
      }
    }
  }
  
  static public class ChangeNodeActionListener extends EventListener<UIAddressBar> {
    public void execute(Event<UIAddressBar> event) throws Exception {
      UIAddressBar uiAddress = event.getSource() ;
      String path = uiAddress.getUIStringInput(FIELD_ADDRESS).getValue() ;
      UIJCRExplorer uiExplorer = uiAddress.getAncestorOfType(UIJCRExplorer.class) ;
      try {
        uiExplorer.setSelectNode(path, uiExplorer.getSession()) ;
        uiExplorer.updateAjax(event) ;
      } catch(Exception e) {
        UIApplication uiApp = uiAddress.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAddressBar.msg.path-not-found", null)) ;
        return ;
      }
    }
  }
  
  static public class HistoryActionListener extends EventListener<UIAddressBar> {
    public void execute(Event<UIAddressBar> event) throws Exception {
      UIAddressBar uiAddressBar = event.getSource() ;
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRExplorer uiExplorer = uiAddressBar.getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.setSelectNode(path, uiExplorer.getSession()) ;
      uiExplorer.refreshExplorer() ;
    }
  }
}
