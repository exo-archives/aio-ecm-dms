/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.info;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.webui.component.UIGrid;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : nqhungvn
 *          nguyenkequanghung@yahoo.com
 * July 3, 2006
 * 10:07:15 AM
 * Edit: lxchiati 2006/10/16
 * Edit: phamtuan Oct 27, 2006
 */

@ComponentConfig(
  template =  "app:/groovy/webui/component/explorer/popup/info/UIReferencesList.gtmpl",
  events = { @EventConfig (listeners = UIReferencesList.CloseActionListener.class)}
)

public class UIReferencesList extends UIGrid implements UIPopupComponent{
  
  private static String[] REFERENCES_BEAN_FIELD = {"workspace", "path"} ;
  
  public UIReferencesList() throws Exception {}
  
  public void activate() throws Exception {
    configure("workspace", REFERENCES_BEAN_FIELD, null) ;
    updateGrid() ;
  }  
  
  public void deActivate() {}
  
  public void updateGrid() throws Exception {
    List<ReferenceBean> referBeans = new ArrayList<ReferenceBean>() ; 
    UIJCRExplorer uiJCRExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    ExtendedNode node = (ExtendedNode)uiJCRExplorer.getCurrentNode() ;
    PropertyIterator referIterator ;
    try {
      referIterator = node.getReferences() ;
    } catch (Exception e) {
      referIterator = null ;
    }
    while (referIterator != null && referIterator.hasNext() ) {        
      Property refer = referIterator.nextProperty() ;
      referBeans.add(new ReferenceBean(refer.getSession().getWorkspace().getName(), refer.getPath())) ;
    }
    ObjectPageList objPageList = new ObjectPageList(referBeans, 10) ;
    getUIPageIterator().setPageList(objPageList) ; 
  }
  
  static public class CloseActionListener extends EventListener<UIReferencesList> {
    public void execute(Event<UIReferencesList> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;
    }
  }
  
  public class ReferenceBean {    
    private String workspace_ ;
    private String path_ ;
    
    public ReferenceBean ( String workspace, String path) {
      workspace_ = workspace ;
      path_ = path ;
    }
    
    public String getPath() { return path_ ;}
    public void setPath(String path) { this.path_ = path ;}
    
    public String getWorkspace() { return workspace_ ;}
    public void setWorkspace(String workspace) { this.workspace_ = workspace ;}
  }
}