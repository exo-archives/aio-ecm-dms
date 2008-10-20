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

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.popup.UIPopupComponent;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.services.cms.thumbnail.impl.AddThumbnailAction;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.ext.action.SessionActionCatalog;
import org.exoplatform.services.jcr.impl.ext.action.SessionEventMatcher;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 20, 2008 2:21:40 PM
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/explorer/popup/action/UIEnableThumbnail.gtmpl",
    events = {                
        @EventConfig(listeners = UIEnableThumbnail.EnableThumbnailActionListener.class),
        @EventConfig(listeners = UIEnableThumbnail.CancelActionListener.class)
    }
)
public class UIEnableThumbnail extends UIComponent implements UIPopupComponent {
  
public UIEnableThumbnail() throws Exception {}
  
  public void activate() throws Exception {}

  public void deActivate() throws Exception {}
  
  static public class EnableThumbnailActionListener extends EventListener<UIEnableThumbnail> {
    public void execute(Event<UIEnableThumbnail> event) throws Exception {
      UIEnableThumbnail uiEnableThumbnail = event.getSource();
      UIJCRExplorer uiExplorer = uiEnableThumbnail.getAncestorOfType(UIJCRExplorer.class);
      Node currentNode = uiExplorer.getCurrentNode();
      if(currentNode.canAddMixin(Utils.EXO_THUMBNAILABLE)) {
        currentNode.addMixin(Utils.EXO_THUMBNAILABLE);
      }
      currentNode.save();
      QPath[] qPath = new QPath[] { ((NodeImpl) currentNode).getInternalPath() };
      String[] wsNames = { currentNode.getSession().getWorkspace().getName() };
      SessionActionCatalog sessionActionCatalog = 
        uiEnableThumbnail.getApplicationComponent(SessionActionCatalog.class);
      SessionEventMatcher matcher = new SessionEventMatcher(javax.jcr.observation.Event.NODE_ADDED,
          qPath, true, wsNames, null);
      sessionActionCatalog.addAction(matcher, new AddThumbnailAction());
      uiExplorer.getSession().save();   
      uiExplorer.updateAjax(event);
    }
  }
  
  static public class CancelActionListener extends EventListener<UIEnableThumbnail> {
    public void execute(Event<UIEnableThumbnail> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      uiExplorer.cancelAction();
    }
  }

}
