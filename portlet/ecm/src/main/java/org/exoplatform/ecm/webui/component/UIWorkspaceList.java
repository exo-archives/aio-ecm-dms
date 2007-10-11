/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jun 21, 2007 2:32:49 PM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/component/explorer/UIFormWithoutAction.gtmpl",
    events = { 
      @EventConfig(listeners = UIWorkspaceList.ChangeWorkspaceActionListener.class)
    }
)
public class UIWorkspaceList extends UIForm {

  static private String WORKSPACE_NAME = "workspaceName" ;
  private List<String> wsList_ ;
  private boolean isShowSystem_ = true ;

  public UIWorkspaceList() throws Exception {
    List<SelectItemOption<String>> wsList = new ArrayList<SelectItemOption<String>>() ;
    UIFormSelectBox uiWorkspaceList = new UIFormSelectBox(WORKSPACE_NAME, WORKSPACE_NAME, wsList) ;
    uiWorkspaceList.setOnChange("ChangeWorkspace") ;
    addUIFormInput(uiWorkspaceList) ;
  }
  
  public void setIsShowSystem(boolean isShowSystem) { isShowSystem_ = isShowSystem ; }
  
  public void setWorkspaceList(String repository) throws Exception {
    wsList_ = new ArrayList<String>() ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    String[] wsNames = repositoryService.getRepository(repository).getWorkspaceNames();
    String systemWsName = 
      repositoryService.getRepository(repository).getConfiguration().getSystemWorkspaceName() ;
    List<SelectItemOption<String>> workspace = new ArrayList<SelectItemOption<String>>() ;
    for(String wsName : wsNames) {
      if(!isShowSystem_) {
        if(!wsName.equals(systemWsName)) {
          workspace.add(new SelectItemOption<String>(wsName,  wsName)) ;
          wsList_.add(wsName) ;
        }
      } else {
        workspace.add(new SelectItemOption<String>(wsName,  wsName)) ;
        wsList_.add(wsName) ;
      }
    }
    UIFormSelectBox uiWorkspaceList = getUIFormSelectBox(WORKSPACE_NAME) ;
    uiWorkspaceList.setOptions(workspace) ;
    UIJCRBrowser uiBrowser = getParent() ;
    if(uiBrowser.getWorkspace() != null) {
      if(wsList_.contains(uiBrowser.getWorkspace())) {
        uiWorkspaceList.setValue(uiBrowser.getWorkspace()) ; 
      }
    }
  }
  
  public void setIsDisable(String wsName, boolean isDisable) {
    if(wsList_.contains(wsName)) getUIFormSelectBox(WORKSPACE_NAME).setValue(wsName) ; 
    getUIFormSelectBox(WORKSPACE_NAME).setDisabled(isDisable) ;
  }
  
  static public class ChangeWorkspaceActionListener extends EventListener<UIWorkspaceList> {
    public void execute(Event<UIWorkspaceList> event) throws Exception {
      UIWorkspaceList uiWorkspaceList = event.getSource() ;
      UIJCRBrowser uiJBrowser = uiWorkspaceList.getParent() ;
      String wsName = uiWorkspaceList.getUIFormSelectBox(WORKSPACE_NAME).getValue() ;
      uiJBrowser.setWorkspace(wsName) ;
      UITreeJCRExplorer uiTreeJCRExplorer = uiJBrowser.getChild(UITreeJCRExplorer.class) ;
      uiTreeJCRExplorer.setRootNode(null) ;
      uiTreeJCRExplorer.buildTree() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiJBrowser) ;
    }
  }
}
