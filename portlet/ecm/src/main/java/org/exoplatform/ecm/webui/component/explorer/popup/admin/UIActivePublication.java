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
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.popup.UIPopupComponent;
import org.exoplatform.ecm.webui.popup.UIPopupContainer;
import org.exoplatform.ecm.webui.utils.LockUtil;
import org.exoplatform.services.ecm.publication.AlreadyInPublicationLifecycleException;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationPresentationService;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.ecm.publication.plugins.webui.UIPublicationLogList;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Sep 9, 2008
 */

/**
 * The Class UIActivePublication.
 */
@ComponentConfig(template = "app:/groovy/webui/component/UIGridWithButton.gtmpl", events = {
    @EventConfig(listeners = UIActivePublication.EnrolActionListener.class),
    @EventConfig(listeners = UIActivePublication.CancelActionListener.class) })
    public class UIActivePublication extends UIGrid implements UIPopupComponent {

  /** The Constant LIFECYCLE_NAME. */
  public final static String LIFECYCLE_NAME     = "LifecycleName";

  /** The Constant LIFECYCLE_DESC. */
  public final static String LIFECYCLE_DESC     = "LifecycleDesc";

  /** The LIFECYCL e_ fields. */
  public static String[]     LIFECYCLE_FIELDS   = { LIFECYCLE_NAME, LIFECYCLE_DESC };

  /** The LIFECYCL e_ action. */
  public static String[]     LIFECYCLE_ACTION   = { "Enrol" };

  /** The Constant LIFECYCLE_SELECTED. */
  public final static String LIFECYCLE_SELECTED = "LifecycleSelected";

  /**
   * Instantiates a new uI active publication.
   * 
   * @throws Exception the exception
   */
  public UIActivePublication() throws Exception {
    configure(LIFECYCLE_NAME, LIFECYCLE_FIELDS, LIFECYCLE_ACTION);
    getUIPageIterator().setId("LifecyclesIterator");    
  }

  /**
   * Gets the actions.
   * 
   * @return the actions
   */
  public String[] getActions() {
    return new String[] { "Cancel" };
  }

  /**
   * Update lifecycles grid.
   * 
   * @throws Exception the exception
   */
  public void updateLifecyclesGrid() throws Exception {
    List<PublicationLifecycleBean> publicationLifecycleBeans = new ArrayList<PublicationLifecycleBean>();
    PublicationService publicationService = getApplicationComponent(PublicationService.class);
    Collection<PublicationPlugin> publicationPlugins = publicationService.getPublicationPlugins().values();
    if (publicationPlugins.size() != 0) {
      for (PublicationPlugin publicationPlugin: publicationPlugins) {        
        PublicationLifecycleBean lifecycleBean = new PublicationLifecycleBean();
        lifecycleBean.setLifecycleName(publicationPlugin.getLifecycleName());
        lifecycleBean.setLifecycleDesc(publicationPlugin.getDescription());
        publicationLifecycleBeans.add(lifecycleBean);
      }
    }
    ObjectPageList objectPageList = new ObjectPageList(publicationLifecycleBeans, 5);
    getUIPageIterator().setPageList(objectPageList);
  } 
  
  public void enrolNodeInLifecycle(Node currentNode, String lifecycleName, WebuiRequestContext requestContext) throws Exception {
    UIJCRExplorer uiJCRExplorer = getAncestorOfType(UIJCRExplorer.class);    
    UIApplication uiApp = getAncestorOfType(UIApplication.class);
    UIPublicationManager uiPublicationManager = uiJCRExplorer.createUIComponent(
        UIPublicationManager.class, null, null);       
    if (currentNode.isLocked()) {
      String lockToken = LockUtil.getLockToken(currentNode);
      if (lockToken != null)
        uiJCRExplorer.getSession().addLockToken(lockToken);
    }       
    Node parentNode = currentNode.getParent();
    if (parentNode.isLocked()) {
      String lockToken1 = LockUtil.getLockToken(parentNode);
      uiJCRExplorer.getSession().addLockToken(lockToken1);
    }    
    PublicationService publicationService = getApplicationComponent(PublicationService.class);
    PublicationPresentationService publicationPresentationService = getApplicationComponent(PublicationPresentationService.class);
    try {            
      if(!currentNode.isCheckedOut()) {        
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.node-checkedin", null, 
            ApplicationMessage.WARNING));
        requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }      
      publicationService.enrollNodeInLifecycle(currentNode, lifecycleName);
    } catch (AlreadyInPublicationLifecycleException e) {
      uiApp.addMessage(new ApplicationMessage("UIActivePublication.msg.already-enroled", null,
          ApplicationMessage.ERROR));
      requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    } catch (Exception e) {
      e.printStackTrace();
      uiApp.addMessage(new ApplicationMessage("UIActivePublication.msg.unknow-error",
          new String[] { e.getMessage() }, ApplicationMessage.ERROR));
      requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return;
    }
    UIContainer container = createUIComponent(UIContainer.class, null, null);
    UIForm uiFormPublicationManager = publicationPresentationService.getStateUI(currentNode, container);
    uiPublicationManager.addChild(uiFormPublicationManager);
    uiPublicationManager.addChild(UIPublicationLogList.class, null, null).setRendered(false);
    UIPublicationLogList uiPublicationLogList = uiPublicationManager.getChild(UIPublicationLogList.class);
    UIPopupContainer UIPopupContainer = uiJCRExplorer.getChild(UIPopupContainer.class);
    UIPopupContainer.activate(uiPublicationManager, 700, 500);
    uiPublicationLogList.setNode(uiJCRExplorer.getCurrentNode());
    uiPublicationLogList.updateGrid();
  }
  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.ecm.webui.popup.UIPopupComponent#activate()
   */
  public void activate() throws Exception {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.ecm.webui.popup.UIPopupComponent#deActivate()
   */
  public void deActivate() throws Exception {
  }

  /**
   * The Class PublicationLifecycleBean.
   */
  public class PublicationLifecycleBean {    
    private String lifecycleName;
    private String lifecycleDesc;

    /**
     * Gets the lifecycle name.
     * 
     * @return the lifecycle name
     */
    public String getLifecycleName() {
      return lifecycleName;
    }

    /**
     * Sets the lifecycle name.
     * 
     * @param lifecycleName the new lifecycle name
     */
    public void setLifecycleName(String lifecycleName) {
      this.lifecycleName = lifecycleName;
    }

    /**
     * Gets the lifecycle desc.
     * 
     * @return the lifecycle desc
     */
    public String getLifecycleDesc() {
      return lifecycleDesc;
    }

    /**
     * Sets the lifecycle desc.
     * 
     * @param lifecycleDesc the new lifecycle desc
     */
    public void setLifecycleDesc(String lifecycleDesc) {
      this.lifecycleDesc = lifecycleDesc;
    }
  }

  /**
   * The listener interface for receiving cancelAction events. The class that is
   * interested in processing a cancelAction event implements this interface,
   * and the object created with that class is registered with a component using
   * the component's <code>addCancelActionListener<code> method. When
   * the cancelAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see CancelActionEvent
   */
  public static class CancelActionListener extends EventListener<UIActivePublication> {

    /*
     * (non-Javadoc)
     * 
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIActivePublication> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      uiExplorer.cancelAction();
    }
  }

  /**
   * The listener interface for receiving enrolAction events. The class that is
   * interested in processing a enrolAction event implements this interface, and
   * the object created with that class is registered with a component using the
   * component's <code>addEnrolActionListener<code> method. When
   * the enrolAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see EnrolActionEvent
   */
  public static class EnrolActionListener extends EventListener<UIActivePublication> {

    /*
     * (non-Javadoc)
     * 
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIActivePublication> event) throws Exception {
      UIActivePublication uiActivePub = event.getSource();
      UIJCRExplorer uiJCRExplorer = uiActivePub.getAncestorOfType(UIJCRExplorer.class);
      String selectedLifecycle = event.getRequestContext().getRequestParameter(OBJECTID);
      Node currentNode = uiJCRExplorer.getCurrentNode();
      uiActivePub.enrolNodeInLifecycle(currentNode,selectedLifecycle,event.getRequestContext());
    }
  }
}
