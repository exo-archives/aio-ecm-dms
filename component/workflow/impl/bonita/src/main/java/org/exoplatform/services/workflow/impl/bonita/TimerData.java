/******************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL. All rights reserved.            *
 * Please look at license.txt in info directory for more license detail.      *
 ******************************************************************************/
package org.exoplatform.services.workflow.impl.bonita;

import java.util.Date;

import org.exoplatform.services.workflow.Timer;

/**
 * Created by Bull R&D
 * @author Brice Revenant
 * Jun 12, 2006
 */
public class TimerData implements Timer {
  
  private Date dueDate = null;
  private String id    = null;
  private String name  = null;

  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.Timer#getDueDate()
   */
  public Date getDueDate() {
    return this.dueDate;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.Timer#getId()
   */
  public String getId() {
    return this.id;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.Timer#getName()
   */
  public String getName() {
    return this.name;
  }
  
  /**
   * Constructs a new Timer Data instance based on the specified Bonita object
   * 
   * @param timerData contains Timer information
   */
  public TimerData(hero.util.TimerData timerData) {
    
    // The pattern is "task:process_instance_id:date"
    String[] type  = timerData.getType().split(":"); 
    
    // Set the class attributes
    this.dueDate = new Date(timerData.getMs());
    this.id      = type[0];
    this.name    = type[1];
  }
}
