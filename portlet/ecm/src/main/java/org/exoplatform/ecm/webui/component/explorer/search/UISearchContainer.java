/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.explorer.UIPopupAction;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;

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
  
  public UISearchContainer() throws Exception {
    addChild(UIECMSearch.class, null, null) ;
    addChild(UIPopupAction.class, null, METADATA_POPUP) ;
  }
  
  public void setSelectedValue(String selectedValue) { selectedValue_ = selectedValue ; }
  
  public void initMetadataPopup() throws Exception {
    UIPopupWindow uiPopup = getChildById(METADATA_POPUP) ;
    if(uiPopup == null) uiPopup = addChild(UIPopupWindow.class, null, METADATA_POPUP) ;
    UIMetadataSelectForm uiSelectForm = createUIComponent(UIMetadataSelectForm.class, null, null) ;
    UIConstraintsForm uiConstraintsForm = findFirstComponentOfType(UIConstraintsForm.class) ;
    String properties = uiConstraintsForm.getUIStringInput(UIConstraintsForm.METADATA_PROPERTY).getValue() ;
    List<String> selected = new ArrayList<String> () ;
    if(properties != null && properties.length() > 0) {
      String[] array = properties.split(",") ;
      for(int i = 0; i < array.length; i ++) {
        selected.add(array[i].trim()) ;
      }
    }
    uiPopup.setUIComponent(uiSelectForm) ;
    uiPopup.setWindowSize(600, 500) ;
    uiSelectForm.renderProperties(selectedValue_) ;
    uiSelectForm.setMetadataOptions() ;
    uiPopup.setRendered(true) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
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
