/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormCheckBoxInput;
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
 * Dec 27, 2006  
 * 10:18:56 AM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/component/UIForm.gtmpl",
    events = {
      @EventConfig(phase=Phase.DECODE, listeners = UIMetadataSelectForm.CancelActionListener.class),
      @EventConfig(listeners = UIMetadataSelectForm.AddActionListener.class),
      @EventConfig(listeners = UIMetadataSelectForm.ChangeMetadataTypeActionListener.class)
    }    
)
public class UIMetadataSelectForm extends UIForm implements UIPopupComponent {

  final static public String METADATA_TYPE= "metadataType" ;
  private String fieldName_ ;
  
  private List<SelectItemOption<String>> options_ = new ArrayList<SelectItemOption<String>>() ;
 
  public UIMetadataSelectForm() throws Exception {
    setActions(new String[] {"Add", "Cancel"}) ;
  }
  
  public String getLabel(ResourceBundle res, String id)  {
    try {
      return res.getString("UIMetadataSelectForm.label." + id) ;
    } catch (MissingResourceException ex) {
      return id ;
    }
  }
  
  public void setFieldName(String fieldName) { fieldName_ = fieldName ; }
  
  public void setMetadataOptions() throws Exception {
    CmsConfigurationService cmsConfigService = getApplicationComponent(CmsConfigurationService.class) ;
    String metadataPath = cmsConfigService.getJcrPath(BasePath.METADATA_PATH) ;
    UIJCRExplorer uiExpolrer = getAncestorOfType(UIJCRExplorer.class) ;
    Node homeNode = (Node) uiExpolrer.getSession().getItem(metadataPath) ;
    NodeIterator nodeIter = homeNode.getNodes() ;
    while(nodeIter.hasNext()) {
      Node meta = nodeIter.nextNode() ;
      options_.add(new SelectItemOption<String>(meta.getName(), meta.getName())) ;
    }
    getUIFormSelectBox(METADATA_TYPE).setOptions(options_) ;
  }

  private boolean propertiesSelected(String name) {
    UISearchContainer uiSearchContainer = getAncestorOfType(UISearchContainer.class) ;
    UIConstraintsForm uiConstraintsForm = 
      uiSearchContainer.findFirstComponentOfType(UIConstraintsForm.class) ;
    String typeValues = uiConstraintsForm.getUIStringInput(fieldName_).getValue() ;
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
  
  public void renderProperties(String metadata) throws Exception {
    UIJCRExplorer uiExpolrer = getAncestorOfType(UIJCRExplorer.class) ;
    getChildren().clear() ;
    UIFormSelectBox uiSelect = new UIFormSelectBox(METADATA_TYPE, METADATA_TYPE, options_) ;
    uiSelect.setOnChange("ChangeMetadataType") ;
    addUIFormInput(uiSelect) ;
    uiSelect.setOptions(options_) ;
    uiSelect.setValue(metadata) ;
    NodeTypeManager ntManager = uiExpolrer.getSession().getWorkspace().getNodeTypeManager() ;
    NodeType nt = ntManager.getNodeType(metadata) ;
    PropertyDefinition[] properties = nt.getPropertyDefinitions() ;
    UIFormCheckBoxInput<String> uiCheckBox ;
    for(int i = 0 ; i < properties.length ; i ++) {
      String name = properties[i].getName() ;
      uiCheckBox = new UIFormCheckBoxInput<String>(name, name, "") ;
      if(propertiesSelected(name)) uiCheckBox.setChecked(true) ;
      else uiCheckBox.setChecked(false) ;
      addUIFormInput(uiCheckBox) ;
    }
  }
  
  public void setProperties(List<String> selectedProperties) {
    String strProperties = null ;
    UISearchContainer uiContainer = getAncestorOfType(UISearchContainer.class) ;
    UIConstraintsForm uiConstraintsForm = uiContainer.findFirstComponentOfType(UIConstraintsForm.class) ;
    for(int i = 0 ; i < selectedProperties.size() ; i++) {
      if(strProperties == null) strProperties = selectedProperties.get(i) ;
      else strProperties = strProperties + "," + selectedProperties.get(i) ;
    }
    uiConstraintsForm.getUIStringInput(fieldName_).setValue(strProperties) ;
  }
  
  public void activate() throws Exception {}

  public void deActivate() throws Exception {}
  
  static  public class CancelActionListener extends EventListener<UIMetadataSelectForm> {
    public void execute(Event<UIMetadataSelectForm> event) throws Exception {
      UISearchContainer uiSearchContainer = event.getSource().getAncestorOfType(UISearchContainer.class) ;
      UIPopupAction uiPopup = uiSearchContainer.getChild(UIPopupAction.class) ;
      uiPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSearchContainer) ;
    }
  }
  
  static  public class AddActionListener extends EventListener<UIMetadataSelectForm> {
    public void execute(Event<UIMetadataSelectForm> event) throws Exception {
      UIMetadataSelectForm uiForm = event.getSource() ;
      UISearchContainer uiSearchContainer = uiForm.getAncestorOfType(UISearchContainer.class) ;
      UIConstraintsForm uiConstraintsForm = 
        uiSearchContainer.findFirstComponentOfType(UIConstraintsForm.class) ;
      List<String> selectedProperties = new ArrayList<String>() ;
      List<UIFormCheckBoxInput> listCheckbox =  new ArrayList<UIFormCheckBoxInput>();
      uiForm.findComponentOfType(listCheckbox, UIFormCheckBoxInput.class);
      String propertiesValue = 
        uiConstraintsForm.getUIStringInput(uiForm.fieldName_).getValue() ;
      if(propertiesValue != null && propertiesValue.length() > 0) {
        String[] array = propertiesValue.split(",") ;
        for(int i = 0; i < array.length; i ++) {
          selectedProperties.add(array[i].trim()) ;
        }
      }
      for(int i = 0; i < listCheckbox.size(); i ++) {
        if(listCheckbox.get(i).isChecked()) {
          if(!selectedProperties.contains(listCheckbox.get(i).getName())) {
            selectedProperties.add(listCheckbox.get(i).getName()) ;
          }
        } else if(selectedProperties.contains(listCheckbox.get(i))) {
          selectedProperties.remove(listCheckbox.get(i).getName()) ;
        } else {
          selectedProperties.remove(listCheckbox.get(i).getName()) ;
        }
      }
      uiForm.setProperties(selectedProperties) ;
      UIPopupAction uiPopup = uiSearchContainer.getChild(UIPopupAction.class) ;
      uiPopup.deActivate() ;
      uiSearchContainer.setSelectedValue(uiForm.getUIFormSelectBox(METADATA_TYPE).getValue()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSearchContainer) ;
    }
  }
  
  static  public class ChangeMetadataTypeActionListener extends EventListener<UIMetadataSelectForm> {
    public void execute(Event<UIMetadataSelectForm> event) throws Exception {
      UIMetadataSelectForm uiForm = event.getSource() ;
      uiForm.renderProperties(uiForm.getUIFormSelectBox(METADATA_TYPE).getValue()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }
}
