/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentInfo;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
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
    template =  "system:/groovy/webui/component/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIMultiLanguageForm.ViewActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIMultiLanguageForm.SetDefaultActionListener.class),
      @EventConfig(listeners = UIMultiLanguageForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIMultiLanguageForm extends UIForm {

  public UIMultiLanguageForm() throws Exception {
    List<SelectItemOption<String>> languages = new ArrayList<SelectItemOption<String>>() ;
    addUIFormInput(new UIFormSelectBox(Utils.LANGUAGES, Utils.LANGUAGES, languages)) ;
  }

  public void updateSelect(Node currentNode) throws Exception {
    List<SelectItemOption<String>> languages = new ArrayList<SelectItemOption<String>>() ;
    String defaultLang = currentNode.getProperty(Utils.EXO_LANGUAGE).getString() ;
    languages.add(new SelectItemOption<String>(defaultLang + "(default)", defaultLang)) ;
    if(currentNode.hasNode(Utils.LANGUAGES)){
      Node languageNode = currentNode.getNode(Utils.LANGUAGES) ;
      NodeIterator iter  = languageNode.getNodes() ;      
      while(iter.hasNext()) {
        Node lang = iter.nextNode() ;
        if(!lang.getName().equals(defaultLang)) {
          languages.add(new SelectItemOption<String>(lang.getName(), lang.getName()));
        }
      }
    }
    getUIFormSelectBox(Utils.LANGUAGES).setOptions(languages) ;
    getUIFormSelectBox(Utils.LANGUAGES).setValue(defaultLang) ;
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
      String selectedLanguage = uiForm.getUIFormSelectBox(Utils.LANGUAGES).getValue() ;
      multiLanguageService.setDefault(uiExplorer.getCurrentNode(), selectedLanguage) ;
      uiExplorer.setLanguage(selectedLanguage) ;
      uiExplorer.setIsHidePopup(false) ;
      uiExplorer.updateAjax(event) ;
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
      String selectedLanguage = uiForm.getUIFormSelectBox(Utils.LANGUAGES).getValue() ;
      if(selectedLanguage.equals(multiLanguageService.getDefault(currNode))) {
        uiJCRExplorer.cancelAction() ;
      } else {
        uiDocumentInfo.setLanguage(uiForm.getUIFormSelectBox(Utils.LANGUAGES).getValue()) ;
        uiExplorer.updateAjax(event) ;
      }
    }
  }
}
