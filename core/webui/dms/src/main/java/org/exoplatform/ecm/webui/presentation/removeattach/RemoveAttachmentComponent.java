/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
 *
 **************************************************************************/
package org.exoplatform.ecm.webui.presentation.removeattach;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.ecm.webui.presentation.action.UIPresentationEventListener;
import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.services.cms.link.NodeFinder;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;

/**
 * Created by The eXo Platform SARL
 * Author : Hoang Van Hung
 *          hunghvit@gmail.com
 * Sep 16, 2009  
 */

@ComponentConfig(
    events = {
      @EventConfig(listeners = RemoveAttachmentComponent.RemoveAttachActionListener.class, confirm = "RemoveAttachmentComponent.msg.confirm-deleteattachment")
    }
)

public class RemoveAttachmentComponent extends UIComponent {
  
  private static final Log LOG = ExoLogger.getLogger(RemoveAttachmentComponent.class);
  
  /**
   * Overide method UIComponent.loadConfirmMesssage() to get resource bundle in jar file
   */
  protected String loadConfirmMesssage(org.exoplatform.webui.config.Event event, WebuiRequestContext context, String beanId) {
    String confirmKey  = event.getConfirm();
    if(confirmKey.length() < 1) return confirmKey;  
    try {
      String confirm = Utils.getResourceBundle(Utils.LOCALE_WEBUI_DMS, confirmKey, getClass().getClassLoader());
      return confirm.replaceAll("\\{0\\}", beanId);
    }catch (Exception e) {}
    return confirmKey;
  }
  
  public static void doDelete(Map<String, Object> variables) throws Exception {
    UIComponent uicomponent = (UIComponent)variables.get(UICOMPONENT);
    UIComponent uiParent = uicomponent.getParent();
    UIApplication uiApp = uicomponent.getAncestorOfType(UIApplication.class);
    NodeFinder nodefinder = uicomponent.getApplicationComponent(NodeFinder.class);
    String repository = String.valueOf(variables.get(Utils.REPOSITORY));
    String wsname = String.valueOf(variables.get(Utils.WORKSPACE_PARAM));
    String nodepath = String.valueOf(variables.get(OBJECTID));
    WebuiRequestContext requestcontext = (WebuiRequestContext)variables.get(Utils.REQUESTCONTEXT);
    try {
        Node node = (Node) nodefinder.getItem(repository, wsname, nodepath);
        Session session = node.getSession();
        node.remove();
        session.save();
        session.logout();
        requestcontext.addUIComponentToUpdateByAjax(uiParent.getParent());
        return;
      } catch (Exception e) {
        LOG.error("an unexpected error occurs while removing the node", e);
        JCRExceptionManager.process(uiApp, e);
        requestcontext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
  }
  
  public static class RemoveAttachActionListener extends UIPresentationEventListener<RemoveAttachmentComponent> {
    @Override
    protected void executeAction(Map<String, Object> variables) throws Exception {
      RemoveAttachmentComponent.doDelete(variables);
    }
  }
  
  
}
