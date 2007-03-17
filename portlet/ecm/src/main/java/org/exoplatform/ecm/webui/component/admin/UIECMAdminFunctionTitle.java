/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin;

import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@exoplatform.com
 * Sep 19, 2006
 * 8:26:51 AM 
 */
@ComponentConfig( template = "app:/groovy/webui/component/admin/UIECMAdminFunctionTitle.gtmpl" )
public class UIECMAdminFunctionTitle extends UIComponent {
  public UIECMAdminFunctionTitle() {}
  
  public String getRenderedChildLabel() {
    return this.<UIECMAdminPortlet>getParent().getRenderedCompName() ;
  }
}