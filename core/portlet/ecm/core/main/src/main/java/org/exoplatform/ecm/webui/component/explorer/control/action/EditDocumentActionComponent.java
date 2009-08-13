/*
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
 */
package org.exoplatform.ecm.webui.component.explorer.control.action;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;

import org.exoplatform.ecm.webui.component.admin.manager.UIAbstractManager;
import org.exoplatform.ecm.webui.component.admin.manager.UIAbstractManagerComponent;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.ecm.webui.component.explorer.control.filter.CanSetPropertyFilter;
import org.exoplatform.ecm.webui.component.explorer.control.filter.IsCheckedOutFilter;
import org.exoplatform.ecm.webui.component.explorer.control.filter.IsDocumentFilter;
import org.exoplatform.ecm.webui.component.explorer.control.filter.IsEditableFilter;
import org.exoplatform.ecm.webui.component.explorer.control.filter.IsNotLockedFilter;
import org.exoplatform.ecm.webui.component.explorer.control.listener.UIActionBarActionListener;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIDocumentForm;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIDocumentFormController;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionContainer;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionForm;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionTypeForm;
import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          nicolas.filotto@exoplatform.com
 * 6 mai 2009  
 */
@ComponentConfig(
     events = {
       @EventConfig(listeners = EditDocumentActionComponent.EditDocumentActionListener.class)
     }
 )

public class EditDocumentActionComponent extends UIAbstractManagerComponent {

  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] {
      new IsDocumentFilter(), new IsEditableFilter(), new CanSetPropertyFilter(),
      new IsNotLockedFilter(), new IsCheckedOutFilter() });
  
  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }

  public static void editDocument(Event<? extends UIComponent> event,
                                  UIComponent uicomp,
                                  UIJCRExplorer uiExplorer,
                                  Node selectedNode,
                                  UIApplication uiApp) throws RepositoryException,
                                                      Exception,
                                                      ValueFormatException,
                                                      PathNotFoundException {
    if (selectedNode.isNodeType(Utils.EXO_ACTION)) {
      UIActionContainer uiContainer = uiExplorer.createUIComponent(UIActionContainer.class, null, null);
      uiExplorer.setIsHidePopup(true);
      UIActionForm uiActionForm =  uiContainer.getChild(UIActionForm.class);
      uiContainer.getChild(UIActionTypeForm.class).setRendered(false);
      uiActionForm.createNewAction(selectedNode, selectedNode.getPrimaryNodeType().getName(), false);
      uiActionForm.setIsUpdateSelect(false);
      uiActionForm.setNodePath(selectedNode.getPath());
      uiActionForm.setWorkspace(selectedNode.getSession().getWorkspace().getName());
      uiActionForm.setStoredPath(selectedNode.getPath());
      UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
      UIPopupContainer.activate(uiContainer, 700, 550);
      event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
    } else {
      String nodeType = null;
      if(selectedNode.hasProperty("exo:presentationType")) {
        nodeType = selectedNode.getProperty("exo:presentationType").getString();
      }else {
        nodeType = selectedNode.getPrimaryNodeType().getName();
      }        
        UIPopupContainer UIPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
        UIDocumentFormController uiController = 
          event.getSource().createUIComponent(UIDocumentFormController.class, null, "EditFormController");
        UIDocumentForm uiDocumentForm = uiController.getChild(UIDocumentForm.class);
        uiDocumentForm.setRepositoryName(uiExplorer.getRepositoryName());
        uiDocumentForm.setContentType(nodeType);
        if(uiDocumentForm.getTemplate() == null) {
          uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.template-null", null));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
        uiDocumentForm.setNodePath(selectedNode.getPath());
        uiDocumentForm.addNew(false);
        uiDocumentForm.setWorkspace(selectedNode.getSession().getWorkspace().getName());
        uiDocumentForm.setStoredPath(selectedNode.getPath());
        uiController.setRenderedChild(UIDocumentForm.class);
        UIPopupContainer.activate(uiController, 800, 600);          
        event.getRequestContext().addUIComponentToUpdateByAjax(UIPopupContainer);
        return;
    }
  }
  
  public static class EditDocumentActionListener extends UIActionBarActionListener<EditDocumentActionComponent> {
    public void processEvent(Event<EditDocumentActionComponent> event) throws Exception {
      EditDocumentActionComponent uicomp = event.getSource();
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class);
      if (nodePath != null && nodePath.length() != 0) {
        Matcher matcher = UIWorkingArea.FILE_EXPLORER_URL_SYNTAX.matcher(nodePath);
        String wsName = null;
        if (matcher.find()) {
          wsName = matcher.group(1);
          nodePath = matcher.group(2);
        } else {
          throw new IllegalArgumentException("The ObjectId is invalid '" + nodePath + "'");
        }
        Session session = uiExplorer.getSessionByWorkspace(wsName);
        UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class);
        Node selectedNode = null;
        try {
          // Use the method getNodeByPath because it is link aware
          if (!uiExplorer.getCurrentPath().equals(nodePath)) {
            uiExplorer.setCurrentPath(nodePath);
            selectedNode = uiExplorer.getCurrentNode();
          } else {
            selectedNode = uiExplorer.getNodeByPath(nodePath, session);
          }
        } catch (PathNotFoundException path) {
          uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", null,
              ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        } catch (AccessDeniedException ace) {
          uiApp.addMessage(new ApplicationMessage("UIDocumentInfo.msg.null-exception", null,
              ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        } catch (Exception e) {
          JCRExceptionManager.process(uiApp, e);
          return;
        }
      }
      Node selectedNode = uiExplorer.getCurrentNode();        
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class);
      editDocument(event, uicomp, uiExplorer, selectedNode, uiApp);
    }
  }

  @Override
  public Class<? extends UIAbstractManager> getUIAbstractManagerClass() {
    return null;
  }
}
