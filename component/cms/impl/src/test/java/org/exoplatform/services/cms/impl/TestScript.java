/*
 * Created on Mar 24, 2006
 */
package org.exoplatform.services.cms.impl;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cms.scripts.CmsScript;
import org.exoplatform.services.jcr.impl.core.*;
import org.exoplatform.faces.core.component.UISelectBox;

/**
 * @author benjaminmestrallet
 */

public class TestScript extends BaseTest{
  
  public void testScript() throws Exception{    
    CmsScript script =  scriptService.getScript("FillSelectBoxWithWorkspaces.groovy");    
    UISelectBox us = new UISelectBox("type","",null);//
    script.execute(us);
  }
  
}
