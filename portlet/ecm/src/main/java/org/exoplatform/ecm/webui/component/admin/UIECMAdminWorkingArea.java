/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin;

import org.exoplatform.ecm.webui.component.admin.action.UIActionManager;
import org.exoplatform.ecm.webui.component.admin.drives.UIDriveManager;
import org.exoplatform.ecm.webui.component.admin.folksonomy.UIFolksonomyManager;
import org.exoplatform.ecm.webui.component.admin.metadata.UIMetadataManager;
import org.exoplatform.ecm.webui.component.admin.namespace.UINamespaceManager;
import org.exoplatform.ecm.webui.component.admin.nodetype.UINodeTypeManager;
import org.exoplatform.ecm.webui.component.admin.queries.UIQueriesManager;
import org.exoplatform.ecm.webui.component.admin.script.UIScriptManager;
import org.exoplatform.ecm.webui.component.admin.taxonomy.UITaxonomyManager;
import org.exoplatform.ecm.webui.component.admin.templates.UITemplatesManager;
import org.exoplatform.ecm.webui.component.admin.views.UIViewManager;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Sep 19, 2006
 * 8:30:33 AM 
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/admin/UIECMAdminWorkingArea.gtmpl"
)
public class UIECMAdminWorkingArea extends UIContainer {
  private String renderedCompId_ ;
  
  public String getRenderedCompId() { return renderedCompId_ ; }
  public void setRenderedCompId(String renderedId) { this.renderedCompId_ = renderedId ; }

  public <T extends UIComponent> void setChild(Class<T> type) {
    renderedCompId_ = getChild(type).getId() ;
    setRenderedChild(type) ;
  }

  public UIECMAdminWorkingArea() throws Exception {
    renderedCompId_ = addChild(UITaxonomyManager.class, null , null).getId() ;
    addChild(UIViewManager.class, null, null).setRendered(false) ;
    addChild(UIMetadataManager.class, null, null).setRendered(false) ;
    addChild(UINodeTypeManager.class, null, null).setRendered(false) ;
    addChild(UIDriveManager.class, null, null).setRendered(false) ;
    addChild(UINamespaceManager .class, null, null).setRendered(false) ;
    addChild(UIActionManager.class, null ,null).setRendered(false) ;
    addChild(UIScriptManager.class, null ,null).setRendered(false) ;
    addChild(UITemplatesManager.class, null, null).setRendered(false) ;
    addChild(UIQueriesManager.class, null, null).setRendered(false) ;
    addChild(UIFolksonomyManager.class, null, null).setRendered(false) ;
  }
}
