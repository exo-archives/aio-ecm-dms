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
import org.exoplatform.ecm.webui.component.admin.rules.UIRuleManager;
import org.exoplatform.ecm.webui.component.admin.script.UIScriptManager;
import org.exoplatform.ecm.webui.component.admin.taxonomy.UITaxonomyManager;
import org.exoplatform.ecm.webui.component.admin.templates.UITemplatesManager;
import org.exoplatform.ecm.webui.component.admin.views.UIViewManager;
import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
        @EventConfig(listeners = UIECMAdminControlPanel.UIRuleManagerActionListener.class),
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
      uiWorkingArea.setRenderedChild(UITemplatesManager.class) ;
      portlet.setRenderedCompName(uiWorkingArea.getChild(UITemplatesManager.class).getId()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(portlet.getChild(UIECMAdminFunctionTitle.class)) ;
    }
  }

  static public class UIScriptManagerActionListener extends EventListener<UIECMAdminControlPanel> {
    public void execute(Event<UIECMAdminControlPanel> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getParent() ;
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      
      uiWorkingArea.getChild(UIScriptManager.class).refresh() ;
      uiWorkingArea.setRenderedChild(UIScriptManager.class) ;
      portlet.setRenderedCompName(uiWorkingArea.getChild(UIScriptManager.class).getId()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(portlet.getChild(UIECMAdminFunctionTitle.class)) ;
    }
  }

  static public class UIRuleManagerActionListener extends EventListener<UIECMAdminControlPanel> {
    public void execute(Event<UIECMAdminControlPanel> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getParent() ;
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      uiWorkingArea.getChild(UIRuleManager.class).refresh() ;
      uiWorkingArea.setRenderedChild(UIRuleManager.class) ;
      portlet.setRenderedCompName(uiWorkingArea.getChild(UIRuleManager.class).getId()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(portlet.getChild(UIECMAdminFunctionTitle.class)) ;
    }
  }

  static public class UIActionManagerActionListener extends EventListener<UIECMAdminControlPanel> {
    public void execute(Event<UIECMAdminControlPanel> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getParent() ;
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      uiWorkingArea.getChild(UIActionManager.class).refresh() ;
      uiWorkingArea.setRenderedChild(UIActionManager.class) ;
      portlet.setRenderedCompName(uiWorkingArea.getChild(UIActionManager.class).getId()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(portlet.getChild(UIECMAdminFunctionTitle.class)) ;
    }
  }

  static public class UINodeTypeManagerActionListener extends EventListener<UIECMAdminControlPanel> {
    public void execute(Event<UIECMAdminControlPanel> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getParent() ;
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      uiWorkingArea.setRenderedChild(UINodeTypeManager.class) ;
      portlet.setRenderedCompName(uiWorkingArea.getChild(UINodeTypeManager.class).getId()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(portlet.getChild(UIECMAdminFunctionTitle.class)) ;
    }
  }

  static public class UIViewManagerActionListener extends EventListener<UIECMAdminControlPanel> {
    public void execute(Event<UIECMAdminControlPanel> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getParent() ;
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      uiWorkingArea.setRenderedChild(UIViewManager.class) ;
      portlet.setRenderedCompName(uiWorkingArea.getChild(UIViewManager.class).getId()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(portlet.getChild(UIECMAdminFunctionTitle.class)) ;
    }
  }

  static public class UITaxonomyManagerActionListener extends EventListener<UIECMAdminControlPanel> {
    public void execute(Event<UIECMAdminControlPanel> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getParent() ;
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      uiWorkingArea.setRenderedChild(UITaxonomyManager.class) ;
      portlet.setRenderedCompName(uiWorkingArea.getChild(UITaxonomyManager.class).getId()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(portlet.getChild(UIECMAdminFunctionTitle.class)) ;
    }
  }

  static public class UINamespaceManagerActionListener extends EventListener<UIECMAdminControlPanel> {
    public void execute(Event<UIECMAdminControlPanel> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getParent() ;
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      uiWorkingArea.getChild(UINamespaceManager.class).refresh() ;
      uiWorkingArea.setRenderedChild(UINamespaceManager.class) ;
      portlet.setRenderedCompName(uiWorkingArea.getChild(UINamespaceManager.class).getId()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(portlet.getChild(UIECMAdminFunctionTitle.class)) ;
    }
  }

  static public class UIMetadataManagerActionListener extends EventListener<UIECMAdminControlPanel> {
    public void execute(Event<UIECMAdminControlPanel> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getParent() ;
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      uiWorkingArea.setRenderedChild(UIMetadataManager.class) ;
      UIMetadataManager uiManager = uiWorkingArea.getChild(UIMetadataManager.class) ;
      uiManager.getChild(UIMetadataList.class).updateGrid() ;
      portlet.setRenderedCompName(uiWorkingArea.getChild(UIMetadataManager.class).getId()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(portlet.getChild(UIECMAdminFunctionTitle.class)) ;
    }
  }

  static public class UIDriveManagerActionListener extends EventListener<UIECMAdminControlPanel> {
    public void execute(Event<UIECMAdminControlPanel> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getParent() ;
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      uiWorkingArea.setRenderedChild(UIDriveManager.class) ;
      portlet.setRenderedCompName(uiWorkingArea.getChild(UIDriveManager.class).getId()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(portlet.getChild(UIECMAdminFunctionTitle.class)) ;
    }
  }

  static public class UIQueriesManagerActionListener extends EventListener<UIECMAdminControlPanel> {
    public void execute(Event<UIECMAdminControlPanel> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getParent() ;
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      uiWorkingArea.setRenderedChild(UIQueriesManager.class) ;
      portlet.setRenderedCompName(uiWorkingArea.getChild(UIQueriesManager.class).getId()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(portlet.getChild(UIECMAdminFunctionTitle.class)) ;
    }
  }
  
  static public class UIFolksonomyManagerActionListener extends EventListener<UIECMAdminControlPanel> {
    public void execute(Event<UIECMAdminControlPanel> event) throws Exception {
      UIECMAdminPortlet portlet = event.getSource().getParent() ;
      UIECMAdminWorkingArea uiWorkingArea = portlet.getChild(UIECMAdminWorkingArea.class);
      uiWorkingArea.setRenderedChild(UIFolksonomyManager.class) ;
      UIFolksonomyManager uiFolkSonomyManager = uiWorkingArea.getChild(UIFolksonomyManager.class) ;
      UITagStyleList uiTagStyleList = uiFolkSonomyManager.getChild(UITagStyleList.class) ;
      uiTagStyleList.updateGrid() ;
      portlet.setRenderedCompName(uiFolkSonomyManager.getId()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingArea) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(portlet.getChild(UIECMAdminFunctionTitle.class)) ;
    }
  }
}