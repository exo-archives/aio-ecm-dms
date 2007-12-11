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
