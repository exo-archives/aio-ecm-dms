/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.jcr ;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.resolver.ResourceResolver;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan08@users.sourceforge.net
 * Mar 15, 2006
 */
public class JCRResourceResolver extends ResourceResolver {
  private Session session_ ;
  private String propertyName_ ;
  
  public JCRResourceResolver(Session session, String propertyName) {
    session_ = session ;
    propertyName_ = propertyName ;
  }

  @SuppressWarnings("unused")
  public URL getResource(String url) throws Exception {
    throw new Exception("This method is not  supported") ;  
  }
  
  public InputStream getInputStream(String url) throws Exception  {
    Node node = (Node)session_.getItem(removeScheme(url)) ;
    return new ByteArrayInputStream(node.getProperty(propertyName_).getString().getBytes()) ;
  }
  
  @SuppressWarnings("unused")
  public List<URL> getResources(String url) throws Exception {
    throw new Exception("This method is not  supported") ;
  }

  public List<InputStream> getInputStreams(String url) throws Exception {
    ArrayList<InputStream>  inputStreams = new ArrayList<InputStream>(1) ;
    inputStreams.add(getInputStream(url)) ;
    return inputStreams ;
  }

  @SuppressWarnings("unused")
  public boolean isModified(String url, long lastAccess) {  return false ; }
  
  public String createResourceId(String url) { return url ; }
  
  public String getResourceScheme() {  return "jcr:" ; }
  
}