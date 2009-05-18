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
package org.exoplatform.ecm.webui.component.admin.folksonomy;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.ecm.webui.component.admin.manager.UIAbstractManager;
import org.exoplatform.services.cms.folksonomy.FolksonomyService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jan 11, 2007  
 * 2:22:08 PM
 */
@ComponentConfig( 
    lifecycle = UIContainerLifecycle.class,
    events = @EventConfig(listeners = UIFolksonomyManager.EditStyleActionListener.class)
)
public class UIFolksonomyManager extends UIAbstractManager {
  
  public UIFolksonomyManager() throws Exception {
    addChild(UITagStyleList.class, null, null) ;
  }
  
  public void refresh() throws Exception {
    update();
  }
  
  public void update() throws Exception {
    getChild(UITagStyleList.class).updateGrid() ;
  }
  public void initTaggingFormPopup(Node selectedTagStyle) throws Exception {
    removeChildById("FolksonomyPopup") ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, "FolksonomyPopup") ;
    uiPopup.setWindowSize(600, 500) ;
    UITagStyleForm uiForm = createUIComponent(UITagStyleForm.class, null, null) ;
    uiForm.setTagStyle(selectedTagStyle) ;
    uiPopup.setUIComponent(uiForm) ;
    uiPopup.setRendered(true) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }
  
  public Node getSelectedTagStyle(String tagStyleName) throws Exception {
    FolksonomyService folksonomyService = getApplicationComponent(FolksonomyService.class) ;
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    for(Node tagStyle: folksonomyService.getAllTagStyle(repository)) {
      if(tagStyle.getName().equals(tagStyleName)) return tagStyle ;
    }
    return null ;
  }
  
  static public class EditStyleActionListener extends EventListener<UIFolksonomyManager> {
    public void execute(Event<UIFolksonomyManager> event) throws Exception {
      UIFolksonomyManager uiManager = event.getSource() ;
      String selectedName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Node selectedTagStyle = uiManager.getSelectedTagStyle(selectedName) ;
      uiManager.initTaggingFormPopup(selectedTagStyle) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
}
