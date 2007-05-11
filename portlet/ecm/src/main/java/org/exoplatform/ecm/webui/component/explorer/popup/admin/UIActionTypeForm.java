package org.exoplatform.ecm.webui.component.explorer.popup.admin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.nodetype.NodeType;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.portal.component.view.Util;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 8, 2006
 * 9:41:47 AM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/component/explorer/UIFormWithoutAction.gtmpl",
    events = @EventConfig(listeners = UIActionTypeForm.ChangeActionTypeActionListener.class) 
)
public class UIActionTypeForm extends UIForm {

  final static public String ACTION_TYPE = "actionType" ;
  final static public String CHANGE_ACTION = "ChangeActionType" ;

  private List<SelectItemOption<String>> typeList_ ;

  public String defaultActionType_ ;

  public UIActionTypeForm() throws Exception {
    typeList_ = new ArrayList<SelectItemOption<String>>() ;
    UIFormSelectBox uiSelectBox = new UIFormSelectBox(ACTION_TYPE, ACTION_TYPE, new ArrayList<SelectItemOption<String>>()) ;
    uiSelectBox.setOnChange(CHANGE_ACTION) ;
    addUIFormInput(uiSelectBox) ;
  }

  private Iterator getCreatedActionTypes() throws Exception {
    ActionServiceContainer actionService = getApplicationComponent(ActionServiceContainer.class) ;
    return actionService.getCreatedActionTypes().iterator();
  }

  public void setDefaultActionType() throws Exception{    
    if(defaultActionType_ == null) {
      defaultActionType_ = "exo:sendMailAction" ;
      UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
      UIActionContainer uiActionContainer = getParent() ;
      UIActionForm uiActionForm = uiActionContainer.getChild(UIActionForm.class) ;
      uiActionForm.createNewAction(uiExplorer.getCurrentNode(), defaultActionType_, true) ;
      getUIFormSelectBox(ACTION_TYPE).setValue(defaultActionType_) ;
    }
  }  

  public void update() throws Exception {
    Iterator actions = getCreatedActionTypes(); 
    while(actions.hasNext()){
      String action = ((NodeType) actions.next()).getName();
      typeList_.add(new SelectItemOption<String>(action, action));
    }    
    getUIFormSelectBox(ACTION_TYPE).setOptions(typeList_) ;
    setDefaultActionType() ;
  }

  static public class ChangeActionTypeActionListener extends EventListener<UIActionTypeForm> {
    public void execute(Event<UIActionTypeForm> event) throws Exception {
      UIActionTypeForm uiActionType = event.getSource() ;
      UIJCRExplorer uiExplorer = uiActionType.getAncestorOfType(UIJCRExplorer.class) ;
      String actionType = uiActionType.getUIFormSelectBox(ACTION_TYPE).getValue() ;
      TemplateService templateService = uiActionType.getApplicationComponent(TemplateService.class) ;
      String userName = Util.getPortalRequestContext().getRemoteUser() ;
      if(templateService.getTemplatePathByUser(true, actionType, userName) == null) {
        UIApplication uiApp = uiActionType.getAncestorOfType(UIApplication.class) ;
        Object[] arg = { actionType } ;
        uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.not-support", arg)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        actionType = "exo:sendMailAction" ;
        uiActionType.getUIFormSelectBox(UIActionTypeForm.ACTION_TYPE).setValue(actionType) ;
        UIActionContainer uiActionContainer = uiActionType.getAncestorOfType(UIActionContainer.class) ;
        uiActionContainer.setRenderSibbling(UIActionContainer.class) ;
      }
      UIActionContainer uiActionContainer = uiActionType.getParent() ;
      UIActionForm uiActionForm = uiActionContainer.getChild(UIActionForm.class) ;
      uiActionForm.createNewAction(uiExplorer.getCurrentNode(), actionType, true) ;
      uiActionContainer.setRenderSibbling(UIActionContainer.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionContainer) ;
    }
  }
}
