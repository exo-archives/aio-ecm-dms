/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.info;

import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmptyFieldValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Jun 28, 2006
 */
@ComponentConfig(template = "app:/groovy/webui/component/UIFormInputSetWithAction.gtmpl")
public class UIPermissionInputSet extends UIFormInputSetWithAction {
  
  final static public String FIELD_USERORGROUP = "userOrGroup" ;
  

  public UIPermissionInputSet(String name) throws Exception {
    super(name);
    setComponentConfig(getClass(), null) ;
    UIFormStringInput userGroup = new UIFormStringInput(FIELD_USERORGROUP, FIELD_USERORGROUP, null) ;
    userGroup.addValidator(EmptyFieldValidator.class) ;   
    userGroup.setEditable(false) ;
    addUIFormInput(userGroup) ;
    for (String perm : PermissionType.ALL) {
      addUIFormInput(new UIFormCheckBoxInput<String>(perm, perm, null)) ;
    }
    setActionInfo(FIELD_USERORGROUP, new String[] {"SelectUser", "SelectMember", "AddAny"}) ;
  }
}