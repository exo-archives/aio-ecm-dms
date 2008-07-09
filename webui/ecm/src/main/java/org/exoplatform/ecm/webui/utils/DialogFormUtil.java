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
package org.exoplatform.ecm.webui.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;

import org.exoplatform.ecm.webui.form.validator.ECMNameValidator;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormUploadInput;
import org.exoplatform.webui.form.validator.DateTimeValidator;
import org.exoplatform.webui.form.validator.EmailAddressValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.NumberFormatValidator;

/*
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 23, 2008  
 */
/**
 * The Class DialogFormUtil.
 */
public class DialogFormUtil {

  /**
   * Prepare map.
   * 
   * @param inputs the inputs
   * @param properties the properties
   * @return the map< string, jcr input property>
   * @throws Exception the exception
   */
  public static Map<String, JcrInputProperty> prepareMap(List inputs, Map properties) throws Exception {
    Map<String, JcrInputProperty> rawinputs = new HashMap<String, JcrInputProperty>();
    HashMap<String, JcrInputProperty> hasMap = new HashMap<String, JcrInputProperty>() ;
    for (int i = 0; i < inputs.size(); i++) {
      JcrInputProperty property = null;
      if(inputs.get(i) instanceof UIFormMultiValueInputSet) {        
        String inputName = ((UIFormMultiValueInputSet)inputs.get(i)).getName() ;        
        if(!hasMap.containsKey(inputName)) {
          List<String> values = (List<String>) ((UIFormMultiValueInputSet)inputs.get(i)).getValue() ;
          property = (JcrInputProperty) properties.get(inputName);        
          if(property != null){          
            property.setValue(values.toArray(new String[values.size()])) ;
          }
        }
        hasMap.put(inputName, property) ;
      } else {
        UIFormInputBase input = (UIFormInputBase) inputs.get(i);
        property = (JcrInputProperty) properties.get(input.getName());
        if(property != null) {
          if (input instanceof UIFormUploadInput) {
            byte[] content = ((UIFormUploadInput) input).getUploadData() ; 
            property.setValue(content);
          } else if(input instanceof UIFormDateTimeInput) {
            property.setValue(((UIFormDateTimeInput)input).getCalendar()) ;
          } else {
            property.setValue(input.getValue()) ;
          }
        }
      }
    }
    Iterator iter = properties.values().iterator() ;
    JcrInputProperty property ;
    while (iter.hasNext()) {
      property = (JcrInputProperty) iter.next() ;
      rawinputs.put(property.getJcrPath(), property) ;
    }
    return rawinputs;
  }

  /**
   * Creates the form input.
   * 
   * @param type the type
   * @param name the name
   * @param label the label
   * @param validateType the validate type
   * @param valueType the value type
   * @return the t
   * @throws Exception the exception
   */
  public static <T extends UIFormInputBase> T createFormInput(Class<T> type,String name, String label, 
      String validateType, Class valueType) throws Exception {
    Object[] args= {name, null, valueType };
    UIFormInputBase formInput = type.getConstructor().newInstance(args) ;    
    if(validateType != null) {
      String[] validatorList = null;
      if (validateType.indexOf(',') > -1) validatorList = validateType.split(",");
      else validatorList = new String[] {validateType};
      for (String validator : validatorList)
        formInput.addValidator(getValidator(validator.trim())) ;
    }     
    if(label != null && label.length()!=0) {
      formInput.setLabel(label);
    }
    return type.cast(formInput);
  }    

  /**
   * Gets the property value as string.
   * 
   * @param node the node
   * @param propertyName the property name
   * @return the property value as string
   * @throws Exception the exception
   */
  public static String getPropertyValueAsString(Node node, String propertyName) throws Exception {
    Property property = null;
    try{
      property = node.getProperty(propertyName);      
    }catch (ItemNotFoundException e) {
      return "";
    }     
    int valueType = property.getType() ;
    switch(valueType) {
    case PropertyType.STRING: //String 
      return property.getString() ;    
    case PropertyType.LONG: // Long    
      return Long.toString(property.getLong()) ;
    case PropertyType.DOUBLE: // Double
      return Double.toString(property.getDouble()) ;
    case PropertyType.DATE: //Date
      return property.getDate().getTime().toString() ;
    case PropertyType.BOOLEAN: //Boolean
      return Boolean.toString(property.getBoolean()) ;
    case PropertyType.NAME: //Name
      return property.getName() ;
    case 8: //Path
    case 9: //References
    case 0: //Undifine      
    }
    return "" ;    
  }

  public static Class getValidator(String validatorType) throws ClassNotFoundException {
    if(validatorType.equals("name")) {
      return ECMNameValidator.class ;
    } else if (validatorType.equals("email")){
      return EmailAddressValidator.class ;
    } else if (validatorType.equals("number")) {
      return NumberFormatValidator.class;
    } else if (validatorType.equals("empty")){
      return MandatoryValidator.class ;
    }else if(validatorType.equals("datetime")) {
      return DateTimeValidator.class;
    }else {
      ClassLoader cl = Thread.currentThread().getContextClassLoader() ;
      return cl.loadClass(validatorType);
    }
  }
  public static void addValidators(UIFormInputBase uiInput, String validators) throws Exception {
    String[] validatorList = null;
    if (validators.indexOf(',') > -1) validatorList = validators.split(",");
    else validatorList = new String[] {validators};
    for (String validator : validatorList)
      uiInput.addValidator(getValidator(validator.trim())) ;
  }
}
