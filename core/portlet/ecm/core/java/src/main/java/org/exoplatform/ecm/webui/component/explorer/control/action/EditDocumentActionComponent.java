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

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.control.filter.CanSetPropertyFilter;
import org.exoplatform.ecm.webui.component.explorer.control.filter.IsCheckedOutFilter;
import org.exoplatform.ecm.webui.component.explorer.control.filter.IsNotLockedFilter;
import org.exoplatform.ecm.webui.component.explorer.control.listener.UIActionBarActionListener;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIDocumentForm;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIDocumentFormController;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionContainer;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionForm;
import org.exoplatform.ecm.webui.component.explorer.popup.admin.UIActionTypeForm;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.UIExtensionFilter;
import org.exoplatform.webui.ext.UIExtensionFilters;

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

public class EditDocumentActionComponent extends UIComponent {

  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[]{new CanSetPropertyFilter(), new IsNotLockedFilter(), new IsCheckedOutFilter()});
  
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
      TemplateService tservice = uicomp.getApplicationComponent(TemplateService.class);
      String repository = uicomp.getAncestorOfType(UIJCRExplorer.class).getRepositoryName();
      List<String> documentNodeType = tservice.getDocumentTemplates(repository);
      String nodeType = null;
      if(selectedNode.hasProperty("exo:presentationType")) {
        nodeType = selectedNode.getProperty("exo:presentationType").getString();
      }else {
        nodeType = selectedNode.getPrimaryNodeType().getName();
      }        
      if(documentNodeType.contains(nodeType)){
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
      } else {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.not-support", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
    }
  }
  
  public static class EditDocumentActionListener extends UIActionBarActionListener<EditDocumentActionComponent> {
    public void processEvent(Event<EditDocumentActionComponent> event) throws Exception {
      EditDocumentActionComponent uicomp = event.getSource();
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class);
      Node selectedNode = uiExplorer.getCurrentNode();        
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class);
      editDocument(event, uicomp, uiExplorer, selectedNode, uiApp);
    }
  }
}
