/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.nodetype;

import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 2, 2006
 * 5:35:40 PM 
 */
@ComponentConfig (lifecycle = UIContainerLifecycle.class)
public class UINodeTypeImportPopup extends UIContainer {

  public UINodeTypeImportPopup() throws Exception {
    addChild(UINodeTypeUpload.class, null, null) ;
    addChild(UINodeTypeImport.class, null, null).setRendered(false) ;
  }
}
