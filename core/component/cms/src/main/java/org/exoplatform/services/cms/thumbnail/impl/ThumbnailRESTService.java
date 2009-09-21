/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.services.cms.thumbnail.impl;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.cms.link.NodeFinder;
import org.exoplatform.services.cms.thumbnail.ThumbnailService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryTemplate;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.PassthroughInputTransformer;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 23, 2008 11:09:39 AM
 */
/**
 * Provide the request which will be used to get the response data
 * {repoName} Repository name
 * {workspaceName} Name of workspace
 * {nodePath} The node path
 * Example: 
 * <img src="/portal/rest/thumbnailImage/repository/collaboration/test.gif" />
 */
@URITemplate("/thumbnailImage/{repoName}/{workspaceName}/{uuid}/")
public class ThumbnailRESTService implements ResourceContainer {
  
  private static final String LASTMODIFIED = "Last-Modified";
  
  private final RepositoryService repositoryService_;
  private final ThumbnailService thumbnailService_;
  private final NodeFinder nodeFinder_;
  private final LinkManager linkManager_;
  
  public ThumbnailRESTService(RepositoryService repositoryService, ThumbnailService thumbnailService, NodeFinder nodeFinder, LinkManager linkManager) {
    repositoryService_ = repositoryService;
    thumbnailService_ = thumbnailService;
    nodeFinder_ = nodeFinder;
    linkManager_ = linkManager;
  }
/**
 * Get the image with medium size
 * ex: /portal/rest/thumbnailImage/repository/collaboration/test.gif/?size=medium
 * @param repoName Repository name
 * @param wsName Workspace name
 * @param nodePath Node path
 * @return Response inputstream
 * @throws Exception
 */  
  @QueryTemplate("size=medium")
  @HTTPMethod("GET")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response getThumbnailImage(@URIParam("repoName") String repoName, 
                                    @URIParam("workspaceName") String wsName,
                                    @URIParam("uuid") String uuid) throws Exception {
    return getThumbnailByType(repoName, wsName, uuid, ThumbnailService.MEDIUM_SIZE);
  }
  
/**
 * Get the image with big size
 * ex: /portal/rest/thumbnailImage/repository/collaboration/test.gif/?size=big
 * @param repoName Repository name
 * @param wsName Workspace name
 * @param nodePath Node path
 * @return Response inputstream
 * @throws Exception
 */   
  @QueryTemplate("size=big")
  @HTTPMethod("GET")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response getCoverImage(@URIParam("repoName") String repoName, 
                                @URIParam("workspaceName") String wsName,
                                @URIParam("uuid") String uuid) throws Exception {
    return getThumbnailByType(repoName, wsName, uuid, ThumbnailService.BIG_SIZE);
  }
  
/**
 * Get the image with small size
 * ex: /portal/rest/thumbnailImage/repository/collaboration/test.gif/?size=small
 * @param repoName Repository name
 * @param wsName Workspace name
 * @param nodePath Node path
 * @return Response inputstream
 * @throws Exception
 */   
  @QueryTemplate("size=small")
  @HTTPMethod("GET")
  public Response getSmallImage(@URIParam("repoName") String repoName, 
                                @URIParam("workspaceName") String wsName,
                                @URIParam("uuid") String uuid) throws Exception {
    return getThumbnailByType(repoName, wsName, uuid, ThumbnailService.SMALL_SIZE);
  }
  
  private Response getThumbnailByType(String repoName, String wsName, String uuid, 
      String propertyName) throws Exception {
    if(!thumbnailService_.isEnableThumbnail()) return Response.Builder.ok().build();
    Node showingNode = getShowingNode(repoName, wsName, uuid);
    Node parentNode = showingNode.getParent();
    String identifier = ((NodeImpl) showingNode).getInternalIdentifier();
    Node targetNode;
    if (linkManager_.isLink(showingNode)) {
      try {
        targetNode = linkManager_.getTarget(showingNode);
      } catch (ItemNotFoundException e) {
        targetNode = showingNode;
      }
    } else {
      targetNode = showingNode;
    }
    if(targetNode.getPrimaryNodeType().getName().equals("nt:file")) {
      Node content = targetNode.getNode("jcr:content");
      if(content.getProperty("jcr:mimeType").getString().startsWith("image")) {
        Node thumbnailFolder = ThumbnailUtils.getThumbnailFolder(parentNode);
        
        Node thumbnailNode = ThumbnailUtils.getThumbnailNode(thumbnailFolder, identifier);
        
        if(!thumbnailNode.hasProperty(propertyName)) {
          BufferedImage image = ImageIO.read(content.getProperty("jcr:data").getStream());
          thumbnailService_.addThumbnailImage(thumbnailNode, image, propertyName);
        }
        String lastModified = null;
        if(thumbnailNode.hasProperty(ThumbnailService.THUMBNAIL_LAST_MODIFIED)) {
          lastModified = thumbnailNode.getProperty(ThumbnailService.THUMBNAIL_LAST_MODIFIED).getString();
        }
        InputStream inputStream = null;
        if(thumbnailNode.hasProperty(propertyName)) {
          inputStream = thumbnailNode.getProperty(propertyName).getStream();
        }
        return Response.Builder.ok().header(LASTMODIFIED, lastModified)
                                    .entity(inputStream, "image")
                                    .build();
      }
    }
    return getThumbnailRes(parentNode, identifier, propertyName);
  }
  
  private Response getThumbnailRes(Node parentNode, String identifier, String propertyName) throws Exception{
    if(parentNode.hasNode(ThumbnailService.EXO_THUMBNAILS_FOLDER)) {
      Node thumbnailFolder = parentNode.getNode(ThumbnailService.EXO_THUMBNAILS_FOLDER);
      if(thumbnailFolder.hasNode(identifier)) {
        Node thumbnailNode = thumbnailFolder.getNode(identifier);
        if(thumbnailNode.hasProperty(propertyName)) {
          InputStream inputStream = thumbnailNode.getProperty(propertyName).getStream();
          return Response.Builder.ok().entity(inputStream, "image").build();
        }
      }
    }
    return Response.Builder.ok().build();
  }
  
  private Node getShowingNode(String repoName, String wsName, String uuid) throws Exception {
    ManageableRepository repository = repositoryService_.getRepository(repoName);
    Session session = getSystemProvider().getSession(wsName, repository);
    return session.getNodeByUUID(uuid);
  }

  private SessionProvider getSystemProvider() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    SessionProviderService service = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    return service.getSystemSessionProvider(null) ;  
  }
}
