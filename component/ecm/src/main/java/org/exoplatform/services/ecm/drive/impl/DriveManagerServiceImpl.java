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
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.ecm.access.PermissionManagerService;
import org.exoplatform.services.ecm.drive.DriveEntry;
import org.exoplatform.services.ecm.drive.DriveManagerService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.registry.RegistryEntry;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.exoplatform.services.log.ExoLogger;
import org.picocontainer.Startable;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * Author : TAN DUNG DANG
 *          dzungdev@gmail.com
 * May 8, 2008  
 */
public class DriveManagerServiceImpl implements DriveManagerService, Startable {

  private static String WORKSPACE = "exo:workspace".intern() ;
  private static String PERMISSIONS = "exo:accessPermissions".intern() ;
  private static String VIEWS = "exo:views".intern() ;
  private static String ICON = "exo:iconPath".intern() ;
  private static String PATH = "exo:homePath".intern() ;
  private static String SHOW_REFERENCES = "exo:showReference".intern() ;
  private static String SHOW_NON_DOCUMENT = "exo:showNonDocument".intern() ;
  private static String SHOW_SIDEBAR = "exo:showSideBar".intern() ;
  private static String SHOW_HIDDEN_NODE = "exo:showHiddenNode".intern() ;
  private static String ALLOW_CREATE_FOLDER = "exo:allowCreateFolder".intern() ;
  private static String DRIVE_NAME = "exo:name".intern() ;
  private static String GROUP = "exo:group".intern() ;
  private static String REPOSITORY = "exo:repository".intern() ;

  private RegistryService registryService_ ;
  private PermissionManagerService permissionMgtService_ ;
  private List<DriveManagerPlugin> drivePlugins_ = new ArrayList<DriveManagerPlugin>() ; 

  private Log log_ = ExoLogger.getLogger("ecm:driveManagerService") ;

  public DriveManagerServiceImpl(RegistryService registryService, PermissionManagerService permissionMgtService) {
    this.registryService_ = registryService ;
    this.permissionMgtService_ = permissionMgtService ;
  }

  public void addPlugin(ComponentPlugin plugin) {
    if(plugin instanceof DriveManagerPlugin) {      
      drivePlugins_.add((DriveManagerPlugin)plugin) ;
    }
  }

  public void addDrive(DriveEntry drive, SessionProvider sessionProvider) throws Exception {
    String groupPath = DRIVE_REGISTRY_PATH + "/" + drive.getGroup() ;
    String entryPath = DRIVE_REGISTRY_PATH + "/" + drive.getGroup() + "/" +drive.getName();
    RegistryEntry registryEntry ;
    try {
      registryEntry = registryService_.getEntry(sessionProvider, entryPath) ;      
      mapToRegistryEntry(drive, registryEntry) ;
      registryService_.recreateEntry(sessionProvider, groupPath, registryEntry) ;
    }catch(PathNotFoundException e) {
      registryEntry = new RegistryEntry(drive.getName()) ;     
      mapToRegistryEntry(drive, registryEntry) ;
      registryService_.createEntry(sessionProvider, groupPath, registryEntry) ;
    }
  }

  public List<DriveEntry> getAllDrives(String repository, SessionProvider sessionProvider) throws Exception {
    List<DriveEntry> driveEntries = new ArrayList<DriveEntry>() ;
    Node regNode = registryService_.getRegistry(sessionProvider).getNode() ;
    Session session = regNode.getSession() ;
    String queryStr = "select * from exo:registryEntry where jcr:path LIKE '" + "/exo:registry/" + DRIVE_REGISTRY_PATH +"/%'" ;

    QueryManager queryManager = session.getWorkspace().getQueryManager() ;
    Query query = queryManager.createQuery(queryStr, Query.SQL) ;
    QueryResult result = query.execute() ;
    NodeIterator nodeIterator = result.getNodes() ;
    while (nodeIterator.hasNext()) {
      Node node = nodeIterator.nextNode() ;
      String entryPath = node.getPath().substring("/exo:registry/".length()) ;
      RegistryEntry registryEntry = registryService_.getEntry(sessionProvider, entryPath) ;      
      DriveEntry driveEntry = new DriveEntry() ;
      mapToDriveEntry(registryEntry, driveEntry) ;
      driveEntries.add(driveEntry) ;
    }

    return driveEntries ;
  }

  public DriveEntry getDrive(String repository, String group, String name, SessionProvider sessionProvider) throws Exception {
    DriveEntry driveEntry = new DriveEntry() ;
    String entryPath = DRIVE_REGISTRY_PATH + "/" + group + "/" + name ;
    RegistryEntry registryEntry = registryService_.getEntry(sessionProvider, entryPath) ;   
    mapToDriveEntry(registryEntry, driveEntry) ;
    return driveEntry;
  }

  // should naming method's name is getDrivesByGroup
  public List<DriveEntry> getDrivesByGroup(String repository, String group, SessionProvider sessionProvider) throws Exception {
    List<DriveEntry> driveEntries = new ArrayList<DriveEntry>() ;
    Node regNode = registryService_.getRegistry(sessionProvider).getNode() ;
    Session session = regNode.getSession() ;
    QueryManager queryManager = session.getWorkspace().getQueryManager() ;
    String queryStr = "select * from exo:registryEntry where jcr:path LIKE '/exo:registry/" + DRIVE_REGISTRY_PATH + "/" + group + "/%'" ;
    Query query = queryManager.createQuery(queryStr, Query.SQL) ;
    QueryResult result = query.execute() ;
    NodeIterator nodeIterator = result.getNodes() ;
    while (nodeIterator.hasNext()) {
      Node node = nodeIterator.nextNode() ;
      String entryPath = node.getPath().substring("/exo:registry/".length()) ;        
      RegistryEntry  registryEntry = registryService_.getEntry(sessionProvider, entryPath) ;   
      DriveEntry driveEntry = new DriveEntry() ;
      mapToDriveEntry(registryEntry, driveEntry) ;
      driveEntries.add(driveEntry) ;
    }
    return driveEntries ;
  }

