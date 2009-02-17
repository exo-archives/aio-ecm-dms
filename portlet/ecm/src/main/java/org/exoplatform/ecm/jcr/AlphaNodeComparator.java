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
