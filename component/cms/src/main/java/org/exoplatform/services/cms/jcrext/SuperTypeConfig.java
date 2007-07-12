/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.jcrext;

import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Xuan Hoa
 *          hoa.pham@exoplatform.com
 * May 9, 2007  
 */
public class SuperTypeConfig {
  
  private String sourceNodeType ;
  private List<String> targetedNodeTypes ;
  
  public String getSourceNodeType() { return sourceNodeType; }
  public void setSourceNodeType(String sourceNodeType) {
    this.sourceNodeType = sourceNodeType;
  }
  
  public List<String> getTargetedNodeTypes() { return targetedNodeTypes; }
  public void setTargetedNodeTypes(List<String> targetedNodeTypes) {
    this.targetedNodeTypes = targetedNodeTypes;
  }  
}
