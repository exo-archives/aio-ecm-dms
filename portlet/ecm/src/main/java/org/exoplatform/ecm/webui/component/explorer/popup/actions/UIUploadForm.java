/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import java.io.ByteArrayInputStream;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Value;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.ecm.jcr.JCRExceptionManager;
import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.portal.component.view.Util;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.i18n.MultiLanguageService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIFormUploadInput;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : nqhungvn
 *          nguyenkequanghung@yahoo.com
 * July 3, 2006
 * 10:07:15 AM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl",
    events = {
      @EventConfig(listeners = UIUploadForm.SaveActionListener.class), 
      @EventConfig(listeners = UIUploadForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)

public class UIUploadForm extends UIForm implements UIPopupComponent {
  
  final static public String FIELD_NAME =  "name" ;
  final static public String FIELD_UPLOAD = "upload" ;  
  final public static String JCR_LASTMODIFIED = "jcr:lastModified" ;
  
  private boolean isMultiLanguage_ = false ;
  private String language_ = null ;
  
  public UIUploadForm() throws Exception {
    setMultiPart(true) ;
    addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null)) ;
    UIFormUploadInput uiInput = new UIFormUploadInput(FIELD_UPLOAD, FIELD_UPLOAD) ;
    uiInput.setEditable(false);
    addUIFormInput(uiInput) ;
  }
  
  public void setIsMultiLanguage(boolean isMultiLanguage, String language) { 
    isMultiLanguage_ = isMultiLanguage ;
    language_ = language ;
  }
  
  private boolean isMultiLanguage() { return isMultiLanguage_ ; }
  
  private String getLanguageSelected() { return language_ ; }
  
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
  static  public class SaveActionListener extends EventListener<UIUploadForm> {
    public void execute(Event<UIUploadForm> event) throws Exception {
      UIUploadForm uiForm = event.getSource();
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
      UIFormUploadInput input = (UIFormUploadInput)uiForm.getUIInput(FIELD_UPLOAD);
      /*POIPropertiesReader reader = new POIPropertiesReader();
      Properties pro = reader.readDCProperties(input.getUploadDataAsStream());
      System.out.println("pro ==== " + pro) ;
      System.out.println("pro ==== " + pro.toString()) ;*/
      
      if(input.getUploadResource() == null) {
        uiApp.addMessage(new ApplicationMessage("UIUploadForm.msg.fileName-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
        
      }
      String fileName = input.getUploadResource().getFileName() ;
      MultiLanguageService multiLangService = uiForm.getApplicationComponent(MultiLanguageService.class) ;
      if(fileName == null || fileName.equals("")) {
        uiApp.addMessage(new ApplicationMessage("UIUploadForm.msg.fileName-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      
      byte[] content = input.getUploadData() ;
      String name = uiForm.getUIStringInput(FIELD_NAME).getValue() ;
      if(name == null) name = fileName;
      MimeTypeResolver mimeTypeSolver = new MimeTypeResolver() ;
      String mimeType = mimeTypeSolver.getMimeType(name) ;
      //String mimeType = input.getUploadResource().getMimeType() ;
      Node selectedNode = uiExplorer.getCurrentNode();      
      
      boolean isExist = selectedNode.hasNode(name) ;
      try {
        String pers = PermissionType.ADD_NODE + "," + PermissionType.SET_PROPERTY ;
        selectedNode.getSession().checkPermission(selectedNode.getPath(), pers);        
        if(uiForm.isMultiLanguage()) {
          ValueFactoryImpl valueFactory = (ValueFactoryImpl) uiExplorer.getSession().getValueFactory() ;
          Value contentValue = valueFactory.createValue(new ByteArrayInputStream(content)) ;
          multiLangService.addFileLanguage(selectedNode, contentValue, uiForm.getLanguageSelected(), false) ;
          uiExplorer.setIsHidePopup(true) ;
          UIMultiLanguageManager uiManager = uiForm.getAncestorOfType(UIMultiLanguageManager.class) ;
          UIMultiLanguageForm uiMultiForm = uiManager.getChild(UIMultiLanguageForm.class) ;
          uiMultiForm.updateSelect(uiExplorer.getCurrentNode()) ;
          uiManager.setRenderedChild(UIMultiLanguageForm.class) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
        } else {
          if(!isExist) {            
            Map<String,JcrInputProperty> inputProperties = new HashMap<String,JcrInputProperty>() ;            
            JcrInputProperty nodeInput = new JcrInputProperty() ;
            nodeInput.setJcrPath("/node") ;
            nodeInput.setValue(name) ;
            nodeInput.setMixintype("mix:i18n,mix:votable,mix:commentable") ;
            nodeInput.setType(JcrInputProperty.NODE) ;
            inputProperties.put("/node",nodeInput) ;
            
            JcrInputProperty jcrContent = new JcrInputProperty() ;
            jcrContent.setJcrPath("/node/jcr:content") ;
            jcrContent.setValue("") ;
            jcrContent.setMixintype("dc:elementSet") ;
            jcrContent.setNodetype(Utils.NT_RESOURCE) ;
            jcrContent.setType(JcrInputProperty.NODE) ;
            inputProperties.put("/node/jcr:content",jcrContent) ;
            
            JcrInputProperty jcrData = new JcrInputProperty() ;
            jcrData.setJcrPath("/node/jcr:content/jcr:data") ;            
            jcrData.setValue(content) ;          
            inputProperties.put("/node/jcr:content/jcr:data",jcrData) ; 
            
            JcrInputProperty jcrMimeType = new JcrInputProperty() ;
            jcrMimeType.setJcrPath("/node/jcr:content/jcr:mimeType") ;
            jcrMimeType.setValue(mimeType) ;          
            inputProperties.put("/node/jcr:content/jcr:mimeType",jcrMimeType) ;
            
            JcrInputProperty jcrLastModified = new JcrInputProperty() ;
            jcrLastModified.setJcrPath("/node/jcr:content/jcr:lastModified") ;
            jcrLastModified.setValue(new GregorianCalendar()) ;
            inputProperties.put("/node/jcr:content/jcr:lastModified",jcrLastModified) ;
            
            JcrInputProperty jcrEncoding = new JcrInputProperty() ;
            jcrEncoding.setJcrPath("/node/jcr:content/jcr:encoding") ;
            jcrEncoding.setValue("UTF-8") ;
            inputProperties.put("/node/jcr:content/jcr:encoding",jcrEncoding) ;          
            CmsService cmsService = (CmsService)PortalContainer.getComponent(CmsService.class) ;
            cmsService.storeNode(Utils.NT_FILE, selectedNode, inputProperties,
                                 true, Util.getUIPortal().getOwner()) ;
            selectedNode.save() ;
            selectedNode.getSession().save() ;                        
          } else {
            Node node = selectedNode.getNode(name) ;
            if(!node.isNodeType(Utils.MIX_VERSIONABLE)) {
              node.addMixin(Utils.MIX_VERSIONABLE) ;            
              node.save() ;            
              node.checkin() ;
              node.checkout() ;
            }
            Node contentNode = node.getNode(Utils.JCR_CONTENT);
            contentNode.setProperty(Utils.JCR_DATA, new ByteArrayInputStream(content));
            contentNode.setProperty(Utils.JCR_MIMETY, mimeType);
            contentNode.setProperty(JCR_LASTMODIFIED, new GregorianCalendar());
            node.save() ;       
            node.checkin() ;
            node.checkout() ;
          }
        }
        uiExplorer.getSession().save() ;
        uiExplorer.updateAjax(event);
      } catch(Exception e) {
        //e.printStackTrace() ;
        JCRExceptionManager.process(uiApp, e);
        return ;
      }
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIUploadForm> {
    public void execute(Event<UIUploadForm> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;
    }
  }
}
