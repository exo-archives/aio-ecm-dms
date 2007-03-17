/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.query.Query;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.ecm.jcr.JCRExceptionManager;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIPopupAction;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormRadioBoxInput;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Dec 26, 2006  
 * 4:29:08 PM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/explorer/popup/admin/UIFormWithMultiRadioBox.gtmpl",
    events = {
      @EventConfig(phase=Phase.DECODE, listeners = UIMetadataSearch.CancelActionListener.class),
      @EventConfig(listeners = UIMetadataSearch.SearchActionListener.class),
      @EventConfig(listeners = UIMetadataSearch.AddMetadataTypeActionListener.class)
    }    
)
public class UIMetadataSearch extends UIForm {

  final static public String INPUT_SEARCH = "input" ;
  final static public String TYPE_SEARCH = "type" ;
  final static public String AND_OPERATION = "and" ;
  final static public String OR_OPERATION = "or" ;
  final static public String OPERATION = "operation" ;
  
  private static final String ROOT_SQL_QUERY = "select * from nt:base where contains(*, '$1')";
  private static final String SQL_QUERY = "select * from nt:base where jcr:path like '$0/%' and contains(*, '$1')";
  
  public UIMetadataSearch() throws Exception {
    addUIFormInput(new UIFormStringInput(INPUT_SEARCH, INPUT_SEARCH, null)) ;
    UIFormInputSetWithAction uiInputAct = new UIFormInputSetWithAction("search") ;
    uiInputAct.addUIFormInput(new UIFormStringInput(TYPE_SEARCH, TYPE_SEARCH, null)) ;
    uiInputAct.setActionInfo(TYPE_SEARCH, new String[] {"AddMetadataType"}) ;
    addUIComponentInput(uiInputAct) ;
    setActions(new String[] {"Search", "Cancel"}) ;
    List<SelectItemOption<String>> typeOperation = new ArrayList<SelectItemOption<String>>() ;
    typeOperation.add(new SelectItemOption<String>(AND_OPERATION, AND_OPERATION));
    typeOperation.add(new SelectItemOption<String>(OR_OPERATION, OR_OPERATION));
    addUIFormInput(new UIFormRadioBoxInput(OPERATION, AND_OPERATION, typeOperation).
                   setAlign(UIFormRadioBoxInput.VERTICAL_ALIGN)) ;
  }

  static  public class CancelActionListener extends EventListener<UIMetadataSearch> {
    public void execute(Event<UIMetadataSearch> event) throws Exception {
      event.getSource().getAncestorOfType(UIPopupAction.class).deActivate() ;
      event.getSource().getAncestorOfType(UIJCRExplorer.class).cancelAction() ;
    }
  }
  
  static public class SearchActionListener extends EventListener<UIMetadataSearch> {
    public void execute(Event<UIMetadataSearch> event) throws Exception {
      UIMetadataSearch uiForm = event.getSource();
      String text = uiForm.getUIStringInput(INPUT_SEARCH).getValue() ;
      String typeOperation = " " + uiForm.<UIFormRadioBoxInput>getUIInput(OPERATION).getValue() + " ";
      String metadata = uiForm.getUIStringInput(TYPE_SEARCH).getValue() ;
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class);
      String queryText = null;
      if ("/".equals(uiExplorer.getCurrentNode().getPath())) queryText = ROOT_SQL_QUERY;
      else queryText = StringUtils.replace(SQL_QUERY, "$0", uiExplorer.getCurrentNode().getPath());
      String statement = null ;
      statement = StringUtils.replace(queryText, "$1", text) ;
      try {
        if(metadata != null && metadata.length() > 0) {
          String[] array = metadata.split(",") ;          
          StringBuffer properties = new StringBuffer() ;
          for(int i = 0; i < array.length ; i ++) {
            properties.append(typeOperation).
                       append(array[i].trim()).append(" like '").append(text).append("' ") ;            
          }
          statement = statement + properties.toString() ;
        }
        UISearch uiSearch = uiForm.getParent() ;
        UISearchResult uiSearchResult = uiSearch.getChild(UISearchResult.class) ;
        uiSearchResult.executeQuery(statement, Query.SQL) ;
        uiSearch.setRenderedChild(UISearchResult.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiSearch.getParent()) ;
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);        
      }
    }
  }
  
  static  public class AddMetadataTypeActionListener extends EventListener<UIMetadataSearch> {
    public void execute(Event<UIMetadataSearch> event) throws Exception {
      UISearchContainer uiContainer = event.getSource().getAncestorOfType(UISearchContainer.class) ;
      uiContainer.initMetadataPopup() ;
      uiContainer.getChild(UISearch.class).setRenderedChild(UIMetadataSearch.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }
}
