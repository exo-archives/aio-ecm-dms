/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.sidebar;

import org.exoplatform.ecm.webui.component.explorer.UIDocumentInfo;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Sep 26, 2007  
 */
@ComponentConfig(
    template =  "app:/groovy/webui/component/explorer/sidebar/UITreeNodePageIterator.gtmpl",
    events = @EventConfig(listeners = UITreeNodePageIterator.ShowPageActionListener.class )    
)
public class UITreeNodePageIterator extends UIPageIterator {
  private String selectedPath_ ;
  
  public UITreeNodePageIterator() {    
  }
  
  public String getSelectedPath() { return selectedPath_ ; }
  public void setSelectedPath(String path) { this.selectedPath_ = path ; }
  @SuppressWarnings("unused")
  static  public class ShowPageActionListener extends EventListener<UITreeNodePageIterator> {
    public void execute(Event<UITreeNodePageIterator> event) throws Exception {      
      UITreeNodePageIterator uiPageIterator = event.getSource() ;      
      int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      uiPageIterator.setCurrentPage(page) ;      
      UIJCRExplorer explorer = uiPageIterator.getAncestorOfType(UIJCRExplorer.class);
      String currentPath = explorer.getCurrentNode().getPath();
      if(!currentPath.equalsIgnoreCase(uiPageIterator.getSelectedPath())) return ;      
      UIDocumentInfo documentInfo = explorer.findFirstComponentOfType(UIDocumentInfo.class);
      UIPageIterator iterator = documentInfo.getContentPageIterator();
      iterator.setCurrentPage(page);
      event.getRequestContext().addUIComponentToUpdateByAjax(documentInfo);
      if(uiPageIterator.getParent() == null) return ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPageIterator.getParent());
    }
  }    
}
