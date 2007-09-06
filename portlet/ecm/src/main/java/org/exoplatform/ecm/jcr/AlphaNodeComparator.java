package org.exoplatform.ecm.jcr;

import java.util.Comparator;

import javax.jcr.Node;

import org.exoplatform.ecm.jcr.model.Preference;


public class AlphaNodeComparator implements Comparator<Node> {
  private String order_ ;
  
  public AlphaNodeComparator(String pOrder) {
    order_ = pOrder ;
  }
  
  public int compare(Node node1, Node node2) {
    try{
      String nodeName1 = node1.getName();
      String nodeName2 = node2.getName();
      if(order_.equals(Preference.ASCENDING_ORDER)) {
        return nodeName1.compareToIgnoreCase(nodeName2) ;
      }
      return nodeName2.compareToIgnoreCase(nodeName1) ;
    }catch (Exception e) {
    }    
    return 0;
  }
}
