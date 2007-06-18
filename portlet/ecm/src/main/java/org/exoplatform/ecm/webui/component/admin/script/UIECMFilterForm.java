/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.script;

import java.util.ArrayList;
import java.util.List;

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
 * Author : pham tuan
 * phamtuanchip@yahoo.de September 27, 2006 10:27:15 AM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/component/admin/script/UIECMFilterForm.gtmpl",
    events = {@EventConfig(listeners = UIECMFilterForm.ChangeActionListener.class)}
)

public class UIECMFilterForm extends UIForm {
  final static public String FIELD_SELECT_SCRIPT = "selectScript" ;

  public UIECMFilterForm() throws Exception { 
    UIFormSelectBox scriptSelect = 
      new UIFormSelectBox(FIELD_SELECT_SCRIPT, FIELD_SELECT_SCRIPT, new ArrayList <SelectItemOption<String>>()) ;
    scriptSelect.setOnChange("Change") ;
    addUIFormInput(scriptSelect) ;
  }
  
  public void setOptions(List <SelectItemOption<String>> options) {
    getUIFormSelectBox(FIELD_SELECT_SCRIPT).setOptions(options) ;
  }

  static public class ChangeActionListener extends EventListener<UIECMFilterForm> {
    public void execute(Event<UIECMFilterForm> event) throws Exception {
      UIECMFilterForm uiForm = event.getSource() ;
      UIECMScripts uiECMScripts = uiForm.getParent() ;
      UIScriptList uiScriptList = uiECMScripts.getChildById(UIECMScripts.SCRIPTLIST_NAME) ;
      String categoryName = uiForm.getUIFormSelectBox(FIELD_SELECT_SCRIPT).getValue() ;
      uiScriptList.updateGrid(uiECMScripts.getECMScript(categoryName)) ;
      UIScriptManager sManager = uiForm.getAncestorOfType(UIScriptManager.class) ;
      sManager.setRenderedChild(UIECMScripts.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiECMScripts) ;
    }
  }
}