/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.watch.impl;

/**
 * Created by The eXo Platform SAS
 * Author : Xuan Hoa Pham
 *          hoapham@exoplatform.com
 * 					phamvuxuanhoa@gmail.com
 * Dec 6, 2006  
 */
public class MessageConfig {
  private String sender ;
  private String subject ;
  private String mimeType ;
  private String content ;
  
  public String getContent() { return content; }
  public void setContent(String content) {this.content = content; }
  
  public String getMimeType() { return mimeType; }
  public void setMimeType(String mimeType) { this.mimeType = mimeType; }
  
  public String getSender() { return sender; }
  public void setSender(String sender) { this.sender = sender; }
  
  public String getSubject() { return subject; }
  public void setSubject(String subject) { this.subject = subject;}
    
  
}
