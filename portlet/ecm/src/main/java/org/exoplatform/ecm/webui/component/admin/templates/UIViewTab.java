/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.templates;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Value;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIGrid;
import org.exoplatform.webui.component.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@yahoo.de
 * Oct 03, 2006
 * 9:43:23 AM 
 */
@ComponentConfig(
    lifecycle = UIContainerLifecycle.class,
    events = {
      @EventConfig(listeners = UIViewTab.EditActionListener.class),
      @EventConfig(listeners = UIViewTab.DeleteActionListener.class)
    }
)

public class UIViewTab extends UIContainer {

  final private static String[] BEAN_FIELD = {"name", "roles", "baseVersion"} ;
  final private static String[] ACTIONS = {"Edit", "Delete"} ;
  final public static String VIEW_LIST_NAME = "VewList" ;
  final public static String VIEW_FORM_NAME = "ViewForm" ;

  public UIViewTab() throws Exception { 
    UIGrid uiGrid = addChild(UIGrid.class, null, VIEW_LIST_NAME) ;
    uiGrid.getUIPageIterator().setId("ViewListIterator") ;
    uiGrid.configure("name", BEAN_FIELD, ACTIONS) ;
    UITemplateContent uiForm = addChild(UITemplateContent.class, null , VIEW_FORM_NAME) ;
    uiForm.setIsDialog(false) ;
    uiForm.update(null) ;
  }

  public void updateGrid(String nodeName) throws Exception {
    TemplateService tempService = getApplicationComponent(TemplateService.class) ;
    NodeIterator iter = tempService.getAllTemplatesOfNodeType(false, nodeName) ;
    List<ViewData> data = new ArrayList<ViewData>() ;
    ViewData item  ;
    while(iter.hasNext()) {
      Node node = (Node) iter.next() ;
      String version = "" ;
      StringBuilder rule = new StringBuilder() ;
      Value[] rules = node.getProperty("exo:roles").getValues() ;
      for(int i = 0; i < rules.length; i++) {
        rule.append("["+rules[i].getString()+"]") ;
      }
      if(node.isNodeType("mix:versionable") && !node.isNodeType("nt:frozenNode")) {
        version = node.getBaseVersion().getName();
      }
      item = new ViewData(node.getName(), rule.toString(), version) ;
      data.add(item);
    }
    UIGrid uiGrid = getChild(UIGrid.class) ;    
    ObjectPageList objDPageList = new ObjectPageList(data, 4) ;
    uiGrid.getUIPageIterator().setPageList(objDPageList) ;  
  }

  public void setTabRendered() {
    UIViewTemplate uiViewTemplate = getAncestorOfType(UIViewTemplate.class) ;
    uiViewTemplate.setRenderedChild(UIViewTab.class) ;
  }

  static public class EditActionListener extends EventListener<UIViewTab> {
    public void execute(Event<UIViewTab> event) throws Exception {
      UIViewTab viewTab = event.getSource() ; 
      String viewName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UITemplateContent uiForm = viewTab.getChild(UITemplateContent.class) ;
      uiForm.update(viewName) ;
      viewTab.setTabRendered() ;
      UITemplatesManager uiManager = viewTab.getAncestorOfType(UITemplatesManager.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }

  static public class DeleteActionListener extends EventListener<UIViewTab> {
    public void execute(Event<UIViewTab> event) throws Exception {
      UIViewTab viewTab = event.getSource() ;
      UIViewTemplate uiViewTemplate = event.getSource().getAncestorOfType(UIViewTemplate.class) ;
      String nodeTypeName = uiViewTemplate.getNodeTypeName() ;
      String templateName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      TemplateService templateService = viewTab.getApplicationComponent(TemplateService.class) ;
      for(String template : TemplateService.UNDELETABLE_TEMPLATES) {        
        if(template.equals(templateName)){
          UIApplication app = viewTab.getAncestorOfType(UIApplication.class) ;
          Object[] args = {template} ;
          app.addMessage(new ApplicationMessage("UIViewTab.msg.undeletable", args)) ;
          viewTab.setTabRendered() ;
          return ;
        }
      }
      templateService.removeTemplate(false, nodeTypeName, templateName) ;
      UITemplateContent uiForm = viewTab.findFirstComponentOfType(UITemplateContent.class) ;
      uiForm.update(null) ;
      viewTab.updateGrid(nodeTypeName) ;
      viewTab.setTabRendered() ;
      UITemplatesManager uiManager = viewTab.getAncestorOfType(UITemplatesManager.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }

  public static class ViewData {
    private String name ;
    private String roles ;
    private String baseVersion ;

    public ViewData(String name, String roles, String version) {
      this.name = name ;
      this.roles = roles ;
      baseVersion = version ;
    }
    public String getName(){return name ; } 
    public String getRoles(){return roles ; }
    public String getBaseVersion(){return baseVersion ; }
  } 
}