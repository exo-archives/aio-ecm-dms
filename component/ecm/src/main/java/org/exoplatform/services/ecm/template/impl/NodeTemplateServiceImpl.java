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
package org.exoplatform.services.ecm.template.impl;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.ecm.access.PermissionManagerService;
import org.exoplatform.services.ecm.template.NodeTemplateService;
import org.exoplatform.services.ecm.template.TemplateEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.registry.RegistryEntry;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
 * Created by The eXo Platform SAS
 * Author : TAN DUNG DANG
 *          dzungdev@gmail.com
 * May 13, 2008  
 */
public class NodeTemplateServiceImpl implements NodeTemplateService{

  private static String NODETYPE_NAME = "exo:nodeTypeName".intern() ;
  private static String LABEL = "exo:label".intern() ;
  private static String TEMPLATE_NAME = "exo:templateName".intern() ;
  private static String IS_DIALOG = "exo:isDialog".intern() ;
  private static String IS_DOCUMENT_TEMPLATE = "exo:isDocumentTemplate".intern() ;
  private static String PERMISSION = "exo:accessPermissions".intern() ;
  private static String TEMPLATE_DATA = "exo:templateData".intern() ; 

  private RegistryService registryService_ ;
  private PermissionManagerService permissionManagerService_ ;

  public NodeTemplateServiceImpl(RegistryService registryService, PermissionManagerService permissionManagerService) {
    registryService_ = registryService ;
    permissionManagerService_ = permissionManagerService ;
  } 

