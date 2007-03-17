/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

import java.util.List ;
import java.util.ArrayList ;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;

import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.model.SelectItemOption;

import org.exoplatform.services.cms.scripts.CmsScript;

public class FillSelectBoxWithWorkspaces implements CmsScript {
  
  private RepositoryService repositoryService_;
  
  public FillSelectBoxWithWorkspaces(RepositoryService repositoryService) {
    repositoryService_ = repositoryService;
  }
  
  public void execute(Object context) {
    UIFormSelectBox selectBox = (UIFormSelectBox) context;

    ManageableRepository jcrRepository = repositoryService_.getRepository();
    List options = new ArrayList();
    String[] workspaceNames = jcrRepository.getWorkspaceNames();
    for(i in 0..<workspaceNames.length) {
      String name = workspaceNames[i];
      options.add(new SelectItemOption(name, name));
    }            
    selectBox.setOptions(options);
  }

  public void setParams(String[] params) {}

}