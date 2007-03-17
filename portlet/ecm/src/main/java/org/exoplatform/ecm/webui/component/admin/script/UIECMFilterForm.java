/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.script;

import java.util.List;

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
 * Author : pham tuan
 * phamtuanchip@yahoo.de September 27, 2006 10:27:15 AM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/component/admin/script/UIECMFilterForm.gtmpl",
    events = {@EventConfig(phase=Phase.DECODE, listeners = UIECMFilterForm.ChangeActionListener.class)}
)

public class UIECMFilterForm extends UIForm {
  final static public String FIELD_SELECT_SCRIPT = "selectScript" ;

  public UIECMFilterForm() throws Exception { 
    UIFormSelectBox scriptSelect = 
      new UIFormSelectBox(FIELD_SELECT_SCRIPT, FIELD_SELECT_SCRIPT, null) ;
    scriptSelect.setOnChange("Change") ;
    addUIFormInput(scriptSelect) ;
  }

  public void setOptions(List <SelectItemOption<String>> options) {
    getUIFormSelectBox(FIELD_SELECT_SCRIPT).setOptions(options) ;
    String category = getUIFormSelectBox(FIELD_SELECT_SCRIPT).getValue() ;
    if(category == null) getUIFormSelectBox(FIELD_SELECT_SCRIPT).setValue(options.get(0).getLabel());
    else getUIFormSelectBox(FIELD_SELECT_SCRIPT).setValue(category) ;    
  }

  static public class ChangeActionListener extends EventListener<UIECMFilterForm> {
    public void execute(Event<UIECMFilterForm> event) throws Exception {
      UIECMFilterForm uiForm = event.getSource() ;
      UIECMScripts uiECMScripts = uiForm.getParent() ;
      UIScriptList uiScriptList = uiECMScripts.getChildById(UIECMScripts.SCRIPTLIST_NAME) ;
      String categoryName = uiForm.getUIFormSelectBox(FIELD_SELECT_SCRIPT).getValue() ;
      uiScriptList.updateGrid(uiECMScripts.getECMScript(categoryName)) ;
      uiECMScripts.refresh() ;
      uiECMScripts.setRendered(true) ;
      UIScriptManager sManager = uiForm.getAncestorOfType(UIScriptManager.class) ;
      sManager.getChild(UICBScripts.class).setRendered(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiECMScripts) ;
    }
  }
}