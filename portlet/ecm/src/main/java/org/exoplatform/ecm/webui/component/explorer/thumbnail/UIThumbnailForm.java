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
package org.exoplatform.ecm.webui.component.explorer.thumbnail;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.version.VersionException;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.popup.UIPopupComponent;
import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
import org.exoplatform.ecm.webui.utils.LockUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.services.cms.thumbnail.ThumbnailService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 24, 2008 10:52:13 AM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/explorer/thumbnail/UIThumbnailForm.gtmpl",
    events = {
      @EventConfig(listeners = UIThumbnailForm.SaveActionListener.class), 
      @EventConfig(listeners = UIThumbnailForm.CancelActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIThumbnailForm.RemoveThumbnailActionListener.class, 
          confirm = "UIThumbnailForm.msg.confirm-delete", phase = Phase.DECODE)
    }
)
public class UIThumbnailForm extends UIForm implements UIPopupComponent {
 
  final static public String MEDIUM_SIZE = "mediumSize";
  private boolean thumbnailRemoved_ = false;
  
  public UIThumbnailForm() throws Exception {
    setMultiPart(true) ;
    UIFormUploadInput uiInput = new UIFormUploadInput(MEDIUM_SIZE, MEDIUM_SIZE) ;
    addUIFormInput(uiInput) ;
  }
  
  public String getThumbnailImage(Node node) throws Exception {
    return Utils.getThumbnailImage(node, ThumbnailService.MEDIUM_SIZE);
  }
  
  public Node getSelectedNode() throws Exception {
    return getAncestorOfType(UIJCRExplorer.class).getCurrentNode();
  }
  
  public boolean isRemovedThumbnail() { return thumbnailRemoved_; }
  
  public String[] getActions() { return new String[] {"Save", "Cancel"}; }
  
  static  public class SaveActionListener extends EventListener<UIThumbnailForm> {
    public void execute(Event<UIThumbnailForm> event) throws Exception {
      UIThumbnailForm uiForm = event.getSource();
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class);
      UIFormUploadInput input = (UIFormUploadInput)uiForm.getUIInput(MEDIUM_SIZE);
      if(input.getUploadResource() == null) {
        uiApp.addMessage(new ApplicationMessage("UIUploadForm.msg.fileName-error", null, 
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      Node selectedNode = uiExplorer.getCurrentNode();
      if(selectedNode.isLocked()) {
        String lockToken = LockUtil.getLockToken(selectedNode);
        if(lockToken != null) uiExplorer.getSession().addLockToken(lockToken);
      }
      String fileName = input.getUploadResource().getFileName();
      MimeTypeResolver mimeTypeSolver = new MimeTypeResolver() ;
      String mimeType = mimeTypeSolver.getMimeType(fileName) ;
      if(!mimeType.startsWith("image")) {
        uiApp.addMessage(new ApplicationMessage("UIThumbnailForm.msg.mimetype-incorrect", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      InputStream inputStream = input.getUploadDataAsStream();
      ThumbnailService thumbnailService = uiForm.getApplicationComponent(ThumbnailService.class);
      BufferedImage image = ImageIO.read(inputStream);
      try {
        thumbnailService.createSpecifiedThumbnail(selectedNode, image, ThumbnailService.MEDIUM_SIZE);
      } catch(AccessDeniedException ace) {
        uiApp.addMessage(new ApplicationMessage("UIThumbnailForm.msg.access-denied", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch(VersionException ver) {
        uiApp.addMessage(new ApplicationMessage("UIThumbnailForm.msg.is-checked-in", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch(Exception e) {
        e.printStackTrace();
        JCRExceptionManager.process(uiApp, e);
      }
      uiExplorer.getSession().save();
      uiExplorer.updateAjax(event);
    }
  }
  
  static  public class RemoveThumbnailActionListener extends EventListener<UIThumbnailForm> {
    public void execute(Event<UIThumbnailForm> event) throws Exception {
      UIThumbnailForm uiForm = event.getSource();
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class);
      uiForm.thumbnailRemoved_ = true;
      Node selectedNode = uiExplorer.getCurrentNode();
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class);
      if(!selectedNode.isCheckedOut()) {
        uiApp.addMessage(new ApplicationMessage("UIThumbnailForm.msg.is-checked-in", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(selectedNode.hasProperty(ThumbnailService.MEDIUM_SIZE)) {
        selectedNode.getProperty(ThumbnailService.MEDIUM_SIZE).remove();
      }
      selectedNode.save();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
      uiExplorer.setIsHidePopup(true);
      uiExplorer.updateAjax(event);
    }
  }  
  
  static  public class CancelActionListener extends EventListener<UIThumbnailForm> {
    public void execute(Event<UIThumbnailForm> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      uiExplorer.cancelAction();
    }
  }

  public void activate() throws Exception {}

  public void deActivate() throws Exception {}
}
