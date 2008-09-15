/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ecm.webui.comparator;

import java.util.Calendar;
import java.util.Comparator;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Sep 6, 2007  
 */
public class DateTimeComparator implements Comparator<Node>{

  public static final String ASCENDING_ORDER = "Ascending" ;
  public static final String DESCENDING_ORDER = "Descending" ;
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
        if(ASCENDING_ORDER.equals(order_)) {
          return date1.compareTo(date2) ;
        }
        return date2.compareTo(date1) ;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }    
    return 0;
  }

}
