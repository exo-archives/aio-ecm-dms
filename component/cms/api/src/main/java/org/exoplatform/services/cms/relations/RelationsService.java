package org.exoplatform.services.cms.relations;

import java.util.List;

import javax.jcr.Node;
/**
 * @author monica franceschini
 */
public interface RelationsService {

  public boolean hasRelations(Node node) throws Exception;

  public List<Node> getRelations(Node node) throws Exception;

  public void removeRelation(Node node, String relationPath) throws Exception;

  public void addRelation(Node node, String relationPath) throws Exception; 
  
  public void addRelation(Node node, String relationPath, boolean replaceAll) throws Exception; 
}
