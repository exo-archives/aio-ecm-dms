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
package org.jbpm.context.exe;

import java.io.Serializable;
import java.util.Iterator;

import org.jbpm.db.JbpmSession;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;

/**
 * is a jbpm-internal class that serves as a base class for classes 
 * that store variable values in the database.
 */
public abstract class VariableInstance implements Serializable {

  private static final long serialVersionUID = 1L;
  
  long id = 0;
  protected String name = null;
  protected Token token = null;
  protected TokenVariableMap tokenVariableMap = null;
  protected ProcessInstance processInstance = null;
  protected Converter converter = null;

  // constructors /////////////////////////////////////////////////////////////
  
  public VariableInstance() {
  }

  // public static VariableInstance create(Token token, String name, Object value) {
  public static VariableInstance create(TokenVariableMap tokenVariableMap, String name, Class valueClass) {
    Token token = tokenVariableMap.token;
    ProcessInstance processInstance = (token!=null ? token.getProcessInstance() : null );
    VariableInstance variableInstance = createVariableInstance(valueClass);
    variableInstance.tokenVariableMap = tokenVariableMap;
    variableInstance.token = token;
    variableInstance.processInstance = processInstance;
    variableInstance.name = name;
    return variableInstance;
  }

  // abstract methods /////////////////////////////////////////////////////////

  /**
   * is true if this variable-instance supports the given type, false otherwise.
   */
  protected abstract boolean supports(Class clazz);
  /**
   * is the value, stored by this variable instance.
   */
  protected abstract Object getObject();
  /**
   * stores the value in this variable instance.
   */
  protected abstract void setObject(Object value);

  // variable management //////////////////////////////////////////////////////

  public static VariableInstance createVariableInstance(Class javaType) {
    VariableInstance variableInstance = null;
    
    Iterator iter = JbpmType.getJbpmTypes().iterator();
    while ( (iter.hasNext())
            && (variableInstance==null) ){
      JbpmType jbpmType = (JbpmType) iter.next();

      // isMatch indicates wether the given javaType matches this jbpmType
      boolean isMatch = false;
      if ( (jbpmType.variableClass!=null)
           && (jbpmType.variableClass.isAssignableFrom(javaType)) ) {
        isMatch = true;
      } else {
        if (javaType.getName().equals(jbpmType.variableClassName)) {
          isMatch = true;
        } else if ("{serializable-classes}".equals(jbpmType.variableClassName)) {
          isMatch = (Serializable.class.isAssignableFrom(javaType));
        } else if ("{hibernateable-long-id-classes}".equals(jbpmType.variableClassName)) {
          JbpmSession currentJbpmSession = JbpmSession.getCurrentJbpmSession();
          isMatch = ( (currentJbpmSession!=null)
                      && (currentJbpmSession.getJbpmSessionFactory().isHibernatableWithLongId(javaType)));
        } else if ("{hibernateable-string-id-classes}".equals(jbpmType.variableClassName)) {
          JbpmSession currentJbpmSession = JbpmSession.getCurrentJbpmSession();
          isMatch = ( (currentJbpmSession!=null)
                  && (currentJbpmSession.getJbpmSessionFactory().isHibernatableWithStringId(javaType)));
        }
      }
      
      if (isMatch) {
        variableInstance = createVariableInstance(jbpmType);
      }
    }
    
    if (variableInstance==null) {
      throw new RuntimeException("contents of jbpm.varmapping.properties does not specify how jbpm should store objects of type '"+javaType.getName()+"' in the database");
    }
    
    return variableInstance;
  }

  private static VariableInstance createVariableInstance(JbpmType jbpmType) {
    VariableInstance variableInstance;
    try {
      variableInstance = (VariableInstance) jbpmType.variableInstanceClass.newInstance();
      variableInstance.converter = jbpmType.converter;
    } catch (Exception e) {
      throw new RuntimeException("couldn't instantiate variable instance of class '"+jbpmType.getClass().getName()+"'", e);
    }
    return variableInstance;
  }

  public void setValue(Object value) {
    if ( (value!=null)
         && (converter!=null) ) {
      if (! converter.supports(value.getClass())) {
        throw new RuntimeException("the converter '"+converter.getClass().getName()+"' in variable instance '"+this.getClass().getName()+"' does not support values of type '"+value.getClass().getName()+"'.  to change the type of a variable, you have to delete it first");
      }
      value = converter.convert(value);
    }
    if ( (value!=null)
         && (! this.supports(value.getClass())) ) {
      throw new RuntimeException("variable instance '"+this.getClass().getName()+"' does not support values of type '"+value.getClass().getName()+"'.  to change the type of a variable, you have to delete it first");
    }
    setObject(value);
  }

  public Object getValue() {
    Object value = getObject();
    if ( (value!=null)
         && (converter!=null) ) {
      value = converter.revert(value);
    }
    return value;
  }
  

  public void removeTokenVariableMapReference() {
    this.tokenVariableMap = null;
    this.token = null;
    this.processInstance = null;
  }


  // utility methods /////////////////////////////////////////////////////////

  public String toString() {
    return "${"+name+"}";
  }
  
  // getters and setters //////////////////////////////////////////////////////

  public String getName() {
    return name;
  }

  public ProcessInstance getProcessInstance() {
    return processInstance;
  }

  public Token getToken() {
    return token;
  }
}
