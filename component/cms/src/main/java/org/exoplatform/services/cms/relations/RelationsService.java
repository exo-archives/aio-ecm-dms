package org.exoplatform.services.cms.relations;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
/**
 * @author monica franceschini
 */
public interface RelationsService {

  public boolean hasRelations(Node node) throws Exception;

  public List<Node> getRelations(Node node, String repository) throws Exception;

  public void removeRelation(Node node, String relationPath, String repository) throws Exception;

  public void addRelation(Node node, String relationPath, Session session) throws Exception; 
  
  public void init(String repository) throws Exception ;
  
  //public void addRelation(Node node, String relationPath, boolean replaceAll) throws Exception; 
}
