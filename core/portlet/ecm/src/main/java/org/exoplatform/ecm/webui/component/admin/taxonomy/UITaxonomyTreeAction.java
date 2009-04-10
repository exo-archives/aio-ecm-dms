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
package org.exoplatform.ecm.webui.component.admin.taxonomy;

import org.exoplatform.ecm.webui.form.UIFormInputSetWithAction;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Hoang Van Hung
 *          hunghvit@gmail.com
 * Apr 3, 2009  
 */
public class UITaxonomyTreeAction extends UIFormInputSetWithAction {
  public static final String FIELD_NAME       = "name";

  public static final String FIELD_WORKSPACE  = "workspace";

  public static final String FIELD_HOMEPATH   = "homePath";

  public static final String FIELD_PERMISSION = "permissions";
  
  public UITaxonomyTreeAction(String name) throws Exception {
    super(name);
    addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null).addValidator(MandatoryValidator.class));
    //addUIFormInput(new UIFormSelectBox(FIELD_WORKSPACE, FIELD_WORKSPACE, null));
    UIFormStringInput homePathField = new UIFormStringInput(FIELD_HOMEPATH, FIELD_HOMEPATH, null);
    homePathField.setEditable(false);
    addUIFormInput(homePathField);
    UIFormStringInput permissonSelectField = new UIFormStringInput(FIELD_PERMISSION, FIELD_PERMISSION, null);
    permissonSelectField.addValidator(MandatoryValidator.class);
    permissonSelectField.setEditable(false);
    addUIFormInput(permissonSelectField);
    setActionInfo(FIELD_PERMISSION, new String[] {"AddPermission"});
    setActionInfo(FIELD_HOMEPATH, new String[] {"AddPath"});
  }
}
