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
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.DialogFormFields;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.i18n.MultiLanguageService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Apr 24, 2007 9:05:25 AM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = DialogFormFields.SaveActionListener.class),
      @EventConfig(listeners = UILanguageDialogForm.CancelActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UILanguageDialogForm.AddActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UILanguageDialogForm.RemoveActionListener.class, phase = Phase.DECODE)
    }
)
public class UILanguageDialogForm extends DialogFormFields {

  private boolean isAddNew_ = false ; 
  private String selectedLanguage_ = null;
  private boolean isDefault_ = false;
  private String documentType_ ;
  
  public UILanguageDialogForm() throws Exception {
    setActions(new String[]{"Save", "Cancel"}) ;
  }
  
  public void setTemplateNode(String type) { documentType_ = type ;}
  
  public String getTemplate() {
    repositoryName_ = getAncestorOfType(UIJCRExplorer.class).getRepositoryName() ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    try {
      resetScriptInterceptor() ;
      return templateService.getTemplatePathByUser(true, documentType_, userName, repositoryName_) ;
    } catch (Exception e) {
      UIApplication uiApp = getAncestorOfType(UIApplication.class) ;
      Object[] arg = { documentType_ } ;
      uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.not-support", arg, 
                                              ApplicationMessage.ERROR)) ;
      return null ;
    } 
  }

  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return getAncestorOfType(UIJCRExplorer.class).getJCRTemplateResourceResolver() ;
  }
  
  public boolean isAddNew() {return isAddNew_ ;}
  public void addNew(boolean b) {isAddNew_ = b ;}
  
  public void setIsAddNew(boolean isAddNew) { isAddNew_ = isAddNew ; }
  
  public boolean isEditing() { return !isAddNew_ ; }
  
  public Node getCurrentNode() throws Exception { 
    return getAncestorOfType(UIJCRExplorer.class).getCurrentNode() ; 
  }
  
  public void setSelectedLanguage(String selectedLanguage) { selectedLanguage_ = selectedLanguage; }
  public String getSelectedLanguage() { return selectedLanguage_ ; }
  
  public void setIsDefaultLanguage(boolean isDefault) { isDefault_ = isDefault ; }
  public boolean isDefaultLanguage() { return isDefault_ ; } 
  
  private boolean hasNodeTypeNTResource(Node node) throws Exception {
    if(node.hasNodes()) {
      NodeIterator nodeIter = node.getNodes() ;
      while(nodeIter.hasNext()) {
        Node childNode = nodeIter.nextNode() ;
        if(childNode.getPrimaryNodeType().getName().equals("nt:resource")) return true ;
      }
    }
    return false ;
  }
  
  @SuppressWarnings("unused")
  public Node storeValue(Event event) throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    Node node = uiExplorer.getCurrentNode() ;
    MultiLanguageService multiLanguageService = getApplicationComponent(MultiLanguageService.class) ;
    UIApplication uiApp = getAncestorOfType(UIApplication.class) ;
    if(selectedLanguage_ == null) {
      uiApp.addMessage(new ApplicationMessage("UILanguageDialogForm.msg.select-lang", null, 
                                              ApplicationMessage.WARNING)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return null;
    }
    if(!uiExplorer.hasAddPermission()) {
      uiApp.addMessage(new ApplicationMessage("UILanguageDialogForm.msg.access-denied", null, 
                                              ApplicationMessage.WARNING)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return null;
    }
    if(node.hasNode(Utils.EXO_IMAGE)) {
      Map inputProperties = Utils.prepareMap(getChildren(), getInputProperties()) ;
      try {
        multiLanguageService.addLanguage(node, inputProperties, getSelectedLanguage(), isDefaultLanguage(), Utils.EXO_IMAGE) ;
      } catch(AccessDeniedException ace) {
        uiApp.addMessage(new ApplicationMessage("UILanguageDialogForm.msg.access-denied", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return null;
      }
    } else if(hasNodeTypeNTResource(node)) {
      Map inputProperties = Utils.prepareMap(getChildren(), getInputProperties()) ;
      try {
        multiLanguageService.addFileLanguage(node, getSelectedLanguage(), inputProperties, isDefaultLanguage()) ;
      } catch(AccessDeniedException ace) {
        uiApp.addMessage(new ApplicationMessage("UILanguageDialogForm.msg.access-denied", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return null;
      }
    } else {
      Map map = Utils.prepareMap(getChildren(), properties) ;
      try {
        multiLanguageService.addLanguage(node, map, getSelectedLanguage(), isDefaultLanguage()) ;
      } catch(AccessDeniedException ace) {
        uiApp.addMessage(new ApplicationMessage("UILanguageDialogForm.msg.access-denied", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return null;        
      }
    }
    node.save() ;
    UIMultiLanguageManager uiManager = getAncestorOfType(UIMultiLanguageManager.class) ;
    UIMultiLanguageForm uiMultiForm = uiManager.getChild(UIMultiLanguageForm.class) ;
    uiMultiForm.updateSelect(node) ;
    if(isDefaultLanguage()) uiExplorer.setLanguage(getSelectedLanguage()) ;
    uiManager.setRenderedChild(UIMultiLanguageForm.class) ;
    UIAddLanguageContainer uiAddContainer = uiManager.getChild(UIAddLanguageContainer.class) ;
    UILanguageTypeForm uiLanguageTypeForm = uiAddContainer.getChild(UILanguageTypeForm.class) ;
    uiLanguageTypeForm.resetLanguage() ;
    uiAddContainer.removeChild(UILanguageDialogForm.class) ;
    uiAddContainer.setComponentDisplay(documentType_) ;
    if(!uiExplorer.getPreference().isJcrEnable()) node.getSession().save() ;
    uiExplorer.setIsHidePopup(true) ;
    event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    uiExplorer.updateAjax(event) ;
    return null ;
  }
  
  static  public class CancelActionListener extends EventListener<UILanguageDialogForm> {
    public void execute(Event<UILanguageDialogForm> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;
    }
  }
  
  static public class AddActionListener extends EventListener<UILanguageDialogForm> {
    public void execute(Event<UILanguageDialogForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getParent()) ;
    }
  }

  static public class RemoveActionListener extends EventListener<UILanguageDialogForm> {
    public void execute(Event<UILanguageDialogForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getParent()) ;
    }
  }
}