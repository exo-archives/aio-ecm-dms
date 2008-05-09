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
package org.exoplatform.services.ecm.drive.impl;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.ecm.drive.DriveEntry;
import org.exoplatform.services.ecm.drive.DriveManagerService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.registry.RegistryEntry;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * Author : TAN DUNG DANG
 *          dzungdev@gmail.com
 * May 8, 2008  
 */
public class DriveManagerServiceImpl implements DriveManagerService{

  private static String WORKSPACE = "exo:workspace".intern() ;
  private static String PERMISSIONS = "exo:accessPermissions".intern() ;
  private static String VIEWS = "exo:views".intern() ;
  private static String ICON = "exo:icon".intern() ;
  private static String PATH = "exo:path".intern() ;
  private static String SHOW_REFERENCES = "exo:viewPreferences".intern() ;
  private static String SHOW_NON_DOCUMENT = "exo:viewNonDocument".intern() ;
  private static String SHOW_SIDEBAR = "exo:viewSideBar".intern() ;
  private static String SHOW_HIDDEN_NODE = "exo:showHiddenNode".intern() ;
  private static String ALLOW_CREATE_FOLDER = "exo:allowCreateFolder".intern() ;

  private RegistryService registryService_ ;

  public DriveManagerServiceImpl(RegistryService registryService) {
    this.registryService_ = registryService ;
  }

  public void addDrive(DriveEntry drive, SessionProvider sessionProvider) throws Exception {
    String groupPath = DRIVE_REGISTRY_PATH + "/" + drive.getGroup() ;
    String entryPath = DRIVE_REGISTRY_PATH + "/" + drive.getGroup() + "/" +drive.getName();
    RegistryEntry registryEntry ;
    try {
      registryEntry = registryService_.getEntry(sessionProvider, entryPath) ;
      Element element = registryEntry.getDocument().getDocumentElement() ;
      setXmlNameSpace(element, "xmlns:exo", "http://www.exoplatform.com/jcr/exo/1.0") ;
      setXmlNameSpace(element, "xmlns:jcr", "http://www.jcp.org/jcr/1.0") ;
      element.setAttribute(WORKSPACE, drive.getWorkspace()) ;
      element.setAttribute(ICON, drive.getIcon()) ;
      element.setAttribute(PATH, drive.getHomePath()) ;
      element.setAttribute(SHOW_REFERENCES, Boolean.toString(drive.getViewPreferences())) ;
      element.setAttribute(SHOW_NON_DOCUMENT, Boolean.toString(drive.getViewNonDocument())) ;
      element.setAttribute(SHOW_SIDEBAR, Boolean.toString(drive.getViewSideBar())) ;
      element.setAttribute(SHOW_HIDDEN_NODE, Boolean.toString(drive.getShowHiddenNode())) ;
      element.setAttribute(ALLOW_CREATE_FOLDER, drive.getAllowCreateFolder()) ;
      element.setAttribute(PERMISSIONS, toMultiValue(drive.getAccessPermissions())) ;
      element.setAttribute(VIEWS, toMultiValue(drive.getViews())) ;
      registryService_.recreateEntry(sessionProvider, groupPath, registryEntry) ;
    }catch(Exception e) {
      registryEntry = new RegistryEntry(drive.getName()) ;
      Element element = registryEntry.getDocument().getDocumentElement() ;
      setXmlNameSpace(element, "xmlns:exo", "http://www.exoplatform.com/jcr/exo/1.0") ;
      setXmlNameSpace(element, "xmlns:jcr", "http://www.jcp.org/jcr/1.0") ;
      element.setAttribute(WORKSPACE, drive.getWorkspace()) ;
      element.setAttribute(ICON, drive.getIcon()) ;
      element.setAttribute(PATH, drive.getHomePath()) ;
      element.setAttribute(SHOW_REFERENCES, Boolean.toString(drive.getViewPreferences())) ;
      element.setAttribute(SHOW_NON_DOCUMENT, Boolean.toString(drive.getViewNonDocument())) ;
      element.setAttribute(SHOW_SIDEBAR, Boolean.toString(drive.getViewSideBar())) ;
      element.setAttribute(SHOW_HIDDEN_NODE, Boolean.toString(drive.getShowHiddenNode())) ;
      element.setAttribute(ALLOW_CREATE_FOLDER, drive.getAllowCreateFolder()) ;
      element.setAttribute(PERMISSIONS, toMultiValue(drive.getAccessPermissions())) ;
      element.setAttribute(VIEWS, toMultiValue(drive.getViews())) ;
      registryService_.createEntry(sessionProvider, groupPath, registryEntry) ;
    }
  }

