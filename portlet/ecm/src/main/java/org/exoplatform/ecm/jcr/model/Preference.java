package org.exoplatform.ecm.jcr.model;

public class Preference {
      
  public static final String ALPHABETICAL_SORT = "Alphabetic" ;
  public static final String TYPE_SORT = "Type" ;
  public static final String PROPERTY_SORT = "Property" ;
  public static final String SHOW_NON_DOCUMENTTYPE = "showNonDocumentType" ;
  public static final String ASCENDING_ORDER = "Ascending" ;
  public static final String DESCENDING_ORDER = "Descending" ;
    
  private boolean jcrEnable = false;
  private String sort = ALPHABETICAL_SORT ;
  private String order = ASCENDING_ORDER ;
  private String customOrderByProperty = "" ;
  private String allowCreateFoder = "" ;
  private boolean showSideBar = false ;
  private boolean isShowNonDocumentType = false ;
  private boolean isShowPreferenceDocuments = false ;
  private boolean isEmpty = true ;
    
  public boolean isJcrEnable() { return jcrEnable ; }
  public void setJcrEnable(boolean b) { jcrEnable = b ; }

  public String getSort() { return sort ; }
  public void setSort(String s) { sort = s ; }
  
  public String getOrder() { return order ; }
  public void setOrder(String s) { order = s ; }
  
  public boolean isShowSideBar() { return showSideBar ; }
  public void setShowSideBar(boolean b) { showSideBar = b ; }
  
  public boolean isShowNonDocumentType() { return isShowNonDocumentType ; }
  public void setShowNonDocumentType( boolean b) { isShowNonDocumentType = b ; }
  
  public boolean isShowPreferenceDocuments() { return isShowPreferenceDocuments ; }
  public void setShowPreferenceDocuments(boolean b) { isShowPreferenceDocuments = b ; }
  
  public boolean isEmpty() { return isEmpty ; }
  public void setEmpty(boolean b) { isEmpty = b ; }
  
  public String getAllowCreateFoder() { return allowCreateFoder ; }
  public void setAllowCreateFoder(String s) { allowCreateFoder = s ; }
  
  public String getProperty() { return customOrderByProperty ; }
  public void setProperty(String s) { customOrderByProperty = s ; }
  
}
