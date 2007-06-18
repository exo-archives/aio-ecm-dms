/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.info;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.ecm.webui.component.DialogFormFields;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorerPortlet;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.metadata.MetadataService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jan 25, 2007  
 * 1:47:55 PM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = UIViewMetadataForm.SaveActionListener.class),
      @EventConfig(listeners = UIViewMetadataForm.CancelActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UIViewMetadataForm.AddActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIViewMetadataForm.RemoveActionListener.class, phase = Phase.DECODE)
    }
)
public class UIViewMetadataForm extends DialogFormFields {

  private String nodeType_ ;
  
  public UIViewMetadataForm() throws Exception {
    setActions(new String[] {"Save", "Cancel"}) ;
  }

  public void setNodeType(String nodeType) { nodeType_ = nodeType ; }
  public String getNodeType() { return nodeType_ ; } 
  
  public String getDialogTemplatePath() { 
    repository_ = getAncestorOfType(UIJCRExplorerPortlet.class).getPreferenceRepository() ;
    MetadataService metadataService = getApplicationComponent(MetadataService.class) ;
    try {
      return metadataService.getMetadataPath(nodeType_, true, repository_) ;
    } catch (Exception e) {
      e.printStackTrace() ;
    } 
    return null ;
  }
  
  public String getTemplate() { return getDialogTemplatePath() ; }
  
  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return getAncestorOfType(UIJCRExplorer.class).getJCRTemplateResourceResolver() ;
  }

  @SuppressWarnings("unchecked")
  static public class SaveActionListener extends EventListener<UIViewMetadataForm> {
    public void execute(Event<UIViewMetadataForm> event) throws Exception {
      UIViewMetadataForm uiForm = event.getSource();
      UIJCRExplorer uiJCRExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
      UIViewMetadataManager uiViewManager = uiForm.getAncestorOfType(UIViewMetadataManager.class) ;
      Node node = uiViewManager.getViewNode(uiForm.getNodeType());
      NodeTypeManager ntManager = uiJCRExplorer.getSession().getWorkspace().getNodeTypeManager();
      PropertyDefinition[] props = ntManager.getNodeType(uiForm.getNodeType()).getPropertyDefinitions();
      List<Value> valueList = new ArrayList<Value>();
      for (PropertyDefinition prop : props) {
        String name = prop.getName();
        String inputName = name.substring(name.indexOf(":") + 1);
        if (!prop.isProtected()) {
          int requiredType = prop.getRequiredType();
          if (prop.isMultiple()) {
            if (requiredType == 5) { // date
              UIFormDateTimeInput uiFormDateTime = (UIFormDateTimeInput) uiForm.getUIInput(inputName);
              valueList.add(uiJCRExplorer.getSession().getValueFactory().createValue(uiFormDateTime.getCalendar())) ;
              node.setProperty(name, valueList.toArray(new Value[] {}));
            } else {
              List<String> values = (List<String>) ((UIFormMultiValueInputSet)uiForm.getUIInput(inputName)).getValue() ;
              node.setProperty(name, values.toArray(new String[values.size()]));
            }
          } else {
            if (requiredType == 6) { // boolean
              UIFormInput uiInput = uiForm.getUIInput(inputName) ;
              String value = "false";
              if(uiInput instanceof UIFormSelectBox) value =  ((UIFormSelectBox)uiInput).getValue() ;
              node.setProperty(name, Boolean.parseBoolean(value));
            } else if (requiredType == 5) { // date
              UIFormDateTimeInput cal = (UIFormDateTimeInput) uiForm.getUIInput(inputName);
              node.setProperty(name, cal.getCalendar());
            } else if(requiredType == 1){
              String value = ((UIFormStringInput)uiForm.getUIInput(inputName)).getValue() ;
              if(value == null) value = "" ;
              node.setProperty(name, value);
            }
          }
        }
      }
      node.save();
      node.getSession().save();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewManager) ;
      UIPopupWindow uiPopup = uiViewManager.getChildById(UIViewMetadataManager.METADATAS_POPUP) ;
      uiPopup.setShow(false) ;
    }
  }
  
  static public class CancelActionListener extends EventListener<UIViewMetadataForm> {
    public void execute(Event<UIViewMetadataForm> event) throws Exception {
      UIViewMetadataForm uiForm = event.getSource() ;
      UIPopupWindow uiPopup = uiForm.getAncestorOfType(UIPopupWindow.class) ;
      uiPopup.setShow(false) ;
      uiPopup.setRendered(false) ;
    }
  }

  static public class AddActionListener extends EventListener<UIViewMetadataForm> {
    public void execute(Event<UIViewMetadataForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getParent()) ;
    }
  }

  static public class RemoveActionListener extends EventListener<UIViewMetadataForm> {
    public void execute(Event<UIViewMetadataForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getParent()) ;
    }
  }  
}
