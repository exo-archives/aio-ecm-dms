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
package org.exoplatform.ecm.webui.component.explorer.sidebar;

import org.exoplatform.ecm.jcr.model.Preference;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentWorkspace;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.ecm.webui.component.explorer.control.UIAllItemsPreferenceForm;
import org.exoplatform.ecm.webui.component.explorer.search.UIShowAllFavouriteResult;
import org.exoplatform.ecm.webui.component.explorer.search.UIShowAllHiddenResult;
import org.exoplatform.ecm.webui.component.explorer.search.UIShowAllOwnedByUserResult;
import org.exoplatform.ecm.webui.component.explorer.search.UIShowAllTrashResult;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 29, 2009  
 * 7:07:27 AM
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/explorer/sidebar/UIAllItems.gtmpl",
    events = {
        @EventConfig(listeners = UIAllItems.ShowAllOwnedByUserActionListener.class),
        @EventConfig(listeners = UIAllItems.ShowAllFavouriteByUserActionListener.class),
        @EventConfig(listeners = UIAllItems.ShowAllFromTrashByUserActionListener.class),
        @EventConfig(listeners = UIAllItems.ShowAllHiddenActionListener.class),
        @EventConfig(listeners = UIAllItems.PreferencesActionListener.class),
        @EventConfig(listeners = UIAllItems.ShowAllFavouriteActionListener.class),
        @EventConfig(listeners = UIAllItems.ShowAllFromTrashActionListener.class)
    }
)
public class UIAllItems extends UIComponent {

  public UIAllItems() throws Exception {
  }

  public Preference getPreference() {
    return getAncestorOfType(UIJCRExplorer.class).getPreference();
  }
  
  static public class ShowAllFavouriteActionListener extends EventListener<UIAllItems> {
    public void execute(Event<UIAllItems> event) throws Exception {
      UIAllItems uiAllItems = event.getSource();
      UIJCRExplorer uiExplorer = uiAllItems.getAncestorOfType(UIJCRExplorer.class);

      uiExplorer.removeChildById("ViewSearch");
      UIDocumentWorkspace uiDocumentWorkspace = 
        uiExplorer.getChild(UIWorkingArea.class).getChild(UIDocumentWorkspace.class);

      UIShowAllFavouriteResult uiShowAllFavouriteResult = 
        uiDocumentWorkspace.getChildById(UIDocumentWorkspace.SHOW_ALL_FAVOURITE_RESULT);           

      long startTime = System.currentTimeMillis();
      long time = System.currentTimeMillis() - startTime;
      uiShowAllFavouriteResult.setShowNodeCase(UIShowAllFavouriteResult.SHOW_ALL_FAVOURITE);
      uiShowAllFavouriteResult.updateList();
      uiShowAllFavouriteResult.setSearchTime(time);
      uiDocumentWorkspace.setRenderedChild(UIShowAllFavouriteResult.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDocumentWorkspace);
    }
  }

  static public class ShowAllFavouriteByUserActionListener extends EventListener<UIAllItems> {
    public void execute(Event<UIAllItems> event) throws Exception {
      UIAllItems uiAllItems = event.getSource();
      UIJCRExplorer uiExplorer = uiAllItems.getAncestorOfType(UIJCRExplorer.class);

      uiExplorer.removeChildById("ViewSearch");
      UIDocumentWorkspace uiDocumentWorkspace = 
        uiExplorer.getChild(UIWorkingArea.class).getChild(UIDocumentWorkspace.class);

      UIShowAllFavouriteResult uiShowAllFavouriteResult = 
        uiDocumentWorkspace.getChildById(UIDocumentWorkspace.SHOW_ALL_FAVOURITE_RESULT);           

      long startTime = System.currentTimeMillis();
      long time = System.currentTimeMillis() - startTime;
      uiShowAllFavouriteResult.setShowNodeCase(
          UIShowAllFavouriteResult.SHOW_ALL_FAVOURITE_BY_USER);
      uiShowAllFavouriteResult.updateList();
      uiShowAllFavouriteResult.setSearchTime(time);
      uiDocumentWorkspace.setRenderedChild(UIShowAllFavouriteResult.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDocumentWorkspace);
    }
  }

