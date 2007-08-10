package org.exoplatform.services.cms.relations;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;
/**
 * @author monica franceschini
 */
public interface RelationsService {

  public boolean hasRelations(Node node) throws Exception;

  public List<Node> getRelations(Node node, String repository, SessionProvider provider) throws Exception;

  public void removeRelation(Node node, String relationPath, String repository) throws Exception;

  public void addRelation(Node node, String relationPath, String workspaceName,String repository) throws Exception; 
  
  public void init(String repository) throws Exception ;
  
  //public void addRelation(Node node, String relationPath, boolean replaceAll) throws Exception; 
}
