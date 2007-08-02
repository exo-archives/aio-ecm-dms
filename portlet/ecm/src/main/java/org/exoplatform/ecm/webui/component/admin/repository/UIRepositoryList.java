/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.repository;

import java.util.ArrayList;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 1, 2007  
 */

@ComponentConfig(
    template = "app:/groovy/webui/component/UIGridWithButton.gtmpl",
    events = {
        @EventConfig(listeners = UIRepositoryList.DeleteActionListener.class, confirm="UIRepositoryList.msg.confirm-delete"),
        @EventConfig(listeners = UIRepositoryList.CloseActionListener.class)
    }
)

public class UIRepositoryList extends UIGrid  implements UIPopupComponent{
  private static String[] REPO_BEAN_FIELD = {"name", "workspaces", "isdefault","accesscontrol", "sessiontimeout" } ;
  private static String[] REPO_ACTION = {"Delete"} ;

  public UIRepositoryList() throws Exception {
    configure("name", REPO_BEAN_FIELD, REPO_ACTION) ;
  }
  public String[] getActions() { return new String[]{"Close"} ; }

  public void updateGrid() throws Exception {
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;
    String defaultName =  rservice.getConfig().getDefaultRepositoryName() ;
    ArrayList<RepoData> repos = new ArrayList<RepoData>() ;
    for(Object obj : rservice.getConfig().getRepositoryConfigurations()) { 
      RepositoryEntry repo  = (RepositoryEntry)obj ;
      StringBuilder sb = new StringBuilder() ;
      repo.getWorkspaceEntries() ;
      for(WorkspaceEntry ws : repo.getWorkspaceEntries()) {
        sb.append(ws.getName()).append(";") ;
      }
      String name = repo.getName() ;
      String isDefault = String.valueOf(defaultName.equals(repo.getName())) ;
      String accessControl = repo.getAccessControl() ;
      String sessionTime = String.valueOf(repo.getSessionTimeOut()) ;
      String workspace = sb.toString() ;
      repos.add(new RepoData(name, workspace, isDefault, accessControl, sessionTime)) ;
    }
    ObjectPageList objPageList = new ObjectPageList(repos, 10) ;
    getUIPageIterator().setPageList(objPageList) ; 
  }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  static  public class DeleteActionListener extends EventListener<UIRepositoryList> {
    public void execute(Event<UIRepositoryList> event) throws Exception {
      UIRepositoryList uiList = event.getSource() ;
      UIECMAdminPortlet uiAdminPortlet = uiList.getAncestorOfType(UIECMAdminPortlet.class) ;
      UIRepositoryControl uiControl = uiAdminPortlet.findFirstComponentOfType(UIRepositoryControl.class) ;
      RepositoryService rservice = uiList.getApplicationComponent(RepositoryService.class) ;
      String repoName = event.getRequestContext().getRequestParameter(OBJECTID)  ;
      UIApplication uiApp = uiList.getAncestorOfType(UIApplication.class) ;
      Object[] args = new Object[]{repoName}  ;        
      if(!rservice.canRemoveRepository(repoName)) {
        uiApp.addMessage(new ApplicationMessage("UIRepositoryList.msg.cannot-delete", args)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
        return ;
      }  
      rservice.removeRepository(repoName) ;
      if(rservice.getConfig().isRetainable()) {
        rservice.getConfig().retain() ;
      }
      uiList.updateGrid() ;
      uiControl.reloadValue(true, rservice);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiList.getAncestorOfType(UIPopupAction.class)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiControl) ;
    }
  }

  static  public class CloseActionListener extends EventListener<UIRepositoryList> {
    public void execute(Event<UIRepositoryList> event) throws Exception {
      UIPopupAction uiPopup = event.getSource().getAncestorOfType(UIPopupAction.class);
      uiPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }

  public class RepoData{
    String name ;
    String workspaces ;
    String isdefault = "false" ;
    String accesscontrol ;
    String sessiontimeout ;

    public RepoData(String rname, String rworkspaces, String risdefault, String raccess, String rtime){
      name = rname ;
      workspaces = rworkspaces ;
      isdefault = risdefault ;
      accesscontrol  = raccess ;
      sessiontimeout = rtime ;
    }
    public String getName() {return name ;}
    public String getWorkspaces () {return workspaces ;}
    public String getIsdefault() {return isdefault ;}
    public String getAccesscontrol() {return accesscontrol ;}
    public String getSessiontimeout() {return sessiontimeout ;}

  }
}
