/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.sidebar;

import java.util.LinkedList;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.exoplatform.ecm.jcr.model.ClipboardCommand;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@yahoo.de
 * Oct 20, 2006
 */
@ComponentConfig(
    template =  "app:/groovy/webui/component/explorer/sidebar/UIClipboard.gtmpl",
    events = {
        @EventConfig(listeners = UIClipboard.PasteActionListener.class),
        @EventConfig(listeners = UIClipboard.DeleteActionListener.class),
        @EventConfig(listeners = UIClipboard.ClearAllActionListener.class)
    }
  )

public class UIClipboard extends UIComponent {
  final static public String[] CLIPBOARD_BEAN_FIELD = {"path", "command"} ;
  final static public String[]  CLIPBOARD_ACTIONS = {"Paste", "Delete"} ;
  
  private LinkedList<ClipboardCommand> clipboard_ ;
  
  public UIClipboard() throws Exception {
  }
  
  public String[] getBeanFields() {
    return CLIPBOARD_BEAN_FIELD ;
  } 
  
  public String[] getBeanActions() {
    return  CLIPBOARD_ACTIONS ;
  }
  
  public LinkedList<ClipboardCommand> getClipboardData() throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    clipboard_ = uiExplorer.getAllClipBoard() ;
    return clipboard_ ;
  }
  
  static public class PasteActionListener extends EventListener<UIClipboard> {
    public void execute(Event<UIClipboard> event) throws Exception {
      UIClipboard uiClipboard = event.getSource() ;
      UIJCRExplorer uiExplorer = uiClipboard.getAncestorOfType(UIJCRExplorer.class) ;
      String id = event.getRequestContext().getRequestParameter(OBJECTID) ;
      int index = Integer.parseInt(id) ;
      ClipboardCommand selectedClipboard = uiClipboard.clipboard_.get(index-1) ;      
      Node node = uiExplorer.getCurrentNode() ;
      String type = selectedClipboard.getType();
      String srcPath = selectedClipboard.getSrcPath();      
      String nodePath = node.getPath();
      String destPath = nodePath + srcPath.substring(srcPath.lastIndexOf("/"));
      UIApplication app = uiClipboard.getAncestorOfType(UIApplication.class) ;
      try {
        Session session = uiExplorer.getSession() ;
        Workspace workspace = session.getWorkspace();
        if(ClipboardCommand.COPY.equals(type)) {
          workspace.copy(srcPath, destPath);
        } else {
          session.move(srcPath, destPath);
          session.save() ;
          uiClipboard.clipboard_.remove(index-1) ;
        }
        Object[] args = { srcPath, destPath };   
        app.addMessage(new ApplicationMessage("UIClipboard.msg.node-pasted", args)) ; 
        uiExplorer.updateAjax(event);
      } catch (Exception e) {
        app.addMessage(new ApplicationMessage("UIClipboard.msg.unable-pasted", null)) ;
      }
    }
  }
  
  static public class DeleteActionListener extends EventListener<UIClipboard> {
    public void execute(Event<UIClipboard> event) throws Exception{
      UIClipboard uiClipboard = event.getSource() ;
      String itemIndex = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiClipboard.clipboard_.remove(Integer.parseInt(itemIndex)-1) ;
    }
  }
  
  static public class ClearAllActionListener extends EventListener<UIClipboard> {
    public void execute(Event<UIClipboard> event) {
      UIClipboard uiClipboard = event.getSource() ;
      uiClipboard.clipboard_.clear() ;
    }
  }
}

