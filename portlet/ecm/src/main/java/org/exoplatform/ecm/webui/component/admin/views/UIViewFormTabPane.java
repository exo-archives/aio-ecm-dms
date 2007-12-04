/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.views;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormTabPane;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@exoplatform.com
 * Sep 19, 2006
 * 5:31:04 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIFormTabPane.gtmpl",
    events = {
      @EventConfig(listeners = UIViewFormTabPane.SaveActionListener.class),
      @EventConfig(listeners = UIViewFormTabPane.ResetActionListener.class),
      @EventConfig(listeners = UIViewFormTabPane.EditTabActionListener.class),
      @EventConfig(listeners = UIViewFormTabPane.DeleteTabActionListener.class),
      @EventConfig(listeners = UIViewFormTabPane.RestoreActionListener.class),
      @EventConfig(listeners = UIViewFormTabPane.CancelActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIViewFormTabPane.CloseActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIViewForm.AddPermissionActionListener.class, phase = Phase.DECODE)
    }
)
public class UIViewFormTabPane extends UIFormTabPane {  

  final static public String POPUP_PERMISSION = "PopupViewPermission" ;
  
  private UIViewForm uiViewForm ;
  private UITabForm uiTabForm ;

  public UIViewFormTabPane() throws Exception {
    super("UIViewFormTabPane") ;

    uiViewForm = new UIViewForm("UIViewForm") ;
    addUIComponentInput(uiViewForm) ;
    
    uiTabForm = new UITabForm("UITabForm") ;
    addUIComponentInput(uiTabForm) ;
    setSelectedTab(uiViewForm.getId()) ;
    setActions(new String[]{}) ;
  }
  
  public String getLabel(ResourceBundle res, String id)  {
    try {
      return res.getString("UIViewForm.label." + id) ;
    } catch (MissingResourceException ex) {
      return id ;
    }
  }
  
  static  public class SaveActionListener extends EventListener<UIViewFormTabPane> {
    public void execute(Event<UIViewFormTabPane> event) throws Exception {
      UIViewFormTabPane uiViewTabPane = event.getSource();
      UIViewContainer uiViewContainer = uiViewTabPane.getAncestorOfType(UIViewContainer.class) ;
      if(uiViewTabPane.getSelectedTabId().equalsIgnoreCase("UIViewForm")) {
        uiViewTabPane.uiViewForm.save() ;
        uiViewContainer.removeChild(UIPopupWindow.class) ;
      } else {
        uiViewTabPane.uiTabForm.save() ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
    }
  }

  static  public class CancelActionListener extends EventListener<UIViewFormTabPane> {
    public void execute(Event<UIViewFormTabPane> event) throws Exception {
      UIViewFormTabPane uiViewTabPane = event.getSource();      
      uiViewTabPane.uiTabForm.refresh(true) ;
      uiViewTabPane.uiViewForm.refresh(true) ;
      uiViewTabPane.removeChildById(POPUP_PERMISSION) ;
      UIViewContainer uiViewContainer = uiViewTabPane.getAncestorOfType(UIViewContainer.class) ;
      uiViewContainer.removeChild(UIPopupWindow.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
    }
  }
  
  static  public class CloseActionListener extends EventListener<UIViewFormTabPane> {
    public void execute(Event<UIViewFormTabPane> event) throws Exception {
      UIViewFormTabPane uiViewTabPane = event.getSource();      
      uiViewTabPane.uiTabForm.refresh(true) ;
      uiViewTabPane.uiViewForm.refresh(true) ;
      uiViewTabPane.removeChildById(POPUP_PERMISSION) ;
      UIViewContainer uiViewContainer = uiViewTabPane.getAncestorOfType(UIViewContainer.class) ;
      uiViewContainer.removeChild(UIPopupWindow.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
    }
  }
  
  static  public class ResetActionListener extends EventListener<UIViewFormTabPane> {
    public void execute(Event<UIViewFormTabPane> event) throws Exception {
      UIViewFormTabPane uiViewTabPane = event.getSource();
      uiViewTabPane.uiTabForm.refresh(true) ;
      if(uiViewTabPane.getSelectedTabId().equalsIgnoreCase("UIViewForm")) {
        uiViewTabPane.uiViewForm.revertVersion() ;
        uiViewTabPane.uiViewForm.refresh(true) ;
        uiViewTabPane.setRenderedChild(UIViewForm.class) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewTabPane.getParent()) ;
    }
  }

  static  public class EditTabActionListener extends EventListener<UIViewFormTabPane> {
    public void execute(Event<UIViewFormTabPane> event) throws Exception {
      UIViewFormTabPane uiViewTabPane = event.getSource();
      String tabName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiViewTabPane.setSelectedTab("UITabForm") ;
      uiViewTabPane.uiViewForm.editTab(tabName) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewTabPane.getParent()) ;
    }
  }
  
  static  public class DeleteTabActionListener extends EventListener<UIViewFormTabPane> {
    public void execute(Event<UIViewFormTabPane> event) throws Exception {
      UIViewFormTabPane uiViewTabPane = event.getSource();
      String tabName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiViewTabPane.setSelectedTab("UIViewForm") ;
      uiViewTabPane.uiViewForm.deleteTab(tabName) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewTabPane.getParent()) ;
    }
  }

  static  public class RestoreActionListener extends EventListener<UIViewFormTabPane> {
    public void execute(Event<UIViewFormTabPane> event) throws Exception {
      UIViewFormTabPane uiViewTabPane = event.getSource();
      uiViewTabPane.uiViewForm.changeVersion() ;
      UIViewContainer uiContainer = uiViewTabPane.getAncestorOfType(UIViewContainer.class) ;
      UIViewList uiViewList = uiContainer.findFirstComponentOfType(UIViewList.class) ;
      uiViewList.updateViewListGrid() ;
      uiViewTabPane.uiTabForm.refresh(true) ;
      uiViewTabPane.uiViewForm.refresh(true) ;
      uiViewTabPane.removeChildById(POPUP_PERMISSION) ;
      UIViewContainer uiViewContainer = uiViewTabPane.getAncestorOfType(UIViewContainer.class) ;
      uiViewContainer.removeChild(UIPopupWindow.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
    }
  }
}