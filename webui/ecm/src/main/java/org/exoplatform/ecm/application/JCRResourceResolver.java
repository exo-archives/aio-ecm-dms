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
package org.exoplatform.ecm.application;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * May 8, 2008 3:07:02 PM
 */
public class JCRResourceResolver extends ResourceResolver {  
  private String repository_ ;
  private String workspace_ ;  
  private String propertyName_ ;

  public JCRResourceResolver(String repository,String workspace,String propertyName) {
    repository_ = repository ;
    workspace_ = workspace;    
    propertyName_ = propertyName ;
  }

  @SuppressWarnings("unused")
  public URL getResource(String url) throws Exception {
    throw new Exception("This method is not  supported") ;  
  }

  public InputStream getInputStream(String url) throws Exception  {
    ExoContainer container = ExoContainerContext.getCurrentContainer() ;
    RepositoryService repositoryService = 
      (RepositoryService)container.getComponentInstanceOfType(RepositoryService.class) ;
    ManageableRepository manageableRepository = repositoryService.getRepository(repository_) ;
    //Use system session to access jcr resource
    SessionProvider provider = SessionProviderFactory.createSystemProvider();
    Session session = provider.getSession(workspace_,manageableRepository);
    Node node = (Node)session.getItem(removeScheme(url)) ;
    return new ByteArrayInputStream(node.getProperty(propertyName_).getString().getBytes()) ;
  }

  @SuppressWarnings("unused")
  public List<URL> getResources(String url) throws Exception {
    throw new Exception("This method is not  supported") ;
  }

  public List<InputStream> getInputStreams(String url) throws Exception {
    ArrayList<InputStream>  inputStreams = new ArrayList<InputStream>(1) ;
    inputStreams.add(getInputStream(url)) ;
    return inputStreams ;
  }

  @SuppressWarnings("unused")
  public boolean isModified(String url, long lastAccess) {  return false ; }

  public String createResourceId(String url) { return url ; }

  public String getResourceScheme() {  return "jcr:" ; }

}
