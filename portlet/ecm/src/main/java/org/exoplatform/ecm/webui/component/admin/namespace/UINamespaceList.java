/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.namespace;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.NamespaceRegistry;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan
 *          phamtuanchip@yahoo.de
 * September 20, 2006
 * 16:37:15 
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/UIGridWithButton.gtmpl",
    events = {@EventConfig(listeners = UINamespaceList.AddNamespaceActionListener.class)}
)

public class UINamespaceList extends UIGrid {

  private static String[] NAMESPACE_BEAN_FIELD = {"prefix", "uri"} ;

  public UINamespaceList() throws Exception { 
    getUIPageIterator().setId("NamespaceListIterator") ;
    configure("prefix", NAMESPACE_BEAN_FIELD, null) ;
  }
  
  public String[] getActions() { return new String[] {"AddNamespace"} ;}
  
  public void updateGrid () throws Exception {
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    NamespaceRegistry namespaceRegistry = getApplicationComponent(RepositoryService.class)
                                          .getRepository(repository).getNamespaceRegistry() ;
    List<NamespaceBean> nspBeans = new ArrayList<NamespaceBean>();
    String[] prefixs = namespaceRegistry.getPrefixes();
    for(int i = 0; i < prefixs.length - 1; i++){      
      NamespaceBean bean = new NamespaceBean(prefixs[i], namespaceRegistry.getURI(prefixs[i])) ;
      nspBeans.add(bean) ;
    }
    ObjectPageList objPageList = new ObjectPageList(nspBeans, 10) ;
    getUIPageIterator().setPageList(objPageList) ; 
  }   

  static public class AddNamespaceActionListener extends EventListener<UINamespaceList> {
    public void execute(Event<UINamespaceList> event) throws Exception {
      UINamespaceManager uiManager = event.getSource().getParent() ;
      uiManager.initPopup() ;
      UINamespaceForm uiForm = uiManager.findFirstComponentOfType(UINamespaceForm.class) ;
      uiForm.reset() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  public static class NamespaceBean {
    private String prefix ;
    private String uri ;
    
    public NamespaceBean(String prefix, String uri){
      this.prefix = prefix ;
      this.uri = uri ;
    }
    
    public String getPrefix () { return prefix ;}
    public String getUri () { return uri ;}
  }
}