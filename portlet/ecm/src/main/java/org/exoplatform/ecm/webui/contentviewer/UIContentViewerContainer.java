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
package org.exoplatform.ecm.webui.contentviewer;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS Author : Phan Le Thanh Chuong
 * chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com Nov 4, 2008
 */

@ComponentConfig(
   lifecycle = Lifecycle.class, 
   template = "app:/groovy/webui/contentviewer/UIContentViewerContainer.gtmpl", 
   events = { 
     @EventConfig(listeners = UIContentViewerContainer.QuickEditActionListener.class)                   
   }
)
public class UIContentViewerContainer extends UIContainer {

  public static final String WEB_CONTENT_DIALOG = "webContentDialog";

  public UIContentViewerContainer() throws Exception {
    addChild(UIContentViewer.class, null, null);
  }

  public boolean isQuickEditAble() throws Exception {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    String portalName = Util.getUIPortal().getName();
    String userId = context.getRemoteUser();
    DataStorage dataStorage = getApplicationComponent(DataStorage.class);
    PortalConfig portalConfig = dataStorage.getPortalConfig(portalName);
    UserACL userACL = getApplicationComponent(UserACL.class);
    return userACL.hasEditPermission(portalConfig, userId);
  }

  public static class QuickEditActionListener extends EventListener<UIContentViewerContainer> {
    public void execute(Event<UIContentViewerContainer> event) throws Exception {
//      UIContentViewerContainer uiContentViewerContainer = event.getSource();
//      UIContentViewer uiContentViewer = uiContentViewerContainer.getChild(UIContentViewer.class);
//      Node contentNode = uiContentViewer.getNode();
//      ManageableRepository manageableRepository = (ManageableRepository) contentNode.getSession()
//                                                                                    .getRepository();
//      String repository = manageableRepository.getConfiguration().getName();
//      String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName();
//      uiContentViewerContainer.removeChild(UIContentViewer.class);
//      UIDocumentDialogForm uiDocumentForm = uiContentViewerContainer.createUIComponent(UIDocumentDialogForm.class,
//                                                                                       null,
//                                                                                       null);
//      uiDocumentForm.setRepositoryName(repository);
//      uiDocumentForm.setWorkspace(workspace);
//      uiDocumentForm.setContentType(contentNode.getPrimaryNodeType().getName());
//      uiDocumentForm.setNodePath(contentNode.getPath());
//      uiDocumentForm.setStoredPath(contentNode.getPath());
//      uiDocumentForm.addNew(false);
//      uiContentViewerContainer.addChild(uiDocumentForm);
//      event.getRequestContext().addUIComponentToUpdateByAjax(uiContentViewerContainer);
    }
  }

}
