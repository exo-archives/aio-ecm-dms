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

import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.HeaderParam;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.PassthroughInputTransformer;
import org.exoplatform.services.rest.transformer.XMLOutputTransformer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author : Hoa.Pham 
 * @Email hoa.pham@exoplatform.com 
 * Jun 23, 2008
 */
@URITemplate("/fckconnector/jcr/")
public class FCKCoreRESTConnector implements ResourceContainer {

  private FCKFileHandler fileHandler;
  private FCKFolderHandler folderHandler;  
  private FileUploadHandler fileUploadHandler;  
  private ThreadLocalSessionProviderService sessionProviderService;
  private RepositoryService repositoryService;    

  /**
   * Instantiates a new fCK core rest connector.
   * 
   * @param repositoryService the repository service
   * @param providerService the provider service
   */
  public FCKCoreRESTConnector(RepositoryService repositoryService, ThreadLocalSessionProviderService providerService) {
    this.repositoryService = repositoryService;
    this.sessionProviderService = providerService;
    this.fileHandler = new FCKFileHandler(ExoContainerContext.getCurrentContainer());
    this.folderHandler = new FCKFolderHandler(ExoContainerContext.getCurrentContainer());
    this.fileUploadHandler = new FileUploadHandler(ExoContainerContext.getCurrentContainer()); 
  }  

  /**
   * Gets the folders and files.
   * 
   * @param repoName the repo name
   * @param workspaceName the workspace name
   * @param currentFolder the current folder
   * @param command the command
   * @param type the type
   * @return the folders and files
   * @throws Exception the exception
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getFoldersAndFiles/")  
  @OutputTransformer(XMLOutputTransformer.class)  
  public Response getFoldersAndFiles(
      @QueryParam("repositoryName") String repoName, 
      @QueryParam("workspaceName") String workspaceName, 
      @QueryParam("currentFolder") String currentFolder, 
      @QueryParam("command") String command,
      @QueryParam("type") String type) throws Exception {
    Session session = getSession(repoName,workspaceName);
    Node currentNode = (Node)session.getItem(currentFolder);
    String ftype = folderHandler.getFolderType(currentNode);
    if(ftype == null) {
      return Response.Builder.badRequest().build();
    }
    Element root = FCKUtils.createRootElement(command,currentNode,ftype);
    Document document = root.getOwnerDocument();
    Element folders = root.getOwnerDocument().createElement("Folders");
    Element files = root.getOwnerDocument().createElement("Files");    
    for(NodeIterator iterator = currentNode.getNodes();iterator.hasNext();) {
      Node child = iterator.nextNode();
      if(child.isNodeType(FCKUtils.EXO_HIDDENABLE)) continue;
      String folderType = folderHandler.getFolderType(child);
      if(folderType != null) {        
        Element folder = folderHandler.createFolderElement(document,child,folderType);
        folders.appendChild(folder);
      }
      String fileType = fileHandler.getFileType(child,type);
      if(fileType != null) {               
        Element file = fileHandler.createFileElement(document,child,fileType);
        files.appendChild(file);
      }
    }
    root.appendChild(folders);
    root.appendChild(files);
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    return Response.Builder.ok(document).mediaType("text/xml").cacheControl(cacheControl).build();
  }

  /**
   * Creates the folder.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param currentFolder the current folder
   * @param newFolderName the new folder name
   * @param language the language
   * @return the response
   * @throws Exception the exception
   */
  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/createFolder/")  
  @OutputTransformer(XMLOutputTransformer.class)
  public Response createFolder(
      @QueryParam("repositoryName") String repositoryName, 
      @QueryParam("workspaceName") String workspaceName,
      @QueryParam("currentFolder") String currentFolder,       
      @QueryParam("newFolderName") String newFolderName,
      @QueryParam("language") String language) throws Exception {
    Session session = getSession(repositoryName,workspaceName);
    Node currentNode = (Node)session.getItem(currentFolder);
    return folderHandler.createNewFolder(currentNode,newFolderName,language);
  }

  /**
   * Upload file.
   * 
   * @return the response
   */
  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/uploadFile/upload/")  
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(XMLOutputTransformer.class)
  public Response uploadFile(
      InputStream inputStream,
      @QueryParam("repositoryName") String repositoryName, 
      @QueryParam("workspaceName") String workspaceName,
      @QueryParam("currentFolder") String currentFolder,
      @QueryParam("uploadId") String uploadId,
      @QueryParam("language") String language,
      @HeaderParam("content-type") String contentType,      
      @HeaderParam("content-length") String contentLength) throws Exception {
    Session session = getSession(repositoryName,workspaceName);    
    Node currentNode = (Node)session.getItem(currentFolder);
    return fileUploadHandler.upload(uploadId,contentType,Double.parseDouble(contentLength), inputStream, currentNode, language);
  }

  /**
   * Control upload file 
   * 
   * @param action the action
   * @param uploadId the upload id
   * @param language the language
   * @return the response
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/uploadFile/control/")  
  @OutputTransformer(XMLOutputTransformer.class)
  public Response processUpload(
      @QueryParam("repositoryName") String repositoryName, 
      @QueryParam("workspaceName") String workspaceName,
      @QueryParam("currentFolder") String currentFolder,
      @QueryParam("action") String action,
      @QueryParam("language") String language,
      @QueryParam("fileName") String fileName,
      @QueryParam("uploadId") String uploadId) throws Exception {       
    if(FileUploadHandler.SAVE_ACTION.equals(action)) {     
      Session session = getSession(repositoryName,workspaceName);
      Node currentNode = (Node)session.getItem(currentFolder);
      return fileUploadHandler.saveAsNTFile(currentNode, uploadId, fileName, language);
    }    
    return fileUploadHandler.control(uploadId,action);
  }

  private Session getSession(String repository,String workspaceName) throws Exception {
    ManageableRepository manageableRepository = null;    
    if(repository == null) {
      manageableRepository = repositoryService.getCurrentRepository();
    }else {
      manageableRepository = repositoryService.getRepository(repository);
    }
    if(workspaceName == null) {
      workspaceName = manageableRepository.getConfiguration().getDefaultWorkspaceName();
    }
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);    
    return sessionProvider.getSession(workspaceName,manageableRepository);
  }
  
}
