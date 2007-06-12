/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.portlet.PortletRequest;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.ecm.jcr.ECMViewComponent;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorerPortlet;
import org.exoplatform.portal.component.view.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.comments.CommentsService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Apr 6, 2007 4:21:18 PM
 */
@ComponentConfig(
    events = @EventConfig(listeners = UIViewSearchResult.ChangeLanguageActionListener.class)
)
public class UIViewSearchResult extends UIContainer implements ECMViewComponent {
  
  private Node node_ ;
  private String language_ ;
  public UIViewSearchResult() throws Exception {
  }

  public String getTemplate() {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    String repository = getAncestorOfType(UIJCRExplorerPortlet.class).getPreferenceRepository() ;
    try {
      String nodeType = node_.getPrimaryNodeType().getName() ;
      return templateService.getTemplatePathByUser(false, nodeType, userName, repository) ;
    } catch(Exception e) {
      e.printStackTrace() ;
    }
    return null; 
  }
  
  public List<Node> getAttachments() throws Exception {
    List<Node> attachments = new ArrayList<Node>() ;
    NodeIterator childrenIterator = node_.getNodes();;
    while(childrenIterator.hasNext()) {
      Node childNode = childrenIterator.nextNode();
      String nodeType = childNode.getPrimaryNodeType().getName();
      if(Utils.NT_FILE.equals(nodeType)) attachments.add(childNode);
    }
    return attachments;
  }

  public Node getNode() throws ValueFormatException, PathNotFoundException, RepositoryException { 
    if(node_.hasProperty(Utils.EXO_LANGUAGE)) {
      String defaultLang = node_.getProperty(Utils.EXO_LANGUAGE).getString() ;
      if(language_ == null) language_ =  defaultLang ;
      if(node_.hasNode(Utils.LANGUAGES)) {
        if(!language_.equals(defaultLang)) {
          Node curNode = node_.getNode(Utils.LANGUAGES + Utils.SLASH + language_) ;
          return curNode ;
        } 
      }
      return node_ ;
    }    
    return node_ ; 
  }
  public Node getOriginalNode() throws Exception {return node_;}
  
  public String getNodeType() throws Exception { return null; }
  
  public List<Node> getRelations() throws Exception {
    List<Node> relations = new ArrayList<Node>() ;
    if (node_.hasProperty(Utils.EXO_RELATION)) {
      Value[] vals = node_.getProperty(Utils.EXO_RELATION).getValues();
      for (int i = 0; i < vals.length; i++) {
        String uuid = vals[i].getString();
        Node node = getNodeByUUID(uuid);
        relations.add(node);
      }
    }
    return relations;
  }

  public boolean isRssLink() { return false ; }
  public String getRssLink() { return null ; }

  public List getSupportedLocalise() throws Exception {
    List<String> local = new ArrayList<String>() ;
    if(node_.hasNode(Utils.LANGUAGES)){
      Node languages = node_.getNode(Utils.LANGUAGES) ;
      NodeIterator iter = languages.getNodes() ;
      while(iter.hasNext()) {
        local.add(iter.nextNode().getName()) ;
      }
      local.add(node_.getProperty(Utils.EXO_LANGUAGE).getString()) ;      
    } 
    return local ;
  }

  public String getTemplatePath() throws Exception { return null; }

  public boolean isNodeTypeSupported() { return false; }
  
  public boolean isNodeTypeSupported(String nodeTypeName) {
    try {      
      TemplateService templateService = getApplicationComponent(TemplateService.class);
      String repository = getAncestorOfType(UIJCRExplorerPortlet.class).getPreferenceRepository() ;
      return templateService.isManagedNodeType(nodeTypeName, repository);
    } catch (Exception e) {
      return false;
    }
  }
  
  public boolean hasPropertyContent(Node node, String property) {
    try {
      String value = node.getProperty(property).getString() ;
      if(value.length() > 0) return true ;
    } catch (Exception e) {
      e.printStackTrace() ;      
    }
    return false ;
  }

  public void setNode(Node node) { node_ = node ; }
  
  public Node getNodeByUUID(String uuid) throws Exception{
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    Session session = uiExplorer.getSession() ;
    return session.getNodeByUUID(uuid);
  }
  
  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return getAncestorOfType(UIJCRExplorer.class).getJCRTemplateResourceResolver() ;
  }

  public List<Node> getComments() throws Exception {
    return getApplicationComponent(CommentsService.class).getComments(node_, language_) ;
  }
  
  public String getViewTemplate(String nodeTypeName, String templateName) throws Exception {
    TemplateService tempServ = getApplicationComponent(TemplateService.class) ;
    String repository = getAncestorOfType(UIJCRExplorerPortlet.class).getPreferenceRepository() ;
    return tempServ.getTemplatePath(false, nodeTypeName, templateName, repository) ;
  }

  public String getLanguage() { return language_; }

  public void setLanguage(String language) { language_ = language ; }

  @SuppressWarnings("unchecked")
  public Object getComponentInstanceOfType(String className) {
    Object service = null;
    try {
      ClassLoader loader =  Thread.currentThread().getContextClassLoader();
      Class clazz = loader.loadClass(className);
      service = getApplicationComponent(clazz);
    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
    } 
    return service;
  }
  

  public String getImage(Node node) throws Exception {
    DownloadService downloadService = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource inputResource ;
    Node imageNode = node.getNode(Utils.EXO_IMAGE) ;
    InputStream input = imageNode.getProperty(Utils.JCR_DATA).getStream() ;
    inputResource = new InputStreamDownloadResource(input, "image") ;
    inputResource.setDownloadName(node.getName()) ;
    return downloadService.getDownloadLink(downloadService.addDownloadResource(inputResource)) ;
  }

  public String getPortalName() {
    PortalContainer pContainer = PortalContainer.getInstance() ;
    return pContainer.getPortalContainerInfo().getContainerName() ;
  }

  public String getWebDAVServerPrefix() throws Exception {
    PortletRequestContext pRequestContext = PortletRequestContext.getCurrentInstance() ;
    PortletRequest pRequest = pRequestContext.getRequest() ;
    String prefixWebDAV = pRequest.getScheme() + "://" + pRequest.getServerName() + ":" 
                          + String.format("%s",pRequest.getServerPort()) ;
    return prefixWebDAV ;
  }

  public String getWorkspaceName() throws Exception {
    return node_.getSession().getWorkspace().getName() ;
  }
  static public class ChangeLanguageActionListener extends EventListener<UIViewSearchResult> {
    public void execute(Event<UIViewSearchResult> event) throws Exception {
      UIViewSearchResult uiViewSearchResult = event.getSource() ;
      String selectedLanguage = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiViewSearchResult.setLanguage(selectedLanguage) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewSearchResult.getParent()) ;
    }   
  }
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

  public String encodeHTML(String text) throws Exception {
    return Utils.encodeHTML(text) ;
  }
}
