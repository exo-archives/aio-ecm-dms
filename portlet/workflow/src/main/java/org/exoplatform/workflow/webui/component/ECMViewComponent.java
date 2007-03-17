/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.workflow.webui.component;

import java.util.List;
import javax.jcr.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jan 16, 2007  
 * 10:36:21 AM
 */
public interface ECMViewComponent {
  
  public void setNode(Node node);

  public Node getNode() throws Exception;

  public String getNodeType() throws Exception;

  public boolean isNodeTypeSupported();

  public String getTemplatePath() throws Exception;

  public List<Node> getRelations() throws Exception;

  public List<Node> getAttachments() throws Exception;

  public boolean hasPropertyContent(Node node, String property) ;

  public boolean isRssLink() ;

  public String getRssLink() ;

  public List getSupportedLocalise() throws Exception  ;
}
