/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.upload.UIUploadForm;
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
  
  public List<SelectItemOption<String>> languages() throws Exception {
    LocaleConfigService localService = getApplicationComponent(LocaleConfigService.class) ;
    List<SelectItemOption<String>> languages = new ArrayList<SelectItemOption<String>>() ;
    Iterator iter = localService.getLocalConfigs().iterator() ;
    languages.add(new SelectItemOption<String>("- - - -", ""));
    while(iter.hasNext()) {
      LocaleConfig localConfig = (LocaleConfig)iter.next() ;
      languages.add(new SelectItemOption<String>(localConfig.getLocale().getDisplayLanguage(), 
                                                 localConfig.getLocale().getDisplayLanguage())) ;
    }
    return languages ;
  }

  static public class ChangeLanguageActionListener extends EventListener<UILanguageTypeForm> {
    public void execute(Event<UILanguageTypeForm> event) throws Exception {
      UILanguageTypeForm uiTypeForm = event.getSource() ;
      String selectedLanguage = uiTypeForm.getUIFormSelectBox(LANGUAGE_TYPE).getValue() ;
      MultiLanguageService multiLanguageService = 
        uiTypeForm.getApplicationComponent(MultiLanguageService.class) ;
      if(selectedLanguage == null || selectedLanguage.length() < 1) return;
      UIJCRExplorer uiExplorer = uiTypeForm.getAncestorOfType(UIJCRExplorer.class);
      UIAddLanguageContainer uiContainer = uiTypeForm.getParent() ;
      if(uiContainer.nodeTypeName_ != null) {
        UILanguageDialogForm uiDialogForm = uiContainer.getChild(UILanguageDialogForm.class) ;
        uiDialogForm.getChildren().clear() ;
        uiDialogForm.setTemplateNode(uiContainer.nodeTypeName_) ;
        Node node = uiExplorer.getCurrentNode() ;
        if(selectedLanguage.equals(multiLanguageService.getDefault(node))) {
          uiTypeForm.getUIFormCheckBoxInput(DEFAULT_TYPE).setChecked(true) ;
          uiTypeForm.getUIFormCheckBoxInput(DEFAULT_TYPE).setEnable(false) ;
        } else {
          uiTypeForm.getUIFormCheckBoxInput(DEFAULT_TYPE).setChecked(false) ;
          uiTypeForm.getUIFormCheckBoxInput(DEFAULT_TYPE).setEnable(true) ;
        }
        if(node.hasNode(Utils.LANGUAGES)) {
          Node languagesNode = node.getNode(Utils.LANGUAGES) ;
          if(node.isNodeType(Utils.NT_FILE)) uiDialogForm.setIsNTFile(true) ;
          else uiDialogForm.setIsNTFile(false) ;
          if(languagesNode.hasNode(selectedLanguage)) {
            uiDialogForm.setNode(languagesNode.getNode(selectedLanguage)) ;
          } else if(selectedLanguage.equals(multiLanguageService.getDefault(node))) {
            uiDialogForm.setNode(node) ;
          } else {
            uiDialogForm.setNode(node) ;
            uiDialogForm.setIsNotEditNode(true) ;
            uiDialogForm.setIsResetMultiField(true) ;
          }
        } else if(!node.hasNode(Utils.LANGUAGES) && selectedLanguage.equals(multiLanguageService.getDefault(node))) {
          uiDialogForm.setNode(node) ;
        } else {
          uiDialogForm.setNode(node) ;
          uiDialogForm.setIsNotEditNode(true) ;
          uiDialogForm.setIsResetMultiField(true) ;
        }
        uiDialogForm.setSelectedLanguage(selectedLanguage) ;
        if(selectedLanguage.equals(node.getProperty(Utils.EXO_LANGUAGE).getString())) {                  
          uiDialogForm.setPropertyNode(node) ;
        } else {
          if(node.hasNode(Utils.LANGUAGES + Utils.SLASH + selectedLanguage)){
            uiDialogForm.getChildren().clear() ;
            Node languageNode = multiLanguageService.getLanguage(node, selectedLanguage) ;
            uiDialogForm.setPropertyNode(languageNode) ;
          } else {
            uiDialogForm.setPropertyNode(null) ;
          }
        }
      } else {
        UIUploadForm uiUploadForm =  uiContainer.findFirstComponentOfType(UIUploadForm.class) ;
        uiUploadForm.setIsMultiLanguage(true, selectedLanguage) ;
      }
      uiContainer.setRenderSibbling(UIAddLanguageContainer.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }
  
  static public class SetDefaultActionListener extends EventListener<UILanguageTypeForm> {
    public void execute(Event<UILanguageTypeForm> event) throws Exception {
      UILanguageTypeForm uiForm = event.getSource() ;
      UIAddLanguageContainer uiLanguageContainer = uiForm.getParent() ;
      UILanguageDialogForm uiDialogForm = uiLanguageContainer.getChild(UILanguageDialogForm.class) ;
      boolean isDefault = uiForm.getUIFormCheckBoxInput(DEFAULT_TYPE).isChecked() ;
      uiDialogForm.setIsDefaultLanguage(isDefault) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }
}
