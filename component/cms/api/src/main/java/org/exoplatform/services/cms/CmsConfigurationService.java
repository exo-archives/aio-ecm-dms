/*
 * Created on Mar 18, 2005
 */
package org.exoplatform.services.cms;

/**
 * @author benjaminmestrallet
 */
public interface CmsConfigurationService {

  public String getWorkspace();
  
  public String getDraftWorkspace();
  
  public String getBackupWorkspace();
  
  public String getContentLocation();
  
  public String getJcrPath(String alias);
  
}