  static public class ShowAllFromTrashActionListener extends EventListener<UIAllItems> {
    public void execute(Event<UIAllItems> event) throws Exception {
      UIAllItems uiAllItems = event.getSource();
      UIJCRExplorer uiExplorer = uiAllItems.getAncestorOfType(UIJCRExplorer.class);

      uiExplorer.removeChildById("ViewSearch");
      UIDocumentWorkspace uiDocumentWorkspace = 
        uiExplorer.getChild(UIWorkingArea.class).getChild(UIDocumentWorkspace.class);

      UIShowAllTrashResult uiShowAllTrashResult = 
        uiDocumentWorkspace.getChildById(UIDocumentWorkspace.SHOW_ALL_TRASH_RESULT);           

      long startTime = System.currentTimeMillis();
      long time = System.currentTimeMillis() - startTime;
      uiShowAllTrashResult.setShowNodeCase(
          UIShowAllTrashResult.SHOW_ALL_FROM_TRASH);
      uiShowAllTrashResult.updateList();
      uiShowAllTrashResult.setSearchTime(time);
      uiDocumentWorkspace.setRenderedChild(UIShowAllTrashResult.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDocumentWorkspace);
    }
  }

  static public class ShowAllFromTrashByUserActionListener extends EventListener<UIAllItems> {
    public void execute(Event<UIAllItems> event) throws Exception {
      UIAllItems uiAllItems = event.getSource();
      UIJCRExplorer uiExplorer = uiAllItems.getAncestorOfType(UIJCRExplorer.class);

      uiExplorer.removeChildById("ViewSearch");
      UIDocumentWorkspace uiDocumentWorkspace = 
        uiExplorer.getChild(UIWorkingArea.class).getChild(UIDocumentWorkspace.class);

      UIShowAllTrashResult uiShowAllTrashResult = 
        uiDocumentWorkspace.getChildById(UIDocumentWorkspace.SHOW_ALL_TRASH_RESULT);           

      long startTime = System.currentTimeMillis();
      long time = System.currentTimeMillis() - startTime;
      uiShowAllTrashResult.setShowNodeCase(
          UIShowAllTrashResult.SHOW_ALL_FROM_TRASH_BY_USER);
      uiShowAllTrashResult.updateList();
      uiShowAllTrashResult.setSearchTime(time);
      uiDocumentWorkspace.setRenderedChild(UIShowAllTrashResult.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDocumentWorkspace);
    }
  }

  static public class ShowAllHiddenActionListener extends EventListener<UIAllItems> {
    public void execute(Event<UIAllItems> event) throws Exception {
      UIAllItems UIAllItems = event.getSource();
      UIJCRExplorer uiExplorer = UIAllItems.getAncestorOfType(UIJCRExplorer.class);

      uiExplorer.removeChildById("ViewSearch");
      UIDocumentWorkspace uiDocumentWorkspace = 
        uiExplorer.getChild(UIWorkingArea.class).getChild(UIDocumentWorkspace.class);

      UIShowAllHiddenResult uiShowAllHiddenResult = 
        uiDocumentWorkspace.getChildById(UIDocumentWorkspace.SHOW_ALL_HIDDEN_RESULT);           

      long startTime = System.currentTimeMillis();
      long time = System.currentTimeMillis() - startTime;
      uiShowAllHiddenResult.updateList();
      uiShowAllHiddenResult.setSearchTime(time);
      uiDocumentWorkspace.setRenderedChild(UIShowAllHiddenResult.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDocumentWorkspace);
    }
  }

  static public class ShowAllOwnedByUserActionListener extends EventListener<UIAllItems> {
    public void execute(Event<UIAllItems> event) throws Exception {
      UIAllItems UIAllItems = event.getSource();
      UIJCRExplorer uiExplorer = UIAllItems.getAncestorOfType(UIJCRExplorer.class);

      uiExplorer.removeChildById("ViewSearch");
      UIDocumentWorkspace uiDocumentWorkspace = 
        uiExplorer.getChild(UIWorkingArea.class).getChild(UIDocumentWorkspace.class);

      UIShowAllOwnedByUserResult uiShowAllOwnedByUserResult = 
        uiDocumentWorkspace.getChildById(UIDocumentWorkspace.SHOW_ALL_OWNED_BY_USER_RESULT);           

      long startTime = System.currentTimeMillis();
      long time = System.currentTimeMillis() - startTime;
      uiShowAllOwnedByUserResult.updateList();
      uiShowAllOwnedByUserResult.setSearchTime(time);
      uiDocumentWorkspace.setRenderedChild(UIShowAllOwnedByUserResult.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDocumentWorkspace);
    }
  }  

  static public class PreferencesActionListener extends EventListener<UIAllItems> {
    public void execute(Event<UIAllItems> event) throws Exception {
      UIAllItems uiAllItems = event.getSource();
      UIJCRExplorer uiJCRExplorer = uiAllItems.getAncestorOfType(UIJCRExplorer.class);                                         
      UIPopupContainer popupAction = uiJCRExplorer.getChild(UIPopupContainer.class);
      UIAllItemsPreferenceForm uiPrefForm = popupAction.activate(UIAllItemsPreferenceForm.class,600) ;
      uiPrefForm.update(uiJCRExplorer.getPreference()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  } 

}
