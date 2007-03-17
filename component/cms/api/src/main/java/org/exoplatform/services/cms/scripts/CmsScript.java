package org.exoplatform.services.cms.scripts;

public interface CmsScript {

  public void execute(Object context);

  public void setParams(String[] params);
  
}
