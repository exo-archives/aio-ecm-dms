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
import org.exoplatform.ecm.webui.component.UIJCRBrowser;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 17, 2006
 * 10:41:44 AM 
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/explorer/UITabPaneWithAction.gtmpl",
    events = @EventConfig(listeners = UICategoryManager.CloseActionListener.class)
)
public class UICategoryManager extends UIContainer implements UIPopupComponent {

  final static public String[] ACTIONS = {"Close"} ;
  
  public UICategoryManager() throws Exception {
    addChild(UICategoriesAddedList.class, null, null) ;
    addChild(UIJCRBrowser.class, null, null).setRendered(false) ;
  }
  
  public String[] getActions() { return ACTIONS ; }

  static public class CloseActionListener extends EventListener<UICategoryManager> {
    public void execute(Event<UICategoryManager> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.setIsHidePopup(false) ;
      uiExplorer.cancelAction() ;
    }
  }

  public void activate() throws Exception { 
    getChild(UICategoriesAddedList.class).updateGrid();
  }

  public void deActivate() throws Exception { }
}
