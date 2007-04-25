/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.UIPopupAction;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Jan 5, 2007 2:32:34 PM 
 */
@ComponentConfig(
    template =  "app:/groovy/webui/component/browse/UIToolBar.gtmpl",
    events = {
        @EventConfig(listeners = UIToolBar.ShowHiddenActionListener.class),
        @EventConfig(listeners = UIToolBar.SelectPathActionListener.class),
        @EventConfig(listeners = UIToolBar.VoteActionListener.class),
        @EventConfig(listeners = UIToolBar.CommentActionListener.class),
        @EventConfig(listeners = UIToolBar.SearchActionListener.class)
    }
)

public class UIToolBar extends UIContainer {
  private boolean isEnableTree_ = true ;
  private boolean isEnablePath_ = true ;
  private boolean isEnableSeach_ = false ;

  public UIToolBar()throws Exception {}
  public void setEnablePath(boolean enablePath) {isEnablePath_ = enablePath ;}
  public boolean enablePath() {return isEnablePath_ ;}
  public void setEnableSearch(boolean enableSearch) {isEnableSeach_ = enableSearch ;}
  public boolean enableSearch() {return isEnableSeach_ ;}
  public void setEnableTree(boolean enableTree) {isEnableTree_ = enableTree ;} 
  public boolean enableTree() {return isEnableTree_ ;}

  public List<Node> getNodePaths(Node node) throws Exception {
    List<Node> list = new ArrayList<Node>() ;
    if(node != null) {
      Node temp = node ;
      while(!temp.equals(getRootNode())) {
        list.add(0, temp) ;
        temp = temp.getParent() ;
      }
    }
    return list;
  }
  public boolean enableComment() {
    UIBrowseContainer uiContainer = getAncestorOfType(UIBrowseContainer.class);
    return Boolean.parseBoolean(uiContainer.getPortletPreferences().getValue(Utils.CB_VIEW_COMMENT,"")) ;
  }
  public boolean enableVote() {
    UIBrowseContainer uiContainer = getAncestorOfType(UIBrowseContainer.class) ;
    return Boolean.parseBoolean(uiContainer.getPortletPreferences().getValue(Utils.CB_VIEW_VOTE,"")) ;
  }
  public Node getRootNode() throws Exception {
    UIBrowseContainer uiContainer = getAncestorOfType(UIBrowseContainer.class) ;
    return uiContainer.getRootNode() ;
  }

  public Node getCurrentNode() throws Exception {
    UIBrowseContainer uiContainer = getAncestorOfType(UIBrowseContainer.class) ;
    return uiContainer.getSelectedTab();}

  public boolean isShowCategoryTree() {
    UIBrowseContainer uiBrowseContainer = getAncestorOfType(UIBrowseContainer.class) ;
    return  uiBrowseContainer.isShowCategoryTree() ;
  }

  static  public class SelectPathActionListener extends EventListener<UIToolBar> {    
    public void execute(Event<UIToolBar> event) throws Exception {
      UIToolBar uiComp = event.getSource() ;
      String nodePath =  event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIBrowseContainer uiContainer = uiComp.getAncestorOfType(UIBrowseContainer.class) ;
      uiContainer.changeNode(uiContainer.getNodeByPath(nodePath)) ;
    }
  }

  static public class ShowHiddenActionListener extends EventListener<UIToolBar> {
    public void execute(Event<UIToolBar> event) throws Exception {
      UIToolBar uiToolBar = event.getSource() ;
      UIBrowseContainer uiBrowseContainer = uiToolBar.getAncestorOfType(UIBrowseContainer.class) ;
      uiBrowseContainer.setShowCategoryTree(!uiBrowseContainer.isShowCategoryTree()) ;
    }
  } 
     
  static public class SearchActionListener extends EventListener<UIToolBar> {
    public void execute(Event<UIToolBar> event) throws Exception {
      UIToolBar uiComp = event.getSource() ;
      UIBrowseContainer uiContainer = uiComp.getAncestorOfType(UIBrowseContainer.class) ;
      UISearchController uiSearchController = uiContainer.getChild(UISearchController.class) ;
      uiSearchController.setShowHiddenSearch() ;
    }
  }  
  static public class VoteActionListener extends EventListener<UIToolBar> {
    public void execute(Event<UIToolBar> event) throws Exception {
      UIToolBar uiComp = event.getSource() ;
      UIBrowseContainer container = uiComp.getAncestorOfType(UIBrowseContainer.class) ;
      UIDocumentDetail uiDocument = container.getChild(UIDocumentDetail.class)  ;
      if(!container.isShowDocumentDetail()) {
        UIApplication app = uiComp.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UIToolBar.msg.select-doc", null)) ;
        return ;
      } 
      if(uiDocument.node_.isNodeType("mix:votable")) {
        UIBrowseContentPortlet cbPortlet = uiComp.getAncestorOfType(UIBrowseContentPortlet.class) ;
        UIPopupAction uiPopupAction = cbPortlet.getChildById("UICBPopupAction") ;
        uiPopupAction.activate(UICBVoteForm.class, 300) ;
        uiPopupAction.getChild(UIPopupWindow.class).setResizable(false) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      }
    }
  }  
  static public class CommentActionListener extends EventListener<UIToolBar> {
    public void execute(Event<UIToolBar> event) throws Exception {
      UIToolBar uiComp = event.getSource() ;
      UIBrowseContainer container = uiComp.getAncestorOfType(UIBrowseContainer.class) ;
      UIDocumentDetail uiDocument = container.getChild(UIDocumentDetail.class)  ;
      if(!container.isShowDocumentDetail()) {
        UIApplication app = uiComp.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UIToolBar.msg.select-doc", null)) ;
        return ;
      } 
      if(uiDocument.node_.isNodeType("mix:commentable")) {
        UIBrowseContentPortlet cbPortlet = uiComp.getAncestorOfType(UIBrowseContentPortlet.class) ;
        UIPopupAction uiPopupAction = cbPortlet.getChildById("UICBPopupAction") ;
        UICBCommentForm commentForm = uiComp.createUIComponent(UICBCommentForm.class, null, null) ;
        commentForm.setDocument(uiDocument.node_) ;
        uiPopupAction.activate(commentForm, 600, 0) ;
        uiPopupAction.getChild(UIPopupWindow.class).setResizable(false) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      }
    }
  }  
}