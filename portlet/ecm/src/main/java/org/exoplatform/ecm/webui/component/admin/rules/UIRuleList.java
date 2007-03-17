/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.rules;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.services.cms.rules.RuleService;
import org.exoplatform.webui.component.UIGrid;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@yahoo.de
 * September 22, 2006
 * 16:37:15 
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/UIGridWithButton.gtmpl",
    events = {
        @EventConfig(listeners = UIRuleList.EditActionListener.class),
        @EventConfig(listeners = UIRuleList.DeleteActionListener.class),
        @EventConfig(listeners = UIRuleList.AddRuleActionListener.class)
    }
)

public class UIRuleList extends UIGrid {

  private static String[] BEAN_FIELD = {"name", "baseVersion"} ;
  private static String[] ACTIONS = {"Edit", "Delete"} ;

  public UIRuleList() throws Exception { 
    getUIPageIterator().setId("RuleListIterator") ;
    configure("name", BEAN_FIELD, ACTIONS) ;
  }

  public String[] getActions() { return new String[] {"AddRule"} ;}

  public void updateGrid() throws Exception {
    List<RuleData> ruleList = new ArrayList<RuleData>() ;    
    RuleService ruleService = getApplicationComponent(RuleService.class) ;
    NodeIterator nodes = ruleService.getRules();   
    RuleData rule = null ;
    while (nodes.hasNext()) {
      Node node = nodes.nextNode() ;
      String version = ""; 
      if(node.isNodeType("mix:versionable") && !node.isNodeType("nt:frozenNode")) {
        version = node.getBaseVersion().getName() ;
      }
      rule = new RuleData(node.getName(), version) ;
      ruleList.add(rule) ;    
    }
    ObjectPageList objPageList = new ObjectPageList(ruleList, 10) ;
    getUIPageIterator().setPageList(objPageList) ;
  } 

  static public class EditActionListener extends EventListener<UIRuleList> {
    public void execute(Event<UIRuleList> event) throws Exception {
      String ruleName = event.getRequestContext().getRequestParameter(OBJECTID);
      UIRuleManager uiRuleMan = event.getSource().getParent() ; 
      UIRuleForm uiForm ;
      if(uiRuleMan.getChild(UIPopupWindow.class) == null) {
        uiForm = uiRuleMan.createUIComponent(UIRuleForm.class, null, null) ;
      } else {
        uiForm = uiRuleMan.findFirstComponentOfType(UIRuleForm.class) ;
      }
      uiForm.update(ruleName) ;
      uiRuleMan.initPopup(uiForm) ;   
      event.getRequestContext().addUIComponentToUpdateByAjax(uiRuleMan) ;
    }
  }

  static public class AddRuleActionListener extends EventListener<UIRuleList> {
    public void execute(Event<UIRuleList> event) throws Exception {
      UIRuleManager uiRuleMan = event.getSource().getParent() ;
      UIRuleForm uiForm = uiRuleMan.findFirstComponentOfType(UIRuleForm.class) ;
      if (uiForm == null) uiForm = uiRuleMan.createUIComponent(UIRuleForm.class, null, null) ;
      uiForm.update(null) ;
      uiRuleMan.initPopup(uiForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiRuleMan) ;
    }
  }

  static public class DeleteActionListener extends EventListener<UIRuleList> {
    public void execute(Event<UIRuleList> event) throws Exception {
      UIRuleList uiRuleList = event.getSource() ;      
      String ruleName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      RuleService ruleService = uiRuleList.getApplicationComponent(RuleService.class) ;
      ruleService.removeRule(ruleName) ;  
      UIRuleManager parent = uiRuleList.getParent() ;
      parent.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiRuleList.getParent()) ;
    }
  }

  public static class RuleData {
    private String name ;
    private String baseVersion ;

    public RuleData(String ruleName, String version) {
      name = ruleName ;
      baseVersion = version ;
    }

    public String getName() { return name ; }
    public String getBaseVersion() { return baseVersion ; }
  }
}