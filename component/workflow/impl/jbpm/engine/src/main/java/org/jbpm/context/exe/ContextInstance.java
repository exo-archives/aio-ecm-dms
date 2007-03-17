package org.jbpm.context.exe;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.graph.exe.Token;
import org.jbpm.module.exe.ModuleInstance;

/**
 * maintains all the key-variable pairs for a process instance. You can obtain a
 * ContextInstance from a processInstance from a process instance like this :
 * <pre>
 * ProcessInstance processInstance = ...;
 * ContextInstance contextInstance = processInstance.getContextInstance();
 * </pre>
 * More information on context and process variables can be found in 
 * <a href="../../../../../userguide/en/html/reference.html#context">the userguide, section context</a>
 */
public class ContextInstance extends ModuleInstance {

  private static final long serialVersionUID = 1L;

  // maps Token's to TokenVariableMap's
  protected Map tokenVariableMaps = null;
  // maps variablenames (String) to values (Object)
  protected transient Map transientVariables = null;

  public ContextInstance() {
  }

  // normal variables (persistent)
  /////////////////////////////////////////////////////////////////////////////

  /**
   * creates a variable on the root-token (= process-instance scope) and
   * calculates the actual VariableInstance-type from the value.
   */
  public void createVariable(String name, Object value) {
    createVariable(name, value, getRootToken());
  }

  /**
   * creates a variable in the scope of the given token and calculates the
   * actual VariableInstance-type from the value.
   */
  public void createVariable(String name, Object value, Token token) {
    TokenVariableMap tokenVariableMap = getOrCreateTokenVariableMap(token);
    tokenVariableMap.createVariableInstance(name, value);
  }

  /**
   * gets all the variables on the root-token (= process-instance scope).
   */
  public Map getVariables() {
    return getVariables(getRootToken());
  }

  /**
   * retrieves all the variables in scope of the given token.
   */
  public Map getVariables(Token token) {
    Map variables = new HashMap();

    TokenVariableMap tokenVariableMap = getTokenVariableMap(token);
    if (tokenVariableMap != null) {
      tokenVariableMap.collectAllVariables(variables);
    }

    return variables;
  }

  /**
   * adds all the variables on the root-token (= process-instance scope).
   */
  public void addVariables(Map variables) {
    addVariables(variables, getRootToken());
  }

  /**
   * adds all the variables to the scope of the given token.
   */
  public void addVariables(Map variables, Token token) {
    TokenVariableMap tokenVariableMap = getOrCreateTokenVariableMap(token);
    tokenVariableMap.addVariables(variables);
  }

  /**
   * gets the variable with the given name on the root-token (= process-instance
   * scope).
   */
  public Object getVariable(String name) {
    return getVariable(name, getRootToken());
  }

  /**
   * retrieves a variable in the scope of the token. If the given token does not
   * have a variable for the given name, the variable is searched for up the
   * token hierarchy.
   */
  public Object getVariable(String name, Token token) {
    Object variable = null;
    TokenVariableMap tokenVariableMap = getTokenVariableMap(token);
    if (tokenVariableMap != null) {
      variable = tokenVariableMap.getVariable(name);
    }
    return variable;
  }

  /**
   * retrieves a variable which is local to the token.
   */
  public Object getLocalVariable(String name, Token token) {
    Object variable = null;
    if (tokenVariableMaps!=null && tokenVariableMaps.containsKey(token)) {
      TokenVariableMap tokenVariableMap = (TokenVariableMap) tokenVariableMaps.get(token);
      if (tokenVariableMap != null) {
        VariableInstance variableInstance = tokenVariableMap.getVariableInstance(name);
        if(variableInstance != null) {
          variable = variableInstance.getValue();
        }
      }
    }
    return variable;
  }

  /**
   * sets a variable on the process instance scope.
   */
  public void setVariable(String name, Object value) {
    setVariable(name, value, getRootToken());
  }

  /**
   * sets a variable. If a variable exists in the scope given by the token, that
   * variable is updated. Otherwise, the variable is created on the root token
   * (=process instance scope).
   */
  public void setVariable(String name, Object value, Token token) {
    TokenVariableMap tokenVariableMap = getOrCreateTokenVariableMap(token);
    tokenVariableMap.setVariable(name, value);
  }