  public void addTemplate(TemplateEntry entry, String repository, SessionProvider sessionProvider) throws Exception {
    RegistryEntry registryEntry ;
    String groupPath = TEMPLATE_REGISTRY_PATH + "/" + entry.getNodeTypeName() ;
    if (entry.isDialog()) {
      groupPath += "/" + DIALOG_TYPE ;
    } else {
      groupPath += "/" + VIEW_TYPE ;
    }
    String entryPath = groupPath + "/" + entry.getTemplateName() ;

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

  public List<String> getDocumentNodeTypes(String repository, SessionProvider sessionProvider) throws Exception {
    //Search on exo:registryEntry->entry.getParent(dialogs).getParent(nodetype)
    ArrayList<String> documentNodeTypes = new ArrayList<String>() ; 
    String queryStr = "select * from exo:registryEntry where jcr:path LIKE '" + "/exo:registry/"+ TEMPLATE_REGISTRY_PATH +"/%'" ;
    Node regNode = registryService_.getRegistry(sessionProvider).getNode() ;
    Session session = regNode.getSession() ;
    QueryManager queryManager = session.getWorkspace().getQueryManager() ;
    Query query = queryManager.createQuery(queryStr, Query.SQL) ;
    QueryResult result = query.execute() ;
    NodeIterator nodeIterator = result.getNodes() ;
    while (nodeIterator.hasNext()) {
      Node entryNode = nodeIterator.nextNode() ;
      documentNodeTypes.add(entryNode.getParent().getParent().getName()) ;
    }
    return documentNodeTypes ;
  }

  public TemplateEntry getTemplate(String nodeTypeName, String templateName, boolean isDialog, String repository, SessionProvider sessionProvider) throws Exception {
    String entryPath = TEMPLATE_REGISTRY_PATH + "/" + nodeTypeName ;
    if (isDialog) {
      entryPath += "/" + DIALOG_TYPE + "/" + templateName ;
    } else {
      entryPath += "/" + VIEW_TYPE + "/" + templateName ;
    }
    RegistryEntry registryEntry = registryService_.getEntry(sessionProvider, entryPath) ;
    TemplateEntry tempEntry = new TemplateEntry();
    mapToTempalateEntry(registryEntry, tempEntry) ;
    return tempEntry ;
  }

  public String getTemplatePath(Node document, boolean isDialog, SessionProvider sessionProvider) throws Exception {
    String presentationNodeType = null ;
    if(document.isNodeType("exo:presentationable") && document.hasProperty("exo:presentationType")) {
      presentationNodeType = document.getProperty("exo:presentationType").getString() ;
    } else {
      presentationNodeType = document.getPrimaryNodeType().getName();
    }
    String userName = document.getSession().getUserID() ;
    String repository = ((ManageableRepository)document.getSession().getRepository()).getConfiguration().getName();    
    return getTemplatePathByUser(presentationNodeType, isDialog, userName, repository, sessionProvider) ;
  }

  public String getTemplatePath(String nodeTypeName, String templateName, boolean isDialog, String repository, SessionProvider sessionProvider) throws Exception {
    String tmp = null ;
    if(isDialog) {
      tmp = nodeTypeName + "/" + DIALOG_TYPE + "/" + templateName ;
    }else {
      tmp = nodeTypeName + "/" + VIEW_TYPE + "/" + templateName ;
    }
    try {
      Node templateRegistryHome = getTemplateRegistryHome(sessionProvider) ;
      return templateRegistryHome.getNode(tmp).getPath() ;
    } catch (PathNotFoundException e) {      
    }
    return null;
  }

  public String getTemplatePathByUser(String nodeTypeName, boolean isDialog, String userName, String repository, SessionProvider sessionProvider) throws Exception {
    String tmp = null ;
    if(isDialog) {
      tmp = nodeTypeName + "/" + DIALOG_TYPE ;
    }else {
      tmp = nodeTypeName + "/" + VIEW_TYPE ;
    }
    try {
      Node registryTemplateHome = getTemplateRegistryHome(sessionProvider) ;
      for(NodeIterator iterator = registryTemplateHome.getNode(tmp).getNodes(); iterator.hasNext();) {
        Node entry = iterator.nextNode();
        String templateName = entry.getName() ;
        TemplateEntry templateEntry = getTemplate(nodeTypeName, templateName, isDialog, repository, sessionProvider) ;
        if(permissionManagerService_.hasPermission(userName, templateEntry.getAccessPermissions())) {
          return entry.getPath() ;
        }
      }
    } catch (PathNotFoundException e) {
    }
    return null;
  }

  public List<String> getTemplatePaths(Node document, boolean isDialog, SessionProvider sessionProvider) throws Exception {
    String presentationNodeType = null ;
    if(document.isNodeType("exo:presentationable") && document.hasProperty("exo:presentationType")) {
      presentationNodeType = document.getProperty("exo:presentationType").getString() ;
    } else {
      presentationNodeType = document.getPrimaryNodeType().getName();
    }
    String userName = document.getSession().getUserID() ;
    String repository = ((ManageableRepository)document.getSession().getRepository()).getConfiguration().getName();
    return getTemplatePathsByUser(presentationNodeType, isDialog, userName, repository, sessionProvider) ;    
  }

  public List<String> getTemplatePathsByUser(String nodeTypeName, boolean isDialog, String userName, String repository, SessionProvider sessionProvider) throws Exception {
    List<String> paths = new ArrayList<String>() ;
    String tmp = null ;
    if(isDialog) {
      tmp = nodeTypeName + "/" + DIALOG_TYPE ;
    }else {
      tmp = nodeTypeName + "/" + VIEW_TYPE ;
    }
    try {
      Node registryTemplateHome = getTemplateRegistryHome(sessionProvider) ;
      for(NodeIterator iterator = registryTemplateHome.getNode(tmp).getNodes(); iterator.hasNext();) {
        Node entry = iterator.nextNode();
        String templateName = entry.getName() ;
        TemplateEntry templateEntry = getTemplate(nodeTypeName, templateName, isDialog, repository, sessionProvider) ;
        if(permissionManagerService_.hasPermission(userName, templateEntry.getAccessPermissions())) {
          paths.add(entry.getPath()) ;
        }
      }
    } catch (PathNotFoundException e) {
    }
    return paths;
  }

  public boolean isManagedNodeType(String nodeTypeName, String repository, SessionProvider sessionProvider) throws Exception {  
    Node templateRegistryHome = getTemplateRegistryHome(sessionProvider) ;
    if (templateRegistryHome.hasNode(nodeTypeName)) {
      return true ;
    }
    return false ;
  }

  public void removeManagedNodeType(String nodeTypeName, String repository, SessionProvider sessionProvider) throws Exception {
    Node templateRegistryHome = getTemplateRegistryHome(sessionProvider) ;
    templateRegistryHome.getNode(nodeTypeName).remove() ;
    templateRegistryHome.save() ;
  }

  public void removeTemplate(String nodeTypeName, String templateName, boolean isDialog, String repository, SessionProvider sessionProvider) throws Exception {
    String entryPath = TEMPLATE_REGISTRY_PATH + "/" + nodeTypeName ;
    if (isDialog) {
      entryPath += "/" + DIALOG_TYPE + "/" + templateName ;
    } else {
      entryPath += "/" + VIEW_TYPE + "/" + templateName ;
    }

    RegistryEntry registryEntry ;
    registryService_.removeEntry(sessionProvider, entryPath) ;
  }

  private Node getTemplateRegistryHome(SessionProvider sessionProvider) throws Exception {
    Node registryNode = registryService_.getRegistry(sessionProvider).getNode() ;
    return registryNode.getNode(TEMPLATE_REGISTRY_PATH) ;
  } 

  private void mapToRegistryEntry(TemplateEntry tempEntry, RegistryEntry registryEntry) {
    Document doc = registryEntry.getDocument() ;
    Element element = doc.getDocumentElement() ;
    setXMLNamespace(element,"xmlns:jcr", "http://www.jcp.org/jcr/1.0" );
    setXMLNamespace(element, "xmlns:exo", "http://www.exoplatform.com/jcr/exo/1.0") ;
    element.setAttribute(NODETYPE_NAME, tempEntry.getNodeTypeName()) ;
    element.setAttribute(LABEL, tempEntry.getLabel()) ;
    element.setAttribute(TEMPLATE_NAME, tempEntry.getTemplateName()) ;
    element.setAttribute(IS_DIALOG, Boolean.toString(tempEntry.isDialog())) ;
    element.setAttribute(IS_DOCUMENT_TEMPLATE, Boolean.toString(tempEntry.isDocumentTemplate())) ;
    element.setAttribute(PERMISSION, toXMLMultiValue(tempEntry.getAccessPermissions())) ;
    storeTemplateDataAsCDATA(doc, TEMPLATE_DATA, tempEntry.getTemplateData()) ;
  }

  private void mapToTempalateEntry(RegistryEntry registryEntry, TemplateEntry tempEntry) {
    Document doc = registryEntry.getDocument() ;
    Element element = doc.getDocumentElement() ;
    tempEntry.setDialog(Boolean.parseBoolean(element.getAttribute(IS_DIALOG))) ;
    tempEntry.setDocumentTemplate(Boolean.parseBoolean(element.getAttribute(IS_DOCUMENT_TEMPLATE))) ;
    tempEntry.setNodeTypeName(element.getAttribute(NODETYPE_NAME)) ;
    tempEntry.setLabel(element.getAttribute(LABEL)) ;
    tempEntry.setTemplateName(element.getAttribute(TEMPLATE_NAME)) ;
    tempEntry.setAccessPermissions(fromXMLMultiValue(element.getAttribute(PERMISSION))) ;
    tempEntry.setTemplateData(getCDATAValue(doc, TEMPLATE_DATA)) ;
  }

  private String toXMLMultiValue(List<String> list) {
    StringBuilder result = new StringBuilder() ;
    int size = list.size() ;
    for (int i = 0; i < size; i++) {
      result.append(list.get(i)) ;
      if (i < (size-1)) result.append(" ") ;
    }

    return result.toString() ;
  } 

  private ArrayList<String> fromXMLMultiValue(String attribute) {
    ArrayList<String> list = new ArrayList<String>() ;
    for(String value: attribute.split(" ")) {
      list.add(value) ;
    }

    return list ;
  }

  private void setXMLNamespace(Element element, String key, String value) {
    String xmlns = element.getAttribute(key) ;
    if (xmlns == null || xmlns.length() < 1) {
      element.setAttribute(key, value) ;
    }
  }

  private void storeTemplateDataAsCDATA(Document doc, String nameTag, String value) {
    org.w3c.dom.Node element = createElementData(doc, nameTag) ;
    org.w3c.dom.Node child;
    while ((child = element.getFirstChild()) != null ) {
      element.removeChild(child) ;
    } 
    org.w3c.dom.Node data =  doc.createCDATASection(value) ;
    element.appendChild(data) ;
  }

  private org.w3c.dom.Node createElementData(Document doc, String nameTag) {
    org.w3c.dom.Node element = doc.getElementsByTagName(nameTag).item(0) ;
    if (element == null) {
      element = doc.createElement(nameTag) ;
      doc.getDocumentElement().appendChild(element) ;
    }
    return element ;
  }

  private String getCDATAValue(Document doc, String cdataTagName) {
    org.w3c.dom.Node eleNode = doc.getElementsByTagName(cdataTagName).item(0) ;
    return eleNode.getFirstChild().getNodeValue() ;
  }

}
