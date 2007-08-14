/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIPopupAction;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
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
        @EventConfig(listeners = UIToolBar.BackActionListener.class),
        @EventConfig(listeners = UIToolBar.NextActionListener.class),
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

  public boolean isClearHistory() {
    return getAncestorOfType(UIBrowseContainer.class).getNodesHistory().isEmpty() ;
  }

  static  public class SelectPathActionListener extends EventListener<UIToolBar> {    
    public void execute(Event<UIToolBar> event) throws Exception {
      UIToolBar uiComp = event.getSource() ;
      String nodePath =  event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIBrowseContainer uiContainer = uiComp.getAncestorOfType(UIBrowseContainer.class) ;
      UIBrowseContentPortlet uiBCPortlet =  uiComp.getAncestorOfType(UIBrowseContentPortlet.class) ;
      Node selectNode = uiContainer.getNodeByPath(nodePath) ;
      if(selectNode == null) {
        if(uiContainer.getNodeByPath(uiContainer.getCategoryPath()) == null) {
          uiBCPortlet.setPorletMode(PortletRequestContext.HELP_MODE);
          uiBCPortlet.reload() ;
        } else {
          UIApplication uiApp = uiComp.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIToolBar.msg.node-removed", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          selectNode = uiComp.getRootNode() ;
          uiContainer.changeNode(selectNode) ;
        }
      } else {
        uiContainer.changeNode(selectNode) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiBCPortlet) ;
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
      UIToolBar uiToolBar = event.getSource() ;
      UIBrowseContainer uiContainer = uiToolBar.getAncestorOfType(UIBrowseContainer.class) ;
      if(uiContainer.isShowDocumentDetail()) {
        UIApplication uiApp = uiToolBar.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIToolBar.msg.back-view-search", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UISearchController uiSearchController = uiContainer.getChild(UISearchController.class) ;
      uiSearchController.setShowHiddenSearch() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }  

  static public class NextActionListener extends EventListener<UIToolBar> {
    public void execute(Event<UIToolBar> event) throws Exception {
      UIToolBar uiComp = event.getSource() ;
      UIBrowseContainer uiContainer = uiComp.getAncestorOfType(UIBrowseContainer.class) ;
      uiContainer.historyNext() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }  

  static public class BackActionListener extends EventListener<UIToolBar> {
    public void execute(Event<UIToolBar> event) throws Exception {
      UIToolBar uiComp = event.getSource() ;
      UIBrowseContainer uiContainer = uiComp.getAncestorOfType(UIBrowseContainer.class) ;
      uiContainer.historyBack() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }  

  static public class VoteActionListener extends EventListener<UIToolBar> {
    public void execute(Event<UIToolBar> event) throws Exception {
      UIToolBar uiComp = event.getSource() ;
      UIBrowseContainer container = uiComp.getAncestorOfType(UIBrowseContainer.class) ;
      UIDocumentDetail uiDocument = container.getChild(UIDocumentDetail.class)  ;
      UIApplication uiApp = uiComp.getAncestorOfType(UIApplication.class) ;
      if(!container.isShowDocumentDetail() || !uiDocument.isValidNode() ) {
        uiApp.addMessage(new ApplicationMessage("UIToolBar.msg.select-doc", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(container) ;
        return ;
      }
      if(!uiDocument.node_.isNodeType("mix:votable")) {
        uiApp.addMessage(new ApplicationMessage("UIToolBar.msg.not-support-vote", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;  
      }
      if((uiDocument.node_.isCheckedOut())) {
        UIBrowseContentPortlet cbPortlet = uiComp.getAncestorOfType(UIBrowseContentPortlet.class) ;
        UIPopupAction uiPopupAction = cbPortlet.getChildById("UICBPopupAction") ;
        uiPopupAction.activate(UICBVoteForm.class, 300) ;
        uiPopupAction.getChild(UIPopupWindow.class).setResizable(false) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      } else {
        uiApp.addMessage(new ApplicationMessage("UIToolBar.msg.readonly-doc", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(container) ;
    }
  }  
  static public class CommentActionListener extends EventListener<UIToolBar> {
    public void execute(Event<UIToolBar> event) throws Exception {
      UIToolBar uiComp = event.getSource() ;
      UIBrowseContainer container = uiComp.getAncestorOfType(UIBrowseContainer.class) ;
      UIDocumentDetail uiDocument = container.getChild(UIDocumentDetail.class)  ;
      UIApplication uiApp = uiComp.getAncestorOfType(UIApplication.class) ;
      if(!container.isShowDocumentDetail() || !uiDocument.isValidNode()) {
        uiApp.addMessage(new ApplicationMessage("UIToolBar.msg.select-doc", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(container) ;
        return ;
      } 
      if(!uiDocument.node_.isNodeType("mix:commentable")) {
        uiApp.addMessage(new ApplicationMessage("UIToolBar.msg.not-support-comment", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;        
      }
      if((uiDocument.node_.isCheckedOut())) {
        UIBrowseContentPortlet cbPortlet = uiComp.getAncestorOfType(UIBrowseContentPortlet.class) ;
        UIPopupAction uiPopupAction = cbPortlet.getChildById("UICBPopupAction") ;
        UICBCommentForm commentForm = uiComp.createUIComponent(UICBCommentForm.class, null, null) ;
        commentForm.setDocument(uiDocument.node_) ;
        uiPopupAction.activate(commentForm, 600, 0) ;
        uiPopupAction.getChild(UIPopupWindow.class).setResizable(false) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      } else {
        uiApp.addMessage(new ApplicationMessage("UIToolBar.msg.readonly-doc", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(container) ;
    }
  }  
}