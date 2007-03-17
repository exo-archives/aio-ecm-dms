/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.templates;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.NodeIterator;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.webui.component.UIGrid;
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
    template = "app:/groovy/webui/component/UIGridWithButton.gtmpl",
    events = {
      @EventConfig(listeners = UINodeTypeList.ViewActionListener.class),
      @EventConfig(listeners = UINodeTypeList.DeleteActionListener.class),
      @EventConfig(listeners = UINodeTypeList.AddNewActionListener.class)
    }
)

public class UINodeTypeList extends UIGrid {
  
  private static String[] NODETYPE_BEAN_FIELD = {"name"} ;
  private static String[] NODETYPE_ACTION = {"View", "Delete"} ;
  
  public UINodeTypeList() throws Exception {
    getUIPageIterator().setId("NodeTypeListIterator") ;
    configure("name", NODETYPE_BEAN_FIELD, NODETYPE_ACTION) ;
  }
  
  public String[] getActions() {
    return new String[] {"AddNew"} ;
  }
  
  public void updateGrid() throws Exception {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    NodeIterator nodes = templateService.getTemplatesHome().getNodes() ;
    List<TemplateData> templateData = new ArrayList<TemplateData>() ;
    while (nodes.hasNext()) {
      templateData.add(new TemplateData(nodes.nextNode().getName())) ;
    }
    ObjectPageList objPageList = new ObjectPageList(templateData, 10) ;
    getUIPageIterator().setPageList(objPageList) ;
  }

  static public class ViewActionListener extends EventListener<UINodeTypeList> {
    public void execute(Event<UINodeTypeList> event) throws Exception {
      UINodeTypeList nodeTypeList = event.getSource() ;
      String nodeType = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UITemplatesManager uiTemplatesManager = nodeTypeList.getParent() ;
      UIViewTemplate uiViewTemplate = uiTemplatesManager.createUIComponent(UIViewTemplate.class, null, null) ;
      uiViewTemplate.getChild(UITemplateEditForm.class).update(nodeType) ;
      uiViewTemplate.setNodeTypeName(nodeType) ;
      UIDialogTab uiDialogTab = uiViewTemplate.findFirstComponentOfType(UIDialogTab.class) ;
      uiDialogTab.updateGrid(nodeType) ;
      UITemplateContent uiDialogTabForm = uiViewTemplate.findComponentById(UIDialogTab.DIALOG_FORM_NAME) ;
      uiDialogTabForm.setNodeTypeName(nodeType) ;
      uiDialogTabForm.update(null) ;
      UIViewTab uiViewTab = uiViewTemplate.findFirstComponentOfType(UIViewTab.class) ;
      uiViewTab.updateGrid(nodeType) ;
      UITemplateContent uiViewTabForm = uiViewTemplate.findComponentById(UIViewTab.VIEW_FORM_NAME) ;
      uiViewTabForm.setNodeTypeName(nodeType) ;
      uiViewTabForm.update(null) ;      
      uiTemplatesManager.initPopup(uiViewTemplate, null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTemplatesManager) ;
    }
  }

  static public class DeleteActionListener extends EventListener<UINodeTypeList> {
    public void execute(Event<UINodeTypeList> event) throws Exception {
      UINodeTypeList nodeTypeList = event.getSource() ;
      String nodeType = event.getRequestContext().getRequestParameter(OBJECTID) ;
      TemplateService templateService = nodeTypeList.getApplicationComponent(TemplateService.class) ;
      templateService.removeManagedNodeType(nodeType) ;
      nodeTypeList.updateGrid() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(nodeTypeList.getParent()) ;
    }
  }
  
  static public class AddNewActionListener extends EventListener<UINodeTypeList> {
    public void execute(Event<UINodeTypeList> event) throws Exception {
      UITemplatesManager uiTemplatesManager = event.getSource().getAncestorOfType(UITemplatesManager.class) ;
      UITemplateForm uiTemplateForm = uiTemplatesManager.createUIComponent(UITemplateForm.class, null, null) ;
      uiTemplatesManager.initPopup(uiTemplateForm, "TemplatePopup") ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTemplatesManager) ;
    }
  }
  
  static public class TemplateData {
    private String name ;

    public TemplateData(String temp ) { name = temp ;}
    public String getName() { return name ;}
  }
}