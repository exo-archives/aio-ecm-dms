/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

import java.util.ArrayList ;
import java.util.List ;
import java.util.Iterator;

import javax.jcr.Session;
import javax.jcr.Node ;

import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.core.model.SelectItemOption;

import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.scripts.CmsScript ;
import org.exoplatform.services.cms.BasePath;

import org.exoplatform.services.jcr.RepositoryService ;
import org.exoplatform.services.jcr.core.ManageableRepository ;


public class FillSelectBoxWithCalendarCategories implements CmsScript {
  
  private RepositoryService repositoryService_ ;
  private CmsConfigurationService cmsConfigService_ ;
  private String repository_ ;
  
  public FillSelectBoxWithCalendarCategories(RepositoryService repositoryService,
                                             CmsConfigurationService cmsConfigurationService) {
    repositoryService_ = repositoryService ;
    cmsConfigService_ = cmsConfigurationService ;
  }
  
  public void execute(Object context) {
    Session session = null ;
		try {
      UIFormSelectBox selectBox = (UIFormSelectBox) context;
      ManageableRepository jcrRepository = repositoryService_.getRepository(repository_);
      session = jcrRepository.getSystemSession(cmsConfigService_.getWorkspace(repository_));
      String path = cmsConfigService_.getJcrPath(BasePath.CALENDAR_CATEGORIES_PATH) ;
      Node calendar = (Node) session.getItem(path) ;
      List options = new ArrayList();
      if (calendar != null){
        Iterator iter = calendar.getNodes() ;
        while(iter.hasNext()) {
          Node categoryNode = iter.next() ;
          options.add(new SelectItemOption(categoryNode.getName() , categoryNode.getPath().substring(1)));
        }            
      }
      selectBox.setOptions(options);
      session.logout();
    } catch(Exception e) {
      if(session !=null) {
        session.logout() ;
      }
      selectBox.setOptions(new ArrayList<SelectItemOption<String>>()) ;
    }
  }
  
  public void setParams(String[] params) { repository_ = params[0]; }
}