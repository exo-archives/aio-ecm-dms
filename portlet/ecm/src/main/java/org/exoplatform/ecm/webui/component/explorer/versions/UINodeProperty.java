/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.versions;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.Version;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorerPortlet;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Le Bien Thuy
 *          lebienthuy@gmail.com
 * Oct 20, 2006  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/component/explorer/versions/UINodeProperty.gtmpl"    
)
public class UINodeProperty extends UIForm{

  public UINodeProperty() {} 

  public List<Property> getVersionedNodeProperties() throws Exception{
    RepositoryService repositoryService = 
      (RepositoryService)PortalContainer.getComponent(RepositoryService.class) ;
    List<Property> list = new ArrayList<Property>() ;
    String repository = getAncestorOfType(UIJCRExplorerPortlet.class).getPreferenceRepository() ;
    NodeTypeManager nodeTypeManager = repositoryService.getRepository(repository).getNodeTypeManager() ;
    NodeType jcrFrozenNode = nodeTypeManager.getNodeType("nt:frozenNode") ;        
    NodeType ntVersion = nodeTypeManager.getNodeType("nt:version") ;
    NodeType ntVersionHistory = nodeTypeManager.getNodeType("nt:versionHistory") ;
    NodeType mixVersionable = nodeTypeManager.getNodeType("mix:versionable") ;
    UIVersionInfo uiVersionInfo = getAncestorOfType(UIVersionInfo.class) ;
    Version version_ = uiVersionInfo.getCurrentVersionNode().getVersion() ;
    Node frozenNode = version_.getNode("jcr:frozenNode") ;
    for(PropertyIterator propertyIter = frozenNode.getProperties(); propertyIter.hasNext() ;) {
      Property property = propertyIter.nextProperty() ;      
      boolean isDefinition = false ;
      for(PropertyDefinition propDef : jcrFrozenNode.getPropertyDefinitions()) {
        if(propDef.getName().equals(property.getName())) isDefinition = true ;
      }
      for(PropertyDefinition propDef : ntVersion.getPropertyDefinitions()) {
        if(propDef.getName().equals(property.getName())) isDefinition = true ;
      }
      for(PropertyDefinition propDef : ntVersionHistory.getPropertyDefinitions()) {
        if(propDef.getName().equals(property.getName())) isDefinition = true ;
      }
      for(PropertyDefinition propDef : mixVersionable.getPropertyDefinitions()) {
        if(propDef.getName().equals(property.getName())) isDefinition = true ;
      }
      if(!isDefinition) list.add(property) ;
    }
    return list ;
  }

  public String getPropertyValue(Property property) throws Exception{    
    switch(property.getType()) {
    case PropertyType.BINARY: return Integer.toString(PropertyType.BINARY) ; 
    case PropertyType.BOOLEAN :return Boolean.toString(property.getValue().getBoolean()) ;
    case PropertyType.DATE : return property.getValue().getDate().getTime().toString() ;
    case PropertyType.DOUBLE : return Double.toString(property.getValue().getDouble()) ;
    case PropertyType.LONG : return Long.toString(property.getValue().getLong()) ;
    case PropertyType.NAME : return property.getValue().getString() ;
    case PropertyType.STRING : return property.getValue().getString() ;
    case PropertyType.REFERENCE : {
      if(property.getName().equals("exo:category") || property.getName().equals("exo:relation")) {
        Session session = getSystemSession() ;
        Node referenceNode = session.getNodeByUUID(property.getValue().getString()) ;
        String path = referenceNode.getPath();
        session.logout() ;
        return path ;
      }
      return property.getValue().getString() ;
    }
    }
    return null ;
  }


  public List<String> getPropertyMultiValues(Property property) throws Exception {
    String propName = property.getName() ;    
    if(propName.equals("exo:category")) return getCategoriesValues(property) ;
    else if(propName.equals("exo:relation")) return getRelationValues(property) ;    
    List<String> values = new ArrayList<String>() ;
    for(Value value:property.getValues()) {
      values.add(value.getString()) ;
    }  
    return values ;
  }

  public boolean isMultiValue(Property prop) throws Exception{
    PropertyDefinition propDef = prop.getDefinition() ;
    return propDef.isMultiple() ;    
  }

  private List<String> getReferenceValues(Property property) throws Exception {
    Session session = getSystemSession() ;
    List<String> pathList = new ArrayList<String>() ;
    Value[] values = property.getValues() ;
    for(Value value:values) {
      Node referenceNode = session.getNodeByUUID(value.getString()) ;
      pathList.add(referenceNode.getPath()) ;
    }
    session.logout();
    return pathList ;
  }

  private List<String> getRelationValues(Property relationProp) throws Exception {
    return getReferenceValues(relationProp) ;
  }  

  private List<String> getCategoriesValues(Property categoryProp) throws Exception {
    return getReferenceValues(categoryProp) ;
  }

  private Session getSystemSession() throws Exception {
    String repository = getAncestorOfType(UIJCRExplorerPortlet.class).getPreferenceRepository() ;    
    ManageableRepository manageableRepository = 
      getApplicationComponent(RepositoryService.class).getRepository(repository) ;
    String systemWorksapce = manageableRepository.getConfiguration().getDefaultWorkspaceName();
    Session session = manageableRepository.getSystemSession(systemWorksapce) ;
    return session ;
  }
}