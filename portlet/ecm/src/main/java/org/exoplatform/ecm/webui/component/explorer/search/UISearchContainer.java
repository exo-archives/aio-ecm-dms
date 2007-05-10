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
    UIPopupAction popup = addChild(UIPopupAction.class, null, METADATA_POPUP) ;
    popup.getChild(UIPopupWindow.class).setId(METADATA_POPUP + "_Popup") ;
  }
  
  public void setSelectedValue(String selectedValue) { selectedValue_ = selectedValue ; }
  
  public void initMetadataPopup(String fieldName) throws Exception {
    UIPopupAction uiPopup = getChild(UIPopupAction.class) ;
    uiPopup.getChild(UIPopupWindow.class).setId(fieldName + METADATA_POPUP) ;
    UIMetadataSelectForm uiSelectForm = createUIComponent(UIMetadataSelectForm.class, null, null) ;
    UIConstraintsForm uiConstraintsForm = findFirstComponentOfType(UIConstraintsForm.class) ;
    String properties = uiConstraintsForm.getUIStringInput(fieldName).getValue() ;
    List<String> selected = new ArrayList<String> () ;
    if(properties != null && properties.length() > 0) {
      String[] array = properties.split(",") ;
      for(int i = 0; i < array.length; i ++) {
        selected.add(array[i].trim()) ;
      }
    }
    uiSelectForm.setFieldName(fieldName) ;
    uiPopup.activate(uiSelectForm, 600, 500) ;
    uiSelectForm.renderProperties(selectedValue_) ;
    uiSelectForm.setMetadataOptions() ;
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
