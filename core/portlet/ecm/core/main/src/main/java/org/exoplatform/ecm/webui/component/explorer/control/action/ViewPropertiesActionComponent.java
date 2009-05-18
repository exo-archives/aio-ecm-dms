/*
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
 */
package org.exoplatform.ecm.webui.component.explorer.control.action;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.control.listener.UIActionBarActionListener;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIPropertiesManager;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIPropertyForm;
import org.exoplatform.ecm.webui.utils.PermissionUtil;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.event.Event;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          nicolas.filotto@exoplatform.com
 * 6 mai 2009  
 */
@ComponentConfig(
     events = {
       @EventConfig(listeners = ViewPropertiesActionComponent.ViewPropertiesActionListener.class)
     }
 )
public class ViewPropertiesActionComponent extends UIComponent {
  
  public static class ViewPropertiesActionListener extends UIActionBarActionListener<ViewPropertiesActionComponent> {
    public void processEvent(Event<ViewPropertiesActionComponent> event) throws Exception {
      UIJCRExplorer uiJCRExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      Node node = uiJCRExplorer.getCurrentNode();
      UIPropertiesManager uiPropertiesManager = 
        uiJCRExplorer.createUIComponent(UIPropertiesManager.class, null, null);
      UIPropertyForm uiForm = uiPropertiesManager.getChild(UIPropertyForm.class);
      uiForm.setRepositoryName(uiJCRExplorer.getRepositoryName());
      uiForm.getUIFormSelectBox(UIPropertyForm.FIELD_NAMESPACE).setOptions(uiForm.getNamespaces());
      UIPopupContainer UIPopupContainer = uiJCRExplorer.getChild(UIPopupContainer.class);
      UIPopupContainer.activate(uiPropertiesManager, 700, 0);
      if(uiJCRExplorer.nodeIsLocked(node)){
        uiPropertiesManager.setLockForm(true);
      } else {
        uiPropertiesManager.setLockForm(!PermissionUtil.canSetProperty(node));
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    }
  }
}