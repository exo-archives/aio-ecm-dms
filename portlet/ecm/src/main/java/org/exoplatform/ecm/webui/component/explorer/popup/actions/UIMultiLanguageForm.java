/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.ecm.webui.component.explorer.UIDocumentInfo;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
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
      Node node = uiExplorer.getCurrentNode() ;
      String defaultLanguage = node.getProperty(EXO_LANGUAGE).getValue().getString() ;
      String selectedLanguage = uiForm.getUIFormSelectBox(LANGUAGES).getValue() ;
      Node languagesNode = null ;
      if(node.hasNode(LANGUAGES)) languagesNode = node.getNode(LANGUAGES) ;
      else languagesNode = node.addNode(LANGUAGES) ;
      if(!defaultLanguage.equals(selectedLanguage)) {
        Node newLang = languagesNode.addNode(defaultLanguage) ;
        Node selectedLangNode = languagesNode.getNode(selectedLanguage) ;
        PropertyDefinition[] properties = node.getPrimaryNodeType().getPropertyDefinitions() ;
        for(PropertyDefinition property : properties){
          if(!property.isProtected()){
            String propertyName = property.getName() ;
            newLang.setProperty(propertyName, node.getProperty(propertyName).getValue()) ;
            node.setProperty(propertyName, selectedLangNode.getProperty(propertyName).getValue()) ;
          }
        }
        node.setProperty(EXO_LANGUAGE, selectedLanguage) ;
        selectedLangNode.remove() ;
        node.save() ;
        node.getSession().save() ;
      } else {
        Node newLang = null ;
        if(languagesNode.hasNode(selectedLanguage)) newLang = languagesNode.getNode(selectedLanguage) ;
        else newLang = node ;
        PropertyIterator properties = newLang.getProperties() ;
        while(properties.hasNext()) {
          Property property = properties.nextProperty() ;
          if(property.getName().startsWith("exo") && node.hasProperty(property.getName())) {
            node.setProperty(property.getName(), property.getValue().getString()) ;
          }
        }
        node.save() ;
        node.getSession().save() ;
      }
      uiExplorer.updateAjax(event) ;
    }
  }
  
  static public class ViewActionListener extends EventListener<UIMultiLanguageForm> {
    public void execute(Event<UIMultiLanguageForm> event) throws Exception {
      UIMultiLanguageForm uiForm = event.getSource() ;
      UIJCRExplorer uiJCRExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
      UIDocumentInfo uiDocumentInfo = uiJCRExplorer.findFirstComponentOfType(UIDocumentInfo.class) ;
      uiDocumentInfo.setLanguage(uiForm.getUIFormSelectBox(LANGUAGES).getValue()) ;
      uiJCRExplorer.cancelAction() ;
    }
  }
}
