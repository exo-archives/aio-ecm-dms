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
    Map inputValues = (Map) context;       
    println(" Pre Node Save, a good form validator");
    Set keys = inputValues.keySet();
    for(String key : keys) {
      JcrInputProperty prop = (JcrInputProperty) inputValues.get(key);
      println("   --> "+prop.getJcrPath());
    }
  }

  public void setParams(String[] params) {}

}