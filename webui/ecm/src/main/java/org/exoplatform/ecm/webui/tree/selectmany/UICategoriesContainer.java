/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.ecm.webui.tree.selectmany;

import org.exoplatform.ecm.webui.popup.UIPopupComponent;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * Aug 11, 2008  
 */

@ComponentConfig(template = "system:/groovy/webui/core/UITabPane.gtmpl")

public class UICategoriesContainer extends UIContainer implements UIPopupComponent{
  public UICategoriesContainer() throws Exception {
    addChild(UICategoriesSelectPanel.class,null,null);
    addChild(UISelectedCategoriesGrid.class,null,null).setRendered(false);
  }
  
  public void activate() throws Exception {    
  }
  
  public void deActivate() throws Exception {
  }
}
