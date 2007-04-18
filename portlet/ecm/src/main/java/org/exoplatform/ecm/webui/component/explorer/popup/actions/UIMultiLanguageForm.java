/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.ecm.webui.component.explorer.UIDocumentInfo;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIPopupAction;
import org.exoplatform.services.cms.i18n.MultiLanguageService;
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
 * Jan 15, 2007  
 * 1:48:19 PM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl",
    events = {
      @EventConfig(listeners = UIMultiLanguageForm.ViewActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIMultiLanguageForm.SetDefaultActionListener.class),
      @EventConfig(listeners = UIMultiLanguageForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIMultiLanguageForm extends UIForm {

  final static public String LANGUAGES = "languages" ;
  final static public String EXO_LANGUAGE = "exo:language";

  public UIMultiLanguageForm() throws Exception {
    List<SelectItemOption<String>> languages = new ArrayList<SelectItemOption<String>>() ;
    addUIFormInput(new UIFormSelectBox(LANGUAGES, LANGUAGES, languages)) ;
  }

  public void updateSelect(Node currentNode) throws Exception {
    List<SelectItemOption<String>> languages = new ArrayList<SelectItemOption<String>>() ;
    String defaultLang = currentNode.getProperty(EXO_LANGUAGE).getString() ;
    languages.add(new SelectItemOption<String>(defaultLang + "(default)", defaultLang)) ;
    if(currentNode.hasNode(LANGUAGES)){
      Node languageNode = currentNode.getNode(LANGUAGES) ;
      NodeIterator iter  = languageNode.getNodes() ;      
      while(iter.hasNext()) {
        Node lang = iter.nextNode() ;
        if(!lang.getName().equals(defaultLang)) {
          languages.add(new SelectItemOption<String>(lang.getName(), lang.getName()));
        }
      }
    }
    getUIFormSelectBox(LANGUAGES).setOptions(languages) ;
    getUIFormSelectBox(LANGUAGES).setValue(defaultLang) ;
  }
  
  static public class CancelActionListener extends EventListener<UIMultiLanguageForm> {
    public void execute(Event<UIMultiLanguageForm> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;
    }
  }
  
  static public class SetDefaultActionListener extends EventListener<UIMultiLanguageForm> {
    public void execute(Event<UIMultiLanguageForm> event) throws Exception {
      UIMultiLanguageForm uiForm = event.getSource() ;
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
      MultiLanguageService multiLanguageService = 
        uiForm.getApplicationComponent(MultiLanguageService.class) ;
      String selectedLanguage = uiForm.getUIFormSelectBox(LANGUAGES).getValue() ;
      multiLanguageService.setDefault(uiExplorer.getCurrentNode(), selectedLanguage) ;
      uiExplorer.updateAjax(event) ;
//      if(uiExplorer.getCurrentNode().getPrimaryNodeType().getName().equals("nt:file")) {
//        UIPopupAction uiPopup = uiForm.getAncestorOfType(UIPopupAction.class) ;
//        uiPopup.deActivate() ;
//      } 
    }
  }
  
  static public class ViewActionListener extends EventListener<UIMultiLanguageForm> {
    public void execute(Event<UIMultiLanguageForm> event) throws Exception {
      UIMultiLanguageForm uiForm = event.getSource() ;
      MultiLanguageService multiLanguageService = uiForm.getApplicationComponent(MultiLanguageService.class) ;
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
      UIJCRExplorer uiJCRExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
      Node currNode = uiExplorer.getCurrentNode() ;
      UIDocumentInfo uiDocumentInfo = uiJCRExplorer.findFirstComponentOfType(UIDocumentInfo.class) ;
      String selectedLanguage = uiForm.getUIFormSelectBox(LANGUAGES).getValue() ;
      if(selectedLanguage.equals(multiLanguageService.getDefault(currNode))) {
        uiJCRExplorer.cancelAction() ;
      } else {
        uiDocumentInfo.setLanguage(uiForm.getUIFormSelectBox(LANGUAGES).getValue()) ;
        uiExplorer.updateAjax(event) ;
      }
    }
  }
}
