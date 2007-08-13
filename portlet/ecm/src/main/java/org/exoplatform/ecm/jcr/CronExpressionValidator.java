/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.jcr;

import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.validator.Validator;
import org.quartz.CronTrigger;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Aug 13, 2007  
 */
public class CronExpressionValidator implements Validator{

  public void validate(UIFormInput uiInput) throws Exception {    
    try{
      new CronTrigger().setCronExpression((String)uiInput.getValue()) ;
    }catch (Exception e) {      
      throw new MessageException(new ApplicationMessage("CronExpressionValidator.invalid-input",null, ApplicationMessage.WARNING)) ;      
    }
  }

}
