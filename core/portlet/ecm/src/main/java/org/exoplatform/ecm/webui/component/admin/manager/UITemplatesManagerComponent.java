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
package org.exoplatform.ecm.webui.component.admin.manager;

import javax.jcr.AccessDeniedException;

import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminWorkingArea;
import org.exoplatform.ecm.webui.component.admin.listener.UIECMAdminControlPanelActionListener;
import org.exoplatform.ecm.webui.component.admin.templates.UITemplatesManager;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.exception.MessageException;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          nicolas.filotto@exoplatform.com
 * 15 mai 2009  
 */
@ComponentConfig(
     events = {
       @EventConfig(listeners = UITemplatesManagerComponent.UITemplatesManagerActionListener.class)
     }
 )
public class UITemplatesManagerComponent extends UIAbstractManagerComponent {

  public static class UITemplatesManagerActionListener extends UIECMAdminControlPanelActionListener<UITemplatesManagerComponent> {
    public void processEvent(Event<UITemplatesManagerComponent> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getAncestorOfType(UIECMAdminPortlet.class);
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      try {
        uiWorkingArea.getChild(UITemplatesManager.class).refresh() ;
        uiWorkingArea.setChild(UITemplatesManager.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
      } catch(AccessDeniedException ace) {
        throw new MessageException(new ApplicationMessage("UIECMAdminControlPanel.msg.access-denied", 
                                                          null, ApplicationMessage.WARNING)) ;
      }
    }
  }  

  @Override
  public Class<? extends UIAbstractManager> getUIAbstractManagerClass() {
    return UITemplatesManager.class;
  }    
}
