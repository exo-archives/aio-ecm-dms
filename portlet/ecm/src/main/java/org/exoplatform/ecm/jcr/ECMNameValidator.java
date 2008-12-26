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

import javax.jcr.NamespaceRegistry;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.validator.Validator;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Apr 11, 2007 5:05:25 PM
 */
public class ECMNameValidator implements Validator {

  public void validate(UIFormInput uiInput) throws Exception {
    String inputValue = ((String)uiInput.getValue()).trim();
    if (inputValue == null || inputValue.length() == 0) {
      throwException("ECMStandardPropertyNameValidator.msg.empty-input", uiInput);      
    }
    switch (inputValue.length()) {
    case 1:
      checkOneChar(inputValue, uiInput);
      break;      
    case 2:
      checkTwoChars(inputValue, uiInput);
    default:
      checkMoreChars(inputValue, uiInput);
      break;
    }
    
  }
  
  /**
   * 
   * @param s
   * @param array
   * @return
   */
//TODO: This method should use private instead of public
  public boolean checkArr(String s, String[] arrFilterChars) {
    for (String filter : arrFilterChars) {
      if (s.equals(filter)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Check String Input s if s.length() = 1
   * @param s
   * @param uiInput
   * @throws MessageException 
   */
//TODO: This method should use private instead of public
  public void checkOneChar(String s, UIFormInput uiInput) throws MessageException {
    String[] arrFilterChars = {".", "/", ":", "[", "]", "*", "'", "|", "\""} ;
    if (checkArr(s, arrFilterChars)) {
      throwException("ECMStandardPropertyNameValidator.msg.Invalid-char", uiInput);     
    }
  }
  
  /**
   * Check String Input s if s.length() = 2
   * @param s
   * @param uiInput
   */
//TODO: This method should use private instead of public
  public void checkTwoChars(String s, UIFormInput uiInput) throws MessageException {
    String s2 = "";
    if (s.startsWith(".")) {
      s2 = s.substring(1, 2);
      checkOneChar(s2, uiInput);
    } else if (s.endsWith(".")) {
      s2 = s.substring(0, 1);
      checkOneChar(s2, uiInput);
    } else {
      String s3 = s.substring(0, 1);
      String s4 = s.substring(1, 2);
      
      String[] arrFilterChars = {".", "/", ":", "[", "]", "*", "'", "|", "\""} ;      
      if (checkArr(s3, arrFilterChars)) {
        throwException("ECMStandardPropertyNameValidator.msg.Invalid-char", uiInput);       
      } else {
        if (checkArr(s4, arrFilterChars)) {
          throwException("ECMStandardPropertyNameValidator.msg.Invalid-char", uiInput);         
        }
      }
    }
  }
  
  /**
   * Check String Input s if s.length() > 2
   * @param s
   * @param uiInput
   */
//TODO: This method should use private instead of public
  public void checkMoreChars(String s, UIFormInput uiInput) throws MessageException,Exception {
    //check nonspace start and end char
    String[] arrFilterChars = {"/", ":", "[", "]", "*", "'", "|", "\""} ; 
    //get start and end char
    String s1 = s.substring(0, 1);
    String s2 = s.substring(s.length() - 1, s.length());
    if (checkArr(s1, arrFilterChars)) {
      throwException("ECMStandardPropertyNameValidator.msg.Invalid-char", uiInput);     
    } else if (checkArr(s2, arrFilterChars)){      
      throwException("ECMStandardPropertyNameValidator.msg.Invalid-char", uiInput); 
    } else {
        for(String filterChar : arrFilterChars) {
        if (s.indexOf(filterChar) > -1) {
        if (!filterChar.equals(":")) {
          throwException("ECMStandardPropertyNameValidator.msg.Invalid-char", uiInput);
        } else {            
          int i = s.indexOf(filterChar) ;
          String s4 = s.substring(0, i);
          if (!checkNamespace(s4))
            throwException("ECMStandardPropertyNameValidator.msg.Invalid-char", uiInput);
        }
        }
      }
    }
  } 
  
  private boolean checkNamespace(String namespace) throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    RepositoryService repositoryService = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);
    String repository = Utils.getRepository();
    NamespaceRegistry namespaceRegistry = repositoryService.getRepository(repository).getNamespaceRegistry();
    String[] prefixs = namespaceRegistry.getPrefixes();
    
    boolean in = false;
    for(int i = 0;i < prefixs.length - 1;i++){
      if(namespace.equals(prefixs[i])) {
        in = true;
      }
    }
    return in;
  }  
  
  private void throwException(String s, UIFormInput uiInput) throws MessageException {
    Object[] args = { uiInput.getName() };
    throw new MessageException(new ApplicationMessage(s, args, ApplicationMessage.WARNING));
  }
  
}
