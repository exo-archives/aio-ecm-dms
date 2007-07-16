/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.templates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jcr.NodeIterator;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIGrid;
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
      @EventConfig(listeners = UITemplateList.EditActionListener.class),
      @EventConfig(listeners = UITemplateList.DeleteActionListener.class, confirm = "UITemplateList.msg.confirm-delete"),
      @EventConfig(listeners = UITemplateList.AddNewActionListener.class)
    }
)

public class UITemplateList extends UIGrid {
  
  private static String[] NODETYPE_BEAN_FIELD = {"name"} ;
  private static String[] NODETYPE_ACTION = {"Edit", "Delete"} ;
  
  public UITemplateList() throws Exception {
    getUIPageIterator().setId("NodeTypeListIterator") ;
    configure("name", NODETYPE_BEAN_FIELD, NODETYPE_ACTION) ;
  }
  
  public String[] getActions() {
    return new String[] {"AddNew"} ;
  }
  
  @SuppressWarnings("unchecked")
  public void updateGrid() throws Exception {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    NodeIterator nodes = templateService.getTemplatesHome(repository).getNodes() ;
    List<TemplateData> templateData = new ArrayList<TemplateData>() ;
    while (nodes.hasNext()) {
      templateData.add(new TemplateData(nodes.nextNode().getName())) ;
    }
    Collections.sort(templateData, new TemplateComparator()) ;
    ObjectPageList objPageList = new ObjectPageList(templateData, 10) ;
    getUIPageIterator().setPageList(objPageList) ;
  }
  
  static public class TemplateComparator implements Comparator {
    public int compare(Object o1, Object o2) throws ClassCastException {
      String name1 = ((TemplateData) o1).getName() ;
      String name2 = ((TemplateData) o2).getName() ;
      return name1.compareToIgnoreCase(name2) ;
    }
  }

  static public class EditActionListener extends EventListener<UITemplateList> {
    public void execute(Event<UITemplateList> event) throws Exception {
      UITemplateList nodeTypeList = event.getSource() ;
      String repository = nodeTypeList.getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
      String nodeType = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UITemplatesManager uiTemplatesManager = nodeTypeList.getParent() ;
      UIViewTemplate uiViewTemplate = uiTemplatesManager.createUIComponent(UIViewTemplate.class, null, null) ;
      uiViewTemplate.getChild(UITemplateEditForm.class).update(nodeType) ;
      uiViewTemplate.setNodeTypeName(nodeType) ;
      UIDialogTab uiDialogTab = uiViewTemplate.findFirstComponentOfType(UIDialogTab.class) ;
      uiDialogTab.updateGrid(nodeType, repository) ;
      UITemplateContent uiDialogTabForm = uiViewTemplate.findComponentById(UIDialogTab.DIALOG_FORM_NAME) ;
      uiDialogTabForm.setNodeTypeName(nodeType) ;
      uiDialogTabForm.update(null) ;
      UIViewTab uiViewTab = uiViewTemplate.findFirstComponentOfType(UIViewTab.class) ;
      uiViewTab.updateGrid(nodeType) ;
      UITemplateContent uiViewTabForm = uiViewTemplate.findComponentById(UIViewTab.VIEW_FORM_NAME) ;
      uiViewTabForm.setNodeTypeName(nodeType) ;
      uiViewTabForm.update(null) ;     
      uiTemplatesManager.removeChildById(UITemplatesManager.NEW_TEMPLATE) ;
      uiTemplatesManager.initPopup(uiViewTemplate, UITemplatesManager.EDIT_TEMPLATE) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTemplatesManager) ;
    }
  }

  static public class DeleteActionListener extends EventListener<UITemplateList> {
    public void execute(Event<UITemplateList> event) throws Exception {
      UITemplateList nodeTypeList = event.getSource() ;
      String nodeType = event.getRequestContext().getRequestParameter(OBJECTID) ;
      TemplateService templateService = nodeTypeList.getApplicationComponent(TemplateService.class) ;
      String repository = nodeTypeList.getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
      templateService.removeManagedNodeType(nodeType, repository) ;
      nodeTypeList.updateGrid() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(nodeTypeList.getParent()) ;
    }
  }
  
  static public class AddNewActionListener extends EventListener<UITemplateList> {
    public void execute(Event<UITemplateList> event) throws Exception {
      UITemplatesManager uiTemplatesManager = event.getSource().getAncestorOfType(UITemplatesManager.class) ;
      UITemplateForm uiTemplateForm = uiTemplatesManager.createUIComponent(UITemplateForm.class, null, null) ;
      uiTemplatesManager.removeChildById(UITemplatesManager.EDIT_TEMPLATE) ;
      uiTemplatesManager.initPopup(uiTemplateForm, UITemplatesManager.NEW_TEMPLATE) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTemplatesManager) ;
    }
  }
  
  static public class TemplateData {
    private String name ;

    public TemplateData(String temp ) { name = temp ;}
    public String getName() { return name ;}
  }
}