/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.views;

import java.util.List;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Quang Hung
 *          nguyenkequanghung@yahoo.com
 * Feb 27, 2006
 */
public interface ManageViewService {

  public void addView(String name, String permissions, String template, List tabs)throws Exception ;
  public Node getViewByName(String viewName) throws Exception;
  public List getButtons() throws Exception;  
  public List getAllViewByPermission(String permission) throws Exception;
  public Node getDefaultView() throws Exception;
  public Node getAdminView() throws Exception;

  public void removeView(String viewName) throws Exception;  
  public Node getViewHome() throws Exception ;  
  public List getAllViews() throws Exception;
  
  public boolean hasView(String name) throws Exception ;
  public Node getTemplateHome(String homeAlias) throws Exception;
  public List<Node> getAllTemplates(String homeAlias) throws Exception;
  public Node getTemplate(String path) throws Exception; 
  public void addTemplate(String name, String content, String homeAlias)throws Exception ;
  public void removeTemplate(String templatePath) throws Exception  ;

  public void addTab(Node view, String name, String buttons) throws Exception ; 

}
