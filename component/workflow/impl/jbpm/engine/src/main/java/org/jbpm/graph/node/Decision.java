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
package org.jbpm.graph.node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.jbpm.graph.action.Script;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.instantiation.Delegation;
import org.jbpm.jpdl.xml.JpdlXmlReader;
import org.jbpm.jpdl.xml.Parsable;

/**
 * decision node.
 */
public class Decision extends Node implements Parsable {
  
  private static final String DECISION_CONDITION_RESULT = "decision_condition_result";
  private static final long serialVersionUID = 1L;

  List decisionConditions = null;
  Delegation decisionDelegation = null;

  public Decision() {
  }

  public Decision(String name) {
    super(name);
  }

  public void read(Element decisionElement, JpdlXmlReader jpdlReader) {
    Element decisionHandlerElement = decisionElement.element("handler");
    if (decisionHandlerElement!=null) {
      decisionDelegation = new Delegation();
      decisionDelegation.read(decisionHandlerElement, jpdlReader);
      
    } else {
      Iterator iter = decisionElement.elementIterator("transition");
      while (iter.hasNext()) {
        Element transitionElement = (Element) iter.next();
        String transitionName = transitionElement.attributeValue("name");
        String conditionExpression = transitionElement.elementText("condition");
        if (decisionConditions==null) {
          decisionConditions = new ArrayList();
        }
        decisionConditions.add(new DecisionCondition(transitionName, conditionExpression));
      }
    }
  }

  public void execute(ExecutionContext executionContext) {
    String transitionName = null;
    
    try {
      if (decisionDelegation!=null) {
        DecisionHandler decisionHandler = (DecisionHandler) decisionDelegation.instantiate();
        transitionName = decisionHandler.decide(executionContext);
        
      } else {
        Iterator iter = decisionConditions.iterator();
        while ((iter.hasNext())
               && (transitionName==null)) {
          DecisionCondition decisionCondition = (DecisionCondition) iter.next();
          if (decisionCondition.expression!=null) {
            Script decisionConditionScript = new Script();
            decisionConditionScript.setExpression("decision_condition_result = ("+decisionCondition.expression+");");
            Map inputMap = decisionConditionScript.createInputMap(executionContext);
            Set outputNames = new HashSet();
            outputNames.add(DECISION_CONDITION_RESULT);
            Map outputMap = decisionConditionScript.eval(inputMap, outputNames);
            Boolean result = (Boolean) outputMap.get(DECISION_CONDITION_RESULT);
            if (result.booleanValue()) {
              transitionName = decisionCondition.transitionName;
            }
          } else {
            transitionName = decisionCondition.transitionName;
          }
        }
      }
    } catch (Throwable exception) {
      raiseException(exception, executionContext);
    }

    Transition transition = getLeavingTransition(transitionName);
    if (transition==null) {
      throw new RuntimeException("decision '"+name+"' selected non existing transition '"+transitionName+"'" );
    }
    executionContext.leaveNode(transition);
  }
}
