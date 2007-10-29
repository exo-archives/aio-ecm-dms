/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.control;

import java.util.List;

import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentWorkspace;
import org.exoplatform.ecm.webui.component.explorer.UIDrivesBrowser;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.search.UISearchResult;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Aug 2, 2006
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/explorer/control/UIViewBar.gtmpl",
    events = {
      @EventConfig(listeners = UIViewBar.PreferencesActionListener.class),
      @EventConfig(listeners = UIViewBar.BackActionListener.class),
      @EventConfig(listeners = UIViewBar.SaveSessionActionListener.class),
      @EventConfig(listeners = UIViewBar.RefreshSessionActionListener.class),
      @EventConfig(listeners = UIViewBar.ChangeViewActionListener.class)
    }
)

public class UIViewBar extends UIForm {
  final static private String FIELD_SELECT_VIEW = "views" ;
  
  public UIViewBar() throws Exception {
    UIFormSelectBox selectView  = new UIFormSelectBox(FIELD_SELECT_VIEW, null, null) ;
    selectView.setOnChange("ChangeView") ;
    addChild(selectView) ;    
  }

  public void setViewOptions(List<SelectItemOption<String>> viewOptions) {
    getUIFormSelectBox(FIELD_SELECT_VIEW).setOptions(viewOptions) ;
    getUIFormSelectBox(FIELD_SELECT_VIEW).setValue(viewOptions.get(0).getValue()) ;
  }
  
  public String getWorkspaceName() {
    UIJCRExplorer uicomp = getAncestorOfType(UIJCRExplorer.class) ;
    PortletPreferences prefs_ = uicomp.getPortletPreferences();
    return prefs_.getValue(Utils.DRIVE, "") ;
  }  
  
  public boolean isShowSaveSession() throws Exception{
    UIJCRExplorer uiExplorer =  getAncestorOfType(UIJCRExplorer.class) ;
    return uiExplorer.getPreference().isJcrEnable() ;    
  }
  
  static public class BackActionListener extends EventListener<UIViewBar> {
    public void execute(Event<UIViewBar> event) throws Exception {
      UIJCRExplorer uiJCRExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      PortletPreferences prefs_ = uiJCRExplorer.getPortletPreferences();
      uiJCRExplorer.setIsViewTag(false) ;
      prefs_.setValue(Utils.WORKSPACE_NAME,"") ;
      prefs_.setValue(Utils.VIEWS,"") ;
      prefs_.setValue(Utils.JCR_PATH,"") ;
      prefs_.setValue(Utils.DRIVE,"") ;
      prefs_.store() ;
      UISearchResult simpleSearchResult = uiJCRExplorer.findComponentById(UIDocumentWorkspace.SIMPLE_SEARCH_RESULT);
      if(simpleSearchResult != null) simpleSearchResult.setRendered(false);
      uiJCRExplorer.setRenderSibbling(UIDrivesBrowser.class) ;
    }
  }  

  static public class PreferencesActionListener extends EventListener<UIViewBar> {
    public void execute(Event<UIViewBar> event) throws Exception {
      UIViewBar viewBar = event.getSource();
      UIJCRExplorer uiJCRExplorer = viewBar.getAncestorOfType(UIJCRExplorer.class);                                         
      UIPopupAction popupAction = uiJCRExplorer.getChild(UIPopupAction.class);
      UIPreferencesForm uiPrefForm = popupAction.activate(UIPreferencesForm.class,600) ;
      uiPrefForm.update(uiJCRExplorer.getPreference()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }  

  static public class SaveSessionActionListener extends EventListener<UIViewBar> {
    public void execute(Event<UIViewBar> event) throws Exception {
      UIJCRExplorer uiJCRExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiJCRExplorer.getSession().save() ;
      uiJCRExplorer.getSession().refresh(false) ;
      UIApplication uiApp = uiJCRExplorer.getAncestorOfType(UIApplication.class) ;
      String mess = "UIJCRExplorer.msg.save-session-success" ;
      uiApp.addMessage(new ApplicationMessage(mess, null, ApplicationMessage.INFO)) ;
    }
  }

  static public class RefreshSessionActionListener extends EventListener<UIViewBar> {
    public void execute(Event<UIViewBar> event) throws Exception {
      UIJCRExplorer uiJCRExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiJCRExplorer.getSession().refresh(false) ;
      uiJCRExplorer.refreshExplorer() ;
      UIControl uiControl = event.getSource().getParent() ;
      UIActionBar uiActionBar = uiControl.getChild(UIActionBar.class) ;
      String viewName = event.getSource().getUIFormSelectBox(FIELD_SELECT_VIEW).getValue() ;
      uiActionBar.setTabOptions(viewName) ;
      UIApplication uiApp = uiJCRExplorer.getAncestorOfType(UIApplication.class) ;
      String mess = "UIJCRExplorer.msg.refresh-session-success" ;
      uiApp.addMessage(new ApplicationMessage(mess, null, ApplicationMessage.INFO)) ;
    }
  }
  
  static public class ChangeViewActionListener extends EventListener<UIViewBar> {
    public void execute(Event<UIViewBar> event) throws Exception {
      UIViewBar uiViewBar = event.getSource();
      String viewName = uiViewBar.getUIFormSelectBox(FIELD_SELECT_VIEW).getValue() ;
      UIControl uiControl = uiViewBar.getParent() ;
      UIActionBar uiActionBar = uiControl.getChild(UIActionBar.class) ;
      uiActionBar.setTabOptions(viewName) ;
    }
  }
}
