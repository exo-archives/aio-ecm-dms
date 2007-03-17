package org.exoplatform.ecm.jcr;

import java.util.Comparator;
import java.util.StringTokenizer;

import org.exoplatform.ecm.jcr.model.Preference;

public class TypeNodeComparator implements Comparator {
	
	private String order_;
	
	public TypeNodeComparator(String pOrder) {
		order_ = pOrder ;
	}

  public int compare(Object o1, Object o2) throws ClassCastException {
    StringTokenizer key1 = new StringTokenizer((String) o1, "//") ;
    StringTokenizer key2 = new StringTokenizer((String) o2, "//") ;
    String type1 = key1.nextToken() ;
    String type2 = key2.nextToken() ;
    int res = 0 ;
    if ("folder".equals(type1) && "folder".equals(type2)) {
    	// mime type
    	key1.nextToken() ;
    	key2.nextToken() ;
    	// sort by name
    	res = key1.nextToken().compareToIgnoreCase(key2.nextToken());
    	if(Preference.ASCENDING_ORDER.equals(order_)) return res ;
	    return -res ;
    } else if ("file".equals(type1) && "file".equals(type2)) {
    	String mimeType1 = key1.nextToken() ; 
    	String mimeType2 = key2.nextToken() ;
    	// sort by mime type
      res = mimeType1.compareToIgnoreCase(mimeType2) ;
      if (res == 0) return key1.nextToken().compareToIgnoreCase(key2.nextToken()) ; 
          // same mime type -> sort by name
      else if(Preference.ASCENDING_ORDER.equals(order_)) return res ;
      else return -res ;
    } else {
    	if(Preference.ASCENDING_ORDER.equals(order_)) res = 1 ;
      else res = -1 ;
    	// folder before file in ascending order
    	if ("folder".equals(type1)) return -res ;
    	return res ;
    }
  }
}
