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
package org.exoplatform.ecm.webui.component.explorer.sidebar ;



import org.exoplatform.ecm.webui.component.explorer.UIDocumentWorkspace;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorerPortlet;
import org.exoplatform.ecm.webui.component.explorer.UIJcrExplorerContainer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.ecm.webui.component.explorer.control.UIAllItemsPreferenceForm;
import org.exoplatform.ecm.webui.component.explorer.search.UIShowAllFavouriteResult;
import org.exoplatform.ecm.webui.component.explorer.search.UIShowAllHiddenResult;
import org.exoplatform.ecm.webui.component.explorer.search.UIShowAllOwnedByUserResult;
import org.exoplatform.ecm.webui.component.explorer.search.UIShowAllTrashResult;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          nguyenkequanghung@yahoo.com
 * oct 5, 2006
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/explorer/sidebar/UISideBar.gtmpl",
    events = {
        @EventConfig(listeners = UISideBar.CloseActionListener.class),
        @EventConfig(listeners = UISideBar.ExplorerActionListener.class),
        @EventConfig(listeners = UISideBar.RelationActionListener.class),
        @EventConfig(listeners = UISideBar.TagExplorerActionListener.class),
        @EventConfig(listeners = UISideBar.ClipboardActionListener.class),
        @EventConfig(listeners = UISideBar.ShowAllOwnedByUserActionListener.class),
//        @EventConfig(listeners = UISideBar.ShowAllFavouriteActionListener.class),
        @EventConfig(listeners = UISideBar.ShowAllFavouriteByUserActionListener.class),
//        @EventConfig(listeners = UISideBar.ShowAllFromTrashActionListener.class),
        @EventConfig(listeners = UISideBar.ShowAllFromTrashByUserActionListener.class),
        @EventConfig(listeners = UISideBar.ShowAllHiddenActionListener.class),
        @EventConfig(listeners = UISideBar.ShowDrivesAreaActionListener.class),
        @EventConfig(listeners = UISideBar.PreferencesActionListener.class),
        @EventConfig(listeners = UISideBar.SavedSearchesActionListener.class)
    }
)
public class UISideBar extends UIContainer {
  private String currentComp = "Explorer";
  
  public UISideBar() throws Exception {
    addChild(UITreeExplorer.class, null, null).getId() ;
    addChild(UIViewRelationList.class, null, null).setRendered(false) ;
    addChild(UITagExplorer.class, null, null).setRendered(false) ;
    addChild(UIClipboard.class, null, null).setRendered(false) ;
    addChild(UISavedSearches.class, null, null).setRendered(false);    
  }
  
  public String getCurrentComp() { return currentComp ; }
  public void setCurrentComp(String currentComp) { this.currentComp = currentComp ; }
  
  public boolean isSystemWorkspace() throws Exception {
    return getAncestorOfType(UIJCRExplorer.class).isSystemWorkspace() ;
  }
  
  public String getRepository() { 
    return getAncestorOfType(UIJCRExplorer.class).getRepositoryName();
  }   
  
