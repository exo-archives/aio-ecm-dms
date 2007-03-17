package org.jbpm.graph.node;

import java.util.*;
import org.dom4j.Element;
import org.jbpm.context.def.*;
import org.jbpm.context.exe.*;
import org.jbpm.graph.def.*;
import org.jbpm.graph.exe.*;
import org.jbpm.jpdl.xml.*;

/**
 * is an unordered set of child nodeMap.  the path of execution will 
 * be given to each node exactly once.   the sequence of the child
 * nodeMap will be determined at runtime.  this implements the 
 * workflow pattern interleved parallel routing. 
 * 
 * If no script is supplied, the transition names will be sequenced
 * in arbitrary order.
 * If a script is provided, the variable transitionNames contains the 
 * available transition names. The returned value has to be one of 
 * those transitionNames.
 * Instead of supplying a script, its also possible to subclass this 
 * class and override the selectTransition method.
 */
public class InterleaveStart extends Node implements Parsable {
  
  private static final long serialVersionUID = 1L;

  private String variableName = "interleave-transition-names";
  private Interleaver interleaver = new DefaultInterleaver(); 
  
  public interface Interleaver {
    String selectNextTransition(Collection transitionNames);
  }
  
  public class DefaultInterleaver implements Interleaver {
    public String selectNextTransition(Collection transitionNames) {
      return (String) transitionNames.iterator().next();
    }
  }
  
  public InterleaveStart() {
  }

  public InterleaveStart(String name) {
    super(name);
  }

  public void read(Element element, JpdlXmlReader jpdlReader) {
    // TODO
    
    // just making sure that the context definition is present
    // because the interleave node needs the context instance at runtime
    ProcessDefinition processDefinition = jpdlReader.getProcessDefinition();
    if (processDefinition.getDefinition(ContextDefinition.class)==null) {
      processDefinition.addDefinition(new ContextDefinition());
    }
  }

  public void write(Element element) {
    // TODO
  }

  public void execute(ExecutionContext executionContext) {
    Token token = executionContext.getToken();
    Collection transitionNames = retrieveTransitionNames(token);
    // if this is the first time we enter
    if ( transitionNames == null ) {
      // collect all leaving transition names
      transitionNames = new ArrayList(getTransitionNames(token));
    }
    
    // select one of the remaining transition names
    String nextTransition = interleaver.selectNextTransition(transitionNames);
    // remove it from the remaining transitions
    transitionNames.remove(nextTransition);

    // store the transition names
    storeTransitionNames(transitionNames,token);

    // pass the token over the selected transition
    token.getNode().leave(executionContext, nextTransition);
  }

  protected Collection getTransitionNames(Token token) {
    Node node = token.getNode();
    return node.getLeavingTransitionsMap().keySet();
  }

  protected void storeTransitionNames(Collection transitionNames, Token token) {
    ContextInstance ci = (ContextInstance) token.getProcessInstance().getInstance(ContextInstance.class);
    if (ci==null) throw new RuntimeException("an interleave start node requires the availability of a context");
    ci.setVariable(variableName,transitionNames, token);
  }

  public Collection retrieveTransitionNames(Token token) {
    ContextInstance ci = (ContextInstance) token.getProcessInstance().getInstance(ContextInstance.class);
    return (Collection) ci.getVariable(variableName, token);
  }

  public void removeTransitionNames(Token token) {
    ContextInstance ci = (ContextInstance) token.getProcessInstance().getInstance(ContextInstance.class);
    ci.setVariable(variableName,null, token);
  }
  
  public Interleaver getInterleaver() {
    return interleaver;
  }
  public void setInterleaver(Interleaver interleaver) {
    this.interleaver = interleaver;
  }
}
