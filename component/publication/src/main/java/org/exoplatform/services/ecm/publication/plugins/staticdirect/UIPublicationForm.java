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
package org.exoplatform.services.ecm.publication.plugins.staticdirect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.jcr.Node;
import javax.jcr.Value;

import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.ecm.publication.plugins.webui.VersionNode;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormRadioBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jun 26, 2008 9:24:30 AM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "classpath:resources/templates/staticdirect/UIPublicationForm.gtmpl",
    events = {
      @EventConfig(listeners = UIPublicationForm.SaveActionListener.class),
      @EventConfig(listeners = UIPublicationForm.CancelActionListener.class)
    }
)
public class UIPublicationForm extends UIForm {
  
  final static public String VISIBILITY = "visibility".intern();
  final static public String STATE = "state".intern();
  
  private VersionNode curentVersion_;
  private VersionNode rootVersion_;
  private Node currentNode_;
  private String visibility_;
  private String state_;
  
  public UIPublicationForm() throws Exception {
  }
  
  public void updateForm(VersionNode versionNode) throws Exception {
    String state = getStateByVersion(versionNode);
    resetCurrentState(state, visibility_);
  }
  
  public void resetCurrentState(String state, String visibility) throws Exception {
    RequestContext context = RequestContext.getCurrentInstance();
    ResourceBundle res = context.getApplicationResourceBundle();
    String published = res.getString("UIPublicationForm.label.published");
    String non_published = res.getString("UIPublicationForm.label.non-published");
    String lblPublic = res.getString("UIPublicationForm.label.public");
    String lblPrivate = res.getString("UIPublicationForm.label.private");
    removeChildById(VISIBILITY);
    removeChildById(STATE);
    List<SelectItemOption<String>> visibilityOptions = new ArrayList<SelectItemOption<String>>();
    visibilityOptions.add(new SelectItemOption<String>(lblPublic, StaticAndDirectPublicationPlugin.PUBLIC));
    visibilityOptions.add(new SelectItemOption<String>(lblPrivate, StaticAndDirectPublicationPlugin.PRIVATE));
    addUIFormInput(new UIFormRadioBoxInput(VISIBILITY, visibility, visibilityOptions).
        setAlign(UIFormRadioBoxInput.HORIZONTAL_ALIGN));
    
    List<SelectItemOption<String>> stateOptions = new ArrayList<SelectItemOption<String>>();
    stateOptions.add(new SelectItemOption<String>(published, StaticAndDirectPublicationPlugin.PUBLISHED));
    stateOptions.add(new SelectItemOption<String>(non_published, StaticAndDirectPublicationPlugin.NON_PUBLISHED));
    addUIFormInput(new UIFormRadioBoxInput(STATE, state, stateOptions).
        setAlign(UIFormRadioBoxInput.HORIZONTAL_ALIGN));
  }
  
  public String getStateByVersion(VersionNode versionNode) throws Exception {
    Value[] publicationStates =  currentNode_.getProperty(StaticAndDirectPublicationPlugin.VERSIONS_PUBLICATION_STATES).getValues();
    for(Value value : publicationStates) {
      String[] arrPublicationState = value.getString().split(",");
      for(int i=0; i < arrPublicationState.length; i++) {
        if(arrPublicationState[0].equals(versionNode.getVersion().getUUID())) {
          return arrPublicationState[1];
        }
      }
    }
    return StaticAndDirectPublicationPlugin.DEFAULT_STATE;
  }
  
  public void initForm(Node currentNode) throws Exception {
    currentNode_ = currentNode;   
    rootVersion_ = new VersionNode(currentNode_.getVersionHistory().getRootVersion());
    curentVersion_ = new VersionNode(currentNode_.getBaseVersion());
    visibility_ = currentNode_.getProperty(StaticAndDirectPublicationPlugin.VISIBILITY).getString();
    
    state_ = getStateByVersion(curentVersion_);
    resetCurrentState(state_, visibility_);
  }
  
  public void setVersionNode(VersionNode versionNode) {
    curentVersion_ = versionNode;
  }
  
  static public class SaveActionListener extends EventListener<UIPublicationForm> {
    public void execute(Event<UIPublicationForm> event) throws Exception {
      UIPublicationForm uiForm = event.getSource();
      String visibility = uiForm.<UIFormRadioBoxInput>getUIInput(VISIBILITY).getValue();
      String state = uiForm.<UIFormRadioBoxInput>getUIInput(STATE).getValue();
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class);
      if(!uiForm.currentNode_.isCheckedOut()) {        
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      PublicationService publicationService = uiForm.getApplicationComponent(PublicationService.class);
      HashMap<String,String> context = new HashMap<String,String>();
      context.put("nodeVersionUUID", uiForm.curentVersion_.getVersion().getUUID());
      context.put("visibility", visibility);
      publicationService.changeState(uiForm.currentNode_, state, context);
      UIPopupWindow uiPopup = uiForm.getAncestorOfType(UIPopupWindow.class);
      uiPopup.setRendered(false);
      uiPopup.setShow(false);
    }
  }
  
  static public class CancelActionListener extends EventListener<UIPublicationForm> {
    public void execute(Event<UIPublicationForm> event) throws Exception {
      UIPublicationForm uiForm = event.getSource();
      UIPopupWindow uiPopup = uiForm.getAncestorOfType(UIPopupWindow.class);
      uiPopup.setRendered(false);
      uiPopup.setShow(false);
    }
  }
}
