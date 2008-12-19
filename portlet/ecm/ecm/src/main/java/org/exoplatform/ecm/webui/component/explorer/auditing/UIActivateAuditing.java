/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.ecm.webui.component.explorer.auditing;

import javax.jcr.Node;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *			    xxx5669@yahoo.com
 * Sep 29, 2008  
 */

@ComponentConfig(
  type = UIActivateAuditing.class,
  template = "app:/groovy/webui/component/explorer/auditing/UIActivateAuditing.gtmpl",
  events = {                
    @EventConfig(listeners = UIActivateAuditing.EnableAuditingActionListener.class),
    @EventConfig(listeners = UIActivateAuditing.CancelActionListener.class)
  }
)
public class UIActivateAuditing extends UIContainer implements UIPopupComponent {
  public UIActivateAuditing() throws Exception {}
  
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
  static public class EnableAuditingActionListener extends EventListener<UIActivateAuditing> {
    public void execute(Event<UIActivateAuditing> event) throws Exception {
      try {
        UIActivateAuditing uiActivateAuditing = event.getSource();
        UIJCRExplorer uiExplorer = uiActivateAuditing.getAncestorOfType(UIJCRExplorer.class);
        Node currentNode = uiExplorer.getCurrentNode();        
        currentNode.addMixin(Utils.EXO_AUDITABLE);
        currentNode.save();
        uiExplorer.getSession().save();   
        uiExplorer.getSession().refresh(true);      
        uiExplorer.updateAjax(event) ;  
      } catch(Exception e) {
        UIActivateAuditing uiActivateAuditing = event.getSource();
        UIJCRExplorer uiExplorer = uiActivateAuditing.getAncestorOfType(UIJCRExplorer.class);
        UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.does-not-support-auditing", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      }
    }
  }
  
  static public class CancelActionListener extends EventListener<UIActivateAuditing> {
    public void execute(Event<UIActivateAuditing> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      uiExplorer.cancelAction();        
    }
  }
}
