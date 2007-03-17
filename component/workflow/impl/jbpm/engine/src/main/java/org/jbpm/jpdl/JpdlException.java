package org.jbpm.jpdl;

import java.util.*;

public class JpdlException extends RuntimeException {

  private static final long serialVersionUID = 1L;
  
  protected List problems = null;
  
  public JpdlException(List problems) {
    super(problems.toString());
    this.problems = problems;
  }

  public JpdlException(String problem) {
    this(Arrays.asList(new Object[]{problem}));
  }

  public List getProblems() {
    return problems;
  }
}
