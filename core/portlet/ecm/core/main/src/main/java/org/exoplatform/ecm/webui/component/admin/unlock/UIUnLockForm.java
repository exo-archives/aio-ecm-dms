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
package org.exoplatform.ecm.webui.component.admin.unlock;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;

import org.exoplatform.ecm.webui.form.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.form.validator.ECMNameValidator;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.utils.LockUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Dec 29, 2006  
 * 11:30:29 AM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIUnLockForm.SaveActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIUnLockForm.CancelActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIUnLockForm.AddPermissionActionListener.class)
    }
)
public class UIUnLockForm extends UIForm implements UISelectable {
  
  final static public String PATH_NODE = "name";
  final static public String PERMISSIONS = "permissions";
  final static public String[] ACTIONS = {"Save", "Cancel"};
  final static public String[] REG_EXPRESSION = {"[", "]", "&"};
  
  public UIUnLockForm() throws Exception {
    addUIFormInput(new UIFormStringInput(PATH_NODE, PATH_NODE, null).
      addValidator(MandatoryValidator.class).addValidator(ECMNameValidator.class));    
    UIFormInputSetWithAction uiInputAct = new UIFormInputSetWithAction("PermissionButton");
    uiInputAct.addUIFormInput( new UIFormStringInput(PERMISSIONS, PERMISSIONS, null).setEditable(false).addValidator(MandatoryValidator.class));
    uiInputAct.setActionInfo(PERMISSIONS, new String[] {"AddPermission"});
    addUIComponentInput(uiInputAct);
  }

  public String[] getActions() { return ACTIONS ; }

  public void doSelect(String selectField, Object value) {
    getUIStringInput(selectField).setValue(value.toString());
    UIUnLockManager uiManager = getAncestorOfType(UIUnLockManager.class);
    UIPopupWindow uiPopup = uiManager.getChildById("PermissionPopup");
    uiPopup.setRendered(false);
    uiPopup.setShow(false);
  }
  
  public void update(String nodePath)throws Exception {
    getUIStringInput(PATH_NODE).setValue(nodePath);
    getUIStringInput(PATH_NODE).setEditable(false);    
    getUIStringInput(PERMISSIONS).setValue("");      
  }

  static public class CancelActionListener extends EventListener<UIUnLockForm> {
    public void execute(Event<UIUnLockForm> event) throws Exception {
      UIUnLockForm uiForm = event.getSource();
      UIUnLockManager uiManager = uiForm.getAncestorOfType(UIUnLockManager.class);
      uiManager.removeChildById(UILockList.ST_EDIT);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager);
    }
  }

  static public class SaveActionListener extends EventListener<UIUnLockForm> {
    public void execute(Event<UIUnLockForm> event) throws Exception {
      UIUnLockForm uiUnLockForm = event.getSource();
      UIApplication uiApp = uiUnLockForm.getAncestorOfType(UIApplication.class);
      String nodePath = uiUnLockForm.getUIStringInput(PATH_NODE).getValue();      
      if(!Utils.isNameValid(nodePath, REG_EXPRESSION)) {
        uiApp.addMessage(new ApplicationMessage("UIUnLockForm.msg.name-invalid", null, 
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      UIFormInputSetWithAction permField = uiUnLockForm.getChildById("PermissionButton");
      String permissions = permField.getUIStringInput(PERMISSIONS).getValue();
      if((permissions == null)||(permissions.trim().length() == 0)) {
        uiApp.addMessage(new ApplicationMessage("UIUnLockForm.msg.permission-require", null, 
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }      
      UIUnLockManager uiManager = uiUnLockForm.getAncestorOfType(UIUnLockManager.class);      
      RepositoryService repositoryService = uiUnLockForm.getApplicationComponent(RepositoryService.class);
      ManageableRepository manageRepository = repositoryService.getCurrentRepository();
      Session session = null;
      Node lockedNode = null;
      for(RepositoryEntry repo : repositoryService.getConfig().getRepositoryConfigurations() ) {
        for(WorkspaceEntry ws : repo.getWorkspaceEntries()) {
          session = SessionProviderFactory.createSessionProvider().getSession(ws.getName(), manageRepository);
          try {
            lockedNode = (Node) session.getItem(nodePath);
          } catch (PathNotFoundException e) {
            continue;
          }          
        }
      }
      if (lockedNode != null) {
        if(lockedNode.canAddMixin(Utils.MIX_LOCKABLE)){
          lockedNode.addMixin(Utils.MIX_LOCKABLE);
          lockedNode.save();
        }
        try {
          session = lockedNode.getSession();
          Lock lock = lockedNode.getLock();
          LockUtil.keepLock(lock, permissions, LockUtil.getLockToken(lockedNode));
          session.save();
        } catch(LockException le) {
          le.printStackTrace();
          uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.cant-lock", null, 
              ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          event.getRequestContext().addUIComponentToUpdateByAjax(uiUnLockForm);
          return;
        } catch (Exception e) {
          e.printStackTrace();
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          event.getRequestContext().addUIComponentToUpdateByAjax(uiUnLockForm);        
        }      
        uiManager.getChild(UILockList.class).updateLockedNodesGrid(1);
        uiManager.removeChildById(UILockList.ST_EDIT);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiManager);
      }
    }
  }
  
  static public class AddPermissionActionListener extends EventListener<UIUnLockForm> {
    public void execute(Event<UIUnLockForm> event) throws Exception {
      UIUnLockManager uiManager = event.getSource().getAncestorOfType(UIUnLockManager.class);
      String membership = event.getSource().getUIStringInput(PERMISSIONS).getValue();
      uiManager.initPermissionPopup(membership);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager);
    }
  }
}