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
package org.exoplatform.ecm.webui.component.explorer.upload;

import java.io.ByteArrayInputStream;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Value;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.ecm.jcr.JCRExceptionManager;
import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIMultiLanguageForm;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIMultiLanguageManager;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.i18n.MultiLanguageService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Created by The eXo Platform SARL
 * Author : nqhungvn
 *          nguyenkequanghung@yahoo.com
 * July 3, 2006
 * 10:07:15 AM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIUploadForm.SaveActionListener.class), 
      @EventConfig(listeners = UIUploadForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)

public class UIUploadForm extends UIForm implements UIPopupComponent {

  final static public String FIELD_NAME =  "name" ;
  final static public String FIELD_UPLOAD = "upload" ;  

  private boolean isMultiLanguage_ = false ;
  private String language_ = null ;
  private boolean isDefault_ = false ;

  public UIUploadForm() throws Exception {
    setMultiPart(true) ;
    addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null)) ;
    UIFormUploadInput uiInput = new UIFormUploadInput(FIELD_UPLOAD, FIELD_UPLOAD) ;
    addUIFormInput(uiInput) ;
  }

  public void setIsMultiLanguage(boolean isMultiLanguage, String language) { 
    isMultiLanguage_ = isMultiLanguage ;
    language_ = language ;
  }

  public boolean isMultiLanguage() { return isMultiLanguage_ ; }

  public void setIsDefaultLanguage(boolean isDefault) { isDefault_ = isDefault ; }

  private String getLanguageSelected() { return language_ ; }

  public void activate() throws Exception {}
  public void deActivate() throws Exception {}

  static  public class SaveActionListener extends EventListener<UIUploadForm> {
    public void execute(Event<UIUploadForm> event) throws Exception {
      UIUploadForm uiForm = event.getSource();
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
      UIFormUploadInput input = (UIFormUploadInput)uiForm.getUIInput(FIELD_UPLOAD);
      if(input.getUploadResource() == null) {
        uiApp.addMessage(new ApplicationMessage("UIUploadForm.msg.fileName-error", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String fileName = input.getUploadResource().getFileName() ;
      MultiLanguageService multiLangService = uiForm.getApplicationComponent(MultiLanguageService.class) ;
      if(fileName == null || fileName.equals("")) {
          uiApp.addMessage(new ApplicationMessage("UIUploadForm.msg.fileName-error", null, 
                                                  ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
      }      
      String[] arrFilterChar = {"&", "$", "@", ":", "]", "[", "*", "%", "!", "+", "(", ")", "'", "#", ";", "}", "{"} ;
      for(String filterChar : arrFilterChar) {
        if (fileName.indexOf(filterChar) > -1) {
          uiApp.addMessage(new ApplicationMessage("UIUploadForm.msg.fileName-invalid", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }

      byte[] content = input.getUploadData() ;
      String name = uiForm.getUIStringInput(FIELD_NAME).getValue() ;
      if (name == null) name = fileName;      
      
      for(String filterChar : arrFilterChar) {
        if(name.indexOf(filterChar) > -1) {
          uiApp.addMessage(new ApplicationMessage("UIUploadForm.msg.fileName-invalid", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
/*
      InputStream in = new ByteArrayInputStream(content) ;
      float fileLeng = in.available()/1024 ;
      if(fileLeng > 20240) {
        uiApp.addMessage(new ApplicationMessage("UIUploadForm.msg.fileSize-too-big", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
*/
      MimeTypeResolver mimeTypeSolver = new MimeTypeResolver() ;
      String mimeType = mimeTypeSolver.getMimeType(fileName) ;
      //String mimeType = input.getUploadResource().getMimeType() ;
      Node selectedNode = uiExplorer.getCurrentNode();      
      boolean isExist = selectedNode.hasNode(name) ;
      String newNodeUUID = null;
      try {
        String pers = PermissionType.ADD_NODE + "," + PermissionType.SET_PROPERTY ;
        selectedNode.getSession().checkPermission(selectedNode.getPath(), pers);        
        if(uiForm.isMultiLanguage()) {
          ValueFactoryImpl valueFactory = (ValueFactoryImpl) uiExplorer.getSession().getValueFactory() ;
          Value contentValue = valueFactory.createValue(new ByteArrayInputStream(content)) ;
          multiLangService.addFileLanguage(selectedNode, name, contentValue, mimeType, uiForm.getLanguageSelected(), uiExplorer.getRepositoryName(), uiForm.isDefault_) ;
          uiExplorer.setIsHidePopup(true) ;
          UIMultiLanguageManager uiManager = uiForm.getAncestorOfType(UIMultiLanguageManager.class) ;
          UIMultiLanguageForm uiMultiForm = uiManager.getChild(UIMultiLanguageForm.class) ;
          uiMultiForm.updateSelect(uiExplorer.getCurrentNode()) ;
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
            CmsService cmsService = uiForm.getApplicationComponent(CmsService.class) ;
            String repository = uiForm.getAncestorOfType(UIJCRExplorer.class).getRepositoryName() ;
            newNodeUUID = cmsService.storeNodeByUUID(Utils.NT_FILE, selectedNode, inputProperties, true,repository) ;
            selectedNode.save() ;
            selectedNode.getSession().save() ;                        
          } else {
            Node node = selectedNode.getNode(name) ;
            if(!node.getPrimaryNodeType().isNodeType(Utils.NT_FILE)) {
              Object[] args = { name } ;
              uiApp.addMessage(new ApplicationMessage("UIUploadForm.msg.name-is-exist", args, 
                                                      ApplicationMessage.WARNING)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
              return ;
            }
            Node contentNode = node.getNode(Utils.JCR_CONTENT);
            if(node.isNodeType(Utils.MIX_VERSIONABLE)) {              
              if(!node.isCheckedOut()) node.checkout() ; 
              contentNode.setProperty(Utils.JCR_DATA, new ByteArrayInputStream(content));
              contentNode.setProperty(Utils.JCR_MIMETYPE, mimeType);
              contentNode.setProperty(Utils.JCR_LASTMODIFIED, new GregorianCalendar());
              node.save() ;       
              node.checkin() ;
              node.checkout() ;
            }else {
              contentNode.setProperty(Utils.JCR_DATA, new ByteArrayInputStream(content));              
            }
            if(node.isNodeType("exo:datetime")) {
              node.setProperty("exo:dateModified",new GregorianCalendar()) ;
            }
            node.save();
          }
        }
        uiExplorer.getSession().save() ;
        UIUploadManager uiManager = uiForm.getParent() ;
        UIUploadContainer uiUploadContainer = uiManager.getChild(UIUploadContainer.class) ;
        if(uiForm.isMultiLanguage_) {
          uiUploadContainer.setUploadedNode(selectedNode) ; 
        } else {
          Node newNode = null ;
          if(!isExist) {
            newNode = uiExplorer.getSession().getNodeByUUID(newNodeUUID) ;
          } else {
            newNode = selectedNode.getNode(name) ;
          }
          uiUploadContainer.setUploadedNode(newNode) ;
        }
        UIUploadContent uiUploadContent = uiManager.findFirstComponentOfType(UIUploadContent.class) ;
        String fileSize = String.valueOf((((float)(content.length/100))/10));     
        String[] arrValues = {fileName, name, fileSize +" Kb", mimeType} ;
        uiUploadContent.setUploadValues(arrValues) ;
        UploadService uploadService = uiForm.getApplicationComponent(UploadService.class) ;
        UIFormUploadInput uiChild = uiForm.getChild(UIFormUploadInput.class) ;
        uploadService.removeUpload(uiChild.getUploadId()) ;
        uiManager.setRenderedChild(UIUploadContainer.class) ;
        uiExplorer.setIsHidePopup(true) ;
        uiExplorer.updateAjax(event) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
      } catch(ConstraintViolationException con) {
        Object[] args = {name, } ;
        con.printStackTrace() ;
        throw new MessageException(new ApplicationMessage("UIUploadForm.msg.contraint-violation", 
                                                           args, ApplicationMessage.WARNING)) ;
      } catch(LockException lock) {
        throw new MessageException(new ApplicationMessage("UIUploadForm.msg.lock-exception", 
            null, ApplicationMessage.WARNING)) ;        
      } catch(Exception e) {
        e.printStackTrace() ;
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
