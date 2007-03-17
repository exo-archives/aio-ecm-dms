/******************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL. All rights reserved.            *
 * Please look at license.txt in info directory for more license detail.      *
 ******************************************************************************/
package org.exoplatform.services.workflow.impl.bonita;

import hero.interfaces.BnProjectLightValue;

import org.exoplatform.services.workflow.Process;

/**
 * Created by Bull R&D
 * @author Brice Revenant
 * Dec 27, 2005
 */

public class ProcessData implements Process {
  String id             = null;
  String name           = null;
  int    version        = 0;
  String startStateName = null;
  
  /**
   * By convention an empty String represents the start state name. This does
   * not match any state in Bonita but is local to the service implementation.
   */
  public static final String START_STATE_NAME = new String();
  
  public ProcessData(BnProjectLightValue projectValue) {
    this.id      = projectValue.getId();
    this.name    = projectValue.getName();
    this.version = 1;
  }
  
  public String getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public int getVersion() {
    return this.version;
  }

  public String getStartStateName() {
    // There is no concept of start state in Bonita so by convention an
    // empty string indicates that the process needs to be instantiated.
    return ProcessData.START_STATE_NAME;
  }
}
