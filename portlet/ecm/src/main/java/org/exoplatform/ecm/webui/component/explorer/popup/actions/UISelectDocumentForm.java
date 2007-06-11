/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.webui.bean.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Nov 8, 2006 10:06:40 AM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/component/explorer/UIFormWithoutAction.gtmpl",
    events = {
      @EventConfig(listeners = UISelectDocumentForm.ChangeActionListener.class)
    }
)
public class UISelectDocumentForm extends UIForm {

  final static public String FIELD_SELECT = "selectTemplate" ;

  public UISelectDocumentForm() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    UIFormSelectBox templateSelect = new UIFormSelectBox(FIELD_SELECT, FIELD_SELECT, options) ;
    templateSelect.setOnChange("Change") ;  
    addUIFormInput(templateSelect) ;
  }

  static public class ChangeActionListener extends EventListener<UISelectDocumentForm> {
    public void execute(Event<UISelectDocumentForm> event) throws Exception {
      UISelectDocumentForm uiSelectForm = event.getSource() ;
      UIDocumentFormController uiDCFormController = uiSelectForm.getParent() ;
      UIDocumentForm documentForm = uiDCFormController.getChild(UIDocumentForm.class) ;
      documentForm.getChildren().clear() ;
      documentForm.resetProperties() ;
      // set path to DocumentForm
      documentForm.setTemplateNode(uiSelectForm.getUIFormSelectBox(UISelectDocumentForm.FIELD_SELECT).getValue()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDCFormController) ;
    }
  }

  public String getSelectValue() {
    return getUIFormSelectBox(FIELD_SELECT).getValue();
  }
}
