/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.nodetype;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValue;
import org.exoplatform.webui.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormCheckBoxInput;
import org.exoplatform.webui.component.UIFormInputInfo;
import org.exoplatform.webui.component.UIFormInputSet;
import org.exoplatform.webui.component.UIFormTableInputSet;
import org.exoplatform.webui.component.UIPopupWindow;
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
 * Sep 29, 2006
 * 12:02:38 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl",
    events = {
      @EventConfig(listeners = UINodeTypeImport.ImportActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UINodeTypeImport.UploadActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UINodeTypeImport.CancelActionListener.class)
    }
)
public class UINodeTypeImport extends UIForm {
  
  private List<NodeType> nodeTypeList_ = new ArrayList<NodeType>() ;
  final static String TABLE_NAME =  "UINodeTypeImport"; 
  final static String [] TABLE_COLUMNS = {"label", "input"};
  
  public UINodeTypeImport() throws Exception {
  }
  
  public void update (List<NodeType> nodeTypeList) throws Exception {
    UIFormTableInputSet uiTableInputSet = getChild(UIFormTableInputSet.class) ;
    if(uiTableInputSet == null ) {
      uiTableInputSet = createUIComponent(UIFormTableInputSet.class, null, null) ;
      addUIComponentInput(uiTableInputSet) ;
    } else {
      uiTableInputSet.getChildren().clear() ;
    }
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    NodeTypeManager ntManager = repositoryService.getRepository().getNodeTypeManager() ;
    UIFormInputSet uiInputSet ;
    uiTableInputSet.setName(TABLE_NAME);
    uiTableInputSet.setColumns(TABLE_COLUMNS);
    nodeTypeList_ = nodeTypeList ;
    for(int i = 0 ; i < nodeTypeList_.size() ; i ++) {
      NodeTypeValue nodeTypeValue = (NodeTypeValue)nodeTypeList_.get(i) ;
      String nodeTypeName = nodeTypeValue.getName() ;
      uiInputSet = new UIFormInputSet(nodeTypeName) ;
      UIFormInputInfo uiInfo = new UIFormInputInfo("label", null, nodeTypeName);
      uiInputSet.addChild(uiInfo);
      UIFormCheckBoxInput<String> checkbox = new UIFormCheckBoxInput<String>(nodeTypeName, nodeTypeName, "") ;
      NodeType register ;
      try {
        register = ntManager.getNodeType(nodeTypeValue.getName()) ;
      } catch(NoSuchNodeTypeException e) {
        register = null ;
      }
      if(register != null) checkbox.setEnable(false);
      else checkbox.setEnable(true) ;
      uiInputSet.addChild(checkbox);
      uiTableInputSet.addChild(uiInputSet);   
    }    
  }
  
  public String getLabel(String id) { return id ; }
  
  static public class CancelActionListener extends EventListener<UINodeTypeImport> {
    public void execute(Event<UINodeTypeImport> event) throws Exception {
      UINodeTypeImport uiImport = event.getSource() ;
      UINodeTypeImportPopup uiImportPopup = uiImport.getParent() ;
      uiImportPopup.setRenderedChild(UINodeTypeUpload.class) ;
      UIPopupWindow uiPopup = uiImportPopup.getParent() ;
      uiPopup.setRendered(false) ;
    }
  }
  
  static public class UploadActionListener extends EventListener<UINodeTypeImport> {
    public void execute(Event<UINodeTypeImport> event) throws Exception {
      UINodeTypeImport uiImport = event.getSource() ;
      UINodeTypeManager uiManager = uiImport.getAncestorOfType(UINodeTypeManager.class) ;
      UIPopupWindow uiPopup = uiManager.findComponentById(UINodeTypeManager.IMPORT_POPUP) ;
      UINodeTypeImportPopup uiImportPopup = uiImport.getParent() ;
      uiImportPopup.setRenderedChild(UINodeTypeUpload.class) ;
      uiPopup.setShow(true) ;
    }
  }
  
  static public class ImportActionListener extends EventListener<UINodeTypeImport> {
    public void execute(Event<UINodeTypeImport> event) throws Exception {
      UINodeTypeImport uiImport = event.getSource() ;
      RepositoryService repositoryService = 
        uiImport.getApplicationComponent(RepositoryService.class) ;
      NodeTypeManager ntManager = repositoryService.getRepository().getNodeTypeManager() ;
      UINodeTypeManager uiManager = uiImport.getAncestorOfType(UINodeTypeManager.class) ;
      UINodeTypeImportPopup uiImportPopup = uiImport.getParent() ;
      uiImportPopup.setRenderedChild(UINodeTypeUpload.class) ;
      UIApplication uiApp = uiImport.getAncestorOfType(UIApplication.class) ;
      ExtendedNodeTypeManager extManager = (ExtendedNodeTypeManager) ntManager ;
      int counter = 0 ;
      List<UIFormCheckBoxInput> listCheckbox =  new ArrayList<UIFormCheckBoxInput>();
      uiImport.findComponentOfType(listCheckbox, UIFormCheckBoxInput.class);
      for(int i = 0 ; i < uiImport.nodeTypeList_.size() ; i ++){
        NodeTypeValue nodeTypeValue = (NodeTypeValue)uiImport.nodeTypeList_.get(i) ;
        if(listCheckbox.get(i).isChecked()) {         
          extManager.registerNodeType(nodeTypeValue, ExtendedNodeTypeManager.IGNORE_IF_EXISTS) ;
          counter += 1 ;          
        }          
      }
      if(counter > 0) {
        String[] count = {String.valueOf(counter)} ;
        uiApp.addMessage(new ApplicationMessage("UINodeTypeImport.msg.nodetype-registered", count)) ;
      } else {
        uiApp.addMessage(new ApplicationMessage("UINodeTypeImport.msg.no-nodetype-registered", null)) ;
      }
      UINodeTypeExport uiExport = uiManager.findComponentById("UINodeTypeExport") ;
      uiExport.update() ;
      UINodeTypeList uiNodeTypeList = uiManager.getChild(UINodeTypeList.class) ;
      uiNodeTypeList.refresh(null) ;
      UIPopupWindow uiPopup = uiManager.findComponentById(UINodeTypeManager.IMPORT_POPUP) ;
      uiPopup.setRendered(false) ;
    }
  }

}
