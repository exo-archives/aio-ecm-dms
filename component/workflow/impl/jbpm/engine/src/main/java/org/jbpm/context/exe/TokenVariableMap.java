package org.jbpm.context.exe;

import java.io.*;
import java.util.*;
import org.jbpm.context.log.*;
import org.jbpm.graph.exe.*;

/**
 * is a jbpm-internal map of variables related to one {@link Token}.  
 * Each token has it's own map of variables, thereby creating 
 * hierarchy and scoping of process variables. 
 */
public class TokenVariableMap implements Serializable {

  private static final long serialVersionUID = 1L;

  long id = 0;
  protected Token token = null;
  protected ContextInstance contextInstance = null;
  protected Map variableInstances = null;

  public TokenVariableMap() {
  }

  public TokenVariableMap(Token token, ContextInstance contextInstance) {
    this.token = token;
    this.contextInstance = contextInstance;
  }

  public void createVariableInstance(String name, Object value) {
    if (value==null) throw new RuntimeException("can't create jbpm process variable '"+name+"' with a null value");
    VariableInstance variableInstance = VariableInstance.create(this, name, value.getClass());
    addVariableInstance(variableInstance);
    token.addLog(new VariableCreateLog(variableInstance));
    variableInstance.setValue(value);
  }

  public void addVariableInstance(VariableInstance variableInstance) {
    if (variableInstance == null)
      throw new NullPointerException("variableInstance is null");
    if (variableInstance.getName() == null)
      throw new IllegalArgumentException("variableInstance does not have a name");
    if (variableInstances == null) {
      variableInstances = new HashMap();
    }
    variableInstances.put(variableInstance.getName(), variableInstance);
  }

  public Object getVariable(String name) {
    Object value = null;
    VariableInstance variableInstance = getVariableInstance(name);
    if (variableInstance != null) {
      value = variableInstance.getValue();
    } else { // the variable instance is not present in this map
      
      // if this is the map of the root token
      if (token.isRoot()) {
        value = null;
        
      } else { // this is a child token 
        // so let's action to the parent token's TokenVariableMap
        value = getParentMap().getVariable(name);
      }
    }
    return value;
  }

  public void setVariable(String name, Object value) {
    VariableInstance variableInstance = getVariableInstance(name);
    // if the variable instance is present in this map
    if (variableInstance != null) {
      variableInstance.setValue(value);
      
    } else { // the variable instance is not present in this map
      // if this is the map of the root token
      if (token.isRoot()) {
        // create the variable instance
        createVariableInstance(name, value);
        
      } else { // this is a child token 
        // so let's action to the parent token's TokenVariableMap
        getParentMap().setVariable(name, value);
      }
    }
  }

  public boolean hasVariable(String name) {
    boolean hasVariable = false;
    VariableInstance variableInstance = getVariableInstance(name);
    // if the variable instance is present in this map
    if (variableInstance != null) {
      hasVariable = true;
      
    // if this is not the map of the root token
    } else if (!token.isRoot()) {
      // this is a child token 
      // so let's action to the parent token's TokenVariableMap
      hasVariable = getParentMap().hasVariable(name);
    }
    return hasVariable;
  }

  public void addVariables(Map variables) {
    if (variables!=null) {
      Iterator iter = variables.entrySet().iterator();
      while (iter.hasNext()) {
        Map.Entry entry = (Map.Entry) iter.next();
        setVariable((String) entry.getKey(), entry.getValue());
      }
    }
  }

  public void deleteVariable(String name) {
    if (variableInstances!=null) {
      VariableInstance variableInstance = (VariableInstance) variableInstances.remove(name);
      if (variableInstance!=null) {
        variableInstance.removeTokenVariableMapReference();
        token.addLog(new VariableDeleteLog(variableInstance));
      }
    }
  }

  void collectAllVariables(Map variables) {
    if (variableInstances != null) {
      Iterator iter = variableInstances.values().iterator();
      while (iter.hasNext()) {
        VariableInstance variableInstance = (VariableInstance) iter.next();
        variables.put(variableInstance.getName(), variableInstance.getValue());
      }
    }
    if (!token.isRoot()) {
      getParentMap().collectAllVariables(variables);
    }
  }

  VariableInstance getVariableInstance(String name) {
    VariableInstance variableInstance = null;
    if (variableInstances != null) {
      variableInstance = (VariableInstance) variableInstances.get(name);
    }
    return variableInstance;
  }

  TokenVariableMap getParentMap() {
    Token parentToken = token.getParent();
    return contextInstance.getTokenVariableMap(parentToken);
  }

  // getters and setters 
  /////////////////////////////////////////////////////////////////////////////
  
  public ContextInstance getContextInstance() {
    return contextInstance;
  }

  public Token getToken() {
    return token;
  }

  public Map getVariableInstances() {
    return variableInstances;
  }

  // private static final Log log = LogFactory.getLog(TokenVariableMap.class);
}
