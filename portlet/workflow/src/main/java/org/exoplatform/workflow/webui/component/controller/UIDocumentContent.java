/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.workflow.webui.component.controller ;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Value;

import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.templates.groovy.ResourceResolver;
import org.exoplatform.webui.application.RequestContext;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.workflow.webui.component.ECMViewComponent;
import org.exoplatform.workflow.webui.component.JCRResourceResolver;

/**
 * Created by The eXo Platform SARL
 * Author : tran the  trong
 *          trongtt@gmail.com
 * July 3, 2006
 * 10:07:15 AM
 */
@ComponentConfig(template = "app:/groovy/webui/component/UIDocumentContent.gtmpl")
public class UIDocumentContent extends UIContainer implements ECMViewComponent {

  private Node node_ ;

  public UIDocumentContent() throws Exception {}
  
  public void setNode(Node node) { this.node_ = node; }
  public Node getNode() { return node_; }

  public String getNodeType() throws Exception { return null ; }
  
  public String getTemplate() {
    UITask task = getAncestorOfType(UITaskManager.class).getChild(UITask.class) ;
    String temp = "" ;
    try {
      if(task.isView()) {
        if(isNodeTypeSupported()) temp = getTemplatePath() ;
        else temp = super.getTemplate() ;
      } else if(task.isCreatedOrUpdated()) temp = task.getDialogPath() ;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return temp ;
  }
  
  public ResourceResolver getTemplateResourceResolver(RequestContext context, String template) {
    UITask task = getAncestorOfType(UITaskManager.class).getChild(UITask.class) ;
    try {
      if((task.isView() && isNodeTypeSupported()) || task.isCreatedOrUpdated()) {
        return new JCRResourceResolver(node_.getSession(), "exo:templateFile") ;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return super.getTemplateResourceResolver(context, template);
  }
  
  public boolean isNodeTypeSupported() {
    try {      
      TemplateService templateService = getApplicationComponent(TemplateService.class) ;
      String nodeTypeName = node_.getPrimaryNodeType().getName();
      return templateService.isManagedNodeType(nodeTypeName);
    } catch (Exception e) {
      return false;
    }
  }

  public List<Node> getRelations() throws Exception {
    List<Node> relations = new ArrayList<Node>();
    try {
      Value[] vals = node_.getProperty("exo:relation").getValues();
      for (Value val : vals) {
        String uuid = val.getString();
        Node currentNode = node_.getSession().getNodeByUUID(uuid);
        relations.add(currentNode);
      }
    } catch (Exception e) {}
    return relations;
  }
  
  public List<Node> getAttachments() throws Exception {
    List<Node> attachments = new ArrayList<Node>();
    String nodeType = "";
    NodeIterator childrenIterator;
    childrenIterator = node_.getNodes();
    while (childrenIterator.hasNext()) {
      Node childNode = childrenIterator.nextNode();
      try {
        nodeType = childNode.getPrimaryNodeType().getName();
        if ("nt:file".equals(nodeType)) attachments.add(childNode);
      } catch (Exception e) {}
    }
    return attachments;
  }  

  public String getAttachmentSize(Node node){
    Node jcrContentNode = null;
    int size = 0;
    String sSize = "0";
    String nodeType = "";
    try {      
      nodeType = node.getPrimaryNodeType().getName();     
    } catch (Exception ex) {
      nodeType = "default";
    }
    try {
      if ("nt:file".equals(nodeType)) jcrContentNode = node.getNode("jcr:content") ;
      else if ("nt:resource".equals(nodeType)) jcrContentNode = node ;
      if (jcrContentNode != null) {
        Property data = null;
        if (jcrContentNode.hasProperty("jcr:data"))
          data = jcrContentNode.getProperty("jcr:data");

        InputStream is = data.getValue().getStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = is.read(buf)) > 0) {
          size += len;
        }
        is.close();
      }
    } catch (Exception e) {}

    if(size >0){
      sSize = String.valueOf((((float)(size/100))/10));
    }
    sSize += " Kb";
    return sSize;
  }


  public boolean hasPropertyContent(Node nod, String property){
    try {
      String value = nod.getProperty(property).getString() ;
      if(value.length() > 0) return true ;
    }catch (Exception e) {
      e.printStackTrace() ;      
    }
    return false ;
  }


  public String getRssLink() { return null ; }
  public boolean isRssLink() { return false ; }

  public List getSupportedLocalise() throws Exception { return null ; }

  public String getTemplatePath() throws Exception { 
    String nodeTypeName = node_.getPrimaryNodeType().getName();
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    return templateService.getTemplatePath(false, nodeTypeName);
  }
}