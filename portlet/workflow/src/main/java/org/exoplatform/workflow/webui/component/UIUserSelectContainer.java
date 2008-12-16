/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.workflow.webui.component;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.organization.account.UIUserSelector;
import org.exoplatform.workflow.webui.component.controller.UITask;
import org.exoplatform.workflow.webui.component.controller.UITaskManager;

/**
 * Created by The eXo Platform SARL
 * Author : Hoang Van Hung
 * Mail   : hunghvit@gmail.com
 * Dec 17, 2008  
 */

@ComponentConfig(
    lifecycle = UIContainerLifecycle.class,
    events = {@EventConfig(listeners = UIUserSelectContainer.AddUserActionListener.class)}
)

public class UIUserSelectContainer extends UIContainer{
  
  String fieldname;
  
  public UIUserSelectContainer() throws Exception {
    UIUserSelector uiUserSelector = getChild(UIUserSelector.class);
    if (uiUserSelector == null) {
      uiUserSelector = addChild(UIUserSelector.class, null, null);
    }
    uiUserSelector.setMulti(false);
    uiUserSelector.setShowSearchGroup(true);
    uiUserSelector.setShowSearchUser(true);
    uiUserSelector.setShowSearch(true);
  }

  static  public class AddUserActionListener extends EventListener<UIUserSelectContainer> {
    public void execute(Event<UIUserSelectContainer> event) throws Exception {
      UIUserSelectContainer uiUserContainer = event.getSource();
      UIUserSelector uiUserSelector = uiUserContainer.getChild(UIUserSelector.class);
      UITaskManager uiTaskManager = uiUserContainer.getAncestorOfType(UITaskManager.class);
      UITask uiTask = uiTaskManager.getChild(UITask.class);
      uiTask.doSelect(uiUserContainer.getFieldname(), uiUserSelector.getSelectedUsers());
      UIPopupWindow uiPopup = uiUserContainer.getParent();
      uiPopup.setShow(false);
      uiPopup.setRendered(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTask);
    }  
  }

  public String getFieldname() {
    return fieldname;
  }

  public void setFieldname(String fieldname) {
    this.fieldname = fieldname;
  }
}
