/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer;

import javax.portlet.PortletPreferences;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SARL Author : Hoang Van Hung hunghvit@gmail.com
 * Mar 7, 2009
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl", events = {
    @EventConfig(phase = Phase.DECODE, listeners = UIJcrExplorerEditForm.SaveActionListener.class),
    @EventConfig(phase = Phase.DECODE, listeners = UIJcrExplorerEditForm.EditActionListener.class),
    @EventConfig(phase = Phase.DECODE, listeners = UIJcrExplorerEditForm.CancelActionListener.class)
})
    
    
public class UIJcrExplorerEditForm extends UIForm {
  private boolean flagSelectRender = false;

  public UIJcrExplorerEditForm() throws Exception {

    UIFormCheckBoxInput<Boolean> checkBoxCategory = new UIFormCheckBoxInput<Boolean>(
        UIJCRExplorerPortlet.CATEGORY_MANDATORY, null, null);
    checkBoxCategory.setChecked(Boolean.parseBoolean(getPreference().getValue(
        UIJCRExplorerPortlet.CATEGORY_MANDATORY, "")));
    checkBoxCategory.setEnable(false);
    addChild(checkBoxCategory);
    setActions(new String[] { "Edit" });
  }

  public boolean isFlagSelectRender() {
    return flagSelectRender;
  }

  public void setFlagSelectRender(boolean flagSelectRender) {
    this.flagSelectRender = flagSelectRender;
  }

  public void setEditable(boolean isEditable) {
    UIFormCheckBoxInput<Boolean> checkBoxCategory = getChildById(UIJCRExplorerPortlet.CATEGORY_MANDATORY);
    checkBoxCategory.setEnable(isEditable);
  }

  private PortletPreferences getPreference() {
    PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext
        .getCurrentInstance();
    return pcontext.getRequest().getPreferences();
  }

  public static class EditActionListener extends EventListener<UIJcrExplorerEditForm> {
    public void execute(Event<UIJcrExplorerEditForm> event) throws Exception {
      UIJcrExplorerEditForm uiForm = event.getSource();
      uiForm.setEditable(true);
      uiForm.setActions(new String[] { "Save", "Cancel" });
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }

  public static class CancelActionListener extends EventListener<UIJcrExplorerEditForm> {
    public void execute(Event<UIJcrExplorerEditForm> event) throws Exception {
      UIJcrExplorerEditForm uiForm = event.getSource();
      PortletPreferences pref = uiForm.getPreference();
      UIFormCheckBoxInput<Boolean> checkBoxCategory = uiForm
          .getChildById(UIJCRExplorerPortlet.CATEGORY_MANDATORY);
      checkBoxCategory.setChecked(Boolean.parseBoolean(pref.getValue(
          UIJCRExplorerPortlet.CATEGORY_MANDATORY, "")));
      uiForm.setEditable(false);
      uiForm.setActions(new String[] { "Edit" });
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }

  public static class SaveActionListener extends EventListener<UIJcrExplorerEditForm> {
    public void execute(Event<UIJcrExplorerEditForm> event) throws Exception {
      UIJcrExplorerEditForm uiForm = event.getSource();
      PortletPreferences pref = uiForm.getPreference();
      UIFormCheckBoxInput<Boolean> checkBoxCategory = uiForm
          .getChildById(UIJCRExplorerPortlet.CATEGORY_MANDATORY);
      pref.setValue(UIJCRExplorerPortlet.CATEGORY_MANDATORY, String.valueOf(checkBoxCategory
          .isChecked()));
      pref.store();
      uiForm.setEditable(false);
      uiForm.setActions(new String[] { "Edit" });
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }
}