  public List<DriveEntry> getAllDrives(String repository, SessionProvider sessionProvider) throws Exception {
    List<DriveEntry> driveEntries = new ArrayList<DriveEntry>() ;
    Node regNode = registryService_.getRegistry(sessionProvider).getNode() ;
    Session session = regNode.getSession() ;
    String queryStr = "select * from exo:registry where jcr:path LIKE '" + "/exo:registry/" + DRIVE_REGISTRY_PATH +"/%'" ;
    QueryManager queryManager = session.getWorkspace().getQueryManager() ;
    Query query = queryManager.createQuery(queryStr, Query.SQL) ;
    QueryResult result = query.execute() ;
    NodeIterator nodeIterator = result.getNodes() ;
    System.out.println("\n\n ===========> Size of Node Iterator: "+ nodeIterator.getSize()) ;
    while (nodeIterator.hasNext()) {
      Node node = nodeIterator.nextNode() ;
      RegistryEntry registryEntry = registryService_.getEntry(sessionProvider, node.getPath()) ;
      Element element = registryEntry.getDocument().getDocumentElement() ;
      DriveEntry driveEntry = new DriveEntry() ;
      driveEntry.setWorkspace(element.getAttribute(WORKSPACE)) ;
      driveEntry.setIcon(element.getAttribute(ICON)) ;
      driveEntry.setHomePath(element.getAttribute(PATH)) ;
      driveEntry.setViewPreferences(Boolean.parseBoolean(element.getAttribute(SHOW_REFERENCES))) ;
      driveEntry.setAllowCreateFolder(element.getAttribute(ALLOW_CREATE_FOLDER)) ;
      driveEntry.setViewNonDocument(Boolean.parseBoolean(element.getAttribute(SHOW_NON_DOCUMENT))) ;
      driveEntry.setViewSideBar(Boolean.parseBoolean(element.getAttribute(SHOW_SIDEBAR))) ;
      driveEntry.setShowHiddenNode(Boolean.parseBoolean(element.getAttribute(SHOW_HIDDEN_NODE)));
      driveEntry.setAcessPermissions(fromMultiValue(element.getAttribute(PERMISSIONS))) ;
      driveEntry.setViews(fromMultiValue(element.getAttribute(VIEWS))) ;
      driveEntries.add(driveEntry) ;
    }
    
    return driveEntries ;
  }

  public DriveEntry getDrive(String repository, String group, String name, SessionProvider sessionProvider) throws Exception {
    DriveEntry driveEntry = new DriveEntry() ;
    String entryPath = DRIVE_REGISTRY_PATH + "/" + group + "/" + name ;
    RegistryEntry registryEntry = registryService_.getEntry(sessionProvider, entryPath) ;    
    //Debug code
    Node registryNode = registryService_.getRegistry(sessionProvider).getNode();
    System.out.println("==============>"+registryNode.getPath());
    System.out.println("==============>"+registryNode.getPrimaryNodeType().getName());
    Element doc = registryEntry.getDocument().getDocumentElement() ;
    driveEntry.setWorkspace(doc.getAttribute(WORKSPACE)) ;
    driveEntry.setIcon(doc.getAttribute(ICON)) ;
    driveEntry.setHomePath(doc.getAttribute(PATH)) ;
    driveEntry.setViewPreferences(Boolean.parseBoolean(doc.getAttribute(SHOW_REFERENCES))) ;
    driveEntry.setAllowCreateFolder(doc.getAttribute(ALLOW_CREATE_FOLDER)) ;
    driveEntry.setViewNonDocument(Boolean.parseBoolean(doc.getAttribute(SHOW_NON_DOCUMENT))) ;
    driveEntry.setViewSideBar(Boolean.parseBoolean(doc.getAttribute(SHOW_SIDEBAR))) ;
    driveEntry.setShowHiddenNode(Boolean.parseBoolean(doc.getAttribute(SHOW_HIDDEN_NODE)));
    driveEntry.setAcessPermissions(fromMultiValue(doc.getAttribute(PERMISSIONS))) ;
    driveEntry.setViews(fromMultiValue(doc.getAttribute(VIEWS))) ;

    return driveEntry;
  }

  // should naming method's name is getDrivesByGroup
  public List<DriveEntry> getDrives(String repository, String group, SessionProvider sessionProvider) throws Exception {
    List<DriveEntry> driveEntriesByGroup = new ArrayList<DriveEntry>() ;
    List<DriveEntry> allDriveEntrie = getAllDrives(repository, sessionProvider) ;
    for (DriveEntry drive : allDriveEntrie) {
      if (hasPermission(drive.getAccessPermissions(), group)) {
        driveEntriesByGroup.add(drive) ;
      }
    }

    return driveEntriesByGroup ;
  }

  public List<DriveEntry> getDrivesByUser(String repository, String userId, SessionProvider sessionProvider) throws Exception {
    return null ;
  }

  public void removeDrive(String repository, String group, String name, SessionProvider sessionProvider) throws Exception {
    String entryPath = DRIVE_REGISTRY_PATH + "/" + group + "/" + name ;
    registryService_.removeEntry(sessionProvider, entryPath) ;
    sessionProvider.close() ;
  }

  private String toMultiValue(List<String> list) {
    int size = list.size() ;
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < size; i++) {
      builder.append(list.get(i)) ;
      if (i < (size - 1)) builder.append(" ");
    }
    return builder.toString() ;
  }

  private ArrayList<String> fromMultiValue(String attribute) {
    ArrayList<String> list = new ArrayList<String>() ;
    for (String value : attribute.split(" ")) {
      list.add(value);
    }
    return list;
  }

  private boolean hasPermission(ArrayList<String> permissionList, String permission) {
    if (permission == null) return false ;
    if (permission.indexOf(":/") > -1) {
      String[] array = permission.split(":/") ;
      if (array == null || array.length < 2) return false ;
      if (permissionList.contains("*:/"+array[1])) return true ;
    }

    return permissionList.contains(permission) ;
  }
  
  private void setXmlNameSpace(Element element, String key, String value) {
    String xmlns = element.getAttribute(key) ; 
    if(xmlns == null || xmlns.trim().length() < 1) {
      element.setAttribute(key, value) ;
    }    
  }
}
