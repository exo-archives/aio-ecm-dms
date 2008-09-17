/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.ecm.webui.component.explorer.popup.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.popup.UIPopupComponent;
import org.exoplatform.ecm.webui.popup.UIPopupContainer;
import org.exoplatform.ecm.webui.utils.LockUtil;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationPresentationService;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.ecm.publication.plugins.webui.UIPublicationLogList;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/*
 * Created by The eXo Platform SAS
 * Author : Anh Do Ngoc
 *          anh.do@exoplatform.com
 * Sep 9, 2008  
 */

@ComponentConfig(
    template = "app:/groovy/webui/component/UIGridWithButton.gtmpl",
    events = {
        @EventConfig(listeners = UIActivePublication.EnrolActionListener.class),
        @EventConfig(listeners = UIActivePublication.CancelActionListener.class)
    }
)

public class UIActivePublication extends UIGrid implements UIPopupComponent {
  
  public final static String LIFECYCLE_NAME   = "LifecycleName";

  public final static String LIFECYCLE_DESC   = "LifecycleDesc";

  public static String[]     LIFECYCLE_FIELDS = { LIFECYCLE_NAME, LIFECYCLE_DESC };

  public static String[]     LIFECYCLE_ACTION = { "Enrol" };
  
  public final static String LIFECYCLE_SELECTED = "LifecycleSelected";
  
  public UIActivePublication() throws Exception {        
    configure(LIFECYCLE_NAME, LIFECYCLE_FIELDS, LIFECYCLE_ACTION);
    getUIPageIterator().setId("LifecyclesIterator");
    updateLifecyclesGrid();
  } 
  
  public String[] getActions() { 
    return new String[]{"Cancel"};
  }
  
  public void updateLifecyclesGrid() throws Exception {
    List<PublicationLifecycleBean> publicationLifecycleBeans = new ArrayList<PublicationLifecycleBean>();
    PublicationService publicationService = getApplicationComponent(PublicationService.class);
    Collection<PublicationPlugin> publicationPlugins = publicationService.getPublicationPlugins()
        .values();
    if (publicationPlugins.size() != 0) {
      for (Iterator<PublicationPlugin> iterator = publicationPlugins.iterator(); iterator.hasNext();) {
        PublicationPlugin publicationPlugin = iterator.next();
        PublicationLifecycleBean lifecycleBean = new PublicationLifecycleBean();
        lifecycleBean.setLifecycleName(publicationPlugin.getLifecycleName());
        lifecycleBean.setLifecycleDesc(publicationPlugin.getDescription());
        publicationLifecycleBeans.add(lifecycleBean);
      }
    }   
    ObjectPageList objectPageList = new ObjectPageList(publicationLifecycleBeans, 5);
    getUIPageIterator().setPageList(objectPageList);
  }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
  
  
  public class PublicationLifecycleBean {
    private String lifecycleName;

    private String lifecycleDesc;

    public String getLifecycleName() {
      return lifecycleName;
    }

    public void setLifecycleName(String lifecycleName) {
      this.lifecycleName = lifecycleName;
    }

    public String getLifecycleDesc() {
      return lifecycleDesc;
    }

    public void setLifecycleDesc(String lifecycleDesc) {
      this.lifecycleDesc = lifecycleDesc;
    }
  }
  
  public static class CancelActionListener extends EventListener<UIActivePublication> {
    public void execute(Event<UIActivePublication> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      uiExplorer.cancelAction();
    }
  }
  
  public static class EnrolActionListener extends EventListener<UIActivePublication> {
    public void execute(Event<UIActivePublication> event) throws Exception { 
      UIActivePublication uiActivePub = event.getSource();
      UIJCRExplorer uiJCRExplorer = uiActivePub.getAncestorOfType(UIJCRExplorer.class);
      UIPopupContainer popupAction = uiJCRExplorer.getChild(UIPopupContainer.class);
      UIPublicationManager uiPublicationManager = uiJCRExplorer.createUIComponent(UIPublicationManager.class, null, null);
      Node currentNode = uiJCRExplorer.getCurrentNode();
      if(currentNode.isLocked()) {
        String lockToken = LockUtil.getLockToken(currentNode);
        if(lockToken != null) uiJCRExplorer.getSession().addLockToken(lockToken);
      }
      PublicationService publicationService = uiActivePub.getApplicationComponent(PublicationService.class);
      PublicationPresentationService publicationPresentationService = uiActivePub.getApplicationComponent(PublicationPresentationService.class);
      String selectedLifecycle = event.getRequestContext().getRequestParameter(OBJECTID);
      Node parentNode = currentNode.getParent() ;
      if(parentNode.isLocked()) {
        String lockToken1 = LockUtil.getLockToken(parentNode);
        uiJCRExplorer.getSession().addLockToken(lockToken1) ;
      }
      publicationService.enrollNodeInLifecycle(currentNode, selectedLifecycle);      
      UIContainer container = uiActivePub.createUIComponent(UIContainer.class, null, null);
      UIForm uiFormPublicationManager = publicationPresentationService.getStateUI(currentNode, container); 
      uiPublicationManager.addChild(uiFormPublicationManager);
      uiPublicationManager.addChild(UIPublicationLogList.class, null, null).setRendered(false);
      UIPublicationLogList uiPublicationLogList = uiPublicationManager
      .getChild(UIPublicationLogList.class);
      popupAction.activate(uiPublicationManager, 700, 500);
      uiPublicationLogList.setNode(uiJCRExplorer.getCurrentNode());
      uiPublicationLogList.updateGrid();
    }    
  }
}
