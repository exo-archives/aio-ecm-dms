/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ecm.webui.component.admin;

import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.webui.component.admin.action.UIActionManager;
import org.exoplatform.ecm.webui.component.admin.drives.UIDriveManager;
import org.exoplatform.ecm.webui.component.admin.folksonomy.UIFolksonomyManager;
import org.exoplatform.ecm.webui.component.admin.metadata.UIMetadataManager;
import org.exoplatform.ecm.webui.component.admin.namespace.UINamespaceManager;
import org.exoplatform.ecm.webui.component.admin.nodetype.UINodeTypeManager;
import org.exoplatform.ecm.webui.component.admin.queries.UIQueriesManager;
import org.exoplatform.ecm.webui.component.admin.script.UIScriptManager;
import org.exoplatform.ecm.webui.component.admin.taxonomy.UITaxonomyManager;
import org.exoplatform.ecm.webui.component.admin.taxonomy.UITaxonomyManagerTrees;
import org.exoplatform.ecm.webui.component.admin.templates.UITemplatesManager;
import org.exoplatform.ecm.webui.component.admin.views.UIViewManager;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
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
    renderedCompId_ = getChild(type).getId();
    setRenderedChild(type);
  }

  public UIECMAdminWorkingArea() throws Exception {
    renderedCompId_ = addChild(UITaxonomyManager.class, null, null).getId();
    addChild(UIViewManager.class, null, null).setRendered(false);
    addChild(UIMetadataManager.class, null, null).setRendered(false);
    addChild(UINodeTypeManager.class, null, null).setRendered(false);
    addChild(UIDriveManager.class, null, null).setRendered(false);
    addChild(UINamespaceManager.class, null, null).setRendered(false);
    addChild(UIActionManager.class, null, null).setRendered(false);
    addChild(UIScriptManager.class, null, null).setRendered(false);
    addChild(UITemplatesManager.class, null, null).setRendered(false);
    addChild(UIQueriesManager.class, null, null).setRendered(false);
    addChild(UIFolksonomyManager.class, null, null).setRendered(false);
    addChild(UITaxonomyManagerTrees.class, null, null).setRendered(false);
  }
  
  public void init() throws Exception { 
    getChild(UITaxonomyManager.class).update();
    getChild(UIViewManager.class).update();
    getChild(UIMetadataManager.class).update();
    getChild(UINodeTypeManager.class).update();
    getChild(UIDriveManager.class).update();
    getChild(UINamespaceManager.class).refresh();
    getChild(UIActionManager.class).refresh();
    getChild(UIScriptManager.class).refresh();
    getChild(UITemplatesManager.class).refresh();
    getChild(UIQueriesManager.class).update();
    getChild(UIFolksonomyManager.class).update();
    getChild(UITaxonomyManagerTrees.class).update();
  }
  
  public void checkRepository() throws Exception{
    PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext
        .getCurrentInstance();
    PortletPreferences pref = pcontext.getRequest().getPreferences();
    String repository = pref.getValue(Utils.REPOSITORY, "");
    try {
      getApplicationComponent(RepositoryService.class).getRepository(repository);
    } catch (Exception e) {
      String defaultRepo = getApplicationComponent(RepositoryService.class).getDefaultRepository()
          .getConfiguration().getName();
      pref.setValue(Utils.REPOSITORY, defaultRepo);
      pref.store();
    }
  }
  
}
