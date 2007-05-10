/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * May 8, 2007 9:37:17 AM
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/explorer/UIRepositoryBrowse.gtmpl",
    events = @EventConfig(listeners = UIRepositoryBrowse.SelectRepoActionListener.class) 
      
  )

public class UIRepositoryBrowse extends UIContainer {
  private String defaultRepo_ = "repository" ;
  public UIRepositoryBrowse() throws Exception {
    // TODO Auto-generated constructor stub
  }
  public List<String> getRepository() throws Exception{
    RepositoryService rservice = getApplicationComponent(RepositoryService.class) ;
    List<String> repositories = new ArrayList<String>() ;
    repositories.add("repository") ;
    repositories.add("repository2") ;
    repositories.add("repository3") ;
    return repositories ;    
  }
  
  public static class SelectRepoActionListener extends EventListener<UIRepositoryBrowse>{
    public void execute(Event<UIRepositoryBrowse> evnet) throws Exception {
      
    }
    
  }
}
