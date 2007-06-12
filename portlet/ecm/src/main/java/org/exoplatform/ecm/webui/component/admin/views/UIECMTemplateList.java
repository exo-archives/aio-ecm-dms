/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.views;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@exoplatform.com
 * Sep 19, 2006
 * 11:45:11 AM 
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/UIGridWithButton.gtmpl",
    events = {
        @EventConfig(listeners = UIECMTemplateList.DeleteActionListener.class, confirm = "UIECMTemplateList.msg.confirm-delete"),
        @EventConfig(listeners = UIECMTemplateList.EditInfoActionListener.class),
        @EventConfig(listeners = UIECMTemplateList.AddActionListener.class)
    }
)
public class UIECMTemplateList extends UIGrid {
  private static String[] VIEW_BEAN_FIELD = {"name", "path", "baseVersion"} ;
  private static String[] VIEW_ACTION = {"EditInfo","Delete"} ;
  public static String ST_ECMTempForm = "ECMTempForm" ;  
  public static String ST_ECMTemp = "ECMTemplate" ;

  public UIECMTemplateList() throws Exception {
    getUIPageIterator().setId("UIECMTemplateGrid") ;
    configure("path", VIEW_BEAN_FIELD, VIEW_ACTION) ;
  }

  public String[] getActions() { return new String[] {"Add"} ; }

  public String getBaseVersion(Node node) throws Exception {
    if(!node.isNodeType(Utils.MIX_VERSIONABLE) || node.isNodeType(Utils.NT_FROZEN)) return "";
    return node.getBaseVersion().getName();    
  }

  public void updateTempListGrid() throws Exception {
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    List<Node> nodes = getApplicationComponent(ManageViewService.class)
                      .getAllTemplates(BasePath.ECM_EXPLORER_TEMPLATES, repository) ;
    List<TemplateBean> tempBeans = new ArrayList<TemplateBean>() ;
    for(Node node : nodes) {
      tempBeans.add(new TemplateBean(node.getName(), node.getPath(), getBaseVersion(node))) ;
    }
    getUIPageIterator().setPageList(new ObjectPageList(tempBeans, 10)) ;
  }

  static  public class AddActionListener extends EventListener<UIECMTemplateList> {
    public void execute(Event<UIECMTemplateList> event) throws Exception {
      UIECMTemplateList uiECMTempList = event.getSource() ;
      UIViewManager uiViewManager = uiECMTempList.getAncestorOfType(UIViewManager.class) ;
      UITemplateContainer uiECMTempContainer = uiViewManager.getChildById(UIECMTemplateList.ST_ECMTemp) ;
      uiECMTempContainer.removeChildById(UIECMTemplateList.ST_ECMTempForm + "Edit") ;
      uiECMTempContainer.initPopup(UIECMTemplateList.ST_ECMTempForm, "Add") ;
      uiViewManager.setRenderedChild(UIECMTemplateList.ST_ECMTemp) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiECMTempContainer) ;
    }
  }

  static  public class DeleteActionListener extends EventListener<UIECMTemplateList> {
    public void execute(Event<UIECMTemplateList> event) throws Exception {
      UIECMTemplateList uiECMTemp = event.getSource() ;
      String repository = uiECMTemp.getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
      ManageViewService vservice = uiECMTemp.getApplicationComponent(ManageViewService.class) ;
      UIViewManager uiViewManager = uiECMTemp.getAncestorOfType(UIViewManager.class) ;
      uiViewManager.setRenderedChild(UIECMTemplateList.ST_ECMTemp) ;
      String templatePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String templateName = templatePath.substring(templatePath.lastIndexOf("/") + 1) ;
      if(!uiECMTemp.getApplicationComponent(ManageDriveService.class).isUsedView(templateName, repository)) {
        UIApplication app = uiECMTemp.getAncestorOfType(UIApplication.class) ;
        Object[] args = {templateName} ;
        app.addMessage(new ApplicationMessage("UIECMTemplateList.msg.template-in-use", args)) ;
        return ;
      }
      vservice.removeTemplate(templatePath, repository) ;
      uiECMTemp.updateTempListGrid() ;
      UITemplateContainer uiTempContainer = uiECMTemp.getParent() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTempContainer) ;
    }
  }

  static  public class EditInfoActionListener extends EventListener<UIECMTemplateList> {
    public void execute(Event<UIECMTemplateList> event) throws Exception {
      UIECMTemplateList uiECMTemp = event.getSource() ;
      String tempPath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UITemplateContainer uiTempContainer = uiECMTemp.getParent() ;
      UIViewManager uiViewManager = uiECMTemp.getAncestorOfType(UIViewManager.class) ;
      uiTempContainer.removeChildById(UIECMTemplateList.ST_ECMTempForm + "Add") ;
      uiTempContainer.initPopup(UIECMTemplateList.ST_ECMTempForm, "Edit") ;
      UITemplateForm uiTempForm = uiTempContainer.findComponentById(UIECMTemplateList.ST_ECMTempForm) ;
      uiTempForm.update(tempPath, null) ;
      uiViewManager.setRenderedChild(UIECMTemplateList.ST_ECMTemp) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTempContainer) ;
    }
  }
}
