/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

import java.util.*;

import org.exoplatform.services.cms.scripts.CmsScript;
import org.exoplatform.services.cms.JcrInputProperty;

public class PreNodeSaveInterceptor implements CmsScript {
  
  public PreNodeSaveInterceptor() {
  }
  
  public void execute(Object context) {
    String path = (String) context;       
    println(" Pre Node Save, a good form validator");
  }

  public void setParams(String[] params) {}

}