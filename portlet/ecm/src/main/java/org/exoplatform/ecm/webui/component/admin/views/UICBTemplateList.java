/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.views;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.webui.component.UIGrid;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
        @EventConfig (listeners = UICBTemplateList.DeleteActionListener.class),
        @EventConfig (listeners = UICBTemplateList.EditInfoActionListener.class),
        @EventConfig (listeners = UICBTemplateList.AddActionListener.class)
    }
)
public class UICBTemplateList extends UIGrid {
  private static String[] VIEW_BEAN_FIELD = {"name", "path", "baseVersion"} ;
  private static String[] VIEW_ACTION = {"EditInfo","Delete"} ;
  
  public UICBTemplateList() throws Exception {
    getUIPageIterator().setId("UICBTemplateGrid") ;
    configure("path", VIEW_BEAN_FIELD, VIEW_ACTION) ;
    updateCBTempListGrid() ;
  }
  
  public String[] getActions() { return new String[] {"Add"} ; }
  
  public String getBaseVersion(Node node) throws Exception {
    if(!node.isNodeType("mix:versionable") || node.isNodeType("nt:frozenNode")) return "";
    return node.getBaseVersion().getName();    
  }
  
  public List<Node> getAllTemplates() throws Exception {
    ManageViewService viewService = getApplicationComponent(ManageViewService.class) ;
    List<Node> templateList = new ArrayList<Node>() ;
    templateList.addAll(viewService.getAllTemplates(BasePath.CB_DETAIL_VIEW_TEMPLATES)) ;
    templateList.addAll(viewService.getAllTemplates(BasePath.CB_PATH_TEMPLATES)) ;
    templateList.addAll(viewService.getAllTemplates(BasePath.CB_QUERY_TEMPLATES)) ;
    templateList.addAll(viewService.getAllTemplates(BasePath.CB_SCRIPT_TEMPLATES)) ;
    return templateList ;
  }
  
  public void updateCBTempListGrid() throws Exception {
    List<Node> nodes = getAllTemplates() ;
    List<TemplateBean> tempBeans = new ArrayList<TemplateBean>() ;
    for(Node node : nodes) {
      tempBeans.add(new TemplateBean(node.getName(), node.getPath(), getBaseVersion(node))) ;
    }
    getUIPageIterator().setPageList(new ObjectPageList(tempBeans, 10)) ;
  }
  
  static  public class AddActionListener extends EventListener<UICBTemplateList> {
    public void execute(Event<UICBTemplateList> event) throws Exception {
      UICBTemplateList uiCBTemp = event.getSource() ;
      UIViewManager uiViewManager = uiCBTemp.getAncestorOfType(UIViewManager.class) ;
      UITemplateContainer uiECMTempContainer = uiViewManager.getChildById("CBTemplate") ;
      uiECMTempContainer.initPopup("CBTempForm") ;
      uiViewManager.setRenderedChild("CBTemplate") ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiECMTempContainer) ;
    }
  }
  
  static  public class DeleteActionListener extends EventListener<UICBTemplateList> {
    public void execute(Event<UICBTemplateList> event) throws Exception {
      UICBTemplateList uiCBTemp = event.getSource() ;
      ManageViewService viewService = uiCBTemp.getApplicationComponent(ManageViewService.class) ;
      String templatePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      viewService.removeTemplate(templatePath) ;
      uiCBTemp.updateCBTempListGrid() ;
      uiCBTemp.setRenderSibbling(UICBTemplateList.class);
      UIViewManager uiViewManager = uiCBTemp.getAncestorOfType(UIViewManager.class) ;
      uiViewManager.setRenderedChild("CBTemplate") ;
      UITemplateContainer uiTempContainer = uiViewManager.getChildById("CBTemplate") ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTempContainer) ;
    }
  }

  static  public class EditInfoActionListener extends EventListener<UICBTemplateList> {
    public void execute(Event<UICBTemplateList> event) throws Exception {
      UICBTemplateList uiCBTemp = event.getSource() ;
      String tempPath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIViewManager uiViewManager = uiCBTemp.getAncestorOfType(UIViewManager.class) ;
      UITemplateContainer uiTempContainer = uiViewManager.getChildById("CBTemplate") ;
      uiTempContainer.initPopup("CBTempForm") ;
      UITemplateForm uiTempForm = uiTempContainer.findComponentById("CBTempForm") ;
      uiTempForm.update(tempPath, null) ;
      uiViewManager.setRenderedChild("CBTemplate") ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTempContainer) ;
    }
  }
}
