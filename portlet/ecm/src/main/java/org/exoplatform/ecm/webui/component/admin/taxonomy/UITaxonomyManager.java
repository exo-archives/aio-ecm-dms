/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.taxonomy;

import javax.jcr.Node;

import org.exoplatform.ecm.jcr.model.ClipboardCommand;
import org.exoplatform.services.cms.categories.CategoriesService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
        @EventConfig(listeners = UITaxonomyManager.RemoveActionListener.class),
        @EventConfig(listeners = UITaxonomyManager.CopyActionListener.class),
        @EventConfig(listeners = UITaxonomyManager.PasteActionListener.class),
        @EventConfig(listeners = UITaxonomyManager.CutActionListener.class)
    }
)
public class UITaxonomyManager extends UIContainer {

  private CategoriesService categoriesService_ ;
  private TaxonomyNode rootTaxonnomy_ ;
  private ClipboardCommand clipboard_ = new ClipboardCommand() ;
  
  public UITaxonomyManager() throws Exception {
    categoriesService_ = getApplicationComponent(CategoriesService.class) ;
    rootTaxonnomy_ = new TaxonomyNode(categoriesService_.getTaxonomyHomeNode(), 0) ;
  }
  
  private TaxonomyNode getRootTaxonomyNode() throws Exception {  return rootTaxonnomy_ ; }
  
  private void resetTaxonomyRoot() throws Exception {
    rootTaxonnomy_ = new TaxonomyNode(categoriesService_.getTaxonomyHomeNode(), 0) ;    
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
    categoriesService_.addTaxonomy(parentPath, name) ;
    resetTaxonomyRoot() ;
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
        uiManager.categoriesService_.removeTaxonomyNode(path) ;
      } catch(Exception e) {
        Object[] arg = { path } ;
        uiApp.addMessage(new ApplicationMessage("UITaxonomyManager.msg.path-error", arg)) ;
        return ;
      }
      uiManager.resetTaxonomyRoot() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  static public class CopyActionListener extends EventListener<UITaxonomyManager> {
    public void execute(Event<UITaxonomyManager> event) throws Exception {
      UITaxonomyManager uiManager = event.getSource() ;
      String realPath = event.getRequestContext().getRequestParameter(OBJECTID);            
      Node node = uiManager.categoriesService_.getTaxonomyNode(realPath) ;
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
        Node destNode = uiManager.categoriesService_.getTaxonomyNode(realPath) ;
        String destPath = destNode.getPath() + srcPath.substring(srcPath.lastIndexOf("/"));       
        uiManager.categoriesService_.moveTaxonomyNode(srcPath,destPath,type) ;                        
      } catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UITaxonomyManager.msg.referential-integrity", null)) ;
        return ;
      }
      uiManager.resetTaxonomyRoot() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  static public class CutActionListener extends EventListener<UITaxonomyManager> {
    public void execute(Event<UITaxonomyManager> event) throws Exception {
      UITaxonomyManager uiManager = event.getSource() ;
      String realPath = event.getRequestContext().getRequestParameter(OBJECTID);            
      Node node = uiManager.categoriesService_.getTaxonomyNode(realPath) ;
      uiManager.clipboard_.setType(ClipboardCommand.CUT) ;
      uiManager.clipboard_.setSrcPath(node.getPath());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
}
