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

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Hoang Van Hung
 *          hunghvit@gmail.com
 * Mar 7, 2009  
 */
@ComponentConfig(
    lifecycle = UIContainerLifecycle.class
)
public class UIJcrExplorerEditContainer extends UIContainer {
  
  public UIJcrExplorerEditContainer() throws Exception {
    addChild(UIJcrExplorerEditForm.class, null, null);
  }
  
  public UIPopupWindow initPopup(String id) throws Exception {
    removeChildById(id);
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, id);
    uiPopup.setWindowSize(700, 350);
    uiPopup.setShow(true);
    uiPopup.setResizable(true);
    return uiPopup;
  }
}