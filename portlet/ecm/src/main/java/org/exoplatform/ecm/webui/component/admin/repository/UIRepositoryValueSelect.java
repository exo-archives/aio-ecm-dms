/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.repository;

import java.util.List;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * 19-07-2007  
 */

@ComponentConfig(
    template = "app:/groovy/webui/component/UIGridWithButton.gtmpl",
    events = {
        @EventConfig(listeners = UIRepositoryValueSelect.SelectActionListener.class),
        @EventConfig(listeners = UIRepositoryValueSelect.CloseActionListener.class)
    }
)
public class UIRepositoryValueSelect  extends UIGrid implements UIPopupComponent {
  private static String[] NODETYPE_BEAN_FIELD = {"name"} ;
  private static String[] NODETYPE_ACTION = {"Select"} ;
  protected boolean isSetAuthentication_ = false ;
  protected boolean isSetContainer_ = false ;
  protected boolean isSetStoreType_ = false ;
  protected boolean isSetQueryHandler_ = false ;

  public UIRepositoryValueSelect() throws Exception{
    getUIPageIterator().setId("ValueSelectIterator") ;
    configure("name", NODETYPE_BEAN_FIELD, NODETYPE_ACTION) ;

  }
  public String[] getActions() {
    return new String[] {"Close"} ;
  }
  public void updateGrid(List<ClassData> datas) throws Exception {
    ObjectPageList objPageList = new ObjectPageList(datas, 10) ;
    getUIPageIterator().setPageList(objPageList) ;
  }

  static public class SelectActionListener extends EventListener<UIRepositoryValueSelect> {
    public void execute(Event<UIRepositoryValueSelect> event) throws Exception {
      UIRepositoryValueSelect repoValueList = event.getSource() ;
      String value =  event.getRequestContext().getRequestParameter(OBJECTID) ;
      if(repoValueList.isSetAuthentication_) {
        UIRepositoryFormContainer uiRepoContainer = repoValueList.getAncestorOfType(UIRepositoryFormContainer.class);
        uiRepoContainer.getChild(UIRepositoryForm.class).setAuthentication(value) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiRepoContainer) ;
      }
      else {
        UIWorkspaceWizardContainer uiWSContainer = repoValueList.getAncestorOfType(UIWorkspaceWizardContainer.class) ;
        if(repoValueList.isSetContainer_) {
          uiWSContainer.getChild(UIWorkspaceWizard.class).setContainerName(value);
        } else if(repoValueList.isSetStoreType_) {
          uiWSContainer.getChild(UIWorkspaceWizard.class).setStoreTypeName(value);
        } else if(repoValueList.isSetQueryHandler_) {
          uiWSContainer.getChild(UIWorkspaceWizard.class).setQueryHandlerName(value);
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(uiWSContainer) ;
      }
      UIPopupAction uiPopup = repoValueList.getAncestorOfType(UIPopupAction.class) ;
      uiPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }
  static public class CloseActionListener extends EventListener<UIRepositoryValueSelect> {
    public void execute(Event<UIRepositoryValueSelect> event) throws Exception {
      UIRepositoryValueSelect repoValueList = event.getSource() ;
      UIPopupAction uiPopup = repoValueList.getAncestorOfType(UIPopupAction.class) ;
      uiPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }
  static public class ClassData {
    private String name ;

    public ClassData(String temp ) { name = temp ;}
    public String getName() { return name ;}
  }
  public void activate() throws Exception {
    // TODO Auto-generated method stub

  }
  public void deActivate() throws Exception {
    isSetAuthentication_ = false ;
    isSetContainer_ = false ;
    isSetStoreType_ = false ;
    isSetQueryHandler_ = false ;
  }
}
