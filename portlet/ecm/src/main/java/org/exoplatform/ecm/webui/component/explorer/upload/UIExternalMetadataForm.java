/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.upload;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.cms.metadata.MetadataService;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormCheckBoxInput;
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
 * May 25, 2007 3:58:09 PM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl",
    events = {
      @EventConfig(listeners = UIExternalMetadataForm.AddActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UIExternalMetadataForm.CancelActionListener.class)
    }    
)
public class UIExternalMetadataForm extends UIForm {
  
  public UIExternalMetadataForm() throws Exception {
  }
  
  public void renderExternalList() throws Exception {
    MetadataService metadataService = getApplicationComponent(MetadataService.class) ;
    UIFormCheckBoxInput<String> uiCheckBox ;
    for(NodeType nodeType : metadataService.getAllMetadatasNodeType()) {
      uiCheckBox = new UIFormCheckBoxInput<String>(nodeType.getName(), nodeType.getName(), "") ;
      if(isExternalUse(nodeType)) {
        if(hasExternalMetadata(nodeType.getName())) {
          uiCheckBox.setChecked(true) ;
          uiCheckBox.setEnable(false) ;
        } else {
          uiCheckBox.setChecked(false) ;
          uiCheckBox.setEnable(true) ;
        }
        addUIFormInput(uiCheckBox) ;
      }
    }
  }
  
  private boolean isExternalUse(NodeType nodeType) throws Exception{
    PropertyDefinition def = 
      ((ExtendedNodeType)nodeType).getPropertyDefinitions("exo:internalUse").getAnyDefinition() ;    
    return !def.getDefaultValues()[0].getBoolean() ;
  }
  
  private boolean hasExternalMetadata(String name) throws Exception {
    UIUploadManager uiUploadManager = getAncestorOfType(UIUploadManager.class) ;
    UIUploadContainer uiUploadContainer = uiUploadManager.getChild(UIUploadContainer.class) ;
    Node uploaded = uiUploadContainer.getUploadedNode() ;
    for(NodeType mixin : uploaded.getMixinNodeTypes()) {
      if(mixin.getName().equals(name)) return true ;
    }
    if(uploaded.hasNode(Utils.JCR_CONTENT)) {
      for(NodeType mixin : uploaded.getNode(Utils.JCR_CONTENT).getMixinNodeTypes()) {
        if(mixin.getName().equals(name)) return true ;
      }
    }
    return false ;
  }
  
  public String getLabel(ResourceBundle res, String id)  {
    try {
      return res.getString("UIExternalMetadataForm.label." + id) ;
    } catch (MissingResourceException ex) {
      return id ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIExternalMetadataForm> {
    public void execute(Event<UIExternalMetadataForm> event) throws Exception {
      UIUploadManager uiUploadManager = event.getSource().getAncestorOfType(UIUploadManager.class) ;
      UIPopupWindow uiPopup = uiUploadManager.getChildById(UIUploadManager.EXTARNAL_METADATA_POPUP) ;
      uiPopup.setShow(false) ;
      uiPopup.setRendered(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiUploadManager) ;
    }
  }
  
  static  public class AddActionListener extends EventListener<UIExternalMetadataForm> {
    public void execute(Event<UIExternalMetadataForm> event) throws Exception {
      UIExternalMetadataForm uiExternalMetadataForm = event.getSource() ;
      List<UIFormCheckBoxInput> listCheckbox =  new ArrayList<UIFormCheckBoxInput>();
      uiExternalMetadataForm.findComponentOfType(listCheckbox, UIFormCheckBoxInput.class); 
      UIUploadManager uiUploadManager = event.getSource().getAncestorOfType(UIUploadManager.class) ;
      UIUploadContainer uiContainer = uiUploadManager.getChild(UIUploadContainer.class) ;
      String metadataName = null ;
      Node uploadedNode = uiContainer.getUploadedNode() ;
      for(int i = 0; i < listCheckbox.size(); i ++) {
        if(listCheckbox.get(i).isChecked() && listCheckbox.get(i).isEnable()) {
          metadataName = listCheckbox.get(i).getName() ;
          if(!uploadedNode.canAddMixin(metadataName)) {
            UIApplication uiApp = uiExternalMetadataForm.getAncestorOfType(UIApplication.class) ;
            uiApp.addMessage(new ApplicationMessage("UIExternalMetadataForm.msg.can-not-add", null, 
                                                    ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
          uploadedNode.addMixin(metadataName) ;
          uploadedNode.save() ;
          UIUploadContent uiUploadContent = uiContainer.getChild(UIUploadContent.class) ;
          uiUploadContent.externalList_.add(metadataName) ;
        }
      }
      uiExternalMetadataForm.getAncestorOfType(UIJCRExplorer.class).getSession().save() ;
      UIPopupWindow uiPopup = uiUploadManager.getChildById(UIUploadManager.EXTARNAL_METADATA_POPUP) ;
      uiPopup.setShow(false) ;
      uiPopup.setRendered(false) ;
      uiContainer.setRenderedChild(UIUploadContent.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiUploadManager) ;
    }
  }
}
