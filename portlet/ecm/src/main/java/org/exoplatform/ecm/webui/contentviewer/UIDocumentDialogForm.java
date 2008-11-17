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

import java.security.AccessControlException;
import java.util.List;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.VersionException;

import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.form.UIDialogForm;
import org.exoplatform.ecm.webui.utils.DialogFormUtil;
import org.exoplatform.ecm.webui.utils.LockUtil;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com,
 * anhdn86@gmail.com Nov 13, 2008
 */

@ComponentConfig(lifecycle = UIFormLifecycle.class, events = {
    @EventConfig(listeners = UIDocumentDialogForm.SaveActionListener.class),
    @EventConfig(listeners = UIDocumentDialogForm.CancelActionListener.class) })
public class UIDocumentDialogForm extends UIDialogForm {

  private Node documentNode;

  public void setDocumentNode(Node node) {
    documentNode = node;
  }

  public Node getDocumentNode() {
    return documentNode;
  }

  public String getTemplate() {
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    String userName = Util.getPortalRequestContext().getRemoteUser();
    try {
      return templateService.getTemplatePathByUser(true, contentType, userName, repositoryName);
    } catch (Exception e) {
      UIApplication uiApp = getAncestorOfType(UIApplication.class);
      Object[] arg = { contentType };
      uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.not-support",
                                              arg,
                                              ApplicationMessage.ERROR));
      return null;
    }
  }

  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    try {
      if (resourceResolver == null) {
        RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
        ManageableRepository manageableRepository = repositoryService.getRepository(this.repositoryName);
        String workspace = manageableRepository.getConfiguration().getSystemWorkspaceName();
        resourceResolver = new JCRResourceResolver(this.repositoryName,
                                                   workspace,
                                                   TemplateService.EXO_TEMPLATE_FILE_PROP);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return resourceResolver;
  }

  public static class SaveActionListener extends EventListener<UIDocumentDialogForm> {

    public void execute(Event<UIDocumentDialogForm> event) throws Exception {
      UIDocumentDialogForm uiDocumentDialogForm = event.getSource();
      UIApplication uiApp = uiDocumentDialogForm.getAncestorOfType(UIApplication.class);
      Node documentNode = uiDocumentDialogForm.getNode();
      Session session = documentNode.getSession();
      ManageableRepository manageableRepository = (ManageableRepository) session.getRepository();
      String repository = manageableRepository.getConfiguration().getName();
      String workspace = manageableRepository.getConfiguration().getSystemWorkspaceName();
      List inputs = uiDocumentDialogForm.getChildren();
      Map inputProperties = DialogFormUtil.prepareMap(inputs,
                                                      uiDocumentDialogForm.getInputProperties());
      String nodeTypeName = documentNode.getPrimaryNodeType().getName();
      Node homeNode = documentNode.getParent();
      Node newNode = null;
      if (documentNode.isLocked())
        session.addLockToken(LockUtil.getLockToken(documentNode));
      try {
        CmsService cmsService = uiDocumentDialogForm.getApplicationComponent(CmsService.class);
        String addedPath = cmsService.storeNode(nodeTypeName,
                                                homeNode,
                                                inputProperties,
                                                uiDocumentDialogForm.isAddNew,
                                                uiDocumentDialogForm.repositoryName);
        try {
          homeNode.save();
          newNode = (Node) homeNode.getSession().getItem(addedPath);
          event.getRequestContext().setAttribute("nodePath", newNode.getPath());
        } catch (Exception e) {
          e.printStackTrace();
        }
      } catch (AccessControlException ace) {
        throw new AccessDeniedException(ace.getMessage());
      } catch (VersionException ve) {
        uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.in-versioning",
                                                null,
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (ItemNotFoundException item) {
        uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.item-not-found",
                                                null,
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (RepositoryException repo) {
        repo.printStackTrace();
        String key = "UIDocumentForm.msg.repository-exception";
        if (ItemExistsException.class.isInstance(repo))
          key = "UIDocumentForm.msg.not-allowed-same-name-sibling";
        uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (NumberFormatException nume) {
        String key = "UIDocumentForm.msg.numberformat-exception";
        uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (Exception e) {
        e.printStackTrace();
        String key = "UIDocumentForm.msg.cannot-save";
        uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      UIContentViewerContainer uiContentViewerContainer = uiDocumentDialogForm.getParent();
      uiContentViewerContainer.removeChild(UIDocumentDialogForm.class);
      UIContentViewer uiContentViewer = uiContentViewerContainer.addChild(UIContentViewer.class,
                                                                          null,
                                                                          null);
      uiContentViewer.setNode(newNode);
      uiContentViewer.setRepository(repository);
      uiContentViewer.setWorkspace(workspace);
    }

  }

  public static class CancelActionListener extends EventListener<UIDocumentDialogForm> {
    public void execute(Event<UIDocumentDialogForm> event) throws Exception {

    }
  }

}
