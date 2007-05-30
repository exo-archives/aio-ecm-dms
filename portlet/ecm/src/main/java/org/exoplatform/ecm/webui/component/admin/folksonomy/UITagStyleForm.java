/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.folksonomy;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIFormTextAreaInput;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jan 11, 2007  
 * 2:56:02 PM
 */
@ComponentConfig( 
    lifecycle = UIFormLifecycle.class,
    //template =  "system:/groovy/webui/component/UIForm.gtmpl",
    template =  "system:/groovy/webui/component/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UITagStyleForm.UpdateStyleActionListener.class),
      @EventConfig(listeners = UITagStyleForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UITagStyleForm extends UIForm {

  final static public String STYLE_NAME = "styleName" ;
  final static public String DOCUMENT_RANGE = "documentRange" ;
  final static public String STYLE_HTML = "styleHTML" ;
  
  private Node selectedTagStyle_ ;

  public UITagStyleForm() throws Exception {
    addUIFormInput(new UIFormStringInput(STYLE_NAME, STYLE_NAME, null)) ;
    addUIFormInput(new UIFormStringInput(DOCUMENT_RANGE, DOCUMENT_RANGE, null)) ;
    addUIFormInput(new UIFormTextAreaInput(STYLE_HTML, STYLE_HTML, null)) ;
  }

  public Node getTagStyle() { return selectedTagStyle_ ; }
  
  public void setTagStyle(Node selectedTagStyle) throws Exception { 
    selectedTagStyle_ = selectedTagStyle ;      
    getUIStringInput(STYLE_NAME).setValue(selectedTagStyle_.getName()) ; 
    getUIStringInput(STYLE_NAME).setEditable(false) ;
    String range = selectedTagStyle_.getProperty(UITagStyleList.RANGE_PROP).getValue().getString() ;    
    getUIStringInput(DOCUMENT_RANGE).setValue(range) ;
    String htmlStyle = selectedTagStyle_.getProperty(UITagStyleList.HTML_STYLE_PROP).getValue().getString() ;
    getUIFormTextAreaInput(STYLE_HTML).setValue(htmlStyle) ;
  }
  
  private boolean validateRange(String range) {      
    String[] vars = null ;      
    try {
      vars = StringUtils.split(range,"..") ;
    } catch(Exception e) {  
      e.printStackTrace() ;
      return false ;        
    }                  
    if(vars == null || vars.length!= 2) return false ;
    String minRange = vars[0], maxRange = vars[1] ;
    if(!StringUtils.isNumeric(minRange)) return false ;      
    try {
      int min = Integer.parseInt(vars[0]) ;
      if(min<0) return false ;      
      if(!StringUtils.isNumeric(maxRange)) {
        if(!maxRange.equals("*")) return false ;
      } else {
        if(Integer.parseInt(maxRange)<=0) return false ;
      }
    } catch(Exception e) {    
      return false ;
    }
    return true ;
  }
  
  static public class UpdateStyleActionListener extends EventListener<UITagStyleForm> {
    public void execute(Event<UITagStyleForm> event) throws Exception {
      UITagStyleForm uiForm = event.getSource() ;
//      FolksonomyService folksonomyService = uiForm.getApplicationComponent(FolksonomyService.class) ;
      RepositoryService repositoryService = uiForm.getApplicationComponent(RepositoryService.class) ;
      CmsConfigurationService cmsConfigServ = uiForm.getApplicationComponent(CmsConfigurationService.class) ;
      Session session = repositoryService.getRepository().getSystemSession(cmsConfigServ.getWorkspace()) ;
      UIFolksonomyManager uiManager = uiForm.getAncestorOfType(UIFolksonomyManager.class) ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      String documentRange = uiForm.getUIStringInput(DOCUMENT_RANGE).getValue() ;
      String styleHTML = uiForm.getUIFormTextAreaInput(STYLE_HTML).getValue() ;
      if(!uiForm.validateRange(documentRange)) {
        uiApp.addMessage(new ApplicationMessage("UITagStyleForm.msg.range-validator", null)) ;
        return ;
      }
      try {
//        folksonomyService.updateStype(uiForm.getTagStyle().getPath(), documentRange, styleHTML) ;
        uiForm.getTagStyle().setProperty(UITagStyleList.RANGE_PROP, documentRange) ;
        uiForm.getTagStyle().setProperty(UITagStyleList.HTML_STYLE_PROP, styleHTML) ;
        uiForm.getTagStyle().save() ;
        session.save() ;
        UITagStyleList uiTagList = uiManager.getChild(UITagStyleList.class) ;
        uiTagList.updateGrid() ;
      } catch(Exception e) {
        String key = "UITagStyleForm.msg.error-update" ;
        uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING)) ;
        return ;
      }
      UIPopupWindow uiPopup = uiManager.getChild(UIPopupWindow.class) ;
      uiPopup.setShow(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  static public class CancelActionListener extends EventListener<UITagStyleForm> {
    public void execute(Event<UITagStyleForm> event) throws Exception {
      UITagStyleForm uiForm = event.getSource() ;
      UIFolksonomyManager uiManager = uiForm.getAncestorOfType(UIFolksonomyManager.class) ;
      UIPopupWindow uiPopup = uiManager.getChild(UIPopupWindow.class) ;
      uiPopup.setShow(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
}
