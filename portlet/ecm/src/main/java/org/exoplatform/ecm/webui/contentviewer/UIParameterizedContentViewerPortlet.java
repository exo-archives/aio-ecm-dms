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

import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.security.AccessControlException;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Sep 24, 2008
 */

@ComponentConfig(lifecycle = UIApplicationLifecycle.class)
public class UIParameterizedContentViewerPortlet extends UIPortletApplication {

  public final static String CONTENT_NOT_FOUND_EXC        = "UIMessageBoard.msg.content-not-found";

  public final static String REPOSITORY_CONFIGURATION_EXC = "UIMessageBoard.msg.repository-configuration-exc";

  public final static String REPOSITORY_EXC               = "UIMessageBoard.msg.repository-exc";

  public final static String ACCESS_CONTROL_EXC           = "UIMessageBoard.msg.access-control-exc";

  public UIParameterizedContentViewerPortlet() throws Exception {
  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    PortletRequestContext porletRequestContext = (PortletRequestContext) context;
    HttpServletRequestWrapper requestWrapper = (HttpServletRequestWrapper) porletRequestContext
        .getRequest();
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    UIPortal uiPortal = Util.getUIPortal();
    String portalURI = portalRequestContext.getPortalURI();
    String requestURI = requestWrapper.getRequestURI();
    String pageNodeSelected = uiPortal.getSelectedNode().getName();
    String parameters = null;
    try {
      parameters = URLDecoder.decode(StringUtils.substringAfter(requestURI, portalURI
          .concat(pageNodeSelected + "/")), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    String nodeIdentifier = null;
    String[] params = parameters.split("/");
    String repository = params[0];
    String workspace = params[1];
    Node currentNode = null;
    SessionProvider sessionProvider = null;
    Session session = null;

    try {
      RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      String userId = Util.getPortalRequestContext().getRemoteUser();
      if (userId == null) {
        sessionProvider = SessionProviderFactory.createAnonimProvider();
      } else {
        sessionProvider = SessionProviderFactory.createSessionProvider();
      }

      session = sessionProvider.getSession(workspace, manageableRepository);
      if (params.length > 2) {
        StringBuffer identifier = new StringBuffer();
        for (int i = 2; i < params.length; i++) {
          identifier.append("/").append(params[i]);
        }
        nodeIdentifier = identifier.toString();
        currentNode = (Node) session.getItem(nodeIdentifier);
      } else if (params.length == 2) {
        currentNode = session.getRootNode();
      }
    } catch (PathNotFoundException e) {
      try {
        String uuid = params[params.length - 1];
        currentNode = session.getNodeByUUID(uuid);
      } catch (ItemNotFoundException exc) {
        renderErrorMessage(context, CONTENT_NOT_FOUND_EXC);
        return;
      }
    } catch (RepositoryConfigurationException rce) {
      renderErrorMessage(context, REPOSITORY_CONFIGURATION_EXC);
      return;
    } catch (RepositoryException e) {
      renderErrorMessage(context, REPOSITORY_EXC);
      return;
    } catch (AccessControlException e) {
      renderErrorMessage(context, ACCESS_CONTROL_EXC);
      return;
    }
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    List<String> documentTypes = templateService.getDocumentTemplates(repository);
    Boolean isDocumentType = false;
    for (String docType : documentTypes) {
      if (currentNode.isNodeType(docType)) {
        isDocumentType = true;
        break;
      }
    }
    if (currentNode.isNodeType("exo:hiddenable")) {
      renderErrorMessage(context, ACCESS_CONTROL_EXC);
      return;
    } else if (isDocumentType) { // content is document
      if (hasChildren()) {
        removeChild(UIContentViewer.class);
      }
      UIContentViewer uiContentViewer = addChild(UIContentViewer.class, null,
          UIPortletApplication.VIEW_MODE);
      uiContentViewer.setNode(currentNode);
      uiContentViewer.setRepository(repository);
      uiContentViewer.setWorkspace(workspace);
      super.processRender(app, context);
    } else { // content is folder
      Writer writer = context.getWriter();
      writer.write("This is folder");
      writer.close();
    }
  }

  private void renderErrorMessage(WebuiRequestContext context, String keyBundle) throws Exception {
    Writer writer = context.getWriter();
    String message = context.getApplicationResourceBundle().getString(keyBundle);
    writer
        .write("<div style=\"height: 55px; font-size: 13px; text-align: center; padding-top: 10px;\">");
    writer.write("<span>");
    writer.write(message);
    writer.write("</span>");
    writer.write("</div>");
    writer.close();
  }

}
