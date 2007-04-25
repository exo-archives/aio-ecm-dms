/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.versions;

import javax.jcr.Node;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.jcr.model.VersionNode;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.portal.component.view.UIContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Implement: lxchiati
 *            lebienthuy@gmail.com
 * July 3, 2006
 * 10:07:15 AM
 */

@ComponentConfig(
    template = "app:/groovy/webui/component/explorer/versions/UIVersionInfo.gtmpl",
    events = {
        @EventConfig(listeners = UIVersionInfo.SelectActionListener.class),
        @EventConfig(listeners = UIVersionInfo.RestoreVersionActionListener.class),        
        @EventConfig(listeners = UIVersionInfo.ViewVersionActionListener.class),
        @EventConfig(listeners = UIVersionInfo.AddLabelActionListener.class),
        @EventConfig(listeners = UIVersionInfo.CompareVersionActionListener.class),
        @EventConfig(listeners = UIVersionInfo.DeleteVersionActionListener.class),
        @EventConfig(listeners = UIVersionInfo.RemoveLabelActionListener.class),
        @EventConfig(listeners = UIVersionInfo.CloseActionListener.class)        
    }
)

public class UIVersionInfo extends UIContainer implements UIPopupComponent {

  protected VersionNode rootVersion_ ;
  protected VersionNode curentVersion_;
  protected Node node_ ;
  
  public UIVersionInfo() throws Exception {
    addChild(UILabelForm.class, null, null).setRendered(false);   
    addChild(UIRemoveLabelForm.class, null, null).setRendered(false);
    addChild(UIViewVersion.class, null, null).setRendered(false);
  }

  public String[] getVersionLabels(VersionNode version) throws Exception {
    VersionHistory vH = node_.getVersionHistory();
    return vH.getVersionLabels(version.getVersion());   
  }

  public boolean isBaseVersion(VersionNode versionNode) throws Exception {
    if( node_.getBaseVersion().getName().equals(versionNode.getVersion().getName())) return true ;
    return false ;
  }

  public VersionNode getRootVersionNode() throws Exception {  return rootVersion_ ; }

  public void activate() throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    node_ = uiExplorer.getCurrentNode() ;    
    rootVersion_ = new VersionNode(node_.getVersionHistory().getRootVersion()) ;
    curentVersion_ = rootVersion_ ;
    getChild(UIViewVersion.class).update() ;  
  }
  
  public void deActivate() throws Exception {}  
  
  public VersionNode getCurrentVersionNode() { return curentVersion_ ;}
  public Node getCurrentNode() { return node_ ; }

  static  public class ViewVersionActionListener extends EventListener<UIVersionInfo> {
    public void execute(Event<UIVersionInfo> event) throws Exception {
      UIVersionInfo uiVersionInfo = event.getSource();
      for(UIComponent uiChild : uiVersionInfo.getChildren()) {
        uiChild.setRendered(false) ;
      }
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiVersionInfo.curentVersion_  = uiVersionInfo.rootVersion_.findVersionNode(objectId) ;
      UIViewVersion uiViewVerion = uiVersionInfo.getChild(UIViewVersion.class) ;
      Version version_ = uiVersionInfo.curentVersion_.getVersion() ;
      Node frozenNode = version_.getNode("jcr:frozenNode") ;
      uiViewVerion.setNode(frozenNode) ;
      uiViewVerion.setRendered(true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiVersionInfo) ;
    }
  }

  static  public class AddLabelActionListener extends EventListener<UIVersionInfo> {
    public void execute(Event<UIVersionInfo> event) throws Exception {      
      UIVersionInfo uiVersionInfo = event.getSource();
      for(UIComponent uiChild : uiVersionInfo.getChildren()) {
        uiChild.setRendered(false) ;
      }
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiVersionInfo.curentVersion_  = uiVersionInfo.rootVersion_.findVersionNode(objectId) ;      
      uiVersionInfo.getChild(UILabelForm.class).setRendered(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiVersionInfo) ;
    }
  }

  static  public class RemoveLabelActionListener extends EventListener<UIVersionInfo> {
    public void execute(Event<UIVersionInfo> event) throws Exception {      
      UIVersionInfo uiVersionInfo = event.getSource();
      for(UIComponent uiChild : uiVersionInfo.getChildren()) {
        uiChild.setRendered(false) ;
      }
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiVersionInfo.curentVersion_  = uiVersionInfo.rootVersion_.findVersionNode(objectId) ;      
      uiVersionInfo.getChild(UIRemoveLabelForm.class).update() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiVersionInfo) ;
    }
  }

  static  public class RestoreVersionActionListener extends EventListener<UIVersionInfo> {
    public void execute(Event<UIVersionInfo> event) throws Exception {      
      UIVersionInfo uiVersionInfo = event.getSource();
      for(UIComponent uiChild : uiVersionInfo.getChildren()) {
        uiChild.setRendered(false) ;
      }
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiVersionInfo.curentVersion_  = uiVersionInfo.rootVersion_.findVersionNode(objectId) ;
      uiVersionInfo.node_.restore(uiVersionInfo.curentVersion_.getVersion(), true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiVersionInfo) ;
    }
  }

  static  public class DeleteVersionActionListener extends EventListener<UIVersionInfo> {
    public void execute(Event<UIVersionInfo> event) throws Exception {
      UIVersionInfo uiVersionInfo = event.getSource();
      for(UIComponent uiChild : uiVersionInfo.getChildren()) {
        uiChild.setRendered(false) ;
      }
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiVersionInfo.curentVersion_  = uiVersionInfo.rootVersion_.findVersionNode(objectId) ;
      Node node = uiVersionInfo.getCurrentNode() ;
      VersionHistory versionHistory = node.getVersionHistory() ;
      try {
        versionHistory.removeVersion(uiVersionInfo.curentVersion_ .getName());
        uiVersionInfo.rootVersion_ = new VersionNode(node.getVersionHistory().getRootVersion()) ;
        uiVersionInfo.curentVersion_ = uiVersionInfo.rootVersion_ ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiVersionInfo) ;
      } catch (Exception e) {
        UIApplication app = uiVersionInfo.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UIVersionInfo.msg.cannot-remove-version",null)) ;
      }
    }
  }

  static  public class CompareVersionActionListener extends EventListener<UIVersionInfo> {
    public void execute(Event<UIVersionInfo> event) throws Exception {
      UIVersionInfo uiVersionInfo = event.getSource();
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiVersionInfo.curentVersion_  = uiVersionInfo.rootVersion_.findVersionNode(objectId) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiVersionInfo) ;
    }
  }

  static public class SelectActionListener extends EventListener<UIVersionInfo> {
    public void execute(Event<UIVersionInfo> event) throws Exception {
      UIVersionInfo uiVersionInfo = event.getSource() ;
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      VersionNode root = uiVersionInfo.getRootVersionNode() ;
      VersionNode selectedVersion= root.findVersionNode(path);
      selectedVersion.setExpanded(!selectedVersion.isExpanded()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiVersionInfo) ;
    }
  } 

  static public class CloseActionListener extends EventListener<UIVersionInfo> {
    public void execute(Event<UIVersionInfo> event) throws Exception {
      UIVersionInfo uiVersionInfo = event.getSource();
      for(UIComponent uiChild : uiVersionInfo.getChildren()) {
        if (uiChild.isRendered()) {
          uiChild.setRendered(false);
          return ;
        }
      }
      UIJCRExplorer uiExplorer = uiVersionInfo.getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;
    }
  }
}