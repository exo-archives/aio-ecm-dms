/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.services.cms.metadata.MetadataService;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
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
      @EventConfig(listeners = UIMetadataList.DeleteActionListener.class, confirm="UIMetadataList.msg.confirm-delete")
    }
)
public class UIMetadataList extends UIContainer {

  final static public String INTERNAL_USE = "exo:internalUse".intern() ;

  public UIMetadataList() throws Exception {
    addChild(UIPageIterator.class, null, "MetaDataListIterator") ;
  }

  public void updateGrid() throws Exception {
    UIPageIterator uiPageIterator = getChild(UIPageIterator.class) ;
    ObjectPageList pageList = new ObjectPageList(getAllMetadatas(), 10) ;
    uiPageIterator.setPageList(pageList) ;
  }
  
  @SuppressWarnings("unchecked")

  public List<Metadata> getAllMetadatas() throws Exception {
    List<Metadata> metadatas = new ArrayList<Metadata>() ;
    MetadataService metadataService = getApplicationComponent(MetadataService.class) ;
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    List<NodeType> nodetypes = metadataService.getAllMetadatasNodeType(repository) ;
    Collections.sort(nodetypes, new Utils.NodeTypeNameComparator()) ;
    for(NodeType nt : nodetypes) {
      Metadata mt = new Metadata() ;
      mt.setName(nt.getName()) ;
      mt.isTemplate(metadataService.hasMetadata(nt.getName(), repository)) ;
      PropertyDefinition def =((ExtendedNodeType)nt).getPropertyDefinitions(INTERNAL_USE).getAnyDefinition() ;
      if(def.getDefaultValues()[0].getBoolean()) mt.setInternalUse("True") ;
      else mt.setInternalUse("False") ;
      metadatas.add(mt) ;
    }
    return metadatas ; 
  }
  
  public List getListMetadata() throws Exception {
    return getChild(UIPageIterator.class).getCurrentPageData() ;
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
      uiManager.initPopup() ;
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
      String repository = uiMetaList.getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
      MetadataService metadataService = uiMetaList.getApplicationComponent(MetadataService.class) ;
      metadataService.removeMetadata(metadataName, repository) ;
      uiMetaList.updateGrid() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
      UIApplication uiApp = uiMetaList.getAncestorOfType(UIApplication.class) ;
      Object[] args = {metadataName} ;
      uiApp.addMessage(new ApplicationMessage("UIMetadataList.msg.delete-successful", args)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
    }
  }
  public class Metadata{
    private String name ;
    private String internalUse ;
    private boolean hasTemplate = false;
    
    public Metadata() {}
    
    public String getName() { return name ;}
    public void setName(String n) { name = n ; }
    
    public String getInternalUse() { return internalUse ;}
    public void setInternalUse(String inter) { internalUse = inter ; }
    
    public boolean hasTemplate() { return hasTemplate ; }
    public void isTemplate(boolean isTemplate) { hasTemplate = isTemplate ; }
  }
}