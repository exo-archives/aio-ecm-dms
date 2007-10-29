/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.sidebar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.cms.folksonomy.FolksonomyService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 26, 2007 4:59:40 PM
 */
@ComponentConfig(
    template =  "app:/groovy/webui/component/explorer/sidebar/UITagExplorer.gtmpl",
    events = {@EventConfig(listeners = UITagExplorer.ViewTagActionListener.class)}
)
public class UITagExplorer extends UIContainer {
  
  public UITagExplorer() throws Exception {
    
  }
  
  public List<Node> getTagLink() throws Exception {
    FolksonomyService folksonomyService = getApplicationComponent(FolksonomyService.class) ;
    return folksonomyService.getAllTags(getRepository()) ;
  }
  public Map<String ,String> getTagStyle() throws Exception {
    FolksonomyService folksonomyService = getApplicationComponent(FolksonomyService.class) ;
    Map<String , String> tagStyle = new HashMap<String ,String>() ;
    for(Node tag : folksonomyService.getAllTagStyle(getRepository())) {
      tagStyle.put(tag.getName(), tag.getProperty("exo:htmlStyle").getValue().getString()) ;
    }
    return tagStyle ;
  }
  
  public String getRepository() { return getAncestorOfType(UIJCRExplorer.class).getRepositoryName();}
  
  static public class ViewTagActionListener extends EventListener<UITagExplorer> {
    public void execute(Event<UITagExplorer> event) throws Exception {
      UITagExplorer uiTagExplorer = event.getSource() ;
      String tagPath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRExplorer uiExplorer = uiTagExplorer.getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.setSelectNode(uiExplorer.getRootNode()) ;
      uiExplorer.setTagPath(tagPath) ;
      uiExplorer.setIsViewTag(true) ;
      uiExplorer.updateAjax(event) ;
    }
  }
}