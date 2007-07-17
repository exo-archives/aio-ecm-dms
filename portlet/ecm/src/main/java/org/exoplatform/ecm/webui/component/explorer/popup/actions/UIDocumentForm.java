/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import java.security.AccessControlException;
import java.util.List;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.version.VersionException;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.DialogFormFields;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
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

  private String documentType_ ;
  private boolean isAddNew_ = false ; 
  public UIDocumentForm() throws Exception {
    setActions(new String[]{"Save", "Cancel"}) ;    
  }

  public void setTemplateNode(String type) { documentType_ = type ;}
  
  public boolean isAddNew() {return isAddNew_ ;}
  
  public void addNew(boolean b) {isAddNew_ = b ;}
  
  private String getRepository() {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
    return portletPref.getValue(Utils.REPOSITORY, "") ;
  }
  
  public String getTemplate() {
    repository_ = getRepository() ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    try {
      resetScriptInterceptor() ;
      return templateService.getTemplatePathByUser(true, documentType_, userName, repository_) ;
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
  
  public Node getCurrentNode() { return getAncestorOfType(UIJCRExplorer.class).getCurrentNode() ; }
  
  public boolean isEditing() { return !isAddNew_ ; }
  
  @SuppressWarnings("unchecked")
  public Node storeValue(Event event) throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    List inputs = getChildren() ;
    Map inputProperties = Utils.prepareMap(inputs, getInputProperties(), uiExplorer.getSession()) ;
    Node newNode = null ;
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
    try {
      String repository = getRepository() ;
      CmsService cmsService = getApplicationComponent(CmsService.class) ;
      String addedPath = cmsService.storeNode(nodeType, homeNode, inputProperties, isAddNew(),repository);
      homeNode.getSession().save() ;
      newNode = homeNode.getNode(addedPath.substring(addedPath.lastIndexOf("/") + 1)) ;
      if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save() ;
      uiExplorer.updateAjax(event);        
    } catch (AccessControlException ace) {
      ace.printStackTrace() ;
      throw new AccessDeniedException(ace.getMessage());
    } catch(VersionException ve) {
      ve.printStackTrace() ;
      UIApplication uiApp = getAncestorOfType(UIApplication.class);
      uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.in-versioning", null, 
                                              ApplicationMessage.WARNING)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return null;
    } catch(Exception e) {
      e.printStackTrace() ;
      UIApplication uiApp = getAncestorOfType(UIApplication.class);
      String key = "UIDocumentForm.msg.cannot-save" ;
      uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return null;
    }
    return newNode ;
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