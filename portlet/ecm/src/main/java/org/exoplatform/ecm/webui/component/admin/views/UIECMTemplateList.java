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
        @EventConfig(listeners = UIECMTemplateList.DeleteActionListener.class),
        @EventConfig(listeners = UIECMTemplateList.EditInfoActionListener.class),
        @EventConfig(listeners = UIECMTemplateList.AddActionListener.class)
    }
)
public class UIECMTemplateList extends UIGrid {
  private static String[] VIEW_BEAN_FIELD = {"name", "path", "baseVersion"} ;
  private static String[] VIEW_ACTION = {"EditInfo","Delete"} ;
  
  public UIECMTemplateList() throws Exception {
    getUIPageIterator().setId("UIECMTemplateGrid") ;
    configure("path", VIEW_BEAN_FIELD, VIEW_ACTION) ;
    updateTempListGrid() ;
  }
  
  public String[] getActions() { return new String[] {"Add"} ; }
  
  public String getBaseVersion(Node node) throws Exception {
    if(!node.isNodeType("mix:versionable") || node.isNodeType("nt:frozenNode")) return "";
    return node.getBaseVersion().getName();    
  }
  
  public void updateTempListGrid() throws Exception {
    ManageViewService service = getApplicationComponent(ManageViewService.class) ;
    List<Node> nodes = service.getAllTemplates(BasePath.ECM_EXPLORER_TEMPLATES) ;
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
      UITemplateContainer uiECMTempContainer = uiViewManager.getChildById("ECMTemplate") ;
      uiECMTempContainer.initPopup("ECMTempForm") ;
      uiViewManager.setRenderedChild("ECMTemplate") ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiECMTempContainer) ;
    }
  }
  
  static  public class DeleteActionListener extends EventListener<UIECMTemplateList> {
    public void execute(Event<UIECMTemplateList> event) throws Exception {
      UIECMTemplateList uiECMTemp = event.getSource() ;
      ManageViewService service = uiECMTemp.getApplicationComponent(ManageViewService.class) ;
      UIViewManager uiViewManager = uiECMTemp.getAncestorOfType(UIViewManager.class) ;
      uiViewManager.setRenderedChild("ECMTemplate") ;
      String templatePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      service.removeTemplate(templatePath) ;
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
      uiTempContainer.initPopup("ECMTempForm") ;
      UITemplateForm uiTempForm = uiTempContainer.findComponentById("ECMTempForm") ;
      uiTempForm.update(tempPath, null) ;
      uiViewManager.setRenderedChild("ECMTemplate") ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTempContainer) ;
    }
  }
}
