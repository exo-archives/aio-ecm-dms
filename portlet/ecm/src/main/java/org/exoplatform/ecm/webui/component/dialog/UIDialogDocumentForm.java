/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.dialog;

import java.security.AccessControlException;
import java.util.List;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.VersionException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.exoplatform.ecm.jcr.JCRResourceResolver;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.DialogFormFields;
import org.exoplatform.portal.component.view.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.component.UIApplication;
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
 * Apr 24, 2007 11:56:19 PM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = DialogFormFields.SaveActionListener.class),
      @EventConfig(listeners = UIDialogDocumentForm.AddActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDialogDocumentForm.RemoveActionListener.class, phase = Phase.DECODE)
    }
)
public class UIDialogDocumentForm extends DialogFormFields {

  private String documentType_ ;
  private JCRResourceResolver jcrTemplateResourceResolver_ ;
  
  public UIDialogDocumentForm() throws Exception {
    setActions(new String[]{"Save"}) ;
  }
  
  public String getTemplate() {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    try {
      resetScriptInterceptor() ;
      return templateService.getTemplatePathByUser(true, documentType_, userName) ;
    } catch (Exception e) {
      UIApplication uiApp = getAncestorOfType(UIApplication.class) ;
      Object[] arg = { documentType_ } ;
      uiApp.addMessage(new ApplicationMessage("UIDialogDocumentForm.msg.not-support", arg, 
                                              ApplicationMessage.ERROR)) ;
      return null ;
    }
  }
  
  public Node getCurrentNode() throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    CmsConfigurationService cmsConfigurationService = 
      getApplicationComponent(CmsConfigurationService.class) ;
    Session session = 
      repositoryService.getRepository().getSystemSession(cmsConfigurationService.getWorkspace()) ;
    PortletRequestContext portletContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance() ;
    PortletRequest request = portletContext.getRequest() ; 
    PortletPreferences preferences = request.getPreferences() ;
    return (Node) session.getItem(preferences.getValue("path", ""));
  }
  
  public void setTemplateNode(String type) { documentType_ = type ; }
  
  public boolean isEditing() { return false ; }
  
  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    if(jcrTemplateResourceResolver_ == null) newJCRTemplateResourceResolver() ; 
    return jcrTemplateResourceResolver_; 
  }
  
  public void newJCRTemplateResourceResolver() {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    CmsConfigurationService cmsConfigurationService = getApplicationComponent(CmsConfigurationService.class) ;
    Session session = null;
    try {
      session = repositoryService.getRepository().getSystemSession(cmsConfigurationService.getWorkspace()) ;
      rootPath_ = session.getRootNode().getPath() ;
    } catch(Exception e) { }
    jcrTemplateResourceResolver_ = new JCRResourceResolver(session, "exo:templateFile") ; 
  }
  
  @SuppressWarnings("unused")
  public Node storeValue(Event event) throws Exception {
    CmsService cmsService = getApplicationComponent(CmsService.class) ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    UIApplication uiApp = getAncestorOfType(UIApplication.class);
    CmsConfigurationService cmsConfigurationService = 
      getApplicationComponent(CmsConfigurationService.class) ;
    Session session = 
      repositoryService.getRepository().getSystemSession(cmsConfigurationService.getWorkspace()) ;
    List inputs = getChildren() ;
    Map inputProperties = Utils.prepareMap(inputs, getInputProperties(), session) ;
    PortletRequestContext portletContext = (PortletRequestContext) event.getRequestContext() ;
    PortletRequest request = portletContext.getRequest() ; 
    PortletPreferences preferences = request.getPreferences() ;
    String prefLocate = preferences.getValue("path", "") ;
    String prefType = preferences.getValue("type", "") ;
    Node homeNode = (Node) session.getItem(prefLocate);
    try {
      String addedPath = cmsService.storeNode(prefType, homeNode, inputProperties, true, 
          Util.getPortalRequestContext().getRemoteUser());
      homeNode.getSession().save() ;
      Object[] args = { prefLocate } ;
      reset() ;
      uiApp.addMessage(new ApplicationMessage("UIDialogDocumentForm.msg.saved-successfully", args)) ;
    } catch (AccessControlException ace) {
      throw new AccessDeniedException(ace.getMessage());
    } catch(VersionException ve) {
      uiApp.addMessage(new ApplicationMessage("UIDialogDocumentForm.msg.in-versioning", null, 
          ApplicationMessage.WARNING)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return null;
    } catch(Exception e) {
      String key = "UIDialogDocumentForm.msg.cannot-save" ;
      uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return null;
    }
    return null ;
  }
  
  static public class AddActionListener extends EventListener<UIDialogDocumentForm> {
    public void execute(Event<UIDialogDocumentForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getParent()) ;
    }
  }

  static public class RemoveActionListener extends EventListener<UIDialogDocumentForm> {
    public void execute(Event<UIDialogDocumentForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getParent()) ;
    }
  }  
}
