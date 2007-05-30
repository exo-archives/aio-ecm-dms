/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import javax.jcr.Node;

import org.exoplatform.ecm.jcr.ECMNameValidator;
import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.cms.folksonomy.FolksonomyService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormInputInfo;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jan 12, 2007  
 * 11:56:51 AM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/component/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UITaggingForm.AddTagActionListener.class),
      @EventConfig(listeners = UITaggingForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UITaggingForm extends UIForm implements UIPopupComponent {
  
  final static public String TAG_NAMES = "names" ;
  final static public String LINKED_TAGS = "linked" ;
  final static public String LINKED_TAGS_SET = "tagSet" ;
  final static public String TAG_STATUS_PROP = "exo:tagStatus".intern() ;  
  final static public String ASCENDING_ORDER = "Ascending".intern();
  
  public UITaggingForm() throws Exception {
    addUIFormInput(new UIFormStringInput(TAG_NAMES, TAG_NAMES, null).addValidator(ECMNameValidator.class)) ;
    UIFormInputSetWithAction uiInputSet = new UIFormInputSetWithAction(LINKED_TAGS_SET) ;
    uiInputSet.addUIFormInput(new UIFormInputInfo(LINKED_TAGS, LINKED_TAGS, null)) ;
    addUIComponentInput(uiInputSet) ;
    uiInputSet.setIsView(false) ;
  }
  
  public void activate() throws Exception {
    FolksonomyService folksonomyService = getApplicationComponent(FolksonomyService.class) ;
    StringBuilder linkedTags = new StringBuilder() ;
    Node currentNode = getAncestorOfType(UIJCRExplorer.class).getCurrentNode() ;
    for(Node tag : folksonomyService.getLinkedTagsOfDocument(currentNode)) {
      if(linkedTags.length() > 0) linkedTags = linkedTags.append(",") ;
      linkedTags.append(tag.getName()) ;
    }
    UIFormInputSetWithAction uiLinkedInput = getChildById(LINKED_TAGS_SET) ;
    uiLinkedInput.setInfoField(LINKED_TAGS, linkedTags.toString()) ;
    uiLinkedInput.setIsShowOnly(true) ;
  }
  public void deActivate() throws Exception {}

  static public class AddTagActionListener extends EventListener<UITaggingForm> {
    public void execute(Event<UITaggingForm> event) throws Exception {
      UITaggingForm uiForm = event.getSource() ;
      String tagName = uiForm.getUIStringInput(TAG_NAMES).getValue() ;
      FolksonomyService folksonomyService = uiForm.getApplicationComponent(FolksonomyService.class) ;
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      String[] tagNames = null ;
      if(tagName.indexOf(";") > -1) tagNames = tagName.split(";") ;
      else tagNames = new String[] {tagName} ;
      for(Node tag : folksonomyService.getLinkedTagsOfDocument(uiExplorer.getCurrentNode())) {
        for(String t : tagNames) {
          if(t.equals(tag.getName())) {
            uiApp.addMessage(new ApplicationMessage("UITaggingForm.msg.name-exist", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
        }
      }
      folksonomyService.addTag(uiExplorer.getCurrentNode(), tagNames) ;
      uiForm.activate() ;
      uiForm.getUIStringInput(TAG_NAMES).setValue(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }
  
  static public class CancelActionListener extends EventListener<UITaggingForm> {
    public void execute(Event<UITaggingForm> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;
    }
  }
}
