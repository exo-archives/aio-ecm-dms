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
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jan 16, 2007  
 * 11:23:26 AM
 */
@ComponentConfig(template = "system:groovy/webui/core/UITabPane.gtmpl")
public class UIMultiLanguageManager extends UIContainer implements UIPopupComponent {

  public UIMultiLanguageManager() throws Exception {
    addChild(UIMultiLanguageForm.class, null, null) ;
    addChild(UIAddLanguageContainer.class, null, null).setRendered(false) ;
  }

  public void activate() throws Exception {
    UIMultiLanguageForm uiForm = getChild(UIMultiLanguageForm.class) ;
    uiForm.updateSelect(getAncestorOfType(UIJCRExplorer.class).getCurrentNode()) ;
  }
  public void deActivate() throws Exception {}

}
