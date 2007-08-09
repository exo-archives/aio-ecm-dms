/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.repository;

import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.NumberFormatValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 8, 2007  
 */
@ComponentConfig(template = "app:/groovy/webui/component/UIFormInputSetWithAction.gtmpl")
public class UIWizardStep3 extends UIFormInputSetWithAction {
  final static public String FIELD_QUERYHANDLER = "queryHandler" ;
  final static public String FIELD_INDEXPATH = "indexPath" ;
  final static public String FIELD_ISCACHE = "isCache" ;
  final static public String FIELD_MAXSIZE = "maxSize" ;
  final static public String FIELD_LIVETIME = "liveTime" ;


  public UIWizardStep3(String name) throws Exception {
    super(name);
    addChild(new UIFormStringInput(FIELD_QUERYHANDLER, FIELD_QUERYHANDLER, null)) ;
    setActionInfo(FIELD_QUERYHANDLER, new String[]{"SelectQueryHandler"}) ;
    addChild(new UIFormStringInput(FIELD_INDEXPATH, FIELD_INDEXPATH, null)) ;
    addChild(new UIFormCheckBoxInput<Boolean>(FIELD_ISCACHE, FIELD_ISCACHE, null)) ;
    addChild(new UIFormStringInput(FIELD_MAXSIZE, FIELD_MAXSIZE, null).addValidator(NumberFormatValidator.class)) ;
    addChild(new UIFormStringInput(FIELD_LIVETIME, FIELD_LIVETIME, null).addValidator(NumberFormatValidator.class)) ;     
  }
  protected void setFieldQueryHandler(String queryHandler) {
    getUIStringInput(FIELD_QUERYHANDLER).setValue(queryHandler) ;
  }
  protected void setFieldIndexPath(String indexPath) {
    getUIStringInput(FIELD_INDEXPATH).setValue(indexPath) ;
  }
  protected void setFieldIsCache(boolean isCache) {
    getUIFormCheckBoxInput(FIELD_ISCACHE).setChecked(isCache) ;
  }
  protected void setFieldCacheMaxSize(String maxSize) {
    getUIStringInput(FIELD_MAXSIZE).setValue(maxSize) ;
  }
  protected void setFieldLiveTime(String liveTime) {
    getUIStringInput(FIELD_LIVETIME).setValue(liveTime) ;
  }
  protected void resetFields(){
    reset() ;
    setFieldIsCache(false) ;
  }
  protected void fillFields(String queryHandler, String indexPath, boolean isCache, String maxSize, String liveTime){
    setFieldQueryHandler(queryHandler) ;
    setFieldIndexPath(indexPath) ;
    setFieldIsCache(isCache) ;
    setFieldCacheMaxSize(maxSize) ;
    setFieldLiveTime(liveTime) ;
  }
  protected void lockFields(boolean isLock){
    boolean isEdiable = !isLock ;
    if(isLock){
      setActionInfo(FIELD_QUERYHANDLER, null) ;
    } else {
      setActionInfo(FIELD_QUERYHANDLER, new String[]{"SelectQueryHandler"}) ;
    }

    getUIStringInput(FIELD_QUERYHANDLER).setEditable(isEdiable) ;
    getUIStringInput(FIELD_INDEXPATH).setEditable(isEdiable) ;
    getUIFormCheckBoxInput(FIELD_ISCACHE).setEnable(isEdiable) ;
    getUIStringInput(FIELD_MAXSIZE).setEditable(isEdiable) ;
    getUIStringInput(FIELD_LIVETIME).setEditable(isEdiable) ;
  }
}
