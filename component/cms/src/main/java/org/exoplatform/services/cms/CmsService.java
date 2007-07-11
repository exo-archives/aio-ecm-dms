
/*
 * Created on Apr 3, 2005
 */
package org.exoplatform.services.cms;

import java.util.Map;

import javax.jcr.Node;

/**
 * @author benjaminmestrallet
 */
public interface CmsService {
  
  public static final String NODE = "/node";  
  
  public String storeNode(String workspace, String nodetypeName, String storePath, Map inputProperties,String repository) throws Exception;
  
  public String storeNode(String nodetypeName, Node storeNode, Map inputProperties, boolean isAddNew,String repository) throws Exception;
  
  public void moveNode(String nodePath, String srcWorkspace, String destWorkspace, String destPath, String repository);  
  
  public void storeMixin(Node node, String mixinNodeType) throws Exception;
}
