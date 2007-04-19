/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.exoplatform.ecm.jcr.ECMViewComponent;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.portal.component.view.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.comments.CommentsService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
  private String language_ = "default";
  private String selectedLanguage_ ;
  public UIViewSearchResult() throws Exception {
  }

  public String getTemplate() {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String userName = Util.getUIPortal().getOwner() ;
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    String temp = null ;
    try {
      String nodeType = node_.getPrimaryNodeType().getName() ;
      if(uiExplorer.getPreference().isJcrEnable()) {
        uiExplorer.setSelectNode(node_) ;
        return uiExplorer.getDocumentInfoTemplate();
      }
      temp = templateService.getTemplatePathByUser(false, nodeType, userName) ;
    } catch(Exception e) {
      e.printStackTrace() ;
    }
    return temp; 
  }
  
  public List<Node> getAttachments() throws Exception {
    List<Node> attachments = new ArrayList<Node>() ;
    NodeIterator childrenIterator = node_.getNodes();;
    while(childrenIterator.hasNext()) {
      Node childNode = childrenIterator.nextNode();
      String nodeType = childNode.getPrimaryNodeType().getName();
      if("nt:file".equals(nodeType)) attachments.add(childNode);
    }
    return attachments;
  }

  public Node getNode() throws ValueFormatException, PathNotFoundException, RepositoryException { 
    if(node_.hasProperty("exo:language")) {
      String defaultLang = node_.getProperty("exo:language").getString() ;
      if(selectedLanguage_ == null) selectedLanguage_ =  defaultLang ;
      if(node_.hasNode("languages")) {
        if(!language_.equals("default") && !language_.equals(defaultLang)) {
          Node curNode = node_.getNode("languages/" + language_) ;
          selectedLanguage_ = language_ ;
          language_ = defaultLang ;
          return curNode ;
        } 
        selectedLanguage_ = defaultLang ;
      }
    }    
    return node_ ; 
  }

  public String getNodeType() throws Exception { return null; }
  
  public List<Node> getRelations() throws Exception {
    List<Node> relations = new ArrayList<Node>() ;
    if (node_.hasProperty("exo:relation")) {
      Value[] vals = node_.getProperty("exo:relation").getValues();
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
    if(node_.hasNode("languages")){
      Node languages = node_.getNode("languages") ;
      NodeIterator iter = languages.getNodes() ;
      while(iter.hasNext()) {
        local.add(iter.nextNode().getName()) ;
      }
      local.add(node_.getProperty("exo:language").getString()) ;      
    } 
    return local ;
  }

  public String getTemplatePath() throws Exception { return null; }

  public boolean isNodeTypeSupported() { return false; }
  
  public boolean isNodeTypeSupported(String nodeTypeName) {
    try {      
      TemplateService templateService = getApplicationComponent(TemplateService.class);
      return templateService.isManagedNodeType(nodeTypeName);
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
    if(node_.hasProperty("exo:language")) {
      String defaultLang = node_.getProperty("exo:language").getString() ;
      if(selectedLanguage_ == null) selectedLanguage_ =  defaultLang ;
      return getApplicationComponent(CommentsService.class).getComments(node_, selectedLanguage_) ;
    }
    return getApplicationComponent(CommentsService.class).getComments(node_, language_) ;
  }
  
  public String getViewTemplate(String nodeTypeName, String templateName) throws Exception {
    TemplateService tempServ = getApplicationComponent(TemplateService.class) ;
    return tempServ.getTemplatePath(false, nodeTypeName, templateName) ;
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
  
  static public class ChangeLanguageActionListener extends EventListener<UIViewSearchResult> {
    public void execute(Event<UIViewSearchResult> event) throws Exception {
      UIViewSearchResult uiViewSearchResult = event.getSource() ;
      String selectedLanguage = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiViewSearchResult.setLanguage(selectedLanguage) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewSearchResult.getParent()) ;
    }   
  }

  public String getImage(Node node) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public String getPortalName() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getWebDAVServerPrefix() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public String getWorkspaceName() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
}
