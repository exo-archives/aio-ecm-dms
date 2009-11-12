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
package org.exoplatform.ecm.webui.component.explorer;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.component.explorer.rightclick.manager.DeleteManageComponent;
import org.exoplatform.ecm.webui.component.explorer.rightclick.manager.RestoreFromTrashManageComponent;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Anh Vu
 *          anhvurz90@gmail.com
 * Nov 11, 2009  
 * 5:32:05 PM
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/explorer/UIRestoreConfirmMessage.gtmpl",
    events = {
        @EventConfig(listeners = UIRestoreConfirmMessage.OKActionListener.class),
        @EventConfig(listeners = UIRestoreConfirmMessage.CloseActionListener.class)
    }
)
public class UIRestoreConfirmMessage extends UIConfirmMessage {
	
	private Node node;
	private String restoreNodeWs;
	private String restoreNodePath;

	public Node getNode() {	return node; }
	public void setNode(Node node) { this.node = node; }
	
	public String getRestoreNodeWs() { return restoreNodeWs; }
	public void setRestoreNodeWs(String ws) { this.restoreNodeWs = ws; }

	public String getRestoreNodePath() { return restoreNodePath; }
	public void setRestoreNodePath(String path) { this.restoreNodePath = path; }
	
	public UIRestoreConfirmMessage() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}
	
  static public class OKActionListener extends EventListener<UIRestoreConfirmMessage> {
    public void execute(Event<UIRestoreConfirmMessage> event) throws Exception {
      UIRestoreConfirmMessage uiConfirm = event.getSource();
      UIJCRExplorer uiExplorer = uiConfirm.getAncestorOfType(UIJCRExplorer.class);
      UIWorkingArea uiWorkingArea = uiExplorer.getChild(UIWorkingArea.class);
//      uiWorkingArea.getChild(RestoreFromTrashManageComponent.class).doRestore(uiConfirm.nodePath_, uiConfirm.node, event);
      uiWorkingArea.getChild(DeleteManageComponent.class).
      				doDeleteWithoutTrash(uiConfirm.restoreNodeWs + ":" + uiConfirm.restoreNodePath, event);
      RestoreFromTrashManageComponent.doRestore(uiConfirm.nodePath_, uiConfirm.node, event);
      uiConfirm.isOK_ = true;
      UIPopupContainer popupAction = uiConfirm.getAncestorOfType(UIPopupContainer.class);
      popupAction.deActivate() ;
    }
  }
  
  static  public class CloseActionListener extends EventListener<UIRestoreConfirmMessage> {
    public void execute(Event<UIRestoreConfirmMessage> event) throws Exception {
      UIRestoreConfirmMessage uiConfirm = event.getSource();
      uiConfirm.isOK_ = false;
      UIPopupContainer popupAction = uiConfirm.getAncestorOfType(UIPopupContainer.class) ;
      popupAction.deActivate() ;
    }
  }
	 

}
