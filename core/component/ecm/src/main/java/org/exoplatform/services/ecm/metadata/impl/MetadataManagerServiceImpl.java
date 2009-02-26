/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.ecm.metadata.impl;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;

import org.apache.commons.logging.Log;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.ecm.metadata.MetadataManagerService;
import org.exoplatform.services.ecm.template.TemplateEntry;
import org.exoplatform.services.ecm.template.impl.NodeTemplatePlugin;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.registry.RegistryEntry;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.exoplatform.services.log.ExoLogger;
import org.picocontainer.Startable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * May 21, 2008  
 */
public class MetadataManagerServiceImpl implements MetadataManagerService, Startable {

  private static String METADATA_TYPE_NAME = "exo:nodeTypeName".intern() ;
  private static String LABEL = "exo:label".intern() ;
  private static String METADATA_NAME = "exo:templateName".intern() ;
  private static String IS_DIALOG = "exo:isDialog".intern() ;
  private static String PERMISSION = "exo:accessPermissions".intern() ;
  private static String METADATA_TEMPLATE = "exo:templateData".intern() ; 

  private RegistryService registryService_ ;
  private List<NodeTemplatePlugin> plugins_ = new ArrayList<NodeTemplatePlugin>() ;
  private Log log_ = ExoLogger.getLogger("ecm:metadataService") ;

  public MetadataManagerServiceImpl(RegistryService registryService) {
    registryService_ = registryService ;
  }

  public void addPlugin(ComponentPlugin plugin) {
    if (plugin instanceof NodeTemplatePlugin) {
      plugins_.add((NodeTemplatePlugin)plugin) ;
    }
  }

  public void addMetadataTemplate(TemplateEntry entry, String repository,SessionProvider sessionProvider) throws Exception {
    String groupPath = METADATA_TEMPLATE_REGISTRY + "/" + entry.getNodeTypeName() ;
    if (entry.isDialog()) {
      groupPath += "/" + DIALOG_TYPE ;
    } else {
      groupPath += "/" + VIEW_TYPE ;
    }
    String entryPath = groupPath + "/" + entry.getTemplateName() ;
    RegistryEntry registryEntry ;
    try{
      registryEntry = registryService_.getEntry(sessionProvider, entryPath) ;
      mapToRegistryEntry(entry, registryEntry) ;
      registryService_.recreateEntry(sessionProvider, groupPath, registryEntry) ;
    }catch(PathNotFoundException e) {
      registryEntry = new RegistryEntry(entry.getTemplateName()) ;
      mapToRegistryEntry(entry, registryEntry) ;
      registryService_.createEntry(sessionProvider, groupPath, registryEntry) ;
    }
  }

  public List<NodeType> getAllMetadataNodeType(String repository) throws Exception {
    List<NodeType> nodeTypeList = new ArrayList<NodeType>() ;
    ExtendedNodeTypeManager nodeTypeManager =  registryService_.getRepositoryService().getRepository(repository).getNodeTypeManager() ;
    for(NodeTypeIterator iterator =  nodeTypeManager.getMixinNodeTypes() ;iterator.hasNext(); ) {
      NodeType nt = iterator.nextNodeType() ;
      if (nt.isNodeType("exo:metadata")) nodeTypeList.add(nt) ;
    }
    return nodeTypeList ;
  }

  public String getMetadataPath(String metadataType, boolean isDialog, String repository, SessionProvider sessionProvider) throws Exception {
    String temp = null ;
    if(isDialog) {
      temp = metadataType + "/" + DIALOG_TYPE ;
    } else {
      temp = metadataType + "/" + VIEW_TYPE ;
    }
    Node registryTemplateHome = getTemplateRegistryHome(sessionProvider) ;
    for(NodeIterator iterator = registryTemplateHome.getNode(temp).getNodes(); iterator.hasNext() ;) {
      Node tempNode = iterator.nextNode() ;
      return tempNode.getPath() ;
    }
    return null ;
  }

