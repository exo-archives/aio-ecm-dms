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
package org.exoplatform.ecm.webui.component.explorer.publication;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.jcr.Node;

import org.exoplatform.ecm.jcr.model.VersionNode;
import org.exoplatform.ecm.webui.component.admin.drives.UIDriveInputSet;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.ecm.publication.plugins.staticdirect.StaticAndDirectPublicationPlugin;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
    template = "app:/groovy/webui/component/explorer/publication/UIPublicationForm.gtmpl",
    events = {
      @EventConfig(listeners = UIPublicationForm.SaveActionListener.class),
      @EventConfig(listeners = UIPublicationForm.CancelActionListener.class)
    }
)
public class UIPublicationForm extends UIForm {
  
  final static public String VISIBILITY = "visibility" ;
  final static public String STATE = "state" ;
  
  private VersionNode curentVersion_ ;
  private VersionNode rootVersion_ ;
  private Node currentNode_ ;
  
  public UIPublicationForm() throws Exception {
    RequestContext context = RequestContext.getCurrentInstance() ;
    ResourceBundle res = context.getApplicationResourceBundle() ;
    String published = res.getString("UIPublicationForm.label.published") ;
    String non_published = res.getString("UIPublicationForm.label.non-published") ;
    String lblPublic = res.getString("UIPublicationForm.label.public") ;
    String lblPrivate = res.getString("UIPublicationForm.label.private") ;
    
    List<SelectItemOption<String>> visibilityOptions = new ArrayList<SelectItemOption<String>>() ;
    visibilityOptions.add(new SelectItemOption<String>(lblPublic, lblPublic)) ;
    visibilityOptions.add(new SelectItemOption<String>(lblPrivate, lblPrivate)) ;
    addUIFormInput(new UIFormRadioBoxInput(VISIBILITY, VISIBILITY, visibilityOptions).
        setAlign(UIFormRadioBoxInput.HORIZONTAL_ALIGN)) ;
    
    List<SelectItemOption<String>> stateOptions = new ArrayList<SelectItemOption<String>>() ;
    stateOptions.add(new SelectItemOption<String>(published, published)) ;
    stateOptions.add(new SelectItemOption<String>(non_published, non_published)) ;
    addUIFormInput(new UIFormRadioBoxInput(STATE, STATE, stateOptions).
        setAlign(UIFormRadioBoxInput.HORIZONTAL_ALIGN)) ;
  }
  
  public void initForm(Node currentNode) throws Exception {
    currentNode_ = currentNode;   
    rootVersion_ = new VersionNode(currentNode_.getVersionHistory().getRootVersion());
    curentVersion_ = rootVersion_;
    String currentState = 
      currentNode_.getProperty(StaticAndDirectPublicationPlugin.VISIBILITY).getString() ;
    ((UIFormRadioBoxInput)getUIInput(VISIBILITY)).setValue(currentState) ;
  }
  
  public void setVersionNode(VersionNode versionNode) {
    curentVersion_ = versionNode ;
  }
  
  public void setCurrentState() {
    
  }
  
  static public class SaveActionListener extends EventListener<UIPublicationForm> {
    public void execute(Event<UIPublicationForm> event) throws Exception {
      UIPublicationForm uiForm = event.getSource() ;
      String visibility = uiForm.<UIFormRadioBoxInput>getUIInput(VISIBILITY).getValue() ;
    }
  }
  
  static public class CancelActionListener extends EventListener<UIPublicationForm> {
    public void execute(Event<UIPublicationForm> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction();
    }
  }
  
}
