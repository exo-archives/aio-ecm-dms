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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.upload.UIUploadForm;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.services.cms.i18n.MultiLanguageService;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jan 16, 2007  
 * 11:35:27 AM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/component/explorer/UIFormWithoutAction.gtmpl",
    events = {
      @EventConfig(phase=Phase.DECODE, listeners = UILanguageTypeForm.ChangeLanguageActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UILanguageTypeForm.SetDefaultActionListener.class)
    }
)
public class UILanguageTypeForm extends UIForm {

  final static public String LANGUAGE_TYPE = "typeLang" ;
  final static public String DEFAULT_TYPE = "default" ;
  
  public UILanguageTypeForm() throws Exception {
    UIFormSelectBox uiSelectForm = new UIFormSelectBox(LANGUAGE_TYPE, LANGUAGE_TYPE, languages()) ;
    uiSelectForm.setOnChange("ChangeLanguage") ;
    addUIFormInput(uiSelectForm) ;
    UIFormCheckBoxInput uiCheckbox = new UIFormCheckBoxInput<Boolean>(DEFAULT_TYPE, DEFAULT_TYPE, null) ;
    uiCheckbox.setOnChange("SetDefault") ;
    addUIFormInput(uiCheckbox) ;
  }
  
  public void resetLanguage() {
    getUIFormSelectBox(LANGUAGE_TYPE).setValue("") ;
    getUIFormCheckBoxInput(DEFAULT_TYPE).setChecked(false) ;
  }
  
  public List<SelectItemOption<String>> languages() throws Exception {
    LocaleConfigService localService = getApplicationComponent(LocaleConfigService.class) ;
    List<SelectItemOption<String>> languages = new ArrayList<SelectItemOption<String>>() ;
    Iterator iter = localService.getLocalConfigs().iterator() ;
    languages.add(new SelectItemOption<String>("- - - -", ""));
    while (iter.hasNext()) {
      LocaleConfig localConfig = (LocaleConfig)iter.next() ;
      languages.add(new SelectItemOption<String>(localConfig.getLocale().getDisplayLanguage(), 
                                                 localConfig.getLocale().getLanguage())) ;
    }
    return languages ;
  }

