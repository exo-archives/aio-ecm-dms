package org.jbpm.graph.action;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import bsh.EvalError;
import bsh.Interpreter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.jbpm.context.def.VariableAccess;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.Token;
import org.jbpm.jpdl.xml.JpdlXmlReader;
import org.jbpm.jpdl.xml.Parsable;

public class Script extends Action implements Parsable {
  
  private static final long serialVersionUID = 1L;
  
  protected String expression = null;
  protected Set variableAccesses = null;

  public void read(Element scriptElement, JpdlXmlReader jpdlReader) {
    if (scriptElement.isTextOnly()) {
      expression = scriptElement.getTextTrim();
    } else {
      this.variableAccesses = new HashSet(jpdlReader.readVariableAccesses(scriptElement));
      expression = scriptElement.element("expression").getTextTrim();
    }
  }

  public void execute(ExecutionContext executionContext) {
    Map outputMap = eval(executionContext);
    setVariables(outputMap, executionContext);
  }

  public Map eval(Token token) {
    return eval(new ExecutionContext(token));
  }

  public Map eval(ExecutionContext executionContext) {
    Map inputMap = createInputMap(executionContext);
    Set outputNames = getOutputNames();
    return eval(inputMap, outputNames);
  }

  public Map createInputMap(ExecutionContext executionContext) {
    Map inputMap = new HashMap();
    inputMap.put( "executionContext", executionContext );
    inputMap.put( "token", executionContext.getToken() );
    inputMap.put( "node", executionContext.getNode() );
    inputMap.put( "task", executionContext.getTask() );
    inputMap.put( "taskInstance", executionContext.getTaskInstance() );
    
    // if no readable variables are specified, 
    ContextInstance contextInstance = executionContext.getContextInstance();
    if (! hasReadableVariable()) {
      // we copy all the variables of the context into the interpreter 
      Map variables = contextInstance.getVariables();
      if ( variables != null ) {
        Iterator iter = variables.entrySet().iterator();
        while( iter.hasNext() ) {
          Map.Entry entry = (Map.Entry) iter.next();
          String variableName = (String) entry.getKey();
          Object variableValue = entry.getValue();
          inputMap.put(variableName, variableValue);
        }
      }

    } else {
      // we only copy the specified variables into the interpreterz
      Iterator iter = variableAccesses.iterator();
      while (iter.hasNext()) {
        VariableAccess variableAccess = (VariableAccess) iter.next();
        if (variableAccess.isReadable()) {
          String variableName = variableAccess.getVariableName();
          String mappedName = variableAccess.getMappedName();
          Object variableValue = contextInstance.getVariable(variableName);
          inputMap.put(mappedName, variableValue);
        }
      }
    }
    
    return inputMap;
  }

  public Map eval(Map inputMap, Set outputNames) {
    Map outputMap = new HashMap();
    
    try {
      log.debug("script input: "+inputMap);
      Interpreter interpreter = new Interpreter();
      Iterator iter = inputMap.keySet().iterator();
      while (iter.hasNext()) {
        String inputName = (String) iter.next();
        Object inputValue = inputMap.get(inputName);
        if (inputValue!=null) {
          interpreter.set(inputName, inputValue);
        }
      }
      interpreter.eval(expression);
      iter = outputNames.iterator();
      while (iter.hasNext()) {
        String outputName = (String) iter.next();
        Object outputValue = interpreter.get(outputName);
        outputMap.put(outputName, outputValue);
      }
      log.debug("script output: "+outputMap);
    } catch (EvalError e) {
      throw new RuntimeException("can't evaluate beanshell script '"+expression+"'", e);
    }

    return outputMap;
  }

  public void addVariableAccess(VariableAccess variableAccess) {
    if (variableAccesses==null) variableAccesses = new HashSet();
    variableAccesses.add(variableAccess);
  }

  Set getOutputNames() {
    Set outputNames = new HashSet();
    if (variableAccesses!=null) {
      Iterator iter = variableAccesses.iterator();
      while (iter.hasNext()) {
        VariableAccess variableAccess = (VariableAccess) iter.next();
        if (variableAccess.isWritable()) {
          outputNames.add(variableAccess.getMappedName());
        }
      }
    }
    return outputNames;
  }

  boolean hasReadableVariable() {
    if (variableAccesses==null) return false;
    Iterator iter = variableAccesses.iterator();
    while (iter.hasNext()) {
      VariableAccess variableAccess = (VariableAccess) iter.next();
      if (variableAccess.isReadable()) {
        return true;
      }
    }
    return false;
  }

  void setVariables(Map outputMap, ExecutionContext executionContext) {
    if ( (outputMap!=null)
         && (!outputMap.isEmpty()) 
         && (executionContext!=null)
       ) {
      Map variableNames = getVariableNames();
      ContextInstance contextInstance = executionContext.getContextInstance();
      Token token = executionContext.getToken();
      
      Iterator iter = outputMap.keySet().iterator();
      while (iter.hasNext()) {
        String mappedName = (String) iter.next();
        String variableName = (String) variableNames.get(mappedName);
        contextInstance.setVariable(variableName, outputMap.get(mappedName), token);
      }
    }
  }

  Map getVariableNames() {
    Map variableNames = new HashMap();
    Iterator iter = variableAccesses.iterator();
    while (iter.hasNext()) {
      VariableAccess variableAccess = (VariableAccess) iter.next();
      if (variableAccess.isWritable()) {
        variableNames.put(variableAccess.getMappedName(), variableAccess.getVariableName());
      }
    }
    return variableNames;
  }

  public String getExpression() {
    return expression;
  }
  public void setExpression(String expression) {
    this.expression = expression;
  }
  public Set getVariableAccesses() {
    return variableAccesses;
  }
  public void setVariableAccesses(Set variableAccesses) {
    this.variableAccesses = variableAccesses;
  }
  
  private static final Log log = LogFactory.getLog(Script.class);
}
