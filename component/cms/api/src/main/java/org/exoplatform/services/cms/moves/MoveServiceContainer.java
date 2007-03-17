package org.exoplatform.services.cms.moves;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;

public interface MoveServiceContainer {
  
  public static final String MOVE_TYPE_SUFFIX = "Move";

  public Collection<String> getMovePluginNames();
  
  public MovePlugin getMovePlugin(String moveServiceName);
  
  public MovePlugin getMovePluginForMoveType(String moveTypeName);

  public Collection<NodeType> getCreatedMoveTypes() throws Exception;
  
  public Node getMove(Node node, String moveName) throws Exception;

  public boolean hasMoves(Node node) throws Exception;

  public List<Node> getMoves(Node node) throws Exception;

  public void removeMove(Node node, String moveName) throws Exception;

  public void addMove(Node node, String type, Map mappings) throws Exception; 
  
}
