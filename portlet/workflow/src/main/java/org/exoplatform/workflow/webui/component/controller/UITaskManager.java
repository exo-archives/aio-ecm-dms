/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.workflow.webui.component.controller ;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : tran the  trong
 *          trongtt@gmail.com
 * July 3, 2006
 * 10:07:15 AM
 */
@ComponentConfig(template = "system:groovy/webui/core/UITabPane.gtmpl")
public class UITaskManager extends UIContainer {
  
  private UIDocumentContent uiDocContent;

  public UITaskManager() throws Exception {
    addChild(UITask.class, null, null) ;
    uiDocContent = createUIComponent(UIDocumentContent.class, null, null).setRendered(false) ;
  }
  
  public UIDocumentContent getUIDocContent() { return uiDocContent ; }
}