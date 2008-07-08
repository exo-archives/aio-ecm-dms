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

import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.resources.ResourceBundleService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 23, 2008  
 */
public class FCKFolderHandler {  
  private TemplateService templateService;
  private FCKMessage fckMessage;
  public FCKFolderHandler(ExoContainer container) {
    templateService = (TemplateService)container.getComponentInstanceOfType(TemplateService.class);
    ResourceBundleService bundleService = 
      (ResourceBundleService)container.getComponentInstanceOfType(ResourceBundleService.class);
    fckMessage = new FCKMessage(bundleService);
  }

  public String getFolderType(final Node node) throws Exception {
    //need use a service to get extended folder type for the node
    NodeType nodeType = node.getPrimaryNodeType();    
    String primaryType = nodeType.getName();
    String repository = ((ManageableRepository) node.getSession().getRepository()).getConfiguration().getName();
    if (templateService.getDocumentTemplates(repository).contains(primaryType)) return null;    
    if (FCKUtils.NT_UNSTRUCTURED.equals(primaryType) || FCKUtils.NT_FOLDER.equals(primaryType)) return primaryType;    
    if (nodeType.isNodeType(FCKUtils.NT_UNSTRUCTURED) || nodeType.isNodeType(FCKUtils.NT_FOLDER)) {
      //check if the nodetype is exo:videoFolder...
      return primaryType;
    }    
    return primaryType;
  }

  public String getFolderURL(final Node folder) throws Exception {
    return FCKUtils.createWebdavURL(folder);    
  }
  /**
   * Creates the folder element for connector response look like
   * <folder name="" url="" folderType="" />
   *         
   * @param document the document
   * @param child the child
   * @param folderType the folder type
   * @param url the url
   * @return the org.w3c.dom.Element element
   * @throws Exception the exception
   */
  public Element createFolderElement(Document document, Node child, String folderType) throws Exception {            
    Element folder = document.createElement("Folder");
    folder.setAttribute("name", child.getName());
    folder.setAttribute("url", getFolderURL(child));
    folder.setAttribute("folderType", folderType);
    return folder;
  }

  /**
   * Creates the new folder.
   * 
   * @param currentNode the current node
   * @param newFolderName the new folder name
   * @param language the language
   * @return the document
   * @throws Exception the exception
   */
  public Document createNewFolder(Node currentNode, String newFolderName, String language) throws Exception {
    String folderType = getFolderType(currentNode);        
    Element root = FCKUtils.createRootElement(FCKUtils.CREATE_FOLDER, currentNode,folderType);    
    Document document = root.getOwnerDocument();
    if(!FCKUtils.hasAddNodePermission(currentNode)) {
      String message = fckMessage.getMessage(FCKMessage.FOLDER_PERMISSION_CREATING,null,language);
      Element element = fckMessage.createMessageElement(document,FCKMessage.FOLDER_PERMISSION_CREATING,message,FCKMessage.ERROR);
      document.appendChild(element);
      return document;
    }
    if(currentNode.hasNode(newFolderName)) {
      String message = fckMessage.getMessage(FCKMessage.FOLDER_EXISTED, null, language);
      Element element = fckMessage.createMessageElement(document,FCKMessage.FOLDER_EXISTED,message,FCKMessage.ERROR);
      document.appendChild(element);
      return document;
    }    
    currentNode.addNode(newFolderName,FCKUtils.NT_FOLDER);
    currentNode.getSession().save();    
    return null;
  }
}
