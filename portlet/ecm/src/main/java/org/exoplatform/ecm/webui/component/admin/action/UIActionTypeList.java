/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jcr.nodetype.NodeType;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@yahoo.de
 * September 20, 2006
 * 16:37:15 
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/UIGridWithButton.gtmpl",
    events = {
        @EventConfig(listeners = UIActionTypeList.AddActionActionListener.class)
    }
)

public class UIActionTypeList extends UIGrid {

  private static String[] ACTIONTYPE_BEAN_FIELD = {"name", "extendType"} ;

  public UIActionTypeList() throws Exception { 
    getUIPageIterator().setId("ActionTypeListIterator");
    configure("name", ACTIONTYPE_BEAN_FIELD, null) ;
  }
  
  public String[] getActions() { return new String[] {"AddAction"} ;}
  
  @SuppressWarnings("unchecked")
  public void updateGrid () throws Exception {
    ActionServiceContainer actionsServiceContainer = 
      getApplicationComponent(ActionServiceContainer.class) ;
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    List actionList = (List)actionsServiceContainer.getCreatedActionTypes(repository) ;
    List<ActionData> actions = new ArrayList<ActionData>(actionList.size()) ;
    for(int i = 0; i < actionList.size(); i ++) {
      ActionData bean = new ActionData() ;
      NodeType action = (NodeType)actionList.get(i) ;
      bean.setName(action.getName()) ;
      NodeType[] superTypes = action.getSupertypes() ;
      StringBuilder types = new StringBuilder() ;
      for(int j = 0; j < superTypes.length; j ++) {
        types.append("[").append(superTypes[j].getName()).append("] ") ;        
      }
      bean.setExtendType(types.toString()) ;
      actions.add(bean) ;
    }
    Collections.sort(actions, new ActionComparator()) ;
    ObjectPageList objPageList = new ObjectPageList(actions, 10) ;
    getUIPageIterator().setPageList(objPageList) ;
  }   
  
  static public class ActionComparator implements Comparator {
    public int compare(Object o1, Object o2) throws ClassCastException {
      String name1 = ((ActionData) o1).getName() ;
      String name2 = ((ActionData) o2).getName() ;
      return name1.compareToIgnoreCase(name2) ;
    }
  }
  
  static public class AddActionActionListener extends EventListener<UIActionTypeList> {
    public void execute(Event<UIActionTypeList> event) throws Exception {
      UIActionManager uiActionMan = event.getSource().getParent() ;
      UIActionTypeForm uiForm = uiActionMan.findFirstComponentOfType(UIActionTypeForm.class) ;
      if (uiForm == null) uiForm = uiActionMan.createUIComponent(UIActionTypeForm.class, null, null) ;
      uiForm.refresh() ;
      uiActionMan.initPopup(uiForm, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionMan) ;
    }
  }
  
  public static class ActionData {
    private String name ;
    private String extendType ;

    public String getName() { return name ; }
    public void setName(String s) { name = s ; }    

    public String getExtendType() { return extendType ; }
    public void setExtendType(String s) { extendType = s ; }
  }
}