/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.repository;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Jun 7, 2007  
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)

public class UIRepositoryFormContainer extends UIContainer implements UIPopupComponent {
  
public UIRepositoryFormContainer() throws Exception {
  addChild(UIRepositoryForm.class, null, null) ;
  UIPopupAction uiPopupAction = addChild(UIPopupAction.class, null, "UIPopupControl");
  uiPopupAction.getChild(UIPopupWindow.class).setId("UIPopupWindowControl") ;
}

protected void refresh(boolean isAddnew, RepositoryEntry re) throws Exception {
  getChild(UIRepositoryForm.class).isAddnew_ = isAddnew ;
  getChild(UIRepositoryForm.class).refresh(re) ;
  getChild(UIRepositoryForm.class).lockRepoForm(isAddnew) ;
}

public void activate() throws Exception {
  // TODO Auto-generated method stub
  
}

public void deActivate() throws Exception {
  // TODO Auto-generated method stub
  
}
}
