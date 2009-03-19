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
package org.exoplatform.ecm.webui.component.explorer.symlink;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.selectone.UIOneNodePathSelector;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Chien Nguyen Van
 * 
 */

@ComponentConfigs(
    {
      @ComponentConfig(
          lifecycle = UIFormLifecycle.class,
          template =  "system:/groovy/webui/form/UIForm.gtmpl",
          events = {
            @EventConfig(listeners = UISymLinkForm.SaveActionListener.class), 
            @EventConfig(listeners = UISymLinkForm.CancelActionListener.class, phase = Phase.DECODE)
          }
      ),
      @ComponentConfig(
          type = UIFormMultiValueInputSet.class,
          id="SymLinkMultipleInputset",
          events = {
            @EventConfig(listeners = UISymLinkForm.RemoveActionListener.class, phase = Phase.DECODE),
            @EventConfig(listeners = UISymLinkForm.AddActionListener.class, phase = Phase.DECODE) 
          }
      )
    }
)

    

public class UISymLinkForm extends UIForm implements UIPopupComponent, UISelectable {

  final static public String FIELD_NAME = "symLinkName";
  final static public String FIELD_PATH = "pathNode";
  final static public String FIELD_SYMLINK = "fieldPathNode";
  final static public String POPUP_SYMLINK = "UIPopupSymLink";
  final static private String SYMLINK = "exo:symlink";
  
