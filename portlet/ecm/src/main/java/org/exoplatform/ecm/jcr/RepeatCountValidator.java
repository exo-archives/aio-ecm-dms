/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.jcr;

import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.validator.Validator;
import org.quartz.SimpleTrigger;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Aug 21, 2007 3:38:21 PM
 */
public class RepeatCountValidator implements Validator {

  public void validate(UIFormInput uiInput) throws Exception {
    try {
      int repeatCount = Integer.parseInt(uiInput.getValue().toString()) ;
      new SimpleTrigger().setRepeatCount(repeatCount) ;
    } catch(Exception e) {
      throw new MessageException(
          new ApplicationMessage("RepeatCountValidator.msg.invalid-value", null, ApplicationMessage.WARNING)) ;
    }
    
  }

}
