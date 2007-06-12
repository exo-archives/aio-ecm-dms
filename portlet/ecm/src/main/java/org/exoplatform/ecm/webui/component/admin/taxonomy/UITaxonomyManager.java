/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.taxonomy;

import javax.jcr.Node;

import org.exoplatform.ecm.jcr.model.ClipboardCommand;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.services.cms.categories.CategoriesService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Sep 29, 2006
 * 5:37:31 PM 
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/admin/taxonomy/UITaxonomyManager.gtmpl",
    events = {
        @EventConfig(listeners = UITaxonomyManager.SelectActionListener.class),
        @EventConfig(listeners = UITaxonomyManager.AddActionListener.class),
        @EventConfig(listeners = UITaxonomyManager.RemoveActionListener.class, confirm = "UITaxonomyManager.msg.confirm-delete"),
        @EventConfig(listeners = UITaxonomyManager.CopyActionListener.class),
        @EventConfig(listeners = UITaxonomyManager.PasteActionListener.class),
        @EventConfig(listeners = UITaxonomyManager.CutActionListener.class)
    }
)
public class UITaxonomyManager extends UIContainer {

  private ClipboardCommand clipboard_ = new ClipboardCommand() ;
  
  public UITaxonomyManager() throws Exception {}
  
  private String getRepository() {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    return pcontext.getRequest().getPreferences().getValue(Utils.REPOSITORY, "") ;
  }
  
  private TaxonomyNode getRootTaxonomyNode() throws Exception {     
    return new TaxonomyNode(getApplicationComponent(CategoriesService.class)
        .getTaxonomyHomeNode(getRepository()), 0) ; 
  }
  
  public void initPopup(String path) throws Exception {
    removeChildById("TaxonomyPopup") ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, "TaxonomyPopup") ;
    uiPopup.setWindowSize(600,250) ;
    UITaxonomyForm uiTaxoForm = createUIComponent(UITaxonomyForm.class, null, null) ;
    uiTaxoForm.setParent(path) ;
    uiPopup.setUIComponent(uiTaxoForm) ;
    uiPopup.setShow(true) ;
  }
  
  public void addTaxonomy(String parentPath, String name) throws Exception {
    getApplicationComponent(CategoriesService.class).addTaxonomy(parentPath, name, getRepository()) ;
  }
  
  static public class SelectActionListener extends EventListener<UITaxonomyManager> {
    public void execute(Event<UITaxonomyManager> event) throws Exception {
      UITaxonomyManager uiManager = event.getSource() ;
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      TaxonomyNode root = uiManager.getRootTaxonomyNode() ;
      TaxonomyNode selectedTaxonomy = root.findTaxonomyNode(path) ;
      selectedTaxonomy.setExpanded(!selectedTaxonomy.isExpanded()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  static public class AddActionListener extends EventListener<UITaxonomyManager> {
    public void execute(Event<UITaxonomyManager> event) throws Exception {
      UITaxonomyManager uiManager = event.getSource() ;
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ; 
      uiManager.initPopup(path) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  static public class RemoveActionListener extends EventListener<UITaxonomyManager> {
    public void execute(Event<UITaxonomyManager> event) throws Exception {
      UITaxonomyManager uiManager = event.getSource();     
      UIApplication uiApp = uiManager.getAncestorOfType(UIApplication.class) ;
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;            
      try {
        uiManager.getApplicationComponent(CategoriesService.class)
        .removeTaxonomyNode(path, uiManager.getRepository()) ;
      } catch(Exception e) {
        Object[] arg = { path } ;
        uiApp.addMessage(new ApplicationMessage("UITaxonomyManager.msg.path-error", arg)) ;
        return ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  static public class CopyActionListener extends EventListener<UITaxonomyManager> {
    public void execute(Event<UITaxonomyManager> event) throws Exception {
      UITaxonomyManager uiManager = event.getSource() ;
      String realPath = event.getRequestContext().getRequestParameter(OBJECTID);            
      Node node = uiManager.getApplicationComponent(CategoriesService.class)
      .getTaxonomyNode(realPath, uiManager.getRepository()) ;
      uiManager.clipboard_.setType(ClipboardCommand.COPY) ;
      uiManager.clipboard_.setSrcPath(node.getPath());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }

  static public class PasteActionListener extends EventListener<UITaxonomyManager> {
    public void execute(Event<UITaxonomyManager> event) throws Exception {
      UITaxonomyManager uiManager = event.getSource() ;
      String realPath = event.getRequestContext().getRequestParameter(OBJECTID);
      UIApplication uiApp = uiManager.getAncestorOfType(UIApplication.class) ;
      String type = uiManager.clipboard_.getType();
      String srcPath = uiManager.clipboard_.getSrcPath();
      if(srcPath == null){
        Object[] arg = { realPath } ;
        uiApp.addMessage(new ApplicationMessage("UITaxonomyManager.msg.no-taxonomy-selected", arg)) ;
        return ;
      }
      try {       
        Node destNode = uiManager.getApplicationComponent(CategoriesService.class)
        .getTaxonomyNode(realPath, uiManager.getRepository()) ;
        String destPath = destNode.getPath() + srcPath.substring(srcPath.lastIndexOf("/"));       
        uiManager.getApplicationComponent(CategoriesService.class)
        .moveTaxonomyNode(srcPath, destPath, type, uiManager.getRepository()) ;                        
      } catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UITaxonomyManager.msg.referential-integrity", null)) ;
        return ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  static public class CutActionListener extends EventListener<UITaxonomyManager> {
    public void execute(Event<UITaxonomyManager> event) throws Exception {
      UITaxonomyManager uiManager = event.getSource() ;
      String realPath = event.getRequestContext().getRequestParameter(OBJECTID);            
      Node node = uiManager.getApplicationComponent(CategoriesService.class)
      .getTaxonomyNode(realPath, uiManager.getRepository()) ;
      uiManager.clipboard_.setType(ClipboardCommand.CUT) ;
      uiManager.clipboard_.setSrcPath(node.getPath());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
}