  public UISymLinkForm() throws Exception {
    addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null).addValidator(MandatoryValidator.class));
  }
  
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
  public void initFieldInput() throws Exception {
    UIFormMultiValueInputSet uiFormMultiValue = createUIComponent(UIFormMultiValueInputSet.class, "SymLinkMultipleInputset", null);
    uiFormMultiValue.setId(FIELD_PATH);
    uiFormMultiValue.setName(FIELD_PATH);
    uiFormMultiValue.setEditable(false);
    uiFormMultiValue.setType(UIFormStringInput.class);
    addUIFormInput(uiFormMultiValue);
  }
    
  @SuppressWarnings("unused")
  public void doSelect(String selectField, Object value) throws Exception {
    String valueNodeName = String.valueOf(value).trim();
    List<String> listNodeName = new ArrayList<String>();
    listNodeName.add(valueNodeName);
    UIFormMultiValueInputSet uiFormMultiValueInputSet = getChild(UIFormMultiValueInputSet.class);
    uiFormMultiValueInputSet.setValue(listNodeName);
    getUIStringInput(FIELD_NAME).setValue(valueNodeName.substring(valueNodeName.lastIndexOf("/") + 1));
    UISymLinkManager uiSymLinkManager = getParent();
    uiSymLinkManager.removeChildById(POPUP_SYMLINK);
  }
  
  static  public class SaveActionListener extends EventListener<UISymLinkForm> {
    public void execute(Event<UISymLinkForm> event) throws Exception {
      UISymLinkForm uiSymLinkForm = event.getSource();
      UIJCRExplorer uiExplorer = uiSymLinkForm.getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = uiSymLinkForm.getAncestorOfType(UIApplication.class);
      String symLinkName = uiSymLinkForm.getUIStringInput(FIELD_NAME).getValue();
      
      String pathNode = "";
      UIFormMultiValueInputSet uiSet = uiSymLinkForm.getChild(UIFormMultiValueInputSet.class);
      List<UIComponent> listChildren = uiSet.getChildren();         
      for (UIComponent component : listChildren) {
        UIFormStringInput uiStringInput = (UIFormStringInput)component;          
        if(uiStringInput.getValue() != null) {
          pathNode = uiStringInput.getValue().trim();
        }
      }
      
      Node node = uiExplorer.getCurrentNode() ;                  
      if(uiExplorer.nodeIsLocked(node)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(symLinkName == null || symLinkName.length() ==0) {
        uiApp.addMessage(new ApplicationMessage("UISymLinkForm.msg.name-invalid", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(pathNode == null || pathNode.length() ==0) {
        uiApp.addMessage(new ApplicationMessage("UISymLinkForm.msg.path-node-invalid", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return ;
      }
      String[] arrFilterChar = {"&", "$", "@", ":", "]", "[", "*", "%", "!", "+", "(", ")", "'", "#", ";", "}", "{", "/", "|", "\""};
      for(String filterChar : arrFilterChar) {
        if(symLinkName.indexOf(filterChar) > -1) {
          uiApp.addMessage(new ApplicationMessage("UISymLinkForm.msg.name-not-allowed", null, 
              ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
      }
      
      Session userSession = node.getSession();
      try {
        userSession.getItem(pathNode);
      } catch (ItemNotFoundException e) {
        uiApp.addMessage(new ApplicationMessage("UISymLinkForm.msg.non-node", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (RepositoryException re) {
        uiApp.addMessage(new ApplicationMessage("UISymLinkForm.msg.non-node", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch(Exception e) {
        e.printStackTrace();
        uiApp.addMessage(new ApplicationMessage("UISymLinkForm.msg.non-node", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      try {        
        Node targetNode = (Node) userSession.getItem(pathNode);
        LinkManager linkManager = uiSymLinkForm.getApplicationComponent(LinkManager.class);
        linkManager.createLink(node, SYMLINK, targetNode, symLinkName);
        uiExplorer.updateAjax(event);
      } catch (AccessControlException ace) {
        throw new AccessDeniedException(ace.getMessage());
      } catch(NumberFormatException nume) {
        String key = "UISymLinkForm.msg.numberformat-exception";
        uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch(Exception e) {
        e.printStackTrace();
        String key = "UISymLinkForm.msg.cannot-save";
        uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
    }
  }

  static  public class CancelActionListener extends EventListener<UISymLinkForm> {
    public void execute(Event<UISymLinkForm> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      uiExplorer.cancelAction();
    }
  }
  
  static  public class RemoveActionListener extends EventListener<UIFormMultiValueInputSet> {
    public void execute(Event<UIFormMultiValueInputSet> event) throws Exception {
      UIFormMultiValueInputSet uiSet = event.getSource();
      UIComponent uiComponent = uiSet.getParent();
      if (uiComponent instanceof UISymLinkForm) {
        UISymLinkForm uiSymLinkForm = (UISymLinkForm)uiComponent;
        String id = event.getRequestContext().getRequestParameter(OBJECTID);
        uiSet.removeChildById(id);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiSymLinkForm);
      }
    }
  }
  
  static  public class AddActionListener extends EventListener<UIFormMultiValueInputSet> {
    public void execute(Event<UIFormMultiValueInputSet> event) throws Exception {
      UIFormMultiValueInputSet uiSet = event.getSource();
      UISymLinkForm uiSymLinkForm =  (UISymLinkForm) uiSet.getParent();
      UISymLinkManager uiSymLinkManager = uiSymLinkForm.getParent();
      UIJCRExplorer uiExplorer = uiSymLinkForm.getAncestorOfType(UIJCRExplorer.class);
      String workspaceName = uiExplorer.getCurrentWorkspace();
      
      UIPopupWindow uiPopupWindow = uiSymLinkManager.initPopupTaxonomy(POPUP_SYMLINK);
      UIOneNodePathSelector uiNodePathSelector = uiSymLinkManager.createUIComponent(UIOneNodePathSelector.class, null, null);
      uiPopupWindow.setUIComponent(uiNodePathSelector);
      uiNodePathSelector.setIsDisable(workspaceName, false);
      uiNodePathSelector.setRootNodeLocation(uiExplorer.getRepositoryName(), workspaceName, "/");
      uiNodePathSelector.setIsShowSystem(false);
      uiNodePathSelector.init(uiExplorer.getSystemProvider());
      String param = "returnField=" + FIELD_SYMLINK;
      uiNodePathSelector.setSourceComponent(uiSymLinkForm, new String[]{param});
      uiPopupWindow.setRendered(true);
      uiPopupWindow.setShow(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSymLinkManager);
    }
  }
}
