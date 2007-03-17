/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

import org.exoplatform.faces.cms.component.JcrBrowser ;
import org.exoplatform.services.cms.BasePath ;
import org.exoplatform.services.cms.CmsConfigurationService ;

import org.exoplatform.services.cms.scripts.CmsScript ;

public class SetJCRBrowserForProductionWorkspaceAndScriptsPath implements CmsScript {
  
  private CmsConfigurationService cmsConfigurationService_;
  
  public SetJCRBrowserForProductionWorkspaceAndScriptsPath(CmsConfigurationService cmsConfigurationService) {
  	cmsConfigurationService_ = cmsConfigurationService ;
  }
  
  public void execute(Object context) {

    JcrBrowser jcrBrowser = (JcrBrowser) context ;
    String[] params = new String[2] ;
    params[0] = cmsConfigurationService_.getWorkspace() ;
    params[1] = cmsConfigurationService_.getJcrPath(BasePath.CMS_SCRIPTS_PATH) ;
    
    jcrBrowser.setParameters(params) ;
  }

  public void setParams(String[] params) {}

}
