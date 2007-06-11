/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import org.exoplatform.ecm.jcr.UIPopupComponent;
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
public class UISearchContainer extends UIContainer implements UIPopupComponent {

  private String selectedValue_ = "dc:elementSet";
  final static public String METADATA_POPUP = "MetadataPopup" ;
  final static public String NODETYPE_POPUP = "NodeTypePopup" ;
  
  public UISearchContainer() throws Exception {
    addChild(UIECMSearch.class, null, null) ;
    UIPopupAction popup = addChild(UIPopupAction.class, null, METADATA_POPUP) ;
    popup.getChild(UIPopupWindow.class).setId(METADATA_POPUP + "_Popup") ;
  }
  
  public void setSelectedValue(String selectedValue) { selectedValue_ = selectedValue ; }
  
  public void initMetadataPopup(String fieldName) throws Exception {
    UIPopupAction uiPopup = getChild(UIPopupAction.class) ;
    uiPopup.getChild(UIPopupWindow.class).setId(fieldName + METADATA_POPUP) ;
    UIMetadataSelectForm uiSelectForm = createUIComponent(UIMetadataSelectForm.class, null, null) ;
    uiSelectForm.setFieldName(fieldName) ;
    uiPopup.activate(uiSelectForm, 600, 500) ;
    uiSelectForm.renderProperties(selectedValue_) ;
    uiSelectForm.setMetadataOptions() ;
  }
  
  public void initNodeTypePopup() throws Exception {
    UIPopupAction uiPopup = getChild(UIPopupAction.class) ;
    uiPopup.getChild(UIPopupWindow.class).setId(NODETYPE_POPUP) ;
    UINodeTypeSelectForm uiSelectForm = createUIComponent(UINodeTypeSelectForm.class, null, null) ;
    uiPopup.activate(uiSelectForm, 400, 400) ;
    uiSelectForm.setRenderNodeTypes() ;
  }

  public void activate() throws Exception {
    UIECMSearch uiSearch = getChild(UIECMSearch.class) ;
    UIJCRAdvancedSearch advanceSearch = uiSearch.getChild(UIJCRAdvancedSearch.class);
    advanceSearch.update();
    UISavedQuery uiQuery = uiSearch.getChild(UISavedQuery.class);
    uiQuery.updateGrid();
  }

  public void deActivate() throws Exception {
  }
}
