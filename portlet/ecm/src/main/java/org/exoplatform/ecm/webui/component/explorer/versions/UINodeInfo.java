/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.versions;

import javax.jcr.Node;
import javax.jcr.version.Version;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.form.UIForm;
/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Oct 20, 2006  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,    
    template = "app:/groovy/webui/component/explorer/versions/UINodeInfo.gtmpl"    
)
public class UINodeInfo extends UIForm {
  
  private Node node_ ;
  private Version version_ ;
  
  public UINodeInfo() {
    
  }
  
  public void update() throws Exception {
    UIVersionInfo uiVersionInfo = getAncestorOfType(UIVersionInfo.class) ;
    node_ = uiVersionInfo.getCurrentNode() ;
    version_ = uiVersionInfo.getCurrentVersionNode().getVersion() ;   
  }
  
  public String getNodeType() throws Exception {
    return node_.getPrimaryNodeType().getName() ;
  }

  public String getNodeName () throws Exception {
    return node_.getName();
  }

  public String getVersionName() throws Exception {
    return getAncestorOfType(UIVersionInfo.class) .getCurrentVersionNode().getName();
  }
  
  public String getVersionLabels() throws Exception{
    UIVersionInfo uiVersionInfo = getAncestorOfType(UIVersionInfo.class) ;
    String[] labels = uiVersionInfo.getVersionLabels(uiVersionInfo.getCurrentVersionNode());
    StringBuffer label = new StringBuffer() ;
    if(labels.length  == 0 ) return "N/A" ;
    for(String temp : labels) {
      label.append(temp).append(" ") ;
    }
    return label.toString() ;
  }

  public String getVersionCreatedDate() throws Exception {
    return version_.getCreated().getTime().toString();
  }
}

