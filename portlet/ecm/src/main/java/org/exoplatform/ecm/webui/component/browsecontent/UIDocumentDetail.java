/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Value;

import org.exoplatform.ecm.jcr.CommentsComponent;
import org.exoplatform.ecm.jcr.ECMViewComponent;
import org.exoplatform.ecm.jcr.VoteComponent;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.portal.component.view.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.comments.CommentsService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Pham
 *          phamtuanchip@gmail.com
 * Jan 15, 2007  
 */

@ComponentConfig( )

public class UIDocumentDetail extends UIComponent implements ECMViewComponent, VoteComponent, CommentsComponent{
  private Node node_ ;
  private String nodeType_ ;

  public UIDocumentDetail() {} 

  public String getTemplatePath(){
    String userName = Util.getUIPortal().getOwner() ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    try{
      return templateService.getTemplatePathByUser(false, getNodeType(), userName) ;
    }catch(Exception e) {
      return null ;
    }    
  }

  public String getTemplate(){ return getTemplatePath() ;}

  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return getAncestorOfType(UIBrowseContainer.class).getTemplateResourceResolver(context, template) ;
  }

  public Node getNode() {return node_ ;}

  public void setNode(Node docNode) {node_ = docNode ;}

  public List<Node> getAttachments() throws Exception {
    List<Node> attachments = new ArrayList<Node>() ;
    NodeIterator childrenIterator = getNode().getNodes();;
    while (childrenIterator.hasNext()) {
      Node childNode = childrenIterator.nextNode();
      String nodeType = childNode.getPrimaryNodeType().getName();
      if (Utils.NT_FILE.equals(nodeType)) attachments.add(childNode);
    }
    return attachments;
  }

  public List<Node> getRelations() throws Exception {
    List<Node> relations = new ArrayList<Node>() ;
    Node node = getNode() ;
    if (node.hasProperty(Utils.EXO_RELATION)) {
      Value[] vals = node.getProperty(Utils.EXO_RELATION).getValues();
      for (Value val : vals) {
        String uuid = val.getString();
        Node rnode = getNodeByUUID(uuid);
        relations.add(rnode);
      }
    }
    return relations;
  }

  public boolean isNodeTypeSupported() {
    try {      
      TemplateService templateService = getApplicationComponent(TemplateService.class);
      return templateService.isManagedNodeType(getNodeType());
    } catch (Exception e) {
      return false;
    }
  }

  public Node getNodeByUUID(String uuid) throws Exception{ 
    UIBrowseContainer container = getAncestorOfType(UIBrowseContainer.class) ;
    return container.getNodeByUUID(uuid);
  }

  public boolean hasPropertyContent(Node node, String property){
    try {
      return node.hasProperty(property) ;
    } catch (Exception e) {
      return false ;
    }

  }

  public String getNodeType() throws Exception {
    nodeType_ = getNode().getPrimaryNodeType().getName() ;
    return nodeType_ ;
  }

  public String getRssLink() {return null ;}

  public List getSupportedLocalise() throws Exception {return null ;}

  public boolean isRssLink() {return false ;}
  
  public double getRating() throws Exception {
    return getNode().getProperty("exo:votingRate").getDouble();
  }

  public long getVoteTotal() throws Exception {
    return getNode().getProperty("exo:voteTotal").getLong();
  }
  
  public String getLanguage(Node node) throws Exception {
    return node.getProperty("exo:language").getValue().getString() ;
  }

  public List<Node> getComments() throws Exception {
    return getApplicationComponent(CommentsService.class).getComments(getNode(), getLanguage(getNode())) ;
  }
  
  public String getCommentTemplate() throws Exception {
    TemplateService tempServ = getApplicationComponent(TemplateService.class) ;
    return tempServ.getTemplatePath(false, "exo:comment", "view1") ;
  }
  
  public String getVoteTemplate() throws Exception {
    TemplateService tempServ = getApplicationComponent(TemplateService.class) ;
    return tempServ.getTemplatePath(false, "exo:vote", "view1") ;
  }
}