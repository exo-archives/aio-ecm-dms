package org.exoplatform.services.cms.metadata;

import java.util.List;

import javax.jcr.nodetype.NodeType;


/**
 * Author : Hung Nguyen Quang
 *          nguyenkequanghung@yahoo.com
 */

public interface MetadataService {
  
	public List<String> getMetadataList() throws Exception;
  public List<NodeType> getAllMetadatasNodeType() throws Exception ;
  public NodeType getMetadataTypeByName(String metadataTypeName) throws Exception ;
  public void addMetadata(String nodetype, boolean isDialog, String role, String content, boolean isAddNew) throws Exception;
  public void removeMetadata(String nodetype) throws Exception;
  public List<String> getExternalMetadataType() throws Exception ;
  public String getMetadataTemplate(String name, boolean isDialog) throws Exception;  
  public String getMetadataPath(String name, boolean isDialog) throws Exception;
  public String getMetadataRoles(String name, boolean isDialog) throws Exception;
  public boolean hasMetadata(String name) throws Exception;
}
