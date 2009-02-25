/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ecm.webui.component.admin.views;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.ecm.webui.form.UIFormInputSetWithAction;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.services.cms.views.ViewConfig.Tab;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Jun 28, 2006
 */

@ComponentConfig(template = "classpath:groovy/ecm/webui/form/UIFormInputSetWithAction.gtmpl")
public class UITabForm extends UIFormInputSetWithAction {
  
  final static public String FIELD_NAME = "tabName" ;
  private List buttons_ ;
  
  public UITabForm(String name) throws Exception {
    super(name) ;
    setComponentConfig(getClass(), null) ;
    addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null)) ;
    ManageViewService vservice_ = getApplicationComponent(ManageViewService.class) ;
    buttons_ = vservice_.getButtons();
    for(Object bt : buttons_) {
      addUIFormInput(new UIFormCheckBoxInput<Boolean>(bt.toString(), "", null)) ;
    }
    setActions(new String[]{"Save", "Reset"}, null) ;
  }
  
  public void processRender(WebuiRequestContext context) throws Exception {
    super.processRender(context) ;
  }
  
  public void refresh(boolean isEditable) throws Exception {
    getUIStringInput(FIELD_NAME).setEditable(isEditable).setValue(null) ;
    for(Object bt : buttons_){
      getUIFormCheckBoxInput(bt.toString()).setChecked(false).setEditable(isEditable) ;
    }
    if(isEditable) setActions(new String[]{"Save", "Reset"}, null) ;
  }

  public void update(Tab tab, boolean isView) throws Exception{
    refresh(!isView) ;    
    if(tab == null) return ;
    getUIStringInput(FIELD_NAME).setEditable(false).setValue(tab.getTabName()) ;
    String buttonsProperty = tab.getButtons() ;
    String[] buttonArray = StringUtils.split(buttonsProperty, ";") ;
    for(String bt : buttonArray){
      UIFormCheckBoxInput cbInput = getUIFormCheckBoxInput(bt.trim()) ;
      if(cbInput != null) cbInput.setChecked(true) ;
    }
  }
  
  public void save() throws Exception {
    String tabName = getUIStringInput(FIELD_NAME).getValue() ;
    UIViewFormTabPane viewFormTabPane = getParent() ;
    UIViewForm uiViewForm = viewFormTabPane.getChild(UIViewForm.class) ;
    UITabForm uiTabForm = viewFormTabPane.getChild(UITabForm.class) ;
    if(tabName == null || tabName.trim().length() == 0) {
      viewFormTabPane.setSelectedTab(uiTabForm.getId()) ;
      throw new MessageException(new ApplicationMessage("UITabForm.msg.tab-name-error", null, 
                                                        ApplicationMessage.WARNING)) ;
    }
    String[] arrFilterChar = {"&", "$", "@", ",", ":","]", "[", "*", "%", "!"} ;
    for(String filterChar : arrFilterChar) {
      if(tabName.indexOf(filterChar) > -1) {
        throw new MessageException(new ApplicationMessage("UITabForm.msg.fileName-invalid", null, 
                                                          ApplicationMessage.WARNING)) ;
      }
    }
    StringBuilder selectedButton = new StringBuilder() ;
    boolean isSelected = false ;
    for(Object bt : buttons_ ) {
      String button = bt.toString() ;
      if(getUIFormCheckBoxInput(button).isChecked()) {
        isSelected = true ;
        if(selectedButton.length() > 0) selectedButton.append(";").append(button) ;
        else selectedButton.append(button) ;
      }
    }
    if(!isSelected) {
      viewFormTabPane.setSelectedTab(uiTabForm.getId()) ;
      throw new MessageException(new ApplicationMessage("UITabForm.msg.button-select-error", null)) ;
    }
    viewFormTabPane.setSelectedTab(uiViewForm.getId()) ;
    uiViewForm.addTab(tabName, selectedButton.toString()) ;
    uiViewForm.update(null, false, null) ;
    refresh(true) ;
  }  
}