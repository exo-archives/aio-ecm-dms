/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.ecm.jcr.model.Preference;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@yahoo.de
 * September 5, 2006
 * 14:07:15 PM
 */

@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template =  "system:/groovy/webui/form/UIFormWithTitle.gtmpl",
  events = {    
      @EventConfig(listeners = UIPreferencesForm.SaveActionListener.class),
      @EventConfig (phase = Phase.DECODE, listeners = UIPreferencesForm.BackActionListener.class)
  }
)

public class UIPreferencesForm extends UIForm {
  
  final static public String FIELD_ENABLESTRUCTURE = "enableStructure" ;
  final static public String FIELD_SHOWSIDEBAR = "showSideBar" ;
  final static public String FIELD_SHOWNONDOCUMENT = "showNonDocument" ;
  final static public String FIELD_SHOWREFDOCUMENTS = "showRefDocuments" ;
  final static public String FIELD_SHORTBY = "sortBy" ;
  final static public String FIELD_ORDERBY = "order" ;
  final static public String FIELD_PROPERTY = "property" ;
  
  private  List<SelectItemOption<String>> sortOptions = new ArrayList<SelectItemOption<String>>() ;
  private  List<SelectItemOption<String>> orderOption = new ArrayList<SelectItemOption<String>>() ;
  
  public UIPreferencesForm() throws Exception {    
   sortOptions.add(
       new SelectItemOption<String>(Preference.ALPHABETICAL_SORT,  Preference.ALPHABETICAL_SORT)) ;
   sortOptions.add(
       new SelectItemOption<String>(Preference.TYPE_SORT, Preference.TYPE_SORT)) ;
   sortOptions.add(
       new SelectItemOption<String>(Preference.PROPERTY_SORT, Preference.PROPERTY_SORT)) ;
   orderOption.add(
       new SelectItemOption<String>(Preference.ASCENDING_ORDER, Preference.ASCENDING_ORDER)) ;
   orderOption.add(
       new SelectItemOption<String>(Preference.DESCENDING_ORDER, Preference.DESCENDING_ORDER)) ;
   
   addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_ENABLESTRUCTURE, FIELD_ENABLESTRUCTURE, null)) ;
   addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_SHOWSIDEBAR, FIELD_SHOWSIDEBAR, null)) ;
   addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_SHOWNONDOCUMENT, FIELD_SHOWNONDOCUMENT, null)) ;
   addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_SHOWREFDOCUMENTS, FIELD_SHOWREFDOCUMENTS, null)) ;
   addUIFormInput(new UIFormSelectBox(FIELD_SHORTBY,FIELD_SHORTBY, sortOptions)) ;
   addUIFormInput(new UIFormSelectBox(FIELD_ORDERBY,FIELD_ORDERBY, orderOption)) ;
   addUIFormInput(new UIFormStringInput(FIELD_PROPERTY, FIELD_PROPERTY, null));
  }
  
  public void update(Preference pref) {          
    getUIFormCheckBoxInput(FIELD_ENABLESTRUCTURE).setChecked(pref.isJcrEnable()) ;
    getUIFormCheckBoxInput(FIELD_SHOWSIDEBAR).setChecked(pref.isShowSideBar()) ;
    getUIFormCheckBoxInput(FIELD_SHOWNONDOCUMENT).setChecked(pref.isShowNonDocumentType()) ;
    getUIFormCheckBoxInput(FIELD_SHOWREFDOCUMENTS).setChecked(pref.isShowPreferenceDocuments()) ;
    getUIFormSelectBox(FIELD_SHORTBY).setDefaultValue(pref.getSort()) ;
    getUIFormSelectBox(FIELD_ORDERBY).setDefaultValue(pref.getOrder()) ;
    getUIStringInput(FIELD_PROPERTY).setValue(pref.getProperty()) ;
  }  

  @SuppressWarnings("unused")
  static  public class SaveActionListener extends EventListener<UIPreferencesForm> {
    public void execute(Event<UIPreferencesForm> event) throws Exception {     
      UIPreferencesForm uiForm = event.getSource() ;
      UIJCRExplorerPortlet explorerPorltet = uiForm.getParent() ;   
      UIJCRExplorer uiExplorer = explorerPorltet.getChild(UIJCRExplorer.class) ;
      Preference pref = uiExplorer.getPreference() ;
      pref.setJcrEnable(uiForm.getUIFormCheckBoxInput(FIELD_ENABLESTRUCTURE).isChecked()) ;
      pref.setShowSideBar(uiForm.getUIFormCheckBoxInput(FIELD_SHOWSIDEBAR).isChecked()) ;
      pref.setShowNonDocumentType(
          uiForm.getUIFormCheckBoxInput(FIELD_SHOWNONDOCUMENT).isChecked()) ;
      pref.setShowPreferenceDocuments(
          uiForm.getUIFormCheckBoxInput(FIELD_SHOWREFDOCUMENTS).isChecked()) ;
      pref.setSort(uiForm.getUIFormSelectBox(FIELD_SHORTBY).getValue()) ;
      pref.setSort(uiForm.getUIFormSelectBox(FIELD_ORDERBY).getValue()) ;
      pref.setProperty(uiForm.getUIStringInput(FIELD_PROPERTY).getValue()) ;
      uiExplorer.refreshExplorer() ;
      uiForm.setRenderSibbling(UIJCRExplorer.class) ;      
    }
  }

  static  public class BackActionListener extends EventListener<UIPreferencesForm> {
    public void execute(Event<UIPreferencesForm> event) throws Exception {
      UIPreferencesForm uiForm = event.getSource() ;      
      uiForm.setRenderSibbling(UIJCRExplorer.class) ;
    }
  }
}

