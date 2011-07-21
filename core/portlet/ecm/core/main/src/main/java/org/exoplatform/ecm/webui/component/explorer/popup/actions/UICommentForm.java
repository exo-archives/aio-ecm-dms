/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.component.explorer.UIDocumentContainer;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentInfo;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentWorkspace;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Jan 30, 2007  
 */

@ComponentConfig(
                 lifecycle = UIFormLifecycle.class,
                 template = "system:/groovy/webui/form/UIForm.gtmpl",
                 events = {
                   @EventConfig(listeners = UICommentForm.SaveActionListener.class),
                   @EventConfig(listeners = UICommentForm.CancelActionListener.class, phase = Phase.DECODE)
                 }
             ) 

public class UICommentForm extends org.exoplatform.ecm.webui.presentation.comment.UICommentForm { 

  public UICommentForm() throws Exception {
    super();
  }

  public String getLanguage() {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    return uiExplorer.getChild(UIWorkingArea.class).getChild(UIDocumentWorkspace.class).
                      getChild(UIDocumentContainer.class).getChild(UIDocumentInfo.class).getLanguage();
  }
  
  public Node getCommentNode() throws Exception {
    return getAncestorOfType(UIJCRExplorer.class).getNodeByPath(nodeCommentPath, document_.getSession());
  }
  
  public Node getCurrentNode() throws Exception {
    return getAncestorOfType(UIJCRExplorer.class).getCurrentNode();
  }
  
  public void updateAjax(Event<org.exoplatform.ecm.webui.presentation.comment.UICommentForm> event) throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    UIPopupWindow uiPopup = uiExplorer.getChildById("ViewSearch");
    if (uiPopup != null)
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
    uiExplorer.updateAjax(event);
  }
  
}