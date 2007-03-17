package org.jbpm.graph.node;

import org.jbpm.graph.def.*;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.jpdl.exe.*;

public class MilestoneEvent implements ActionHandler {

  private static final long serialVersionUID = 1L;
  
  private String milestoneName = null;
  private String relativeTokenPath = null;
  
  public MilestoneEvent() {
  }

  public MilestoneEvent( String milestoneName, String relativeTokenPath ) {
    this.milestoneName = milestoneName;
    this.relativeTokenPath = relativeTokenPath;
  }

  public void execute(ExecutionContext ac) {
    MilestoneInstance mi = MilestoneInstance.getMilestoneInstance(milestoneName, ac.getToken());
    mi.setReached(true);
    mi.notifyListeners();
  }
  



  public String getMilestoneName() {
    return milestoneName;
  }
  public void setMilestoneName(String milestoneName) {
    this.milestoneName = milestoneName;
  }
  public String getRelativeTokenPath() {
    return relativeTokenPath;
  }
  public void setRelativeTokenPath(String relativeTokenPath) {
    this.relativeTokenPath = relativeTokenPath;
  }
}
