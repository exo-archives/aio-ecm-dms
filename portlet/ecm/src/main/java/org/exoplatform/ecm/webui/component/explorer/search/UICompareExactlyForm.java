/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Value;
import javax.jcr.query.QueryResult;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmptyFieldValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * May 6, 2007  
 * 10:18:56 AM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/explorer/search/UICompareExactlyForm.gtmpl",
    events = {
      @EventConfig(listeners = UICompareExactlyForm.SelectActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UICompareExactlyForm.CancelActionListener.class)
    }    
)
public class UICompareExactlyForm extends UIForm implements UIPopupComponent {
  private static final String FILTER = "filter" ;
  private static final String RESULT = "result";
  private static final String TEMP_RESULT = "tempSel";
  private List<String> listValue_ ;
  
  public UICompareExactlyForm() throws Exception {}
  
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
  public void init(String properties, QueryResult result) throws Exception {
    listValue_ = new ArrayList<String>() ;
    List<SelectItemOption<String>> opts = new ArrayList<SelectItemOption<String>>();
    addUIFormInput(new UIFormStringInput(FILTER, FILTER, null)) ;
    addUIFormInput(new UIFormSelectBox(RESULT, RESULT, opts).setSize(15).addValidator(EmptyFieldValidator.class)) ;
    addUIFormInput(new UIFormSelectBox(TEMP_RESULT, TEMP_RESULT, opts)) ;
    
    NodeIterator iter = result.getNodes() ;
    String[] props = {} ;
    if(properties.indexOf(",") > -1) props = properties.split(",") ;
    while(iter.hasNext()) {
      Node node = iter.nextNode() ;
      if(props.length > 0) {
        for(String pro : props) {
          if(node.hasProperty(pro)) {
            Property property = node.getProperty(pro) ;
            setPropertyResult(property) ;
          }
        }
      } else {
        if(node.hasProperty(properties)) {
          Property property = node.getProperty(properties) ;
          setPropertyResult(property) ;
        }
      }
    }
    Collections.sort(listValue_) ;
    for(String value : listValue_) {
      opts.add(new SelectItemOption<String>(value, value)) ;
    }
  }

  public void setPropertyResult(Property property) throws Exception {
    if(property.getDefinition().isMultiple()) {
      Value[] values = property.getValues() ;
      for(Value value : values) {
        if(!listValue_.contains(value.getString())) listValue_.add(value.getString()) ;
      }
    } else {
      Value value = property.getValue() ;
      if(!listValue_.contains(value.getString())) listValue_.add(value.getString()) ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UICompareExactlyForm> {
    public void execute(Event<UICompareExactlyForm> event) throws Exception {
      UISearchContainer uiSearchContainer = event.getSource().getAncestorOfType(UISearchContainer.class) ;
      UIPopupAction uiPopup = uiSearchContainer.getChild(UIPopupAction.class) ;
      uiPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }
  
  static  public class SelectActionListener extends EventListener<UICompareExactlyForm> {
    public void execute(Event<UICompareExactlyForm> event) throws Exception {
      UICompareExactlyForm uiForm = event.getSource() ;
      String value = uiForm.getUIFormSelectBox(RESULT).getValue();
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class);
      UISearchContainer uiSearchContainer = uiPopupAction.getParent() ;
      UIConstraintsForm uiConstraintsForm =
        uiSearchContainer.findFirstComponentOfType(UIConstraintsForm.class) ;
      uiConstraintsForm.getUIStringInput(UIConstraintsForm.CONTAIN_EXACTLY).setValue(value) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiConstraintsForm) ;
    }
  }
}