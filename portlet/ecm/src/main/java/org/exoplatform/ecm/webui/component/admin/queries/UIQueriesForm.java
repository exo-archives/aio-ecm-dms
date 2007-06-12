/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.queries;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Value;
import javax.jcr.query.Query;

import org.exoplatform.ecm.jcr.ECMNameValidator;
import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.services.cms.queries.QueryService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.EmptyFieldValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Dec 29, 2006  
 * 11:30:29 AM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIQueriesForm.SaveActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIQueriesForm.CancelActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIQueriesForm.ChangeQueryTypeActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIQueriesForm.AddPermissionActionListener.class)
    }
)
public class UIQueriesForm extends UIForm implements UISelector {

  final static public String QUERY_NAME = "name" ;
  final static public String QUERY_TYPE = "type" ;
  final static public String STATEMENT = "statement" ;
  final static public String PERMISSIONS = "permissions" ;
  final static public String CACHE_RESULT = "cache" ;
  final static public String[] ACTIONS = {"Save", "Cancel"} ;
  final static public String SQL_QUERY = "select * from exo:article where jcr:path like '/cms/publications/%'" ;
  final static public String XPATH_QUERY = "/jcr:root/cms/publications//element(*, exo:article)" ;
  final static public String[] REG_EXPRESSION = {"[", "]", ":", "&"} ;
  public UIQueriesForm() throws Exception {
    addUIFormInput(new UIFormStringInput(QUERY_NAME, QUERY_NAME, null).
        addValidator(ECMNameValidator.class)) ;
    List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>() ;
    ls.add(new SelectItemOption<String>("xPath", "xpath")) ;
    ls.add(new SelectItemOption<String>("SQL", "sql")) ;
    UIFormSelectBox uiSelectBox = new UIFormSelectBox(QUERY_TYPE, QUERY_TYPE, ls) ;
    uiSelectBox.setOnChange("ChangeQueryType") ;
    addUIFormInput(uiSelectBox) ;
    addUIFormInput(new UIFormTextAreaInput(STATEMENT, STATEMENT, null).setValue(XPATH_QUERY).
        addValidator(EmptyFieldValidator.class)) ;
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(CACHE_RESULT, CACHE_RESULT, null)) ;
    UIFormInputSetWithAction uiInputAct = new UIFormInputSetWithAction("PermissionButton") ;
    uiInputAct.addUIFormInput( new UIFormStringInput(PERMISSIONS, PERMISSIONS, null).setEditable(false));
    uiInputAct.setActionInfo(PERMISSIONS, new String[] {"AddPermission"}) ;
    addUIComponentInput(uiInputAct) ;
  }

  public String[] getActions() { return ACTIONS ; }

  public void updateSelect(String selectField, String value) {
    getUIStringInput(selectField).setValue(value) ;
    UIQueriesManager uiManager = getAncestorOfType(UIQueriesManager.class) ;
    UIPopupWindow uiPopup = uiManager.getChildById("PermissionPopup") ;
    uiPopup.setRendered(false) ;
    uiPopup.setShow(false) ;
  }

  public void update(String queryName)throws Exception{
    QueryService queryService = getApplicationComponent(QueryService.class) ;
    if(queryName == null) {
      reset() ;
      return ;
    }
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    Node query = queryService.getSharedQuery(queryName, repository) ;
    getUIStringInput(QUERY_NAME).setValue(queryName) ;
    getUIStringInput(QUERY_NAME).setEditable(false) ;
    getUIFormCheckBoxInput(CACHE_RESULT).setChecked(query.getProperty("exo:cachedResult").getBoolean()) ;
    getUIFormTextAreaInput(STATEMENT).setValue(query.getProperty("jcr:statement").getString()) ;
    getUIFormSelectBox(QUERY_TYPE).setValue(query.getProperty("jcr:language").getString()) ;
    Value[] values = query.getProperty("exo:permissions").getValues() ;
    StringBuilder strValues = new StringBuilder() ;
    for(int i = 0; i < values.length; i ++) {
      if(strValues.length() > 0) strValues = strValues.append(",") ;
      strValues = strValues.append(values[i].getString()) ;
    }
    getUIStringInput(PERMISSIONS).setValue(strValues.toString()) ;      
  }

  static public class CancelActionListener extends EventListener<UIQueriesForm> {
    public void execute(Event<UIQueriesForm> event) throws Exception {
      UIQueriesForm uiForm = event.getSource() ;
      UIQueriesManager uiManager = uiForm.getAncestorOfType(UIQueriesManager.class) ;
      uiManager.removeChildById(UIQueriesList.ST_ADD) ;
      uiManager.removeChildById(UIQueriesList.ST_EDIT) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }

  static public class SaveActionListener extends EventListener<UIQueriesForm> {
    public void execute(Event<UIQueriesForm> event) throws Exception {
      UIQueriesForm uiForm = event.getSource() ;
      String repository = uiForm.getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
      QueryService queryService = uiForm.getApplicationComponent(QueryService.class) ;
      String queryName = uiForm.getUIStringInput(QUERY_NAME).getValue() ;
      if(!Utils.isNameValid(queryName, UIQueriesForm.REG_EXPRESSION)) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIQueriesForm.msg.name-invalid", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String statement = uiForm.getUIFormTextAreaInput(STATEMENT).getValue() ;
      UIFormInputSetWithAction permField = uiForm.getChildById("PermissionButton") ;
      String permissions = permField.getUIStringInput(PERMISSIONS).getValue() ;
      if((permissions == null)||(permissions.trim().length() == 0)) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIQueriesForm.msg.permission-require", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String queryType = uiForm.getUIFormSelectBox(QUERY_TYPE).getValue() ;
      boolean cacheResult = uiForm.getUIFormCheckBoxInput(CACHE_RESULT).isChecked() ;
      if(permissions.indexOf(",") > -1) {
        queryService.addSharedQuery(queryName, statement, queryType, permissions.split(","), 
                                   cacheResult, repository) ;  
      } else {
        queryService.addSharedQuery(queryName, statement, queryType, new String[] {permissions}, 
                                   cacheResult, repository) ;
      }   
      UIQueriesManager uiManager = uiForm.getAncestorOfType(UIQueriesManager.class) ;
      uiManager.getChild(UIQueriesList.class).updateQueriesGrid() ;
      uiManager.removeChildById(UIQueriesList.ST_ADD) ;
      uiManager.removeChildById(UIQueriesList.ST_EDIT) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }

  static public class ChangeQueryTypeActionListener extends EventListener<UIQueriesForm> {
    public void execute(Event<UIQueriesForm> event) throws Exception {
      UIQueriesForm uiForm= event.getSource() ;
      String queryType = uiForm.getUIFormSelectBox(QUERY_TYPE).getValue() ;
      if(queryType.equals(Query.XPATH)) {
        uiForm.getUIFormTextAreaInput(STATEMENT).setValue(XPATH_QUERY) ;
      } else {
        uiForm.getUIFormTextAreaInput(STATEMENT).setValue(SQL_QUERY) ;
      }
    }
  }

  static public class AddPermissionActionListener extends EventListener<UIQueriesForm> {
    public void execute(Event<UIQueriesForm> event) throws Exception {
      UIQueriesManager uiManager = event.getSource().getAncestorOfType(UIQueriesManager.class) ;
      String membership = event.getSource().getUIStringInput(PERMISSIONS).getValue() ;
      uiManager.initPermissionPopup(membership) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
}
