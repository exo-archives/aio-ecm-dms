package org.jbpm.context.def;

import java.io.Serializable;

/**
 * specifies access to a variable.
 * Variable access is used in 3 situations:
 * 1) process-state 
 * 2) script 
 * 3) task controllers 
 */
public class VariableAccess implements Serializable {

  private static final long serialVersionUID = 1L;
  
  long id = 0;
  protected String variableName = null;
  protected String access = null;
  protected String mappedName = null;

  // constructors /////////////////////////////////////////////////////////////

  public VariableAccess() {
  }

  public VariableAccess(String variableName, String access, String mappedName) {
    this.variableName = variableName;
    if (access!=null) access = access.toLowerCase();
    this.access = access;
    this.mappedName = mappedName;
  }

  // getters and setters //////////////////////////////////////////////////////

  /**
   * the mapped name.  The mappedName defaults to the variableName in case 
   * no mapped name is specified.  
   */
  public String getMappedName() {
    if (mappedName==null) {
      return variableName;
    }
    return mappedName;
  }

  /**
   * specifies a comma separated list of access literals {read, write, required}.
   */
  public String getAccess() {
    return access;
  }
  public String getVariableName() {
    return variableName;
  }
  
  public boolean isReadable() {
    return hasAccess("read");
  }

  public boolean isWritable() {
    return hasAccess("write");
  }

  public boolean isRequired() {
    return hasAccess("required");
  }

  /**
   * verifies if the given accessLiteral is included in the access text.
   */
  public boolean hasAccess(String accessLiteral) {
    if (access==null) return false;
    return (access.indexOf(accessLiteral.toLowerCase())!=-1);
  }
}
