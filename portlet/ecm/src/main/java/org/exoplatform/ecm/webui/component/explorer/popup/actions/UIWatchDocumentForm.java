/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.cms.watch.WatchDocumentService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jan 10, 2007  
 * 2:34:12 PM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl",
    events = {
      @EventConfig(listeners = UIWatchDocumentForm.WatchActionListener.class), 
      @EventConfig(listeners = UIWatchDocumentForm.CancelActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIWatchDocumentForm.UnwatchActionListener.class, phase = Phase.DECODE)
    }
)
public class UIWatchDocumentForm extends UIForm implements UIPopupComponent {

  final static public String NOTIFICATION_TYPE = "notificationType" ;
  final static public String NOTIFICATION_BY_EMAIL = "Email" ;
  final static public String NOTIFICATION_BY_RSS = "RSS" ;
  
  private Node watchNode_ = null ;
  
  public UIWatchDocumentForm() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    UIFormSelectBox uiSelectBox = new UIFormSelectBox(NOTIFICATION_TYPE, NOTIFICATION_TYPE, options) ;
    addUIFormInput(uiSelectBox) ;
  }
  
  public Node getWatchNode() { return watchNode_ ; }
  public void setWatchNode(Node node) { watchNode_ = node ; }
  
  public String getUserName() { 
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    return context.getRemoteUser() ; 
  }
  
  public boolean isWatching() throws Exception{   
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;    
    WatchDocumentService watchService = getApplicationComponent(WatchDocumentService.class) ;
    int notifyType = watchService.getNotificationType(getWatchNode(),getUserName()) ;    
    if(notifyType == WatchDocumentService.FULL_NOTIFICATION) {
      options.add(new SelectItemOption<String>(NOTIFICATION_BY_EMAIL,NOTIFICATION_BY_EMAIL)) ;
      options.add(new SelectItemOption<String>(NOTIFICATION_BY_RSS,NOTIFICATION_BY_RSS)) ;
      getUIFormSelectBox(NOTIFICATION_TYPE).setOptions(options) ;
      return true ;
    } else if(notifyType == WatchDocumentService.NOTIFICATION_BY_EMAIL ) {
      options.add(new SelectItemOption<String>(NOTIFICATION_BY_EMAIL,NOTIFICATION_BY_EMAIL)) ;
      getUIFormSelectBox(NOTIFICATION_TYPE).setOptions(options) ;
      return true ;
    } else if(notifyType == WatchDocumentService.NOTIFICATION_BY_RSS) {
      options.add(new SelectItemOption<String>(NOTIFICATION_BY_RSS,NOTIFICATION_BY_RSS)) ;
      getUIFormSelectBox(NOTIFICATION_TYPE).setOptions(options) ;
      return true ; 
    } else {
      options.add(new SelectItemOption<String>(NOTIFICATION_BY_EMAIL,NOTIFICATION_BY_EMAIL)) ;
      options.add(new SelectItemOption<String>(NOTIFICATION_BY_RSS,NOTIFICATION_BY_RSS)) ;
      getUIFormSelectBox(NOTIFICATION_TYPE).setOptions(options) ;
      return false ;
    }          
  }

  public int getNotifyType() throws Exception {
    WatchDocumentService watchService = getApplicationComponent(WatchDocumentService.class) ;
    return watchService.getNotificationType(getWatchNode(), getUserName()) ;
  }

  public void activate() throws Exception {
    setWatchNode(getAncestorOfType(UIJCRExplorer.class).getCurrentNode()) ;
    if(!isWatching()) setActions(new String[] {"Watch", "Cancel"}) ;
    else setActions(new String[] {"Unwatch", "Cancel"}) ;
  }
  
  public void deActivate() throws Exception {
  }
  
  static  public class CancelActionListener extends EventListener<UIWatchDocumentForm> {
    public void execute(Event<UIWatchDocumentForm> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;
    }
  }

  static  public class WatchActionListener extends EventListener<UIWatchDocumentForm> {
    public void execute(Event<UIWatchDocumentForm> event) throws Exception {
      UIWatchDocumentForm uiForm = event.getSource() ;
      String notifyType = uiForm.getUIFormSelectBox(NOTIFICATION_TYPE).getValue() ;
      WatchDocumentService watchService = uiForm.getApplicationComponent(WatchDocumentService.class) ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if(notifyType.equalsIgnoreCase(NOTIFICATION_BY_EMAIL)) {
        watchService.watchDocument(uiForm.getWatchNode(), uiForm.getUserName(), WatchDocumentService.NOTIFICATION_BY_EMAIL) ;
      } else if(notifyType.equalsIgnoreCase(NOTIFICATION_BY_RSS)) {
        watchService.watchDocument(uiForm.getWatchNode(), uiForm.getUserName(), WatchDocumentService.NOTIFICATION_BY_RSS) ;
      } else {
        uiApp.addMessage(new ApplicationMessage("UIWatchDocumentForm.msg.not-support", null)) ;
        return ;
      }
      uiApp.addMessage(new ApplicationMessage("UIWatchDocumentForm.msg.watching-successfully", null)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp) ;
      uiForm.getAncestorOfType(UIJCRExplorer.class).updateAjax(event) ;
    }
  }
  
  static  public class UnwatchActionListener extends EventListener<UIWatchDocumentForm> {
    public void execute(Event<UIWatchDocumentForm> event) throws Exception {
      UIWatchDocumentForm uiForm = event.getSource() ;
      String notifyType = uiForm.getUIFormSelectBox(NOTIFICATION_TYPE).getValue() ;
      WatchDocumentService watchService = uiForm.getApplicationComponent(WatchDocumentService.class) ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if(notifyType.equalsIgnoreCase(NOTIFICATION_BY_EMAIL)) {
        watchService.unwatchDocument(uiForm.getWatchNode(), uiForm.getUserName(), WatchDocumentService.NOTIFICATION_BY_EMAIL) ;
      } else if(notifyType.equalsIgnoreCase(NOTIFICATION_BY_RSS)) {
        watchService.unwatchDocument(uiForm.getWatchNode(), uiForm.getUserName(), WatchDocumentService.NOTIFICATION_BY_RSS) ;
      } else {
        uiApp.addMessage(new ApplicationMessage("UIWatchDocumentForm.msg.not-support", null)) ;
        return ;
      }
      uiApp.addMessage(new ApplicationMessage("UIWatchDocumentForm.msg.unwatching-successfully", null)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp) ;
      uiForm.getAncestorOfType(UIJCRExplorer.class).updateAjax(event) ;
    }
  }
}
