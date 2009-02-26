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
package org.exoplatform.ecm.connector.fckeditor;

import java.security.AccessControlException;

import javax.jcr.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 23, 2008  
 */
public class FCKUtils {
  
  public static String GET_FOLDERS_AND_FILES="getFoldersAndFiles".intern();
  public static String CREATE_FOLDER = "createFolder".intern();
  public static String UPLOAD_FILE = "upload".intern();
  
  public static final String EXO_HIDDENABLE = "exo:hiddenable".intern();
  public static final String NT_FILE = "nt:file".intern();
  public static final String NT_FOLDER = "nt:folder".intern();
  public static final String NT_UNSTRUCTURED = "nt:unstructured".intern();
  
  public final static String DOCUMENT_TYPE = "file".intern();
  
  /** The Constant IMAGE_TYPE. */
  public final static String IMAGE_TYPE = "image".intern();
  public final static String FLASH_TYPE = "flash".intern();
  public final static String LINK_TYPE = "link".intern();
  
  
  /**
   * Creates the root element for connector response. The full connector response looks like:
   *<Connector command="GetFolders" resourceType="">
   *  <CurrentFolder folderType="" name="" path="" url=""/>  
   *  </CurrentFolder>
   *</Connector>
   *
   * @param command the command
   * @param node the node
   * @param folderType the folder type
   * @param url the url
   * @return the org.w3c.dom.Element element
   * @throws Exception the exception
   */
  public static Element createRootElement(String command, Node node, String folderType) throws Exception {
    Document doc = null;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    doc = builder.newDocument();
    String currentPath = node.getPath();
    if (!currentPath.endsWith("/")) {
      currentPath = currentPath + "/";
    }    
    Element rootElement = doc.createElement("Connector");
    doc.appendChild(rootElement);
    rootElement.setAttribute("command", command);
    rootElement.setAttribute("resourceType", "Node");    
    Element currentFolderElement = doc.createElement("CurrentFolder");
    currentFolderElement.setAttribute("name", node.getName());
    currentFolderElement.setAttribute("folderType", folderType);
    currentFolderElement.setAttribute("path", currentPath);
    currentFolderElement.setAttribute("url", createWebdavURL(node));
    rootElement.appendChild(currentFolderElement);
    return rootElement;
  }    
  
  public static boolean hasAddNodePermission(Node node) throws Exception {
    try {
      ((ExtendedNode)node).checkPermission(PermissionType.ADD_NODE) ;
      return true ;
    } catch (AccessControlException e) { }
    return false ;
  }
  
  public static String createWebdavURL(final Node node) throws Exception {
    String repository = ((ManageableRepository) node.getSession().getRepository()).getConfiguration().getName();
    String workspace = node.getSession().getWorkspace().getName();
    String currentPath = node.getPath();
    String url = "/portal/rest/jcr/" + repository + "/" + workspace + currentPath;
    return url;
  }
}
