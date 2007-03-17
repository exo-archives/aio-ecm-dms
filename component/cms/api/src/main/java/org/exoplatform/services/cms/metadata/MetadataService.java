package org.exoplatform.services.cms.metadata;

import java.util.List;


/**
 * Author : Hung Nguyen Quang
 *          nguyenkequanghung@yahoo.com
 */

public interface MetadataService {
  
	public List getMetadataList() throws Exception;
  public void addMetadata(String nodetype, boolean isDialog, String role, String content, boolean isAddNew) throws Exception;
  public void removeMetadata(String nodetype) throws Exception;
  public List getMixinNodeTypes() throws Exception;
  public String getMetadataTemplate(String name, boolean isDialog) throws Exception;  
  public String getMetadataPath(String name, boolean isDialog) throws Exception;
  public String getMetadataRoles(String name, boolean isDialog) throws Exception;
  public boolean hasMetadata(String name) throws Exception;
}