  public List<String> getMetadataPaths(String metadataType, boolean isDialog, String repository,SessionProvider sessionProvider) throws Exception {
    List<String> metadataPathList = new ArrayList<String>() ;
    String temp = null ;
    if (isDialog) {
      temp = metadataType + "/" + DIALOG_TYPE ;
    } else {
      temp = metadataType + "/" + VIEW_TYPE ;
    }
    Node templateRegistryHome = getTemplateRegistryHome(sessionProvider) ;
    for (NodeIterator iterator = templateRegistryHome.getNode(temp).getNodes() ; iterator.hasNext(); ) {
      Node node = iterator.nextNode() ;
      metadataPathList.add(node.getPath()) ;
    }
    return metadataPathList ;
  }

  public boolean isManagedNodeType(String metadataType, String repository, SessionProvider sessionProvider) throws Exception {
    Node templateRegistryHome = getTemplateRegistryHome(sessionProvider) ;
    if (templateRegistryHome.hasNode(metadataType)) {
      return true;
    }
    return false ;
  }

  public void removeMetadataTemplateType(String nodetype, String repository, SessionProvider sessionProvider) throws Exception {
    String groupPath = METADATA_TEMPLATE_REGISTRY + "/" + nodetype ;
    registryService_.removeEntry(sessionProvider, groupPath) ;
  }

  private void mapToRegistryEntry(TemplateEntry templateEntry, RegistryEntry registryEntry) {
    Document doc = registryEntry.getDocument() ;
    Element element = doc.getDocumentElement() ;
    setXmlNamespace(element, "xmlns:exo", "http://www.exoplatform.com/jcr/exo/1.0") ;
    setXmlNamespace(element, "xmlns:jcr", "http://www.jcp.org/jcr/1.0") ;
    element.setAttribute(METADATA_TYPE_NAME, templateEntry.getNodeTypeName()) ;
    element.setAttribute(LABEL, templateEntry.getLabel()) ;
    element.setAttribute(METADATA_NAME, templateEntry.getTemplateName()) ;
    element.setAttribute(IS_DIALOG, Boolean.toString(templateEntry.isDialog())) ;
    element.setAttribute(PERMISSION, toXMLMultiValue(templateEntry.getAccessPermissions())) ;
    storeCDATA(doc, METADATA_TEMPLATE, templateEntry.getTemplateData()) ;
  }

  private String toXMLMultiValue(List<String> list) {
    StringBuilder result = new StringBuilder() ;
    for (int i = 0; i < list.size(); i++) {
      result.append(list.get(i)) ;
      if ( i < (list.size()-1)) {
        result.append(" ") ;
      }
    }

    return result.toString() ;
  }

  private void storeCDATA(Document doc, String attributeName, String value) {
    org.w3c.dom.Node node = doc.getElementsByTagName(attributeName).item(0) ;
    if (node == null) {
      node = doc.getDocumentElement().appendChild(doc.createElement(attributeName)) ;
    } else {
      if (node.getFirstChild() != null) {
        node.removeChild(node.getFirstChild()) ;
      }
    }
    node.appendChild(doc.createCDATASection(value)) ;
  }

  private Node getTemplateRegistryHome(SessionProvider sessionProvider) throws Exception {
    Node registryNode = registryService_.getRegistry(sessionProvider).getNode() ;
    return registryNode.getNode(METADATA_TEMPLATE_REGISTRY) ;
  }

  private void setXmlNamespace(Element element, String key, String value) {
    String xmlns = element.getAttribute(key) ;
    if (xmlns == null || xmlns.length() < 1 ) {
      element.setAttribute(key, value) ;
    }
  }

  public void start() {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    for (NodeTemplatePlugin plugin: plugins_) {
      try {
        String repository = plugin.getRepository() ;
        for(TemplateEntry entry: plugin.getTemplateEntries()) {
          addMetadataTemplate(entry, repository, sessionProvider) ;
        }
      } catch (Exception e) {
        log_.error("Error when load template from plugin: "+ plugin.getName(),e) ;
      } 
    }
    sessionProvider.close() ;
  }

  public void stop() {}
}
