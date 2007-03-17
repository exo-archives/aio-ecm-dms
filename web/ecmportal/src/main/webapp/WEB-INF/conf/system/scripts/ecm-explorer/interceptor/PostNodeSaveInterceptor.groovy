/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

import java.util.Map;

import org.exoplatform.services.cms.scripts.CmsScript;

public class PostNodeSaveInterceptor implements CmsScript {
  
  public PostNodeSaveInterceptor() {
  }
  
  public void execute(Object context) {
    String path = (String) context;       

    //TODO Should send an email
    println("Post node save interceptor, created node: "+path);
  }

  public void setParams(String[] params) {}

}