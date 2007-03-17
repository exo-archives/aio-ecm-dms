/*
 * Created on Mar 28, 2005
 */
package org.exoplatform.services.workflow;

import java.util.HashSet;

/**
 * @author benjaminmestrallet
 */
public class ProcessesConfig {
  
  private HashSet predefinedProcess = new HashSet(5);
  private String processLocation ;
  
  public HashSet getPredefinedProcess() {   return predefinedProcess; }
  public void setPredefinedProcess(HashSet s) {  this.predefinedProcess = s; }
  
  public String getProcessLocation() {  return processLocation; }
  public void setProcessLocation(String s) { this.processLocation = s; }

}
