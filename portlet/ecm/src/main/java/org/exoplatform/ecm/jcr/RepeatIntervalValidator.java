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
 * Aug 21, 2007 6:25:58 PM
 */
public class RepeatIntervalValidator implements Validator {

  public void validate(UIFormInput uiInput) throws Exception {
    try {
      long timeInterval = Long.parseLong(uiInput.getValue().toString()) ;
      new SimpleTrigger().setRepeatInterval(timeInterval) ;
    } catch(Exception e) {
      throw new MessageException(
          new ApplicationMessage("RepeatIntervalValidator.msg.invalid-value", null, ApplicationMessage.WARNING)) ;
    }    
  }
}