  /**
   * checks if a variable is present with the given name on the root-token (=
   * process-instance scope).
   */
  public boolean hasVariable(String name) {
    return hasVariable(name, getRootToken());
  }

  /**
   * checks if a variable is present with the given name in the scope of the
   * token.
   */
  public boolean hasVariable(String name, Token token) {
    boolean hasVariable = false;
    TokenVariableMap tokenVariableMap = getTokenVariableMap(token);
    if (tokenVariableMap != null) {
      hasVariable = tokenVariableMap.hasVariable(name);
    }
    return hasVariable;
  }

  /**
   * deletes the given variable on the root-token (=process-instance scope).
   */
  public void deleteVariable(String name) {
    deleteVariable(name, getRootToken());
  }

  /**
   * deletes a variable from the given token.  For safety reasons, this method does 
   * not propagate the deletion to parent tokens in case the given token does not contain 
   * the variable. 
   */
  public void deleteVariable(String name, Token token) {
    TokenVariableMap tokenVariableMap = getTokenVariableMap(token);
    if (tokenVariableMap != null) {
      tokenVariableMap.deleteVariable(name);
    }
  }

  // transient variables
  /////////////////////////////////////////////////////////////////////////////

  /**
   * retrieves the transient variable for the given name.
   */
  public Object getTransientVariable(String name) {
    Object transientVariable = null;
    if (transientVariables!= null) {
      transientVariable = transientVariables.get(name);
    }
    return transientVariable;
  }

  /**
   * sets the transient variable for the given name to the given value.
   */
  public void setTransientVariable(String name, Object value) {
    if (transientVariables == null) {
      transientVariables = new HashMap();
    }
    transientVariables.put(name, value);
  }

  /**
   * tells if a transient variable with the given name is present.
   */
  public boolean hasTransientVariable(String name) {
    if (transientVariables == null) {
      return false;
    }
    return transientVariables.containsKey(name);
  }

  /**
   * retrieves all the transient variables map. note that no deep copy is
   * performed, changing the map leads to changes in the transient variables of
   * this context instance.
   */
  public Map getTransientVariables() {
    return transientVariables;
  }

  /**
   * replaces the transient variables with the given map.
   */
  public void setTransientVariables(Map transientVariables) {
    this.transientVariables = transientVariables;
  }

  /**
   * removes the transient variable.
   */
  public void deleteTransientVariable(String name) {
    if (transientVariables == null)
      return;
    transientVariables.remove(name);
  }

  
  Token getRootToken() {
    return processInstance.getRootToken();
  }

  /**
   * looks up the token-variable-map for the given token
   * and creates it if it doesn't exist.  This method also
   * contains all the token maps for all the token's parents. 
   */
  TokenVariableMap getOrCreateTokenVariableMap(Token token) {
    TokenVariableMap tokenVariableMap = null;
    if (tokenVariableMaps==null) {
      tokenVariableMaps = new HashMap();
    }
    if (tokenVariableMaps.containsKey(token)) {
      tokenVariableMap = (TokenVariableMap) tokenVariableMaps.get(token); 
    } else {
      tokenVariableMap = new TokenVariableMap(token, this);
      tokenVariableMaps.put( token, tokenVariableMap );

      Token parent = token.getParent();
      TokenVariableMap parentVariableMap = null;
      while (parent!=null) {
        if (! tokenVariableMaps.containsKey(parent) ) {
          parentVariableMap = new TokenVariableMap(parent, this);
          tokenVariableMaps.put( parent, parentVariableMap );
        }
        parent = parent.getParent();
      }
    }
    return tokenVariableMap;
  }

  /**
   * looks for the first token-variable-map that is found
   * up the token-parent hirarchy.
   */
  TokenVariableMap getTokenVariableMap(Token token) {
    TokenVariableMap tokenVariableMap = null;
    if (tokenVariableMaps!=null) {
      if (tokenVariableMaps.containsKey(token)) {
        tokenVariableMap = (TokenVariableMap) tokenVariableMaps.get(token);
      } else if (! token.isRoot()) {
        tokenVariableMap = getTokenVariableMap(token.getParent());
      }
    }
    return tokenVariableMap;
  }
}
