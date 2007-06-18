package org.exoplatform.services.cms.metadata;

import java.util.List;

import javax.jcr.nodetype.NodeType;


/**
 * Author : Hung Nguyen Quang
 *          nguyenkequanghung@yahoo.com
 */

public interface MetadataService {  
	public List<String> getMetadataList(String repository) throws Exception;
  public List<NodeType> getAllMetadatasNodeType(String repository) throws Exception ;
  public void addMetadata(String nodetype, boolean isDialog, String role, String content, boolean isAddNew, String repository) throws Exception;
  public void removeMetadata(String nodetype, String repository) throws Exception;
  public List<String> getExternalMetadataType(String repository) throws Exception ;
  public String getMetadataTemplate(String name, boolean isDialog, String repository) throws Exception;  
  public String getMetadataPath(String name, boolean isDialog, String repository) throws Exception;
  public String getMetadataRoles(String name, boolean isDialog, String repository) throws Exception;
  public boolean hasMetadata(String name, String repository) throws Exception;
  public void init(String repository) throws Exception ;
}
