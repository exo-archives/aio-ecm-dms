/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.ecm.webui.component.explorer.popup.admin;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@yahoo.de
 * September 17, 2006
 * 10:07:15 AM
 */
@ComponentConfig(
    template = "system:/groovy/webui/core/UITabPane.gtmpl",
    events = {
        @EventConfig(listeners = UIPropertiesManager.ChangeTabActionListener.class)
    }
)

public class UIPropertiesManager extends UIContainer implements UIPopupComponent {
  
  public UIPropertiesManager() throws Exception {
    addChild(UIPropertyTab.class, null, null)  ;
    addChild(UIPropertyForm.class, null, null).setRendered(false) ;
  }
  
  public void activate() throws Exception {
  }
  
  public void deActivate() throws Exception {}
  public void setLockForm(boolean isLockForm) {
    getChild(UIPropertyForm.class).lockForm(isLockForm) ;
  }
  
  @SuppressWarnings("unused")
  static public class ChangeTabActionListener extends EventListener<UIPropertiesManager> {
    public void execute(Event<UIPropertiesManager> event) throws Exception {
    }
  }
}