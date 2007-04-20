
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
  public static final String MIXIN_PROPERTY = "/node/mixin/";
  
  public String storeNode(String workspace, String nodetypeName, String storePath, Map inputProperties) throws Exception;
  
  public String storeNode(String nodetypeName, Node storeNode, Map inputProperties, boolean isAddNew) throws Exception;
  
  public void moveNode(String nodePath, String srcWorkspace, String destWorkspace, String destPath);  
  
  public void storeMixin(Node node, String mixinNodeType) throws Exception;
}
