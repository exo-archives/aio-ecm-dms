/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.workflow.webui.component.controller;

import java.security.AccessControlException;
import java.util.List;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.VersionException;

import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.workflow.utils.SessionsUtils;
import org.exoplatform.workflow.utils.Utils;
import org.exoplatform.workflow.webui.component.DialogFormFields;
import org.exoplatform.workflow.webui.component.JCRResourceResolver;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jun 13, 2007 8:57:46 AM
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

public class UIDocumentForm extends DialogFormFields {

  private String documentType_ ;
  
  public UIDocumentForm() throws Exception {
    setActions(new String[]{"Save", "Cancel"}) ;    
  }

  public void setTemplateNode(String type) { documentType_ = type ; }
  
  private String getRepository() throws RepositoryException {
    ManageableRepository manaRepo = (ManageableRepository)node_.getSession().getRepository() ;
    return manaRepo.getConfiguration().getName() ;
  }
  
  public String getTemplate() {
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    try {
      resetScriptInterceptor() ;
      return templateService.getTemplatePathByUser(true, documentType_, userName, getRepository()) ;
    } catch (Exception e) {
      return null ;
    } 
  }

  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    try {
      ManageableRepository repository = repositoryService.getRepository(getRepository()) ;
      String workspaceName = node_.getSession().getWorkspace().getName() ;
      Session session = SessionsUtils.getSystemProvider().getSession(workspaceName, repository) ;
      return new JCRResourceResolver(session, Utils.EXO_TEMPLATEFILE) ;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return super.getTemplateResourceResolver(context, template);
  }
  
  public Node getCurrentNode() { return node_ ; }
  
  public boolean isEditing() { return true ; }
  
  @SuppressWarnings("unchecked")
  public Node storeValue(Event event) throws Exception {
    List inputs = getChildren() ;
    Map inputProperties = Utils.prepareMap(inputs, getInputProperties()) ;
    Node newNode = null ;
    Node homeNode = getNode().getParent() ;
    try {
      String repository = getRepository() ;
      CmsService cmsService = getApplicationComponent(CmsService.class) ;
      String addedPath = 
        cmsService.storeNode(documentType_, homeNode, inputProperties, false,repository);
      homeNode.getSession().save() ;
      newNode = homeNode.getNode(addedPath.substring(addedPath.lastIndexOf("/") + 1)) ;
      homeNode.save() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(getParent()) ;
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
      UITaskManager uiTaskManager = event.getSource().getParent() ;
      uiTaskManager.setRenderedChild(UITask.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTaskManager) ;
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