  public List<DriveEntry> getDrivesByUser(String repository, String userId, SessionProvider sessionProvider) throws Exception {
    List<DriveEntry> userDrives = new ArrayList<DriveEntry>() ;
    for(DriveEntry driveEntry: getAllDrives(repository, sessionProvider)) {
      if(permissionMgtService_.hasPermission(userId, driveEntry.getAccessPermissions())) {
        userDrives.add(driveEntry) ;
      }
    }
    return userDrives ;
  }

  public void removeDrive(String repository, String group, String name, SessionProvider sessionProvider) throws Exception {
    String entryPath = DRIVE_REGISTRY_PATH + "/" + group + "/" + name ;
    registryService_.removeEntry(sessionProvider, entryPath) ;
  }

  private String toXMLMultiValue(List<String> list) {
    int size = list.size() ;
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < size; i++) {
      builder.append(list.get(i)) ;
      if (i < (size - 1)) builder.append(" ");
    }
    return builder.toString() ;
  }

  private ArrayList<String> fromXMLMultiValue(String attribute) {
    ArrayList<String> list = new ArrayList<String>() ;
    for (String value : attribute.split(" ")) {
      list.add(value);
    }
    return list;
  }

  private void setXmlNamespace(Element element, String key, String value) {
    String xmlns = element.getAttribute(key) ; 
    if(xmlns == null || xmlns.trim().length() < 1) {
      element.setAttribute(key, value) ;
    }    
  }

  private void mapToRegistryEntry(DriveEntry drive, RegistryEntry registryEntry) {
    Element element = registryEntry.getDocument().getDocumentElement() ;
    setXmlNamespace(element, "xmlns:exo", "http://www.exoplatform.com/jcr/exo/1.0") ;
    setXmlNamespace(element, "xmlns:jcr", "http://www.jcp.org/jcr/1.0") ;
    element.setAttribute(DRIVE_NAME, drive.getName()) ;
    element.setAttribute(GROUP, drive.getGroup()) ;
    element.setAttribute(REPOSITORY, drive.getRepository()) ;
    element.setAttribute(WORKSPACE, drive.getWorkspace()) ;
    element.setAttribute(ICON, drive.getIconPath()) ;
    element.setAttribute(PATH, drive.getHomePath()) ;
    element.setAttribute(SHOW_REFERENCES, Boolean.toString(drive.getShowPreferences())) ;
    element.setAttribute(SHOW_NON_DOCUMENT, Boolean.toString(drive.getShowNonDocument())) ;
    element.setAttribute(SHOW_SIDEBAR, Boolean.toString(drive.getShowSideBar())) ;
    element.setAttribute(SHOW_HIDDEN_NODE, Boolean.toString(drive.getShowHiddenNode())) ;
    element.setAttribute(ALLOW_CREATE_FOLDER, drive.getAllowCreateFolder()) ;
    element.setAttribute(PERMISSIONS, toXMLMultiValue(drive.getAccessPermissions())) ;
    element.setAttribute(VIEWS, toXMLMultiValue(drive.getViews())) ;
  }

  private void mapToDriveEntry(RegistryEntry registryEntry, DriveEntry drive) {
    Element element = registryEntry.getDocument().getDocumentElement() ;
    drive.setName(element.getAttribute(DRIVE_NAME)) ;
    drive.setGroup(element.getAttribute(GROUP)) ;
    drive.setRepository(element.getAttribute(REPOSITORY)) ;
    drive.setWorkspace(element.getAttribute(WORKSPACE)) ;
    drive.setIconPath(element.getAttribute(ICON)) ;
    drive.setHomePath(element.getAttribute(PATH)) ;
    drive.setShowPreferences(Boolean.parseBoolean(element.getAttribute(SHOW_REFERENCES))) ;
    drive.setAllowCreateFolder(element.getAttribute(ALLOW_CREATE_FOLDER)) ;
    drive.setShowNonDocument(Boolean.parseBoolean(element.getAttribute(SHOW_NON_DOCUMENT))) ;
    drive.setShowSideBar(Boolean.parseBoolean(element.getAttribute(SHOW_SIDEBAR))) ;
    drive.setShowHiddenNode(Boolean.parseBoolean(element.getAttribute(SHOW_HIDDEN_NODE)));
    drive.setAcessPermissions(fromXMLMultiValue(element.getAttribute(PERMISSIONS))) ;
    drive.setViews(fromXMLMultiValue(element.getAttribute(VIEWS))) ;
  }

  public void start() {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;     
    for(DriveManagerPlugin plugin: drivePlugins_) {
      for(Iterator<ObjectParameter> iterator = plugin.getPredefinedDriveEntries();iterator.hasNext();) {
        DriveEntry driveEntry = (DriveEntry)iterator.next().getObject();
        try {
          addDrive(driveEntry, sessionProvider) ; 
        } catch (Exception e) {
          log_.error("Can not init drive "+driveEntry.getName(), e) ;
        }       
      }
    }
    sessionProvider.close() ;

  }

  public void stop() {

  }

}