  static public class CloseActionListener extends EventListener<UISideBar> {
    public void execute(Event<UISideBar> event) throws Exception {
      UIWorkingArea uiWorkingArea = event.getSource().getParent() ;
      uiWorkingArea.setShowSideBar(false);
      UIJCRExplorerPortlet explorerPorltet = uiWorkingArea.getAncestorOfType(UIJCRExplorerPortlet.class);
      UIJCRExplorer uiExplorer = explorerPorltet.findFirstComponentOfType(UIJCRExplorer.class);
      UIJcrExplorerContainer uiJcrExplorerContainer= explorerPorltet.getChild(UIJcrExplorerContainer.class);
      uiExplorer.refreshExplorer();      
      uiJcrExplorerContainer.setRenderedChild(UIJCRExplorer.class);      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiExplorer);      
    }
  }

  static public class ExplorerActionListener extends EventListener<UISideBar> {
    public void execute(Event<UISideBar> event) throws Exception {
      UISideBar uiSideBar = event.getSource() ;
      uiSideBar.currentComp = "Explorer" ;
      UIJCRExplorer uiExplorer = uiSideBar.getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.setSelectNode(uiExplorer.getCurrentPath()) ;
      uiExplorer.setIsViewTag(false) ;
      uiSideBar.setRenderedChild(UITreeExplorer.class) ;
      uiExplorer.updateAjax(event) ;
    }
  }

  static public class RelationActionListener extends EventListener<UISideBar> {
    public void execute(Event<UISideBar> event) throws Exception {
      UISideBar uiSideBar = event.getSource() ;
      uiSideBar.currentComp = "Relation" ;
      uiSideBar.setRenderedChild(UIViewRelationList.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSideBar.getParent()) ;
    }
  }
  
  static public class TagExplorerActionListener extends EventListener<UISideBar> {
    public void execute(Event<UISideBar> event) throws Exception {
      UISideBar uiSideBar = event.getSource() ;
      UIJCRExplorer uiExplorer = uiSideBar.getAncestorOfType(UIJCRExplorer.class) ;
      uiSideBar.currentComp = "TagExplorer" ;
      uiExplorer.setCurrentState() ;
      uiSideBar.setRenderedChild(UITagExplorer.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSideBar.getParent()) ;
    }
  }
  
  static public class ClipboardActionListener extends EventListener<UISideBar> {
    public void execute(Event<UISideBar> event) throws Exception {
      UISideBar uiSideBar = event.getSource() ;
      uiSideBar.currentComp = "Clipboard" ;
      uiSideBar.setRenderedChild(UIClipboard.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSideBar.getParent()) ;
    }
  }
  
  static public class ShowAllFavouriteActionListener extends EventListener<UISideBar> {
	  public void execute(Event<UISideBar> event) throws Exception {
	      UISideBar uiSideBar = event.getSource();
	      UIJCRExplorer uiExplorer = uiSideBar.getAncestorOfType(UIJCRExplorer.class);
	      
	      //Node currentNode = uiExplorer.getCurrentNode();
	      uiExplorer.removeChildById("ViewSearch");
	      UIDocumentWorkspace uiDocumentWorkspace = uiExplorer.getChild(UIWorkingArea.class).
	      getChild(UIDocumentWorkspace.class);
	     
	      UIShowAllFavouriteResult uiShowAllFavouriteResult = uiDocumentWorkspace.getChildById(UIDocumentWorkspace.SHOW_ALL_FAVOURITE_RESULT);           

	      long startTime = System.currentTimeMillis();
	      //uiShowAllFavouriteResult.clearAll();
	      //uiShowAllFavouriteResult.setQueryResults(queryResult);
	      //uiShowAllFavouriteResult.updateGrid(true);
	      //uiShowAllFavouriteResult.update();
	      long time = System.currentTimeMillis() - startTime;
	      uiShowAllFavouriteResult.setShowNodeCase(
	    		  UIShowAllFavouriteResult.SHOW_ALL_FAVOURITE);
	      uiShowAllFavouriteResult.updateList();
	      uiShowAllFavouriteResult.setSearchTime(time);
	      uiDocumentWorkspace.setRenderedChild(UIShowAllFavouriteResult.class);
	      event.getRequestContext().addUIComponentToUpdateByAjax(uiDocumentWorkspace);
	  }
  }
  
  static public class ShowAllFavouriteByUserActionListener extends EventListener<UISideBar> {
	  public void execute(Event<UISideBar> event) throws Exception {
	      UISideBar uiSideBar = event.getSource();
	      UIJCRExplorer uiExplorer = uiSideBar.getAncestorOfType(UIJCRExplorer.class);
	      
	      //Node currentNode = uiExplorer.getCurrentNode();
	      uiExplorer.removeChildById("ViewSearch");
	      UIDocumentWorkspace uiDocumentWorkspace = uiExplorer.getChild(UIWorkingArea.class).
	      getChild(UIDocumentWorkspace.class);
	     
	      UIShowAllFavouriteResult uiShowAllFavouriteResult = uiDocumentWorkspace.getChildById(UIDocumentWorkspace.SHOW_ALL_FAVOURITE_RESULT);           

	      long startTime = System.currentTimeMillis();
	      //uiShowAllFavouriteResult.clearAll();
	      //uiShowAllFavouriteResult.setQueryResults(queryResult);
	      //uiShowAllFavouriteResult.updateGrid(true);
	      //uiShowAllFavouriteResult.update();
	      long time = System.currentTimeMillis() - startTime;
	      uiShowAllFavouriteResult.setShowNodeCase(
	    		  UIShowAllFavouriteResult.SHOW_ALL_FAVOURITE_BY_USER);
	      uiShowAllFavouriteResult.updateList();
	      uiShowAllFavouriteResult.setSearchTime(time);
	      uiDocumentWorkspace.setRenderedChild(UIShowAllFavouriteResult.class);
	      event.getRequestContext().addUIComponentToUpdateByAjax(uiDocumentWorkspace);
	  }
  }
  
  static public class ShowAllFromTrashActionListener extends EventListener<UISideBar> {
	  public void execute(Event<UISideBar> event) throws Exception {
	      UISideBar uiSideBar = event.getSource();
	      UIJCRExplorer uiExplorer = uiSideBar.getAncestorOfType(UIJCRExplorer.class);
	      
	      //Node currentNode = uiExplorer.getCurrentNode();
	      uiExplorer.removeChildById("ViewSearch");
	      UIDocumentWorkspace uiDocumentWorkspace = uiExplorer.getChild(UIWorkingArea.class).
	      getChild(UIDocumentWorkspace.class);
	     
	      UIShowAllTrashResult uiShowAllTrashResult = uiDocumentWorkspace.getChildById(UIDocumentWorkspace.SHOW_ALL_TRASH_RESULT);           

	      long startTime = System.currentTimeMillis();
	      //uiShowAllFavouriteResult.clearAll();
	      //uiShowAllFavouriteResult.setQueryResults(queryResult);
	      //uiShowAllFavouriteResult.updateGrid(true);
	      //uiShowAllFavouriteResult.update();
	      long time = System.currentTimeMillis() - startTime;
	      uiShowAllTrashResult.setShowNodeCase(
	    		  UIShowAllTrashResult.SHOW_ALL_FROM_TRASH);
	      uiShowAllTrashResult.updateList();
	      uiShowAllTrashResult.setSearchTime(time);
	      uiDocumentWorkspace.setRenderedChild(UIShowAllTrashResult.class);
	      event.getRequestContext().addUIComponentToUpdateByAjax(uiDocumentWorkspace);
	  }
  }
  
  static public class ShowAllFromTrashByUserActionListener extends EventListener<UISideBar> {
	  public void execute(Event<UISideBar> event) throws Exception {
	      UISideBar uiSideBar = event.getSource();
	      UIJCRExplorer uiExplorer = uiSideBar.getAncestorOfType(UIJCRExplorer.class);
	      
	      //Node currentNode = uiExplorer.getCurrentNode();
	      uiExplorer.removeChildById("ViewSearch");
	      UIDocumentWorkspace uiDocumentWorkspace = uiExplorer.getChild(UIWorkingArea.class).
	      getChild(UIDocumentWorkspace.class);
	     
	      UIShowAllTrashResult uiShowAllTrashResult = uiDocumentWorkspace.getChildById(UIDocumentWorkspace.SHOW_ALL_TRASH_RESULT);           

	      long startTime = System.currentTimeMillis();
	      //uiShowAllFavouriteResult.clearAll();
	      //uiShowAllFavouriteResult.setQueryResults(queryResult);
	      //uiShowAllFavouriteResult.updateGrid(true);
	      //uiShowAllFavouriteResult.update();
	      long time = System.currentTimeMillis() - startTime;
	      uiShowAllTrashResult.setShowNodeCase(
	    		  UIShowAllTrashResult.SHOW_ALL_FROM_TRASH_BY_USER);
	      uiShowAllTrashResult.updateList();
	      uiShowAllTrashResult.setSearchTime(time);
	      uiDocumentWorkspace.setRenderedChild(UIShowAllTrashResult.class);
	      event.getRequestContext().addUIComponentToUpdateByAjax(uiDocumentWorkspace);
	  }
  }
  
  static public class ShowAllHiddenActionListener extends EventListener<UISideBar> {
	  public void execute(Event<UISideBar> event) throws Exception {
	      UISideBar uiSideBar = event.getSource();
	      UIJCRExplorer uiExplorer = uiSideBar.getAncestorOfType(UIJCRExplorer.class);
	      
	      //Node currentNode = uiExplorer.getCurrentNode();
	      uiExplorer.removeChildById("ViewSearch");
	      UIDocumentWorkspace uiDocumentWorkspace = uiExplorer.getChild(UIWorkingArea.class).
	      getChild(UIDocumentWorkspace.class);
	     
	      UIShowAllHiddenResult uiShowAllHiddenResult = uiDocumentWorkspace.getChildById(UIDocumentWorkspace.SHOW_ALL_HIDDEN_RESULT);           

	      long startTime = System.currentTimeMillis();
	      //uiShowAllFavouriteResult.clearAll();
	      //uiShowAllFavouriteResult.setQueryResults(queryResult);
	      //uiShowAllFavouriteResult.updateGrid(true);
	      //uiShowAllFavouriteResult.update();
	      long time = System.currentTimeMillis() - startTime;
	      uiShowAllHiddenResult.updateList();
	      uiShowAllHiddenResult.setSearchTime(time);
	      uiDocumentWorkspace.setRenderedChild(UIShowAllHiddenResult.class);
	      event.getRequestContext().addUIComponentToUpdateByAjax(uiDocumentWorkspace);
	  }
  }
  
  static public class ShowAllOwnedByUserActionListener extends EventListener<UISideBar> {
	  public void execute(Event<UISideBar> event) throws Exception {
	      UISideBar uiSideBar = event.getSource();
	      UIJCRExplorer uiExplorer = uiSideBar.getAncestorOfType(UIJCRExplorer.class);
	      
	      //Node currentNode = uiExplorer.getCurrentNode();
	      uiExplorer.removeChildById("ViewSearch");
	      UIDocumentWorkspace uiDocumentWorkspace = uiExplorer.getChild(UIWorkingArea.class).
	      getChild(UIDocumentWorkspace.class);
	     
	      UIShowAllOwnedByUserResult uiShowAllOwnedByUserResult = uiDocumentWorkspace.getChildById(UIDocumentWorkspace.SHOW_ALL_OWNED_BY_USER_RESULT);           

	      long startTime = System.currentTimeMillis();
	      //uiShowAllFavouriteResult.clearAll();
	      //uiShowAllFavouriteResult.setQueryResults(queryResult);
	      //uiShowAllFavouriteResult.updateGrid(true);
	      //uiShowAllFavouriteResult.update();
	      long time = System.currentTimeMillis() - startTime;
	      uiShowAllOwnedByUserResult.updateList();
	      uiShowAllOwnedByUserResult.setSearchTime(time);
	      uiDocumentWorkspace.setRenderedChild(UIShowAllOwnedByUserResult.class);
	      event.getRequestContext().addUIComponentToUpdateByAjax(uiDocumentWorkspace);
	  }
  }
  
  static public class ShowDrivesAreaActionListener extends EventListener<UISideBar> {
    public void execute(Event<UISideBar> event) throws Exception {
    }
  }

  static public class SavedSearchesActionListener extends EventListener<UISideBar> {
    public void execute(Event<UISideBar> event) throws Exception {
      UISideBar uiSideBar = event.getSource() ;
      uiSideBar.currentComp = "SavedSearches" ;
      uiSideBar.setRenderedChild(UISavedSearches.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSideBar) ;
    }
  }
  
  static public class PreferencesActionListener extends EventListener<UISideBar> {
	    public void execute(Event<UISideBar> event) throws Exception {
	      UISideBar sideBar = event.getSource();
	      UIJCRExplorer uiJCRExplorer = sideBar.getAncestorOfType(UIJCRExplorer.class);                                         
	      UIPopupContainer popupAction = uiJCRExplorer.getChild(UIPopupContainer.class);
	      UIAllItemsPreferenceForm uiPrefForm = popupAction.activate(UIAllItemsPreferenceForm.class,600) ;
	      uiPrefForm.update(uiJCRExplorer.getPreference()) ;
	      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
	    }
	  }  
}
