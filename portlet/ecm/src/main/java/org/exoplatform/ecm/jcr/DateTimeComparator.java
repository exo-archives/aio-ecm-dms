/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.jcr;

import javax.jcr.Node;

import org.exoplatform.ecm.jcr.model.Preference;

import java.util.Calendar;
import java.util.Comparator;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Sep 6, 2007  
 */
public class DateTimeComparator implements Comparator<Node>{

  private String propertyName_ ;
  private String order_;
  
  public DateTimeComparator(String property,String order) {
    this.propertyName_ = property;
    this.order_ = order ;
  }
  
  
  public int compare(Node node1, Node node2) {
    try{
      if(node1.hasProperty(propertyName_)  && node2.hasProperty(propertyName_)) {
        Calendar date1 = node1.getProperty(propertyName_).getDate();
        Calendar date2 = node2.getProperty(propertyName_).getDate();
        if(Preference.ASCENDING_ORDER.equals(order_)) {
          return date1.compareTo(date2) ;
        }
        return date2.compareTo(date1) ;
      }
    }catch (Exception e) {
      // TODO: handle exception
    }    
    return 0;
  }

}
