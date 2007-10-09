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
import javax.jcr.RepositoryException;
import javax.jcr.version.VersionException;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.jcr.ComponentSelector;
import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.DialogFormFields;
import org.exoplatform.ecm.webui.component.UIJCRBrowser;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
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
      @EventConfig(listeners = UIDocumentForm.RemoveActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDocumentForm.ShowComponentActionListener.class, phase = Phase.DECODE)
    }
)

public class UIDocumentForm extends DialogFormFields implements UIPopupComponent, UISelector {

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
  
  public void updateSelect(String selectField, String value) {
    isUpdateSelect_ = true ;
    getUIStringInput(selectField).setValue(value) ;
    UIDocumentFormController uiContainer = getParent() ;
    uiContainer.removeChildById("PopupComponent") ;
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
    UIApplication uiApp = getAncestorOfType(UIApplication.class);
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
      try {
        homeNode.save() ;
        newNode = homeNode.getNode(addedPath.substring(addedPath.lastIndexOf("/") + 1)) ;
      } catch(Exception e) {
        if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save() ;
        uiExplorer.updateAjax(event);
        return null ;
      }
      if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save() ;
      uiExplorer.updateAjax(event);        
    } catch (AccessControlException ace) {
      ace.printStackTrace() ;
      throw new AccessDeniedException(ace.getMessage());
    } catch(VersionException ve) {
      ve.printStackTrace() ;
      uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.in-versioning", null, 
                                              ApplicationMessage.WARNING)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return null;
    } catch(RepositoryException repo) {
      repo.printStackTrace() ;
      String key = "UIDocumentForm.msg.repository-exception" ;
      uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return null;
    } catch(NumberFormatException nume) {
      String key = "UIDocumentForm.msg.numberformat-exception" ;
      uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return null;
    } catch(Exception e) {
      e.printStackTrace() ;
      String key = "UIDocumentForm.msg.cannot-save" ;
      uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return null;
    }
    return newNode ;
  }
  
  @SuppressWarnings("unchecked")
  static public class ShowComponentActionListener extends EventListener<UIDocumentForm> {
    public void execute(Event<UIDocumentForm> event) throws Exception {
      UIDocumentForm uiForm = event.getSource() ;
      UIDocumentFormController uiContainer = uiForm.getParent() ;
      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Map fieldPropertiesMap = uiForm.components.get(fieldName) ;
      String classPath = (String)fieldPropertiesMap.get("selectorClass") ;
      ClassLoader cl = Thread.currentThread().getContextClassLoader() ;
      Class clazz = Class.forName(classPath, true, cl) ;
      UIComponent uiComp = uiContainer.createUIComponent(clazz, null, null);
      if(uiComp instanceof UIJCRBrowser) {
        UIJCRExplorer explorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
        String repositoryName = explorer.getRepositoryName() ;
        SessionProvider provider = explorer.getSessionProvider() ;                
        ((UIJCRBrowser)uiComp).setRepository(repositoryName) ;
        ((UIJCRBrowser)uiComp).setSessionProvider(provider) ;
        String selectorParams = (String)fieldPropertiesMap.get("selectorParams") ;
        if(selectorParams != null) {
          String[] arrParams = selectorParams.split(",") ;
          if(arrParams.length == 4) {
            ((UIJCRBrowser)uiComp).setFilterType(new String[] {Utils.NT_FILE}) ;
            ((UIJCRBrowser)uiComp).setIsDisable(arrParams[1], true) ;
            ((UIJCRBrowser)uiComp).setRootPath(arrParams[2]) ;
            ((UIJCRBrowser)uiComp).setMimeTypes(new String[] {arrParams[3]}) ;
          }
        }
      }
      uiContainer.initPopup(uiComp) ;
      String param = "returnField=" + fieldName ;
      ((ComponentSelector)uiComp).setComponent(uiForm, new String[]{param}) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
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