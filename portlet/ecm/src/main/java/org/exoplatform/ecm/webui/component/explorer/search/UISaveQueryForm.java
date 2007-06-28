/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import javax.jcr.query.Query;

import org.exoplatform.ecm.jcr.ECMNameValidator;
import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorerPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.queries.QueryService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jun 28, 2007 9:43:21 AM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UISaveQueryForm.SaveActionListener.class),
      @EventConfig(listeners = UISaveQueryForm.CancelActionListener.class, phase=Phase.DECODE)
    }
)
public class UISaveQueryForm extends UIForm implements UIPopupComponent {
  
  final static public String QUERY_NAME = "queryName" ;
  private String statement_ ;
  private boolean isSimpleSearch_ = false ;
  private String queryType_ ;

  public UISaveQueryForm() throws Exception {
    addUIFormInput(new UIFormStringInput(QUERY_NAME, QUERY_NAME, null).addValidator(ECMNameValidator.class)) ;
  }
  
  public void activate() throws Exception {}
  
  public void deActivate() throws Exception {}
  
  public void setSimpleSearch(boolean isSimpleSearch) { isSimpleSearch_ = isSimpleSearch ; }
  
  public void setStatement(String statement) { statement_ = statement ; }
  
  public void setQueryType(String queryType) { queryType_ = queryType ; } 
  
  static  public class SaveActionListener extends EventListener<UISaveQueryForm> {
    public void execute(Event<UISaveQueryForm> event) throws Exception {
      UISaveQueryForm uiSaveQueryForm = event.getSource() ;
      String repository = uiSaveQueryForm.getAncestorOfType(UIJCRExplorerPortlet.class).getPreferenceRepository() ;
      UIECMSearch uiECMSearch = uiSaveQueryForm.getAncestorOfType(UIECMSearch.class) ;
      UIApplication uiApp = uiSaveQueryForm.getAncestorOfType(UIApplication.class) ;
      String userName = Util.getPortalRequestContext().getRemoteUser() ;
      QueryService queryService = uiSaveQueryForm.getApplicationComponent(QueryService.class) ;
      String queryName = uiSaveQueryForm.getUIStringInput(QUERY_NAME).getValue() ;
      if(queryName == null || queryName.trim().length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UISaveQueryForm.msg.query-name-null", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      try {
        queryService.addQuery(queryName, uiSaveQueryForm.statement_, uiSaveQueryForm.queryType_, userName, repository) ;        
      } catch (Exception e){
        uiApp.addMessage(new ApplicationMessage("UISaveQueryForm.msg.save-failed", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      uiECMSearch.getChild(UISavedQuery.class).updateGrid() ;
      if(uiSaveQueryForm.isSimpleSearch_) {
        UISearchContainer uiSearchContainer = uiSaveQueryForm.getAncestorOfType(UISearchContainer.class) ;
        UIPopupAction uiPopup = uiSearchContainer.getChild(UIPopupAction.class) ;
        uiPopup.deActivate() ;
      }
      uiECMSearch.setRenderedChild(UISavedQuery.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiECMSearch) ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UISaveQueryForm> {
    public void execute(Event<UISaveQueryForm> event) throws Exception {
      UISearchContainer uiSearchContainer = event.getSource().getAncestorOfType(UISearchContainer.class) ;
      UIPopupAction uiPopup = uiSearchContainer.getChild(UIPopupAction.class) ;
      uiPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }
}