  static public class ChangeLanguageActionListener extends EventListener<UILanguageTypeForm> {
    public void execute(Event<UILanguageTypeForm> event) throws Exception {
      UILanguageTypeForm uiTypeForm = event.getSource();
      String selectedLang = uiTypeForm.getUIFormSelectBox(LANGUAGE_TYPE).getValue().trim();
      String selectedLanguage = selectedLang;
      
      LocaleConfigService localService = uiTypeForm.getApplicationComponent(LocaleConfigService.class) ;
      Iterator iter = localService.getLocalConfigs().iterator() ;
      while (iter.hasNext()) {
        LocaleConfig localConfig = (LocaleConfig)iter.next() ;
        if (localConfig.getLocale().getLanguage().trim().equals(selectedLang)) {
          selectedLanguage = localConfig.getLocale().getDisplayLanguage();
        }
      }
      
      MultiLanguageService multiLanguageService = 
        uiTypeForm.getApplicationComponent(MultiLanguageService.class) ;
      if (selectedLang == null || selectedLang.length() < 1) return;
      UIJCRExplorer uiExplorer = uiTypeForm.getAncestorOfType(UIJCRExplorer.class);
      UIAddLanguageContainer uiContainer = uiTypeForm.getParent() ;
      if(uiContainer.nodeTypeName_ != null) {
        UILanguageDialogForm uiDialogForm = uiContainer.getChild(UILanguageDialogForm.class) ;
        uiDialogForm.getChildren().clear() ;
        uiDialogForm.setTemplateNode(uiContainer.nodeTypeName_) ;
        Node node = uiExplorer.getRealCurrentNode() ;        
        String currentPath = uiExplorer.getCurrentPath() ;
        if(selectedLang.equals(multiLanguageService.getDefault(node))) {
          uiTypeForm.getUIFormCheckBoxInput(DEFAULT_TYPE).setChecked(true) ;
          uiTypeForm.getUIFormCheckBoxInput(DEFAULT_TYPE).setEnable(false) ;
        } else {
          uiTypeForm.getUIFormCheckBoxInput(DEFAULT_TYPE).setChecked(false) ;
          uiTypeForm.getUIFormCheckBoxInput(DEFAULT_TYPE).setEnable(true) ;
        }
        if(node.hasNode(Utils.LANGUAGES)) {
          Node languagesNode = node.getNode(Utils.LANGUAGES) ;
          if(node.isNodeType(Utils.NT_FILE)) {
            uiDialogForm.setIsNTFile(true) ;
            uiDialogForm.setIsAddNew(false) ;
          } else {
            uiDialogForm.setIsNTFile(false) ;
          }
          if (languagesNode.hasNode(selectedLang)) {
//            uiDialogForm.setNode(languagesNode.getNode(selectedLanguage)) ;
            uiDialogForm.setNodePath(languagesNode.getNode(selectedLang).getPath()) ;
          } else if(selectedLang.equals(multiLanguageService.getDefault(node))) {
//            uiDialogForm.setNode(node) ;
            uiDialogForm.setNodePath(currentPath) ;
          } else {
//            uiDialogForm.setNode(node) ;
            uiDialogForm.setNodePath(currentPath) ;
            uiDialogForm.setIsNotEditNode(true) ;
            uiDialogForm.setIsResetMultiField(true) ;
          }
        } else if(!node.hasNode(Utils.LANGUAGES) && selectedLang.equals(multiLanguageService.getDefault(node))) {
          uiDialogForm.setIsNotEditNode(false) ;
//          uiDialogForm.setNode(node) ;
          uiDialogForm.setNodePath(currentPath) ;
        } else {
//          uiDialogForm.setNode(node) ;
          uiDialogForm.setNodePath(currentPath) ;
          uiDialogForm.setIsNotEditNode(true) ;
          uiDialogForm.setIsResetMultiField(true) ;
        }
        uiDialogForm.setSelectedLanguage(selectedLang) ;
        if(selectedLang.equals(node.getProperty(Utils.EXO_LANGUAGE).getString())) {                  
//          uiDialogForm.setPropertyNode(node) ;
          uiDialogForm.setChildPath(currentPath) ;
        } else {
          if(node.hasNode(Utils.LANGUAGES + Utils.SLASH + selectedLang)){
            uiDialogForm.getChildren().clear() ;
            Node languageNode = multiLanguageService.getLanguage(node, selectedLang) ;
//            uiDialogForm.setPropertyNode(languageNode) ;
            uiDialogForm.setChildPath(languageNode.getPath()) ;
          } else {
//            uiDialogForm.setPropertyNode(null) ;
            uiDialogForm.setChildPath(null) ;
          }
        }
      } else {
        UIUploadForm uiUploadForm =  uiContainer.findFirstComponentOfType(UIUploadForm.class) ;
        uiUploadForm.setIsMultiLanguage(true, selectedLang) ;
      }
      uiContainer.setRenderSibbling(UIAddLanguageContainer.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }
  
  static public class SetDefaultActionListener extends EventListener<UILanguageTypeForm> {
    public void execute(Event<UILanguageTypeForm> event) throws Exception {
      UILanguageTypeForm uiForm = event.getSource() ;
      UIAddLanguageContainer uiLanguageContainer = uiForm.getParent() ;
      boolean isDefault = uiForm.getUIFormCheckBoxInput(DEFAULT_TYPE).isChecked() ;
      if(uiLanguageContainer.nodeTypeName_ != null) {
        UILanguageDialogForm uiDialogForm = uiLanguageContainer.getChild(UILanguageDialogForm.class) ;
        uiDialogForm.setIsDefaultLanguage(isDefault) ;
      } else {
        UIUploadForm uiUploadForm = uiLanguageContainer.findFirstComponentOfType(UIUploadForm.class) ;
        uiUploadForm.setIsDefaultLanguage(isDefault) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }
}