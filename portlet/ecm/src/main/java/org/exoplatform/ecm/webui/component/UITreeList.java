/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 17, 2006
 * 9:32:56 AM 
 */
public abstract class UITreeList extends UIContainer {
  
  private List<Node> nodes_ ;
  private String[] arrType_ ;
  private String[] arrMimeType_ ;
  
  public UITreeList() throws Exception {
  }
  
  public void setNodeList(List<Node> nodes) { nodes_ = nodes ;  }
  public List<Node> getNodeList() {return nodes_; } ; 
  
  public void setFilterType(String[] arrType) { arrType_ = arrType ; }
  public String[] getFilterType() { return arrType_ ;};
  
  public void setMimeTypes(String[] arrMimeType) { arrMimeType_ = arrMimeType ; }
  public String[] getMimeTypes() { return arrMimeType_ ; }
}