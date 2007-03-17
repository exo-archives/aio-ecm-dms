package org.jbpm.logging.log;

import java.util.ArrayList;
import java.util.List;

public class CompositeLog extends ProcessLog {
  
  private static final long serialVersionUID = 1L;
  
  private List children = null;
  
  public CompositeLog() {
  }

  public List getChildren() {
    return children;
  }
  public void setChildren(List children) {
    this.children = children;
  }

  public String toString() {
    return "composite";
  }

  public void addChild(ProcessLog processLog) {
    if (children==null) children = new ArrayList();
    children.add(processLog);
  }
}
