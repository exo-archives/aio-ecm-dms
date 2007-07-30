/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.utils.SessionsUtils;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
        @EventConfig (listeners = UICBTemplateList.DeleteActionListener.class, confirm = "UICBTemplateList.msg.confirm-delete"),
        @EventConfig (listeners = UICBTemplateList.EditInfoActionListener.class),
        @EventConfig (listeners = UICBTemplateList.AddActionListener.class)
    }
)
public class UICBTemplateList extends UIGrid {
  private static String[] VIEW_BEAN_FIELD = {"name", "path", "baseVersion"} ;
  private static String[] VIEW_ACTION = {"EditInfo","Delete"} ;
  public static String ST_CBTempForm = "CBTempForm" ;  
  public static String ST_CBTemp = "CBTemplate" ;
  
  public UICBTemplateList() throws Exception {
    getUIPageIterator().setId("UICBTemplateGrid") ;
    configure("path", VIEW_BEAN_FIELD, VIEW_ACTION) ;
    //updateCBTempListGrid() ;
  }
  public String[] getActions() { return new String[] {"Add"} ; }
  public String getBaseVersion(Node node) throws Exception {
    if(!node.isNodeType(Utils.MIX_VERSIONABLE) || node.isNodeType(Utils.NT_FROZEN)) return "";
    return node.getBaseVersion().getName();    
  }
  
  public List<Node> getAllTemplates() throws Exception {
    ManageViewService viewService = getApplicationComponent(ManageViewService.class) ;
    List<Node> templateList = new ArrayList<Node>() ;
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    SessionProvider provider = SessionsUtils.getSessionProvider() ;
    templateList.addAll(viewService.getAllTemplates(BasePath.CB_DETAIL_VIEW_TEMPLATES, repository,provider)) ;
    templateList.addAll(viewService.getAllTemplates(BasePath.CB_PATH_TEMPLATES, repository,provider)) ;
    templateList.addAll(viewService.getAllTemplates(BasePath.CB_QUERY_TEMPLATES, repository,provider)) ;
    templateList.addAll(viewService.getAllTemplates(BasePath.CB_SCRIPT_TEMPLATES,repository,provider)) ;
    return templateList ;
  }
  
  @SuppressWarnings("unchecked")
  public void updateCBTempListGrid() throws Exception {
    List<Node> nodes = getAllTemplates() ;
    List<TemplateBean> tempBeans = new ArrayList<TemplateBean>() ;
    for(Node node : nodes) {
      tempBeans.add(new TemplateBean(node.getName(), node.getPath(), getBaseVersion(node))) ;
    }
    Collections.sort(tempBeans, new CBViewComparator()) ;
    getUIPageIterator().setPageList(new ObjectPageList(tempBeans, 10)) ;
  }
  
  static public class CBViewComparator implements Comparator {
    public int compare(Object o1, Object o2) throws ClassCastException {
      String name1 = ((TemplateBean) o1).getName() ;
      String name2 = ((TemplateBean) o2).getName() ;
      return name1.compareToIgnoreCase(name2) ;
    }
  }
  
  static  public class AddActionListener extends EventListener<UICBTemplateList> {
    public void execute(Event<UICBTemplateList> event) throws Exception {
      UICBTemplateList uiCBTemp = event.getSource() ;
      UIViewManager uiViewManager = uiCBTemp.getAncestorOfType(UIViewManager.class) ;
      UITemplateContainer uiECMTempContainer = uiViewManager.getChildById(UICBTemplateList.ST_CBTemp) ;
      uiECMTempContainer.removeChildById(UICBTemplateList.ST_CBTempForm + "Edit") ;
      uiECMTempContainer.initPopup(UICBTemplateList.ST_CBTempForm, "Add") ;
      uiViewManager.setRenderedChild(UICBTemplateList.ST_CBTemp) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiECMTempContainer) ;
    }
  }
  
  static  public class DeleteActionListener extends EventListener<UICBTemplateList> {
    public void execute(Event<UICBTemplateList> event) throws Exception {
      UICBTemplateList uiCBTemp = event.getSource() ;
      String repository = uiCBTemp.getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
      String templatePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiCBTemp.getApplicationComponent(ManageViewService.class).removeTemplate(templatePath, repository) ;
      uiCBTemp.updateCBTempListGrid() ;
      uiCBTemp.setRenderSibbling(UICBTemplateList.class);
      UIViewManager uiViewManager = uiCBTemp.getAncestorOfType(UIViewManager.class) ;
      uiViewManager.setRenderedChild(UICBTemplateList.ST_CBTemp) ;
      UITemplateContainer uiTempContainer = uiViewManager.getChildById(UICBTemplateList.ST_CBTemp) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTempContainer) ;
    }
  }

  static  public class EditInfoActionListener extends EventListener<UICBTemplateList> {
    public void execute(Event<UICBTemplateList> event) throws Exception {
      UICBTemplateList uiCBTemp = event.getSource() ;
      String tempPath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIViewManager uiViewManager = uiCBTemp.getAncestorOfType(UIViewManager.class) ;
      UITemplateContainer uiTempContainer = uiViewManager.getChildById(UICBTemplateList.ST_CBTemp) ;
      uiTempContainer.removeChildById(UICBTemplateList.ST_CBTempForm + "Add") ;
      uiTempContainer.initPopup(UICBTemplateList.ST_CBTempForm, "Edit") ;
      UITemplateForm uiTempForm = uiTempContainer.findComponentById(UICBTemplateList.ST_CBTempForm) ;
      uiTempForm.isAddNew_ = false ;
      uiTempForm.update(tempPath, null) ;
      uiViewManager.setRenderedChild(UICBTemplateList.ST_CBTemp) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTempContainer) ;
    }
  }
}
