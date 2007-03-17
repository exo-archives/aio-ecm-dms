/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.versions;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.version.Version;

import org.exoplatform.ecm.jcr.JCRResourceResolver;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.templates.groovy.ResourceResolver;
import org.exoplatform.webui.application.RequestContext;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : lxchiati  
 *          lebienthuy@gmail.com
 * Oct 19, 2006
 * 10:07:15 AM
 */

@ComponentConfig(
  type     = UIViewVersion.class,
  template = "app:/groovy/webui/component/UIECMTabPane.gtmpl"
)

public class UIViewVersion extends UIContainer {

  public UIViewVersion() throws Exception {    
    addChild(UINodeInfo.class, null, null) ;
    addChild(UINodeProperty.class, null, null).setRendered(false) ;
  } 
 
  public String getTemplate() {
    Node node = getAncestorOfType(UIJCRExplorer.class).getCurrentNode() ;
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    try {
      String nodeType = node.getPrimaryNodeType().getName();
      if(isNodeTypeSupported(node)) return templateService.getTemplatePath(false, nodeType) ;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return super.getTemplate() ;
  }

  public ResourceResolver getTemplateResourceResolver(RequestContext context, String template) {
    Node node = getAncestorOfType(UIJCRExplorer.class).getCurrentNode() ;
    try {
      if(isNodeTypeSupported(node)) return new JCRResourceResolver(node.getSession(), "exo:templateFile") ;
    } catch (RepositoryException e) {
      e.printStackTrace();
    }
    return super.getTemplateResourceResolver(context, template);
  }
  
  public boolean isNodeTypeSupported(Node node) {
    try {      
      TemplateService templateService = getApplicationComponent(TemplateService.class) ;
      String nodeTypeName = node.getPrimaryNodeType().getName();
      return templateService.isManagedNodeType(nodeTypeName);
    } catch (Exception e) {
      return false;
    }
  }
  
  public Node getNode() throws RepositoryException {
    UIVersionInfo uiVersionInfo = getAncestorOfType(UIVersionInfo.class) ;
    Version version_ = uiVersionInfo.getCurrentVersionNode().getVersion() ;
    Node frozenNode = version_.getNode("jcr:frozenNode") ;
    return frozenNode ;
  }
  
  public Node getNodeByUUID(String uuid) throws Exception{
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    Session session = uiExplorer.getSession() ;
    return session.getNodeByUUID(uuid);
  }
  
  public List<Node> getRelations() throws Exception {
    List<Node> relations = new ArrayList<Node>() ;
    if (getNode().hasProperty("exo:relation")) {
      Value[] vals = getNode().getProperty("exo:relation").getValues();
      for (int i = 0; i < vals.length; i++) {
        String uuid = vals[i].getString();
        Node node = getNodeByUUID(uuid);
        relations.add(node);
      }
    }
    return relations;
  }
  
  public List<Node> getAttachments() throws Exception {
    List<Node> attachments = new ArrayList<Node>() ;
    NodeIterator childrenIterator = getNode().getNodes();;
    while (childrenIterator.hasNext()) {
      Node childNode = childrenIterator.nextNode();
      String nodeType = childNode.getPrimaryNodeType().getName();
      if ("nt:file".equals(nodeType)) attachments.add(childNode);
    }
    return attachments;
  }
  
  public boolean hasPropertyContent(Node node, String property){
    try {
      String value = node.getProperty(property).getString() ;
      if(value.length() > 0) return true ;
    } catch (Exception e) {
      e.printStackTrace() ;      
    }
    return false ;
  }
  
  public boolean isRssLink() { return false ; }
  public String getRssLink() { return null ; }
  
  public void update() throws Exception {    
    getChild(UINodeInfo.class).update();
  }  
}