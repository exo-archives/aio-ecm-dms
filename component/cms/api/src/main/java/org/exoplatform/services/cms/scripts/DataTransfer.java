package org.exoplatform.services.cms.scripts;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

public class DataTransfer {
  private String workspace_ ;
  private String path_ ;
  private Node node_ ;
  private List<Node> contentList_ = new ArrayList<Node> () ;
  private Session session_ ;
  
  public DataTransfer() {}
  
  public void setWorkspace( String ws ) { workspace_ = ws ; }
  public String getWorkspace() { return workspace_ ; }
  
  public void setPath( String path ) { path_ = path ; }
  public String getPath() { return path_ ; }
  
  public Node getNode() { return node_ ; }
  public void setNode( Node node ) { node_ = node ; }
  
  public List<Node> getContentList() { return contentList_ ; }
  public void setContentList( List<Node> content ) { contentList_ = content ; }
  
  public void setSession(Session s) { session_ = s ; }
  public Session getSession() { return session_ ; }
}
