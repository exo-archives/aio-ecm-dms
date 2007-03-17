package org.jbpm.context.def;

import org.jbpm.context.exe.*;
import org.jbpm.module.def.*;
import org.jbpm.module.exe.*;

public class ContextDefinition extends ModuleDefinition {

  private static final long serialVersionUID = 1L;

  public ContextDefinition() {
  }

  public ModuleInstance createInstance() {
    return new ContextInstance();
  }
}
