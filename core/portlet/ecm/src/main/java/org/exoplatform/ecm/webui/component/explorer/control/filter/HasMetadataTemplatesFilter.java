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
package org.exoplatform.ecm.webui.component.explorer.control.filter;

import java.util.Hashtable;
import java.util.Map;

import org.exoplatform.ecm.webui.component.explorer.control.UIActionBar;
import org.exoplatform.webui.ext.UIExtensionAbstractFilter;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          nicolas.filotto@exoplatform.com
 * 6 mai 2009  
 */
public class HasMetadataTemplatesFilter extends UIExtensionAbstractFilter {

  public boolean accept(Map<String, Object> context) throws Exception {
    UIActionBar uiActionBar = (UIActionBar) context.get(UIActionBar.class.getName());
    Hashtable<String, String> metaDataTemp = uiActionBar.getMetadataTemplates();
    return !metaDataTemp.isEmpty();
  }

  public void onDeny(Map<String, Object> context) throws Exception {
    createUIPopupMessages(context, "UIViewMetadataContainer.msg.path-not-found");
  }    
}