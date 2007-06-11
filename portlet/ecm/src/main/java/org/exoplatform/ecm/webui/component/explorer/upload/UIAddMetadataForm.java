/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.upload;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.ecm.webui.component.DialogFormFields;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.metadata.MetadataService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
 * May 25, 2007 8:58:25 AM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = UIAddMetadataForm.SaveActionListener.class),
      @EventConfig(listeners = UIAddMetadataForm.CancelActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UIAddMetadataForm.AddActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIAddMetadataForm.RemoveActionListener.class, phase = Phase.DECODE)
    }
)
public class UIAddMetadataForm extends DialogFormFields {

  private String nodeType_ ;
  public UIAddMetadataForm() throws Exception {
    setActions(new String[] {"Save", "Cancel"}) ;
  }
  
  public void setNodeType(String nodeType) { nodeType_ = nodeType ; }
  public String getNodeType() { return nodeType_ ; } 
  
  public String getDialogTemplatePath() {    
    MetadataService metadataService = getApplicationComponent(MetadataService.class) ;
    try {
      return metadataService.getMetadataPath(nodeType_, true) ;
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
  static public class SaveActionListener extends EventListener<UIAddMetadataForm> {
    public void execute(Event<UIAddMetadataForm> event) throws Exception {
      UIAddMetadataForm uiForm = event.getSource();
      UIJCRExplorer uiJCRExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
      UIUploadContainer uiUploadContainer = uiForm.getAncestorOfType(UIUploadContainer.class) ;
      Node node = uiUploadContainer.getEditNode(uiForm.nodeType_) ;
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
      uiUploadContainer.setRenderedChild(UIUploadContent.class) ;
      uiUploadContainer.removeChild(UIAddMetadataForm.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiUploadContainer) ;
    }
  }
  
  static public class CancelActionListener extends EventListener<UIAddMetadataForm> {
    public void execute(Event<UIAddMetadataForm> event) throws Exception {
      UIUploadContainer uiUploadContainer = event.getSource().getParent() ;
      uiUploadContainer.removeChild(UIAddMetadataForm.class) ;
      uiUploadContainer.setRenderedChild(UIUploadContent.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiUploadContainer) ;
    }
  }

  static public class AddActionListener extends EventListener<UIAddMetadataForm> {
    public void execute(Event<UIAddMetadataForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getParent()) ;
    }
  }

  static public class RemoveActionListener extends EventListener<UIAddMetadataForm> {
    public void execute(Event<UIAddMetadataForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getParent()) ;
    }
  }
}
