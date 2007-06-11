/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.drives;

import java.util.List;

import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.services.cms.views.impl.ViewDataImpl;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputSet;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Jun 28, 2006
 */
public class UIViewsInputSet extends UIFormInputSet {
  private ManageViewService vService_ ;
  
  public UIViewsInputSet(String name) throws Exception {
    super(name);
    vService_ = getApplicationComponent(ManageViewService.class);
    List views_ = vService_.getAllViews();
    for(Object view : views_) {
      String viewName = ((ViewDataImpl)view).getName() ;
      addUIFormInput(new UIFormCheckBoxInput<Boolean>(viewName, viewName, null)) ;
    }
  }
  
  public String getViewsSelected() throws Exception {
    StringBuilder selectedView = new StringBuilder() ;
    List views_ = vService_.getAllViews();
    for(Object view : views_){
      String viewName= ((ViewDataImpl)view).getName() ;
      boolean checked = getUIFormCheckBoxInput(viewName).isChecked() ;
      if(checked){
        if(selectedView.length() > 0) selectedView.append(", ") ;
        selectedView.append(viewName) ;
      } 
    }
    if(selectedView.length() < 1 ) {
      throw new MessageException(new ApplicationMessage("UIDriveForm.msg.drive-views-invalid",
                                                        null, ApplicationMessage.WARNING)) ;
    }
    return selectedView.toString() ;
  }
  
  private void clear() throws Exception {
    List views_ = vService_.getAllViews();
    for(Object view : views_){
      String viewName = ((ViewDataImpl)view).getName() ;
      getUIFormCheckBoxInput(viewName).setChecked(false) ;
    }    
  }

  public void update(DriveData drive) throws Exception {
    clear() ;
    if( drive == null) return ;
    String views = drive.getViews() ;
    String[] array = views.split(",") ;
    for(String view: array){
      getUIFormCheckBoxInput(view.trim()).setChecked(true) ;
    }
  }
}