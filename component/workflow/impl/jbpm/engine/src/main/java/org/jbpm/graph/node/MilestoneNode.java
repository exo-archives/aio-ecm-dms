package org.jbpm.graph.node;
import org.jbpm.graph.def.*;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.Token;
import org.jbpm.jpdl.exe.*;

public class MilestoneNode extends Node {
  
  private static final long serialVersionUID = 1L;
  
  private String tokenPath = ".";
  
  public MilestoneNode() {
  }

  public MilestoneNode(String name) {
    super(name);
  }

  public void execute(ExecutionContext executionContext) {
    Token token = executionContext.getToken();
    // get the token on which the milestone should be verified
    Token milestoneToken = token.findToken( tokenPath );
    if ( isMilestoneReached( name, milestoneToken ) ) {
      
      // continue to pass the token over the default transition
      token.getNode().leave(executionContext);

    } else {
      addMilestoneListener(name,milestoneToken);
    }
  }
  
  public boolean isMilestoneReached(String milestoneName, Token token) {
    MilestoneInstance mi = MilestoneInstance.getMilestoneInstance(milestoneName, token);
    return (mi != null ? mi.isReached() : false);
  }

  public void addMilestoneListener(String milestoneName, Token token) {
    MilestoneInstance mi = MilestoneInstance.getMilestoneInstance(milestoneName, token);
    mi.addListener(token);
  }


  public String getTokenPath() {
    return tokenPath;
  }
  public void setTokenPath(String relativeTokenPath) {
    this.tokenPath = relativeTokenPath;
  }
}
