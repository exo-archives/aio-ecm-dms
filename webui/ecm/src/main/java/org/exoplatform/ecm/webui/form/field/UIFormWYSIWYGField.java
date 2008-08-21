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
package org.exoplatform.ecm.webui.form.field;

import org.exoplatform.ecm.webui.form.DialogFormField;
import org.exoplatform.ecm.webui.utils.DialogFormUtil;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 23, 2008  
 */
public class UIFormWYSIWYGField extends DialogFormField {

  public UIFormWYSIWYGField(String name, String label, String[] arguments) {
    super(name, label, arguments);
  }
  
  @SuppressWarnings("unchecked")
  public <T extends UIFormInputBase> T createUIFormInput() throws Exception {
    boolean useBasicToolBar;
    if("basic".equals(options)) 
      useBasicToolBar = true ;
    else 
      useBasicToolBar = false;      
    UIFormWYSIWYGInput wysiwyg = new UIFormWYSIWYGInput(name, name, defaultValue, useBasicToolBar) ;
    if(validateType != null) {
      DialogFormUtil.addValidators(wysiwyg, validateType);
    }    
    return (T)wysiwyg;
  }
  
}
