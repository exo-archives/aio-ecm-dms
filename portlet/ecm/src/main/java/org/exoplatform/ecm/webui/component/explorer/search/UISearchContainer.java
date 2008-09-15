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
package org.exoplatform.ecm.webui.component.explorer.search;

import org.exoplatform.ecm.webui.popup.UIPopupContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Dec 27, 2006  
 * 2:04:24 PM
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UISearchContainer extends UIContainer {

  final static public String METADATA_POPUP = "MetadataPopup" ;
  final static public String NODETYPE_POPUP = "NodeTypePopup" ;
  final static public String SAVEQUERY_POPUP = "SaveQueryPopup" ;
  
  public UISearchContainer() throws Exception {
    addChild(UISimpleSearch.class, null, null) ;
    addChild(UIConstraintsForm.class, null, null).setRendered(false) ;
    UIPopupContainer popup = addChild(UIPopupContainer.class, null, METADATA_POPUP) ;
    popup.getChild(UIPopupWindow.class).setId(METADATA_POPUP + "_Popup") ;
  }
  
  public void initMetadataPopup(String fieldName) throws Exception {
    UIPopupContainer uiPopup = getChild(UIPopupContainer.class) ;
    uiPopup.getChild(UIPopupWindow.class).setId(fieldName + METADATA_POPUP) ;
    UISelectPropertyForm uiSelectForm = createUIComponent(UISelectPropertyForm.class, null, null) ;
    uiSelectForm.setFieldName(fieldName) ;
    uiPopup.activate(uiSelectForm, 500, 450) ;
  }
  
  public void initNodeTypePopup() throws Exception {
    UIPopupContainer uiPopup = getChild(UIPopupContainer.class) ;
    uiPopup.getChild(UIPopupWindow.class).setId(NODETYPE_POPUP) ;
    UINodeTypeSelectForm uiSelectForm = createUIComponent(UINodeTypeSelectForm.class, null, null) ;
    uiPopup.activate(uiSelectForm, 400, 400) ;
    uiSelectForm.setRenderNodeTypes() ;
  }
  
  public void initSaveQueryPopup(String statement, boolean isSimpleSearch, String queryType) throws Exception {
    UIPopupContainer uiPopup = getChild(UIPopupContainer.class) ;
    uiPopup.getChild(UIPopupWindow.class).setId(SAVEQUERY_POPUP) ;
    UISaveQueryForm uiSaveQueryForm = createUIComponent(UISaveQueryForm.class, null, null) ;
    uiSaveQueryForm.setStatement(statement) ;
    uiSaveQueryForm.setSimpleSearch(isSimpleSearch) ;
    uiSaveQueryForm.setQueryType(queryType) ;
    uiPopup.activate(uiSaveQueryForm, 400, 300) ;
  }
}