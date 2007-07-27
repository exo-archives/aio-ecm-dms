/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.services.cms.folksonomy.FolksonomyService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * 18-07-2007  
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/browse/UITagList.gtmpl",
    events = {
      @EventConfig(listeners = UITagList.ViewByTagActionListener.class)
    }
)
public class UITagList extends UIComponent {
  private String tagPath_;

  public UITagList() {}
   
  public List<Node> getTagLink() throws Exception {
    String repository = getRepository() ;
    FolksonomyService folksonomyService = getApplicationComponent(FolksonomyService.class) ;
    return folksonomyService.getAllTags(repository) ;
  }
  public Map<String ,String> getTagStyle() throws Exception {
    String repository = getRepository() ;
    FolksonomyService folksonomyService = getApplicationComponent(FolksonomyService.class) ;
    Map<String , String> tagStyle = new HashMap<String ,String>() ;
    for(Node tag : folksonomyService.getAllTagStyle(repository)) {
      tagStyle.put(tag.getName(), tag.getProperty("exo:htmlStyle").getValue().getString()) ;
    }
    return tagStyle ;
  }
  public String getRepository() { return getAncestorOfType(UIBrowseContainer.class).getRepository();} 
  public String getTagPath() {return tagPath_ ;}

  public void setTagPath(String tagName) {tagPath_ = tagName ;}

  static public class ViewByTagActionListener extends EventListener<UITagList> {
    public void execute(Event<UITagList> event) throws Exception {
      UITagList uiTaglist = event.getSource() ;
      String tagPath = event.getRequestContext().getRequestParameter(OBJECTID);
      UIBrowseContainer uiContainer = uiTaglist.getAncestorOfType(UIBrowseContainer.class) ;
      uiContainer.setShowDocumentByTag(true) ;
      uiContainer.setTagPath(tagPath) ;
      uiContainer.setPageIterator(uiContainer.getDocumentByTag()) ;
      uiContainer.storeHistory() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }
}
