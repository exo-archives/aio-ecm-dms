/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import java.io.InputStream;
import java.security.AccessControlException;
import java.util.List;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.VersionException;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.DialogFormFields;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.portal.component.view.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.i18n.MultiLanguageService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.component.UIFormInput;
import org.exoplatform.webui.component.UIFormMultiValueInputSet;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : nqhungvn
 *          nguyenkequanghung@yahoo.com
 * July 3, 2006
 * 10:07:15 AM
 * Editor : Pham Tuan
 *        phamtuanchip@yahoo.de
 * Nov 08, 2006  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = DialogFormFields.SaveActionListener.class),
      @EventConfig(listeners = UIDocumentForm.CancelActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDocumentForm.AddActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDocumentForm.RemoveActionListener.class, phase = Phase.DECODE)
    }
)

public class UIDocumentForm extends DialogFormFields implements UIPopupComponent {

  final static public String LANGUAGES = "languages" ;
  final static public String  JCRCONTENT = "jcr:content";
  final static public String  JCRMIMETYPE = "jcr:mimeType";
  final static public String  NT_FILE = "nt:file";
  final static public String  JCRDATA = "jcr:data";
  final static public String  NTUNSTRUCTURED = "nt:unstructured";
  
  private String documentType_ ;
  private boolean isAddNew_ = false ; 
  private boolean isMultiLanguage_ = false ;
  private String selectedLanguage_ = "en" ;
  private String scriptPath_ = null ;
  private InputStream binaryData_ ;
  private Node contentNode_ ;
  private boolean isDefault_ = false;
  
  public UIDocumentForm() throws Exception {
    setActions(new String[]{"Save", "Cancel"}) ;    
  }

  public void setTemplateNode(String type) { documentType_ = type ;}
  
  public boolean isAddNew() {return isAddNew_ ;}
  
  public void setIsMultiLanguage(boolean isMultiLanguage) { isMultiLanguage_ = isMultiLanguage; }
  public boolean isMultiLanguage() { return isMultiLanguage_; }
  
  public void setIsDefaultLanguage(boolean isDefault) { isDefault_ = isDefault ; }
  public boolean isDefaultLanguage() { return isDefault_ ; } 
  
  public void setSelectedLanguage(String selectedLanguage) { selectedLanguage_ = selectedLanguage; }
  public String getSelectedLanguage() { return selectedLanguage_ ; }
  
  public void addNew(boolean b) {isAddNew_ = b ;}
  
