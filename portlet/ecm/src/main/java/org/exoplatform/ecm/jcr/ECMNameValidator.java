/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.jcr;

import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIFormInput;
import org.exoplatform.webui.component.validator.Validator;
import org.exoplatform.webui.exception.MessageException;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Apr 11, 2007 5:05:25 PM
 */
public class ECMNameValidator implements Validator {

  public void validate(UIFormInput uiInput) throws Exception {
    String s = (String)uiInput.getValue();
    if(s == null || s.trim().length() == 0) {
      Object[] args = { uiInput.getName() };
      throw new MessageException(new ApplicationMessage("ECMNameValidator.msg.empty-input", args, ApplicationMessage.WARNING)) ;
    } 
    for(int i = 0; i < s.length(); i ++){
      char c = s.charAt(i);
      if(Character.isLetter(c) || Character.isDigit(c) || Character.isSpaceChar(c) || c=='_'
        || c=='-' || c=='.' || c==':' || c=='$' || c=='@' || c=='^' || c=='[' || c==']') {
        continue ;
      }
      Object[] args = { uiInput.getName(), s };
      throw new MessageException(new ApplicationMessage("ECMNameValidator.msg.Invalid-char", args, ApplicationMessage.WARNING)) ;
    }
  }
}
