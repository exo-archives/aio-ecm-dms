/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.cms.webdav;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.exoplatform.ecm.utils.text.Text;
import javax.jcr.Item;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cms.link.LinkUtils;
import org.exoplatform.services.cms.link.NodeFinder;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.webdav.Depth;
import org.exoplatform.services.jcr.webdav.command.MoveCommand;
import org.exoplatform.services.jcr.webdav.command.PropFindCommand;
import org.exoplatform.services.jcr.webdav.WebDavHeaders;
import org.exoplatform.services.jcr.webdav.WebDavMethods;
import org.exoplatform.services.jcr.webdav.command.LockCommand;
import org.exoplatform.services.jcr.webdav.command.UnLockCommand;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.jcr.webdav.xml.XMLInputTransformer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.ContextParam;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.HeaderParam;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.ResourceBinder;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.transformer.PassthroughInputTransformer;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;
import org.exoplatform.services.rest.transformer.SerializableTransformer;
import java.net.URLEncoder;
import java.util.List;


/**
 * This class is used to override the default WebDavServiceImpl in order to support symlinks
 * 
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          nicolas.filotto@exoplatform.com
 * 9 avr. 2009  
 */
@URITemplate("/jcr/")
public class WebDavServiceImpl extends org.exoplatform.services.jcr.webdav.WebDavServiceImpl {

  /**
   * Logger.
   */
  private static Log log = ExoLogger.getLogger("cms.webdav.WebDavServiceImpl");
  
  private final NodeFinder nodeFinder;
  private final RepositoryService repositoryService;
  private final NullResourceLocksHolder           nullResourceLocks;
  
  public WebDavServiceImpl(InitParams params,
                           RepositoryService repositoryService,
                           ThreadLocalSessionProviderService sessionProviderService,
                           ResourceBinder resourceBinder,
                           NodeFinder nodeFinder) throws Exception {
    super(params, repositoryService, sessionProviderService, resourceBinder);
    this.nodeFinder = nodeFinder;
    this.repositoryService = repositoryService;
    this.nullResourceLocks = new NullResourceLocksHolder();
  }

  private String getRealDestinationHeader(String baseURI, String repoName, String destinationHeader) {
    String serverURI = baseURI + "/jcr/" + repoName;

    destinationHeader = TextUtil.unescape(destinationHeader, '%');

    if (!destinationHeader.startsWith(serverURI)) {
      return null;
    }

    String destPath = destinationHeader.substring(serverURI.length() + 1);
    
    try {
      Item item = nodeFinder.getItem(repoName, workspaceName(destPath), LinkUtils.getParentPath(path(Text.escapeIllegalJcrChars(destPath))), true);
      return item.getSession().getWorkspace().getName() + LinkUtils.createPath(item.getPath(), LinkUtils.getItemName(path(Text.escapeIllegalJcrChars(destPath))));
    } catch (Exception e) {
      log.warn("Cannot find the item at " + repoName + "/" + destPath, e);
      return null;
    }
  }
  
