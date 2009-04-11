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
package org.exoplatform.ecm.webui.component.explorer.control ;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.AccessDeniedException;

import org.exoplatform.ecm.webui.component.explorer.UIDocumentContainer;
import org.exoplatform.ecm.webui.component.explorer.UIDocumentWorkspace;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIWorkingArea;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer.HistoryEntry;
import org.exoplatform.services.cms.link.LinkUtils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@exoplatform.com
 * Aug 2, 2006
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/explorer/control/UIAddressBar.gtmpl",
    events = {
      @EventConfig(listeners = UIAddressBar.ChangeNodeActionListener.class),
      @EventConfig(listeners = UIAddressBar.BackActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIAddressBar.HistoryActionListener.class, phase = Phase.DECODE)
    }
)

public class UIAddressBar extends UIForm {
  
  public static final Pattern FILE_EXPLORER_URL_SYNTAX = Pattern.compile("([^:/]+):(.*)");
  
  public final static String WS_NAME = "workspaceName";  
  public final static String FIELD_ADDRESS = "address" ; 
  
  public UIAddressBar() throws Exception {
    addUIFormInput(new UIFormStringInput(FIELD_ADDRESS, FIELD_ADDRESS, null)) ;
  }

  @Deprecated
  public Set<String> getHistory() {
    UIJCRExplorer uiJCRExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    return uiJCRExplorer.getAddressPath() ;
  }

  public Collection<HistoryEntry> getFullHistory() {
    UIJCRExplorer uiJCRExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    return uiJCRExplorer.getHistory() ;
  }
  
  static public class BackActionListener extends EventListener<UIAddressBar> {
    public void execute(Event<UIAddressBar> event) throws Exception {
      UIAddressBar uiAddressBar = event.getSource() ;
      UIJCRExplorer uiExplorer = uiAddressBar.getAncestorOfType(UIJCRExplorer.class) ;
      UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class) ;
      try {        
        uiExplorer.getChild(UIWorkingArea.class).getChild(UIDocumentWorkspace.class).
        setRenderedChild(UIDocumentContainer.class) ;
        if(uiExplorer.isViewTag() && !uiExplorer.getCurrentNode().equals(uiExplorer.getRootNode())) {
          uiExplorer.setSelectRootNode() ;
          uiExplorer.setIsViewTag(true) ;
        } else if(uiExplorer.isViewTag() && uiExplorer.getCurrentStateNode() != null) {
          uiExplorer.setIsViewTag(false) ;
          uiExplorer.setSelectNode(uiExplorer.getCurrentStatePath()) ;
        } else {
          String previousNodePath = uiExplorer.rewind() ;
          String previousWs = uiExplorer.previousWsName();
          uiExplorer.setBackNodePath(previousWs, previousNodePath) ;
        }
        uiExplorer.updateAjax(event) ;
      } catch (AccessDeniedException ade) {
        uiApp.addMessage(new ApplicationMessage("UIAddressBar.msg.access-denied", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIJCRExplorer.msg.no-node-history",
                                                null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
    }
  }
  
  static public class ChangeNodeActionListener extends EventListener<UIAddressBar> {
    public void execute(Event<UIAddressBar> event) throws Exception {
      UIAddressBar uiAddress = event.getSource() ;
      String path = uiAddress.getUIStringInput(FIELD_ADDRESS).getValue() ;
      if (path == null || path.trim().length() == 0) path = "/";
      UIJCRExplorer uiExplorer = uiAddress.getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.setIsViewTag(false) ;
      try {
        String prefix = uiExplorer.getRootPath();
        String nodePath = LinkUtils.evaluatePath(LinkUtils.createPath(prefix, path));
        uiExplorer.setSelectNode(nodePath) ;
        uiExplorer.setCurrentStatePath(nodePath) ;
      } catch(Exception e) {
        UIApplication uiApp = uiAddress.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAddressBar.msg.path-not-found", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      uiExplorer.updateAjax(event) ;
    }
  }
  
  static public class HistoryActionListener extends EventListener<UIAddressBar> {
    public void execute(Event<UIAddressBar> event) throws Exception {
      UIAddressBar uiAddressBar = event.getSource() ;
      String fullPath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRExplorer uiExplorer = uiAddressBar.getAncestorOfType(UIJCRExplorer.class) ;
      String workspace = null;
      String path = null;
      try{
        Matcher matcher = FILE_EXPLORER_URL_SYNTAX.matcher(fullPath);
        if (matcher.find()) {
          workspace = matcher.group(1);
          path = matcher.group(2);
        }
        uiExplorer.setSelectNode(workspace, path) ;
        uiExplorer.refreshExplorer() ;
      } catch (AccessDeniedException ade) {
        UIApplication uiApp = uiAddressBar.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAddressBar.msg.access-denied", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
    }
  }
}