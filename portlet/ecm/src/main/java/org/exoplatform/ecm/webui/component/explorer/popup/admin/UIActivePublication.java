package org.exoplatform.ecm.webui.component.explorer.popup.admin;

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

import java.security.AccessControlException;

import javax.jcr.Node;

import org.exoplatform.ecm.jcr.JCRExceptionManager;
import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.ecm.publication.PublicationPresentationService;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.ecm.publication.plugins.staticdirect.UIPublicationForm;
import org.exoplatform.services.ecm.publication.plugins.staticdirect.UIStaticDirectVersionList;
import org.exoplatform.services.ecm.publication.plugins.webui.UIPublicationLogList;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *          xxx5669@yahoo.com
 * Jun 19, 2008  
 */

@ComponentConfig(
    type = UIActivePublication.class,
    template = "app:/groovy/webui/component/explorer/popup/admin/UIActivePublication.gtmpl",
    events = {                
        @EventConfig(listeners = UIActivePublication.EnablePublicationActionListener.class),
        @EventConfig(listeners = UIActivePublication.CancelActionListener.class)
    }
)

public class UIActivePublication extends UIContainer implements UIPopupComponent {
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  static  public class EnablePublicationActionListener extends EventListener<UIActivePublication> {
    public void execute(Event<UIActivePublication> event) throws Exception {    
      UIActivePublication uiActivatePublication = event.getSource();      
      UIJCRExplorer uiExplorer = uiActivatePublication.getAncestorOfType(UIJCRExplorer.class) ;
      UIPopupAction uiPopupAction = uiExplorer.getChild(UIPopupAction.class);
      Node currentNode = uiExplorer.getCurrentNode() ;
      UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
      if(currentNode.isLocked()) {
        String lockToken = Utils.getLockToken(currentNode);
        if(lockToken != null) uiExplorer.getSession().addLockToken(lockToken);
      }
      PublicationService publicationService = uiActivatePublication.getApplicationComponent(PublicationService.class);
      PublicationPresentationService publicationPresentationService = uiActivatePublication.getApplicationComponent(PublicationPresentationService.class);
      Node parentNode = currentNode.getParent() ;
      if(parentNode.isLocked()) {
        String lockToken1 = Utils.getLockToken(parentNode);
        uiExplorer.getSession().addLockToken(lockToken1) ;
      }
      try {
        publicationService.enrollNodeInLifecycle(currentNode, "StaticAndDirect");
      } catch(Exception e) {
        JCRExceptionManager.process(uiApp, e);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
      UIContainer cont = uiActivatePublication.createUIComponent(UIContainer.class, null, null);
      UIForm uiForm = publicationPresentationService.getStateUI(currentNode, cont);
      UIPublicationManager uiPublicationManager = 
        uiExplorer.createUIComponent(UIPublicationManager.class, null, null);
      uiPublicationManager.addChild(uiForm) ;
      uiPublicationManager.addChild(UIPublicationLogList.class, null, null).setRendered(false) ;
      UIPublicationLogList uiPublicationLogList = 
        uiPublicationManager.getChild(UIPublicationLogList.class);
      uiPopupAction.activate(uiPublicationManager, 700, 500) ;      
      uiPublicationLogList.setNode(uiExplorer.getCurrentNode()) ;
      uiPublicationLogList.updateGrid();
      UIPublicationForm uiPublicationForm = 
        uiForm.findFirstComponentOfType(UIPublicationForm.class);
      UIStaticDirectVersionList uiVersionTreeList = 
        uiForm.findFirstComponentOfType(UIStaticDirectVersionList.class);
      uiVersionTreeList.initVersion(currentNode);
      uiPublicationForm.initForm(currentNode) ;
    }
  }

  static public class CancelActionListener extends EventListener<UIActivePublication> {
    public void execute(Event<UIActivePublication> event) throws Exception {      
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction();
    }
  }

}
