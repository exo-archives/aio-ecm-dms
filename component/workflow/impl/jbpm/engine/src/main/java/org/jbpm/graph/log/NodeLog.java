package org.jbpm.graph.log;

import java.util.Date;

import org.jbpm.graph.def.Node;
import org.jbpm.logging.log.ProcessLog;

public class NodeLog extends ProcessLog {
  
  private static final long serialVersionUID = 1L;
  
  protected Node node = null;
  protected Date enter = null;
  protected Date leave = null;
  protected long duration = -1;

  // constructors /////////////////////////////////////////////////////////////

  public NodeLog() {
  }
  
  public NodeLog(Node node, Date enter, Date leave) {
    this.node = node;
    this.enter = enter;
    this.leave = leave;
    this.duration = leave.getTime()-enter.getTime();
  }
  
  // getters and setters //////////////////////////////////////////////////////

  public String toString() {
    return "node["+node.getName()+"]";
  }
  public long getDuration() {
    return duration;
  }
  public Date getEnter() {
    return enter;
  }
  public Date getLeave() {
    return leave;
  }
  public Node getNode() {
    return node;
  }
}
