/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormCheckBoxInput;
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
 * May 11, 2007 4:21:57 PM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl",
    events = {
      @EventConfig(listeners = UINodeTypeSelectForm.SaveActionListener.class),
      @EventConfig(listeners = UINodeTypeSelectForm.CancelActionListener.class, phase=Phase.DECODE)
    }    
)
public class UINodeTypeSelectForm extends UIForm implements UIPopupComponent {

  public UINodeTypeSelectForm() throws Exception {
  }
  
  public String getLabel(ResourceBundle res, String id)  {
    try {
      return res.getString("UINodeTypeSelectForm.label." + id) ;
    } catch (MissingResourceException ex) {
      return id ;
    }
  }
  
  @SuppressWarnings("unchecked")
  public void setRenderNodeTypes() throws Exception {
    getChildren().clear() ;
    UIFormCheckBoxInput<String> uiCheckBox ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    List<String> templates = templateService.getDocumentTemplates() ;
    for(String template : templates) {
      uiCheckBox = new UIFormCheckBoxInput<String>(template, template, "") ;
      if(propertiesSelected(template)) uiCheckBox.setChecked(true) ;
      else uiCheckBox.setChecked(false) ;
      addUIFormInput(uiCheckBox) ;
    }
  }
  
  private boolean propertiesSelected(String name) {
    UISearchContainer uiSearchContainer = getAncestorOfType(UISearchContainer.class) ;
    UIConstraintsForm uiConstraintsForm = 
      uiSearchContainer.findFirstComponentOfType(UIConstraintsForm.class) ;
    String typeValues = uiConstraintsForm.getUIStringInput(UIConstraintsForm.DOC_TYPE).getValue() ;
    if(typeValues == null) return false ;
    if(typeValues.indexOf(",") > -1) {
      String[] values = typeValues.split(",") ;
      for(String value : values) {
        if(value.equals(name)) return true ;
      }
    } else if(typeValues.equals(name)) {
      return true ;
    } 
    return false ;
  }
  
  public void setNodeTypes(List<String> selectedNodeTypes) {
    String strNodeTypes = null ;
    UISearchContainer uiContainer = getAncestorOfType(UISearchContainer.class) ;
    UIConstraintsForm uiConstraintsForm = uiContainer.findFirstComponentOfType(UIConstraintsForm.class) ;
    for(int i = 0 ; i < selectedNodeTypes.size() ; i++) {
      if(strNodeTypes == null) strNodeTypes = selectedNodeTypes.get(i) ;
      else strNodeTypes = strNodeTypes + "," + selectedNodeTypes.get(i) ;
    }
    uiConstraintsForm.getUIStringInput(UIConstraintsForm.DOC_TYPE).setValue(strNodeTypes) ;
  }

  public void activate() throws Exception {}

  public void deActivate() throws Exception {}
  
  static public class SaveActionListener extends EventListener<UINodeTypeSelectForm> {
    public void execute(Event<UINodeTypeSelectForm> event) throws Exception {
      UINodeTypeSelectForm uiForm = event.getSource() ;
      UISearchContainer uiSearchContainer = uiForm.getAncestorOfType(UISearchContainer.class) ;
      UIConstraintsForm uiConstraintsForm = 
        uiSearchContainer.findFirstComponentOfType(UIConstraintsForm.class) ;
      List<String> selectedNodeTypes = new ArrayList<String>() ;
      List<UIFormCheckBoxInput> listCheckbox =  new ArrayList<UIFormCheckBoxInput>();
      uiForm.findComponentOfType(listCheckbox, UIFormCheckBoxInput.class);
      String nodeTypesValue = 
        uiConstraintsForm.getUIStringInput(UIConstraintsForm.DOC_TYPE).getValue() ;
      if(nodeTypesValue != null && nodeTypesValue.length() > 0) {
        String[] array = nodeTypesValue.split(",") ;
        for(int i = 0; i < array.length; i ++) {
          selectedNodeTypes.add(array[i].trim()) ;
        }
      }
      for(int i = 0; i < listCheckbox.size(); i ++) {
        if(listCheckbox.get(i).isChecked()) {
          if(!selectedNodeTypes.contains(listCheckbox.get(i).getName())) {
            selectedNodeTypes.add(listCheckbox.get(i).getName()) ;
          }
        } else if(selectedNodeTypes.contains(listCheckbox.get(i))) {
          selectedNodeTypes.remove(listCheckbox.get(i).getName()) ;
        } else {
          selectedNodeTypes.remove(listCheckbox.get(i).getName()) ;
        }
      }
      uiForm.setNodeTypes(selectedNodeTypes) ;
      UIPopupAction uiPopup = uiSearchContainer.getChild(UIPopupAction.class) ;
      uiPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSearchContainer) ;
    }
  }

  static public class CancelActionListener extends EventListener<UINodeTypeSelectForm> {
    public void execute(Event<UINodeTypeSelectForm> event) throws Exception {
      UISearchContainer uiSearchContainer = event.getSource().getAncestorOfType(UISearchContainer.class) ;
      UIPopupAction uiPopup = uiSearchContainer.getChild(UIPopupAction.class) ;
      uiPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSearchContainer) ;
    }
  }
}
