package org.exoplatform.services.cms.rules;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.drools.WorkingMemory;

public interface RuleService {

  public WorkingMemory getRule(String ruleName) throws Exception;
  
  public String getRuleAsText(String ruleName) throws Exception;
  
  public NodeIterator getRules() throws Exception;
  
  public boolean hasRules() throws Exception;
   
  public void addRule(String name, String text) throws Exception;
  
  public void removeRule(String ruleName) throws Exception;
  
  public Node getRuleNode(String ruleName) throws Exception; 
  
}