  public String getTemplate() {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String userName = Util.getUIPortal().getOwner() ;
    try {
      return templateService.getTemplatePathByUser(true, documentType_, userName) ;
    } catch (Exception e) {
      UIApplication uiApp = getAncestorOfType(UIApplication.class) ;
      Object[] arg = { documentType_ } ;
      uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.not-support", arg, 
                                              ApplicationMessage.ERROR)) ;
      return null ;
    } 
  }

  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return getAncestorOfType(UIJCRExplorer.class).getJCRTemplateResourceResolver() ;
  }

  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
  public void setContentNode(Node contentNode) { contentNode_ = contentNode ; }
  public Node getContentNode() { return contentNode_ ; }
  
  public Node getCurrentNode() { return getAncestorOfType(UIJCRExplorer.class).getCurrentNode() ; }
  
  private void setPath(String scriptPath) { 
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    if(scriptPath.indexOf(":") < 0) {
      scriptPath = uiExplorer.getSession().getWorkspace().getName() + ":" + scriptPath ;
    }
    scriptPath_ = scriptPath ; 
  }
  
  public String getPath() { return scriptPath_ ; }
  
  public boolean isEditing() { return !isAddNew_ ; }
  
  public InputStream getBinaryData() { return binaryData_ ;}
  public void setBinaryData(InputStream data) { this.binaryData_ = data ; }
  
  public void editDocument(Node editNode) throws Exception {
    String documentType = editNode.getProperty("jcr:primaryType").getString();
    if(NT_FILE.equals(documentType)) {
      Node jcrContent = editNode.getNode(JCRCONTENT) ;      
      String mimeType = jcrContent.getProperty(JCRMIMETYPE).getString() ;
      if(!mimeType.startsWith("text")) {      
        setBinaryData(jcrContent.getProperty("jcr:data").getStream()) ;
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  public void setMultiValue(Node node, List inputs, JcrInputProperty property, Session session) throws Exception {
    for (int i = 0; i < inputs.size(); i++) {
      if(inputs.get(i) instanceof UIFormMultiValueInputSet) {
        String inputName = ((UIFormMultiValueInputSet)inputs.get(i)).getName() ;
        List<String> values = (List<String>) ((UIFormMultiValueInputSet)inputs.get(i)).getValue() ;
        property = (JcrInputProperty) properties.get(inputName);
        Value[] multiValue = Utils.getMultiValue(values, property.getType(), session);
        node.setProperty(propertiesName_.get(inputName), multiValue) ;
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  public void storeValue(Event event) throws Exception {
    CmsService cmsService = getApplicationComponent(CmsService.class) ;
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    List inputs = getChildren() ;
    Map inputProperties = Utils.prepareMap(inputs, getInputProperties(), uiExplorer.getSession()) ;
    if(!isMultiLanguage()) {
      String nodeType ;
      Node homeNode ;
      if(isAddNew()) {
        UIDocumentFormController uiDFController = getParent() ;
        homeNode = uiExplorer.getCurrentNode() ;
        nodeType = uiDFController.getChild(UISelectDocumentForm.class).getSelectValue() ;
      } else { 
        homeNode = getNode().getParent() ;
        nodeType = getNode().getPrimaryNodeType().getName() ;
      }       
      String name = getUIStringInput("name").getValue() ;
      String rootPath = "/node";
      JcrInputProperty property = new JcrInputProperty();
      if(inputProperties.containsKey(rootPath)) {
        property = (JcrInputProperty) inputProperties.get(rootPath);  
        property.setValue(name);
        inputProperties.put(rootPath, property);
      } else {
        property.setJcrPath(rootPath) ;
        property.setValue(name) ;
        inputProperties.put(rootPath,property) ;
      }     
      if(isEditing()) {
        if(NT_FILE.equals(documentType_)) {
          JcrInputProperty jcrDataInput = new JcrInputProperty() ;
          jcrDataInput.setJcrPath("/node/jcr:content/jcr:data") ;
          jcrDataInput.setValue(getBinaryData()) ;
          inputProperties.put("/node/jcr:content/jcr:data",jcrDataInput) ;
          Node jcrNode = homeNode.getNode(name).getNode(JCRCONTENT) ; 
          setMultiValue(jcrNode, inputs, property, uiExplorer.getSession()) ;
        }
      }
      try {
        try {
          homeNode.getSession().checkPermission(homeNode.getPath(),"add_node,set_property");
        } catch (AccessControlException e) {
          throw new AccessDeniedException(e.getMessage());
        }
        cmsService.storeNode(nodeType, homeNode, inputProperties, isAddNew());
        homeNode.getSession().save() ;
        Node newNode = homeNode.getNode(name) ;
        if(!newNode.isNodeType("mix:i18n")) newNode.addMixin("mix:i18n") ;
        if(documentType_.equals(NT_FILE) && !isEditing()) {
          setMultiValue(newNode.getNode(JCRCONTENT), inputs, property, uiExplorer.getSession()) ;
        }
        if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save() ;
        uiExplorer.updateAjax(event);
        setPath(newNode.getPath()) ;
      } catch(VersionException ve) {
        UIApplication uiApp = getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.in-versioning", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }catch(Exception e) {
        //e.printStackTrace() ;
        UIApplication uiApp = getAncestorOfType(UIApplication.class);
        String key = "UIDocumentForm.msg.cannot-save" ;
        uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
    } else {
      addLanguage(uiExplorer) ;
      uiExplorer.setIsHidePopup(true) ;
      UIMultiLanguageManager uiManager = getAncestorOfType(UIMultiLanguageManager.class) ;
      UIMultiLanguageForm uiMultiForm = uiManager.getChild(UIMultiLanguageForm.class) ;
      uiMultiForm.updateSelect(uiExplorer.getCurrentNode()) ;
      uiManager.setRenderedChild(UIMultiLanguageForm.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
      uiExplorer.updateAjax(event) ;
    }
  }
  
  private void addLanguage(UIJCRExplorer uiExplorer) throws Exception {
    Node node = uiExplorer.getCurrentNode() ;
    Node languagesNode = null ;
    MultiLanguageService multiLanguageService = getApplicationComponent(MultiLanguageService.class) ;
    if(node.hasNode(LANGUAGES)) languagesNode = node.getNode(LANGUAGES) ;
    else languagesNode = node.addNode(LANGUAGES, NTUNSTRUCTURED) ;
    Node languageNode = null ;
    Workspace ws = uiExplorer.getSession().getWorkspace() ;
    if(node.getPrimaryNodeType().getName().equals(NT_FILE)) { 
      if(languagesNode.hasNode(getSelectedLanguage())) {
        languageNode = languagesNode.getNode(getSelectedLanguage()) ;
      } else {
        languageNode = languagesNode.addNode(getSelectedLanguage()) ;
        Node jcrContent = node.getNode(JCRCONTENT) ;
        node.save() ;
        ws.copy(jcrContent.getPath(), languageNode.getPath() + "/" + jcrContent.getName()) ;
        NodeType[] mixins = node.getMixinNodeTypes() ;
        for(NodeType mixin:mixins) {
          languageNode.addMixin(mixin.getName()) ;            
        }
        node.save() ;
      }
      for(UIComponent uiChild : getChildren()) {
        if(propertiesName_.get(uiChild.getName()).equals(JCRDATA)) {
          String value = ((UIFormInput) uiChild).getValue().toString() ;
          languageNode.getNode(JCRCONTENT).setProperty(JCRDATA, value) ;
        }
      }
      if(isDefaultLanguage()) multiLanguageService.setDefault(node, getSelectedLanguage()) ;
      node.save() ;
    } else {
      if(languagesNode.hasNode(getSelectedLanguage())) {
        languageNode = languagesNode.getNode(getSelectedLanguage()) ;
        for(UIComponent uiChild : getChildren()) {
          if(!propertiesName_.get(uiChild.getName()).equals("/node")) {
            String value = ((UIFormInput) uiChild).getValue().toString() ;
            languageNode.setProperty(propertiesName_.get(uiChild.getName()), value) ;
          }
        }
      } else {
        languageNode = languagesNode.addNode(getSelectedLanguage()) ;
        for(UIComponent uiChild : getChildren()) {
          String value = ((UIFormInput) uiChild).getValue().toString() ;
          languageNode.setProperty(propertiesName_.get(uiChild.getName()), value) ;
        }
      }
      if(isDefaultLanguage()) multiLanguageService.setDefault(node, getSelectedLanguage()) ;
    }
    
    if(!uiExplorer.getPreference().isJcrEnable()) node.getSession().save() ;
  }

  static  public class CancelActionListener extends EventListener<UIDocumentForm> {
    public void execute(Event<UIDocumentForm> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;
    }
  }
  
  static public class AddActionListener extends EventListener<UIDocumentForm> {
    public void execute(Event<UIDocumentForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getParent()) ;
    }
  }

  static public class RemoveActionListener extends EventListener<UIDocumentForm> {
    public void execute(Event<UIDocumentForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getParent()) ;
    }
  }  
}