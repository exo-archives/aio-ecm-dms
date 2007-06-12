/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin;

import java.util.List;

import org.exoplatform.ecm.webui.component.admin.action.UIActionManager;
import org.exoplatform.ecm.webui.component.admin.drives.UIDriveManager;
import org.exoplatform.ecm.webui.component.admin.folksonomy.UIFolksonomyManager;
import org.exoplatform.ecm.webui.component.admin.folksonomy.UITagStyleList;
import org.exoplatform.ecm.webui.component.admin.metadata.UIMetadataList;
import org.exoplatform.ecm.webui.component.admin.metadata.UIMetadataManager;
import org.exoplatform.ecm.webui.component.admin.namespace.UINamespaceManager;
import org.exoplatform.ecm.webui.component.admin.nodetype.UINodeTypeManager;
import org.exoplatform.ecm.webui.component.admin.queries.UIQueriesManager;
import org.exoplatform.ecm.webui.component.admin.script.UIScriptManager;
import org.exoplatform.ecm.webui.component.admin.taxonomy.UITaxonomyManager;
import org.exoplatform.ecm.webui.component.admin.templates.UITemplatesManager;
import org.exoplatform.ecm.webui.component.admin.views.UIViewManager;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Sep 19, 2006
 * 8:26:51 AM 
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/admin/UIECMAdminControlPanel.gtmpl",
    events = {
        @EventConfig(listeners = UIECMAdminControlPanel.UIViewManagerActionListener.class),
        @EventConfig(listeners = UIECMAdminControlPanel.UIMetadataManagerActionListener.class),
        @EventConfig(listeners = UIECMAdminControlPanel.UITaxonomyManagerActionListener.class),
        @EventConfig(listeners = UIECMAdminControlPanel.UINamespaceManagerActionListener.class),
        @EventConfig(listeners = UIECMAdminControlPanel.UINodeTypeManagerActionListener.class),
        @EventConfig(listeners = UIECMAdminControlPanel.UITemplatesManagerActionListener.class),
        @EventConfig(listeners = UIECMAdminControlPanel.UIActionManagerActionListener.class),
        @EventConfig(listeners = UIECMAdminControlPanel.UIScriptManagerActionListener.class),
        @EventConfig(listeners = UIECMAdminControlPanel.UIDriveManagerActionListener.class),
        @EventConfig(listeners = UIECMAdminControlPanel.UIQueriesManagerActionListener.class),
        @EventConfig(listeners = UIECMAdminControlPanel.UIFolksonomyManagerActionListener.class)
    }
)
public class UIECMAdminControlPanel extends UIComponent {
  public UIECMAdminControlPanel() throws Exception {}
  public List getEvents() { return getComponentConfig().getEvents() ; }
  
  static public class UITemplatesManagerActionListener extends EventListener<UIECMAdminControlPanel> {
    public void execute(Event<UIECMAdminControlPanel> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getParent() ;
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      uiWorkingArea.getChild(UITemplatesManager.class).refresh() ;
      uiWorkingArea.setChild(UITemplatesManager.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
    }
  }

  static public class UIScriptManagerActionListener extends EventListener<UIECMAdminControlPanel> {
    public void execute(Event<UIECMAdminControlPanel> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getParent() ;
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      uiWorkingArea.getChild(UIScriptManager.class).refresh() ;
      uiWorkingArea.setChild(UIScriptManager.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
    }
  }

  static public class UIActionManagerActionListener extends EventListener<UIECMAdminControlPanel> {
    public void execute(Event<UIECMAdminControlPanel> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getParent() ;
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      uiWorkingArea.getChild(UIActionManager.class).refresh() ;
      uiWorkingArea.setChild(UIActionManager.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
    }
  }

  static public class UINodeTypeManagerActionListener extends EventListener<UIECMAdminControlPanel> {
    public void execute(Event<UIECMAdminControlPanel> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getParent() ;
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      uiWorkingArea.getChild(UINodeTypeManager.class).update() ;
      uiWorkingArea.setChild(UINodeTypeManager.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
    }
  }

  static public class UIViewManagerActionListener extends EventListener<UIECMAdminControlPanel> {
    public void execute(Event<UIECMAdminControlPanel> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getParent() ;
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      uiWorkingArea.getChild(UIViewManager.class).update() ;
      uiWorkingArea.setChild(UIViewManager.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
    }
  }

  static public class UITaxonomyManagerActionListener extends EventListener<UIECMAdminControlPanel> {
    public void execute(Event<UIECMAdminControlPanel> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getParent() ;
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      uiWorkingArea.setChild(UITaxonomyManager.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
    }
  }

  static public class UINamespaceManagerActionListener extends EventListener<UIECMAdminControlPanel> {
    public void execute(Event<UIECMAdminControlPanel> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getParent() ;
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      uiWorkingArea.getChild(UINamespaceManager.class).refresh() ;
      uiWorkingArea.setChild(UINamespaceManager.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
    }
  }

  static public class UIMetadataManagerActionListener extends EventListener<UIECMAdminControlPanel> {
    public void execute(Event<UIECMAdminControlPanel> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getParent() ;
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      uiWorkingArea.setChild(UIMetadataManager.class) ;
      UIMetadataManager uiManager = uiWorkingArea.getChild(UIMetadataManager.class) ;
      uiManager.getChild(UIMetadataList.class).updateGrid() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
    }
  }

  static public class UIDriveManagerActionListener extends EventListener<UIECMAdminControlPanel> {
    public void execute(Event<UIECMAdminControlPanel> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getParent() ;
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      uiWorkingArea.getChild(UIDriveManager.class).update() ;
      uiWorkingArea.setChild(UIDriveManager.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
    }
  }

  static public class UIQueriesManagerActionListener extends EventListener<UIECMAdminControlPanel> {
    public void execute(Event<UIECMAdminControlPanel> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getParent() ;
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      uiWorkingArea.getChild(UIQueriesManager.class).update() ;
      uiWorkingArea.setChild(UIQueriesManager.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
    }
  }
  
  static public class UIFolksonomyManagerActionListener extends EventListener<UIECMAdminControlPanel> {
    public void execute(Event<UIECMAdminControlPanel> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getParent() ;
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      uiWorkingArea.setChild(UIFolksonomyManager.class) ;
      UIFolksonomyManager uiFolkSonomyManager = uiWorkingArea.getChild(UIFolksonomyManager.class) ;
      UITagStyleList uiTagStyleList = uiFolkSonomyManager.getChild(UITagStyleList.class) ;
      uiTagStyleList.updateGrid() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
    }
  }
}