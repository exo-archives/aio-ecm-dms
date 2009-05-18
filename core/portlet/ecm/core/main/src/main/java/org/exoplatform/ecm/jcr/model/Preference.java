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
package org.exoplatform.ecm.jcr.model;

public class Preference {
      
  public static final String SORT_BY_NODENAME = "Alphabetic" ;
  public static final String SORT_BY_NODETYPE= "Type" ;
  public static final String SORT_BY_CREATED_DATE= "CreatedDate" ;
  public static final String SORT_BY_MODIFIED_DATE= "ModifiedDate" ;
  public static final String PROPERTY_SORT = "Property" ;
  public static final String SORT_BY_OWNER = "Owner";
  public static final String SORT_BY_VERSIONABLE = "Versionable";
  public static final String SORT_BY_AUDITING = "Auditing";

  public static final String[] SORT_BY_SINGLEVALUE_PROPERTY = {"SingleValueProperty"};
  
  public static final String SHOW_NON_DOCUMENTTYPE = "showNonDocumentType" ;
  public static final String ASCENDING_ORDER = "Ascending" ;
  public static final String DESCENDING_ORDER = "Descending" ;
  
  public static final String BLUE_DOWN_ARROW = "BlueDownArrow" ;
  public static final String BLUE_UP_ARROW = "BlueUpArrow" ;
    
  private String sortType = SORT_BY_NODENAME ;
  private String order = ASCENDING_ORDER ;  
  private String allowCreateFoder = "" ;
  
  private boolean jcrEnable = false;  
  private boolean showSideBar = false ;
  private boolean isShowNonDocumentType = false ;
  private boolean isShowPreferenceDocuments = false ;
  private boolean isShowHiddenNode = false ;
  
  private int nodesPerPage = 20;
    
  public boolean isJcrEnable() { return jcrEnable ; }
  public void setJcrEnable(boolean b) { jcrEnable = b ; }

  public String getSortType() { return sortType ; }
  public void setSortType(String s) { sortType = s ; }
  
  public String getOrder() { return order ; }
  public void setOrder(String s) { order = s ; }
  
  public boolean isShowSideBar() { return showSideBar ; }
  public void setShowSideBar(boolean b) { showSideBar = b ; }
  
  public boolean isShowNonDocumentType() { return isShowNonDocumentType ; }
  public void setShowNonDocumentType( boolean b) { isShowNonDocumentType = b ; }
  
  public boolean isShowPreferenceDocuments() { return isShowPreferenceDocuments ; }
  public void setShowPreferenceDocuments(boolean b) { isShowPreferenceDocuments = b ; }
  
  public boolean isShowHiddenNode() { return isShowHiddenNode ; }
  public void setShowHiddenNode(boolean b) { isShowHiddenNode = b ; }
  
  public String getAllowCreateFoder() { return allowCreateFoder ; }
  public void setAllowCreateFoder(String s) { allowCreateFoder = s ; }
  
  public int getNodesPerPage(){return nodesPerPage ; }
  public void setNodesPerPage(int number) { this.nodesPerPage = number ; }
}
