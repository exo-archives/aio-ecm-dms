package org.jbpm.graph.node;

import org.jbpm.graph.def.*;

public class ProcessFactory {

  public static ProcessDefinition createProcessDefinition(String[] nodes, String[] transitions) {
    ProcessDefinition pd = new ProcessDefinition();
    addNodesAndTransitions(pd, nodes, transitions);
    return pd;
  }

  public static void addNodesAndTransitions(ProcessDefinition pd, String[] nodes, String[] transitions) {
    for ( int i = 0; i < nodes.length; i++ ) {
      pd.addNode( createNode( nodes[i] ) );
    }

    for ( int i = 0; i < transitions.length; i++ ) {
      String[] parsedTransition = cutTransitionText( transitions[i] );
      Node from = pd.getNode( parsedTransition[0] );
      Node to = pd.getNode( parsedTransition[2] );
      Transition t = new Transition( parsedTransition[1] );
      from.addLeavingTransition(t);
      to.addArrivingTransition(t);
    }
  }

  public static String getTypeName(Node node) {
    if (node==null) return null;
    return NodeTypes.getNodeName(node.getClass());
  }

  /**
   * @throws NullPointerException if text is null.
   */
  public static Node createNode(String text) {
    Node node = null;
    
    String typeName = null;
    String name = null;
    
    text = text.trim();
    int spaceIndex = text.indexOf(' ');
    if (spaceIndex!=-1) {
      typeName = text.substring(0, spaceIndex);
      name = text.substring(spaceIndex + 1);
    } else {
      typeName = text;
      name = null;
    }

    Class nodeType = NodeTypes.getNodeType(typeName);
    if ( nodeType==null ) throw new IllegalArgumentException("unknown node type name '" + typeName + "'");
    try {
      node = (Node) nodeType.newInstance();
      node.setName(name);
    } catch (Exception e) {
      throw new RuntimeException("couldn't instantiate nodehandler for type '" + typeName + "'");
    }
    return node;
  }

  public static String[] cutTransitionText(String transitionText) {
    String[] parts = new String[3];
    if ( transitionText == null ) {
      throw new NullPointerException( "transitionText is null" );
    }
    int start = transitionText.indexOf( "--" );
    if ( start == -1 ) {
      throw new IllegalArgumentException( "incorrect transition format exception : nodefrom --transitionname--> nodeto" );
    }
    parts[0] = transitionText.substring(0,start).trim();

    int end = transitionText.indexOf( "-->", start );
    if ( start < end ) {
      parts[1] = transitionText.substring(start+2,end).trim();
    } else {
      parts[1] = null;
    }
    parts[2] = transitionText.substring(end+3).trim();
    return parts;
  }
}
