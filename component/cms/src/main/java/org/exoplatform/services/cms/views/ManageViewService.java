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

  public void addView(String name, String permissions, String template, List tabs, String repository)throws Exception ;
  public Node getViewByName(String viewName, String repository) throws Exception;
  public List getButtons() throws Exception;  
  public List getAllViewByPermission(String permission, String repository) throws Exception;
  public Node getDefaultView(String repository) throws Exception;
  public Node getAdminView(String repository) throws Exception;

  public void removeView(String viewName, String repository) throws Exception;  
  public Node getViewHome(String repository) throws Exception ;  
  public List<ViewConfig> getAllViews(String repository) throws Exception;
  
  public boolean hasView(String name, String repository) throws Exception ;
  public Node getTemplateHome(String homeAlias, String repository) throws Exception;
  public List<Node> getAllTemplates(String homeAlias, String repository) throws Exception;
  public Node getTemplate(String path, String repository) throws Exception; 
  public String addTemplate(String name, String content, String homeAlias, String repository)throws Exception ;
  public void removeTemplate(String templatePath, String repository) throws Exception  ;

  public void addTab(Node view, String name, String buttons) throws Exception ; 
  public void init(String repository) throws Exception ;

}
