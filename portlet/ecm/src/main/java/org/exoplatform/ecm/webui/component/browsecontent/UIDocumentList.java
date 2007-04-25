/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.util.Date;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.webui.component.UIComponentDecorator;
import org.exoplatform.webui.component.UIPageIterator;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Pham
 *          phamtuanchip@gmail.com
 * Jan 9, 2007  
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/browse/UIDocumentList.gtmpl",
    events = {@EventConfig(listeners = UIDocumentList.ViewDocumentActionListener.class)}
)
public class UIDocumentList extends UIComponentDecorator {
  private UIPageIterator uiPageIterator_ ;
  private Node docNode_ ;
  
  public UIDocumentList() throws Exception {
    uiPageIterator_ = createUIComponent(UIPageIterator.class, null, "ArticleListIterator");
    setUIComponent(uiPageIterator_);
  }
  
  public String getOwner(Node node) throws Exception { 
    return ((ExtendedNode)node).getACL().getOwner() ; 
  }  

  public void updateGrid(List<Node> list, int numbPerPage) throws Exception {
      ObjectPageList objPageList = new ObjectPageList(list , numbPerPage) ;
      uiPageIterator_.setPageList(objPageList) ;
  }
  public Node getDocNode() {return docNode_ ;}
  
  public void setDocNode(Node node) {docNode_ = node ;}

  public List getCurrentList() throws Exception {return uiPageIterator_.getCurrentPageData() ;}
  
  public UIPageIterator getUIPageIterator() {return uiPageIterator_ ;}
  
  static public class ViewDocumentActionListener extends EventListener<UIDocumentList> {
    public void execute(Event<UIDocumentList> event) throws Exception {
      UIDocumentList uiList = event.getSource() ;
      UIBrowseContainer uiContainer = uiList.getParent() ;
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      UIDocumentDetail uiDocumentDetail = uiContainer.getChild(UIDocumentDetail.class) ;
      Node docNode = uiContainer.getNodeByPath(nodePath) ;
      uiDocumentDetail.setNode(docNode) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDocumentDetail) ;
    }
  }
}
