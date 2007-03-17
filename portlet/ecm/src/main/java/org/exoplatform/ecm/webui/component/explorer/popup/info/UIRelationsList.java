/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.info;

import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : nqhungvn
 *          nguyenkequanghung@yahoo.com
 * July 3, 2006
 * 10:07:15 AM
 */

@ComponentConfig(
  events = {
    @EventConfig(listeners = UIRelationsList.AddRelationActionListener.class), 
    @EventConfig(listeners = UIRelationsList.CancelActionListener.class)
  }
)

public class UIRelationsList extends UIContainer {
  
  public UIRelationsList() throws Exception {
   
  }
  
  @SuppressWarnings("unused")
  static  public class AddRelationActionListener extends EventListener<UIRelationsList> {
    public void execute(Event<UIRelationsList> event) throws Exception {
      
    }
  }
  
  @SuppressWarnings("unused")
  static  public class CancelActionListener extends EventListener<UIRelationsList> {
    public void execute(Event<UIRelationsList> event) throws Exception {
      
    }
  }
}

