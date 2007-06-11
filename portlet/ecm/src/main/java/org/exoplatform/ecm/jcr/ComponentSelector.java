/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.jcr;

import org.exoplatform.webui.core.UIComponent;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 16, 2006  
 */
public interface ComponentSelector {
  
  public void setComponent(UIComponent uicomponent, String[] initParams) ;
    
}
