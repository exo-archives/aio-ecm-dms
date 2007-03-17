/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component;

import java.util.HashMap;
import java.util.List;

import org.exoplatform.webui.application.RequestContext;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormInput;
import org.exoplatform.webui.component.UIFormInputSet;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Sep 20, 2006
 */
@ComponentConfig(
   template = "app:/groovy/webui/component/admin/UIFormInputSetWithAction.gtmpl"
)
public class UIFormInputSetWithAction extends UIFormInputSet implements UIFormInput {

  private String[] actions_ ;
  private String[] values_ ;
  private boolean isView_ ;
  private boolean isShowOnly_ = false;
  private HashMap<String, String> infor_ = new HashMap<String, String>() ;
  private HashMap<String, String[]> actionInfo_ = new HashMap<String, String[]>() ;
  
  public UIFormInputSetWithAction(String name) {  
    setId(name) ;
    setComponentConfig(getClass(), null) ;  
  }
  
  public void processRender(RequestContext context) throws Exception {
    super.processRender(context) ;
  }

  public void setActions(String[] actionList, String[] values){ 
    actions_ = actionList ; 
    values_ = values ;    
  }
  
  public String[] getInputSetActions() { return actions_ ; }
  public String[] getActionValues() { return values_ ; }
  
  public String getFormName() { 
    UIForm uiForm = getAncestorOfType(UIForm.class);
    return uiForm.getId() ; 
  }
  
  public boolean isShowOnly() { return isShowOnly_ ; }
  public void setIsShowOnly(boolean isShowOnly) { isShowOnly_ = isShowOnly ; }
  
  public void setInfoField(String fieldName, String fieldInfo) {
    infor_.put(fieldName, fieldInfo) ;
  }
  
  public String getInfoField(String fieldName) {
    if(infor_.containsKey(fieldName)) return infor_.get(fieldName) ;
    return null ;
  }
  
  public void setActionInfo(String fieldName, String[] actionNames) {
    actionInfo_.put(fieldName, actionNames) ;
  }
  
  public String[] getActionInfo(String fieldName) {
    if(actionInfo_.containsKey(fieldName)) return actionInfo_.get(fieldName) ;
    return null ;
  }
  
  public void setIsView(boolean isView) { isView_ = isView; }
  public boolean isView() { return isView_ ; }

  @SuppressWarnings("unused")
  public UIFormInput addValidator(Class clazz) throws Exception { return null; }

  public String getBindingField() { return null; }

  public List getValidators() { return null; }

  public Object getValue() throws Exception { return null; }

  @SuppressWarnings("unused")
  public UIFormInput setValue(Object value) throws Exception { return null; }
}