  @HTTPMethod(WebDavMethods.CHECKIN)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(XMLInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response checkin(@URIParam("repoName") String repoName,
                          @URIParam("repoPath") String repoPath,
                          @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                          @HeaderParam(WebDavHeaders.IF) String ifHeader,
                          HierarchicalProperty body) {

    try {
    	repoName = repositoryService.getCurrentRepository().getConfiguration().getName();
      repoPath = convertRepoPath(repoName, repoPath, true);
    } catch (PathNotFoundException exc) {
      return Response.Builder.notFound().build();
    } catch (NoSuchWorkspaceException exc) {
      return Response.Builder.notFound().build();
    } catch (Exception e) {
      log.warn("Cannot find the item at " + repoName + "/" + repoPath, e);
      return Response.Builder.serverError().build();
    }
    return super.checkin(repoName, repoPath, lockTokenHeader, ifHeader, body);
  }

  @HTTPMethod(WebDavMethods.CHECKOUT)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(XMLInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response checkout(@URIParam("repoName") String repoName,
                           @URIParam("repoPath") String repoPath,
                           @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                           @HeaderParam(WebDavHeaders.IF) String ifHeader,
                           HierarchicalProperty body) {
    try {
    	repoName = repositoryService.getCurrentRepository().getConfiguration().getName();
      repoPath = convertRepoPath(repoName, repoPath, true);
    } catch (PathNotFoundException exc) {
      return Response.Builder.notFound().build();
    } catch (NoSuchWorkspaceException exc) {
      return Response.Builder.notFound().build();
    } catch (Exception e) {
      log.warn("Cannot find the item at " + repoName + "/" + repoPath, e);
      return Response.Builder.serverError().build();
    }
    return super.checkout(repoName, repoPath, lockTokenHeader, ifHeader, body);
  }

  @HTTPMethod(WebDavMethods.COPY)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(XMLInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response copy(@URIParam("repoName") String repoName,
                       @URIParam("repoPath") String repoPath,
                       @HeaderParam(WebDavHeaders.DESTINATION) String destinationHeader,
                       @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                       @HeaderParam(WebDavHeaders.IF) String ifHeader,
                       @HeaderParam(WebDavHeaders.DEPTH) String depthHeader,
                       @HeaderParam(WebDavHeaders.OVERWRITE) String overwriteHeader,
                       @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                       HierarchicalProperty body) {

    try {
    	repoName = repositoryService.getCurrentRepository().getConfiguration().getName();
      repoPath = convertRepoPath(repoName, repoPath, true);
    } catch (PathNotFoundException exc) {
      return Response.Builder.notFound().build();
    } catch (NoSuchWorkspaceException exc) {
      return Response.Builder.notFound().build();
    } catch (Exception e) {
      log.warn("Cannot find the item at " + repoName + "/" + repoPath, e);
      return Response.Builder.serverError().build();
    }
    String realDestinationHeader = getRealDestinationHeader(baseURI, repoName, destinationHeader);
    if (realDestinationHeader != null) {
      destinationHeader = realDestinationHeader;
    }
    return super.copy(repoName, repoPath, destinationHeader, lockTokenHeader, ifHeader, 
                depthHeader, overwriteHeader, baseURI, body);
  }

  @HTTPMethod(WebDavMethods.GET)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response get(@URIParam("repoName") String repoName,
                      @URIParam("repoPath") String repoPath,
                      @HeaderParam(WebDavHeaders.RANGE) String rangeHeader,
                      @HeaderParam(WebDavHeaders.IF_MODIFIED_SINCE) String ifModifiedSince,
                      @QueryParam("version") String version,
                      @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI) {

    try {
    	repoName = repositoryService.getCurrentRepository().getConfiguration().getName();
      repoPath = convertRepoPath(repoName, repoPath, true);
    } catch (PathNotFoundException exc) {
      return Response.Builder.notFound().build();
    } catch (NoSuchWorkspaceException exc) {
      return Response.Builder.notFound().build();
    } catch (Exception e) {
      log.warn("Cannot find the item at " + repoName + "/" + repoPath, e);
      return Response.Builder.serverError().build();
    }
    return super.get(repoName, repoPath, rangeHeader, ifModifiedSince, version, baseURI);
  }

  @HTTPMethod(WebDavMethods.HEAD)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response head(@URIParam("repoName") String repoName,
                       @URIParam("repoPath") String repoPath,
                       @QueryParam("version") String version,
                       @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI) {

    try {
    	repoName = repositoryService.getCurrentRepository().getConfiguration().getName();
      repoPath = convertRepoPath(repoName, repoPath, true);
    } catch (PathNotFoundException exc) {
      return Response.Builder.notFound().build();
    } catch (NoSuchWorkspaceException exc) {
      return Response.Builder.notFound().build();
    } catch (Exception e) {
      log.warn("Cannot find the item at " + repoName + "/" + repoPath, e);
      return Response.Builder.serverError().build();
    }
    return super.head(repoName, repoPath, version, baseURI);
  }

  @HTTPMethod(WebDavMethods.LOCK)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(XMLInputTransformer.class)
  @OutputTransformer(SerializableTransformer.class)
  public Response lock(@URIParam("repoName") String repoName,
                       @URIParam("repoPath") String repoPath,
                       @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                       @HeaderParam(WebDavHeaders.IF) String ifHeader,
                       @HeaderParam(WebDavHeaders.DEPTH) String depthHeader,
                       @HeaderParam(WebDavHeaders.TIMEOUT) String timeout,
                       HierarchicalProperty body) {

    try {
    	repoName = repositoryService.getCurrentRepository().getConfiguration().getName();
      repoPath = convertRepoPath(repoName, repoPath, true);
    } catch (PathNotFoundException exc) {
    	try {
    		Session session = session(repoName, workspaceName(repoPath), lockTokens(lockTokenHeader, ifHeader));
  			return new LockCommand(nullResourceLocks).lock(session, path(repoPath), body, new Depth(depthHeader), "86400");
    	} catch (Exception ex) {
    		return Response.Builder.notFound().build();
    	}
    } catch (NoSuchWorkspaceException exc) {
      return Response.Builder.notFound().build();
    } catch (Exception e) {
      log.warn("Cannot find the item at " + repoName + "/" + repoPath, e);
      return Response.Builder.serverError().build();
    }     
    return super.lock(repoName, repoPath, lockTokenHeader, ifHeader, depthHeader, timeout, body);
  }

  @HTTPMethod(WebDavMethods.UNLOCK)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(XMLInputTransformer.class)
  @OutputTransformer(SerializableTransformer.class)
  public Response unlock(@URIParam("repoName") String repoName,
                         @URIParam("repoPath") String repoPath,
                         @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                         @HeaderParam(WebDavHeaders.IF) String ifHeader,
                         HierarchicalProperty body) {

    try {
    	repoName = repositoryService.getCurrentRepository().getConfiguration().getName();
      repoPath = convertRepoPath(repoName, repoPath, true);
    } catch (PathNotFoundException exc) {
    	try {
    		List<String> tokens = lockTokens(lockTokenHeader, ifHeader);
    		Session session = session(repoName, workspaceName(repoPath), lockTokens(lockTokenHeader, ifHeader));
    		return new UnLockCommand(nullResourceLocks).unLock(session, path(repoPath), tokens);
    	} catch (Exception ex) {
    		return Response.Builder.notFound().build();
    	}      
    } catch (NoSuchWorkspaceException exc) {
      return Response.Builder.notFound().build();
    } catch (Exception e) {
      log.warn("Cannot find the item at " + repoName + "/" + repoPath, e);
      return Response.Builder.serverError().build();
    }
    return super.unlock(repoName, repoPath, lockTokenHeader, ifHeader, body);
  }

  @HTTPMethod(WebDavMethods.OPTIONS)
  @URITemplate("/{repoName}/")
  @InputTransformer(XMLInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response options(@URIParam("repoName") String repoName, HierarchicalProperty body) {
    return super.options(repoName, body);
  }

  @HTTPMethod(WebDavMethods.ORDERPATCH)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(XMLInputTransformer.class)
  @OutputTransformer(SerializableTransformer.class)
  public Response order(@URIParam("repoName") String repoName,
                        @URIParam("repoPath") String repoPath,
                        @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                        @HeaderParam(WebDavHeaders.IF) String ifHeader,
                        @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                        HierarchicalProperty body) {

    try {
    	repoName = repositoryService.getCurrentRepository().getConfiguration().getName();
      repoPath = convertRepoPath(repoName, repoPath, true);
    } catch (PathNotFoundException exc) {
      return Response.Builder.notFound().build();
    } catch (NoSuchWorkspaceException exc) {
      return Response.Builder.notFound().build();
    } catch (Exception e) {
      log.warn("Cannot find the item at " + repoName + "/" + repoPath, e);
      return Response.Builder.serverError().build();
    }
    return super.order(repoName, repoPath, lockTokenHeader, ifHeader, baseURI, body);
  }

  @HTTPMethod(WebDavMethods.PROPFIND)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(XMLInputTransformer.class)
  @OutputTransformer(SerializableTransformer.class)
  public Response propfind(@URIParam("repoName") String repoName,
                           @URIParam("repoPath") String repoPath,
                           @HeaderParam(WebDavHeaders.DEPTH) String depthHeader,
                           @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                           HierarchicalProperty body) {

    try {
    	repoName = repositoryService.getCurrentRepository().getConfiguration().getName();
      repoPath = convertRepoPath(repoName, repoPath, true);
    } catch (PathNotFoundException exc) {
      return Response.Builder.notFound().build();
    } catch (NoSuchWorkspaceException exc) {
      return Response.Builder.notFound().build();
    } catch (Exception e) {
      log.warn("Cannot find the item at " + repoName + "/" + repoPath, e);
      return Response.Builder.serverError().build();
    }
    return super.propfind(repoName, repoPath, depthHeader, baseURI, body);
  }

  @HTTPMethod(WebDavMethods.PROPPATCH)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(XMLInputTransformer.class)
  @OutputTransformer(SerializableTransformer.class)
  public Response proppatch(@URIParam("repoName") String repoName,
                            @URIParam("repoPath") String repoPath,
                            @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                            @HeaderParam(WebDavHeaders.IF) String ifHeader,
                            @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                            HierarchicalProperty body) {

    try {
    	repoName = repositoryService.getCurrentRepository().getConfiguration().getName();
      repoPath = convertRepoPath(repoName, repoPath, true);
    } catch (PathNotFoundException exc) {
      return Response.Builder.notFound().build();
    } catch (NoSuchWorkspaceException exc) {
      return Response.Builder.notFound().build();
    } catch (Exception e) {
      log.warn("Cannot find the item at " + repoName + "/" + repoPath, e);
      return Response.Builder.serverError().build();
    }
    return super.proppatch(repoName, repoPath, lockTokenHeader, ifHeader, baseURI, body);
  }

  @HTTPMethod(WebDavMethods.PUT)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response put(@URIParam("repoName") String repoName,
                      @URIParam("repoPath") String repoPath,
                      @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                      @HeaderParam(WebDavHeaders.IF) String ifHeader,
                      @HeaderParam(WebDavHeaders.NODETYPE) String nodeTypeHeader,
                      @HeaderParam(WebDavHeaders.MIXTYPE) String mixinTypesHeader,
                      @HeaderParam(WebDavHeaders.CONTENTTYPE) String mimeType,
                      InputStream inputStream) {

  	try {
      repoName = repositoryService.getCurrentRepository().getConfiguration().getName();
      try {
        repoPath = Text.escapeIllegalJcrChars(repoPath);
        Item item = nodeFinder.getItem(repoName, workspaceName(repoPath), LinkUtils.getParentPath(path(repoPath)), true);
        repoPath = item.getSession().getWorkspace().getName()
            + LinkUtils.createPath(item.getPath(), LinkUtils.getItemName(path(repoPath)));
      } catch (PathNotFoundException e) {
        Item item = nodeFinder.getItem(repoName, workspaceName(repoPath),
                                       LinkUtils.getParentPath(path(Text.escapeIllegalJcrChars(repoPath))),
                                       true);
        repoPath = item.getSession().getWorkspace().getName()
            + LinkUtils.createPath(item.getPath(), LinkUtils.getItemName(path(repoPath)));
      }
    } catch (NoSuchWorkspaceException exc) {
      return Response.Builder.notFound().build();
    } catch (Exception e) {
      log.warn("Cannot find the item at " + repoName + "/" + repoPath, e);
      return Response.Builder.serverError().build();
    }
    return super.put(repoName, repoPath, lockTokenHeader, ifHeader, nodeTypeHeader,
                     mixinTypesHeader, mimeType, inputStream);
  }

  @HTTPMethod(WebDavMethods.REPORT)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(XMLInputTransformer.class)
  @OutputTransformer(SerializableTransformer.class)
  public Response report(@URIParam("repoName") String repoName,
                         @URIParam("repoPath") String repoPath,
                         @HeaderParam(WebDavHeaders.DEPTH) String depthHeader,
                         @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                         HierarchicalProperty body) {

    try {
    	repoName = repositoryService.getCurrentRepository().getConfiguration().getName();
      repoPath = convertRepoPath(repoName, repoPath, true);
    } catch (PathNotFoundException exc) {
      return Response.Builder.notFound().build();
    } catch (NoSuchWorkspaceException exc) {
      return Response.Builder.notFound().build();
    } catch (Exception e) {
      log.warn("Cannot find the item at " + repoName + "/" + repoPath, e);
      return Response.Builder.serverError().build();
    }
    return super.report(repoName, repoPath, depthHeader, baseURI, body);
  }

  @HTTPMethod(WebDavMethods.SEARCH)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(XMLInputTransformer.class)
  @OutputTransformer(SerializableTransformer.class)
  public Response search(@URIParam("repoName") String repoName,
                         @URIParam("repoPath") String repoPath,
                         @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                         HierarchicalProperty body) {

    try {
    	repoName = repositoryService.getCurrentRepository().getConfiguration().getName();
      repoPath = convertRepoPath(repoName, repoPath, true);
    } catch (PathNotFoundException exc) {
      return Response.Builder.notFound().build();
    } catch (NoSuchWorkspaceException exc) {
      return Response.Builder.notFound().build();
    } catch (Exception e) {
      log.warn("Cannot find the item at " + repoName + "/" + repoPath, e);
      return Response.Builder.serverError().build();
    }
    return super.search(repoName, repoPath, baseURI, body);
  }

  @HTTPMethod(WebDavMethods.UNCHECKOUT)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(XMLInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response uncheckout(@URIParam("repoName") String repoName,
                             @URIParam("repoPath") String repoPath,
                             @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                             @HeaderParam(WebDavHeaders.IF) String ifHeader,
                             HierarchicalProperty body) {

    try {
    	repoName = repositoryService.getCurrentRepository().getConfiguration().getName();
      repoPath = convertRepoPath(repoName, repoPath, true);
    } catch (PathNotFoundException exc) {
      return Response.Builder.notFound().build();
    } catch (NoSuchWorkspaceException exc) {
      return Response.Builder.notFound().build();
    } catch (Exception e) {
      log.warn("Cannot find the item at " + repoName + "/" + repoPath, e);
      return Response.Builder.serverError().build();
    }
    return super.uncheckout(repoName, repoPath, lockTokenHeader, ifHeader, body);
  }

  @HTTPMethod(WebDavMethods.VERSIONCONTROL)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response versionControl(@URIParam("repoName") String repoName,
                                 @URIParam("repoPath") String repoPath,
                                 @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                                 @HeaderParam(WebDavHeaders.IF) String ifHeader) {

    try {
    	repoName = repositoryService.getCurrentRepository().getConfiguration().getName();
      repoPath = convertRepoPath(repoName, repoPath, true);
    } catch (PathNotFoundException exc) {
      return Response.Builder.notFound().build();
    } catch (NoSuchWorkspaceException exc) {
      return Response.Builder.notFound().build();
    } catch (Exception e) {
      log.warn("Cannot find the item at " + repoName + "/" + repoPath, e);
      return Response.Builder.serverError().build();
    }
    return super.versionControl(repoName, repoPath, lockTokenHeader, ifHeader);
  }
   @HTTPMethod(WebDavMethods.MOVE)
     @URITemplate("/{repoName}/{repoPath}/")
     @InputTransformer(XMLInputTransformer.class)
    @OutputTransformer(PassthroughOutputTransformer.class)
  public Response move(@URIParam("repoName") String repoName, @URIParam("repoPath") String repoPath,
     @HeaderParam(WebDavHeaders.DESTINATION) String destinationHeader,
     @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader, @HeaderParam(WebDavHeaders.IF) String ifHeader,
     @HeaderParam(WebDavHeaders.DEPTH) String depthHeader,
     @HeaderParam(WebDavHeaders.OVERWRITE) String overwriteHeader, @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                         HierarchicalProperty body)
  {
   try {
      int lastIndexOfSlash = destinationHeader.lastIndexOf('/');
      String newPath = destinationHeader.substring(0, lastIndexOfSlash);
      String newFileName = Text.escape(Text.escapeIllegalJcrChars(Text.unescape(destinationHeader.substring(lastIndexOfSlash + 1))));
      destinationHeader = newPath + "/" + newFileName;
      Item item = nodeFinder.getItem(repoName, workspaceName(repoPath), path(Text.escapeIllegalJcrChars(repoPath)), true);
      repoPath = item.getSession().getWorkspace().getName() + item.getPath();
    } catch (PathNotFoundException exc) {
      return Response.Builder.notFound().build();
    } catch (NoSuchWorkspaceException exc) {
       return Response.Builder.notFound().build();
    } catch (Exception e) {
      log.warn("Cannot find the item at " + repoName + "/" + repoPath, e);
     return Response.Builder.serverError().build();
   }
      return super.move(repoName, repoPath, destinationHeader, lockTokenHeader, ifHeader,
                depthHeader, overwriteHeader, baseURI, body);

  }
  private String convertRepoPath(String repoName, String repoPath, boolean giveTarget) throws Exception{
    try {
      Item item = nodeFinder.getItem(repoName, workspaceName(repoPath), path(normalizePath(repoPath)), giveTarget);
      return item.getSession().getWorkspace().getName() + item.getPath();
    } catch (PathNotFoundException e) {
      Item item = nodeFinder.getItem(repoName, workspaceName(repoPath), path(Text.escapeIllegalJcrChars(repoPath)), giveTarget);
      return item.getSession().getWorkspace().getName() + item.getPath();
    }
  }
  
  /**
   * Normalizes path.
   * 
   * @param repoPath repository path
   * @return normalized path.
   */
  protected String normalizePath(String repoPath)
  {
     if (repoPath.length() > 0 && repoPath.endsWith("/"))
     {
        return repoPath.substring(0, repoPath.length() - 1);
     }

     String[] pathElements = repoPath.split("/");
     StringBuffer escapedPath = new StringBuffer();
     for (String element : pathElements)
     {
        try
        {
           if (element.contains("'"))
           {
              element = element.replaceAll("'", URLEncoder.encode("'", "UTF-8"));
           }
           escapedPath.append(element + "/");
        }
        catch (Exception e)
        {
           log.warn(e.getMessage());
        }
     }

     return escapedPath.toString().substring(0, escapedPath.length() - 1);
  }

}
