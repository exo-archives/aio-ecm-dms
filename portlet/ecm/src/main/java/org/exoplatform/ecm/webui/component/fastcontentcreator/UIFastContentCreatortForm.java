/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.fastcontentcreator;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.version.VersionException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.exoplatform.ecm.jcr.JCRResourceResolver;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.DialogFormFields;
import org.exoplatform.portal.component.view.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;

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
      @EventConfig(listeners = UIFastContentCreatortForm.AddActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIFastContentCreatortForm.RemoveActionListener.class, phase = Phase.DECODE)
    }
)
public class UIFastContentCreatortForm extends DialogFormFields {

  private String documentType_ ;
  private JCRResourceResolver jcrTemplateResourceResolver_ ;
  
  public UIFastContentCreatortForm() throws Exception {
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
      uiApp.addMessage(new ApplicationMessage("UIFastContentCreatortForm.msg.not-support", arg, 
                                              ApplicationMessage.ERROR)) ;
      return null ;
    }
  }
  
  public Node getCurrentNode() throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    PortletRequestContext portletContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance() ;
    PortletRequest request = portletContext.getRequest() ; 
    PortletPreferences preferences = request.getPreferences() ;
    Session session = 
      repositoryService.getRepository().getSystemSession(preferences.getValue("workspace", "")) ;
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
    PortletRequestContext portletContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance() ;
    PortletRequest request = portletContext.getRequest() ; 
    PortletPreferences preferences = request.getPreferences() ;
    Session session = null ;
    try {
      session = repositoryService.getRepository().getSystemSession(preferences.getValue("workspace", ""));
      rootPath_ = session.getRootNode().getPath() ;
    } catch(Exception e) { }
    jcrTemplateResourceResolver_ = new JCRResourceResolver(session, "exo:templateFile") ; 
  }
  
  @SuppressWarnings("unused")
  public Node storeValue(Event event) throws Exception {
    CmsService cmsService = getApplicationComponent(CmsService.class) ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    UIApplication uiApp = getAncestorOfType(UIApplication.class);
    PortletRequestContext portletContext = (PortletRequestContext) event.getRequestContext() ;
    PortletRequest request = portletContext.getRequest() ; 
    PortletPreferences preferences = request.getPreferences() ;
    String prefLocate = preferences.getValue("path", "") ;
    String prefType = preferences.getValue("type", "") ;
    String workspace = preferences.getValue("workspace", "") ;
    Session session = repositoryService.getRepository().getSystemSession(workspace) ;
    Map inputProperties = Utils.prepareMap(getChildren(), getInputProperties(), session) ;
    Node homeNode = (Node) session.getItem(prefLocate);
    try {
      String addedPath = cmsService.storeNode(prefType, homeNode, inputProperties, true, 
                                              Util.getPortalRequestContext().getRemoteUser());
      homeNode.getSession().save() ;
      reset() ;
      for(UIComponent uiChild : getChildren()) {
        if(uiChild instanceof UIFormMultiValueInputSet) {
          ((UIFormMultiValueInputSet)uiChild).setValue(new ArrayList<Value>()) ;
        }
      }
      Object[] args = { prefLocate } ;
      uiApp.addMessage(new ApplicationMessage("UIFastContentCreatortForm.msg.saved-successfully", args)) ;
    } catch (AccessControlException ace) {
      throw new AccessDeniedException(ace.getMessage());
    } catch(VersionException ve) {
      uiApp.addMessage(new ApplicationMessage("UIFastContentCreatortForm.msg.in-versioning", null, 
                                              ApplicationMessage.WARNING)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return null;
    } catch(Exception e) {
      String key = "UIFastContentCreatortForm.msg.cannot-save" ;
      uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return null;
    }
    return null ;
  }
  
  static public class AddActionListener extends EventListener<UIFastContentCreatortForm> {
    public void execute(Event<UIFastContentCreatortForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getParent()) ;
    }
  }

  static public class RemoveActionListener extends EventListener<UIFastContentCreatortForm> {
    public void execute(Event<UIFastContentCreatortForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getParent()) ;
    }
  }  
}
