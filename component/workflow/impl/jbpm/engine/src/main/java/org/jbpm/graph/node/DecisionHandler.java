package org.jbpm.graph.node;

import java.io.Serializable;

import org.jbpm.graph.exe.ExecutionContext;

/**
 * decision handler as in jbpm 2.
 */
public interface DecisionHandler extends Serializable {

  String decide(ExecutionContext executionContext) throws Exception;

}
