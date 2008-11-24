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
package org.exoplatform.ecm.workflow.webui.component.controller;

import java.security.AccessControlException;
import java.util.List;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.version.VersionException;

import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.form.UIDialogForm;
import org.exoplatform.ecm.webui.utils.DialogFormUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.templates.TemplateService;
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
import org.exoplatform.workflow.webui.component.controller.UITaskManager;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jun 13, 2007 8:57:46 AM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = UIDocumentForm.SaveActionListener.class),
      @EventConfig(listeners = UIDocumentForm.CancelActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDocumentForm.AddActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDocumentForm.RemoveActionListener.class, phase = Phase.DECODE)
    }
)

public class UIDocumentForm extends UIDialogForm {

  private String documentType_ ;
  
  public UIDocumentForm() throws Exception {
    setActions(new String[]{"Save", "Cancel"}) ;    
  }

  public void setTemplateNode(String type) { documentType_ = type ; }
  
  private String getRepository() throws Exception {
    ManageableRepository manaRepo = (ManageableRepository)getCurrentNode().getSession().getRepository() ;
    return manaRepo.getConfiguration().getName() ;
  }
  
  public String getTemplate() {
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    try {
      return templateService.getTemplatePathByUser(true, documentType_, userName, getRepository()) ;
    } catch (Exception e) {
    	e.printStackTrace();
      return null ;
    } 
  }

  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    try {
      String workspaceName = getCurrentNode().getSession().getWorkspace().getName() ;
      return new JCRResourceResolver(getRepository(), workspaceName, Utils.EXO_TEMPLATEFILE);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return super.getTemplateResourceResolver(context, template);
  }
  
  public Node getCurrentNode() throws Exception { return getNode() ; }
  
  public boolean isEditing() { return true ; }
  
  static  public class SaveActionListener extends EventListener<UIDocumentForm> {
    public void execute(Event<UIDocumentForm> event) throws Exception {
      UIDocumentForm uiForm = event.getSource();
      List inputs = uiForm.getChildren() ;
      Map inputProperties = DialogFormUtil.prepareMap(inputs, uiForm.getInputProperties()) ;
      Node homeNode = uiForm.getNode().getParent() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class);
      try {
        String repository = uiForm.getRepository() ;
        CmsService cmsService = uiForm.getApplicationComponent(CmsService.class) ;
        cmsService.storeNode(uiForm.documentType_, homeNode, inputProperties, false,repository);
        homeNode.getSession().save() ;
        homeNode.save() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
      } catch (AccessControlException ace) {
        ace.printStackTrace() ;
        throw new AccessDeniedException(ace.getMessage());
      } catch(VersionException ve) {
        ve.printStackTrace() ;
        uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.in-versioning", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } catch(Exception e) {
        e.printStackTrace() ;
        String key = "UIDocumentForm.msg.cannot-save" ;
        uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      }
    }
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