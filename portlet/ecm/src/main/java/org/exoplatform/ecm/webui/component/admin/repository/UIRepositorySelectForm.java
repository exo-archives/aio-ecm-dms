/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.repository;

import java.util.List;

import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Jun 2, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/component/admin/UIRepositorySelectForm.gtmpl",
    events = {@EventConfig(listeners = UIRepositorySelectForm.OnchangeActionListener.class)}
)

public class UIRepositorySelectForm extends UIForm {
  final public static String FIELD_SELECTREPO = "selectRepo" ; 
  public UIRepositorySelectForm() {
    addChild(new UIFormSelectBox(FIELD_SELECTREPO, FIELD_SELECTREPO, null)) ;
  }

  protected void setOptionValue(List<SelectItemOption<String>> list){
    getUIFormSelectBox(FIELD_SELECTREPO).setOptions(list) ; 
  }
  
  protected void setActionEvent(){
    getUIFormSelectBox(FIELD_SELECTREPO).setOnChange("Onchange") ;
  }
  
  protected String getSelectedValue() {    
    return getUIFormSelectBox(FIELD_SELECTREPO).getValue() ;
  }

  protected void setSelectedValue(String value) {    
    getUIFormSelectBox(FIELD_SELECTREPO).setValue(value) ;
  }  

  public static class OnchangeActionListener extends EventListener<UIRepositorySelectForm>{
    public void execute(Event<UIRepositorySelectForm> event) throws Exception {
      UIRepositorySelectForm uiForm = event.getSource() ;
      UIRepositoryControl uiControl = uiForm.getAncestorOfType(UIRepositoryControl.class) ;
      PortletPreferences portletPref = uiForm.getAncestorOfType(UIECMAdminPortlet.class).getPortletPreferences() ;
      portletPref.setValue(Utils.REPOSITORY, uiForm.getSelectedValue()) ;
      portletPref.store() ;
      uiForm.setOptionValue(uiControl.getRepoItem(true, uiForm.getApplicationComponent(RepositoryService.class))) ;
      uiForm.getAncestorOfType(UIECMAdminPortlet.class).initChilds() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiControl.getAncestorOfType(UIECMAdminPortlet.class)) ;
    }
  }
}
