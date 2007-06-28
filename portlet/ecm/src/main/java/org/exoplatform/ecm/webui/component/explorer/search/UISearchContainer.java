/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import org.exoplatform.ecm.webui.component.UIPopupAction;
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
    UIPopupAction popup = addChild(UIPopupAction.class, null, METADATA_POPUP) ;
    popup.getChild(UIPopupWindow.class).setId(METADATA_POPUP + "_Popup") ;
  }
  
  public void initMetadataPopup(String fieldName) throws Exception {
    UIPopupAction uiPopup = getChild(UIPopupAction.class) ;
    uiPopup.getChild(UIPopupWindow.class).setId(fieldName + METADATA_POPUP) ;
    UISelectPropertyForm uiSelectForm = createUIComponent(UISelectPropertyForm.class, null, null) ;
    uiSelectForm.setFieldName(fieldName) ;
    uiPopup.activate(uiSelectForm, 500, 450) ;
  }
  
  public void initNodeTypePopup() throws Exception {
    UIPopupAction uiPopup = getChild(UIPopupAction.class) ;
    uiPopup.getChild(UIPopupWindow.class).setId(NODETYPE_POPUP) ;
    UINodeTypeSelectForm uiSelectForm = createUIComponent(UINodeTypeSelectForm.class, null, null) ;
    uiPopup.activate(uiSelectForm, 400, 400) ;
    uiSelectForm.setRenderNodeTypes() ;
  }
  
  public void initSaveQueryPopup(String statement, boolean isSimpleSearch, String queryType) throws Exception {
    UIPopupAction uiPopup = getChild(UIPopupAction.class) ;
    uiPopup.getChild(UIPopupWindow.class).setId(SAVEQUERY_POPUP) ;
    UISaveQueryForm uiSaveQueryForm = createUIComponent(UISaveQueryForm.class, null, null) ;
    uiSaveQueryForm.setStatement(statement) ;
    uiSaveQueryForm.setSimpleSearch(isSimpleSearch) ;
    uiSaveQueryForm.setQueryType(queryType) ;
    uiPopup.activate(uiSaveQueryForm, 400, 300) ;
  }
}