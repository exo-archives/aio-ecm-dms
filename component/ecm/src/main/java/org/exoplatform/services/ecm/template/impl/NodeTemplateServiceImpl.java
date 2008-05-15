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
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.exoplatform.services.ecm.access.PermissionManagerService;
import org.exoplatform.services.ecm.template.NodeTemplateService;
import org.exoplatform.services.ecm.template.TemplateEntry;
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
    String groupPath = TEMPLATE_REGISTRY_PATH + entry.getNodeTypeName() ;
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

  public List<String> getDocumentTemplates(String repository, SessionProvider sessionProvider) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public TemplateEntry getTemplate(String nodeTypeName, String templateName, boolean isDialog, String repository, SessionProvider sessionProvider) throws Exception {
    String entryPath = TEMPLATE_REGISTRY_PATH + nodeTypeName ;
    if (isDialog) {
      entryPath += "/" + DIALOG_TYPE + "/" + templateName ;
    } else {
      entryPath += "/" + VIEW_TYPE + "/" + templateName ;
    }

    TemplateEntry tempEntry = new TemplateEntry();
    RegistryEntry registryEntry ;

    registryEntry = registryService_.getEntry(sessionProvider, entryPath) ;
    mapToTempalateEntry(registryEntry, tempEntry) ;

    return tempEntry ;
  }

  public String getTemplatePath(Node node, boolean isDialog) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public String getTemplatePath(String nodeTypeName, String templateName, boolean isDialog, String repository, SessionProvider sessionProvider) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public String getTemplatePathByUser(String nodeTypeName, boolean isDialog, String userName, String repository, SessionProvider sessionProvider) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<String> getTemplatePaths(Node node, boolean isDialog) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<String> getTemplatePathsByUser(String nodeTypeName, boolean isDialog, String userName, String repository, SessionProvider sessionProvider) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isManagedNodeType(String nodeTypeName, String repository, SessionProvider sessionProvider) throws Exception {
    Node regNode = registryService_.getRegistry(sessionProvider).getNode() ;
    Session session = regNode.getSession() ;
    Node systemTemplateHome = (Node) session.getItem("/exo:registry/"+ TEMPLATE_REGISTRY_PATH) ;
    boolean isManagedNodeType = false ;
    if (systemTemplateHome.hasNode(nodeTypeName)) {
      isManagedNodeType = true ;
    }
    return isManagedNodeType ;
  }

  public void removeManagedNodeType(String nodeTypeName, String repository, SessionProvider sessionProvider) throws Exception {
    // TODO Auto-generated method stub

  }

  public void removeTemplate(String nodeTypeName, String templateName, boolean isDialog, String repository, SessionProvider sessionProvider) throws Exception {
    String entryPath = TEMPLATE_REGISTRY_PATH + nodeTypeName ;
    if (isDialog) {
      entryPath += "/" + DIALOG_TYPE + "/" + templateName ;
    } else {
      entryPath += "/" + VIEW_TYPE + "/" + templateName ;
    }

    RegistryEntry registryEntry ;
    registryService_.removeEntry(sessionProvider, entryPath) ;
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
