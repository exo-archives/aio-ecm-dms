/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.browsecontent.UICBSearchResults.ResultData;
import org.exoplatform.ecm.webui.component.explorer.UIPopupAction;
import org.exoplatform.webui.component.UIContainer;
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

  public UIToolBar()throws Exception {
    addChild(UICBSearchForm.class, null, null).setRendered(false) ;
    addChild(UICBSearchResults.class, null, null).setRendered(false) ;
  }

  public void setShowHiddenSearch() throws Exception {
    UICBSearchForm uiSearch = getChild(UICBSearchForm.class) ;
    uiSearch.reset() ;
    UICBSearchResults uiSearchResults = getChild(UICBSearchResults.class) ;
    List<ResultData> queryResult = new ArrayList<ResultData>() ;
    uiSearchResults.updateGrid(queryResult) ;
    uiSearch.setRendered(!uiSearch.isRendered()) ;
    uiSearchResults.setRendered(!uiSearchResults.isRendered()) ;
    UIBrowseContainer container = getAncestorOfType(UIBrowseContainer.class) ;
    container.setShowSearchForm(!container.isShowSearchForm()) ;
  }

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

  public Node getCurrentNode() {
    UIBrowseContainer uiContainer = getAncestorOfType(UIBrowseContainer.class) ;
    return uiContainer.getCurrentNode() ;}

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
      uiComp.setShowHiddenSearch() ;
    }
  }  
  static public class VoteActionListener extends EventListener<UIToolBar> {
    public void execute(Event<UIToolBar> event) throws Exception {
      UIToolBar uiComp = event.getSource() ;
      UIBrowseContainer container = uiComp.getAncestorOfType(UIBrowseContainer.class) ;
      UIDocumentDetail uiDocument = container.getChild(UIDocumentDetail.class)  ;
      if(uiDocument.isRendered()) {
        if(uiDocument.getNode().isNodeType("mix:votable")) {
          UIBrowseContentPortlet cbPortlet = uiComp.getAncestorOfType(UIBrowseContentPortlet.class) ;
          UIPopupAction uiPopupAction = cbPortlet.getChild(UIPopupAction.class) ;
          uiPopupAction.activate(UICBVoteForm.class, 300) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        }
      }
    }
  }  
  static public class CommentActionListener extends EventListener<UIToolBar> {
    public void execute(Event<UIToolBar> event) throws Exception {
      UIToolBar uiComp = event.getSource() ;
      UIBrowseContainer container = uiComp.getAncestorOfType(UIBrowseContainer.class) ;
      UIDocumentDetail uiDocument = container.getChild(UIDocumentDetail.class)  ;
      if (uiDocument.isRendered()) {
        if(uiDocument.getNode().isNodeType("mix:commentable")) {
          UIBrowseContentPortlet cbPortlet = uiComp.getAncestorOfType(UIBrowseContentPortlet.class) ;
          UIPopupAction uiPopupAction = cbPortlet.getChild(UIPopupAction.class) ;
          UICBCommentForm commentForm = uiComp.createUIComponent(UICBCommentForm.class, null, null) ;
          commentForm.setDocument(uiDocument.getNode()) ;
          uiPopupAction.activate(commentForm, 600, 0) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        }
      }
    }
  }  
}