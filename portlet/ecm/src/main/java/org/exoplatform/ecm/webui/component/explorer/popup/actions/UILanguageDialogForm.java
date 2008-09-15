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

import org.exoplatform.ecm.webui.utils.DialogFormUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.form.UIDialogForm;
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
      @EventConfig(listeners = UILanguageDialogForm.SaveActionListener.class),
      @EventConfig(listeners = UILanguageDialogForm.CancelActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UILanguageDialogForm.AddActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UILanguageDialogForm.RemoveActionListener.class, phase = Phase.DECODE)
    }
)
public class UILanguageDialogForm extends UIDialogForm {

  private boolean isAddNew_ = false ; 
  private String selectedLanguage_ = null;
  private boolean isDefault_ = false;
  private String documentType_ ;
  
  public UILanguageDialogForm() throws Exception {
    setActions(new String[]{"Save", "Cancel"}) ;
  }
  
  public void setTemplateNode(String type) { documentType_ = type ;}
  
  public String getTemplate() {
    repositoryName = getAncestorOfType(UIJCRExplorer.class).getRepositoryName() ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    try {      
      return templateService.getTemplatePathByUser(true, documentType_, userName, repositoryName) ;
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
  
  static  public class SaveActionListener extends EventListener<UILanguageDialogForm> {
    public void execute(Event<UILanguageDialogForm> event) throws Exception {
      UILanguageDialogForm languageDialogForm = event.getSource();
      UIJCRExplorer uiExplorer = languageDialogForm.getAncestorOfType(UIJCRExplorer.class) ;
      Node node = uiExplorer.getCurrentNode() ;
      MultiLanguageService multiLanguageService = languageDialogForm.getApplicationComponent(MultiLanguageService.class) ;
      UIApplication uiApp = languageDialogForm.getAncestorOfType(UIApplication.class) ;
      if(languageDialogForm.selectedLanguage_ == null) {
        uiApp.addMessage(new ApplicationMessage("UILanguageDialogForm.msg.select-lang", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      }
      if(!uiExplorer.hasAddPermission()) {
        uiApp.addMessage(new ApplicationMessage("UILanguageDialogForm.msg.access-denied", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      }
      if(node.hasNode(Utils.EXO_IMAGE)) {
        Map inputProperties = DialogFormUtil.prepareMap(languageDialogForm.getChildren(), languageDialogForm.getInputProperties()) ;
        try {
          multiLanguageService.addLanguage(node, inputProperties, languageDialogForm.getSelectedLanguage(), languageDialogForm.isDefaultLanguage(), Utils.EXO_IMAGE) ;
        } catch(AccessDeniedException ace) {
          uiApp.addMessage(new ApplicationMessage("UILanguageDialogForm.msg.access-denied", null, 
                                                  ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return;
        }
      } else if(languageDialogForm.hasNodeTypeNTResource(node)) {
        Map inputProperties = DialogFormUtil.prepareMap(languageDialogForm.getChildren(), languageDialogForm.getInputProperties()) ;
        try {
          multiLanguageService.addFileLanguage(node, languageDialogForm.getSelectedLanguage(), inputProperties, languageDialogForm.isDefaultLanguage()) ;
        } catch(AccessDeniedException ace) {
          uiApp.addMessage(new ApplicationMessage("UILanguageDialogForm.msg.access-denied", null, 
                                                  ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return;
        }
      } else {
        Map map = DialogFormUtil.prepareMap(languageDialogForm.getChildren(), languageDialogForm.properties) ;
        try {
          multiLanguageService.addLanguage(node, map, languageDialogForm.getSelectedLanguage(), languageDialogForm.isDefaultLanguage()) ;
        } catch(AccessDeniedException ace) {
          uiApp.addMessage(new ApplicationMessage("UILanguageDialogForm.msg.access-denied", null, 
                                                  ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return;        
        }
      }
      node.save() ;
      UIMultiLanguageManager uiManager = languageDialogForm.getAncestorOfType(UIMultiLanguageManager.class) ;
      UIMultiLanguageForm uiMultiForm = uiManager.getChild(UIMultiLanguageForm.class) ;
      uiMultiForm.doSelect(node) ;
      if(languageDialogForm.isDefaultLanguage()) uiExplorer.setLanguage(languageDialogForm.getSelectedLanguage()) ;
      uiManager.setRenderedChild(UIMultiLanguageForm.class) ;
      UIAddLanguageContainer uiAddContainer = uiManager.getChild(UIAddLanguageContainer.class) ;
      UILanguageTypeForm uiLanguageTypeForm = uiAddContainer.getChild(UILanguageTypeForm.class) ;
      uiLanguageTypeForm.resetLanguage() ;
      uiAddContainer.removeChild(UILanguageDialogForm.class) ;
      uiAddContainer.setComponentDisplay(languageDialogForm.documentType_) ;
      if(!uiExplorer.getPreference().isJcrEnable()) node.getSession().save() ;
      uiExplorer.setIsHidePopup(true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
      uiExplorer.updateAjax(event) ;      
    }
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