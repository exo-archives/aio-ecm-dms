package org.exoplatform.ecm.jcr.model;

public class Preference {
      
  public static final String SORT_BY_NODENAME = "Alphabetic" ;
  public static final String SORT_BY_NODETYPE= "Type" ;
  public static final String SORT_BY_CREATED_DATE= "CreatedDate" ;
  public static final String SORT_BY_MODIFIED_DATE= "ModifiedDate" ;
  public static final String PROPERTY_SORT = "Property" ;
  
  public static final String SHOW_NON_DOCUMENTTYPE = "showNonDocumentType" ;
  public static final String ASCENDING_ORDER = "Ascending" ;
  public static final String DESCENDING_ORDER = "Descending" ;
    
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
