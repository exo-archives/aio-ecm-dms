package org.exoplatform.services.cms.relations.impl;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.relations.RelationsService;
import org.exoplatform.services.jcr.RepositoryService;
import org.picocontainer.Startable;

/**
 * @author monica franceschini
 */

public class RelationsServiceImpl implements RelationsService, Startable {
  private static final String RELATION_MIXIN = "exo:relationable";
  private static final String RELATION_PROP = "exo:relation";

  private RepositoryService repositoryService_;
  String repositories_ ;
  private CmsConfigurationService cmsConfig_;

  public RelationsServiceImpl(RepositoryService repositoryService,
      CmsConfigurationService cmsConfig, InitParams params) {
    repositoryService_ = repositoryService;
    cmsConfig_ = cmsConfig;
    repositories_ = params.getValueParam("repositories").getValue();
  }

  public boolean hasRelations(Node node) throws Exception {
    if (node.isNodeType(RELATION_MIXIN))
      return true;
    return false;

  }

  public List<Node> getRelations(Node node, Session session) {
    List<Node> rels = new ArrayList<Node>();
    try {
      if(node.hasProperty(RELATION_PROP)) {
        Value[] values = node.getProperty(RELATION_PROP).getValues();
        for (int i = 0; i < values.length; i++) {
          rels.add(session.getNodeByUUID(values[i].getString()));
        }
      }
    } catch(Exception e) {
      e.printStackTrace() ;
    }
    return rels ;    
  }

  public void removeRelation(Node node, String relationPath, Session session) throws Exception {
    List<Value> vals = new ArrayList<Value>();
    if (!"*".equals(relationPath)) {
      Property relations = node.getProperty(RELATION_PROP);
      if (relations != null) {
        Value[] values = relations.getValues();
        String uuid2Remove = null;
        for (int i = 0; i < values.length; i++) {
          String uuid = values[i].getString();
          Node refNode = session.getNodeByUUID(uuid);
          if (refNode.getPath().equals(relationPath)) {
            uuid2Remove = uuid;
          } else {
            vals.add(values[i]);
          }
        }
        if (uuid2Remove == null) return;
      }
    }
    node.setProperty(RELATION_PROP, vals.toArray(new Value[vals.size()]));
  }

  public void addRelation(Node node, String relationPath, Session session) throws Exception {
    Node catNode = (Node) session.getItem(relationPath);    
    if(!catNode.isNodeType("mix:referenceable")) {
      catNode.addMixin("mix:referenceable") ;
      catNode.save() ;
      session.save() ;
      session.refresh(true) ;
    }      
    Value value2add = session.getValueFactory().createValue(catNode); 
    if (!node.isNodeType(RELATION_MIXIN)) {
      node.addMixin(RELATION_MIXIN);    
      node.setProperty(RELATION_PROP, new Value[] {value2add});
    } else {
      List<Value> vals = new ArrayList<Value>();
      Value[] values = node.getProperty(RELATION_PROP).getValues();
      for (int i = 0; i < values.length; i++) {
        Value value = values[i];
        String uuid = value.getString();
        Node refNode = session.getNodeByUUID(uuid);
        if(refNode.getPath().equals(relationPath))
          return;
        vals.add(value);
      }
      vals.add(value2add);
      node.setProperty(RELATION_PROP, vals.toArray(new Value[vals.size()]));
      session.save() ;
      session.refresh(true) ;
    }
  }

  public void start() {
    Session session = null;
    Node relationsHome = null;
    try {
      String relationPath = cmsConfig_.getJcrPath(BasePath.CMS_PUBLICATIONS_PATH);
      String[] repositories = repositories_.split(",") ;
      for(String repo : repositories) {
        session = getSession(repo.trim());
        relationsHome = (Node) session.getItem(relationPath);
        for (NodeIterator iterator = relationsHome.getNodes(); iterator.hasNext();) {
          Node rel = iterator.nextNode();
          rel.addMixin("mix:referenceable");
        }
        relationsHome.save();
        session.save();
        session.logout();
      }      
    } catch (Exception e) {
      if(session !=null && session.isLive()) session.logout();
      // e.printStackTrace() ;
    }
  }

  public void stop() {
  }

  public void init(String repository) throws Exception {
    try {
      Session session = getSession(repository);
      String relationPath = cmsConfig_.getJcrPath(BasePath.CMS_PUBLICATIONS_PATH);
      Node relationsHome = (Node) session.getItem(relationPath);
      for (NodeIterator iterator = relationsHome.getNodes(); iterator.hasNext();) {
        Node rel = iterator.nextNode();
        rel.addMixin("mix:referenceable");
      }
      relationsHome.save();
      session.logout();
    } catch (Exception e) {
      // e.printStackTrace() ;
    }
  }

  protected Session getSession(String repository) throws Exception {	
    return repositoryService_.getRepository(repository)
    .getSystemSession(cmsConfig_.getWorkspace(repository));    	
  }
}
