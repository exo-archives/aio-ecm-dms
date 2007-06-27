/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.admin;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@yahoo.de
 * September 13, 2006
 * 10:07:15 AM
 */

@ComponentConfig(
    template =  "app:/groovy/webui/component/explorer/popup/info/UIPropertyTab.gtmpl",
    events = {@EventConfig(listeners = UIPropertyTab.CloseActionListener.class)}
)

public class UIPropertyTab extends UIContainer {
  private static String[] PRO_BEAN_FIELD = {"icon", "name", "multiValue", "value"} ;
  final private static String PRO_KEY_BINARYTYPE = "binary" ;
  final private static String PRO_KEY_CANNOTGET = "cannotget" ;
  
  public String[] getBeanFields() { return PRO_BEAN_FIELD ;}
  
  public String[] getActions() {return  new String[] {"Close"} ;}
  
  public PropertyIterator getProperties() throws Exception { 
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ; 
    return uiExplorer.getCurrentNode().getProperties() ; 
  }
  
  /*private boolean isMultiValue(Property prop) throws Exception {
    return prop.getDefinition().isMultiple() ;
  }*/
  
  public String getPropertyValue(Property prop) throws Exception {
    if(prop.getType() == PropertyType.BINARY) return PRO_KEY_BINARYTYPE ;
    try {
      if(prop.getDefinition() != null && prop.getDefinition().isMultiple()) {
        Value[] values =  prop.getValues();
        StringBuilder sB = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
          if(i > 0) sB.append("; ") ;
          sB.append(values[i].getString());
        }
        return sB.toString();
      }
      return prop.getString() ;
    } catch(ValueFormatException e) {
      return PRO_KEY_CANNOTGET ;
    } 
  }
  
  static public class CloseActionListener extends EventListener<UIPropertyTab> {
    public void execute(Event<UIPropertyTab> event) throws Exception {
      event.getSource().getAncestorOfType(UIJCRExplorer.class).cancelAction() ;
    }
  }
}