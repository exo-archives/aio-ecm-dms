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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.upload.UIUploadForm;
import org.exoplatform.ecm.webui.component.explorer.upload.UIUploadManager;
import org.exoplatform.ecm.webui.form.validator.ECMNameValidator;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.selectone.UIOneNodePathSelector;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
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
  
  public UISymLinkForm() throws Exception {
    addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null).addValidator(ECMNameValidator.class));
  }
  
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
  public void initFieldInput() throws Exception {
    UIFormMultiValueInputSet uiFormMultiValue = createUIComponent(UIFormMultiValueInputSet.class, "SymLinkMultipleInputset", null);
    uiFormMultiValue.setId(FIELD_PATH);
    uiFormMultiValue.setName(FIELD_PATH);
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
    
    UISymLinkManager uiSymLinkManager = getParent();
    uiSymLinkManager.removeChildById(POPUP_SYMLINK);
  }
  
  static  public class SaveActionListener extends EventListener<UISymLinkForm> {
    public void execute(Event<UISymLinkForm> event) throws Exception {
      UISymLinkForm uiSymLinkForm = event.getSource();
      UIJCRExplorer uiExplorer = uiSymLinkForm.getAncestorOfType(UIJCRExplorer.class) ;
      UIApplication uiApp = uiSymLinkForm.getAncestorOfType(UIApplication.class);
      String name = uiSymLinkForm.getUIStringInput(FIELD_NAME).getValue();
      Node node = uiExplorer.getCurrentNode() ;                  
      if(uiExplorer.nodeIsLocked(node)) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }      
      if(name == null || name.length() ==0) {
        uiApp.addMessage(new ApplicationMessage("UIFolderForm.msg.name-invalid", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }      
      String[] arrFilterChar = {"&", "$", "@", ":", "]", "[", "*", "%", "!", "+", "(", ")", "'", "#", ";", "}", "{", "/", "|", "\""};
      for(String filterChar : arrFilterChar) {
        if(name.indexOf(filterChar) > -1) {
          uiApp.addMessage(new ApplicationMessage("UIFolderForm.msg.name-not-allowed", null, 
              ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
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
        UIFormStringInput uiFormStringInput = uiSet.getChildById(id);
        String value = uiFormStringInput.getValue().trim();
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
      NodeHierarchyCreator nodeHierarchyCreator = uiSymLinkForm.getApplicationComponent(NodeHierarchyCreator.class);
      String repository = uiExplorer.getRepositoryName();
      ManageableRepository manaRepository = 
        uiSymLinkForm.getApplicationComponent(RepositoryService.class).getRepository(repository);
      String workspaceName = manaRepository.getConfiguration().getSystemWorkspaceName();
      
      UIPopupWindow uiPopupWindow = uiSymLinkManager.initPopupTaxonomy(POPUP_SYMLINK);
      UIOneNodePathSelector uiNodePathSelector = uiSymLinkManager.createUIComponent(UIOneNodePathSelector.class, null, null);
      uiPopupWindow.setUIComponent(uiNodePathSelector);
      uiNodePathSelector.setIsDisable(workspaceName, true);
      uiNodePathSelector.setRootNodeLocation(repository, workspaceName, 
          nodeHierarchyCreator.getJcrPath(BasePath.EXO_TAXONOMIES_PATH));
      uiNodePathSelector.init(uiExplorer.getSystemProvider());
      String param = "returnField=" + FIELD_SYMLINK;
      uiNodePathSelector.setSourceComponent(uiSymLinkForm, new String[]{param});
      uiPopupWindow.setRendered(true);
      uiPopupWindow.setShow(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSymLinkManager);
    }
  }
}
