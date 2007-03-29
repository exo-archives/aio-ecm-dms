/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.cms.i18n.MultiLanguageService;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

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
    events = @EventConfig(phase=Phase.DECODE, listeners = UILanguageTypeForm.ChangeLanguageActionListener.class)
)
public class UILanguageTypeForm extends UIForm {

  final static public String LANGUAGE_TYPE = "typeLang" ;
  
  public UILanguageTypeForm() throws Exception {
    UIFormSelectBox uiSelectForm = new UIFormSelectBox(LANGUAGE_TYPE, LANGUAGE_TYPE, languages()) ;
    uiSelectForm.setOnChange("ChangeLanguage") ;
    addUIFormInput(uiSelectForm) ;
  }
  
  public List<SelectItemOption<String>> languages() throws Exception {
    LocaleConfigService localService = getApplicationComponent(LocaleConfigService.class) ;
    List<SelectItemOption<String>> languages = new ArrayList<SelectItemOption<String>>() ;
    Iterator iter = localService.getLocalConfigs().iterator() ;
    languages.add(new SelectItemOption<String>("- - - -", ""));
    while(iter.hasNext()) {
      LocaleConfig localConfig = (LocaleConfig)iter.next() ;
      languages.add(new SelectItemOption<String>(localConfig.getLanguage(), localConfig.getLanguage())) ;
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
        UIDocumentForm uiDocumentForm = uiContainer.getChild(UIDocumentForm.class) ;
        uiDocumentForm.getChildren().clear() ;
        uiDocumentForm.setTemplateNode(uiContainer.nodeTypeName_) ;
        uiDocumentForm.setIsMultiLanguage(true) ;
        Node node = uiExplorer.getCurrentNode() ;
        if(node.hasNode(UIMultiLanguageForm.LANGUAGES)) {
          Node languagesNode = node.getNode(UIMultiLanguageForm.LANGUAGES) ;
          if(languagesNode.hasNode(selectedLanguage)) {
            uiDocumentForm.setNode(languagesNode.getNode(selectedLanguage)) ;
          } else if(selectedLanguage.equals(multiLanguageService.getDefault(node))) {
            uiDocumentForm.setNode(node) ;
          } else {
            uiDocumentForm.setNode(null) ;
          }
        } else {
          uiDocumentForm.setNode(node) ;
        }
        uiDocumentForm.setSelectedLanguage(selectedLanguage) ;
        if(selectedLanguage.equals(node.getProperty(UIMultiLanguageForm.EXO_LANGUAGE).getString())) {                  
          uiDocumentForm.setPropertyNode(node) ;
        } else {
          if(node.hasNode("languages/"+ selectedLanguage)){
            uiDocumentForm.getChildren().clear() ;
            Node languageNode = multiLanguageService.getLanguage(node, selectedLanguage) ;
            uiDocumentForm.setPropertyNode(languageNode) ;
          } else {
            uiDocumentForm.setPropertyNode(null) ;
          }
        }
      } else {
        UIUploadForm uiUploadForm =  uiContainer.getChild(UIUploadForm.class) ;
        uiUploadForm.setIsMultiLanguage(true, selectedLanguage) ;
      }
      uiContainer.setRenderSibbling(UIAddLanguageContainer.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }
}
