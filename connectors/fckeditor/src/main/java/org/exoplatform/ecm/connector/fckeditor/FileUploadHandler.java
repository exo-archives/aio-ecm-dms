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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.rest.Response;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 23, 2008  
 */
/**
 * The Class FileUploadHandler.
 */
public class FileUploadHandler {

  /** The Constant UPLOAD_ACTION. */
  public final static String UPLOAD_ACTION = "upload".intern();

  /** The Constant PROGRESS_ACTION. */
  public final static String PROGRESS_ACTION = "progress".intern();

  /** The Constant ABORT_ACTION. */
  public final static String ABORT_ACTION = "abort".intern();

  /** The Constant DELETE_ACTION. */
  public final static String DELETE_ACTION = "delete".intern();

  /** The Constant SAVE_ACTION. */
  public final static String SAVE_ACTION = "save".intern();

  private UploadService uploadService;
  private FCKMessage fckMessage;
  /**
   * Instantiates a new file upload handler.
   * 
   * @param container the container
   */
  public FileUploadHandler(ExoContainer container) {
    uploadService = (UploadService)container.getComponentInstanceOfType(UploadService.class);
    fckMessage = new FCKMessage();
  }
  
  public Response upload(String uploadId, String contentType, double contentLength, InputStream inputStream, Node currentNode, String language) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    if(!FCKUtils.hasAddNodePermission(currentNode)) {
      Object[] args = { currentNode.getPath() };
      Document message = 
        fckMessage.createMessage(FCKMessage.FILE_UPLOAD_RESTRICTION,FCKMessage.ERROR,language,args);
      return Response.Builder.ok(message).mediaType("text/xml").cacheControl(cacheControl).build();
    }
    uploadService.createUploadResource(uploadId,null,contentType,contentLength,inputStream);
    return Response.Builder.ok().cacheControl(cacheControl).build();            
  }

  public Response control(String uploadId, String action) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    if(FileUploadHandler.PROGRESS_ACTION.equals(action)) {
      Document currentProgress = getProgress(uploadId);      
      return Response.Builder.ok(currentProgress).cacheControl(cacheControl).build();
    }else if(FileUploadHandler.ABORT_ACTION.equals(action)) {
      uploadService.removeUpload(uploadId);
      return Response.Builder.ok().cacheControl(cacheControl).build();    
    }else if(FileUploadHandler.DELETE_ACTION.equals(action)) {
      uploadService.removeUpload(uploadId);
      return Response.Builder.ok().cacheControl(cacheControl).build();    
    }
    return Response.Builder.badRequest().cacheControl(cacheControl).build();
  }
  
  public Response saveAsNTFile(Node parent, String uploadId, String fileName, String language) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    UploadResource resource = uploadService.getUploadResource(uploadId) ;
    if((fileName == null) || (fileName.length() == 0)) {
      fileName = resource.getFileName();
    }
    if(parent.hasNode(fileName)) {
      Object args[] = { fileName, parent.getPath() };
      Document fileExisted = 
        fckMessage.createMessage(FCKMessage.FILE_EXISTED,FCKMessage.ERROR,language,args);
      return Response.Builder.ok(fileExisted).mediaType("text/xml").cacheControl(cacheControl).build();
    }                
    String location = resource.getStoreLocation();
    byte[] uploadData = IOUtil.getFileContentAsBytes(location);
    Node file = parent.addNode(fileName,FCKUtils.NT_FILE);
    Node jcrContent = file.addNode("jcr:content","nt:resource");
    MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
    String mimetype = mimeTypeResolver.getMimeType(fileName);
    jcrContent.setProperty("jcr:data",new ByteArrayInputStream(uploadData));
    jcrContent.setProperty("jcr:lastModified",new GregorianCalendar());    
    jcrContent.setProperty("jcr:mimeType",mimetype);
    parent.getSession().save();
    uploadService.removeUpload(uploadId);
    return Response.Builder.ok().mediaType("text/xml").cacheControl(cacheControl).build();
  }
  
  private Document getProgress(String uploadId) throws Exception {    
    UploadResource resource = uploadService.getUploadResource(uploadId);
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();    
    if(resource == null) {
      return doc;
    }
    Double percent = 0.0;
    if (resource.getStatus() == UploadResource.UPLOADING_STATUS) {
      percent = (resource.getUploadedSize() * 100) / resource.getEstimatedSize();      
    } else {
      percent = 100.0;
    } 
    Element rootElement = doc.createElement("UploadProgress");
    rootElement.setAttribute("uploadId", uploadId);
    rootElement.setAttribute("fileName", resource.getFileName());
    rootElement.setAttribute("percent", percent.intValue() + "");
    doc.appendChild(rootElement);
    return doc;
  }
}
