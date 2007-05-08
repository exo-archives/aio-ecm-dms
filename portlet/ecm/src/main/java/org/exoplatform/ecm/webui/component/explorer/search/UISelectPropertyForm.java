/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIPopupAction;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormRadioBoxInput;
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
 * Author : Tran The Trong
 *          trong.tran@exoplatform.com
 * May 6, 2007
 * 10:18:56 AM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl",
    events = {
      @EventConfig(phase=Phase.DECODE, listeners = UISelectPropertyForm.CancelActionListener.class),
      @EventConfig(listeners = UISelectPropertyForm.AddActionListener.class),
      @EventConfig(listeners = UISelectPropertyForm.ChangeMetadataTypeActionListener.class)
    }    
)
public class UISelectPropertyForm extends UIForm implements UIPopupComponent {
  final static public String METADATA_TYPE= "metadataType" ;
  final static public String PROPERTY = "property" ;
  
  private List<SelectItemOption<String>> properties_ = new ArrayList<SelectItemOption<String>>() ;
  
  public UISelectPropertyForm() throws Exception {
    setActions(new String[] {"Add", "Cancel"}) ;
  }
  
  public String getLabel(ResourceBundle res, String id)  {
    try {
      return super.getLabel(res, id) ;
    } catch (Exception ex) {
      return id ;
    }
  }
  
  public void activate() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    CmsConfigurationService cmsConfigService = getApplicationComponent(CmsConfigurationService.class) ;
    UIFormSelectBox uiSelect = new UIFormSelectBox(METADATA_TYPE, METADATA_TYPE, options) ;
    uiSelect.setOnChange("ChangeMetadataType") ;
    addUIFormInput(uiSelect) ;
    String metadataPath = cmsConfigService.getJcrPath(BasePath.METADATA_PATH) ;
    UIJCRExplorer uiExpolrer = getAncestorOfType(UIJCRExplorer.class) ;
    Node homeNode = (Node) uiExpolrer.getSession().getItem(metadataPath) ;
    NodeIterator nodeIter = homeNode.getNodes() ;
    Node meta = nodeIter.nextNode() ;
    renderProperties(meta.getName()) ;
    options.add(new SelectItemOption<String>(meta.getName(), meta.getName())) ;
    while(nodeIter.hasNext()) {
      meta = nodeIter.nextNode() ;
      options.add(new SelectItemOption<String>(meta.getName(), meta.getName())) ;
    }
    
    addUIFormInput(new UIFormRadioBoxInput(PROPERTY, null, properties_).
                        setAlign(UIFormRadioBoxInput.VERTICAL_ALIGN)) ;
  }
  
  public void deActivate() throws Exception {}

  public void renderProperties(String metadata) throws Exception {
    properties_.clear() ;
    UIJCRExplorer uiExpolrer = getAncestorOfType(UIJCRExplorer.class) ;
    NodeTypeManager ntManager = uiExpolrer.getSession().getWorkspace().getNodeTypeManager() ;
    NodeType nt = ntManager.getNodeType(metadata) ;
    PropertyDefinition[] properties = nt.getPropertyDefinitions() ;
    for(PropertyDefinition property : properties) {
      String name = property.getName() ;
      properties_.add(new SelectItemOption<String>(name, name)) ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UISelectPropertyForm> {
    public void execute(Event<UISelectPropertyForm> event) throws Exception {
      UISearchContainer uiSearchContainer = event.getSource().getAncestorOfType(UISearchContainer.class) ;
      UIPopupAction uiPopup = uiSearchContainer.getChild(UIPopupAction.class) ;
      uiPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }
  
  static  public class AddActionListener extends EventListener<UISelectPropertyForm> {
    public void execute(Event<UISelectPropertyForm> event) throws Exception {
      UISelectPropertyForm uiForm = event.getSource() ;
      String property = uiForm.<UIFormRadioBoxInput>getUIInput(PROPERTY).getValue();
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class);
      UISearchContainer uiSearchContainer = uiPopupAction.getParent() ;
      UIConstraintsForm uiConstraintsForm =
        uiSearchContainer.findFirstComponentOfType(UIConstraintsForm.class) ;
      uiConstraintsForm.getUIStringInput(UIConstraintsForm.PROPERTY1).setValue(property) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiConstraintsForm) ;
    }
  }
  
  static  public class ChangeMetadataTypeActionListener extends EventListener<UISelectPropertyForm> {
    public void execute(Event<UISelectPropertyForm> event) throws Exception {
      UISelectPropertyForm uiForm = event.getSource() ;
      uiForm.renderProperties(uiForm.getUIFormSelectBox(METADATA_TYPE).getValue()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }
}
