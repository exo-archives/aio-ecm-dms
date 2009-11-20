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
package org.exoplatform.ecm.webui.component.admin.templates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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
  public void updateGrid(int currentPage) throws Exception {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    Node templatesHome = templateService.getTemplatesHome(repository, 
        SessionProviderFactory.createSessionProvider()) ;
    List<TemplateData> templateData = new ArrayList<TemplateData>() ;
    if(templatesHome != null) {
      NodeTypeManager ntManager = templatesHome.getSession().getWorkspace().getNodeTypeManager();
      NodeTypeIterator nodetypeIter = ntManager.getAllNodeTypes();
      List<String> listNodeTypeName = new ArrayList<String>();
      while (nodetypeIter.hasNext()) {
        NodeType n1 = nodetypeIter.nextNodeType();
        listNodeTypeName.add(n1.getName());
      }
      NodeIterator nodes = templatesHome.getNodes() ;
      while (nodes.hasNext()) {
        Node node  = nodes.nextNode();
        if (listNodeTypeName.contains(node.getName())) {
          templateData.add(new TemplateData(node.getName()));
        }
      }
      Collections.sort(templateData, new TemplateComparator()) ;
    } 
    ObjectPageList objPageList = new ObjectPageList(templateData, 10) ;
    getUIPageIterator().setPageList(objPageList) ;
    if(currentPage > getUIPageIterator().getAvailablePage())
      getUIPageIterator().setCurrentPage(currentPage-1);
    else
      getUIPageIterator().setCurrentPage(currentPage);
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
      UISkinTab uiSkinTab = uiViewTemplate.findFirstComponentOfType(UISkinTab.class) ;
      uiSkinTab.updateGrid(nodeType, repository) ;
      UITemplateContent uiSkinTabForm = uiViewTemplate.findComponentById(UISkinTab.SKIN_FORM_NAME) ;
      uiSkinTabForm.setNodeTypeName(nodeType) ;
      uiSkinTabForm.update(null) ;
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
      nodeTypeList.updateGrid(nodeTypeList.getUIPageIterator().getCurrentPage()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(nodeTypeList.getParent()) ;
    }
  }
  
  static public class AddNewActionListener extends EventListener<UITemplateList> {
    public void execute(Event<UITemplateList> event) throws Exception {
      UITemplatesManager uiTemplatesManager = event.getSource().getAncestorOfType(UITemplatesManager.class) ;
      UITemplateForm uiTemplateForm = uiTemplatesManager.createUIComponent(UITemplateForm.class, null, null) ;
      uiTemplatesManager.removeChildById(UITemplatesManager.EDIT_TEMPLATE) ;
      if(uiTemplateForm.getOption().size() == 0) {
        UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UITemplateList.msg.access-denied", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
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