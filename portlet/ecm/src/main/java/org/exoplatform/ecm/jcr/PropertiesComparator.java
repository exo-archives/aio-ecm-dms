package org.exoplatform.ecm.jcr;

import java.util.Comparator;

import org.exoplatform.ecm.jcr.model.Preference;


public class PropertiesComparator implements Comparator {
  private String order_;
  
  public PropertiesComparator(String pOrder) {
    order_ = pOrder ;
  }
  
  public int compare(Object o1, Object o2) throws ClassCastException {
    if(Preference.ASCENDING_ORDER.equals(order_)) {
      return ((String) o1).compareToIgnoreCase((String) o2) ; 
    }
    return -1 * ((String) o1).compareToIgnoreCase((String) o2) ;
  }
}
