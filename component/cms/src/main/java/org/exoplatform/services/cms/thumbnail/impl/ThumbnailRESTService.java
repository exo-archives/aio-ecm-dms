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
import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cms.thumbnail.ThumbnailService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
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
@URITemplate("/thumbnailImage/{repoName}/{workspaceName}/{nodePath}/")
public class ThumbnailRESTService implements ResourceContainer {
  
  private static final String LASTMODIFIED = "Last-Modified";
  
  private RepositoryService repositoryService_;
  private ThumbnailService thumbnailService_;
  
  public ThumbnailRESTService(RepositoryService repositoryService, ThumbnailService thumbnailService) {
    repositoryService_ = repositoryService;
    thumbnailService_ = thumbnailService;
  }
  
  @QueryTemplate("size=medium")
  @HTTPMethod("GET")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response getThumbnailImage(@URIParam("repoName") String repoName, 
                                    @URIParam("workspaceName") String wsName,
                                    @URIParam("nodePath") String nodePath) throws Exception {
    return getThumbnailByType(repoName, wsName, nodePath, ThumbnailService.MEDIUM_SIZE);
  }
  
  @QueryTemplate("size=big")
  @HTTPMethod("GET")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response getCoverImage(@URIParam("repoName") String repoName, 
                                @URIParam("workspaceName") String wsName,
                                @URIParam("nodePath") String nodePath) throws Exception {
    return getThumbnailByType(repoName, wsName, nodePath, ThumbnailService.BIG_SIZE);
  }
  
  @QueryTemplate("size=small")
  @HTTPMethod("GET")
  public Response getSmallImage(@URIParam("repoName") String repoName, 
                                @URIParam("workspaceName") String wsName,
                                @URIParam("nodePath") String nodePath) throws Exception {
    return getThumbnailByType(repoName, wsName, nodePath, ThumbnailService.SMALL_SIZE);
  }
  
  private Response getThumbnailByType(String repoName, String wsName, String nodePath, 
      String propertyName) throws Exception {
    if(thumbnailService_.isEnableThumbnail()) {
      Node showingNode = getShowingNode(repoName, wsName, nodePath);
      if(!showingNode.isCheckedOut()) return Response.Builder.ok().build(); 
      if(!showingNode.hasProperty(propertyName)) {
        if(showingNode.getPrimaryNodeType().getName().equals("nt:file")) {
          Node content = showingNode.getNode("jcr:content");
          BufferedImage image = ImageIO.read(content.getProperty("jcr:data").getStream());
          if(content.getProperty("jcr:mimeType").getString().startsWith("image")) {
            thumbnailService_.createSpecifiedThumbnail(showingNode, image, propertyName);
          }
        }
      }
      String lastModified = null;
      if(showingNode.hasProperty(ThumbnailService.THUMBNAIL_LAST_MODIFIED)) {
        lastModified = showingNode.getProperty(ThumbnailService.THUMBNAIL_LAST_MODIFIED).getString();
      }
      InputStream inputStream = null;
      if(showingNode.hasProperty(propertyName)) {
        inputStream = showingNode.getProperty(propertyName).getStream();
      }
      return Response.Builder.ok()
                             .header(LASTMODIFIED, lastModified)
                             .entity(inputStream, "image")
                             .build();
    }
    return Response.Builder.ok().build();
  }
  
  private Node getShowingNode(String repoName, String wsName, String nodePath) throws Exception {
    ManageableRepository reposiotry = repositoryService_.getRepository(repoName);
    Session session = getSystemProvider().getSession(wsName, reposiotry);
    Node showingNode = null;
    if(nodePath.equals("/")) showingNode = session.getRootNode();
    else showingNode = (Node)session.getItem("/" + nodePath);
    return showingNode;
  }

  private SessionProvider getSystemProvider() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    SessionProviderService service = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    return service.getSystemSessionProvider(null) ;  
  }
}
