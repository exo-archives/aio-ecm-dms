/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.folksonomy.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Xuan Hoa Pham
 *          hoapham@exoplatform.com
 * 					phamvuxuanhoa@gmail.com
 * Dec 8, 2006  
 */
public class TagStyleConfig {
  
  private List<HtmlTagStyle> tagStyleList = new ArrayList<HtmlTagStyle>() ;
  private String repository ;
  
  public List<HtmlTagStyle> getTagStyleList() { return this.tagStyleList ; }
  public void setTagStyleList(List<HtmlTagStyle> list) { this.tagStyleList = list ; }
  
  public void setRepository(String repo) { repository = repo ; }
  public String getRepository() { return repository ; }
  
  static public class HtmlTagStyle {
    String name ;
    String tagRate ;
    String htmlStyle ;
    String description ;
    
    public String getDescription() { return description; }
    public void setDescription(String description) { 
      this.description = description;
    }
    public String getHtmlStyle() { return htmlStyle; }
    public void setHtmlStyle(String htmlStyle) {
      this.htmlStyle = htmlStyle;
    }
    
    public String getName() { return name; }
    public void setName(String name) {
      this.name = name;
    }
    
    public String getTagRate() { return tagRate; }
    public void setTagRate(String tagRate) {
      this.tagRate = tagRate;
    }
  }
  
}
