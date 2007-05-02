/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.ecm.webui.component.UIJCRBrowser;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Apr 25, 2007 9:05:38 AM
 */
@ComponentConfig (template = "app:/groovy/webui/component/explorer/UIEditModeController.gtmpl")
public class UIEditModeController extends UIContainer {
  
  private String defaultDocument_ ;
  private static String DEFAULT_VALUE = "exo:article" ;
  private static String DEFAULT_WORKSPACE = "production" ;
  
  public UIEditModeController() throws Exception {
    addChild(UIEditModeDocumentType.class, null, null) ;
    UIEditModeDocumentForm uiEditModeForm = createUIComponent(UIEditModeDocumentForm.class, null, null) ;
    uiEditModeForm.setTemplateNode(DEFAULT_VALUE) ;
    uiEditModeForm.setIsEditMode(true) ;
    addChild(uiEditModeForm) ;  
  }
  
  public void initPopupJCRBrowser(String workspaceName) throws Exception {
    removeChild(UIPopupWindow.class) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, null);
    uiPopup.setWindowSize(610, 300);
    if(workspaceName == null || workspaceName.equals("")) workspaceName = DEFAULT_WORKSPACE ;
    UIJCRBrowser uiJCRBrowser = createUIComponent(UIJCRBrowser.class, null, null) ;
    uiJCRBrowser.setWorkspace(workspaceName) ;
    uiPopup.setUIComponent(uiJCRBrowser);
    UIEditModeDocumentType uiEditModeDocumentType = getChild(UIEditModeDocumentType.class) ;
    uiJCRBrowser.setComponent(uiEditModeDocumentType, new String[] {UIEditModeDocumentType.FIELD_SAVEDPATH}) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }
  
  public void initEditMode() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    UIEditModeDocumentType uiSelectForm = getChild(UIEditModeDocumentType.class) ;
    UIFormSelectBox uiSelectBox = uiSelectForm.getUIFormSelectBox(UIEditModeDocumentType.FIELD_SELECT) ;
    boolean hasDefaultDoc = false ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    List templates = templateService.getDocumentTemplates() ;
    try {
      for(int i = 0; i < templates.size(); i ++) {
        String nodeTypeName = templates.get(i).toString() ;
        if(nodeTypeName.equals(DEFAULT_VALUE)) {
          defaultDocument_ = DEFAULT_VALUE ;
          hasDefaultDoc = true ;
        }
        options.add(new SelectItemOption<String>(nodeTypeName)) ;
      }
      uiSelectBox.setOptions(options) ;
      if(hasDefaultDoc) {
        uiSelectBox.setValue(defaultDocument_);
      } else if(options.size() > 0) {
        defaultDocument_ = options.get(0).getValue() ;
        uiSelectBox.setValue(defaultDocument_);
      } 
    } catch(Exception e) {
      e.printStackTrace() ;
    }
    getChild(UIEditModeDocumentForm.class).resetProperties();
  }
}
