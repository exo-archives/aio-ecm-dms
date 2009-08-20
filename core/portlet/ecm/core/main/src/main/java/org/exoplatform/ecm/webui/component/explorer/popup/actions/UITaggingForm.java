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
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.ecm.jcr.model.Preference;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UISideBar;
import org.exoplatform.ecm.webui.form.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.services.cms.folksonomy.FolksonomyService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jan 12, 2007  
 * 11:56:51 AM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UITaggingForm.AddTagActionListener.class),
      @EventConfig(listeners = UITaggingForm.RemoveActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UITaggingForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UITaggingForm extends UIForm implements UIPopupComponent {
  
  final static public String TAG_NAMES = "names";
  final static public String LINKED_TAGS = "linked";
  final static public String LINKED_TAGS_SET = "tagSet";
  final static public String TAG_STATUS_PROP = "exo:tagStatus".intern();  
  final static public String TAG_NAME_ACTION = "tagNameAct".intern();
  final static public String ASCENDING_ORDER = "Ascending".intern();
  
  public UITaggingForm() throws Exception {
    UIFormInputSetWithAction uiInputSet = new UIFormInputSetWithAction(LINKED_TAGS_SET);
    uiInputSet.addUIFormInput(new UIFormStringInput(TAG_NAMES, TAG_NAMES, null));
    uiInputSet.addUIFormInput(new UIFormInputInfo(LINKED_TAGS, LINKED_TAGS, null));
    uiInputSet.setIntroduction(TAG_NAMES, "UITaggingForm.introduction.tagName");
    addUIComponentInput(uiInputSet);
    uiInputSet.setIsView(false);
    super.setActions(new String[] {"AddTag", "Cancel"});
  }
  
  public void activate() throws Exception {
    String repository = getAncestorOfType(UIJCRExplorer.class).getRepositoryName();
    FolksonomyService folksonomyService = getApplicationComponent(FolksonomyService.class);
    StringBuilder linkedTags = new StringBuilder();
    Node currentNode = getAncestorOfType(UIJCRExplorer.class).getCurrentNode();
    for(Node tag : folksonomyService.getLinkedTagsOfDocument(currentNode, repository)) {
      if(linkedTags.length() > 0) linkedTags = linkedTags.append(",");
      linkedTags.append(tag.getName());
    }
    UIFormInputSetWithAction uiLinkedInput = getChildById(LINKED_TAGS_SET);
    uiLinkedInput.setInfoField(LINKED_TAGS, linkedTags.toString());
    uiLinkedInput.setActionInfo(LINKED_TAGS, new String[] { "Remove" });
    uiLinkedInput.setIsShowOnly(true);
    uiLinkedInput.setIsDeleteOnly(true);
    
  }
  public void deActivate() throws Exception {}

  static public class AddTagActionListener extends EventListener<UITaggingForm> {
    public void execute(Event<UITaggingForm> event) throws Exception {
      UITaggingForm uiForm = event.getSource();
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class);
      String repository = uiForm.getAncestorOfType(UIJCRExplorer.class).getRepositoryName();
      String tagName = uiForm.getUIStringInput(TAG_NAMES).getValue();
      FolksonomyService folksonomyService = uiForm.getApplicationComponent(FolksonomyService.class);
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class);
      Node currentNode = uiExplorer.getCurrentNode();
      uiExplorer.addLockToken(currentNode);
      if(tagName == null || tagName.trim().length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UITaggingForm.msg.tag-name-empty", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      String[] tagNames = null;
      if (tagName.indexOf(",") > -1) {
        tagNames = tagName.split(",");
        List<String> listTagNames = new ArrayList<String>(tagNames.length);
        List<String> listTagNamesClone = new ArrayList<String>(tagNames.length);
        for (String tName : tagNames) {
          listTagNames.add(tName.trim());          
          listTagNamesClone.add(tName.trim());
        }        
        for (int i = 0; i < listTagNames.size(); i++) {          
          String tag = listTagNames.get(i);
          listTagNamesClone.remove(tag);
          if (listTagNamesClone.contains(tag)) {
            uiApp.addMessage(new ApplicationMessage("UITaggingForm.msg.tag-name-duplicate", null, 
                ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
          listTagNamesClone.add(tag);          
        }
      }
      else tagNames = new String[] {tagName};
      String[] fitlerTagNames = new String[tagNames.length];
      int i = 0;
      for(String t : tagNames) {
        fitlerTagNames[i] = tagNames[i].trim();
        i++;
        if(t.trim().length() == 0) {
          uiApp.addMessage(new ApplicationMessage("UITaggingForm.msg.tag-name-empty", null, 
              ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
        if(t.trim().length() > 20) {
          uiApp.addMessage(new ApplicationMessage("UITaggingForm.msg.tagName-too-long", null, 
                                                  ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
        String[] arrFilterChar = {"&", "'", "$", "@", ":","]", "[", "*", "%", "!", "/", "\\"};
        for(String filterChar : arrFilterChar) {
          if(t.indexOf(filterChar) > -1) {
            uiApp.addMessage(new ApplicationMessage("UITaggingForm.msg.tagName-invalid", null, 
                                                    ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
        }
      }
      for(Node tag : folksonomyService.getLinkedTagsOfDocument(uiExplorer.getCurrentNode(), repository)) {
        for(String t : fitlerTagNames) {
          if(t.equals(tag.getName())) {
            Object[] args = {t};
            uiApp.addMessage(new ApplicationMessage("UITaggingForm.msg.name-exist", args, 
                                                    ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
        }
      }
      folksonomyService.addTag(currentNode, fitlerTagNames, repository);
      uiForm.activate();
      
      Preference preferences = uiExplorer.getPreference();
      if (preferences.isShowSideBar()) {
        UISideBar uiSideBar = uiExplorer.findFirstComponentOfType(UISideBar.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiSideBar);
      }
      uiForm.getUIStringInput(TAG_NAMES).setValue(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }
  
  static public class CancelActionListener extends EventListener<UITaggingForm> {
    public void execute(Event<UITaggingForm> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      uiExplorer.cancelAction();
    }
  }

  static public class RemoveActionListener extends EventListener<UITaggingForm> {
    public void execute(Event<UITaggingForm> event) throws Exception {
      UITaggingForm uiForm = event.getSource();
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class);
      Node currentNode = uiExplorer.getCurrentNode();
      String tagName = event.getRequestContext().getRequestParameter(OBJECTID);
      String[] arrFilterChar = { "&", "'", "$", "@", ":", "]", "[", "*", "%", "!", "/", "\\" };
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class);
      if (!Utils.isNameValid(tagName, arrFilterChar)) {
        uiApp.addMessage(new ApplicationMessage("UITaggingForm.msg.tagName-invalid", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      FolksonomyService folksonomyService = uiForm.getApplicationComponent(FolksonomyService.class);
      folksonomyService.removeTagOfDocument(currentNode, tagName, uiExplorer.getRepositoryName());
      uiForm.activate();

      Preference preferences = uiExplorer.getPreference();
      if (preferences.isShowSideBar()) {
        UISideBar uiSideBar = uiExplorer.findFirstComponentOfType(UISideBar.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiSideBar);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }
}
