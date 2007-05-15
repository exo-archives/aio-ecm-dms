/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.metadata;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.metadata.MetadataService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIPageIterator;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Sep 19, 2006
 * 11:57:24 AM 
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/admin/metadata/UIMetadataList.gtmpl",
    events = {
      @EventConfig(listeners = UIMetadataList.ViewActionListener.class),
      @EventConfig(listeners = UIMetadataList.EditActionListener.class),
      @EventConfig(listeners = UIMetadataList.DeleteActionListener.class, confirm="UIMetadataList.msg.confirm-delete"),
      @EventConfig(listeners = UIMetadataList.AddActionListener.class)
    }
)
public class UIMetadataList extends UIContainer {

  private List<String> metadatasName_ = new ArrayList<String>() ;
  
  final static public String METADATA_PATH = "metadataPath" ;
  final static public String METADATA_MAPPING = "metadataMapping" ;
  final static public String MAPPING = "mapping" ;
  
  final static public String[] ACTIONS = {"Add"} ;

  public UIMetadataList() throws Exception {
    addChild(UIPageIterator.class, null, "MetaDataListIterator") ;
  }

  public void updateGrid() throws Exception {
    UIPageIterator uiPageIterator = getChild(UIPageIterator.class) ;
    ObjectPageList pageList = new ObjectPageList(getAllMetadatas(), 10) ;
    uiPageIterator.setPageList(pageList) ;
  }
  
  @SuppressWarnings("unchecked")
  public List getAllMetadatas() throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    CmsConfigurationService cmsConfigService = getApplicationComponent(CmsConfigurationService.class) ;
    Session session = 
      repositoryService.getRepository().getSystemSession(cmsConfigService.getWorkspace()) ;
    NodeTypeManager ntManager = session.getWorkspace().getNodeTypeManager();
    MetadataService metadataService = getApplicationComponent(MetadataService.class) ;
    List<NodeType> metadatas = new ArrayList<NodeType>() ;
    metadatasName_ = metadataService.getMetadataList() ;
    for(int i = 0; i<metadatasName_.size(); i++) {
      metadatas.add(ntManager.getNodeType(metadatasName_.get(i).toString())) ;
    }
    return metadatas ; 
  }
  
  public List<String> getMetadatas() { return metadatasName_ ;  }
  
  public String[] getActions() { return ACTIONS ; }

  public List getListMetadata() throws Exception {
    return getChild(UIPageIterator.class).getCurrentPageData() ;
  }

  static public class AddActionListener extends EventListener<UIMetadataList> {
    public void execute(Event<UIMetadataList> event) throws Exception {
      UIMetadataList uiMetaList = event.getSource() ;
      UIMetadataManager uiManager = uiMetaList.getParent() ;
      uiManager.initPopup(true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  static public class ViewActionListener extends EventListener<UIMetadataList> {
    public void execute(Event<UIMetadataList> event) throws Exception {
      UIMetadataList uiMetaList = event.getSource() ;
      String metadataName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMetadataManager uiManager = uiMetaList.getParent() ;
      uiManager.removeChildById(UIMetadataManager.VIEW_METADATA_POPUP) ;
      uiManager.initViewPopup(metadataName) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  static public class EditActionListener extends EventListener<UIMetadataList> {
    public void execute(Event<UIMetadataList> event) throws Exception {
      UIMetadataList uiMetaList = event.getSource() ;
      String metadataName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMetadataManager uiManager = uiMetaList.getParent() ;
      uiManager.initPopup(false) ;
      UIMetadataForm uiForm = uiManager.findFirstComponentOfType(UIMetadataForm.class) ;
      uiForm.update(metadataName) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  
  static public class DeleteActionListener extends EventListener<UIMetadataList> {
    public void execute(Event<UIMetadataList> event) throws Exception {
      UIMetadataList uiMetaList = event.getSource() ;
      String metadataName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMetadataManager uiManager = uiMetaList.getParent() ;
      MetadataService metadataService = uiMetaList.getApplicationComponent(MetadataService.class) ;
      metadataService.removeMetadata(metadataName) ;
      uiMetaList.updateGrid() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
}
