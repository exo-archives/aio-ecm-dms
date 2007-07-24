/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.nodetype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormTableInputSet;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Sep 22, 2006
 * 2:20:31 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/component/UIFormInputSetWithAction.gtmpl",
    events = {
      @EventConfig(listeners = UINodeTypeOptionList.AddTypeActionListener.class)
    }
)
public class UINodeTypeOptionList extends UIFormInputSetWithAction {

  final static String TABLE_NAME =  "UINodeTypeOptionList"; 
  final static String[] TABLE_COLUMNS = {"label", "input"};
  
  public UINodeTypeOptionList(String name) throws Exception {
    super(name) ;
    setComponentConfig(getClass(), null) ;
  }  
  
  public void processRender(WebuiRequestContext context) throws Exception {
    super.processRender(context) ;
  }
  
  private boolean getCheckedValue(String values, String name) {
    String[] selectNodes = values.split(",") ;
    for(int i = 0 ; i < selectNodes.length ; i ++ ) {
      if(selectNodes[i].equals(name)) return true ;
    }
    return false ;
  }
  
  @SuppressWarnings("unchecked")
  public void update(String values) throws Exception {
    UIFormTableInputSet uiTableInputSet = createUIComponent(UIFormTableInputSet.class, null, null) ;
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    NodeTypeManager ntManager = getApplicationComponent(RepositoryService.class)
                                .getRepository(repository).getNodeTypeManager() ;
    NodeTypeIterator nodeTypeIter = ntManager.getAllNodeTypes() ;
    List<NodeType> nodeTypeList = new ArrayList<NodeType>() ;
    while(nodeTypeIter.hasNext()) {
      NodeType nt = nodeTypeIter.nextNodeType() ;
      nodeTypeList.add(nt) ;
    }
    Collections.sort(nodeTypeList, new Utils.NodeTypeNameComparator()) ;
    UIFormInputSet uiInputSet ;
    uiTableInputSet.setName(TABLE_NAME);
    uiTableInputSet.setColumns(TABLE_COLUMNS);
    for(NodeType nt : nodeTypeList) {
      String ntName = nt.getName() ;
      uiInputSet = new UIFormInputSet(ntName) ;
      UIFormInputInfo uiInfo = new UIFormInputInfo("label", null, ntName);
      uiInputSet.addChild(uiInfo);
      UIFormCheckBoxInput<String> uiCheckbox = new UIFormCheckBoxInput<String>(ntName, ntName, null);
      if(values != null) {
        if(getCheckedValue(values, ntName)) uiCheckbox.setChecked(true) ;
      } else {
        uiCheckbox.setChecked(false); 
      }
      uiCheckbox.setValue(ntName);
      uiInputSet.addChild(uiCheckbox);
      uiTableInputSet.addChild(uiInputSet);      
    }
    addUIFormInput(uiTableInputSet) ;
  }
  
  
  
  private void setFieldValues(String fieldName, List<String> selectedNodes) throws Exception {
    String strNodeList = null ;
    UINodeTypeForm uiNodeTypeForm = getParent() ;
    for(int i = 0 ; i < selectedNodes.size() ; i++) {
      if(strNodeList == null) strNodeList = selectedNodes.get(i) ;
      else strNodeList = strNodeList + "," + selectedNodes.get(i) ;
    }
    uiNodeTypeForm.getUIStringInput(fieldName).setValue(strNodeList) ;
    if(fieldName.equals(UINodeTypeForm.SUPER_TYPE)) {
      for(UIComponent uiComp : uiNodeTypeForm.getChildren()) {
        UIFormInputSetWithAction tab = uiNodeTypeForm.getChildById(uiComp.getId()) ;
        if(tab.getId().equals(UINodeTypeForm.NODETYPE_DEFINITION)) tab.setRendered(true) ;
        else tab.setRendered(false) ;
      }
      uiNodeTypeForm.removeChildById(UINodeTypeForm.SUPER_TYPE_TAB) ;
    } else if(fieldName.equals(UIChildNodeDefinitionForm.REQUIRED_PRIMARY_TYPE)) { 
      for(UIComponent uiComp : uiNodeTypeForm.getChildren()) {
        UIFormInputSetWithAction tab = uiNodeTypeForm.getChildById(uiComp.getId()) ;
        if(tab.getId().equals(UINodeTypeForm.CHILDNODE_DEFINITION)) tab.setRendered(true) ;
        else tab.setRendered(false) ;
      }
      uiNodeTypeForm.removeChildById(UINodeTypeForm.REQUIRED_PRIMARY_TYPE_TAB) ;
    } else if(fieldName.equals(UIChildNodeDefinitionForm.DEFAULT_PRIMARY_TYPE)) {
      for(UIComponent uiComp : uiNodeTypeForm.getChildren()) {
        UIFormInputSetWithAction tab = uiNodeTypeForm.getChildById(uiComp.getId()) ;
        if(tab.getId().equals(UINodeTypeForm.CHILDNODE_DEFINITION)) tab.setRendered(true) ;
        else tab.setRendered(false) ;
      }
      uiNodeTypeForm.removeChildById(UINodeTypeForm.DEFAULT_PRIMARY_TYPE_TAB) ;
    }
  }

  static public class AddTypeActionListener extends EventListener<UINodeTypeForm> {
    public void execute(Event<UINodeTypeForm> event) throws Exception {
      UINodeTypeForm uiForm= event.getSource() ;
      UINodeTypeOptionList uiOptionList = uiForm.getChild(UINodeTypeOptionList.class) ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      List<String> selectedNodes = new ArrayList<String>() ;
      UINodeTypeManager uiManager = uiForm.getAncestorOfType(UINodeTypeManager.class) ;
      List<UIFormCheckBoxInput> listCheckbox =  new ArrayList<UIFormCheckBoxInput>();
      uiForm.findComponentOfType(listCheckbox, UIFormCheckBoxInput.class);
      int count = 0 ;
      for(int i = 0; i < listCheckbox.size(); i ++) {
        if(listCheckbox.get(i).isChecked()) {
          selectedNodes.add(listCheckbox.get(i).getName()) ;
          count ++ ;
        }
      }
      if(fieldName.equals(UIChildNodeDefinitionForm.DEFAULT_PRIMARY_TYPE) && count > 1) {
        uiApp.addMessage(new ApplicationMessage("UINodeTypeForm.msg.values-error", null)) ;
        uiForm.setTabRender(UINodeTypeForm.DEFAULT_PRIMARY_TYPE_TAB) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;        
      }
      uiOptionList.setFieldValues(fieldName, selectedNodes) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  static public class CancelTabActionListener extends EventListener<UINodeTypeForm> {
    public void execute(Event<UINodeTypeForm> event) throws Exception {
      UINodeTypeForm uiForm= event.getSource() ;
      String tabName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      if(tabName.equals(UINodeTypeForm.SUPER_TYPE_TAB)) {
        uiForm.removeChildById(tabName) ;
        uiForm.setTabRender(UINodeTypeForm.NODETYPE_DEFINITION) ;
      } else if(tabName.equals(UINodeTypeForm.DEFAULT_PRIMARY_TYPE_TAB) ||
          tabName.equals(UINodeTypeForm.REQUIRED_PRIMARY_TYPE_TAB)) {
        uiForm.removeChildById(tabName) ;
        uiForm.setTabRender(UINodeTypeForm.CHILDNODE_DEFINITION) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
}