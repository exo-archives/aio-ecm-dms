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
package org.exoplatform.ecm.webui.presentation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Value;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.categories.CategoriesService;
import org.exoplatform.services.cms.comments.CommentsService;
import org.exoplatform.services.cms.folksonomy.FolksonomyService;
import org.exoplatform.services.cms.i18n.MultiLanguageService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.cms.voting.VotingService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.application.portlet.PortletRequestContext;

//TODO: Auto-generated Javadoc
/*
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 23, 2008  
 */
/**
 * The Class UIBaseNodePresentation should implement some common method in
 * NodePresentation like getIcons,getWebDavLink....
 */
public abstract class UIBaseNodePresentation extends UIContainer implements NodePresentation {

  /** The language_. */
  private String language_ ;

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getNode()
   */
  public abstract Node getNode() throws Exception ;

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getTemplatePath()
   */
  public abstract String getTemplatePath() throws Exception ;

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getOriginalNode()
   */
  public abstract Node getOriginalNode() throws Exception ;

  /**
   * Gets the repository name.
   * 
   * @return the repository name
   * 
   * @throws Exception the exception
   */
  public abstract String getRepositoryName() throws Exception ;

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#encodeHTML(java.lang.String)
   */
  public String encodeHTML(String text) throws Exception { return Utils.encodeHTML(text) ; }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getAttachments()
   */
  public List<Node> getAttachments() throws Exception {
    List<Node> attachments = new ArrayList<Node>() ;
    NodeIterator childrenIterator = getNode().getNodes();;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    while (childrenIterator.hasNext()) {
      Node childNode = childrenIterator.nextNode();
      String nodeType = childNode.getPrimaryNodeType().getName();
      List<String> listCanCreateNodeType = 
        Utils.getListAllowedFileType(getNode(), getRepository(), templateService) ;
      if (listCanCreateNodeType.contains(nodeType)) attachments.add(childNode);
    }
    return attachments;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getComments()
   */
  public List<Node> getComments() throws Exception {
    return getApplicationComponent(CommentsService.class).getComments(getNode(), getLanguage()) ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getComponentInstanceOfType(java.lang.String)
   */
  @SuppressWarnings("unchecked")
  public Object getComponentInstanceOfType(String className) {
    Object service = null;
    try {
      ClassLoader loader =  Thread.currentThread().getContextClassLoader();
      Class object = loader.loadClass(className);
      service = getApplicationComponent(object);
    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
    } 
    return service;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getDownloadLink(javax.jcr.Node)
   */
  public String getDownloadLink(Node node) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource dresource ;
    if(!node.getPrimaryNodeType().getName().equals(Utils.NT_FILE)) return null; 
    Node jcrContentNode = node.getNode(Utils.JCR_CONTENT) ;
    InputStream input = jcrContentNode.getProperty(Utils.JCR_DATA).getStream() ;
    dresource = new InputStreamDownloadResource(input, "image") ;
    dresource.setDownloadName(node.getName()) ;
    return dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getIcons(javax.jcr.Node, java.lang.String)
   */
  public String getIcons(Node node, String size) throws Exception { return Utils.getNodeTypeIcon(node, size) ; }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getImage(javax.jcr.Node)
   */
  public String getImage(Node node) throws Exception {    
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource dresource ;
    Node imageNode = node.getNode(Utils.EXO_IMAGE) ;
    InputStream input = imageNode.getProperty(Utils.JCR_DATA).getStream() ;
    dresource = new InputStreamDownloadResource(input, "image") ;
    dresource.setDownloadName(node.getName()) ;
    return dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getLanguage()
   */
  public String getLanguage() { return language_ ; }  

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#setLanguage(java.lang.String)
   */
  public void setLanguage(String language) { language_ = language ; }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getPortalName()
   */
  public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getRelations()
   */
  public List<Node> getRelations() throws Exception {
    List<Node> relations = new ArrayList<Node>() ;
    if (getNode().hasProperty(Utils.EXO_RELATION)) {
      Value[] vals = getNode().getProperty(Utils.EXO_RELATION).getValues();
      for (int i = 0; i < vals.length; i++) {
        String uuid = vals[i].getString();
        Node node = getNodeByUUID(uuid);
        relations.add(node);
      }
    }
    return relations;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getRepository()
   */
  public String getRepository() throws Exception {
    return ((ManageableRepository)getNode().getSession().getRepository()).getConfiguration().getName() ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getRssLink()
   */
  public String getRssLink() { return null ; }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#isRssLink()
   */
  public boolean isRssLink() { return false ; }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getSupportedLocalise()
   */
  public List getSupportedLocalise() throws Exception {
    MultiLanguageService multiLanguageService = getApplicationComponent(MultiLanguageService.class) ;
    return multiLanguageService.getSupportedLanguages(getNode()) ;
  }  

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getViewTemplate(java.lang.String, java.lang.String)
   */
  public String getViewTemplate(String nodeTypeName, String templateName) throws Exception {
    TemplateService tempServ = getApplicationComponent(TemplateService.class) ;
    return tempServ.getTemplatePath(false, nodeTypeName, templateName, getRepositoryName()) ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getWebDAVServerPrefix()
   */
  public String getWebDAVServerPrefix() throws Exception {
    PortletRequestContext portletRequestContext = PortletRequestContext.getCurrentInstance() ;
    String prefixWebDAV = portletRequestContext.getRequest().getScheme() + "://" + 
    portletRequestContext.getRequest().getServerName() + ":" +
    String.format("%s",portletRequestContext.getRequest().getServerPort()) ;
    return prefixWebDAV ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getWorkspaceName()
   */
  public String getWorkspaceName() throws Exception {
    return getNode().getSession().getWorkspace().getName();
  }  

  /**
   * Gets the node by uuid.
   * 
   * @param uuid the uuid
   * 
   * @return the node by uuid
   * 
   * @throws Exception the exception
   */
  public Node getNodeByUUID(String uuid) throws Exception{ 
    ManageableRepository manageRepo = getApplicationComponent(RepositoryService.class).getRepository(getRepositoryName()) ;
    String[] workspaces = manageRepo.getWorkspaceNames() ;
    //TODO: SystemProvider or SessionProvider
    SessionProvider provider = SessionProviderFactory.createSystemProvider() ;
    for(String ws : workspaces) {
      try{
        return provider.getSession(ws,manageRepo).getNodeByUUID(uuid) ;
      }catch(Exception e) { }      
    }
    return null;
  }

  /**
   * Retrieve all categories of a node
   * 
   * @param node the node
   * 
   * @return the categories
   * 
   * @throws Exception the exception
   */
  public List<Node> getCategories(Node node) throws Exception {
    CategoriesService categoriesService = getApplicationComponent(CategoriesService.class);
    return categoriesService.getCategories(node,getRepositoryName());
  }

  /**
   * Retrieve all tags of a node
   * 
   * @param node the node
   * 
   * @return the tags
   * 
   * @throws Exception the exception
   */
  public List<Node> getTags(Node node) throws Exception {
    FolksonomyService folksonomyService = getApplicationComponent(FolksonomyService.class);
    return folksonomyService.getLinkedTagsOfDocument(node,getRepositoryName());
  }

  /**
   * Retrieve the voting rate
   * 
   * @param node the node
   * 
   * @return the votes
   * 
   * @throws Exception the exception
   */
  public long getVotingRate(Node node) throws Exception {
    VotingService votingService = getApplicationComponent(VotingService.class);
    return votingService.getVoteTotal(node);
  }

  /**
   * Retrieve the image in property value
   * 
   * @param node the node
   * @param propertyName the property name
   * 
   * @return the image in property
   * 
   * @throws Exception the exception
   */
  public String getImageURIInProperty(Node node, String propertyName) throws Exception {            
    try {
      InputStream input = node.getProperty(propertyName).getStream() ;
      InputStreamDownloadResource dresource = new InputStreamDownloadResource(input, "image") ;
      dresource.setDownloadName(node.getName()) ;
      DownloadService dservice = getApplicationComponent(DownloadService.class) ;
      return dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
    } catch (Exception e) {
    }    
    return null;
  }

}
