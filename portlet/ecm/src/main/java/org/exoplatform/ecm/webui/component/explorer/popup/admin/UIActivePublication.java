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

import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.version.Version;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.publication.UIPublicationContainer;
import org.exoplatform.ecm.webui.component.explorer.publication.UIPublicationLogList;
import org.exoplatform.ecm.webui.component.explorer.publication.UIPublicationManager;
import org.exoplatform.ecm.webui.component.explorer.publication.UIVersionTreeList;
import org.exoplatform.services.ecm.publication.PublicationPresentationService;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.ecm.publication.plugins.staticdirect.StaticAndDirectPublicationPlugin;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
    template = "app:/groovy/webui/component/explorer/publication/UIActivePublication.gtmpl",
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
      
      PublicationService publicationService = uiActivatePublication.getApplicationComponent(PublicationService.class);
      PublicationPresentationService publicationPresentationService = uiActivatePublication.getApplicationComponent(PublicationPresentationService.class);
      publicationService.enrollNodeInLifecycle(currentNode, "StaticAndDirect");
            
      Version version = currentNode.checkin();
      currentNode.checkout();
      
      HashMap<String,String> context = new HashMap<String,String>();
      context.put("nodeVersionUUID", version.getUUID());
      context.put("visibility", StaticAndDirectPublicationPlugin.PUBLIC);
      
      publicationService.changeState(currentNode, StaticAndDirectPublicationPlugin.PUBLISHED, context);      
      UIContainer cont = uiActivatePublication.createUIComponent(UIContainer.class, null, null);
      UIForm uiForm = publicationPresentationService.getStateUI(currentNode, cont);
      UIPublicationManager uiPublicationManager = 
        uiExplorer.createUIComponent(UIPublicationManager.class, null, null);
      UIPublicationContainer uiPublicationContainer = 
        uiPublicationManager.getChild(UIPublicationContainer.class);
      uiPublicationContainer.addChild(uiForm);
      uiPublicationContainer.initChild();
      UIPublicationLogList uiPublicationLogList = 
        uiPublicationManager.getChild(UIPublicationLogList.class);
      uiPopupAction.activate(uiPublicationManager, 700, 500) ;      
      uiPublicationLogList.updateGrid();
      
      UIVersionTreeList uiVersionTreeList = uiPublicationContainer.getChild(UIVersionTreeList.class);
      uiVersionTreeList.initVersion(currentNode);
    }
  }

  static public class CancelActionListener extends EventListener<UIActivePublication> {
    public void execute(Event<UIActivePublication> event) throws Exception {      
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction();
    }
  }

}
